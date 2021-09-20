/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import java.util.HashMap;
import java.util.Map;

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
    
    private static final Map<Integer, SnmpValueType> s_intMap = new HashMap<Integer, SnmpValueType>();
    
    private int m_int;
    private String m_displayString;
    
    static {
        for (SnmpValueType type : SnmpValueType.values()) {
            s_intMap.put(Integer.valueOf(type.getInt()), type);
        }
    }

    private SnmpValueType(int i, String displayString) {
        m_int = i;
        m_displayString = displayString;
    }
    
    public int getInt() {
        return m_int;
    }
    
    public String getDisplayString() {
        return m_displayString;
    }
    
    public static SnmpValueType valueOf(int i) {
        return s_intMap.get(Integer.valueOf(i));
    }
}
