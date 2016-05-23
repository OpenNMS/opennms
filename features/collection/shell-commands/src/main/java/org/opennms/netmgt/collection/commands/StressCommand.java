/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collection.commands;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.AttributeType;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Used to stress the persistence layer with generated collection sets.
 *
 * @author jwhite
 */
@Command(scope = "metrics", name = "stress", description="Stress the current persistence strategy with generated collection sets.")
public class StressCommand extends OsgiCommandSupport {

    private PersisterFactory persisterFactory;

    @Option(name="-i", aliases="--interval", description="interval in seconds at which collection sets will be generated, defaults to 300", required=false, multiValued=false)
    int intervalInSeconds = 300;

    @Option(name="-n", aliases="--nodes", description="number of nodes for which metrics will be generated, defaults to 1000", required=false, multiValued=false)
    int numberOfNodes = 1000;

    @Option(name="-f", aliases="--interfaces", description="number of interfaces on each node, defaults to 10", required=false, multiValued=false)
    int numberOfInterfacesPerNode = 10;

    @Option(name="-g", aliases="--groups", description="number of groups on each interface, defaults to 5", required=false, multiValued=false)
    int numberOfGroupsPerInterface = 5;

    @Option(name="-a", aliases="--attributes", description="number of number attributes in each group, defaults to 10", required=false, multiValued=false)
    int numberOfNumericAttributesPerGroup = 10;

    @Option(name="-s", aliases="--strings", description="number of string attributes in each group, defaults to 2", required=false, multiValued=false)
    int numberOfStringAttributesPerGroup = 2;

    @Option(name="-r", aliases="--report", description="number of seconds after which the report should be generated, defaults to 30", required=false, multiValued=false)
    int reportIntervalInSeconds = 30;

    @Option(name="-t", aliases="--threads", description="number of threads that will be used to generate and persist collection sets, defaults to 1", required=false, multiValued=false)
    int numberOfGeneratorThreads = 1;

    private final AtomicBoolean abort = new AtomicBoolean(false);

    private final MetricRegistry metrics = new MetricRegistry();

    final Timer batchTimer = metrics.timer("batches");

    private final Meter numericAttributesGenerated = metrics.meter("numeric-attributes-generated");

    private final Meter stringAttributesGenerated = metrics.meter("string-attributes-generated");

    /**
     * Used to calculate non-constant, but predictable values stored in numeric attributes.
     */
    private AtomicInteger seed = new AtomicInteger();

    @Override
    protected Void doExecute() {
        // Apply sane lower bounds to all of the configurable options
        intervalInSeconds = Math.max(1, intervalInSeconds);
        numberOfNodes = Math.max(1, numberOfNodes);
        numberOfInterfacesPerNode = Math.max(1, numberOfInterfacesPerNode);
        numberOfGroupsPerInterface = Math.max(1, numberOfGroupsPerInterface);
        numberOfNumericAttributesPerGroup = Math.max(0, numberOfNumericAttributesPerGroup);
        numberOfStringAttributesPerGroup = Math.max(0, numberOfStringAttributesPerGroup);
        reportIntervalInSeconds = Math.max(1, reportIntervalInSeconds);
        numberOfGeneratorThreads = Math.max(1, numberOfGeneratorThreads);

        // Display the effective settings and rates
        double attributesPerSecond = (1 / (double)intervalInSeconds) * numberOfGroupsPerInterface
                * numberOfInterfacesPerNode * numberOfNodes;
        System.out.printf("Generating collection sets every %d seconds\n", intervalInSeconds);
        System.out.printf("\t for %d nodes\n", numberOfNodes);
        System.out.printf("\t with %d interfaces\n", numberOfInterfacesPerNode);
        System.out.printf("\t with %d attribute groups\n", numberOfGroupsPerInterface);
        System.out.printf("\t with %d numeric attributes\n", numberOfNumericAttributesPerGroup);
        System.out.printf("\t with %d string attributes\n", numberOfStringAttributesPerGroup);
        System.out.printf("Across %d threads\n", numberOfGeneratorThreads);
        System.out.printf("Which will yield an effective\n");
        System.out.printf("\t %.2f numeric attributes per second\n", numberOfNumericAttributesPerGroup * attributesPerSecond);
        System.out.printf("\t %.2f string attributes per second\n", numberOfStringAttributesPerGroup * attributesPerSecond);

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        // Setup the executor
        ThreadFactory threadFactoy = new ThreadFactoryBuilder()
            .setNameFormat("Metrics Stress Tool Generator #%d")
            .build();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfGeneratorThreads, threadFactoy);

        // Setup auxiliary objects needed by the persister
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repository = new RrdRepository();
        repository.setStep(Math.max(intervalInSeconds, 1));
        repository.setHeartBeat(repository.getStep() * 2);
        repository.setRraList(Lists.newArrayList(
                // Use the default list of RRAs we provide in our stock configuration files
                "RRA:AVERAGE:0.5:1:2016",
                "RRA:AVERAGE:0.5:12:1488",
                "RRA:AVERAGE:0.5:288:366",
                "RRA:MAX:0.5:288:366",
                "RRA:MIN:0.5:288:366"));
        repository.setRrdBaseDir(Paths.get(System.getProperty("opennms.home"),"share","rrd","snmp").toFile());

        // Start generating, and keep generating until we're interrupted
        try {
            reporter.start(reportIntervalInSeconds, TimeUnit.SECONDS);
            while (true) {
                final Context context = batchTimer.time();
                try {
                    // Split the tasks up among the threads
                    List<Future<Void>> futures = new ArrayList<>();
                    for (int generatorThreadId = 0; generatorThreadId < numberOfGeneratorThreads; generatorThreadId++) {
                        futures.add(executor.submit(generateAndPersistCollectionSets(params, repository, generatorThreadId)));
                    }
                    // Wait for all the tasks to complete before starting others
                    for (Future<Void> future : futures) {
                        future.get();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    break;
                } finally {
                    context.stop();
                }

                try {
                    Thread.sleep(intervalInSeconds * 1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } finally {
            reporter.stop();
            abort.set(true);
            executor.shutdownNow();
        }

        return null;
    }

    private Callable<Void> generateAndPersistCollectionSets(ServiceParameters params, RrdRepository repository, int generatorThreadId) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (int nodeId = 0; nodeId < numberOfNodes; nodeId++) {
                    if (nodeId % numberOfGeneratorThreads != generatorThreadId) {
                        // A different generator will handle this node
                        continue;
                    }

                    // Build the node resource
                    CollectionAgent agent = new MockCollectionAgent(nodeId);
                    NodeLevelResource nodeResource = new NodeLevelResource(nodeId);

                    // Don't reuse the persister instance across nodes to help simulate collectd's actual behavior
                    Persister persister = persisterFactory.createPersister(params, repository);
                    for (int interfaceId = 0; interfaceId < numberOfInterfacesPerNode; interfaceId++) {
                        // Return immediately if the abort flag is set
                        if (abort.get()) {
                            return null;
                        }

                        // Build the interface resource
                        InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeResource, "tap" + interfaceId);

                        // Generate the collection set
                        CollectionSet collectionSet = generateCollectionSet(agent, interfaceResource);

                        // Persist
                        collectionSet.visit(persister);
                    }
                }
                return null;
            }
        };
    }

    private CollectionSet generateCollectionSet(CollectionAgent agent, Resource resource) {
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        for (int groupId = 0; groupId < numberOfGroupsPerInterface; groupId++) {
            String groupName = "group" + groupId;
            // Number attributes
            for (int attributeId = 0; attributeId < numberOfNumericAttributesPerGroup; attributeId++) {
                // Generate a predictable, non-constant number
                int value = groupId * attributeId + seed.incrementAndGet() % 100;
                builder.withNumericAttribute(resource, groupName, "metric" + attributeId, value, AttributeType.GAUGE);
                numericAttributesGenerated.mark();
            }

            // String attributes
            for (int stringAttributeId = 0; stringAttributeId < numberOfStringAttributesPerGroup; stringAttributeId++) {
                // Use constant values for the string attributes
                builder.withStringAttribute(resource, groupName, "tag" + stringAttributeId, "key" + stringAttributeId);
                stringAttributesGenerated.mark();
            }
        }
        return builder.build();
    }

    public void setPersisterFactory(PersisterFactory persisterFactory) {
        this.persisterFactory = persisterFactory;
    }

    private static class MockCollectionAgent implements CollectionAgent {

        private final int nodeId;

        public MockCollectionAgent(int nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public InetAddress getAddress() {
            return null;
        }

        @Override
        public <V> V getAttribute(String property) {
            return null;
        }

        @Override
        public Object setAttribute(String property, Object value) {
            return null;
        }

        @Override
        public Boolean isStoreByForeignSource() {
            return ResourceTypeUtils.isStoreByForeignSource();
        }

        @Override
        public String getHostAddress() {
            return null;
        }

        @Override
        public void setSavedIfCount(int ifCount) {
            // pass
        }

        @Override
        public int getNodeId() {
            return nodeId;
        }

        @Override
        public String getNodeLabel() {
            return Integer.toString(nodeId);
        }

        @Override
        public String getForeignSource() {
            return "STRESS";
        }

        @Override
        public String getForeignId() {
            return Integer.toString(nodeId);
        }

        @Override
        public File getStorageDir() {
            // Copied from org.opennms.netmgt.collectd.org.opennms.netmgt.collectd#getStorageDir
            File dir = new File(Integer.toString(getNodeId()));
            final String foreignSource = getForeignSource();
            final String foreignId = getForeignId();
            if(isStoreByForeignSource() && foreignSource != null && foreignId != null) {
                File fsDir = new File(ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY, foreignSource);
                dir = new File(fsDir, foreignId);
            }
            return dir;
        }

        @Override
        public String getSysObjectId() {
            return null;
        }

        @Override
        public long getSavedSysUpTime() {
            return 0;
        }

        @Override
        public void setSavedSysUpTime(long sysUpTime) {
            // pass
        }
    }
}
