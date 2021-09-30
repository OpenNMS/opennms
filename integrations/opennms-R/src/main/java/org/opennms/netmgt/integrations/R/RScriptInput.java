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
