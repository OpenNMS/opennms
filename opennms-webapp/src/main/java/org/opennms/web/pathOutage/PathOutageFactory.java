//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Apr 05: Add method to read the critical path
//              (getCriticalPath). - ayres@opennms.org
// 2007 Jul 24: Organize imports, Java 5 generics. - dj@opennms.org
// 2006 Apr 27: reworked getLabelAndStatus
// 2006 Apr 25: replaced getNodeLabelAndColor with getLabelAndStatus to
//              speed things up
// 2006 Apr 17: Created file
//
// Orignal code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.pathOutage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.web.WebSecurityUtils;

/**
 * The source for all path outage business objects (nodes, critical path IPs,
 * critical path service names). Encapsulates all lookup functionality for 
 * these business objects in one place.
 * 
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class PathOutageFactory extends Object {

    private static final String GET_CRITICAL_PATHS = "SELECT DISTINCT criticalpathip, criticalpathservicename FROM pathoutage ORDER BY criticalpathip, criticalpathservicename";

    private static final String GET_CRITICAL_PATH_BY_NODEID = "SELECT criticalpathip, criticalpathservicename FROM pathoutage WHERE nodeid=?";
    
    private static final String GET_NODES_IN_PATH = "SELECT DISTINCT pathoutage.nodeid FROM pathoutage, ipinterface WHERE pathoutage.criticalpathip=? AND pathoutage.criticalpathservicename=? AND pathoutage.nodeid=ipinterface.nodeid AND ipinterface.ismanaged!='D' ORDER BY nodeid";

    private static final String COUNT_MANAGED_SVCS = "SELECT count(*) FROM ifservices WHERE status ='A' and nodeid=?";

    private static final String COUNT_OUTAGES = "SELECT count(*) FROM outages WHERE svcregainedeventid IS NULL and nodeid=?";

    private static final String COUNT_NODES_IN_PATH = "SELECT count(DISTINCT pathoutage.nodeid) FROM pathoutage, ipinterface WHERE pathoutage.criticalpathip=? AND pathoutage.criticalpathservicename=? AND pathoutage.nodeid=ipinterface.nodeid AND ipinterface.ismanaged!='D'";

    private static final String GET_NODELABEL_BY_IP = "SELECT nodelabel FROM node WHERE nodeid IN (SELECT nodeid FROM ipinterface WHERE ipaddr=? AND ismanaged!='D')";

    private static final String GET_NODEID_BY_IP = "SELECT nodeid FROM ipinterface WHERE ipaddr=? AND ismanaged!='D' ORDER BY nodeid DESC LIMIT 1";

    private static final String GET_NODELABEL_BY_NODEID = "SELECT nodelabel FROM node WHERE nodeid=?";

    private static final String GET_CRITICAL_PATH_STATUS = "SELECT count(*) FROM outages WHERE ipaddr=? AND ifregainedservice IS NULL AND serviceid=(SELECT serviceid FROM service WHERE servicename=?)";

    private static final String IS_CRITICAL_PATH_MANAGED = "SELECT count(*) FROM ifservices WHERE ipaddr=? AND status='A' AND serviceid=(SELECT serviceid FROM service WHERE servicename=?)";
    
    public static final String NO_CRITICAL_PATH = "Not Configured";

    /**
     * <p>
     * Retrieve all the critical paths
     * from the database
     */
    public static List<String[]> getAllCriticalPaths() throws SQLException {
        Connection conn = Vault.getDbConnection();
        List<String[]> paths = new ArrayList<String[]>();

        try {
            PreparedStatement stmt = conn.prepareStatement(GET_CRITICAL_PATHS);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String[] path = new String[2];
                path[0] = rs.getString(1);
                path[1] = rs.getString(2);
                paths.add(path);
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
        return paths;
    }
    
    /**
     * <p>
     * Retrieve critical path by nodeid
     * from the database
     * @param String nodeID
     */
    public static String getCriticalPath(int nodeID) throws SQLException {
        final DBUtils d = new DBUtils(PathOutageFactory.class);
        String result = NO_CRITICAL_PATH;

        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement(GET_CRITICAL_PATH_BY_NODEID);
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                result = (rs.getString(1) + " " + rs.getString(2));
            }
        } finally {
            d.cleanUp();
        }

        return result;
    }

    /**
     * <p>
     * Retrieve all the nodes in a critical path
     * from the database
     * 
     * @param criticalPathIp
     *            IP address of the critical path
     * @param criticalPathServiceName
     *            service name for the critical path
     */
    public static List<String> getNodesInPath(String criticalPathIp, String criticalPathServiceName) throws SQLException {
        Connection conn = Vault.getDbConnection();
        List<String> pathNodes = new ArrayList<String>();

        try {
            PreparedStatement stmt = conn.prepareStatement(GET_NODES_IN_PATH);
            stmt.setString(1, criticalPathIp);
            stmt.setString(2, criticalPathServiceName);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pathNodes.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
        return pathNodes;
    }

    /**
     * This method is responsible for determining the 
     * node label of a node, and the up/down status
     * and status color 
     * 
     * @param String nodeID
     *            the nodeID of the node being checked
     */
    public static String[] getLabelAndStatus(String nodeIDStr, Connection conn) throws SQLException {

        int countManagedSvcs = 0;
        int countOutages = 0;
        String result[] = new String[3];
        result[1] = "Cleared";
        result[2] = "Unmanaged";
        
        int nodeID = WebSecurityUtils.safeParseInt(nodeIDStr);

        PreparedStatement stmt = conn.prepareStatement(GET_NODELABEL_BY_NODEID);
        stmt.setInt(1, nodeID);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            result[0] = rs.getString(1);
        }
        rs.close();
        stmt.close();

        stmt = conn.prepareStatement(COUNT_MANAGED_SVCS);
        stmt.setInt(1, nodeID);
        rs = stmt.executeQuery();
        while (rs.next()) {
            countManagedSvcs = rs.getInt(1);
        }
        rs.close();
        stmt.close();

        if(countManagedSvcs > 0) {
            stmt = conn.prepareStatement(COUNT_OUTAGES);
            stmt.setInt(1, nodeID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                countOutages = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            if(countManagedSvcs == countOutages) {
                result[1] = "Critical";
                result[2] = "All Services Down";
            } else if(countOutages == 0) {
                result[1] = "Normal";
                result[2] = "All Services Up";
            } else {
                result[1] = "Minor";
                result[2] = "Some Services Down";
            }
        }
        return result;
    }

    /**
     * This method is responsible for determining the 
     * data related to the critical path:
     * node label, nodeId, the number of nodes
     * dependent on this path, and the managed state
     * of the path
     * 
     * @param String criticalPathIp
     *            the criticalPathIp of the node
     *            being checked
     * 
     * @param String criticalPathServiceName the
     *            criticalPathServiceName of the
     *            node being checked
     */
    public static String[] getCriticalPathData(String criticalPathIp, String criticalPathServiceName) throws SQLException {
        Connection conn = Vault.getDbConnection();
        String[] result = new String[4];
        int nodeCount=0;
        int count = 0;

        try {

            PreparedStatement stmt0 = conn.prepareStatement(GET_NODELABEL_BY_IP);
            stmt0.setString(1, criticalPathIp);

            ResultSet rs0 = stmt0.executeQuery();
            while (rs0.next()) {
                count++;
                result[0] = rs0.getString(1);
            }
            if (count > 1) {
                result[0] = "(" + count + " nodes have this IP)";
            }

            rs0.close();
            stmt0.close();

            count = 0;
            PreparedStatement stmt1 = conn.prepareStatement(GET_NODEID_BY_IP);
            stmt1.setString(1, criticalPathIp);

            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                result[1] = rs1.getString(1);
            }
            rs1.close();
            stmt1.close();

            PreparedStatement stmt2 = conn.prepareStatement(COUNT_NODES_IN_PATH);
            stmt2.setString(1, criticalPathIp);
            stmt2.setString(2, criticalPathServiceName);

            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                nodeCount = rs2.getInt(1);
            }
            result[2] = Integer.toString(nodeCount);
            rs2.close();
            stmt2.close();

            PreparedStatement stmt = conn.prepareStatement(IS_CRITICAL_PATH_MANAGED);
            stmt.setString(1, criticalPathIp);
            stmt.setString(2, criticalPathServiceName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            if(count > 0) {
                PreparedStatement stmt3 = conn.prepareStatement(GET_CRITICAL_PATH_STATUS);
                stmt3.setString(1, criticalPathIp);
                stmt3.setString(2, criticalPathServiceName);

                ResultSet rs3 = stmt3.executeQuery();
                while (rs3.next()) {
                    count = rs3.getInt(1);
                }
                if(count > 0) {
                    result[3] = "Critical";
                } else {
                    result[3] = "Normal";
                }
                while (rs3.next()) {
                    result[3] = rs3.getString(1);
                }
                rs3.close();
                stmt3.close();
            } else {
                result[3] = "Cleared";
            }
        } finally {
            Vault.releaseDbConnection(conn);
        }
        return result;
    }
}
