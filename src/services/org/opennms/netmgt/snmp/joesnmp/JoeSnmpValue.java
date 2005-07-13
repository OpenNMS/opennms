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

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.protocols.snmp.SnmpCounter64;
import org.opennms.protocols.snmp.SnmpEndOfMibView;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpUInt32;

class JoeSnmpValue implements SnmpValue {
    SnmpSyntax m_value;
    
    JoeSnmpValue(SnmpSyntax value) {
        m_value = value;
    }

    public boolean isEndOfMib() {
        return m_value instanceof SnmpEndOfMibView;
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
        default:
            return Long.parseLong(m_value.toString());
        }
    }

    public String toDisplayString() {
        return m_value.toString();
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
}