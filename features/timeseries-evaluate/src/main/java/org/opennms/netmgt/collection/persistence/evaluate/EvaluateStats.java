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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * The Class EvaluateStats.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateStats {

    /** The resource map. */
    private final ConcurrentMap<String, Boolean> resourceMap = new ConcurrentHashMap<String, Boolean>();

    /** The attribute map. */
    private final ConcurrentMap<String, Boolean> attributeMap = new ConcurrentHashMap<String, Boolean>();

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
        final Gauge<Integer> resources = () -> { return resourceMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "resources"), resources);

        final Gauge<Integer> attributes = () -> { return attributeMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "attributes"), attributes);

        final Gauge<Integer> groups = () -> { return groupMap.size(); };
        registry.register(MetricRegistry.name("evaluate", "groups"), groups);

        samplesMeter = registry.meter(MetricRegistry.name("evaluate", "samples"));
    }

    /**
     * Checks q resource.
     *
     * @param resource the resource
     */
    public void checkResource(String resource) {
        resourceMap.putIfAbsent(resource, true);
    }

    /**
     * Checks a attribute.
     *
     * @param attribute the attribute
     */
    public void checkAttribute(String attribute) {
        attributeMap.putIfAbsent(attribute, true);
    }

    /**
     * Checks a group.
     *
     * @param group the group
     */
    public void checkGroup(String group) {
        groupMap.putIfAbsent(group, true);
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
