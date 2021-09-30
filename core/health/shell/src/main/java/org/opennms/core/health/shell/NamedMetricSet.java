/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.health.shell;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;

public class NamedMetricSet {
    private static final String NAME_PROP_KEY = "name";
    private static final String DESCRIPTION_PROP_KEY = "description";

    private final MetricSet metricSet;
    private final String name;
    private final String description;

    public NamedMetricSet(MetricSet metricSet, String name, String description) {
        this.metricSet = Objects.requireNonNull(metricSet);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public boolean hasDescription() {
        return description != null && description.length() > 0;
    }

    public String getDescription() {
        return description;
    }

    public MetricRegistry toMetricRegistry() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.registerAll(metricSet);
        return metricRegistry;
    }

    public static List<NamedMetricSet> getNamedMetricSetsInContext(BundleContext bundleContext) {
        final Map<String, NamedMetricSet> metricSetsByName = new HashMap<>();
        // Gather the available metric sets from the service registry
        final Collection<ServiceReference<MetricSet>> metricSetRefs;
        try {
            metricSetRefs = bundleContext.getServiceReferences(MetricSet.class, null);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
        for (ServiceReference<MetricSet> metricSetRef : metricSetRefs) {
            final String name = (String) metricSetRef.getProperty(NAME_PROP_KEY);
            final String description = (String) metricSetRef.getProperty(DESCRIPTION_PROP_KEY);
            final MetricSet metricSet = bundleContext.getService(metricSetRef);
            // Key the metric sets by name, first one wins
            metricSetsByName.putIfAbsent(name, new NamedMetricSet(metricSet, name, description));
        }
        // Sort them by name
        return metricSetsByName.values().stream()
                .sorted(Comparator.comparing(NamedMetricSet::getName))
                .collect(Collectors.toList());
    }
}
