/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/


/**
 * <p>AutoIntegerReplacement class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.core.db.install.columnchanges;

import java.sql.ResultSet;
import java.util.Map;

import org.opennms.core.db.install.ColumnChange;
import org.opennms.core.db.install.ColumnChangeReplacement;

public class AutoIntegerReplacement implements ColumnChangeReplacement {
    private int m_value;
    
    /**
     * <p>Constructor for AutoIntegerReplacement.</p>
     *
     * @param initialValue a int.
     */
    public AutoIntegerReplacement(int initialValue) {
        m_value = initialValue;
    }
    
    /**
     * <p>getInt</p>
     *
     * @return a int.
     */
    public int getInt() {
        return m_value++;
    }
    
    /** {@inheritDoc} */
    public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) {
        return getInt();
    }

    /**
     * <p>addColumnIfColumnIsNew</p>
     *
     * @return a boolean.
     */
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    /**
     * <p>close</p>
     */
    public void close() {
    }
}
