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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

@Command(scope = "opennms", name = "metrics-list", description="List the available metric sets.")
@Service
public class MetricsListCommand implements Action {

    @Reference
    private BundleContext bundleContext;

    @Override
    public Object execute() {
        final List<NamedMetricSet> metricSets = NamedMetricSet.getNamedMetricSetsInContext(bundleContext);
        if (metricSets.size() < 1) {
            System.out.println("(No metric sets are currently available.)");
            return null;
        }

        // Determine the length of the name
        int maxNameLength = metricSets.stream().mapToInt(m -> m.getName().length()).max().getAsInt();
        final String format = String.format("%%-%ds\t%%s\n", maxNameLength);

        System.out.printf(format, "Name", "Description");
        for (NamedMetricSet namedMetricSet : metricSets) {
            System.out.printf(format, namedMetricSet.getName(),
                    namedMetricSet.hasDescription() ? namedMetricSet.getDescription() : "(No description)");
        }
        System.out.println();

        return null;
    }

}
