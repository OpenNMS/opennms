//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.math.BigInteger;
import java.net.InetAddress;


public interface SnmpValue {
    // These values match the ASN.1 constants
    public final static int SNMP_INT32 = (0x02);

    public final static int SNMP_OCTET_STRING = (0x04);

    public final static int SNMP_NULL = (0x05);

    public final static int SNMP_OBJECT_IDENTIFIER = (0x06);

    public final static int SNMP_IPADDRESS = (0x40);

    public final static int SNMP_COUNTER32 = (0x41);

    public final static int SNMP_GAUGE32 = (0x42);

    public final static int SNMP_TIMETICKS = (0x43);

    public final static int SNMP_OPAQUE = (0x44);

    public final static int SNMP_COUNTER64 = (0x46);
    
    public final static int SNMP_NO_SUCH_OBJECT = (0x80);
    
    public final static int SNMP_NO_SUCH_INSTANCE = (0x81);

    public final static int SNMP_END_OF_MIB = (0x82);
    
    public abstract boolean isEndOfMib();
    
    public abstract boolean isError();

    public abstract boolean isNull();

    public abstract boolean isDisplayable();

    public abstract boolean isNumeric();

    public abstract int toInt();

    public abstract String toDisplayString();

    public abstract InetAddress toInetAddress();

    public abstract long toLong();
    
    public abstract BigInteger toBigInteger();

    public abstract String toHexString();
    
    public abstract int getType();
    
    public abstract byte[] getBytes();

    public abstract SnmpObjId toSnmpObjId();


}
