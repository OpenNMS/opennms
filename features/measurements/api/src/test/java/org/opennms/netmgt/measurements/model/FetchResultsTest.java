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

package org.opennms.netmgt.measurements.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.Filter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class FetchResultsTest {

    @Test
    public void canConvertTableToAndFromFetchResults() {
        final double delta = 0.0000001;

        // Simple table with 3 columns and 3 rows
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        table.put(0L, Filter.TIMESTAMP_COLUMN_NAME, 0d);
        table.put(0L, "x", 1d);

        table.put(1L, Filter.TIMESTAMP_COLUMN_NAME, 100d);
        table.put(1L, "x", 1d);
        
        // Don't add values for x, but add a value for y in the last row
        table.put(2L, Filter.TIMESTAMP_COLUMN_NAME, 200d);
        table.put(2L, "y", 99d);

        // Create the fetch results using the table
        Map<String, Object> constants = Maps.newHashMap();
        FetchResults results = new FetchResults(table, 300, constants);

        // Verify
        Map<String, double[]> columns = results.getColumns();
        assertArrayEquals(columns.get("x"), new double[]{1d, 1d, Double.NaN}, delta);
        assertArrayEquals(columns.get("y"), new double[]{Double.NaN, Double.NaN, 99d}, delta);

        // Convert back to a table
        table = results.asRowSortedTable();

        // Verify
        assertEquals(3, table.columnKeySet().size());
        assertTrue(table.columnKeySet().containsAll(Lists.newArrayList(Filter.TIMESTAMP_COLUMN_NAME, "x", "y")));

        assertEquals(0d, table.get(0L, Filter.TIMESTAMP_COLUMN_NAME), delta);
        assertEquals(100d, table.get(1L, Filter.TIMESTAMP_COLUMN_NAME), delta);
        assertEquals(200d, table.get(2L, Filter.TIMESTAMP_COLUMN_NAME), delta);

        assertEquals(1d, table.get(0L, "x"), delta);
        assertEquals(1d, table.get(1L, "x"), delta);
        assertEquals(Double.NaN, table.get(2L, "x"), delta);

        assertEquals(Double.NaN, table.get(0L, "y"), delta);
        assertEquals(Double.NaN, table.get(1L, "y"), delta);
        assertEquals(99d, table.get(2L, "y"), delta);
    }
}
