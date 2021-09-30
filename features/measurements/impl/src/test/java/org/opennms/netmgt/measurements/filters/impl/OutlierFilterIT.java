/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
