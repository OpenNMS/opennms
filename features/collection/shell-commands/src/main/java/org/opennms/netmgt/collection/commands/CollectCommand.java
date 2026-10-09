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
package org.opennms.netmgt.collection.commands;

import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.InvalidCollectionAgentException;
import org.opennms.netmgt.collection.api.LocationAwareCollectorClient;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.snmp.InetAddrUtils;

import com.google.common.collect.Lists;

@Command(scope = "opennms", name = "collect", description="Invokes a collector against a host at a specified location.")
@Service
public class CollectCommand implements Action {

    public static final List<String> DEFAULT_RRA = Lists.newArrayList(
            // Use the default list of RRAs we provide in our stock
            // configuration files
            "RRA:AVERAGE:0.5:1:2016",
            "RRA:AVERAGE:0.5:12:1488",
            "RRA:AVERAGE:0.5:288:366",
            "RRA:MAX:0.5:288:366",
            "RRA:MIN:0.5:288:366");

    /**
     * Default RRD step in seconds, matching the stock datacollection-config.xml.
     */
    public static final int DEFAULT_STEP = 300;

    @Option(name = "-l", aliases = "--location", description = "Location", required = false, multiValued = false)
    String location;

    @Option(name = "-s", aliases = "--system-id", description = "System ID")
    String systemId;

    @Option(name = "-t", aliases = "--ttl", description = "Time to live in milliseconds", required = false, multiValued = false)
    Long ttlInMs;

    @Option(name = "-n", aliases = "--node", description = "Node ID or FS:FID", required = false, multiValued = false)
    String nodeCriteria;

    @Option(name = "-p",  aliases = "--persist", description = "Persist collection")
    boolean persist;

    @Argument(index = 0, name = "collectorClass", description = "Collector class", required = true, multiValued = false)
    @Completion(CollectorClassNameCompleter.class)
    String className;

    @Argument(index = 1, name = "host", description = "Hostname or IP Address of the system to poll", required = true, multiValued = false)
    String host;

    @Argument(index = 2, name = "attributes", description = "Collector specific attributes in key=value form", multiValued = true)
    List<String> attributes;

    @Option(name="-x", aliases="--rra", description="Round Robin Archives, defaults to the pristine content on datacollection-config.xml", required=false, multiValued=true)
    List<String> rras = null;


    @Reference
    public ServiceCollectorRegistry serviceCollectorRegistry;

    @Reference
    public LocationAwareCollectorClient locationAwareCollectorClient;

    @Reference
    public CollectionAgentFactory collectionAgentFactory;

    @Reference
    private PersisterFactory persisterFactory;


    @Override
    public Void execute() {
        final ServiceCollector collector = serviceCollectorRegistry.getCollectorFutureByClassName(className).getNow(null);
        if (collector == null) {
            System.out.printf("No collector found with class name '%s'. Aborting.\n", className);
            return null;
        }

        try {
            // The collector may not have been initialized - initialize it
            collector.initialize();
        } catch (CollectionInitializationException e) {
            System.out.println("Failed to initialize the collector. Aborting.");
            e.printStackTrace();
            return null;
        }

        final CollectionAgent agent = getCollectionAgent();
        final CompletableFuture<CollectionSet> future = locationAwareCollectorClient.collect()
                .withAgent(agent)
                .withSystemId(systemId)
                // For Service collectors that implement integration api will have proxy collectors.
                // fetching class name from proxy won't match with class name in collector registry.
                .withCollectorClassName(className)
                .withTimeToLive(ttlInMs)
                .withAttributes(parse(attributes))
                .execute();

        Persister persister = null;
        if (persist) {
            // Mirror what collectd does: the service parameters are the collector attributes
            // (so a "collection=xyz" attribute selects the data collection), and the RRD
            // repository comes from the collector's own data collection configuration.
            final ServiceParameters params = new ServiceParameters(parse(attributes));
            final RrdRepository repository = buildRrdRepository(collector, params);
            System.out.printf("Persisting using RRD repository: %s\n", repository);
            persister = persisterFactory.createPersister(params, repository);
        }
        while (true) {
            try {
                try {
                    CollectionSet collectionSet = future.get(1, TimeUnit.SECONDS);
                    if (CollectionStatus.SUCCEEDED.equals(collectionSet.getStatus())) {
                        printCollectionSet(collectionSet);
                        if (persist) {
                            collectionSet.visit(persister);
                            System.out.println("---- Persisted collection ----");
                        }
                    } else {
                        System.out.printf("\nThe collector returned a collection set with status: %s\n", collectionSet.getStatus());
                    }
                } catch (InterruptedException e) {
                    System.out.println("\nInterrupted.");
                } catch (ExecutionException e) {
                    final Throwable cause = e.getCause();
                    if (cause != null && cause instanceof InvalidCollectionAgentException) {
                        System.out.printf("The collector requires a valid node and interface. Try specifying a valid node using the --node option.\n", e);
                        break;
                    }
                    System.out.printf("\nCollect failed with: %s \n", e);
                }
                break;
            } catch (TimeoutException e) {
                // pass
            }
            System.out.print(".");
            System.out.flush();
        }
        return null;
    }

    /**
     * Builds the {@link RrdRepository} used to persist the collection set.
     *
     * The repository is taken from the collector's data collection configuration for the
     * requested collection, exactly like collectd does. If the collector cannot provide one
     * (or provides an incomplete one) sensible defaults are used instead, so that the step
     * and heartbeat are never zero: rrdtool refuses to create a file with a heartbeat of 0,
     * which would otherwise fail with "value must be positive".
     */
    RrdRepository buildRrdRepository(final ServiceCollector collector, final ServiceParameters params) {
        final String collectionName = params.getCollectionName();

        RrdRepository configured = null;
        try {
            configured = collector.getRrdRepository(collectionName);
        } catch (Exception e) {
            System.out.printf("Unable to determine the RRD repository for collection '%s' from the collector, using defaults: %s\n", collectionName, e.getMessage());
        }

        final RrdRepository repository = new RrdRepository();

        // Step: use the configured value when valid, otherwise the stock default
        int step = configured != null ? configured.getStep() : 0;
        if (step <= 0) {
            if (configured != null) {
                System.out.printf("The collector has no valid RRD settings for collection '%s' (step=%d), using the default step of %d seconds.\n", collectionName, step, DEFAULT_STEP);
            }
            step = DEFAULT_STEP;
        }
        repository.setStep(step);

        // Heartbeat: use the configured value when valid, otherwise twice the step
        int heartBeat = configured != null ? configured.getHeartBeat() : 0;
        if (heartBeat <= 0) {
            heartBeat = 2 * step;
        }
        repository.setHeartBeat(heartBeat);

        // RRAs: explicit command line option wins, then the configured list, then the stock default
        if (rras != null && !rras.isEmpty()) {
            repository.setRraList(Lists.newArrayList(rras));
        } else if (configured != null && configured.getRraList() != null && !configured.getRraList().isEmpty()) {
            repository.setRraList(Lists.newArrayList(configured.getRraList()));
        } else {
            repository.setRraList(Lists.newArrayList(DEFAULT_RRA));
        }

        // Base directory: use the configured value, otherwise $OPENNMS_HOME/share/rrd/snmp
        if (configured != null && configured.getRrdBaseDir() != null) {
            repository.setRrdBaseDir(configured.getRrdBaseDir());
        } else {
            repository.setRrdBaseDir(Paths.get(System.getProperty("opennms.home"), "share", "rrd", "snmp").toFile());
        }

        return repository;
    }

    private CollectionAgent getCollectionAgent() {
        final InetAddress hostAddr = InetAddrUtils.addr(host);
        if (nodeCriteria != null) {
            return collectionAgentFactory.createCollectionAgentAndOverrideLocation(nodeCriteria, hostAddr, location);
        } else {
            System.out.println("NOTE: Some collectors require a database node and IP interface.\n");
            final CollectionAgentDTO agent = new CollectionAgentDTO();
            agent.setLocationName(location);
            agent.setAddress(hostAddr);
            agent.setStorageResourcePath(ResourcePath.fromString(""));
            return agent;
        }
    }

    private static void printCollectionSet(CollectionSet collectionSet) {
        AtomicBoolean didPrintAttribute = new AtomicBoolean(false);
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitResource(CollectionResource resource) {
                System.out.printf("%s\n", resource);
            }

            @Override
            public void visitGroup(AttributeGroup group) {
                System.out.printf("\tGroup: %s\n", group.getName());
            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                System.out.printf("\t\t%s\n", attribute);
                didPrintAttribute.set(true);
            }
        });

        if (!didPrintAttribute.get()) {
            System.out.println("(Empty collection set)");
        }
    }

    private Map<String, Object> parse(List<String> attributeList) {
        final Map<String, Object> properties = new HashMap<>();
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property " + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1, keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
}
