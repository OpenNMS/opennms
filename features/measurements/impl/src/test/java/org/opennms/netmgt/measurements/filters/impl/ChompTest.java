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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class ChompTest extends AnalyticsFilterTest {

    @Test
    public void canCutoffRows() throws Exception {
        FilterDef filterDef = new FilterDef("Chomp",
                "cutoffDate", "60000",
                "stripNaNs", "false");

        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        long k = 0;

        // Add some NaNs to the table
        for (; k < 10; k++) {
            table.put(k, Filter.TIMESTAMP_COLUMN_NAME, (double)k*1000);
            table.put(k, "X", Double.NaN);
        }

        // Add some values to the table
        for (; k < 90; k++) {
            table.put(k, Filter.TIMESTAMP_COLUMN_NAME, (double)k*1000);
            table.put(k, "X", (double)k);
        }

        // Verify the initial size
        Assert.assertEquals(90, table.rowKeySet().size());

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        Assert.assertEquals(30, table.rowKeySet().size());
        for (long i = 0; i < 5; i++) {
            Assert.assertEquals((double) (i + 60) * 1000, table.get(i, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals((double) (i + 60), table.get(i, "X"), 0.0001);
        }
    }

    @Test
    public void canStripNaNs() throws Exception {
        FilterDef filterDef = new FilterDef("Chomp",
                "stripNaNs", "true");

        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        long k = 0;

        // Add some NaNs to the table
        for (; k < 10; k++) {
            table.put(k, Filter.TIMESTAMP_COLUMN_NAME, (double)k);
            table.put(k, "X", Double.NaN);
        }

        // Add some values to the table
        for (; k < 90; k++) {
            table.put(k, Filter.TIMESTAMP_COLUMN_NAME, (double)k);
            table.put(k, "X", (double)k);
        }

        // Add some more NaNs
        for (; k < 100; k++) {
            table.put(k, Filter.TIMESTAMP_COLUMN_NAME, (double)k);
            table.put(k, "X", Double.NaN);
        }

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        Assert.assertEquals(80, table.rowKeySet().size());
        for (long i = 0; i < 80; i++) {
            Assert.assertEquals((double) (i + 10), table.get(i, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals((double) (i + 10), table.get(i, "X"), 0.0001);
        }
    }

    @Test
    public void doesntFailOnEmtpyDs() throws Exception {
        FilterDef filterDef = new FilterDef("Chomp");
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        getFilterEngine().filter(filterDef, table);
    }
}
