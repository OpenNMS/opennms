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

@Command(scope = "opennms", name = "metrics-display", description="Display metrics from one or more metric sets.")
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
