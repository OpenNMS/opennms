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
