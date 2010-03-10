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
// Modifications:
//
// 2007 May 29: Add "id". - dj@opennms.org
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

package org.opennms.web.element;

public class Interface {
    int m_id;
    
    int m_nodeId;

    int m_ifIndex;

    int m_ipStatus;

    String m_ipHostName;

    String m_ipAddr;

    char m_isManaged;

    String m_ipLastCapsdPoll;

    String m_snmpIpAdEntNetMask;

    String m_snmpPhysAddr;

    String m_snmpIfDescr;

    String m_snmpIfName;

    int m_snmpIfIndex;

    int m_snmpIfType;

    int m_snmpIfOperStatus;

    long m_snmpIfSpeed;

    int m_snmpIfAdminStatus;
    
    String m_snmpIfAlias;

    String m_isSnmpPrimary;
    
    char m_isSnmpPoll;
    
    String m_snmpLastSnmpPoll;

    String m_snmpLastCapsdPoll;

    public int getId() {
        return m_id;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public int getIfIndex() {
        return m_ifIndex;
    }

    public int getIpStatus() {
        return m_ipStatus;
    }

    public String getHostname() {
        return m_ipHostName;
    }

    public String getIpAddress() {
        return m_ipAddr;
    }

    public char isManagedChar() {
        return m_isManaged;
    }

    public String getLastCapsdPoll() {
        return m_ipLastCapsdPoll;
    }

    public int getSnmpIfIndex() {
        return m_snmpIfIndex;
    }

    public String getSnmpIpAdEntNetMask() {
        return m_snmpIpAdEntNetMask;
    }

    public String getPhysicalAddress() {
        return m_snmpPhysAddr;
    }

    public String getSnmpIfDescription() {
        return m_snmpIfDescr;
    }

    public String getSnmpIfName() {
        return m_snmpIfName;
    }

    public int getSnmpIfType() {
        return m_snmpIfType;
    }

    public int getSnmpIfOperStatus() {
        return m_snmpIfOperStatus;
    }

    public long getSnmpIfSpeed() {
        return m_snmpIfSpeed;
    }

    public int getSnmpIfAdminStatus() {
        return m_snmpIfAdminStatus;
    }

    public char isSnmpPollChar() {
        return m_isSnmpPoll;
    }
    
    public String getSnmpLastSnmpPoll() {
        return m_snmpLastSnmpPoll;
    }

    public String getSnmpLastCapsdPoll() {
        return m_snmpLastCapsdPoll;
    }

    public String toString() {
        return m_ipHostName;
    }

    public boolean isManaged() {
        return (m_isManaged == 'M');
    }

    public boolean isSnmpPoll() {
        return (m_isSnmpPoll == 'P');
    }

    public String getSnmpIfAlias() {
        return m_snmpIfAlias;
    }

    public String getIsSnmpPrimary() {
        return m_isSnmpPrimary;
    }

    public String getName() {
        return m_ipHostName == null? m_ipAddr : m_ipHostName;
    }
}
