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

public class JEXLFilterTest extends AnalyticsFilterTest {

    @Test
    public void canDuplicateColumn() throws Exception {
        String jexlExpression = "for (k : table.rowKeySet()) {table.put(k, \"Z\", table.get(k, \"X\"));}";
        FilterDef filterDef = new FilterDef("JEXL",
                "expression", jexlExpression);

        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        // Add some values to the table
        for (long k = 0; k < 90; k++) {
            table.put(k, Filter.TIMESTAMP_COLUMN_NAME, (double)k*1000);
            table.put(k, "X", (double)k);
        }

        // Apply the filter
        getFilterEngine().filter(filterDef, table);

        // Verify
        Assert.assertEquals(90, table.rowKeySet().size());
        for (long k = 0; k < 90; k++) {
            Assert.assertEquals((double)k*1000, table.get(k, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals((double)k, table.get(k, "Z"), 0.0001);
        }
    }
}
