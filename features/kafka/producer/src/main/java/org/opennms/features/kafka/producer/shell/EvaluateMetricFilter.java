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
import java.util.Collections;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
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
    static final String DISABLE_SPLITTING_PROPERTY = "disable.metrics.splitting";
    static final String USE_JSON_PROPERTY = "metrics.useJson";

    /** Matches expressions that look at the instance property, whose tree value may differ from the producer's. */
    private static final Pattern INSTANCE_REFERENCE = Pattern.compile("\\binstance\\b|getInstance\\s*\\(");

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

    @Reference(optional = true)
    ResourceTypesDao resourceTypesDao;

    @Option(name = "-n", aliases = "--node", description = "Node ID or FS:FID. When omitted, all nodes with resources are walked, up to --limit resources.")
    String nodeCriteria;

    @Option(name = "-l", aliases = "--limit", description = "Maximum number of resources to evaluate, also when a node is given. 0 for no limit.")
    int limit = 100;

    @Option(name = "-v", aliases = "--verbose", description = "Print every property the expression can see for each resource.")
    boolean verbose;

    @Option(name = "-j", aliases = "--json", description = "Print the surviving resources in the JSON encoding the producer uses with metrics.useJson=true. Shows the shape only: attribute names and types, no values.")
    boolean json;

    @Option(name = "-c", aliases = "--show-config", description = "Print the metric forwarding configuration, including the configured and running filter.")
    boolean showConfig;

    @Argument(index = 0, name = "expression", description = "A SpEL metric filter expression. Defaults to the configured metricFilter.", required = false)
    String spelExpression;

    private final ResourceTreeMapper mapper = new ResourceTreeMapper();

    /** Tree resources skipped because the producer would not forward them, with the reason. */
    private final List<String> skipped = new ArrayList<>();

    /** Resource types whose storage strategy names resources differently from their index. */
    private Set<String> labelNamedTypes = Collections.emptySet();

    /** A resource from the tree together with its protobuf rendering and the filter result. */
    static final class Evaluation {
        /** The tree resources behind this row: several when node-level directories of other collectors were merged. */
        final List<OnmsResource> sources;
        final CollectionSetProtos.CollectionSetResource proto;
        final MetricFilter.Result result;
        /** True when the expression looks at instance and the tree cannot know the instance the producer sends. */
        final boolean uncertain;

        Evaluation(List<OnmsResource> sources, CollectionSetProtos.CollectionSetResource proto, MetricFilter.Result result, boolean uncertain) {
            this.sources = Objects.requireNonNull(sources);
            this.proto = Objects.requireNonNull(proto);
            this.result = result;
            this.uncertain = uncertain;
        }

        Evaluation merge(OnmsResource source, CollectionSetProtos.CollectionSetResource other) {
            final List<OnmsResource> merged = new ArrayList<>(sources);
            merged.add(source);
            return new Evaluation(merged, proto.toBuilder()
                    .addAllNumeric(other.getNumericList())
                    .addAllString(other.getStringList())
                    .build(), result, uncertain);
        }

        String treeIds() {
            return sources.stream().map(r -> r.getId().toString()).collect(Collectors.joining(", "));
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

        labelNamedTypes = findLabelNamedResourceTypes();
        final List<Evaluation> evaluations = sessionUtils.withReadOnlyTransaction(() -> evaluate(filter));
        if (evaluations == null) {
            return null;
        }
        if (evaluations.isEmpty()) {
            if (skipped.isEmpty()) {
                System.out.println("No resources found in the resource tree. Nothing has been collected yet, or the node has no persisted data.");
            } else {
                System.out.println("No resources the producer would forward were found in the resource tree.");
                printSkipped();
            }
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
            // Walk node by node and stop at the limit, instead of findTopLevelResources(), which
            // probes the storage of every node before returning anything
            for (Integer nodeId : new TreeSet<>(nodeDao.getNodeIds())) {
                if (limitReached(evaluations)) {
                    break;
                }
                final OnmsNode node = nodeDao.get(nodeId);
                if (node != null) {
                    walk(resourceDao.getResourceForNode(node), node, filter, evaluations);
                }
            }
        }
        return evaluations;
    }

    /**
     * Resource types whose storage strategy is not the index strategy: the resource tree knows them by
     * their storage label, while the producer sends the raw index as the instance.
     */
    Set<String> findLabelNamedResourceTypes() {
        if (resourceTypesDao == null) {
            return Collections.emptySet();
        }
        final Set<String> types = new TreeSet<>();
        for (ResourceType type : resourceTypesDao.getResourceTypes().values()) {
            if (type.getStorageStrategy() != null
                    && !Strings.isNullOrEmpty(type.getStorageStrategy().getClazz())
                    && !IndexStorageStrategy.class.getName().equals(type.getStorageStrategy().getClazz())) {
                types.add(type.getName());
            }
        }
        return types;
    }

    private boolean isUncertain(MetricFilter filter, CollectionSetProtos.CollectionSetResource proto) {
        return filter != null
                && proto.hasGeneric()
                && labelNamedTypes.contains(proto.getGeneric().getType())
                && INSTANCE_REFERENCE.matcher(filter.getExpressionString()).find();
    }

    private void walk(OnmsResource parent, OnmsNode node, MetricFilter filter, List<Evaluation> evaluations) {
        // Node-level resources of one node share a resource id: the node's own nodeSnmp directory and the
        // directories of non-SNMP collectors are forwarded as one kind of resource, so show them as one row
        final Map<String, Integer> nodeLevelRows = new LinkedHashMap<>();
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
            if (proto.hasNode()) {
                final Integer existing = nodeLevelRows.get(proto.getResourceId());
                if (existing != null) {
                    evaluations.set(existing, evaluations.get(existing).merge(child, proto));
                    continue;
                }
                nodeLevelRows.put(proto.getResourceId(), evaluations.size());
            }
            evaluations.add(new Evaluation(Collections.singletonList(child), proto, filter != null ? filter.evaluate(proto) : null, isUncertain(filter, proto)));
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
            row.addContent(e.uncertain ? ctx.getInstance() + " (label)" : nullToDash(ctx.getInstance()));
            row.addContent(nullToDash(ctx.getIfIndex()));
            row.addContent(ctx.getNodeId() != null ? ctx.getNodeId() + " (" + nullToDash(ctx.getNodeLabel()) + ")" : "-");
            row.addContent(nullToDash(ctx.getLocation()));
            row.addContent(e.proto.getNumericCount());
            row.addContent(e.proto.getStringCount());
            row.addContent(e.uncertain ? describe(e.result) + " (uncertain)" : describe(e.result));
        }
        table.print(System.out);
    }

    private void printVerbose(List<Evaluation> evaluations) {
        System.out.println();
        for (Evaluation e : evaluations) {
            System.out.printf("%s: %s\n", describe(e.result), e.treeIds());
            System.out.printf("    %-16s = %s\n", "treeResource", e.treeIds());
            for (Map.Entry<String, Object> entry : new MetricFilter.ResourceFilterContext(e.proto).toMap().entrySet()) {
                System.out.printf("    %-16s = %s\n", entry.getKey(), entry.getValue());
            }
            if (e.uncertain) {
                System.out.printf("    %-16s = %s\n", "uncertain", "instance is the storage label, the producer sends the raw index");
            }
            if (e.result != null && e.result.getCause() != null) {
                System.out.printf("    %-16s = %s\n", "error", e.result.getCause().getMessage());
            }
        }
    }

    private void printSummary(List<Evaluation> evaluations) {
        long forwarded = 0, dropped = 0, errors = 0, uncertain = 0;
        for (Evaluation e : evaluations) {
            if (e.uncertain) {
                uncertain++;
            }
            if (e.result == null || e.result.getOutcome() == MetricFilter.Outcome.FORWARD) {
                forwarded++;
            } else if (e.result.getOutcome() == MetricFilter.Outcome.DROP) {
                dropped++;
            } else {
                errors++;
            }
        }
        System.out.println();
        System.out.printf("Resources: %d, forwarded: %d, dropped: %d, errors (forwarded anyway): %d, uncertain: %d\n",
                evaluations.size(), forwarded + errors, dropped, errors, uncertain);
        if (uncertain > 0) {
            System.out.printf("%d result(s) are uncertain: the expression uses instance, and for resource types with a storage strategy other than %s (%s)\n",
                    uncertain, IndexStorageStrategy.class.getSimpleName(),
                    evaluations.stream().filter(e -> e.uncertain).map(e -> e.proto.getGeneric().getType()).distinct().sorted().collect(Collectors.joining(", ")));
            System.out.println("the resource tree only knows the storage label shown here, while the producer sends the raw index as the instance.");
        }
        if (forwarded + errors == 0) {
            System.out.println("No resource matches the filter: the producer would not send any message for these resources.");
        }
        printSkipped();
        if (limitReached(evaluations)) {
            System.out.printf("Stopped after %d resources. Use --limit to evaluate more.\n", limit);
        }
    }

    private void printSkipped() {
        if (!skipped.isEmpty()) {
            System.out.printf("Skipped %d resource(s) the producer never forwards:\n", skipped.size());
            for (String s : skipped) {
                System.out.printf("    %s\n", s);
            }
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
        System.out.println("Surviving resources in the producer's JSON encoding (shape only: the resource tree holds no attribute values):");
        System.out.println(KafkaPersister.toJson(builder.build()));
    }

    private static void printCaveats() {
        System.out.println();
        System.out.println("Note: results are derived from the persisted resource tree, not from a live collection.");
        System.out.println("      Resources that are no longer collected remain listed until their data is removed, and new");
        System.out.println("      services appear only after their first collection. Response time resources carry node");
        System.out.println("      information, but their location property is only set for nodes outside the Default location.");
        System.out.println("      The Resource column shows the id the producer sends, which differs from the resource tree id.");
        System.out.println("      Node directories without a matching SNMP interface hold node-level data of other collectors (JMX,");
        System.out.println("      XML, WMI) and are merged into the node's nodeSnmp row, which is how the producer forwards them.");
        System.out.println("      The instance of a generic resource is its storage label; the producer sends the raw index, which only");
        System.out.println("      matches the label for the index storage strategy. Rows affected by that are marked uncertain.");
    }

    private void printConfig(Dictionary<String, Object> config) {
        System.out.printf("Configuration (%s):\n", PRODUCER_PID);
        System.out.printf("    %-26s = %s\n", KafkaPersisterActivator.FORWARD_METRICS, valueOf(config, KafkaPersisterActivator.FORWARD_METRICS));
        System.out.printf("    %-26s = %s\n", METRIC_TOPIC_PROPERTY, valueOf(config, METRIC_TOPIC_PROPERTY));
        System.out.printf("    %-26s = %s\n", METRIC_FILTER_PROPERTY, valueOf(config, METRIC_FILTER_PROPERTY));
        System.out.printf("    %-26s = %s\n", DISABLE_SPLITTING_PROPERTY, valueOf(config, DISABLE_SPLITTING_PROPERTY));
        System.out.printf("    %-26s = %s\n", USE_JSON_PROPERTY, valueOf(config, USE_JSON_PROPERTY));
        final KafkaPersisterFactory factory = findKafkaPersisterFactory();
        if (factory != null) {
            // Everything the activator reads once at start, as the running persisters see it
            final Map<String, String> running = new LinkedHashMap<>();
            running.put(METRIC_FILTER_PROPERTY, Strings.nullToEmpty(factory.getMetricFilter()));
            running.put(METRIC_TOPIC_PROPERTY, Strings.nullToEmpty(factory.getTopicName()));
            running.put(DISABLE_SPLITTING_PROPERTY, String.valueOf(factory.isDisableMetricsSplitting()));
            running.put(USE_JSON_PROPERTY, String.valueOf(factory.isUseJson()));
            final Map<String, String> configured = new LinkedHashMap<>();
            configured.put(METRIC_FILTER_PROPERTY, Strings.nullToEmpty(configuredFilter(config)));
            configured.put(METRIC_TOPIC_PROPERTY, Strings.nullToEmpty(stringValue(config, METRIC_TOPIC_PROPERTY)));
            configured.put(DISABLE_SPLITTING_PROPERTY, String.valueOf(Boolean.parseBoolean(stringValue(config, DISABLE_SPLITTING_PROPERTY))));
            configured.put(USE_JSON_PROPERTY, String.valueOf(Boolean.parseBoolean(stringValue(config, USE_JSON_PROPERTY))));

            System.out.println("Running (read when the feature started):");
            final List<String> stale = new ArrayList<>();
            for (Map.Entry<String, String> entry : running.entrySet()) {
                final String value = entry.getValue().isEmpty() ? "(none)" : entry.getValue();
                System.out.printf("    %-26s = %s\n", entry.getKey(), value);
                if (!Objects.equals(entry.getValue(), configured.get(entry.getKey()))) {
                    stale.add(entry.getKey());
                }
            }
            if (!stale.isEmpty()) {
                System.out.printf("WARNING: the running values of %s differ from the configuration. These settings are read when the\n", String.join(", ", stale));
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
        return stringValue(config, METRIC_FILTER_PROPERTY);
    }

    private static String stringValue(Dictionary<String, Object> config, String key) {
        final Object value = config != null ? config.get(key) : null;
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
