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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * Represents a Service in the ifServices table
 * @author brozow
 */
public class BasicService extends BasicElement {

    private BasicInterface m_iface;
    private long m_serviceId;

    public BasicService(BasicInterface iface, long serviceId) {
        m_iface = iface;
        m_serviceId = serviceId;
    }
    
    public BasicInterface getInterface() {
        return m_iface;
    }
    
    public BasicNode getNode() {
        return m_iface.getNode();
    }
    
    public BasicNetwork getNetwork() {
        return m_iface.getNetwork();
    }

    public long getNodeId() {
       return m_iface.getNodeId();
    }
    
    public String getIpAddr() {
        return m_iface.getIpAddr();
    }
    
    public long getServiceId() {
        return m_serviceId;
    }
    
    public boolean isValid() {
        return m_iface.isValid() && m_serviceId != -1;
    }
    
    public String toString() {
        return getNodeId()+"/"+getIpAddr()+"/"+m_serviceId;
    }

    public boolean openOutageExists(Connection dbConn) throws SQLException {
        PreparedStatement openStmt = null;
        openStmt = dbConn.prepareStatement(OutageConstants.DB_COUNT_OPEN_OUTAGES_FOR_SVC);
        openStmt.setLong(1, getNodeId());
        openStmt.setString(2, getIpAddr());
        openStmt.setLong(3, getServiceId());
        return DbUtil.countQueryIsPositive(openStmt);
    }

    /**
     * @param dbConn
     * @param eventID
     * @param eventTime
     * @throws SQLException
     */
    public void closeOutage(Connection dbConn, long eventID, String eventTime) throws SQLException {
        // Set the database commit mode
        dbConn.setAutoCommit(false);
    
        // Prepare SQL statement used to update the 'regained time' in
        // an open entry
        PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGE_FOR_SERVICE);
        outageUpdater.setLong(1, eventID);
        outageUpdater.setTimestamp(2, BasicNetwork.convertEventTimeIntoTimestamp(eventTime));
        outageUpdater.setLong(3, getNodeId());
        outageUpdater.setString(4, getIpAddr());
        outageUpdater.setLong(5, getServiceId());
        outageUpdater.executeUpdate();
    
        // close statement
        outageUpdater.close();
    }

    /**
     * @param dbConn
     * @param eventID
     * @param eventTime
     * @return
     * @throws SQLException
     */
    public boolean openOutage(Connection dbConn, long eventID, String eventTime) throws SQLException {
        // check that there is no 'open' entry already
        if (openOutageExists(dbConn)) 
            return false;
        
        Category log = ThreadCategory.getInstance(OutageWriter.class);
        // Set the database commit mode
        dbConn.setAutoCommit(false);
        
        PreparedStatement newOutageWriter = null;
        if (log.isDebugEnabled())
            log.debug("openOutage: creating new outage entry for "+this+" ...");
        newOutageWriter = dbConn.prepareStatement(OutageConstants.DB_INS_NEW_OUTAGE);
        newOutageWriter.setLong(1, getNextOutageId(dbConn));
        newOutageWriter.setLong(2, eventID);
        newOutageWriter.setLong(3, getNodeId());
        newOutageWriter.setString(4, getIpAddr());
        newOutageWriter.setLong(5, getServiceId());
        newOutageWriter.setTimestamp(6, BasicNetwork.convertEventTimeIntoTimestamp(eventTime));
        
        
        // execute
        newOutageWriter.executeUpdate();
        
        // close statement
        newOutageWriter.close();
        return true;
        
    }

}
