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
import org.opennms.netmgt.integrations.R.RScriptException;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class HWForecastIT extends AnalyticsFilterTest {

    @Test
    public void canCheckForecastSupport() throws RScriptException  {
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
            Assert.assertEquals(1.0d, table.get(i, "HWLwr"), 0.0001);
            Assert.assertEquals(1.0d, table.get(i, "HWUpr"), 0.0001);
        }
    }
}
