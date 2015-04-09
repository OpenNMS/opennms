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

public class HWForecastTest {
    @Test
    public void canForecastValues() throws Exception {
        final String qs = "ANALYTICS:HoltWinters=HW:X:12:1:0.95";
        RrdDataSourceFilter dse = new RrdDataSourceFilter(qs);

        // Use constant values for the Y column
        RowSortedTable<Integer, String, Double> table = TreeBasedTable.create();
        for (int i = 0; i < 100; i++) {
            table.put(i, "Timestamp", (double)(i * 1000));
            table.put(i, "X", 1.0d);
        }
        assertEquals(100, table.rowKeySet().size());

        // Make the forecasts
        dse.filter(table);

        // Original size + 12 forecasts
        assertEquals(112, table.rowKeySet().size());

        // The timestamps should be continuous
        for (int i = 0; i < 112; i++) {
            assertEquals((double)(i * 1000), table.get(i, "Timestamp"), 0.0001);
        }

        // The forecasted value should be constant
        for (int i = 100; i < 112; i++) {
            assertEquals(1.0d, table.get(i, "HWFit"), 0.0001);
            assertEquals(1.0d, table.get(i, "HWLwr"), 0.0001);
            assertEquals(1.0d, table.get(i, "HWUpr"), 0.0001);
        }
    }
}
