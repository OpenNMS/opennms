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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

@RunWith(Parameterized.class)
public class PercentileTest extends AnalyticsFilterTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 {
                     new double[] { 0, 0 ,0, 0, 0 },
                     0.95,
                     0
                 },
                 {
                     new double[] { 2, 2 ,2, Double.NaN, 2, Double.NaN, 2, 2, 2, 1000 },
                     0.7,
                     2
                 },
                 {
                     new double[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
                     0.5,
                     4.5
                 }
           });
    }
    
    private double[] values;

    private double quantile;

    private double expected;

    public PercentileTest(double[] values, double quantile, double expected) {
        this.values = values;
        this.quantile = quantile;
        this.expected = expected;
    }

    @Test
    public void canCaculatePercentile() throws Exception {
        FilterDef filterDef = new FilterDef("Percentile",
                "inputColumn", "X",
                "outputColumn", "Y",
                "quantile", Double.valueOf(quantile).toString());

        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        // Add some values to the table
        for (int k = 0; k < values.length; k++) {
            table.put(Long.valueOf(k), Filter.TIMESTAMP_COLUMN_NAME, (double)k*1000);
            table.put(Long.valueOf(k), "X", values[k]);
        }

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        Assert.assertEquals(values.length, table.rowKeySet().size());
        for (long k = 0; k < values.length; k++) {
            Assert.assertEquals((double)k*1000, table.get(k, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals(expected, table.get(k, "Y"), 0.0001);
        }
    }
}
