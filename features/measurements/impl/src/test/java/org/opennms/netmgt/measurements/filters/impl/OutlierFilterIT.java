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
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class OutlierFilterIT extends AnalyticsFilterTest {
    @Test
    public void canFilterOutliers() throws Exception {
        FilterDef filterDef = new FilterDef("Outlier",
                "inputColumn", "Y");

        // Use constant values for the Y column
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, "X", (double)i);
            table.put(i, "Y", 1.0d);
        }

        // Add an outlier to the first and last elements
        table.put(0L, "Y", 9999.0d);
        table.put(99L, "Y", 9999.0d);

        // Add another one near the middle of the series
        table.put(42L, "Y", 9999.0d);

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // The Y column should be constant - no outliers
        for (long i = 1; i < 99; i++) {
            Assert.assertEquals((double) i, table.get(i, "X"), 0.0001);
            Assert.assertEquals(1.0d, table.get(i, "Y"), 0.0001);
        }

        // Outliers at the beginning and end of the series should be replaced with
        // NaN, since these can't be properly interpolated
        Assert.assertTrue(Double.isNaN(table.get(0L, "Y")));
        Assert.assertTrue(Double.isNaN(table.get(99L, "Y")));
    }

    @Test
    public void canInterpolateValues() throws Exception {
        FilterDef filterDef = new FilterDef("Outlier",
                "inputColumn", "Y",
                "quantile", "0.99");

        // Use non-constant values for the Y column
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, "X", (double)i);
            table.put(i, "Y", 8 * Math.sin(i));
        }

        // Add an outlier near the middle of the series
        table.put(42L, "Y", 9999.0d);

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        for (long i = 0; i < 100; i++) {
            Assert.assertEquals((double) i, table.get(i, "X"), 0.0001);
            double delta = 0.0001;
            if (i == 42) {
                // Use a larger delta on the same index as the outlier
                delta = 6;
            }
            Assert.assertEquals(8 * Math.sin(i), table.get(i, "Y"), delta);
        }
    }

    @Test
    public void canInterpolateLargeGaps() throws Exception {
        FilterDef filterDef = new FilterDef("Outlier",
                "inputColumn", "Y",
                "probability", "0.99");

        // Fill with NaNs
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, "X", (double)i);
            table.put(i, "Y", Double.NaN);
        }

        // Add a value near the middle, and near the ned
        table.put(42L, "Y", 1.0d);
        table.put(95L, "Y", 1.0d);

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        for (long i = 0; i < 42; i++) {
            Assert.assertEquals((double) i, table.get(i, "X"), 0.0001);
            Assert.assertEquals(Double.NaN, table.get(i, "Y"), 0.0001);
        }

        for (long i = 42; i < 96; i++) {
            Assert.assertEquals((double) i, table.get(i, "X"), 0.0001);
            Assert.assertEquals(1.0d, table.get(i, "Y"), 0.0001);
        }

        for (long i = 96; i < 100; i++) {
            Assert.assertEquals((double) i, table.get(i, "X"), 0.0001);
            Assert.assertEquals(Double.NaN, table.get(i, "Y"), 0.0001);
        }
    }

    @Test
    public void doesntFailWhenDsOnlyContainsNaNs() throws Exception {
        FilterDef filterDef = new FilterDef("Outlier",
                "inputColumn", "X",
                "probability", "0.99");

        // Fill with NaNs
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, "X", Double.NaN);
        }

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // No exception should be thrown

        // Now add a single value
        table.put(42L, "X", 3.14);

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // No exception should be thrown
    }
}
