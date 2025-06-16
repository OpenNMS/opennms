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
package org.opennms.netmgt.timeseries.shell;

import java.io.PrintStream;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.stats.StatisticsCollector;

/**
 * Shows statistics of the time series layer.
 * Install: feature:install opennms-timeseries-shell
 * Usage: type opennms:ts-stats in karaf console
 */
@Command(scope = "opennms", name = "ts-stats",
        description = "Prints statistics about the timeseries integration layer.")
@Service
public class TimeseriesStatsCommand implements Action {

    @Reference
    private TimeseriesStorageManager storageManager;

    @Reference
    private StatisticsCollector stats;


    @Override
    public Object execute() throws Exception {
        PrintStream out = System.out;
        out.println("Active TimeSeriesStorage plugin:");
        out.println(storageManager.get().getClass().getName());
        out.println();
        out.println("Metrics with highest number of tags:");
        stats.getTopNMetricsWithMostTags().stream().map(this::toString).forEach(out::println);
        out.println();
        out.println("Tags with highest number of unique values (top 100):");
        stats.getTopNTags().stream().limit(100).forEach(out::println);
        return null;
    }

    private String toString(final Metric metric) {
        return metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue() + "/"
                + metric.getFirstTagByKey(IntrinsicTagNames.name).getValue() +
                "\n    metaTags:      " + metric.getMetaTags().toString() +
                "\n    externalTags: " + metric.getExternalTags().toString();
    }
}
