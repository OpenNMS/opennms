/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.kafka.producer.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Row;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.features.kafka.producer.collection.KafkaPersister;
import org.opennms.features.kafka.producer.collection.KafkaPersisterActivator;
import org.opennms.features.kafka.producer.collection.KafkaPersisterFactory;
import org.opennms.features.kafka.producer.collection.MetricFilter;
import org.opennms.features.kafka.producer.collection.ResourceTreeMapper;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.expression.ParseException;

import com.google.common.base.Strings;

/**
 * Evaluates a Kafka producer metric filter against the resources found in the resource tree,
 * showing which resources would be forwarded to Kafka. No collection is triggered: the command
 * only looks at what has already been persisted for the selected node(s).
 */
@Command(scope = "opennms", name = "kafka-evaluate-metric-filter",
        description = "Evaluates a Kafka producer metricFilter expression against the resources in the resource tree "
                + "and shows which of them would be forwarded to Kafka. Uses the configured metricFilter when no expression is given. "
                + "Does not trigger a collection: results reflect what has been persisted so far, not what the next collection will produce.")
@Service
public class EvaluateMetricFilter implements Action {

    static final String PRODUCER_PID = KafkaPersisterActivator.PRODUCER_CONFIG;
    static final String METRIC_FILTER_PROPERTY = "metricFilter";
    static final String METRIC_TOPIC_PROPERTY = "metricTopic";

    @Reference
    NodeDao nodeDao;

    @Reference
    ResourceDao resourceDao;

    @Reference
    SessionUtils sessionUtils;

    @Reference
    ConfigurationAdmin configAdmin;

    @Reference
    BundleContext bundleContext;

    @Option(name = "-n", aliases = "--node", description = "Node ID or FS:FID. When omitted, all nodes with resources are walked, up to --limit resources.")
    String nodeCriteria;

    @Option(name = "-l", aliases = "--limit", description = "Maximum number of resources to evaluate, also when a node is given. 0 for no limit.")
    int limit = 100;

    @Option(name = "-v", aliases = "--verbose", description = "Print every property the expression can see for each resource.")
    boolean verbose;

    @Option(name = "-j", aliases = "--json", description = "Print the surviving resources as the JSON the producer would send with metrics.useJson=true.")
    boolean json;

    @Option(name = "-c", aliases = "--show-config", description = "Print the metric forwarding configuration, including the configured and running filter.")
    boolean showConfig;

    @Argument(index = 0, name = "expression", description = "A SpEL metric filter expression. Defaults to the configured metricFilter.", required = false)
    String spelExpression;

    private final ResourceTreeMapper mapper = new ResourceTreeMapper();

    /** Tree resources skipped because the producer would not forward them, with the reason. */
    private final List<String> skipped = new ArrayList<>();

    /** A resource from the tree together with its protobuf rendering and the filter result. */
    static final class Evaluation {
        final OnmsResource resource;
        final CollectionSetProtos.CollectionSetResource proto;
        final MetricFilter.Result result;

        Evaluation(OnmsResource resource, CollectionSetProtos.CollectionSetResource proto, MetricFilter.Result result) {
            this.resource = Objects.requireNonNull(resource);
            this.proto = Objects.requireNonNull(proto);
            this.result = result;
        }
    }

    @Override
    public Object execute() throws Exception {
        final Dictionary<String, Object> config = readProducerConfig();
        if (showConfig) {
            printConfig(config);
        }

        final String expressionSource;
        String expressionString = spelExpression;
        if (!Strings.isNullOrEmpty(expressionString)) {
            expressionSource = "command line";
        } else {
            expressionString = configuredFilter(config);
            expressionSource = "configuration (" + PRODUCER_PID + ")";
        }

        final MetricFilter filter;
        try {
            filter = MetricFilter.parse(expressionString);
        } catch (ParseException e) {
            System.out.printf("Failed to parse expression '%s': %s\n", expressionString, e.getMessage());
            return null;
        }
        if (filter == null) {
            System.out.printf("No metric filter is set (%s): every resource would be forwarded.\n", expressionSource);
        } else {
            System.out.printf("SPEL Expression: %s (from %s)\n", filter.getExpressionString(), expressionSource);
        }

        final List<Evaluation> evaluations = sessionUtils.withReadOnlyTransaction(() -> evaluate(filter));
        if (evaluations == null) {
            return null;
        }
        if (evaluations.isEmpty()) {
            System.out.println("No resources found in the resource tree. Nothing has been collected yet, or the node has no persisted data.");
            return null;
        }

        printTable(evaluations);
        if (verbose) {
            printVerbose(evaluations);
        }
        printSummary(evaluations);
        if (json) {
            printJson(evaluations);
        }
        printCaveats();
        return null;
    }

    /**
     * Walks the resource tree and evaluates the filter against every resource found. Must run inside a transaction.
     *
     * @return the evaluations, or null if the walk could not be performed (for instance the node does not exist)
     */
    List<Evaluation> evaluate(final MetricFilter filter) {
        final List<Evaluation> evaluations = new ArrayList<>();
        if (!Strings.isNullOrEmpty(nodeCriteria)) {
            final OnmsNode node;
            try {
                node = nodeDao.get(nodeCriteria);
            } catch (RuntimeException e) {
                System.out.printf("Invalid node criteria '%s': expected a node ID or FS:FID.\n", nodeCriteria);
                return null;
            }
            if (node == null) {
                System.out.printf("No node found with criteria: %s\n", nodeCriteria);
                return null;
            }
            System.out.printf("Node: %s (id=%d, foreignSource=%s, foreignId=%s, location=%s)\n",
                    node.getLabel(), node.getId(), node.getForeignSource(), node.getForeignId(),
                    node.getLocation() != null ? node.getLocation().getLocationName() : null);
            walk(resourceDao.getResourceForNode(node), node, filter, evaluations);
        } else {
            for (OnmsResource top : resourceDao.findTopLevelResources()) {
                if (limitReached(evaluations)) {
                    break;
                }
                final OnmsEntity entity = top.getEntity();
                walk(top, entity instanceof OnmsNode ? (OnmsNode) entity : null, filter, evaluations);
            }
        }
        return evaluations;
    }

    private void walk(OnmsResource parent, OnmsNode node, MetricFilter filter, List<Evaluation> evaluations) {
        for (OnmsResource child : parent.getChildResources()) {
            if (limitReached(evaluations)) {
                return;
            }
            final OnmsNode resourceNode = node != null ? node : ResourceTreeMapper.findNode(child);
            final String skipReason = mapper.skipReason(child, resourceNode);
            if (skipReason != null) {
                skipped.add(child.getId() + ": " + skipReason);
                continue;
            }
            final CollectionSetProtos.CollectionSetResource proto = mapper.toProto(child, resourceNode);
            evaluations.add(new Evaluation(child, proto, filter != null ? filter.evaluate(proto) : null));
        }
    }

    private boolean limitReached(List<Evaluation> evaluations) {
        return limit > 0 && evaluations.size() >= limit;
    }

    private void printTable(List<Evaluation> evaluations) {
        final ShellTable table = new ShellTable();
        table.column("Type");
        table.column("Resource");
        table.column("Instance");
        table.column("ifIndex");
        table.column("Node");
        table.column("Location");
        table.column("Numeric");
        table.column("Strings");
        table.column("Result");
        for (Evaluation e : evaluations) {
            final MetricFilter.ResourceFilterContext ctx = new MetricFilter.ResourceFilterContext(e.proto);
            final Row row = table.addRow();
            row.addContent(nullToDash(ctx.getResourceTypeName()));
            // The resource id the producer sends, which is not the id the resource tree uses
            row.addContent(nullToDash(Strings.emptyToNull(ctx.getResourceId())));
            row.addContent(nullToDash(ctx.getInstance()));
            row.addContent(nullToDash(ctx.getIfIndex()));
            row.addContent(ctx.getNodeId() != null ? ctx.getNodeId() + " (" + nullToDash(ctx.getNodeLabel()) + ")" : "-");
            row.addContent(nullToDash(ctx.getLocation()));
            row.addContent(e.proto.getNumericCount());
            row.addContent(e.proto.getStringCount());
            row.addContent(describe(e.result));
        }
        table.print(System.out);
    }

    private void printVerbose(List<Evaluation> evaluations) {
        System.out.println();
        for (Evaluation e : evaluations) {
            System.out.printf("%s: %s\n", describe(e.result), e.resource.getId());
            System.out.printf("    %-16s = %s\n", "treeResource", e.resource.getId());
            for (Map.Entry<String, Object> entry : new MetricFilter.ResourceFilterContext(e.proto).toMap().entrySet()) {
                System.out.printf("    %-16s = %s\n", entry.getKey(), entry.getValue());
            }
            if (e.result != null && e.result.getCause() != null) {
                System.out.printf("    %-16s = %s\n", "error", e.result.getCause().getMessage());
            }
        }
    }

    private void printSummary(List<Evaluation> evaluations) {
        long forwarded = 0, dropped = 0, errors = 0;
        for (Evaluation e : evaluations) {
            if (e.result == null || e.result.getOutcome() == MetricFilter.Outcome.FORWARD) {
                forwarded++;
            } else if (e.result.getOutcome() == MetricFilter.Outcome.DROP) {
                dropped++;
            } else {
                errors++;
            }
        }
        System.out.println();
        System.out.printf("Resources: %d, forwarded: %d, dropped: %d, errors (forwarded anyway): %d\n",
                evaluations.size(), forwarded + errors, dropped, errors);
        if (forwarded + errors == 0) {
            System.out.println("No resource matches the filter: the producer would not send any message for these resources.");
        }
        if (!skipped.isEmpty()) {
            System.out.printf("Skipped %d resource(s) the producer never forwards:\n", skipped.size());
            for (String s : skipped) {
                System.out.printf("    %s\n", s);
            }
        }
        if (limitReached(evaluations)) {
            System.out.printf("Stopped after %d resources. Use --limit to evaluate more.\n", limit);
        }
    }

    private void printJson(List<Evaluation> evaluations) throws IOException {
        final CollectionSetProtos.CollectionSet.Builder builder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(System.currentTimeMillis());
        for (Evaluation e : evaluations) {
            if (e.result == null || e.result.isForwarded()) {
                builder.addResource(e.proto);
            }
        }
        System.out.println();
        System.out.println("Surviving resources as JSON (attribute values are placeholders, the tree does not hold values):");
        System.out.println(KafkaPersister.toJson(builder.build()));
    }

    private static void printCaveats() {
        System.out.println();
        System.out.println("Note: results are derived from the persisted resource tree, not from a live collection.");
        System.out.println("      Resources that are no longer collected remain listed until their data is removed, and new");
        System.out.println("      services appear only after their first collection. Response time resources carry node");
        System.out.println("      information, but their location property is only set for nodes outside the Default location.");
        System.out.println("      The Resource column shows the id the producer sends, which differs from the resource tree id.");
    }

    private void printConfig(Dictionary<String, Object> config) {
        System.out.printf("Configuration (%s):\n", PRODUCER_PID);
        System.out.printf("    %-18s = %s\n", KafkaPersisterActivator.FORWARD_METRICS, valueOf(config, KafkaPersisterActivator.FORWARD_METRICS));
        System.out.printf("    %-18s = %s\n", METRIC_TOPIC_PROPERTY, valueOf(config, METRIC_TOPIC_PROPERTY));
        System.out.printf("    %-18s = %s\n", METRIC_FILTER_PROPERTY, valueOf(config, METRIC_FILTER_PROPERTY));
        final KafkaPersisterFactory factory = findKafkaPersisterFactory();
        if (factory != null) {
            final String running = factory.getMetricFilter();
            System.out.printf("Running metric filter: %s\n", Strings.isNullOrEmpty(running) ? "(none)" : running);
            if (!Objects.equals(Strings.nullToEmpty(running), Strings.nullToEmpty(configuredFilter(config)))) {
                System.out.println("WARNING: the running filter differs from the configured one. The metric filter is read when the");
                System.out.println("         feature starts, restart the opennms-kafka-producer feature to apply the configuration.");
            }
        } else {
            System.out.println("Running metric filter: the Kafka persister is not registered (forward.metrics is false, or the feature has not started).");
        }
        System.out.println();
    }

    /**
     * Looks up the Kafka persister factory registered by the activator. A plain {@code @Reference} cannot be used:
     * the Karaf shell resolves the initial service without applying the reference filter, so with several
     * {@link PersisterFactory} services registered it may hand back the wrong one.
     */
    KafkaPersisterFactory findKafkaPersisterFactory() {
        try {
            final Collection<ServiceReference<PersisterFactory>> references = bundleContext.getServiceReferences(PersisterFactory.class, "(strategy=kafka)");
            for (ServiceReference<PersisterFactory> reference : references) {
                final PersisterFactory service = bundleContext.getService(reference);
                try {
                    if (service instanceof KafkaPersisterFactory) {
                        return (KafkaPersisterFactory) service;
                    }
                } finally {
                    bundleContext.ungetService(reference);
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    private Dictionary<String, Object> readProducerConfig() {
        try {
            final Configuration[] configurations = configAdmin.listConfigurations("(service.pid=" + PRODUCER_PID + ")");
            if (configurations != null && configurations.length > 0) {
                return configurations[0].getProperties();
            }
        } catch (IOException | InvalidSyntaxException e) {
            System.out.printf("Failed to read configuration %s: %s\n", PRODUCER_PID, e.getMessage());
        }
        return null;
    }

    private static String configuredFilter(Dictionary<String, Object> config) {
        final Object value = config != null ? config.get(METRIC_FILTER_PROPERTY) : null;
        return value instanceof String ? (String) value : null;
    }

    private static String valueOf(Dictionary<String, Object> config, String key) {
        final Object value = config != null ? config.get(key) : null;
        return value == null ? "(not set)" : value.toString();
    }

    private static String describe(MetricFilter.Result result) {
        if (result == null) {
            return "FORWARD (no filter)";
        }
        switch (result.getOutcome()) {
            case FORWARD:
                return "FORWARD";
            case DROP:
                return "DROP";
            default:
                return "ERROR (forwarded): " + (result.getCause() != null ? result.getCause().getMessage() : "unknown");
        }
    }

    private static Object nullToDash(Object value) {
        return value == null ? "-" : value;
    }
}
