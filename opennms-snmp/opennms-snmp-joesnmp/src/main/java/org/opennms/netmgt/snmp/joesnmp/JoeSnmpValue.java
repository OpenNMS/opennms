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
package org.opennms.netmgt.snmp.joesnmp;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpEndOfMibView;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpUInt32;

class JoeSnmpValue implements SnmpValue {
    SnmpSyntax m_value;
    
    JoeSnmpValue(SnmpSyntax value) {
        m_value = value;
    }
    
    JoeSnmpValue(int typeId, byte[] bytes) {
        switch(typeId) {
        case SnmpSMI.SMI_COUNTER64: {
            BigInteger val = new BigInteger(bytes);
            m_value = new SnmpCounter64(val);
            break;
        }
        case SnmpSMI.SMI_INTEGER: {
            BigInteger val = new BigInteger(bytes);
            m_value = new SnmpInt32(val.intValue());
            break;
        }
        case SnmpSMI.SMI_COUNTER32: {
            BigInteger val = new BigInteger(bytes);
            m_value = new SnmpCounter32(val.longValue());
            break;
        }
        case SnmpSMI.SMI_TIMETICKS: {
            BigInteger val = new BigInteger(bytes);
            m_value = new SnmpTimeTicks(val.longValue());
            break;
        }
        case SnmpSMI.SMI_UNSIGNED32: {
            BigInteger val = new BigInteger(bytes);
            m_value = new SnmpUInt32(val.longValue());
            break;
        }
        case SnmpSMI.SMI_IPADDRESS: {
            m_value = new SnmpIPAddress(bytes);
            break;
        }
        case SnmpSMI.SMI_OBJECTID: {
            m_value = new SnmpObjectId(new String(bytes));
            break;
        }
        case SnmpSMI.SMI_OPAQUE: {
            m_value = new SnmpOpaque(bytes);
            break;
        }
        case SnmpSMI.SMI_STRING: {
            m_value = new SnmpOctetString(bytes);
            break;
        }
        case SnmpSMI.SMI_NULL: {
            m_value = new SnmpNull();
            break;
        }
        default:
            throw new IllegalArgumentException("invaldi type id "+typeId);
        }    
    }
    
    /**
     * <p>getBytes</p>
     *
     * @return an array of byte.
     */
    public byte[] getBytes() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
        case SnmpSMI.SMI_INTEGER:
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return toBigInteger().toByteArray();
        case SnmpSMI.SMI_IPADDRESS:
            return toInetAddress().getAddress();
        case SnmpSMI.SMI_OBJECTID:
            return ((SnmpObjectId)m_value).toString().getBytes();
        case SnmpSMI.SMI_OPAQUE:
        case SnmpSMI.SMI_STRING:
            return ((SnmpOctetString)m_value).getString();
        case SnmpSMI.SMI_NULL:
            return new byte[0];
        default:
            throw new IllegalArgumentException("cannot convert "+m_value+" to a byte array");
        }
    }        

    /**
     * <p>isEndOfMib</p>
     *
     * @return a boolean.
     */
    public boolean isEndOfMib() {
        return m_value instanceof SnmpEndOfMibView;
    }
    
    /**
     * <p>isError</p>
     *
     * @return a boolean.
     */
    public boolean isError() {
        switch (getType()) {
        case SnmpValue.SNMP_NO_SUCH_INSTANCE:
        case SnmpValue.SNMP_NO_SUCH_OBJECT:
            return true;
        default:
            return false;
        }
        
    }

    /**
     * <p>isNumeric</p>
     *
     * @return a boolean.
     */
    public boolean isNumeric() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_INTEGER:
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_COUNTER64:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * <p>toInt</p>
     *
     * @return a int.
     */
    public int toInt() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
            return ((SnmpCounter64)m_value).getValue().intValue();
        case SnmpSMI.SMI_INTEGER:
            return ((SnmpInt32)m_value).getValue();
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return (int)((SnmpUInt32)m_value).getValue();
        default:
            return Integer.parseInt(m_value.toString());
        }
    }
    
    /**
     * <p>toLong</p>
     *
     * @return a long.
     */
    public long toLong() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
            return ((SnmpCounter64)m_value).getValue().longValue();
        case SnmpSMI.SMI_INTEGER:
            return ((SnmpInt32)m_value).getValue();
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return ((SnmpUInt32)m_value).getValue();
        case SnmpSMI.SMI_STRING:
	    return (convertStringToLong());
        default:
            return Long.parseLong(m_value.toString());
        }
    }


    private long convertStringToLong() {
        return Double.valueOf(m_value.toString()).longValue();
    }


    
    /**
     * <p>getType</p>
     *
     * @return a int.
     */
    public int getType() {
        return m_value.typeId();
    }

    /**
     * <p>toDisplayString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toDisplayString() {
        
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_TIMETICKS :
            return Long.toString(toLong());
        case SnmpSMI.SMI_STRING:
            return SnmpOctetString.toDisplayString((SnmpOctetString)m_value);
        default :
            return m_value.toString();
        }
    }

    /**
     * <p>toInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress toInetAddress() {
        switch (m_value.typeId()) {
            case SnmpSMI.SMI_IPADDRESS:
                return SnmpIPAddress.toInetAddress((SnmpIPAddress)m_value);
            default:
                throw new IllegalArgumentException("cannot convert "+m_value+" to an InetAddress"); 
        }
    }

    /**
     * <p>toHexString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toHexString() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_STRING:
            return SnmpOctetString.toHexString((SnmpOctetString)m_value);
        default:
            throw new IllegalArgumentException("cannt convert "+m_value+" to a HexString");
        }
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return toDisplayString();
    }

    /**
     * <p>toBigInteger</p>
     *
     * @return a {@link java.math.BigInteger} object.
     */
    public BigInteger toBigInteger() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_COUNTER64:
            return ((SnmpCounter64)m_value).getValue();
        case SnmpSMI.SMI_INTEGER:
            return BigInteger.valueOf(((SnmpInt32)m_value).getValue());
        case SnmpSMI.SMI_COUNTER32:
        case SnmpSMI.SMI_TIMETICKS:
        case SnmpSMI.SMI_UNSIGNED32:
            return BigInteger.valueOf(((SnmpUInt32)m_value).getValue());
        default:
            return new BigInteger(m_value.toString());
        }
    }

    /**
     * <p>toSnmpObjId</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId toSnmpObjId() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_OBJECTID:
            return SnmpObjId.get(((SnmpObjectId)m_value).getIdentifiers());
        default:
            throw new IllegalArgumentException("cannt convert "+m_value+" to a SnmpObjId");
        }
    }

    /**
     * <p>isDisplayable</p>
     *
     * @return a boolean.
     */
    public boolean isDisplayable() {
        if (isNumeric())
            return true;
        
        if (getType() == SnmpValue.SNMP_OBJECT_IDENTIFIER || getType() == SnmpValue.SNMP_IPADDRESS)
            return true;
        
        if (getType() == SnmpValue.SNMP_OCTET_STRING) {
            return allBytesDisplayable(getBytes());
        }
        
        return false;
    }

	private boolean allBytesDisplayable(byte[] bytes) {
		for(int i = 0; i < bytes.length; i++) {
		    byte b = bytes[i];
		    if ((b < 32 && b != 9 && b != 10 && b != 13 && b != 0) || b == 127)
		        return false;
		}
		return true;
	}

    /**
     * <p>isNull</p>
     *
     * @return a boolean.
     */
    public boolean isNull() {
        return getType() == SnmpValue.SNMP_NULL;
    }

    /**
     * <p>getSnmpSyntax</p>
     *
     * @return a {@link org.opennms.protocols.snmp.SnmpSyntax} object.
     */
    public SnmpSyntax getSnmpSyntax() {
        return m_value;
    }
    
    
    
}
