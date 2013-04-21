/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp.joesnmp;

import java.math.BigInteger;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.AbstractSnmpValue;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpCounter32;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpEndOfMibView;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpNoSuchInstance;
import org.opennms.protocols.snmp.SnmpNoSuchObject;
import org.opennms.protocols.snmp.SnmpNull;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpOpaque;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpUInt32;

class JoeSnmpValue extends AbstractSnmpValue {
    SnmpSyntax m_value;
    
    JoeSnmpValue(final SnmpSyntax value) {
        m_value = value;
    }
    
    JoeSnmpValue(final int typeId, final byte[] bytes) {
        switch(typeId) {
        case SnmpSMI.SMI_COUNTER64: {
            m_value = new SnmpCounter64(new BigInteger(bytes));
            break;
        }
        case SnmpSMI.SMI_INTEGER: {
            m_value = new SnmpInt32(new BigInteger(bytes).intValue());
            break;
        }
        case SnmpSMI.SMI_COUNTER32: {
            m_value = new SnmpCounter32(new BigInteger(bytes).longValue());
            break;
        }
        case SnmpSMI.SMI_TIMETICKS: {
            m_value = new SnmpTimeTicks(new BigInteger(bytes).longValue());
            break;
        }
        case SnmpSMI.SMI_UNSIGNED32: {
            m_value = new SnmpUInt32(new BigInteger(bytes).longValue());
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
        case SnmpSMI.SMI_ENDOFMIBVIEW: {
        	m_value = new SnmpEndOfMibView();
        	break;
        }
        case SnmpSMI.SMI_NOSUCHINSTANCE: {
        	m_value = new SnmpNoSuchInstance();
        	break;
        }
        case SnmpSMI.SMI_NOSUCHOBJECT: {
        	m_value = new SnmpNoSuchObject();
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
        case SnmpSMI.SMI_ENDOFMIBVIEW:
        case SnmpSMI.SMI_NOSUCHINSTANCE:
        case SnmpSMI.SMI_NOSUCHOBJECT:
        case SnmpSMI.SMI_NULL:
            return new byte[0];
        default:
            throw new IllegalArgumentException("cannot convert "+m_value+" to a byte array");
        }
    }        

    public boolean isEndOfMib() {
        return m_value instanceof SnmpEndOfMibView;
    }
    
    public boolean isError() {
        switch (getType()) {
        case SnmpValue.SNMP_NO_SUCH_INSTANCE:
        case SnmpValue.SNMP_NO_SUCH_OBJECT:
            return true;
        default:
            return false;
        }
        
    }

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


    
    public int getType() {
        return m_value.typeId();
    }

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

    public InetAddress toInetAddress() {
        switch (m_value.typeId()) {
            case SnmpSMI.SMI_IPADDRESS:
                return SnmpIPAddress.toInetAddress((SnmpIPAddress)m_value);
            default:
                throw new IllegalArgumentException("cannot convert "+m_value+" to an InetAddress"); 
        }
    }

    public String toHexString() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_STRING:
            return SnmpOctetString.toHexString((SnmpOctetString)m_value);
        default:
            throw new IllegalArgumentException("cannt convert "+m_value+" to a HexString");
        }
    }
    
    public String toString() {
        return toDisplayString();
    }

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

    public SnmpObjId toSnmpObjId() {
        switch (m_value.typeId()) {
        case SnmpSMI.SMI_OBJECTID:
            return SnmpObjId.get(((SnmpObjectId)m_value).getIdentifiers());
        default:
            throw new IllegalArgumentException("cannt convert "+m_value+" to a SnmpObjId");
        }
    }

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

    public boolean isNull() {
        return getType() == SnmpValue.SNMP_NULL;
    }

    public SnmpSyntax getSnmpSyntax() {
        return m_value;
    }
    
    @Override
    public int hashCode() {
        if (m_value == null) return 2677;
        return m_value.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
           if (obj == null) return false;
           if (obj == this) return true;
           if (obj.getClass() != getClass()) return false;

           final JoeSnmpValue that = (JoeSnmpValue)obj;
           return m_value == null ? that.m_value == null : m_value.equals(that.m_value);

    }
    
}
