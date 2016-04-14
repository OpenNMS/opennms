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

import org.opennms.netmgt.model.ResourceTypeUtils;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * The Class EvaluateStats.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateStats {

    /** The node map. */
    private final ConcurrentMap<String, Boolean> nodeMap = new ConcurrentHashMap<String, Boolean>();

    /** The resource map. */
    private final ConcurrentMap<String, Boolean> resourceMap = new ConcurrentHashMap<String, Boolean>();

    /** The numeric attribute map. */
    private final ConcurrentMap<String, Boolean> numericAttributeMap = new ConcurrentHashMap<String, Boolean>();

    /** The string attribute map. */
    private final ConcurrentMap<String, Boolean> stringAttributeMap = new ConcurrentHashMap<String, Boolean>();

    /** The group map. */
    private final ConcurrentMap<String, Boolean> groupMap = new ConcurrentHashMap<String, Boolean>();

    /** The samples meter. */
    private final Meter samplesMeter;

    /**
     * Instantiates a new evaluate statistics.
     *
     * @param registry the registry
     */
    public EvaluateStats(MetricRegistry registry) {
        final Gauge<Integer> node = () -> { return nodeMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "node"), node);

        final Gauge<Integer> resources = () -> { return resourceMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "resources"), resources);

        final Gauge<Integer> numericAttributes = () -> { return numericAttributeMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "numeric-attributes"), numericAttributes);

        final Gauge<Integer> stringAttributes = () -> { return stringAttributeMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "string-attributes"), stringAttributes);

        if (ResourceTypeUtils.isStoreByGroup()) {
            final Gauge<Integer> groups = () -> { return groupMap.size(); };
            registry.register(MetricRegistry.name("evaluate", "groups"), groups);
        }

        samplesMeter = registry.meter(MetricRegistry.name("evaluate", "samples"));
    }

    /**
     * Checks a node.
     *
     * @param nodeId the node identifier
     */
    public void checkNode(String nodeId) {
        nodeMap.putIfAbsent(nodeId, true);
    }

    /**
     * Checks a resource.
     *
     * @param resourceId the resource identifier
     */
    public void checkResource(String resourceId) {
        resourceMap.putIfAbsent(resourceId, true);
    }

    /**
     * Checks a attribute.
     *
     * @param attributeId the attribute identifier
     * @param isNumeric true if the attribute is numeric
     */
    public void checkAttribute(String attributeId, boolean isNumeric) {
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
    public void checkGroup(String groupId) {
        groupMap.putIfAbsent(groupId, true);
    }

    /**
     * Gets the samples meter.
     *
     * @return the samples meter
     */
    public Meter getSamplesMeter() {
        return samplesMeter;
    }
}
