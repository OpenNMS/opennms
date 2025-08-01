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
package org.opennms.netmgt.measurements.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.Filter;

import com.google.common.collect.Lists;
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
        FetchResults results = new FetchResults(table, 300, new HashMap<String,Object>(), null);

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
