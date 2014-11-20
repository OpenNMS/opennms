/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;

/**
 * The source for all path outage business objects (nodes, critical path IPs,
 * critical path service names). Encapsulates all lookup functionality for
 * these business objects in one place.
 *
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class PathOutageManagerJdbcImpl implements PathOutageManager{

    private static final String GET_CRITICAL_PATHS = "SELECT DISTINCT node.nodelabel, pathoutage.criticalpathip, pathoutage.criticalpathservicename FROM pathoutage, ipinterface, node WHERE pathoutage.nodeid = node.nodeid ORDER BY node.nodelabel, pathoutage.criticalpathip, pathoutage.criticalpathservicename";

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

    private static final String GET_DEPENDENCY_NODES_BY_NODEID="select po.nodeid from pathoutage po left join ipinterface intf on po.criticalpathip=intf.ipaddr where intf.nodeid=?";

    private static final String GET_NODES_IN_PATHS = "SELECT DISTINCT pathoutage.nodeid FROM pathoutage, ipinterface WHERE pathoutage.criticalpathip=? AND pathoutage.nodeid=ipinterface.nodeid AND ipinterface.ismanaged!='D' ORDER BY nodeid";

    public static PathOutageManager getInstance() {
        return new PathOutageManagerJdbcImpl();
    }

    /**
     * <p>
     * Retrieve all the critical paths
     * from the database
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public List<String[]> getAllCriticalPaths() throws SQLException {
        final Connection conn = DataSourceFactory.getInstance().getConnection();
        final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class, conn);

        final List<String[]> paths = new ArrayList<String[]>();

        try {
            final PreparedStatement stmt = conn.prepareStatement(GET_CRITICAL_PATHS);
            d.watch(stmt);

            final ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                String[] path = new String[3];
                path[0] = rs.getString(1);
                path[1] = rs.getString(2);
                path[2] = rs.getString(3);
                paths.add(path);
            }
            return paths;
        } finally {
            d.cleanUp();
        }
    }

    /**
     * <p>
     * Retrieve critical path by nodeid
     * from the database
     *
     * @param nodeID a int.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public String getPrettyCriticalPath(int nodeID) throws SQLException {
        String[] path = queryForCriticalPath(nodeID);
        if (path[0] == null) {
            return NO_CRITICAL_PATH;
        } else {
            return path[0] + " " + path[1];
        }
    }

    private final String[] queryForCriticalPath(int nodeId) {
        final String[] cpath = new String[2];
        Querier querier = new Querier(DataSourceFactory.getInstance(), GET_CRITICAL_PATH_BY_NODEID) {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                cpath[0] = rs.getString(1);
                cpath[1] = rs.getString(2);
            }

        };
        querier.execute(Integer.valueOf(nodeId));
        return cpath;
    }

    @Override
    public String[] getCriticalPath(int nodeId) {
        final String[] cpath = queryForCriticalPath(nodeId);
        if (cpath[0] == null || "".equals(cpath[0].trim())) {
            // If no critical path was located in the table, then use the default critical path
            cpath[0] = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathIp();
            cpath[1] = "ICMP";
        } else if (cpath[1] == null || "".equals(cpath[1].trim())) {
            // If there was no service name in the table, then use the default of ICMP
            cpath[1] = "ICMP";
        }
        return cpath;
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
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public Set<Integer> getNodesInPath(String criticalPathIp, String criticalPathServiceName) throws SQLException {
        final Connection conn = DataSourceFactory.getInstance().getConnection();
        final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class, conn);

        final Set<Integer> pathNodes = new TreeSet<Integer>();

        try {
            final PreparedStatement stmt = conn.prepareStatement(GET_NODES_IN_PATH);
            d.watch(stmt);
            stmt.setString(1, criticalPathIp);
            stmt.setString(2, criticalPathServiceName);

            final ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                pathNodes.add(rs.getInt(1));
            }
        } finally {
            d.cleanUp();
        }
        return pathNodes;
    }

    /**
     * This method is responsible for determining the
     * node label of a node, and the up/down status
     * and status color
     *
     * @param nodeIDStr a {@link java.lang.String} object.
     * @param conn a {@link java.sql.Connection} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public String[] getLabelAndStatus(String nodeIDStr, Connection conn) throws SQLException {
        final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class);

        try {
            int countManagedSvcs = 0;
            int countOutages = 0;
            String[] result = new String[3];
            result[1] = "Cleared";
            result[2] = "Unmanaged";

            int nodeID = WebSecurityUtils.safeParseInt(nodeIDStr);

            PreparedStatement stmt = conn.prepareStatement(GET_NODELABEL_BY_NODEID);
            d.watch(stmt);
            stmt.setInt(1, nodeID);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                result[0] = rs.getString(1);
            }

            rs.close();
            stmt.close();

            stmt = conn.prepareStatement(COUNT_MANAGED_SVCS);
            d.watch(stmt);
            stmt.setInt(1, nodeID);

            rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                countManagedSvcs = rs.getInt(1);
            }

            rs.close();
            stmt.close();

            if(countManagedSvcs > 0) {
                stmt = conn.prepareStatement(COUNT_OUTAGES);
                d.watch(stmt);
                stmt.setInt(1, nodeID);

                rs = stmt.executeQuery();
                d.watch(rs);

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
        } finally {
            d.cleanUp();
        }
    }

    /**
     * This method is responsible for determining the
     * data related to the critical path:
     * node label, nodeId, the number of nodes
     * dependent on this path, and the managed state
     * of the path
     *
     * @param criticalPathIp a {@link java.lang.String} object.
     * @param criticalPathServiceName a {@link java.lang.String} object.
     * @return an array of {@link java.lang.String} objects.
     * @throws java.sql.SQLException if any.
     */
    @Override
    public String[] getCriticalPathData(String criticalPathIp, String criticalPathServiceName) throws SQLException {
        final Connection conn = DataSourceFactory.getInstance().getConnection();
        final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class, conn);

        String[] result = new String[4];
        int nodeCount=0;
        int count = 0;

        try {
            final PreparedStatement stmt0 = conn.prepareStatement(GET_NODELABEL_BY_IP);
            d.watch(stmt0);
            stmt0.setString(1, criticalPathIp);

            final ResultSet rs0 = stmt0.executeQuery();
            d.watch(rs0);

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
            final PreparedStatement stmt1 = conn.prepareStatement(GET_NODEID_BY_IP);
            d.watch(stmt1);
            stmt1.setString(1, criticalPathIp);

            final ResultSet rs1 = stmt1.executeQuery();
            d.watch(rs1);

            while (rs1.next()) {
                result[1] = rs1.getString(1);
            }

            rs1.close();
            stmt1.close();

            final PreparedStatement stmt2 = conn.prepareStatement(COUNT_NODES_IN_PATH);
            d.watch(stmt2);
            stmt2.setString(1, criticalPathIp);
            stmt2.setString(2, criticalPathServiceName);

            final ResultSet rs2 = stmt2.executeQuery();
            d.watch(rs2);

            while (rs2.next()) {
                nodeCount = rs2.getInt(1);
            }
            result[2] = Integer.toString(nodeCount);

            rs2.close();
            stmt2.close();

            final PreparedStatement stmt = conn.prepareStatement(IS_CRITICAL_PATH_MANAGED);
            d.watch(stmt);
            stmt.setString(1, criticalPathIp);
            stmt.setString(2, criticalPathServiceName);

            final ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                count = rs.getInt(1);
            }

            rs.close();
            stmt.close();

            if(count > 0) {
                final PreparedStatement stmt3 = conn.prepareStatement(GET_CRITICAL_PATH_STATUS);
                d.watch(stmt3);
                stmt3.setString(1, criticalPathIp);
                stmt3.setString(2, criticalPathServiceName);

                final ResultSet rs3 = stmt3.executeQuery();
                d.watch(rs3);

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
            d.cleanUp();
        }
        return result;
    }
  
    @Override
    public Set<Integer> getAllNodesDependentOnAnyServiceOnInterface(String criticalpathip) throws SQLException {
	    final Connection conn = DataSourceFactory.getInstance().getConnection();
	    final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class, conn);
	    Set<Integer> pathNodes = new TreeSet<Integer>();
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_NODES_IN_PATHS);
            d.watch(stmt);
            stmt.setString(1, criticalpathip);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                pathNodes.add(rs.getInt(1));
            }
        } finally {
            d.cleanUp();
        }
	    return pathNodes;
        
    }
    
    @Override
	public Set<Integer> getAllNodesDependentOnAnyServiceOnNode(int nodeid) throws SQLException {
	    final Connection conn = DataSourceFactory.getInstance().getConnection();
	    final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class, conn);

	    Set<Integer> pathNodes = new TreeSet<Integer>();
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_DEPENDENCY_NODES_BY_NODEID);
            d.watch(stmt);
            stmt.setInt(1, nodeid);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                pathNodes.add(rs.getInt(1));
            }
        } finally {
            d.cleanUp();
        }
	    
	    return pathNodes;
	}
}
