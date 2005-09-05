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
package org.opennms.netmgt.outage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

/**
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BasicNode extends BasicElement {

    private BasicNetwork m_network;
    private long m_nodeId;
    
    public BasicNode(BasicNetwork network, long nodeId) {
        m_network = network;
        m_nodeId = nodeId;
    }
    
    public long getNodeId() {
        return m_nodeId;
    }
    
    public BasicNetwork getNetwork() {
        return m_network;
    }

    public String toString() {
        return String.valueOf(m_nodeId);
    }

    /**
     * @return
     */
    public boolean isValid() {
        return !(m_nodeId == -1);
    }

    public boolean openOutageExists(Connection dbConn) throws SQLException {
        PreparedStatement openStmt = null;
        openStmt = dbConn.prepareStatement(OutageConstants.DB_COUNT_OPEN_OUTAGES_FOR_NODE);
        openStmt.setLong(1, getNodeId());
        return DbUtil.countQueryIsPositive(openStmt);
    }

    /**
     * @param dbConn
     * @param eventID
     * @param eventTime
     * @return
     * @throws SQLException
     */
    public int closeOutages(Connection dbConn, long eventID, String eventTime) throws SQLException {
    
        // Set the database commit mode
        dbConn.setAutoCommit(false);
        
        // Prepare SQL statement used to update the 'regained time' for
        // all open outage entries for the nodeid
        PreparedStatement outageUpdater = dbConn.prepareStatement(OutageConstants.DB_UPDATE_OUTAGES_FOR_NODE);
        outageUpdater.setLong(1, eventID);
        outageUpdater.setTimestamp(2, BasicNetwork.convertEventTimeIntoTimestamp(eventTime));
        outageUpdater.setLong(3, getNodeId());
        int count = outageUpdater.executeUpdate();
    
        // close statement
        outageUpdater.close();
        return count;
    }

    /**
     * @param dbConn
     * @param eventID
     * @param eventTime
     * @param writer
     * @throws SQLException
     */
    public void openOutages(Connection dbConn, long eventID, String eventTime) throws SQLException {
        // Set the database commit mode
        dbConn.setAutoCommit(false);
    
        // Prepare SQL statement used to get active services for the nodeid
        PreparedStatement activeSvcsStmt = dbConn.prepareStatement(OutageConstants.DB_GET_ACTIVE_SERVICES_FOR_NODE);
    
        Category log = ThreadCategory.getInstance(getClass());
        if (log.isDebugEnabled())
            log.debug("handleNodeDown: creating new outage entries...");
    
        // Get all active services for the nodeid
        activeSvcsStmt.setLong(1, getNodeId());
        ResultSet activeSvcsRS = activeSvcsStmt.executeQuery();
        while (activeSvcsRS.next()) {
            BasicService svc = getNetwork().getService(getNodeId(), activeSvcsRS.getString(1), activeSvcsRS.getLong(2));
            if (!svc.openOutage(dbConn, eventID, eventTime)) {
                if (log.isDebugEnabled())
                    log.debug("handleNodeDown: " + svc + " already down");
            }
    
        }
    
        // close result set
        activeSvcsRS.close();
    
        // close statements
        activeSvcsStmt.close();
    }

}
