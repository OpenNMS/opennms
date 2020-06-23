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

package org.opennms.netmgt.integrations.R;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class RScriptExecutorIT {
    @Test
    public void canRunScriptInClasspath() throws RScriptException {
        long N = 8192;

        // Generate data for the input table
        RowSortedTable<Long, String, Double> expectedTable = TreeBasedTable.create();
        for (long i = 0; i < N; i++) {
            expectedTable.put(i, "x", Double.valueOf(i));
            expectedTable.put(i, "y", Double.valueOf(i*2));
        }

        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("unescapable_option", "\"'\\U0027\\U0027");

        // Execute the script
        RScriptExecutor executor = new RScriptExecutor();
        RScriptOutput output = executor.exec("/echo.R", new RScriptInput(expectedTable, arguments));
        ImmutableTable<Long, String, Double> actualTable = output.getTable();

        // Expect the same table back
        for (long i = 0; i < N; i++) {
            assertEquals(i, actualTable.get(i, "x"), 0.0001);
            assertEquals(i*2, actualTable.get(i, "y"), 0.0001);
        }
    }
}
