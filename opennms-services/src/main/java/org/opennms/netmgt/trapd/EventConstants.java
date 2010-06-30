//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// EventConstants.java,v 1.1.1.1 2001/11/11 17:34:38 ben Exp
//

package org.opennms.netmgt.trapd;

import java.math.BigInteger;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class EventConstants extends Object {

    public final static String TYPE_STRING = "string";

    public final static String TYPE_INT = "int";

    public final static String TYPE_SNMP_OCTET_STRING = "OctetString";

    public final static String TYPE_SNMP_INT32 = "Int32";

    public final static String TYPE_SNMP_NULL = "Null";

    public final static String TYPE_SNMP_OBJECT_IDENTIFIER = "ObjectIdentifier";

    public final static String TYPE_SNMP_IPADDRESS = "IpAddress";

    public final static String TYPE_SNMP_TIMETICKS = "TimeTicks";

    public final static String TYPE_SNMP_COUNTER32 = "Counter32";

    public final static String TYPE_SNMP_GAUGE32 = "Gauge32";

    public final static String TYPE_SNMP_OPAQUE = "Opaque";

    public final static String TYPE_SNMP_SEQUENCE = "Sequence";

    public final static String TYPE_SNMP_COUNTER64 = "Counter64";

    public final static String XML_ENCODING_TEXT = "text";

    public final static String XML_ENCODING_BASE64 = "base64";

    public static final String XML_ENCODING_MAC_ADDRESS = "macAddress";

    public final static SnmpObjId OID_SNMP_IFINDEX = SnmpObjId.get(".1.3.6.1.2.1.2.2.1.1");


    /** Empty, private constructor so this object cannot be instantiated. */
    private EventConstants() {
    }

    /**
     * Converts the value of the instance to a string representation in the
     * correct encoding system.
     * 
     */
    public static String toString(String encoding, Object value) {
        if (encoding == null || value == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String result = null;

        if (XML_ENCODING_TEXT.equals(encoding)) {
            if (value instanceof String)
                result = (String) value;
            else if (value instanceof Number)
                result = value.toString();
            else if (value instanceof SnmpValue)
                result = ((SnmpValue)value).toString();
        } else if (XML_ENCODING_BASE64.equals(encoding)) {
            if (value instanceof String)
                result = new String(Base64.encodeBase64(((String) value).getBytes()));
            else if (value instanceof Number) {
                
                byte[] ibuf = null;
                if (value instanceof BigInteger)
                    ibuf = ((BigInteger) value).toByteArray();
                else
                    ibuf = BigInteger.valueOf(((Number) value).longValue()).toByteArray();

                result = new String(Base64.encodeBase64(ibuf));
            }
            else if (value instanceof SnmpValue) {
                SnmpValue snmpValue = (SnmpValue)value;
                result = new String(Base64.encodeBase64(snmpValue.getBytes()));
            }
        } else if (XML_ENCODING_MAC_ADDRESS.equals(encoding)) {
            if (value instanceof SnmpValue) {
                SnmpValue snmpValue = (SnmpValue)value;
                StringBuffer macAddress = new StringBuffer();
                byte[] bytes = snmpValue.getBytes();
                for (int i = 0; i < bytes.length; i++) {
                    if (i > 0) macAddress.append(":");
                    macAddress.append(String.format("%02X", bytes[i]));
                }
                result = macAddress.toString();
            }
        }
        
        if (result == null)
            throw new IllegalArgumentException("unable to encode "+value+" of type "+value.getClass());

        return result;
    }

}
