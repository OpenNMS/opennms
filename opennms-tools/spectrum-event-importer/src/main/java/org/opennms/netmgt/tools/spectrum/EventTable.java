/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tools.spectrum;

import java.util.HashMap;

public class EventTable extends HashMap<Integer,String> {
    private static final long serialVersionUID = 7016759899154786992L;
    private String m_tableName;
    
    public EventTable(String name) {
        super();
        
        if (name == null) {
            throw new IllegalArgumentException("The name must not be null");
        }
        m_tableName = name;
    }

    public String getTableName() {
        return m_tableName;
    }

    public void setTableName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name must not be null");
        }
        m_tableName = name;
    }
    
    public void put(String keyString, String valueString) {
        int keyInt;
        if (keyString.matches("^0x[0-9A-Fa-f]+$")) {
            keyInt = Integer.parseInt(keyString.substring(2), 16);
        } else if (keyString.matches("^0[0-7]+$")) {
            keyInt = Integer.parseInt(keyString.substring(1), 8);
        } else {
            keyInt = Integer.parseInt(keyString);
        }
        
        put(keyInt, valueString);
    }

}