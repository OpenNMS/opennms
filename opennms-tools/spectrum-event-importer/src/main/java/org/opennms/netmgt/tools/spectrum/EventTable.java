/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 9, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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