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
