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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;

/**
 * Used to group all of the arguments/values retrieved from the script.
 *
 * @see {@link org.opennms.netmgt.integrations.R.RScriptExecutor}
 * @author jwhite
 */
public class RScriptOutput {
    private final ImmutableTable<Long, String, Double> m_table;

    public RScriptOutput(ImmutableTable<Long, String, Double> table) {
        Preconditions.checkNotNull(table, "table argument");
        m_table = table;
    }

    public ImmutableTable<Long, String, Double> getTable() {
        return m_table;
    }
}
