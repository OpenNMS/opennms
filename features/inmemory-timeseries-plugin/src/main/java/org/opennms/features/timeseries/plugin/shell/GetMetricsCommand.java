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
package org.opennms.features.timeseries.plugin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.features.timeseries.plugin.InMemoryStorage;

/**
 * Simple diagnostic for looking at timeseries data
 *
 */
@Command(scope = "opennms", name="get-tss-plugin-metrics", description = "Print all collected metrics")
@Service
public class GetMetricsCommand implements Action {

    @Reference
    private TimeSeriesStorage storage;

    @Override
    public Object execute() {
        ((InMemoryStorage)storage).getAllMetrics().entrySet().forEach(entry -> {
            System.out.println(String.format("Metric <%s> has %d data points",
                    entry.getKey().getKey(),
                    entry.getValue().size()));
            entry.getValue().stream().forEach(dataPoint ->
                System.out.printf("\t%d:%f\n", dataPoint.getTime().toEpochMilli(), dataPoint.getValue()));
        });
        return null;
    }

}
