//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Aug 28: Restore search and display capabilities for non-ip interfaces
// 2007 May 29: Add "id" for Interface model object. - dj@opennms.org
// 2003 Feb 05: Added ORDER BY to SQL statement.
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

package org.opennms.web.element;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.Util;
import org.opennms.web.svclayer.AggregateStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The source for all network element business objects (nodes, interfaces,
 * services). Encapsulates all lookup functionality for the network element
 * business objects in one place.
 *
 * To use this factory to lookup network elements, you must first initialize the
 * Vault with the database connection manager * and JDBC URL it will use. Call
 * the init method to initialize the factory. After that, you can call any
 * lookup methods.
 *
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="larry@opennms.org">Larry Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class NetworkElementFactory {

    /**
     * A mapping of service names (strings) to service identifiers (integers).
     */
    protected static Map<String, Integer> serviceName2IdMap;

    /**
     * A mapping of service identifiers (integers) to service names (strings).
     */
    protected static Map<Integer, String> serviceId2NameMap;

    /**
     * Private, empty constructor so that this class cannot be instantiated. All
     * of its methods should static and accessed through the class name.
     */
    private NetworkElementFactory() {
    }

    private static final Comparator<Interface> INTERFACE_COMPARATOR = new InterfaceComparator();

    /**
     * Translate a node id into a human-readable node label. Note these values
     * are not cached.
     *
     * @return A human-readable node name or null if the node id given does not
     *         specify a real node.
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     */
    public static String getNodeLabel(int nodeId) throws SQLException {
        String label = null;

        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT NODELABEL FROM NODE WHERE NODEID = ?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            if (rs.next()) {
                label = rs.getString("NODELABEL");
            }
        } finally {
            d.cleanUp();
        }

        return (label);
    }

    /**
     * Translate a node id into a human-readable ipaddress. Note these values
     * are not cached.
     *
     * @return A human-readable node name or null if the node id given does not
     *         specify a real node.
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     */
    public static String getIpPrimaryAddress(int nodeId) throws SQLException {
        String label = null;

        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT IPADDR FROM ipinterface WHERE NODEID = ? and isSnmpPrimary='P'");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            if (rs.next()) {
                label = rs.getString("IPADDR");
            }
        } finally {
            d.cleanUp();
        }

        return (label);
    }

    /**
     * <p>getNode</p>
     *
     * @param nodeId a int.
     * @return a {@link org.opennms.web.element.Node} object.
     * @throws java.sql.SQLException if any.
     */
    public static Node getNode(int nodeId) throws SQLException {
        Node node = null;

        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID = ?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            Node[] nodes = rs2Nodes(rs);

            // what do I do if this actually returns more than one node?
            if (nodes.length > 0) {
                node = nodes[0];
            }
        } finally {
            d.cleanUp();
        }

        return node;
    }

    /**
     * Returns all non-deleted nodes.
     *
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getAllNodes() throws SQLException {
        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM NODE WHERE NODETYPE != 'D' ORDER BY NODELABEL");
            d.watch(rs);

            nodes = rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes that have the given nodeLabel substring
     * somewhere in their nodeLabel.
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesLike(String nodeLabel) throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE LOWER(NODELABEL) LIKE ? AND NODETYPE != 'D' ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setString(1, buffer.toString());
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes with an IP address like the rule given.
     *
     * @param iplike a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithIpLike(String iplike) throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT node.* FROM NODE, IPINTERFACE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND IPINTERFACE.ISMANAGED != 'D' AND node.NODETYPE != 'D' ORDER BY node.NODELABEL");
            d.watch(stmt);
            stmt.setString(1, iplike);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes that have the given service.
     *
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithService(int serviceId) throws SQLException {
        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT NODEID FROM IFSERVICES WHERE SERVICEID=?) AND NODETYPE != 'D' ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setInt(1, serviceId);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes that have the given mac.
     *
     * @param macAddr a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithPhysAddr(String macAddr) throws SQLException {
        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(macAddr);
            buffer.append("%");

        	PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT * FROM node WHERE " +
            		"nodetype != 'D' AND " +
            		"(nodeid IN (SELECT nodeid FROM snmpinterface WHERE snmpphysaddr LIKE ? ) OR " +
					" nodeid IN (SELECT nodeid FROM atinterface WHERE atphysaddr LIKE ? )) " +
            		"ORDER BY nodelabel");
        	d.watch(stmt);
            stmt.setString(1, buffer.toString());
            stmt.setString(2, buffer.toString());

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }


    /**
     * Returns all non-deleted nodes with a MAC address like the rule given from AtInterface.
     *
     * @param macAddr a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithPhysAddrAtInterface(String macAddr)
			throws SQLException {
		if (macAddr == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
		try {
	        Connection conn = Vault.getDbConnection();
	        d.watch(conn);
	        
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(macAddr);
            buffer.append("%");

            PreparedStatement stmt = conn
					.prepareStatement("SELECT DISTINCT * FROM node WHERE nodetype != 'D' " +
							"AND nodeid IN (SELECT nodeid FROM atinterface WHERE atphysaddr LIKE '% ? %') ORDER BY nodelabel");
            d.watch(stmt);
            
			stmt.setString(1, buffer.toString());
			ResultSet rs = stmt.executeQuery();
			d.watch(rs);
			
			nodes = rs2Nodes(rs);
		} finally {
		    d.cleanUp();
		}

		return nodes;
	}

    /**
     * Returns all non-deleted nodes with a MAC address like the rule given from SnmpInterface.
     *
     * @param macAddr a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithPhysAddrFromSnmpInterface(String macAddr)
			throws SQLException {
		if (macAddr == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
		try {
	        Connection conn = Vault.getDbConnection();
	        d.watch(conn);
	        
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(macAddr);
            buffer.append("%");

			PreparedStatement stmt = conn
					.prepareStatement("SELECT DISTINCT * FROM node WHERE nodetype != 'D' AND " +
							"nodeid IN (SELECT nodeid FROM snmpinterface WHERE snmpphysaddr LIKE '% ? %') ORDER BY nodelabel");
			d.watch(stmt);
			stmt.setString(1, buffer.toString());

			ResultSet rs = stmt.executeQuery();
			d.watch(rs);
			
			nodes = rs2Nodes(rs);
		} finally {
		    d.cleanUp();
		}

		return nodes;
	}

    /**
     * Returns all non-deleted nodes that contain the given string in an ifAlias
     *
     * @Param ifAlias
     *               the ifAlias string we are looking for
     * @return nodes
     *               the nodes with a matching ifAlias on one or more interfaces
     * @param ifAlias a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithIfAlias(String ifAlias) throws SQLException {
        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(ifAlias);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT SNMPINTERFACE.NODEID FROM SNMPINTERFACE,IPINTERFACE WHERE SNMPINTERFACE.SNMPIFALIAS ILIKE ? AND SNMPINTERFACE.SNMPIFINDEX=IPINTERFACE.IFINDEX AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID AND IPINTERFACE.ISMANAGED != 'D') AND NODETYPE != 'D' ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setString(1, buffer.toString());

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * Resolve an IP address to a DNS hostname via the database. If no hostname
     * can be found, the given IP address is returned.
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static String getHostname(String ipAddress) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String hostname = ipAddress;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT IPADDR, IPHOSTNAME FROM IPINTERFACE WHERE IPADDR=? AND IPHOSTNAME IS NOT NULL");
            d.watch(stmt);
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            if (rs.next()) {
                hostname = rs.getString("IPHOSTNAME");
            }
        } finally {
            d.cleanUp();
        }

        return hostname;
    }

    /**
     * <p>getInterface</p>
     *
     * @param ipInterfaceId a int.
     * @return a {@link org.opennms.web.element.Interface} object.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getInterface(int ipInterfaceId) throws SQLException {
        Interface intf = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE ID = ?");
            d.watch(stmt);
            stmt.setInt(1, ipInterfaceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            Interface[] intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);

            // what do I do if this actually returns more than one node?
            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            d.cleanUp();
        }

        return intf;
    }

    /**
     * <p>getInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.element.Interface} object.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getInterface(int nodeId, String ipAddress) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Interface intf = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IPADDR=?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            Interface[] intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);

            // what do I do if this actually returns more than one node?
            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            d.cleanUp();
        }

        return intf;
    }

    /**
     * <p>getInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param ifindex a int.
     * @return a {@link org.opennms.web.element.Interface} object.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getInterface(int nodeId, String ipAddress, int ifindex) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Interface intf = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IPADDR=? AND IFINDEX=?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            stmt.setInt(3, ifindex);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            Interface[] intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);

            // what do I do if this actually returns more than one ?
            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            d.cleanUp();
        }

        return intf;
    }

    /**
     * Get interface from snmpinterface table. Intended for use with non-ip interfaces.
     *
     * @return Interface
     * @throws java.sql.SQLException if any.
     * @param nodeId a int.
     * @param ifIndex a int.
     */
    public static Interface getSnmpInterface(int nodeId, int ifIndex) throws SQLException {

        Interface intf = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM SNMPINTERFACE WHERE NODEID = ? AND SNMPIFINDEX=?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.setInt(2, ifIndex);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            Interface[] intfs = rs2SnmpInterfaces(rs);

            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            d.cleanUp();
        }

        return intf;
    }
    
    /**
     * <p>getInterfacesWithIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getInterfacesWithIpAddress(String ipAddress) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE IPADDR=?");
            d.watch(stmt);
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);

        } finally {
            d.cleanUp();
        }

        return intfs;
    }

    /**
     * Returns all non-deleted Interfaces on the specified node that
     * contain the given string in an ifAlias
     *
     * @Param nodeId
     *               The nodeId of the node we are looking at
     * @Param ifAlias
     *               the ifAlias string we are looking for
     * @return intfs
     *               the Interfaces with a matching ifAlias
     * @param nodeId a int.
     * @param ifAlias a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getInterfacesWithIfAlias(int nodeId, String ifAlias) throws SQLException {
        if (ifAlias == null) {
            throw new IllegalArgumentException("Cannot take null parameter ifAlias");
        }

        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(ifAlias);
            buffer.append("%");

            PreparedStatement stmt = null;
            if(nodeId > 0) {
                stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX IN (SELECT SNMPIFINDEX FROM SNMPINTERFACE WHERE SNMPIFALIAS ILIKE ? AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID) AND ISMANAGED != 'D'");
                d.watch(stmt);
                stmt.setInt(1, nodeId);
                stmt.setString(2, buffer.toString());
            } else {
                stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE IPINTERFACE.IFINDEX IN (SELECT SNMPIFINDEX FROM SNMPINTERFACE WHERE SNMPIFALIAS ILIKE ? AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID) AND IPINTERFACE.ISMANAGED != 'D' ORDER BY IPINTERFACE.NODEID");
                d.watch(stmt);
                stmt.setString(1, buffer.toString());
            }
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);

        } finally {
            d.cleanUp();
        }

        return intfs;
    }

    /**
     * Returns true if node has any snmpIfAliases
     *
     * @Param nodeId
     *               The nodeId of the node we are looking at
     *               the ifAlias string we are looking for
     * @return boolean
     *               true if node has any snmpIfAliases
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     */
    public static boolean nodeHasIfAliases(int nodeId) throws SQLException {

        boolean hasAliases = false;

        if (nodeId > 0) {
            final DBUtils d = new DBUtils(NetworkElementFactory.class);
            try {
                Connection conn = Vault.getDbConnection();
                d.watch(conn);
                PreparedStatement stmt = conn.prepareStatement("SELECT ID FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX IN (SELECT SNMPIFINDEX FROM SNMPINTERFACE WHERE SNMPIFALIAS ILIKE '%_%' AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID) AND ISMANAGED != 'D'");
                d.watch(stmt);
                stmt.setInt(1, nodeId);
                ResultSet rs = stmt.executeQuery();
                d.watch(rs);
                
                if (rs.next()) {
                    hasAliases = true;
                }
            } finally {
                d.cleanUp();
            }
        }

        return hasAliases;
    }

    /**
     * <p>getAllInterfacesOnNode</p>
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getAllInterfacesOnNode(int nodeId) throws SQLException {
        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);
        } finally {
            d.cleanUp();
        }

        return intfs;
    }

    /**
     * Returns all snmp interfaces on a node
     *
     * @Param int nodeId
     *               The nodeId of the node we are looking at
     * @return Interface[]
     * @param nodeId a int.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getAllSnmpInterfacesOnNode(int nodeId) throws SQLException {
        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM SNMPINTERFACE WHERE NODEID = ? ORDER BY SNMPIFINDEX");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            intfs = rs2SnmpInterfaces(rs);

        } finally {
            d.cleanUp();
        }

        return intfs;
    }
    
    /**
     * <p>getActiveInterfacesOnNode</p>
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getActiveInterfacesOnNode(int nodeId) throws SQLException {
        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND ISMANAGED != 'D' ORDER BY IFINDEX");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            intfs = rs2Interfaces(rs);

            augmentInterfacesWithSnmpData(intfs, conn);
        } finally {
            d.cleanUp();
        }

        return intfs;
    }

    /*
     * Returns all interfaces, including their SNMP information
     */
    /**
     * <p>getAllInterfaces</p>
     *
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getAllInterfaces() throws SQLException {
        return getAllInterfaces(true);
    }

    /*
     * Returns all interfaces, but only includes snmp data if includeSNMP is true
     * This may be useful for pages that don't need snmp data and don't want to execute
     * a sub-query per interface!
     *
     * @param includeSNMP a boolean.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getAllInterfaces(boolean includeSNMP) throws SQLException {
        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery("SELECT * FROM IPINTERFACE ORDER BY IPHOSTNAME, NODEID, IPADDR");
            d.watch(rs);
            
            intfs = rs2Interfaces(rs);

            if(includeSNMP) {
                augmentInterfacesWithSnmpData(intfs, conn);
            }            
        } finally {
            d.cleanUp();
        }

        return intfs;
    }

    /**
     * <p>getAllManagedIpInterfaces</p>
     *
     * @param includeSNMP a boolean.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Interface[] getAllManagedIpInterfaces(boolean includeSNMP) throws SQLException {
        Interface[] intfs = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery("SELECT * FROM IPINTERFACE WHERE IPINTERFACE.ISMANAGED != 'D' AND IPINTERFACE.IPADDR != '0.0.0.0' AND IPINTERFACE.IPADDR IS NOT NULL ORDER BY IPHOSTNAME, NODEID, IPADDR");
            d.watch(rs);
            
            intfs = rs2Interfaces(rs);

            if(includeSNMP) {
                augmentInterfacesWithSnmpData(intfs, conn);
            }            
        } finally {
            d.cleanUp();
        }

        return intfs;
    }

    /**
     * Return the service specified by the node identifier, IP address, and
     * service identifier.
     *
     * <p>
     * Note that if there are both an active service and historically deleted
     * services with this (nodeid, ipAddress, serviceId) key, then the active
     * service will be returned. If there are only deleted services, then the
     * first deleted service will be returned.
     * </p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return a {@link org.opennms.web.element.Service} object.
     * @throws java.sql.SQLException if any.
     */
    public static Service getService(int nodeId, String ipAddress, int serviceId) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Service service = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            // big hack here, I'm relying on the fact that the ifservices.status
            // field uses 'A' as active, and thus should always turn up before
            // any
            // historically deleted services
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.IPADDR=? AND IFSERVICES.SERVICEID=? ORDER BY IFSERVICES.STATUS");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            stmt.setInt(3, serviceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            Service[] services = rs2Services(rs);

            // only take the first service, which should be the active service,
            // cause we're sorting by status in the SQL statement above; if
            // there
            // are no active services, then the first deleted service will be
            // returned,
            // which is what we want
            if (services.length > 0) {
                service = services[0];
            }
        } finally {
            d.cleanUp();
        }

        return service;
    }
    /**
     * Return the service specified by the node identifier, IP address, and
     * service identifier.
     *
     * <p>
     * Note that if there are both an active service and historically deleted
     * services with this (nodeid, ipAddress, serviceId) key, then the active
     * service will be returned. If there are only deleted services, then the
     * first deleted service will be returned.
     * </p>
     *
     * @param ifServiceId a int.
     * @return a {@link org.opennms.web.element.Service} object.
     * @throws java.sql.SQLException if any.
     */
    public static Service getService(int ifServiceId) throws SQLException {
        Service service = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            // big hack here, I'm relying on the fact that the ifservices.status
            // field uses 'A' as active, and thus should always turn up before
            // any
            // historically deleted services
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.ID=? ORDER BY IFSERVICES.STATUS");
            d.watch(stmt);
            stmt.setInt(1, ifServiceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            Service[] services = rs2Services(rs);

            // only take the first service, which should be the active service,
            // cause we're sorting by status in the SQL statement above; if
            // there
            // are no active services, then the first deleted service will be
            // returned,
            // which is what we want
            if (services.length > 0) {
                service = services[0];
            }
        } finally {
            d.cleanUp();
        }

        return service;
    }

    /**
     * <p>getAllServices</p>
     *
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Service[] getAllServices() throws SQLException {
        Service[] services = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID = SERVICE.SERVICEID ORDER BY SERVICE.SERVICEID, inet(IFSERVICES.IPADDR)");
            d.watch(rs);
            
            services = rs2Services(rs);
        } finally {
            d.cleanUp();
        }

        return services;
    }

    /**
     * <p>getServicesOnInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Service[] getServicesOnInterface(int nodeId, String ipAddress) throws SQLException {
        return getServicesOnInterface(nodeId, ipAddress, false);
    }

    /**
     * <p>getServicesOnInterface</p>
     *
     * @param nodeId a int.
     * @param ipAddress a {@link java.lang.String} object.
     * @param includeDeletions a boolean.
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Service[] getServicesOnInterface(int nodeId, String ipAddress, boolean includeDeletions) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Service[] services = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            StringBuffer buffer = new StringBuffer("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.IPADDR=?");

            if (!includeDeletions) {
                buffer.append(" AND IFSERVICES.STATUS <> 'D'");
            }

            PreparedStatement stmt = conn.prepareStatement(buffer.toString());
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            services = rs2Services(rs);
        } finally {
            d.cleanUp();
        }

        return services;
    }

    /**
     * Get the list of all services on a given node.
     *
     * @param nodeId a int.
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Service[] getServicesOnNode(int nodeId) throws SQLException {
        Service[] services = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            services = rs2Services(rs);
        } finally {
            d.cleanUp();
        }

        return services;
    }

    /**
     * Get the list of all instances of a specific service on a given node.
     *
     * @param nodeId a int.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Service[] getServicesOnNode(int nodeId, int serviceId) throws SQLException {
        Service[] services = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.SERVICEID=?");
            d.watch(stmt);
            stmt.setInt(1, nodeId);
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            services = rs2Services(rs);
        } finally {
            d.cleanUp();
        }

        return services;
    }

    /**
     * This method returns the data from the result set as an array of Node
     * objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Node[] rs2Nodes(ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("rs parameter cannot be null");
        }

        List<Node> nodes = new LinkedList<Node>();

        while (rs.next()) {
            Node node = new Node();

            node.m_nodeId = rs.getInt("nodeId");
            node.m_dpname = rs.getString("dpName");

            Timestamp timestamp = rs.getTimestamp("nodeCreateTime");
            if (timestamp != null) {
                node.m_nodeCreateTime = Util.formatDateToUIString(new Date((timestamp).getTime()));
            }
            
            Integer nodeParentID = rs.getInt("nodeParentID");
            if (nodeParentID != null) {
                node.m_nodeParent = nodeParentID.intValue();
            }

            String nodeType = rs.getString("nodeType");
            if (nodeType != null) {
                node.m_nodeType = nodeType.charAt(0);
            }

            node.m_nodeSysId = rs.getString("nodeSysOID");
            node.m_nodeSysName = rs.getString("nodeSysName");
            node.m_nodeSysDescr = rs.getString("nodeSysDescription");
            node.m_nodeSysLocn = rs.getString("nodeSysLocation");
            node.m_nodeSysContact = rs.getString("nodeSysContact");
            node.m_label = rs.getString("nodelabel");
            node.m_operatingSystem = rs.getString("operatingsystem");
            node.m_foreignSource = rs.getString("foreignSource");
            node.m_foreignId = rs.getString("foreignId");

            nodes.add(node);
        }
        
        return nodes.toArray(new Node[nodes.size()]);
    }

    /**
     * This method returns the data from the result set as an vector of
     * ipinterface objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.Interface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Interface[] rs2Interfaces(ResultSet rs) throws SQLException {
        List<Interface> intfs = new ArrayList<Interface>();

        while (rs.next()) {
            
            Object element = null;
            Interface intf = new Interface();

            intf.m_id = rs.getInt("id");
            intf.m_nodeId = rs.getInt("nodeid");
            intf.m_ifIndex = rs.getInt("ifIndex");
            intf.m_ipStatus = rs.getInt("ipStatus");
            intf.m_ipHostName = rs.getString("ipHostname");
            intf.m_ipAddr = rs.getString("ipAddr");
            
            element = rs.getString("isManaged");
            if (element != null) {
                intf.m_isManaged = ((String) element).charAt(0);
            }

            element = rs.getTimestamp("ipLastCapsdPoll");
            if (element != null) {
                intf.m_ipLastCapsdPoll = Util.formatDateToUIString(new Date(((Timestamp) element).getTime()));
            }

            intfs.add(intf);
        }

        Collections.sort(intfs, INTERFACE_COMPARATOR);
        return intfs.toArray(new Interface[intfs.size()]);

    }

    /**
     * This method returns the data from the result set as an vector of
     * interface objects for non-ip interfaces.
     *
     * @return Interface[]
     * @throws java.sql.SQLException if any.
     * @param rs a {@link java.sql.ResultSet} object.
     */
    protected static Interface[] rs2SnmpInterfaces(ResultSet rs) throws SQLException {
        List<Interface> intfs = new ArrayList<Interface>();

        while (rs.next()) {
            
            Interface intf = new Interface();

            intf.m_nodeId = rs.getInt("nodeid");
            intf.m_ipAddr = rs.getString("ipaddr");
            intf.m_snmpIfIndex = rs.getInt("snmpifindex");
            intf.m_snmpIpAdEntNetMask = rs.getString("snmpIpAdEntNetMask");
            intf.m_snmpPhysAddr = rs.getString("snmpPhysAddr");
            intf.m_snmpIfDescr = rs.getString("snmpIfDescr");
            intf.m_snmpIfName = rs.getString("snmpIfName");
            intf.m_snmpIfType = rs.getInt("snmpIfType");
            intf.m_snmpIfOperStatus = rs.getInt("snmpIfOperStatus");
            intf.m_snmpIfSpeed = rs.getLong("snmpIfSpeed");
            intf.m_snmpIfAdminStatus = rs.getInt("snmpIfAdminStatus");
            intf.m_snmpIfAlias = rs.getString("snmpIfAlias");
            
            Object element = rs.getString("snmpPoll");
            if (element != null) {
                intf.m_isSnmpPoll = ((String) element).charAt(0);
            }

            element = rs.getTimestamp("snmpLastCapsdPoll");
            if (element != null) {
                intf.m_snmpLastCapsdPoll = Util.formatDateToUIString(new Date(((Timestamp) element).getTime()));
            }

            element = rs.getTimestamp("snmpLastSnmpPoll");
            if (element != null) {
                intf.m_snmpLastSnmpPoll = Util.formatDateToUIString(new Date(((Timestamp) element).getTime()));
            }

            intfs.add(intf);
        }

        Collections.sort(intfs, INTERFACE_COMPARATOR);
        return intfs.toArray(new Interface[intfs.size()]);

    }

    
    /**
     * <p>augmentInterfacesWithSnmpData</p>
     *
     * @param intfs an array of {@link org.opennms.web.element.Interface} objects.
     * @param conn a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    protected static void augmentInterfacesWithSnmpData(Interface[] intfs, Connection conn) throws SQLException {
        if (intfs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        for (int i = 0; i < intfs.length; i++) {
            if (intfs[i].getIfIndex() != 0) {
                PreparedStatement pstmt;
                ResultSet rs;
                try {
                    pstmt = conn.prepareStatement("SELECT * FROM SNMPINTERFACE WHERE NODEID=? AND SNMPIFINDEX=?");
                    d.watch(pstmt);
                    pstmt.setInt(1, intfs[i].getNodeId());
                    pstmt.setInt(2, intfs[i].getIfIndex());

                    rs = pstmt.executeQuery();
                    d.watch(rs);
                    
                    if (rs.next()) {
                        intfs[i].m_snmpIfIndex = rs.getInt("snmpifindex");
                        intfs[i].m_snmpIpAdEntNetMask = rs.getString("snmpIpAdEntNetMask");
                        intfs[i].m_snmpPhysAddr = rs.getString("snmpPhysAddr");
                        intfs[i].m_snmpIfDescr = rs.getString("snmpIfDescr");
                        intfs[i].m_snmpIfName = rs.getString("snmpIfName");
                        intfs[i].m_snmpIfType = rs.getInt("snmpIfType");
                        intfs[i].m_snmpIfOperStatus = rs.getInt("snmpIfOperStatus");
                        intfs[i].m_snmpIfSpeed = rs.getLong("snmpIfSpeed");
                        intfs[i].m_snmpIfAdminStatus = rs.getInt("snmpIfAdminStatus");
                        intfs[i].m_snmpIfAlias = rs.getString("snmpIfAlias");
                        
                        Object element = rs.getString("snmpPoll");
                        if (element != null) {
                            intfs[i].m_isSnmpPoll = ((String) element).charAt(0);
                        }

                        element = rs.getTimestamp("snmpLastCapsdPoll");
                        if (element != null) {
                            intfs[i].m_snmpLastCapsdPoll = Util.formatDateToUIString(new Date(((Timestamp) element).getTime()));
                        }

                        element = rs.getTimestamp("snmpLastSnmpPoll");
                        if (element != null) {
                            intfs[i].m_snmpLastSnmpPoll = Util.formatDateToUIString(new Date(((Timestamp) element).getTime()));
                        }

                    }

                    pstmt = conn.prepareStatement("SELECT issnmpprimary FROM ipinterface WHERE nodeid=? AND ifindex=? AND ipaddr=?");
                    d.watch(pstmt);
                    pstmt.setInt(1, intfs[i].getNodeId());
                    pstmt.setInt(2, intfs[i].getIfIndex());
                    pstmt.setString(3, intfs[i].getIpAddress());

                    rs = pstmt.executeQuery();
                    d.watch(rs);

                    if (rs.next()) {
                        intfs[i].m_isSnmpPrimary = rs.getString("issnmpprimary");
                    }
                } finally {
                    d.cleanUp();
                }
            }
        }
    }

    /**
     * <p>rs2Services</p>
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Service[] rs2Services(ResultSet rs) throws SQLException {
        List<Service> services = new ArrayList<Service>();

        while (rs.next()) {
            Service service = new Service();

            Object element = null;
            
            service.setId(rs.getInt("id"));
            service.setNodeId(rs.getInt("nodeid"));
            service.setIfIndex(rs.getInt("ifindex"));
            service.setIpAddress(rs.getString("ipaddr"));

            element = rs.getTimestamp("lastgood");
            if (element != null) {
                service.setLastGood(Util.formatDateToUIString(new Date(((Timestamp) element).getTime())));
            }

            service.setServiceId(rs.getInt("serviceid"));
            service.setServiceName(rs.getString("servicename"));

            element = rs.getTimestamp("lastfail");
            if (element != null) {
                service.setLastFail(Util.formatDateToUIString(new Date(((Timestamp) element).getTime())));
            }

            service.setNotify(rs.getString("notify"));

            element = rs.getString("status");
            if (element != null) {
                service.setStatus(((String) element).charAt(0));
            }

            services.add(service);
        }

        return services.toArray(new Service[services.size()]);
    }

    /**
     * <p>getServiceNameFromId</p>
     *
     * @param serviceId a int.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static String getServiceNameFromId(int serviceId) throws SQLException {
        if (serviceId2NameMap == null) {
            createServiceIdNameMaps();
        }

        String serviceName = serviceId2NameMap.get(new Integer(serviceId));

        return (serviceName);
    }

    /**
     * <p>getServiceIdFromName</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getServiceIdFromName(String serviceName) throws SQLException {
        if (serviceName == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        int serviceId = -1;

        if (serviceName2IdMap == null) {
            createServiceIdNameMaps();
        }

        Integer value = serviceName2IdMap.get(serviceName);

        if (value != null) {
            serviceId = value.intValue();
        }

        return (serviceId);
    }

    /**
     * <p>getServiceIdToNameMap</p>
     *
     * @return a java$util$Map object.
     * @throws java.sql.SQLException if any.
     */
    public static Map<Integer, String> getServiceIdToNameMap() throws SQLException {
        if (serviceId2NameMap == null) {
            createServiceIdNameMaps();
        }

        return (new HashMap<Integer, String>(serviceId2NameMap));
    }

    /**
     * <p>getServiceNameToIdMap</p>
     *
     * @return a java$util$Map object.
     * @throws java.sql.SQLException if any.
     */
    public static Map<String, Integer> getServiceNameToIdMap() throws SQLException {
        if (serviceName2IdMap == null) {
            createServiceIdNameMaps();
        }

        return (new HashMap<String, Integer>(serviceName2IdMap));
    }

    /**
     * <p>createServiceIdNameMaps</p>
     *
     * @throws java.sql.SQLException if any.
     */
    protected static void createServiceIdNameMaps() throws SQLException {
        HashMap<Integer, String> idMap = new HashMap<Integer, String>();
        HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery("SELECT SERVICEID, SERVICENAME FROM SERVICE");
            d.watch(rs);
            
            while (rs.next()) {
                int id = rs.getInt("SERVICEID");
                String name = rs.getString("SERVICENAME");

                idMap.put(new Integer(id), name);
                nameMap.put(name, new Integer(id));
            }
        } finally {
            d.cleanUp();
        }

        serviceId2NameMap = idMap;
        serviceName2IdMap = nameMap;
    }

    // OpenNMS IA Stuff
    
    /**
     * <p>getNodesLikeAndIpLike</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param iplike a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesLikeAndIpLike(String nodeLabel, String iplike,
            int serviceId) throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) AND NODETYPE != 'D' AND LOWER(NODELABEL) LIKE ? AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODE.NODEID=IPINTERFACE.NODEID ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setInt(1, serviceId);
            stmt.setString(2, buffer.toString());
            stmt.setString(3, iplike);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = NetworkElementFactory.rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getNodesLike</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesLike(String nodeLabel, int serviceId)
            throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE LOWER(NODELABEL) LIKE ? AND NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setString(1, buffer.toString());
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = NetworkElementFactory.rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getNodesWithIpLike</p>
     *
     * @param iplike a {@link java.lang.String} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesWithIpLike(String iplike, int serviceId)
            throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT * FROM NODE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setString(1, iplike);
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = NetworkElementFactory.rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getAllNodes</p>
     *
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getAllNodes(int serviceId) throws SQLException {
        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            d.watch(stmt);
            stmt.setInt(1, serviceId);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = NetworkElementFactory.rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getAtInterfacesFromPhysaddr</p>
     *
     * @param AtPhysAddr a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.AtInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static AtInterface[] getAtInterfacesFromPhysaddr(String AtPhysAddr)
            throws SQLException {

        if (AtPhysAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ATINTERFACE WHERE ATPHYSADDR LIKE '%"
                            + AtPhysAddr + "%' AND STATUS != 'D'");
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2AtInterface(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getNodesFromPhysaddr</p>
     *
     * @param AtPhysAddr a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Node[] getNodesFromPhysaddr(String AtPhysAddr)
            throws SQLException {

        if (AtPhysAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(*) FROM IPINTERFACE WHERE NODEID IN "
                            + "(SELECT NODEID FROM ATINTERFACE WHERE ATPHYSADDR LIKE '%"
                            + AtPhysAddr + "%' AND STATUS != 'D'");
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = NetworkElementFactory.rs2Nodes(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getAtInterface</p>
     *
     * @param nodeID a int.
     * @param ipaddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.element.AtInterface} object.
     * @throws java.sql.SQLException if any.
     */
    public static AtInterface getAtInterface(int nodeID, String ipaddr)
            throws SQLException {

        if (ipaddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        AtInterface node = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ATINTERFACE WHERE NODEID = ? AND IPADDR = ? AND STATUS != 'D'");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            stmt.setString(2, ipaddr);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2AtInterface(rs);
        } finally {
            d.cleanUp();
        }
        if (nodes.length > 0) {
            return nodes[0];
        }
        return node;
    }

    /**
     * <p>getIpRoute</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.IpRouteInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static IpRouteInterface[] getIpRoute(int nodeID) throws SQLException {

        IpRouteInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPROUTEINTERFACE WHERE NODEID = ? AND STATUS != 'D' ORDER BY ROUTEDEST");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2IpRouteInterface(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getIpRoute</p>
     *
     * @param nodeID a int.
     * @param ifindex a int.
     * @return an array of {@link org.opennms.web.element.IpRouteInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static IpRouteInterface[] getIpRoute(int nodeID, int ifindex)
            throws SQLException {

        IpRouteInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPROUTEINTERFACE WHERE NODEID = ? AND ROUTEIFINDEX = ? AND STATUS != 'D' ORDER BY ROUTEDEST");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2IpRouteInterface(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>isParentNode</p>
     *
     * @param nodeID a int.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public static boolean isParentNode(int nodeID) throws SQLException {

        boolean isPN = false;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D' ");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isPN = true;
            }
        } finally {
            d.cleanUp();
        }

        return isPN;
    }

    /**
     * <p>isBridgeNode</p>
     *
     * @param nodeID a int.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public static boolean isBridgeNode(int nodeID) throws SQLException {

        boolean isPN = false;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM STPNODE WHERE NODEID = ? AND STATUS != 'D' ");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isPN = true;
            }
        } finally {
            d.cleanUp();
        }

        return isPN;
    }

    /**
     * <p>isRouteInfoNode</p>
     *
     * @param nodeID a int.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public static boolean isRouteInfoNode(int nodeID) throws SQLException {

        boolean isRI = false;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM IPROUTEINTERFACE WHERE NODEID = ? AND STATUS != 'D' ");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isRI = true;
            }
        } finally {
            d.cleanUp();
        }

        return isRI;
    }

    /**
     * <p>getDataLinksOnNode</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static DataLinkInterface[] getDataLinksOnNode(int nodeID) throws SQLException {
        DataLinkInterface[] normalnodes = null;
        normalnodes = NetworkElementFactory.getDataLinks(nodeID);
        DataLinkInterface[] parentnodes = null;
        parentnodes = NetworkElementFactory.getDataLinksFromNodeParent(nodeID);
        DataLinkInterface[] nodes = new DataLinkInterface[normalnodes.length+parentnodes.length]; 
        int j = 0;

        for (DataLinkInterface normalnode : normalnodes) {
        	nodes[j++] = normalnode;
        	
        }
        
        for (DataLinkInterface parentnode : parentnodes) {
        	nodes[j++] = parentnode;
        }

        return nodes;
    	
    }

    /**
     * <p>getLinkedNodeIdOnNode</p>
     *
     * @param nodeID a int.
     * @return a {@link java.util.Set} object.
     * @throws java.sql.SQLException if any.
     */
    public static Set<Integer> getLinkedNodeIdOnNode(int nodeID) throws SQLException {
        Set<Integer> nodes = new TreeSet<Integer>();
        Integer node = null;
        
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT distinct(nodeparentid) as parentid FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D'");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    	    while (rs.next()) {
	            Object element = new Integer(rs.getInt("parentid"));
	            if (element != null) {
	                node = ((Integer) element);
	            }
	            nodes.add(node);
	        }

            stmt = conn.prepareStatement("SELECT distinct(nodeid) as parentid FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D'");
            d.watch(stmt);
		    stmt.setInt(1, nodeID);
		    rs = stmt.executeQuery();
		    d.watch(rs);
    	    while (rs.next()) {
	            Object element = new Integer(rs.getInt("parentid"));
	            if (element != null) {
	                node = ((Integer) element);
	            }
	            nodes.add(node);
	        }
        } finally {
            d.cleanUp();
        }
        return nodes;
        
    }
    
    /**
     * <p>getLinkedNodeIdOnNode</p>
     *
     * @param nodeID a int.
     * @param conn a {@link java.sql.Connection} object.
     * @return a {@link java.util.Set} object.
     * @throws java.sql.SQLException if any.
     */
    public static Set<Integer> getLinkedNodeIdOnNode(int nodeID,Connection conn) throws SQLException {
        Set<Integer> nodes = new TreeSet<Integer>();
        Integer node = null;
        
        PreparedStatement stmt;
        ResultSet rs;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            stmt = conn.prepareStatement("SELECT distinct(nodeparentid) as parentid FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D'");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                Object element = new Integer(rs.getInt("parentid"));
                if (element != null) {
                    node = ((Integer) element);
                }
                nodes.add(node);
            }

            stmt = conn.prepareStatement("SELECT distinct(nodeid) as parentid FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D'");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                Object element = new Integer(rs.getInt("parentid"));
                if (element != null) {
                    node = ((Integer) element);
                }
                nodes.add(node);
            }
        } finally {
            d.cleanUp();
        }
        return nodes;
        
    }    

    /**
     * <p>getLinkedNodeIdOnNodes</p>
     *
     * @param nodeIds a {@link java.util.Set} object.
     * @param conn a {@link java.sql.Connection} object.
     * @return a {@link java.util.Set} object.
     * @throws java.sql.SQLException if any.
     */
    public static Set<Integer> getLinkedNodeIdOnNodes(Set<Integer> nodeIds, Connection conn) throws SQLException {
        List<Integer> nodes = new ArrayList<Integer>();
        if(nodeIds==null || nodeIds.size()==0){
        	return new TreeSet<Integer>();
        }
        
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
        	StringBuffer query = new StringBuffer("SELECT distinct(nodeparentid) as parentid FROM DATALINKINTERFACE WHERE NODEID IN (");
        	Iterator<Integer> it = nodeIds.iterator();
        	StringBuffer nodesStrBuff = new StringBuffer("");
        	while(it.hasNext()){
        		nodesStrBuff.append( (it.next()).toString());
        		if(it.hasNext()){
        			nodesStrBuff.append(", ");
        		}
        	}
        	query.append(nodesStrBuff);
        	query.append(") AND STATUS != 'D'");
        	
            PreparedStatement stmt = conn.prepareStatement(query.toString());
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
    	    while (rs.next()) {
	            nodes.add(new Integer(rs.getInt("parentid")));
	        }

            query = new StringBuffer("SELECT distinct(nodeid) as parentid FROM DATALINKINTERFACE WHERE NODEID IN (");
            query.append(nodesStrBuff);
        	query.append(") AND STATUS != 'D'");            
            rs = stmt.executeQuery();
            d.watch(rs);
    	    while (rs.next()) {
	            nodes.add(new Integer(rs.getInt("parentid")));
	        }
        } finally {
            d.cleanUp();
        }
        
        return new TreeSet<Integer>(nodes);
        
    }
    
    /**
     * <p>getDataLinks</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static DataLinkInterface[] getDataLinks(int nodeID)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' ORDER BY IFINDEX");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2DataLink(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getDataLinksFromNodeParent</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static DataLinkInterface[] getDataLinksFromNodeParent(int nodeID)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D' ORDER BY PARENTIFINDEX");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2DataLink(rs);
        } finally {
            d.cleanUp();
        }

        return invertDataLinkInterface(nodes);
    }

    /**
     * <p>getDataLinksOnInterface</p>
     *
     * @param nodeID a int.
     * @param ifindex a int.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static DataLinkInterface[] getDataLinksOnInterface(int nodeID, int ifindex) throws SQLException {
        DataLinkInterface[] normalnodes = null;
        normalnodes = NetworkElementFactory.getDataLinks(nodeID,ifindex);
        DataLinkInterface[] parentnodes = null;
        parentnodes = NetworkElementFactory.getDataLinksFromNodeParent(nodeID,ifindex);
        DataLinkInterface[] nodes = new DataLinkInterface[normalnodes.length+parentnodes.length]; 
        int j = 0;

        for (DataLinkInterface normalnode : normalnodes) {
        	nodes[j++] = normalnode;
        	
        }
        
        for (DataLinkInterface parentnode : parentnodes) {
        	nodes[j++] = parentnode;
        }

        return nodes;
    	
    	
    }

    /**
     * <p>getDataLinks</p>
     *
     * @param nodeID a int.
     * @param ifindex a int.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static DataLinkInterface[] getDataLinks(int nodeID, int ifindex)
    throws SQLException {

    	DataLinkInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
    	try {
    	    Connection conn = Vault.getDbConnection();
    	    d.watch(conn);
    		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' AND IFINDEX = ?");
    		d.watch(stmt);
    		stmt.setInt(1, nodeID);
    		stmt.setInt(2, ifindex);
    		ResultSet rs = stmt.executeQuery();
    		d.watch(rs);
    		
    		nodes = rs2DataLink(rs);
    	} finally {
    	    d.cleanUp();
    	}

    	return nodes;
    }

    /**
     * <p>getDataLinksFromNodeParent</p>
     *
     * @param nodeID a int.
     * @param ifindex a int.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static DataLinkInterface[] getDataLinksFromNodeParent(int nodeID,
            int ifindex) throws SQLException {

        DataLinkInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND PARENTIFINDEX = ? AND STATUS != 'D' ");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2DataLink(rs);
        } finally {
            d.cleanUp();
        }
        
        return invertDataLinkInterface(nodes);
    }

    /**
     * <p>getAllDataLinks</p>
     *
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static DataLinkInterface[] getAllDataLinks() throws SQLException {

        DataLinkInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE STATUS != 'D' ORDER BY NODEID, IFINDEX");
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2DataLink(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getVlansOnNode</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.Vlan} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Vlan[] getVlansOnNode(int nodeID) throws SQLException {
    	Vlan[] vlans = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            String sqlQuery = "SELECT * from vlan WHERE status != 'D' AND nodeid = ? order by vlanid;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            vlans = rs2Vlan(rs);
        } finally {
            d.cleanUp();
        }

        return vlans;
    }
    
    /**
     * <p>getStpInterface</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.StpInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static StpInterface[] getStpInterface(int nodeID)
            throws SQLException {

        StpInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            String sqlQuery = "SELECT DISTINCT(stpnode.nodeid) AS droot, stpinterfacedb.* FROM "
                    + "((SELECT DISTINCT(stpnode.nodeid) AS dbridge, stpinterface.* FROM "
                    + "stpinterface LEFT JOIN stpnode ON SUBSTR(stpportdesignatedbridge,5,16) = stpnode.basebridgeaddress " 
					+ "AND stpportdesignatedbridge != '0000000000000000'"
                    + "WHERE stpinterface.status != 'D' AND stpinterface.nodeid = ?) AS stpinterfacedb "
                    + "LEFT JOIN stpnode ON SUBSTR(stpportdesignatedroot, 5, 16) = stpnode.basebridgeaddress) order by stpinterfacedb.stpvlan, stpinterfacedb.ifindex;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2StpInterface(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getStpInterface</p>
     *
     * @param nodeID a int.
     * @param ifindex a int.
     * @return an array of {@link org.opennms.web.element.StpInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    public static StpInterface[] getStpInterface(int nodeID, int ifindex)
            throws SQLException {

        StpInterface[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            String sqlQuery = "SELECT DISTINCT(stpnode.nodeid) AS droot, stpinterfacedb.* FROM "
                + "((SELECT DISTINCT(stpnode.nodeid) AS dbridge, stpinterface.* FROM "
                + "stpinterface LEFT JOIN stpnode ON SUBSTR(stpportdesignatedbridge,5,16) = stpnode.basebridgeaddress "
				+ "AND stpportdesignatedbridge != '0000000000000000'"
                + "WHERE stpinterface.status != 'D' AND stpinterface.nodeid = ? AND stpinterface.ifindex = ?) AS stpinterfacedb "
                + "LEFT JOIN stpnode ON SUBSTR(stpportdesignatedroot, 5, 16) = stpnode.basebridgeaddress) order by stpinterfacedb.stpvlan, stpinterfacedb.ifindex;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            nodes = rs2StpInterface(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * <p>getStpNode</p>
     *
     * @param nodeID a int.
     * @return an array of {@link org.opennms.web.element.StpNode} objects.
     * @throws java.sql.SQLException if any.
     */
    public static StpNode[] getStpNode(int nodeID) throws SQLException {

        StpNode[] nodes = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            PreparedStatement stmt = conn.prepareStatement("select distinct(e2.nodeid) as stpdesignatedrootnodeid, e1.* from (stpnode e1 left join stpnode e2 on substr(e1.stpdesignatedroot, 5, 16) = e2.basebridgeaddress) where e1.nodeid = ? AND e1.status != 'D' ORDER BY e1.basevlan");
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            nodes = rs2StpNode(rs);
        } finally {
            d.cleanUp();
        }

        return nodes;
    }

    /**
     * This method returns the data from the result set as an array of
     * AtInterface objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.AtInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static AtInterface[] rs2AtInterface(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<AtInterface> atIfs = new ArrayList<AtInterface>();

        while (rs.next()) {
            AtInterface atIf = new AtInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            atIf.m_nodeId = ((Integer) element).intValue();

            element = rs.getString("ipaddr");
            atIf.m_ipaddr = (String) element;

            element = rs.getString("atphysaddr");
            atIf.m_physaddr = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null) {
                atIf.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));
            }

            element = new Integer(rs.getInt("sourcenodeID"));
            if (element != null) {
                atIf.m_sourcenodeid = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("ifindex"));
            if (element != null) {
                atIf.m_ifindex = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                atIf.m_status = ((String) element).charAt(0);
            }

            atIfs.add(atIf);
        }

        return atIfs.toArray(new AtInterface[atIfs.size()]);
    }

    /**
     * This method returns the data from the result set as an array of
     * IpRouteInterface objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.IpRouteInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static IpRouteInterface[] rs2IpRouteInterface(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<IpRouteInterface> ipRtIfs = new ArrayList<IpRouteInterface>();

        while (rs.next()) {
            IpRouteInterface ipRtIf = new IpRouteInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            ipRtIf.m_nodeId = ((Integer) element).intValue();

            element = rs.getString("routedest");
            ipRtIf.m_routedest = (String) element;

            element = rs.getString("routemask");
            ipRtIf.m_routemask = (String) element;

            element = rs.getString("routenexthop");
            ipRtIf.m_routenexthop = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null) {
                ipRtIf.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));
            }

            element = new Integer(rs.getInt("routeifindex"));
            if (element != null) {
                ipRtIf.m_routeifindex = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric1"));
            if (element != null) {
                ipRtIf.m_routemetric1 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric2"));
            if (element != null) {
                ipRtIf.m_routemetric2 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric3"));
            if (element != null) {
                ipRtIf.m_routemetric4 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric4"));
            if (element != null) {
                ipRtIf.m_routemetric4 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routemetric5"));
            if (element != null) {
                ipRtIf.m_routemetric5 = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routetype"));
            if (element != null) {
                ipRtIf.m_routetype = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("routeproto"));
            if (element != null) {
                ipRtIf.m_routeproto = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                ipRtIf.m_status = ((String) element).charAt(0);
            }

            ipRtIfs.add(ipRtIf);
        }

        return ipRtIfs.toArray(new IpRouteInterface[ipRtIfs.size()]);
    }

    /**
     * This method returns the data from the result set as an array of
     * StpInterface objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.StpInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static StpInterface[] rs2StpInterface(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<StpInterface> stpIfs = new ArrayList<StpInterface>();

        while (rs.next()) {
            StpInterface stpIf = new StpInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            stpIf.m_nodeId = ((Integer) element).intValue();

            element = rs.getTimestamp("lastpolltime");
            if (element != null) {
                stpIf.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));
            }

            element = new Integer(rs.getInt("bridgeport"));
            if (element != null) {
                stpIf.m_bridgeport = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("ifindex"));
            if (element != null) {
                stpIf.m_ifindex = ((Integer) element).intValue();
            }

            element = rs.getString("stpportdesignatedroot");
            stpIf.m_stpdesignatedroot = (String) element;

            element = new Integer(rs.getInt("stpportdesignatedcost"));
            if (element != null) {
                stpIf.m_stpportdesignatedcost = ((Integer) element).intValue();
            }

            element = rs.getString("stpportdesignatedbridge");
            stpIf.m_stpdesignatedbridge = (String) element;

            element = rs.getString("stpportdesignatedport");
            stpIf.m_stpdesignatedport = (String) element;

            element = new Integer(rs.getInt("stpportpathcost"));
            if (element != null) {
                stpIf.m_stpportpathcost = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stpportstate"));
            if (element != null) {
                stpIf.m_stpportstate = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stpvlan"));
            if (element != null) {
                stpIf.m_stpvlan = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                stpIf.m_status = ((String) element).charAt(0);
            }

            element = new Integer(rs.getInt("dbridge"));
            if (element != null) {
                stpIf.m_stpbridgenodeid = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("droot"));
            if (element != null) {
                stpIf.m_stprootnodeid = ((Integer) element).intValue();
            }
            
            if (stpIf.get_ifindex() == -1 ) {
                stpIf.m_ipaddr = getIpAddress(stpIf.get_nodeId());
            } else {
                stpIf.m_ipaddr = getIpAddress(stpIf.get_nodeId(), stpIf
                        .get_ifindex());
            }

            stpIfs.add(stpIf);
        }

        return stpIfs.toArray(new StpInterface[stpIfs.size()]);
    }

    /**
     * This method returns the data from the result set as an array of StpNode
     * objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.StpNode} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static StpNode[] rs2StpNode(ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<StpNode> stpNodes = new ArrayList<StpNode>();

        while (rs.next()) {
            StpNode stpNode = new StpNode();

            Object element = new Integer(rs.getInt("nodeId"));
            stpNode.m_nodeId = ((Integer) element).intValue();

            element = rs.getString("basebridgeaddress");
            stpNode.m_basebridgeaddress = (String) element;

            element = rs.getString("stpdesignatedroot");
            stpNode.m_stpdesignatedroot = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null) {
                stpNode.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));
            }

            element = new Integer(rs.getInt("basenumports"));
            if (element != null) {
                stpNode.m_basenumports = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("basetype"));
            if (element != null) {
                stpNode.m_basetype = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("basevlan"));
            if (element != null) {
                stpNode.m_basevlan = ((Integer) element).intValue();
            }

            element = rs.getString("basevlanname");
            if (element != null) {
                stpNode.m_basevlanname = (String) element;
            }

            element = new Integer(rs.getInt("stppriority"));
            if (element != null) {
                stpNode.m_stppriority = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stpprotocolspecification"));
            if (element != null) {
                stpNode.m_stpprotocolspecification = ((Integer) element)
                        .intValue();
            }

            element = new Integer(rs.getInt("stprootcost"));
            if (element != null) {
                stpNode.m_stprootcost = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("stprootport"));
            if (element != null) {
                stpNode.m_stprootport = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                stpNode.m_status = ((String) element).charAt(0);
            }

            element = new Integer(rs.getInt("stpdesignatedrootnodeid"));
            if (element != null) {
                stpNode.m_stprootnodeid = ((Integer) element).intValue();
            }

            stpNodes.add(stpNode);
        }

        return stpNodes.toArray(new StpNode[stpNodes.size()]);
    }

    /**
     * This method returns the data from the result set as an array of StpNode
     * objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.Vlan} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static Vlan[] rs2Vlan(ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<Vlan> vlan = new ArrayList<Vlan>();

        while (rs.next()) {
            Vlan vlanEntry = new Vlan();

            Object element = new Integer(rs.getInt("nodeId"));
            vlanEntry.m_nodeId = ((Integer) element).intValue();

            element = rs.getInt("vlanId");
            if (element != null) {
                vlanEntry.m_vlanId = ((Integer) element).intValue();
            }

            element = rs.getString("vlanname");
            vlanEntry.m_vlanname = (String) element;

            element = rs.getTimestamp("lastpolltime");
            if (element != null) {
                vlanEntry.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));
            }

            element = new Integer(rs.getInt("vlantype"));
            if (element != null) {
                vlanEntry.m_vlantype = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("vlanstatus"));
            if (element != null) {
            	vlanEntry.m_vlanstatus= ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                vlanEntry.m_status = ((String) element).charAt(0);
            }

            vlan.add(vlanEntry);
        }

        return vlan.toArray(new Vlan[vlan.size()]);
    }


    /**
     * This method returns the data from the result set as an array of
     * DataLinkInterface objects.
     *
     * @param rs a {@link java.sql.ResultSet} object.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @throws java.sql.SQLException if any.
     */
    protected static DataLinkInterface[] rs2DataLink(ResultSet rs)
            throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<DataLinkInterface> dataLinkIfs = new ArrayList<DataLinkInterface>();

        while (rs.next()) {
            DataLinkInterface dataLinkIf = new DataLinkInterface();

            Object element = new Integer(rs.getInt("nodeId"));
            dataLinkIf.m_nodeId = ((Integer) element).intValue();

            element = new Integer(rs.getInt("ifindex"));
            if (element != null) {
                dataLinkIf.m_ifindex = ((Integer) element).intValue();
            }

            element = rs.getTimestamp("lastpolltime");
            if (element != null) {
                dataLinkIf.m_lastPollTime = EventConstants.formatToString(new Date(
                        ((Timestamp) element).getTime()));
            }

            element = new Integer(rs.getInt("nodeparentid"));
            if (element != null) {
                dataLinkIf.m_nodeparentid = ((Integer) element).intValue();
            }

            element = new Integer(rs.getInt("parentifindex"));
            if (element != null) {
                dataLinkIf.m_parentifindex = ((Integer) element).intValue();
            }

            element = rs.getString("status");
            if (element != null) {
                dataLinkIf.m_status = ((String) element).charAt(0);
            }

            dataLinkIf.m_parentipaddress = getIpAddress(dataLinkIf.get_nodeparentid(), dataLinkIf
                    .get_parentifindex());

            if (dataLinkIf.get_ifindex() == -1 ) {
                dataLinkIf.m_ipaddress = getIpAddress(dataLinkIf.get_nodeId());
            } else {
                dataLinkIf.m_ipaddress = getIpAddress(dataLinkIf.get_nodeId(), dataLinkIf
                        .get_ifindex());
            }

            dataLinkIfs.add(dataLinkIf);
        }

        return dataLinkIfs.toArray(new DataLinkInterface[dataLinkIfs.size()]);
    }

    /**
     * <p>invertDataLinkInterface</p>
     *
     * @param nodes an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     * @return an array of {@link org.opennms.web.element.DataLinkInterface} objects.
     */
    protected static DataLinkInterface[] invertDataLinkInterface(DataLinkInterface[] nodes) {
    	for (int i=0; i<nodes.length;i++) {
    		DataLinkInterface dli = nodes[i];
    		dli.invertNodewithParent();
    		nodes[i] = dli;
    	}
    	
    	return nodes;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @param nodeid a int.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    protected static String getIpAddress(int nodeid) throws SQLException {

        String ipaddr = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(IPADDR) FROM IPINTERFACE WHERE NODEID = ?");
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                 ipaddr = rs.getString("ipaddr");
            }
        } finally {
            d.cleanUp();
        }

        return ipaddr;

    }

    /**
     * <p>getIpAddress</p>
     *
     * @param nodeid a int.
     * @param ifindex a int.
     * @return a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    protected static String getIpAddress(int nodeid, int ifindex)
            throws SQLException {
        String ipaddr = null;
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);

            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(IPADDR) FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX = ? ");
            d.watch(stmt);
            stmt.setInt(1, nodeid);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            while (rs.next()) {
                ipaddr = rs.getString("ipaddr");
            }
        } finally {
            d.cleanUp();
        }

        return ipaddr;

    }

    /**
     * Returns all non-deleted nodes with an IP address like the rule given.
     *
     * @param iplike a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    public static List<Integer> getNodeIdsWithIpLike(String iplike) throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<Integer> nodecont = new ArrayList<Integer>();
        final DBUtils d = new DBUtils(NetworkElementFactory.class);
        try {
            Connection conn = Vault.getDbConnection();
            d.watch(conn);
            
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(node.nodeid) FROM NODE,IPINTERFACE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODETYPE != 'D'");
            d.watch(stmt);
            stmt.setString(1, iplike);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);

            Integer node = null;
    	    while (rs.next()) {
	            Object element = new Integer(rs.getInt("nodeid"));
	            if (element != null) {
	                node = ((Integer) element);
	            }
	            nodecont.add(node);
	        }

        } finally {
            d.cleanUp();
        }

        return nodecont;
    }
    


    /**
     * <p>getNodesWithCategories</p>
     *
     * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     * @param categories1 an array of {@link java.lang.String} objects.
     * @param onlyNodesWithDownAggregateStatus a boolean.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     */
    public static Node[] getNodesWithCategories(TransactionTemplate transTemplate, final NodeDao nodeDao, final CategoryDao categoryDao, final String[] categories1, final boolean onlyNodesWithDownAggregateStatus) {
    	return transTemplate.execute(new TransactionCallback<Node[]>() {

			public Node[] doInTransaction(TransactionStatus arg0) {
				return getNodesWithCategories(nodeDao, categoryDao, categories1, onlyNodesWithDownAggregateStatus);	
			}
    		
    	});
    }
    
    /**
     * <p>getNodesWithCategories</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     * @param categories1 an array of {@link java.lang.String} objects.
     * @param onlyNodesWithDownAggregateStatus a boolean.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     */
    public static Node[] getNodesWithCategories(NodeDao nodeDao, CategoryDao categoryDao, String[] categories1, boolean onlyNodesWithDownAggregateStatus) {
        Collection<OnmsNode> ourNodes = getNodesInCategories(nodeDao, categoryDao, categories1);
        
        if (onlyNodesWithDownAggregateStatus) {
            AggregateStatus as = new AggregateStatus(new HashSet<OnmsNode>(ourNodes));
            ourNodes = as.getDownNodes();
        }
        return convertOnmsNodeCollectionToNodeArray(ourNodes);
    }

    private static Collection<OnmsNode> getNodesInCategories(NodeDao nodeDao,
            CategoryDao categoryDao, String[] categoryStrings) {
        
        ArrayList<OnmsCategory> categories =
            new ArrayList<OnmsCategory>(categoryStrings.length);
        for (String categoryString : categoryStrings) {
            categories.add(categoryDao.findByName(categoryString));
        }
        
        Collection<OnmsNode> ourNodes =
            nodeDao.findAllByCategoryList(categories);
        return ourNodes;
    }

    /**
     * <p>getNodesWithCategories</p>
     *
     * @param transTemplate a {@link org.springframework.transaction.support.TransactionTemplate} object.
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     * @param categories1 an array of {@link java.lang.String} objects.
     * @param categories2 an array of {@link java.lang.String} objects.
     * @param onlyNodesWithDownAggregateStatus a boolean.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     */
    public static Node[] getNodesWithCategories(TransactionTemplate transTemplate, final NodeDao nodeDao, final CategoryDao categoryDao, final String[] categories1, final String[] categories2, final boolean onlyNodesWithDownAggregateStatus) {
    	return transTemplate.execute(new TransactionCallback<Node[]>() {

			public Node[] doInTransaction(TransactionStatus arg0) {
				return getNodesWithCategories(nodeDao, categoryDao, categories1, categories2, onlyNodesWithDownAggregateStatus);	
			}
    		
    	});
    }
    /**
     * <p>getNodesWithCategories</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     * @param categories1 an array of {@link java.lang.String} objects.
     * @param categories2 an array of {@link java.lang.String} objects.
     * @param onlyNodesWithDownAggregateStatus a boolean.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     */
    public static Node[] getNodesWithCategories(NodeDao nodeDao, CategoryDao categoryDao, String[] categories1, String[] categories2, boolean onlyNodesWithDownAggregateStatus) {
        ArrayList<OnmsCategory> c1 = new ArrayList<OnmsCategory>(categories1.length);
        for (String category : categories1) {
                c1.add(categoryDao.findByName(category));
        }
        ArrayList<OnmsCategory> c2 = new ArrayList<OnmsCategory>(categories2.length);
        for (String category : categories2) {
                c2.add(categoryDao.findByName(category));
        }
        
        Collection<OnmsNode> ourNodes1 = getNodesInCategories(nodeDao, categoryDao, categories1);
        Collection<OnmsNode> ourNodes2 = getNodesInCategories(nodeDao, categoryDao, categories2);
        
        Set<Integer> n2id = new HashSet<Integer>(ourNodes2.size());
        for (OnmsNode n2 : ourNodes2) {
            n2id.add(n2.getId()); 
        }

        Set<OnmsNode> ourNodes = new HashSet<OnmsNode>();
        for (OnmsNode n1 : ourNodes1) {
            if (n2id.contains(n1.getId())) {
                ourNodes.add(n1);
            }
        }
        
        if (onlyNodesWithDownAggregateStatus) {
            AggregateStatus as = new AggregateStatus(ourNodes);
            ourNodes = as.getDownNodes();
        }

        return convertOnmsNodeCollectionToNodeArray(ourNodes);
    }
    
    /**
     * <p>convertOnmsNodeCollectionToNodeArray</p>
     *
     * @param ourNodes a {@link java.util.Collection} object.
     * @return an array of {@link org.opennms.web.element.Node} objects.
     */
    public static Node[] convertOnmsNodeCollectionToNodeArray(Collection<OnmsNode> ourNodes) {
        ArrayList<Node> theirNodes = new ArrayList<Node>(ourNodes.size());
        for (OnmsNode on : ourNodes) {
            theirNodes.add(new Node(on.getId().intValue(),
                                    0, //on.getParent().getId().intValue(),
                                    on.getLabel(),
                                    null, //on.getDpname(),
                                    on.getCreateTime().toString(),
                                    null, // on.getNodeSysId(),
                                    on.getSysName(),
                                    on.getSysDescription(),
                                    on.getSysLocation(),
                                    on.getSysContact(),
                                    on.getType().charAt(0),
                                    on.getOperatingSystem(),
                                    on.getForeignId(),
                                    on.getForeignSource()));

        }
        
        return theirNodes.toArray(new Node[0]);
    }

    public static class InterfaceComparator implements Comparator<Interface> {
        public int compare(Interface o1, Interface o2) {

            // Sort by IP first if the IPs are non-0.0.0.0
            if (!"0.0.0.0".equals(o1.getIpAddress()) && !"0.0.0.0".equals(o2.getIpAddress())) {
                if (InetAddressUtils.toIpAddrLong(o1.getIpAddress()) > InetAddressUtils.toIpAddrLong(o2.getIpAddress())) {
                    return 1;
                } else if (InetAddressUtils.toIpAddrLong(o1.getIpAddress()) < InetAddressUtils.toIpAddrLong(o2.getIpAddress())) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                // Sort IPs that are non-0.0.0.0 so they are first
                if (!"0.0.0.0".equals(o1.getIpAddress())) {
                    return -1;
                } else if (!"0.0.0.0".equals(o2.getIpAddress())) {
                    return 1;
                }
            }
            return 0;
        }
    }
}
