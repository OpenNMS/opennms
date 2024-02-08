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
package org.opennms.netmgt.measurements.filters.impl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.FilterDef;
import org.opennms.netmgt.measurements.model.QueryMetadata;

import com.google.common.collect.RowSortedTable;

public class PercentilePerfTest extends AnalyticsFilterTest {

    /**
     * Verify that a large result set gets processed in a reasonable amount of time.
     * We limit the test with a timeout of 30 seconds, to avoid the results from flapping,
     * but we expect this to be done in significantly less time.
     *
     * @throws Exception on error
     */
    @Test(timeout=300000)
    public void canProcessResultsWithThousandsOfValues() throws Exception {
        // Build a large result set with 100k values
        final int N = 100000;
        long[] timestamps = new long[N];
        double[] values = new double[N];
        for (int i = 0; i < N; i++) {
            timestamps[i] = i * 1000;
            values[i] = i;
        }
        final Map<String, double[]> columns = new HashMap<>();
        columns.put("X", values);

        long now = System.currentTimeMillis();
        FetchResults results = new FetchResults(timestamps, columns, 1, Collections.emptyMap(), new QueryMetadata());
        RowSortedTable<Long, String, Double> table = results.asRowSortedTable();

        // Apply the filter
        double quantile = 0.95;
        FilterDef filterDef = new FilterDef("Percentile",
                "inputColumn", "X",
                "outputColumn", "Y",
                "quantile", Double.valueOf(quantile).toString());
        getFilterEngine().filter(filterDef, table);
        results = new FetchResults(table, results.getStep(), results.getConstants(), new QueryMetadata());

        // Quickly validate the results
        assertEquals(94999.95, table.get(1000L, "Y"), 0.0001);

        // Print out how long we took
        long delta = System.currentTimeMillis() - now;
        System.out.printf("%d values processed in: %.2f seconds\n", N, (double)delta / 1000d);
    }
}
