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
}
