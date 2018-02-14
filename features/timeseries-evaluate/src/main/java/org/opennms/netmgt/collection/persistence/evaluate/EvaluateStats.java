/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.collection.persistence.evaluate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 * The Class EvaluateStats.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateStats {

    /** The Constant LOGGING_PREFFIX. */
    private static final String LOGGING_PREFFIX = "EvaluationMetrics";

    /** The Constant LOGGING_SUFFIX. */
    private static final String LOGGING_SUFFIX = "-Cache";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LOGGING_PREFFIX);

    /** The node map. */
    private final ConcurrentMap<String, Boolean> nodeMap = new ConcurrentHashMap<String, Boolean>();

    /** The interface map. */
    private final ConcurrentMap<String, Boolean> interfaceMap = new ConcurrentHashMap<String, Boolean>();

    /** The resource map. */
    private final ConcurrentMap<String, Boolean> resourceMap = new ConcurrentHashMap<String, Boolean>();

    /** The numeric attribute map. */
    private final ConcurrentMap<String, Boolean> numericAttributeMap = new ConcurrentHashMap<String, Boolean>();

    /** The string attribute map. */
    private final ConcurrentMap<String, Boolean> stringAttributeMap = new ConcurrentHashMap<String, Boolean>();

    /** The group map. */
    private final ConcurrentMap<String, Boolean> groupMap = new ConcurrentHashMap<String, Boolean>();

    /** The numeric samples meter. */
    private final Meter numericSamplesMeter;

    /**
     * Instantiates a new evaluate statistics.
     *
     * @param registry the metrics registry
     * @param dumpStatsFreq the frequency in minutes to dump the statistics to the log file
     * @param dumpCacheFreq the frequency in minutes to dump the cache content to the log file
     */
    public EvaluateStats(MetricRegistry registry, Integer dumpStatsFreq, Integer dumpCacheFreq) {
        Assert.notNull(registry, "MetricRegistry is required");
        Assert.notNull(dumpStatsFreq, "Dump statistics frequency is required");
        Assert.isTrue(dumpStatsFreq > 0, "Dump statistics frequency must be positive");
        Assert.notNull(dumpCacheFreq, "Dump cache frequency is required");
        Assert.isTrue(dumpCacheFreq > 0, "Dump cache frequency must be positive");

        final Gauge<Integer> nodes = () -> { return nodeMap.keySet().size(); };
        registry.register(MetricRegistry.name("evaluate", "nodes"), nodes);

        final Gauge<Integer> interfaces = () -> { return interfaceMap.keySet().size(); };
        registry.register(MetricRegistry.name("evaluate", "interfaces"), interfaces);

        final Gauge<Integer> resources = () -> { return resourceMap.keySet().size(); };
        registry.register(MetricRegistry.name("evaluate", "resources"), resources);

        final Gauge<Integer> numericAttributes = () -> { return numericAttributeMap.keySet().size(); };
        registry.register(MetricRegistry.name("evaluate", "numeric-attributes"), numericAttributes);

        final Gauge<Integer> stringAttributes = () -> { return stringAttributeMap.keySet().size(); };
        registry.register(MetricRegistry.name("evaluate", "string-attributes"), stringAttributes);

        if (ResourceTypeUtils.isStoreByGroup()) {
            final Gauge<Integer> groups = () -> { return groupMap.keySet().size(); };
            registry.register(MetricRegistry.name("evaluate", "groups"), groups);
        }

        numericSamplesMeter = registry.meter(MetricRegistry.name("evaluate", "samples"));

        // Metric Reporter
        Logging.withPrefix(LOGGING_PREFFIX, () -> {
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                    .outputTo(LOG)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(dumpStatsFreq, TimeUnit.MINUTES);
        });

        // Cache Dump, for debugging purposes
        Logging.withPrefix(LOGGING_PREFFIX + LOGGING_SUFFIX, () -> {
            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            svc.scheduleAtFixedRate(this::dumpCache, 0, dumpCacheFreq, TimeUnit.MINUTES);
        });
    }

    /**
     * Checks a node.
     *
     * @param nodeId the node identifier
     */
    public void checkNode(final String nodeId) {
        if (nodeId.startsWith("fs") || nodeId.matches("\\d+")) {
            nodeMap.putIfAbsent(nodeId, true);
        } else {
            interfaceMap.putIfAbsent(nodeId, true);
        }
    }

    /**
     * Checks a resource.
     *
     * @param resourceId the resource identifier
     */
    public void checkResource(final String resourceId) {
        resourceMap.putIfAbsent(resourceId, true);
    }

    /**
     * Checks a attribute.
     *
     * @param attributeId the attribute identifier
     * @param isNumeric true if the attribute is numeric
     */
    public void checkAttribute(final String attributeId, boolean isNumeric) {
        if (isNumeric) {
            numericAttributeMap.putIfAbsent(attributeId, true);
        } else {
            stringAttributeMap.putIfAbsent(attributeId, true);
        }
    }

    /**
     * Checks a group.
     *
     * @param groupId the group identifier
     */
    public void checkGroup(final String groupId) {
        groupMap.putIfAbsent(groupId, true);
    }

    /**
     * Marks the numeric samples meter.
     *
     * @return the numeric samples meter
     */
    public void markNumericSamplesMeter() {
        numericSamplesMeter.mark();
    }

    /**
     * Dumps the content of the cache.
     */
    protected void dumpCache() {
        nodeMap.keySet().stream().sorted().forEach(s -> LOG.info("node: {}", s));
        interfaceMap.keySet().stream().sorted().forEach(s -> LOG.info("interface: {}", s));
        resourceMap.keySet().stream().sorted().forEach(s -> LOG.info("resource: {}", s));
        groupMap.keySet().stream().sorted().forEach(s -> LOG.info("group: {}", s));
        numericAttributeMap.keySet().stream().sorted().forEach(s -> LOG.info("numeric-attribute: {}", s));
        stringAttributeMap.keySet().stream().sorted().forEach(s -> LOG.info("string-attribute: {}", s));
    }

}
