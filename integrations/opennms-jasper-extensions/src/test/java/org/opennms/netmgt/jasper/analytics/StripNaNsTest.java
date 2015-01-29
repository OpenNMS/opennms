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

package org.opennms.netmgt.jasper.analytics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class StripNaNsTest {
    @Test
    public void canStripNaNs() throws Exception {
        final String qs = "ANALYTICS:StripNaNs=1";
        RrdDataSourceFilter dse = new RrdDataSourceFilter(qs);

        RowSortedTable<Integer, String, Double> table = TreeBasedTable.create();
        int k = 0;
        
        // Add some NaNs to the table
        for (; k < 10; k++) {
            table.put(k, "Timestamp", (double)k);
            table.put(k, "X", Double.NaN);
        }

        // Add some values to the table
        for (; k < 90; k++) {
            table.put(k, "Timestamp", (double)k);
            table.put(k, "X", (double)k);
        }

        // Add some more NaNs
        for (; k < 100; k++) {
            table.put(k, "Timestamp", (double)k);
            table.put(k, "X", Double.NaN);
        }

        // Apply the filter
        dse.filter(table);

        // Verify
        assertEquals(80, table.rowKeySet().size());
        for (int i = 0; i < 80; i++) {
            assertEquals((double)(i+10), table.get(i, "Timestamp"), 0.0001);
            assertEquals((double)(i+10), table.get(i, "X"), 0.0001);
        };
    }
}
