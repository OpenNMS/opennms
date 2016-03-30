/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
public class DerivativeTest extends AnalyticsFilterTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                 {
                     new double[] { 1, 1, 1, 1, 1 },
                     new double[] { Double.NaN, 0, 0, 0, 0 }
                 },
                 {
                     new double[] { 1, 1, 1, Double.NaN, 1 },
                     new double[] { Double.NaN, 0, 0, Double.NaN, Double.NaN }
                 },
                 {
                     new double[] { 1, 2, 3, 4, Double.NaN, 100, 110, 130, 160 },
                     new double[] { Double.NaN, 1, 1, 1, Double.NaN, Double.NaN, 10, 20, 30 }
                 }
           });
    }

    private double[] inputValues;

    private double[] outputValues;

    public DerivativeTest(double[] inputValues, double[] outputValues) {
        this.inputValues = inputValues;
        this.outputValues = outputValues;
    }

    @Test
    public void canCaculatePercentile() throws Exception {
        FilterDef filterDef = new FilterDef("Derivative",
                "inputColumn", "X",
                "outputColumn", "Y");

        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        // Add some values to the table
        for (int k = 0; k < inputValues.length; k++) {
            table.put(Long.valueOf(k), Filter.TIMESTAMP_COLUMN_NAME, (double)k*1000);
            table.put(Long.valueOf(k), "X", inputValues[k]);
        }

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        Assert.assertEquals(inputValues.length, table.rowKeySet().size());
        for (long k = 0; k < inputValues.length; k++) {
            Assert.assertEquals((double)k*1000, table.get(k, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals(outputValues[(int)k], table.get(k, "Y"), 0.0001);
        }
    }
}
