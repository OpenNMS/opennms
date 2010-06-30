/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 May 10: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
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
package org.opennms.netmgt.snmp;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>SnmpValueType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public enum SnmpValueType {
    //  The integer values match the ASN.1 constants
    INT32(0x02, "INTEGER"),
    OCTET_STRING(0x04, "STRING"),
    NULL(0x05, "Null"),
    OBJECT_IDENTIFIER(0x06, "OID"),
    IPADDRESS(0x40, "IpAddress"),
    COUNTER32(0x41, "Counter32"),
    GAUGE32(0x42, "Gauge32"),
    TIMETICKS(0x43, "Timeticks"),
    OPAQUE(0x44, "Opaque"),
    COUNTER64(0x46, "Counter64"),
    NO_SUCH_OBJECT(0x80, "NoSuchObject"),
    NO_SUCH_INSTANCE(0x81, "NoSuchInstance"),
    END_OF_MIB(0x82, "EndOfMib");
    
    /** Constant <code>s_intMap</code> */
    private static final Map<Integer, SnmpValueType> s_intMap = new HashMap<Integer, SnmpValueType>();
    
    private int m_int;
    private String m_displayString;
    
    static {
        for (SnmpValueType type : SnmpValueType.values()) {
            s_intMap.put(new Integer(type.getInt()), type);
        }
    }

    private SnmpValueType(int i, String displayString) {
        m_int = i;
        m_displayString = displayString;
    }
    
    /**
     * <p>getInt</p>
     *
     * @return a int.
     */
    public int getInt() {
        return m_int;
    }
    
    /**
     * <p>getDisplayString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisplayString() {
        return m_displayString;
    }
    
    /**
     * <p>valueOf</p>
     *
     * @param i a int.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValueType} object.
     */
    public static SnmpValueType valueOf(int i) {
        return s_intMap.get(new Integer(i));
    }
}
