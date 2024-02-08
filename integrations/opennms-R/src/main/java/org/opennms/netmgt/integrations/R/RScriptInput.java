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

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.RowSortedTable;

/**
 * Used to group all of the arguments/values passed to the script.
 *
 * @see {@link org.opennms.netmgt.integrations.R.RScriptExecutor}
 * @author jwhite
 */
public class RScriptInput {
    private final Map<String, Object> m_arguments;
    private final RowSortedTable<Long, String, Double> m_table;

    public RScriptInput(RowSortedTable<Long, String, Double> table) {
        m_table = table;
        m_arguments = Maps.newHashMap();
    }

    public RScriptInput(RowSortedTable<Long, String, Double> table, Map<String, Object> arguments) {
        m_table = table;
        m_arguments = arguments;
    }

    public RowSortedTable<Long, String, Double> getTable() {
        return m_table;
    }

    public Map<String, Object> getArguments() {
        return m_arguments;
    }
}
