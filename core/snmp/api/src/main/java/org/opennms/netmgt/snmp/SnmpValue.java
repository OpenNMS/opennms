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

import java.math.BigInteger;
import java.net.InetAddress;

public interface SnmpValue {
    // These values match the ASN.1 constants
    public static final int SNMP_INT32 = (0x02);

    public static final int SNMP_OCTET_STRING = (0x04);

    public static final int SNMP_NULL = (0x05);

    public static final int SNMP_OBJECT_IDENTIFIER = (0x06);

    public static final int SNMP_IPADDRESS = (0x40); // 64

    public static final int SNMP_COUNTER32 = (0x41); // 65

    public static final int SNMP_GAUGE32 = (0x42); // 66

    public static final int SNMP_TIMETICKS = (0x43); // 67

    public static final int SNMP_OPAQUE = (0x44); // 68

    public static final int SNMP_COUNTER64 = (0x46); // 70
    
    public static final int SNMP_NO_SUCH_OBJECT = (0x80); // 128
    
    public static final int SNMP_NO_SUCH_INSTANCE = (0x81); // 129

    public static final int SNMP_END_OF_MIB = (0x82); // 8*16 + 2 = 130
    
    boolean isEndOfMib();
    
    boolean isError();

    boolean isNull();

    boolean isDisplayable();

    boolean isNumeric();

    int toInt();

    String toDisplayString();

    InetAddress toInetAddress();

    long toLong();
    
    BigInteger toBigInteger();

    String toHexString();
    
    int getType();
    
    byte[] getBytes();

    SnmpObjId toSnmpObjId();
    
    default boolean isDouble() {return false;}
    
    default Double toDouble() {return null;}
}
