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
