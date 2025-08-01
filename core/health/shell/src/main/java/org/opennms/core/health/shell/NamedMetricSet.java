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
