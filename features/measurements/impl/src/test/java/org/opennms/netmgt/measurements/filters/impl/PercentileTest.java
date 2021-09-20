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
