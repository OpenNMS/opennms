/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Used to stress the persistence layer with generated collection sets.
 *
 * @author jwhite
 */
@Command(scope = "opennms", name = "stress-metrics", description="Stress the current persistence strategy with generated collection sets.")
@Service
public class StressCommand implements Action {

    @Reference
    private PersisterFactory persisterFactory;

    @Option(name="-b", aliases="--burst", description="Generate the collection sets in bursts instead of continously inserting them", required=false, multiValued=false)
    boolean burst = false;

    @Option(name="-i", aliases="--interval", description="Interval in seconds at which collection sets will be generated", required=false, multiValued=false)
    int intervalInSeconds = 300;

    @Option(name="-n", aliases="--nodes", description="Number of nodes for which metrics will be generated", required=false, multiValued=false)
    int numberOfNodes = 1000;

    @Option(name="-f", aliases="--interfaces", description="Number of interfaces on each node", required=false, multiValued=false)
    int numberOfInterfacesPerNode = 10;

    @Option(name="-g", aliases="--groups", description="Number of groups on each interface", required=false, multiValued=false)
    int numberOfGroupsPerInterface = 5;

    @Option(name="-a", aliases="--attributes", description="Number of number attributes in each group", required=false, multiValued=false)
    int numberOfNumericAttributesPerGroup = 10;

    @Option(name="-s", aliases="--strings", description="Number of string attributes in each group", required=false, multiValued=false)
    int numberOfStringAttributesPerGroup = 2;

    @Option(name="-r", aliases="--report", description="Number of seconds after which the report should be generated", required=false, multiValued=false)
    int reportIntervalInSeconds = 30;

    @Option(name="-t", aliases="--threads", description="Number of threads that will be used to generate and persist collection sets", required=false, multiValued=false)
    int numberOfGeneratorThreads = 1;

    @Option(name="-z", aliases="--string-variation-factor", description="When set, every n-th group will use unique string attribute values in each batch", required=false, multiValued=false)
    int stringVariationFactor = 0;

    @Option(name="-x", aliases="--rra", description="Round Robin Archives, defaults to the same RRA values as a pristine datacollection-config.xml.\nExample: -x 'RRA:AVERAGE:0.5:1:2016' -x 'RRA:AVERAGE:0.5:12:1488'", required=false, multiValued=true)
    List<String> rras = null;

    @Option(name="-ml", aliases="--metric-extra-length", description="It will append specific length of string at the end of metrics.", required=false, multiValued=false)
    int metricExtraLength = 0;

    @Option(name="-mlv", aliases="--metric-extra-length-variance", description="It will affect the length of metric-extra-length. Must be smaller than metric-extra-length.", required=false, multiValued=false)
    int metricExtraLengthVariance = 0;

    @Option(name="-rl", aliases="--resource-extra-length", description="It will append specific length of string at the end of resources.", required=false, multiValued=false)
    int resourceExtraLength = 0;

    @Option(name="-rlv", aliases="--resource-extra-length-variance", description="It will affect the length of resource-extra-length. Must be smaller than resource-extra-length.", required=false, multiValued=false)
    int resourceExtraLengthVariance = 0;

    private RateLimiter rateLimiter;

    private int numNumericAttributesPerNodePerCycle;

    private double numNumericAttributesPerSecond;

    private double numStringAttributesPerSecond;

    private final AtomicBoolean abort = new AtomicBoolean(false);

    private final MetricRegistry metrics = new MetricRegistry();

    final Timer batchTimer = metrics.timer("batches");

    private final Meter numericAttributesGenerated = metrics.meter("numeric-attributes-generated");

    private final Meter stringAttributesGenerated = metrics.meter("string-attributes-generated");

    private Meter stringAttributesVaried;

    /**
     * Used to calculate non-constant, but predictable values stored in numeric attributes.
     */
    private AtomicInteger seed = new AtomicInteger();

    @Override
    public Void execute() throws Exception {
        // check parameter
        if (metricExtraLength > 0 && metricExtraLengthVariance >= metricExtraLength) {
            System.out.println("metricExtraLength must larger than metricExtraLengthVariance!");
            return null;
        }
        if (resourceExtraLength > 0 && resourceExtraLengthVariance >= resourceExtraLength) {
            System.out.println("resourceExtraLength must larger than resourceExtraLengthVariance!");
            return null;
        }
        // Apply sane lower bounds to all of the configurable options
        intervalInSeconds = Math.max(1, intervalInSeconds);
        numberOfNodes = Math.max(1, numberOfNodes);
        numberOfInterfacesPerNode = Math.max(1, numberOfInterfacesPerNode);
        numberOfGroupsPerInterface = Math.max(1, numberOfGroupsPerInterface);
        numberOfNumericAttributesPerGroup = Math.max(0, numberOfNumericAttributesPerGroup);
        numberOfStringAttributesPerGroup = Math.max(0, numberOfStringAttributesPerGroup);
        reportIntervalInSeconds = Math.max(1, reportIntervalInSeconds);
        numberOfGeneratorThreads = Math.max(1, numberOfGeneratorThreads);
        stringVariationFactor = Math.max(0, stringVariationFactor);
        if (stringVariationFactor > 0) {
            stringAttributesVaried = metrics.meter("string-attributes-varied");
        }

        // Display the effective settings and rates
        final double groupsPerSecond = (1 / (double)intervalInSeconds) * numberOfGroupsPerInterface
                * numberOfInterfacesPerNode * numberOfNodes;
        numNumericAttributesPerNodePerCycle = numberOfInterfacesPerNode * numberOfGroupsPerInterface
                * numberOfNumericAttributesPerGroup;
        numNumericAttributesPerSecond = numberOfNumericAttributesPerGroup * groupsPerSecond;
        numStringAttributesPerSecond = numberOfStringAttributesPerGroup * groupsPerSecond;
        System.out.printf("Generating collection sets every %d seconds\n", intervalInSeconds);
        System.out.printf("\t for %d nodes\n", numberOfNodes);
        System.out.printf("\t with %d interfaces\n", numberOfInterfacesPerNode);
        System.out.printf("\t with %d attribute groups\n", numberOfGroupsPerInterface);
        System.out.printf("\t with %d numeric attributes\n", numberOfNumericAttributesPerGroup);
        System.out.printf("\t with %d string attributes\n", numberOfStringAttributesPerGroup);
        System.out.printf("Across %d threads\n", numberOfGeneratorThreads);
        if (stringVariationFactor > 0) {
            System.out.printf("With string variation factor %d\n", stringVariationFactor);
        }
        System.out.printf("Which will yield an effective\n");
        System.out.printf("\t %.2f numeric attributes per second\n", numNumericAttributesPerSecond);
        System.out.printf("\t %.2f string attributes per second\n", numStringAttributesPerSecond);

        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        // Setup the executor
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Metrics Stress Tool Generator #%d")
            .build();
        ExecutorService executor = Executors.newFixedThreadPool(numberOfGeneratorThreads, threadFactory);

        // Setup auxiliary objects needed by the persister
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repository = new RrdRepository();
        repository.setStep(Math.max(intervalInSeconds, 1));
        repository.setHeartBeat(repository.getStep() * 2);
        if (rras != null && rras.size() > 0) {
            repository.setRraList(rras);
        } else {
            repository.setRraList(Lists.newArrayList(CollectCommand.DEFAULT_RRA));
        }
        repository.setRrdBaseDir(Paths.get(System.getProperty("opennms.home"),"share","rrd","snmp").toFile());

        // Display effective rate limiting strategy:
        System.out.printf("Limiting rate by\n");
        if (burst) {
            System.out.printf("\t sleeping %d seconds between batches (batch mode)\n", intervalInSeconds);
            rateLimiter = null;
        } else {
            rateLimiter = RateLimiter.create(numNumericAttributesPerSecond);
            System.out.printf("\t smoothing persistence to %.2f attributes per second\n", numNumericAttributesPerSecond);
        }

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

                if (burst) {
                    try {
                        Thread.sleep(intervalInSeconds * 1000L);
                    } catch (InterruptedException e) {
                        break;
                    }
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
                    if (rateLimiter != null) {
                        rateLimiter.acquire(numNumericAttributesPerNodePerCycle);
                    }

                    // Build the node resource
                    CollectionAgent agent = new MockCollectionAgent(nodeId);
                    NodeLevelResource nodeResource = new NodeLevelResource(nodeId);

                    // Don't reuse the persister instances across nodes to help simulate collectd's actual behavior
                    Persister persister = persisterFactory.createPersister(params, repository);
                    for (int interfaceId = 0; interfaceId < numberOfInterfacesPerNode; interfaceId++) {
                        // Return immediately if the abort flag is set
                        if (abort.get()) {
                            return null;
                        }

                        // Build the interface resource
                        InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeResource, "tap" + interfaceId);

                        // Generate the collection set
                        CollectionSet collectionSet = generateCollectionSet(agent, nodeId, interfaceId, interfaceResource);

                        // Persist
                        collectionSet.visit(persister);
                    }
                }
                return null;
            }
        };
    }

    protected CollectionSet generateCollectionSet(CollectionAgent agent, int nodeId, int interfaceId, Resource resource) {
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        for (int groupId = 0; groupId < numberOfGroupsPerInterface; groupId++) {
            String groupName = "group" + groupId;
            // Number attributes
            for (int attributeId = 0; attributeId < numberOfNumericAttributesPerGroup; attributeId++) {
                // Generate a predictable, non-constant number
                int value = groupId * attributeId + seed.incrementAndGet() % 100;
                StringBuilder collectionName = new StringBuilder("metric_");
                collectionName.append(groupId);
                collectionName.append("_");
                collectionName.append(attributeId);
                if (metricExtraLength > 0) {
                    collectionName.append(generateExtraString(collectionName.toString(), nodeId, metricExtraLength, metricExtraLengthVariance));
                }
                if (resourceExtraLength > 0) {
                    groupName += "_" + generateExtraString(groupName, nodeId, resourceExtraLength, resourceExtraLengthVariance);
                }
                builder.withNumericAttribute(resource, groupName, collectionName.toString(), value, AttributeType.GAUGE);
                numericAttributesGenerated.mark();
            }
            String stringAttributeValueSuffix = "";
            int groupInstance = (nodeId + 1) * (interfaceId + 1) * (groupId + 1); // Add 1 to each of these to make sure they are > 0
            if (stringVariationFactor > 0 && groupInstance % stringVariationFactor == 0) {
                stringAttributeValueSuffix = String.format("-%d-varied", groupInstance);
                stringAttributesVaried.mark(numberOfStringAttributesPerGroup);
            }

            // String attributes
            for (int stringAttributeId = 0; stringAttributeId < numberOfStringAttributesPerGroup; stringAttributeId++) {
                // String attributes are stored at the resource level, and not at the group level, so we prefix
                // the keys with the group name to make sure these don't collide
                String key = String.format("%s-key-%d", groupName, stringAttributeId);
                String value = String.format("%s-value-%d%s", groupName, stringAttributeId, stringAttributeValueSuffix);
                builder.withStringAttribute(resource, groupName, key, value);
                stringAttributesGenerated.mark();
            }
        }
        return builder.build();
    }

    /**
     * Generate a string is not the same per difference params but keep consistence by the same params.
     *
     * @param prefix
     * @param id
     * @param length
     * @param variance
     * @return
     */
    private String generateExtraString(String prefix, int id, int length, int variance) {
        HashFunction sha1 = Hashing.sha1();
        String hash = sha1.hashString(prefix, StandardCharsets.UTF_8).toString();
        int outLength = length;
        // calculate length with variance by nodeId
        if (length > 0) {
            outLength = length - variance + (id % (2 * variance + 1));
        }
        if (outLength > hash.length()) {
            int repeat = (int) Math.ceil((double) outLength / hash.length());
            hash = hash.repeat(repeat);
        }
        return hash.substring(0, outLength);
    }

    protected static class MockCollectionAgent implements CollectionAgent {

        private final int nodeId;

        public MockCollectionAgent(int nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public InetAddress getAddress() {
            return null;
        }

        @Override
        public Set<String> getAttributeNames() {
            return Collections.emptySet();
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
        public String getLocationName() {
            return null;
        }

        @Override
        public ResourcePath getStorageResourcePath() {
            // Copied from org.opennms.netmgt.collectd.org.opennms.netmgt.collectd#getStorageDir
            final String foreignSource = getForeignSource();
            final String foreignId = getForeignId();

            final ResourcePath dir;
            if(isStoreByForeignSource() && foreignSource != null && foreignId != null) {
                dir = ResourcePath.get(ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY,
                                       foreignSource,
                                       foreignId);
            } else {
                dir = ResourcePath.get(String.valueOf(getNodeId()));
            }

            return dir;
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
