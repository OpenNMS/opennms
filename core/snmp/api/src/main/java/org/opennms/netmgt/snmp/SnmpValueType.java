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
