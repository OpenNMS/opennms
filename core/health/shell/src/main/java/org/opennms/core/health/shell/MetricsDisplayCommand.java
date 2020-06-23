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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

@Command(scope = "health", name = "metrics-display", description="Display metrics from one or more metric sets.")
@Service
public class MetricsDisplayCommand implements Action {

    @Option(name = "-m", description = "Only display metric set with names containing the given substring.")
    private String metricSetFilter;

    @Reference
    private BundleContext bundleContext;

    @Override
    public Object execute() {
        final List<NamedMetricSet> metricSets = NamedMetricSet.getNamedMetricSetsInContext(bundleContext);
        if (metricSets.size() < 1) {
            System.out.println("(No metric sets are currently available.)");
            return null;
        }
        if (metricSetFilter != null) {
            metricSets.removeIf(m -> !m.getName().toLowerCase().contains(metricSetFilter.toLowerCase()));
        }
        if (metricSets.size() < 1) {
            System.out.printf("(No metric set names match the given filter '%s'.)\n", metricSetFilter);
            return null;
        }

        boolean first = true;
        for (NamedMetricSet namedMetricSet : metricSets) {
            // Add some extract spacing between the reports
            if (first) {
                first = false;
            } else {
                System.out.println("\n\n");
            }

            // Print a header
            System.out.println("Metric set:");
            System.out.printf("%s%s\n", namedMetricSet.getName(),
                    namedMetricSet.hasDescription() ? String.format(" (%s)",namedMetricSet.getDescription()) : "");
            // Add the metrics to a new registry and use the console reporter to display the results
            final MetricRegistry metricRegistry = namedMetricSet.toMetricRegistry();
            final ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .build();
            consoleReporter.report();
        }

        return null;
    }
}
