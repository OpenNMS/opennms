//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a Service in the ifServices table
 * @author brozow
 */
public class BasicService extends BasicElement {

    private long m_nodeId;
    private String m_ipAddr;
    private long m_serviceId;

    public BasicService(long nodeId, String ipAddr, long serviceId) {
        m_nodeId = nodeId;
        m_ipAddr = ipAddr;
        m_serviceId = serviceId;
    }
    
    public BasicService(BasicInterface iface, long serviceId) {
        this(iface.getNodeId(), iface.getIpAddr(), serviceId);
    }

    public String getIpAddr() {
        return m_ipAddr;
    }
    public long getNodeId() {
        return m_nodeId;
    }
    public long getServiceId() {
        return m_serviceId;
    }
    
    public boolean isValid() {
        return !(m_nodeId == -1 || m_ipAddr == null || m_serviceId == -1);
    }
    
    public String toString() {
        return m_nodeId+"/"+m_ipAddr+"/"+m_serviceId;
    }

    public boolean openOutageExists(Connection dbConn) throws SQLException {
        PreparedStatement openStmt = null;
        openStmt = dbConn.prepareStatement(OutageConstants.DB_OPEN_RECORD);
        openStmt.setLong(1, getNodeId());
        openStmt.setString(2, getIpAddr());
        openStmt.setLong(3, getServiceId());
        return DbUtil.countQueryIsPositive(openStmt);
    }
}
