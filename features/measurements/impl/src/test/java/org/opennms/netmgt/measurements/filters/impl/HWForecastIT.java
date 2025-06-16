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

public class HWForecastIT extends AnalyticsFilterTest {

    @Test
    public void canCheckForecastSupport() {
        // Verify that this function doesn't throw any exceptions under normal circumstances
        HWForecast.checkForecastSupport();
    }

    @Test
    public void canForecastValues() throws Exception {
        FilterDef filterDef = new FilterDef("HoltWinters",
                "outputPrefix", "HW",
                "inputColumn", "X",
                "numPeriodsToForecast", "12",
                "periodInSeconds", "1",
                "confidenceLevel", "0.95");

        // Use constant values for the Y column
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i * 1000));
            table.put(i, "X", 1.0d);
        }

        // Make the forecasts
        getFilterEngine().filter(filterDef, table);

        // Original size + 12 forecasts
        Assert.assertEquals(112, table.rowKeySet().size());

        // The timestamps should be continuous
        for (long i = 0; i < 112; i++) {
            Assert.assertEquals((double) (i * 1000), table.get(i, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
        }

        // The forecasted value should be constant
        for (long i = 100; i < 112; i++) {
            Assert.assertEquals(1.0d, table.get(i, "HWFit"), 0.0001);
        }
    }
}
