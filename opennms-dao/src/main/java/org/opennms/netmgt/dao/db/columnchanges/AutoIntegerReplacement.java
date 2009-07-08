/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;

public class AutoIntegerReplacement implements ColumnChangeReplacement {
    private int m_value;
    
    public AutoIntegerReplacement(int initialValue) {
        m_value = initialValue;
    }
    
    public int getInt() {
        return m_value++;
    }
    
    public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) {
        return getInt();
    }

    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    public void close() {
    }
}
