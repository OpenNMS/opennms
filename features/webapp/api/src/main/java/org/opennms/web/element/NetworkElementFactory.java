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

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.ThreadCategory;
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
     */
    public static String getNodeLabel(int nodeId) throws SQLException {
        String label = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT NODELABEL FROM NODE WHERE NODEID = ?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                label = rs.getString("NODELABEL");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return (label);
    }

    public static Node getNode(int nodeId) throws SQLException {
        Node node = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID = ?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            Node[] nodes = rs2Nodes(rs);

            // what do I do if this actually returns more than one node?
            if (nodes.length > 0) {
                node = nodes[0];
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return node;
    }

    /**
     * Returns all non-deleted nodes.
     */
    public static Node[] getAllNodes() throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM NODE WHERE NODETYPE != 'D' ORDER BY NODELABEL");

            nodes = rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes that have the given nodeLabel substring
     * somewhere in their nodeLabel.
     */
    public static Node[] getNodesLike(String nodeLabel) throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE LOWER(NODELABEL) LIKE ? AND NODETYPE != 'D' ORDER BY NODELABEL");
            stmt.setString(1, buffer.toString());
            ResultSet rs = stmt.executeQuery();

            nodes = rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes with an IP address like the rule given.
     */
    public static Node[] getNodesWithIpLike(String iplike) throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT node.* FROM NODE, IPINTERFACE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND IPINTERFACE.ISMANAGED != 'D' AND node.NODETYPE != 'D' ORDER BY node.NODELABEL");
            stmt.setString(1, iplike);
            ResultSet rs = stmt.executeQuery();

            nodes = rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes that have the given service.
     */
    public static Node[] getNodesWithService(int serviceId) throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT NODEID FROM IFSERVICES WHERE SERVICEID=?) AND NODETYPE != 'D' ORDER BY NODELABEL");
            stmt.setInt(1, serviceId);
            ResultSet rs = stmt.executeQuery();

            nodes = rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * Returns all non-deleted nodes that have the given mac.
     */
    public static Node[] getNodesWithPhysAddr(String macAddr) throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(macAddr);
            buffer.append("%");

        	PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT * FROM node WHERE " +
            		"nodetype != 'D' AND " +
            		"(nodeid IN (SELECT nodeid FROM snmpinterface WHERE snmpphysaddr LIKE ? ) OR " +
					" nodeid IN (SELECT nodeid FROM atinterface WHERE atphysaddr LIKE ? )) " +
            		"ORDER BY nodelabel");
            stmt.setString(1, buffer.toString());
            stmt.setString(2, buffer.toString());
            ResultSet rs = stmt.executeQuery();

            nodes = rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }


    /**
     * Returns all non-deleted nodes with a MAC address like the rule given from AtInterface.
     */

    public static Node[] getNodesWithPhysAddrAtInterface(String macAddr)
			throws SQLException {
		if (macAddr == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Node[] nodes = null;
		Connection conn = Vault.getDbConnection();

		try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(macAddr);
            buffer.append("%");

            PreparedStatement stmt = conn
					.prepareStatement("SELECT DISTINCT * FROM node WHERE nodetype != 'D' " +
							"AND nodeid IN (SELECT nodeid FROM atinterface WHERE atphysaddr LIKE '% ? %') ORDER BY nodelabel");

			stmt.setString(1, buffer.toString());
			ResultSet rs = stmt.executeQuery();

			nodes = rs2Nodes(rs);

			rs.close();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}

		return nodes;
	}

    /**
     * Returns all non-deleted nodes with a MAC address like the rule given from SnmpInterface.
     */

    public static Node[] getNodesWithPhysAddrFromSnmpInterface(String macAddr)
			throws SQLException {
		if (macAddr == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}

		Node[] nodes = null;
		Connection conn = Vault.getDbConnection();

		try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(macAddr);
            buffer.append("%");

			PreparedStatement stmt = conn
					.prepareStatement("SELECT DISTINCT * FROM node WHERE nodetype != 'D' AND " +
							"nodeid IN (SELECT nodeid FROM snmpinterface WHERE snmpphysaddr LIKE '% ? %') ORDER BY nodelabel");

			stmt.setString(1, buffer.toString());
			ResultSet rs = stmt.executeQuery();

			nodes = rs2Nodes(rs);

			rs.close();
			stmt.close();
		} finally {
			Vault.releaseDbConnection(conn);
		}

		return nodes;
	}

	/**
     * Returns all non-deleted nodes that contain the given string in an ifAlias
     * @Param ifAlias
     *               the ifAlias string we are looking for
     * @return nodes
     *               the nodes with a matching ifAlias on one or more interfaces
     */
    public static Node[] getNodesWithIfAlias(String ifAlias) throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(ifAlias);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT SNMPINTERFACE.NODEID FROM SNMPINTERFACE,IPINTERFACE WHERE SNMPINTERFACE.SNMPIFALIAS ILIKE ? AND SNMPINTERFACE.SNMPIFINDEX=IPINTERFACE.IFINDEX AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID AND IPINTERFACE.ISMANAGED != 'D') AND NODETYPE != 'D' ORDER BY NODELABEL");

	    stmt.setString(1, buffer.toString());
            ResultSet rs = stmt.executeQuery();

            nodes = rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * Resolve an IP address to a DNS hostname via the database. If no hostname
     * can be found, the given IP address is returned.
     */
    public static String getHostname(String ipAddress) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String hostname = ipAddress;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT IPADDR, IPHOSTNAME FROM IPINTERFACE WHERE IPADDR=? AND IPHOSTNAME IS NOT NULL");
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                hostname = rs.getString("IPHOSTNAME");
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return hostname;
    }

    public static Interface getInterface(int ipInterfaceId) throws SQLException {
        Interface intf = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE ID = ?");
            stmt.setInt(1, ipInterfaceId);
            ResultSet rs = stmt.executeQuery();

            Interface[] intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);

            // what do I do if this actually returns more than one node?
            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return intf;
    }

    public static Interface getInterface(int nodeId, String ipAddress) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Interface intf = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IPADDR=?");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            ResultSet rs = stmt.executeQuery();

            Interface[] intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);

            // what do I do if this actually returns more than one node?
            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return intf;
    }

    public static Interface getInterface(int nodeId, String ipAddress, int ifindex) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Interface intf = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IPADDR=? AND IFINDEX=?");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            stmt.setInt(3, ifindex);

            ResultSet rs = stmt.executeQuery();

            Interface[] intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);

            // what do I do if this actually returns more than one node?
            if (intfs.length > 0) {
                intf = intfs[0];
            }
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return intf;
    }

    public static Interface[] getInterfacesWithIpAddress(String ipAddress) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE IPADDR=?");
            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();

            intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);

        } finally {
            Vault.releaseDbConnection(conn);
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
     */
    public static Interface[] getInterfacesWithIfAlias(int nodeId, String ifAlias) throws SQLException {
        if (ifAlias == null) {
            throw new IllegalArgumentException("Cannot take null parameter ifAlias");
        }

        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(ifAlias);
            buffer.append("%");

            PreparedStatement stmt = conn.prepareStatement("");
	    if(nodeId > 0) {
      		stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX IN (SELECT SNMPIFINDEX FROM SNMPINTERFACE WHERE SNMPIFALIAS ILIKE ? AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID) AND ISMANAGED != 'D'");
            stmt.setInt(1, nodeId);
	    stmt.setString(2, buffer.toString());
	    } else {
                stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE IPINTERFACE.IFINDEX IN (SELECT SNMPIFINDEX FROM SNMPINTERFACE WHERE SNMPIFALIAS ILIKE ? AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID) AND IPINTERFACE.ISMANAGED != 'D' ORDER BY IPINTERFACE.NODEID");
	    stmt.setString(1, buffer.toString());
	    }
            ResultSet rs = stmt.executeQuery();

            intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);

        } finally {
            Vault.releaseDbConnection(conn);
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
     */
    public static boolean nodeHasIfAliases(int nodeId) throws SQLException {

        boolean hasAliases = false;

        if (nodeId > 0) {
            Connection conn = Vault.getDbConnection();

            try {
               PreparedStatement stmt = conn.prepareStatement("SELECT ID FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX IN (SELECT SNMPIFINDEX FROM SNMPINTERFACE WHERE SNMPIFALIAS ILIKE '%_%' AND IPINTERFACE.NODEID=SNMPINTERFACE.NODEID) AND ISMANAGED != 'D'");
                stmt.setInt(1, nodeId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    hasAliases = true;
                }

                rs.close();
                stmt.close();

            } finally {
                Vault.releaseDbConnection(conn);
            }
        }

        return hasAliases;
    }

    public static Interface[] getAllInterfacesOnNode(int nodeId) throws SQLException {
        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return intfs;
    }

    public static Interface[] getActiveInterfacesOnNode(int nodeId) throws SQLException {
        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM IPINTERFACE WHERE NODEID = ? AND ISMANAGED != 'D' ORDER BY IFINDEX");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            augmentInterfacesWithSnmpData(intfs, conn);
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return intfs;
    }

    /*
     * Returns all interfaces, including their SNMP information
     */
    public static Interface[] getAllInterfaces() throws SQLException {
        return getAllInterfaces(true);
    }

    /*
     * Returns all interfaces, but only includes snmp data if includeSNMP is true
     * This may be useful for pages that don't need snmp data and don't want to execute
     * a sub-query per interface!
     */
    public static Interface[] getAllInterfaces(boolean includeSNMP) throws SQLException {
        Interface[] intfs = null;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM IPINTERFACE ORDER BY IPHOSTNAME, NODEID, IPADDR");

            intfs = rs2Interfaces(rs);

            rs.close();
            stmt.close();

            if(includeSNMP) {
                augmentInterfacesWithSnmpData(intfs, conn);
            }            
        } finally {
            Vault.releaseDbConnection(conn);
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
     */
    public static Service getService(int nodeId, String ipAddress, int serviceId) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Service service = null;
        Connection conn = Vault.getDbConnection();

        try {
            // big hack here, I'm relying on the fact that the ifservices.status
            // field uses 'A' as active, and thus should always turn up before
            // any
            // historically deleted services
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.IPADDR=? AND IFSERVICES.SERVICEID=? ORDER BY IFSERVICES.STATUS");
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            stmt.setInt(3, serviceId);
            ResultSet rs = stmt.executeQuery();

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

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
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
     */
    public static Service getService(int ifServiceId) throws SQLException {
        Service service = null;
        Connection conn = Vault.getDbConnection();

        try {
            // big hack here, I'm relying on the fact that the ifservices.status
            // field uses 'A' as active, and thus should always turn up before
            // any
            // historically deleted services
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.ID=? ORDER BY IFSERVICES.STATUS");
            stmt.setInt(1, ifServiceId);
            ResultSet rs = stmt.executeQuery();

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

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return service;
    }

    public static Service[] getAllServices() throws SQLException {
        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID = SERVICE.SERVICEID ORDER BY SERVICE.SERVICEID, inet(IFSERVICES.IPADDR)");

            services = rs2Services(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return services;
    }

    public static Service[] getServicesOnInterface(int nodeId, String ipAddress) throws SQLException {
        return getServicesOnInterface(nodeId, ipAddress, false);
    }

    public static Service[] getServicesOnInterface(int nodeId, String ipAddress, boolean includeDeletions) throws SQLException {
        if (ipAddress == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.IPADDR=?");

            if (!includeDeletions) {
                buffer.append(" AND IFSERVICES.STATUS <> 'D'");
            }

            PreparedStatement stmt = conn.prepareStatement(buffer.toString());
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddress);
            ResultSet rs = stmt.executeQuery();

            services = rs2Services(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return services;
    }

    /**
     * Get the list of all services on a given node.
     */
    public static Service[] getServicesOnNode(int nodeId) throws SQLException {
        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=?");
            stmt.setInt(1, nodeId);
            ResultSet rs = stmt.executeQuery();

            services = rs2Services(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return services;
    }

    /**
     * Get the list of all instances of a specific service on a given node.
     */
    public static Service[] getServicesOnNode(int nodeId, int serviceId) throws SQLException {
        Service[] services = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT IFSERVICES.*, SERVICE.SERVICENAME FROM IFSERVICES, SERVICE WHERE IFSERVICES.SERVICEID=SERVICE.SERVICEID AND IFSERVICES.NODEID=? AND IFSERVICES.SERVICEID=?");
            stmt.setInt(1, nodeId);
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();

            services = rs2Services(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return services;
    }

    /**
     * This method returns the data from the result set as an array of Node
     * objects.
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

    protected static void augmentInterfacesWithSnmpData(Interface[] intfs, Connection conn) throws SQLException {
        if (intfs == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        for (int i = 0; i < intfs.length; i++) {
            if (intfs[i].getIfIndex() != 0) {
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM SNMPINTERFACE WHERE NODEID=? AND SNMPIFINDEX=?");
                pstmt.setInt(1, intfs[i].getNodeId());
                pstmt.setInt(2, intfs[i].getIfIndex());

                ResultSet rs = pstmt.executeQuery();

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
                }

                rs.close();
                pstmt.close();

                pstmt = conn.prepareStatement("SELECT issnmpprimary FROM ipinterface WHERE nodeid=? AND ifindex=? AND ipaddr=?");
                pstmt.setInt(1, intfs[i].getNodeId());
                pstmt.setInt(2, intfs[i].getIfIndex());
		pstmt.setString(3, intfs[i].getIpAddress());

                rs = pstmt.executeQuery();

                if (rs.next()) {
                    intfs[i].m_isSnmpPrimary = rs.getString("issnmpprimary");
                }

                rs.close();
                pstmt.close();
            }
        }
    }

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

    public static String getServiceNameFromId(int serviceId) throws SQLException {
        if (serviceId2NameMap == null) {
            createServiceIdNameMaps();
        }

        String serviceName = serviceId2NameMap.get(new Integer(serviceId));

        return (serviceName);
    }

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

    public static Map<Integer, String> getServiceIdToNameMap() throws SQLException {
        if (serviceId2NameMap == null) {
            createServiceIdNameMaps();
        }

        return (new HashMap<Integer, String>(serviceId2NameMap));
    }

    public static Map<String, Integer> getServiceNameToIdMap() throws SQLException {
        if (serviceName2IdMap == null) {
            createServiceIdNameMaps();
        }

        return (new HashMap<String, Integer>(serviceName2IdMap));
    }

    protected static void createServiceIdNameMaps() throws SQLException {
        HashMap<Integer, String> idMap = new HashMap<Integer, String>();
        HashMap<String, Integer> nameMap = new HashMap<String, Integer>();
        Connection conn = Vault.getDbConnection();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SERVICEID, SERVICENAME FROM SERVICE");

            while (rs.next()) {
                int id = rs.getInt("SERVICEID");
                String name = rs.getString("SERVICENAME");

                idMap.put(new Integer(id), name);
                nameMap.put(name, new Integer(id));
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        serviceId2NameMap = idMap;
        serviceName2IdMap = nameMap;
    }

    // OpenNMS IA Stuff
    
    public static Node[] getNodesLikeAndIpLike(String nodeLabel, String iplike,
            int serviceId) throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM NODE WHERE NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) AND NODETYPE != 'D' AND LOWER(NODELABEL) LIKE ? AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODE.NODEID=IPINTERFACE.NODEID ORDER BY NODELABEL");
            stmt.setInt(1, serviceId);
            stmt.setString(2, buffer.toString());
            stmt.setString(3, iplike);

            ResultSet rs = stmt.executeQuery();

            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getNodesLike(String nodeLabel, int serviceId)
            throws SQLException {
        if (nodeLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        nodeLabel = nodeLabel.toLowerCase();
        Connection conn = Vault.getDbConnection();

        try {
            StringBuffer buffer = new StringBuffer("%");
            buffer.append(nodeLabel);
            buffer.append("%");

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM NODE WHERE LOWER(NODELABEL) LIKE ? AND NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            stmt.setString(1, buffer.toString());
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();

            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getNodesWithIpLike(String iplike, int serviceId)
            throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT * FROM NODE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            stmt.setString(1, iplike);
            stmt.setInt(2, serviceId);
            ResultSet rs = stmt.executeQuery();

            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getAllNodes(int serviceId) throws SQLException {
        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM NODE WHERE NODETYPE != 'D' AND NODEID IN (SELECT DISTINCT NODEID FROM IFSERVICES WHERE SERVICEID = ?) ORDER BY NODELABEL");
            stmt.setInt(1, serviceId);
            ResultSet rs = stmt.executeQuery();
            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static AtInterface[] getAtInterfacesFromPhysaddr(String AtPhysAddr)
            throws SQLException {

        if (AtPhysAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM ATINTERFACE WHERE ATPHYSADDR LIKE '%"
                            + AtPhysAddr + "%' AND STATUS != 'D'");
            ResultSet rs = stmt.executeQuery();
            nodes = rs2AtInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Node[] getNodesFromPhysaddr(String AtPhysAddr)
            throws SQLException {

        if (AtPhysAddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        Node[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT(*) FROM IPINTERFACE WHERE NODEID IN "
                            + "(SELECT NODEID FROM ATINTERFACE WHERE ATPHYSADDR LIKE '%"
                            + AtPhysAddr + "%' AND STATUS != 'D'");
            ResultSet rs = stmt.executeQuery();
            nodes = NetworkElementFactory.rs2Nodes(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static AtInterface getAtInterface(int nodeID, String ipaddr)
            throws SQLException {

        if (ipaddr == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        AtInterface[] nodes = null;
        AtInterface node = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM ATINTERFACE WHERE NODEID = ? AND IPADDR = ? AND STATUS != 'D'");
            stmt.setInt(1, nodeID);
            stmt.setString(2, ipaddr);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2AtInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
        if (nodes.length > 0) {
            return nodes[0];
        }
        return node;
    }

    public static IpRouteInterface[] getIpRoute(int nodeID) throws SQLException {

        IpRouteInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM IPROUTEINTERFACE WHERE NODEID = ? AND STATUS != 'D' ORDER BY ROUTEDEST");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2IpRouteInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static IpRouteInterface[] getIpRoute(int nodeID, int ifindex)
            throws SQLException {

        IpRouteInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM IPROUTEINTERFACE WHERE NODEID = ? AND ROUTEIFINDEX = ? AND STATUS != 'D' ORDER BY ROUTEDEST");
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2IpRouteInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static boolean isParentNode(int nodeID) throws SQLException {

        Connection conn = Vault.getDbConnection();
        boolean isPN = false;
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isPN = true;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return isPN;
    }

    public static boolean isBridgeNode(int nodeID) throws SQLException {

        Connection conn = Vault.getDbConnection();
        boolean isPN = false;
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) FROM STPNODE WHERE NODEID = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isPN = true;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return isPN;
    }

    public static boolean isRouteInfoNode(int nodeID) throws SQLException {

        Connection conn = Vault.getDbConnection();
        boolean isRI = false;
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT COUNT(*) FROM IPROUTEINTERFACE WHERE NODEID = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                isRI = true;
            }

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return isRI;
    }

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

    public static Set<Integer> getLinkedNodeIdOnNode(int nodeID) throws SQLException {
        Set<Integer> nodes = new TreeSet<Integer>();
        Connection conn = Vault.getDbConnection();
        Integer node = null;
        
        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT distinct(nodeparentid) as parentid FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D'");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
    	    while (rs.next()) {
	            Object element = new Integer(rs.getInt("parentid"));
	            if (element != null) {
	                node = ((Integer) element);
	            }
	            nodes.add(node);
	        }
	        rs.close();
            stmt.close();
            stmt = conn.prepareStatement("SELECT distinct(nodeid) as parentid FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D'");
		    stmt.setInt(1, nodeID);
		    rs = stmt.executeQuery();
    	    while (rs.next()) {
	            Object element = new Integer(rs.getInt("parentid"));
	            if (element != null) {
	                node = ((Integer) element);
	            }
	            nodes.add(node);
	        }
		    rs.close();
		    stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
        return nodes;
        
    }
    
    public static Set<Integer> getLinkedNodeIdOnNode(int nodeID,Connection conn) throws SQLException {
        Set<Integer> nodes = new TreeSet<Integer>();
        Integer node = null;
        
   
        PreparedStatement stmt = conn
                .prepareStatement("SELECT distinct(nodeparentid) as parentid FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D'");
        stmt.setInt(1, nodeID);
        ResultSet rs = stmt.executeQuery();
	    while (rs.next()) {
            Object element = new Integer(rs.getInt("parentid"));
            if (element != null) {
                node = ((Integer) element);
            }
            nodes.add(node);
        }
        rs.close();
        stmt.close();
        stmt = conn.prepareStatement("SELECT distinct(nodeid) as parentid FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D'");
	    stmt.setInt(1, nodeID);
	    rs = stmt.executeQuery();
	    while (rs.next()) {
            Object element = new Integer(rs.getInt("parentid"));
            if (element != null) {
                node = ((Integer) element);
            }
            nodes.add(node);
        }
	    rs.close();
	    stmt.close();
        return nodes;
        
    }    

    public static Set<Integer> getLinkedNodeIdOnNodes(Set<Integer> nodeIds, Connection conn) throws SQLException {
		String LOG4J_CATEGORY = "OpenNMS.Map";
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		Category log= ThreadCategory.getInstance(NetworkElementFactory.class);
		
        List<Integer> nodes = new ArrayList<Integer>();
        if(nodeIds==null || nodeIds.size()==0){
        	return new TreeSet<Integer>();
        }
        
        try {
        	log.debug("Before First select");
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
            ResultSet rs = stmt.executeQuery();
    	    while (rs.next()) {
	            nodes.add(new Integer(rs.getInt("parentid")));
	        }
	        rs.close();
            stmt.close();
            log.debug("After First select");
            log.debug("Before Second select");
            query = new StringBuffer("SELECT distinct(nodeid) as parentid FROM DATALINKINTERFACE WHERE NODEID IN (");
            query.append(nodesStrBuff);
        	query.append(") AND STATUS != 'D'");            
            rs = stmt.executeQuery();
    	    while (rs.next()) {
	            nodes.add(new Integer(rs.getInt("parentid")));
	        }
		    rs.close();
		    stmt.close();
		    log.debug("After Second select");
        } finally {
            
        }
        
        return new TreeSet<Integer>(nodes);
        
    }
    
    protected static DataLinkInterface[] getDataLinks(int nodeID)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' ORDER BY IFINDEX");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    protected static DataLinkInterface[] getDataLinksFromNodeParent(int nodeID)
            throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND STATUS != 'D' ORDER BY PARENTIFINDEX");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return invertDataLinkInterface(nodes);
    }

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

    public static DataLinkInterface[] getDataLinks(int nodeID, int ifindex)
    throws SQLException {

    	DataLinkInterface[] nodes = null;
    	Connection conn = Vault.getDbConnection();

    	try {
    		PreparedStatement stmt = conn
            .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEID = ? AND STATUS != 'D' AND IFINDEX = ?");
    		stmt.setInt(1, nodeID);
    		stmt.setInt(2, ifindex);
    		ResultSet rs = stmt.executeQuery();
    		nodes = rs2DataLink(rs);

    		rs.close();
    		stmt.close();
    	} finally {
    		Vault.releaseDbConnection(conn);
    	}

    	return nodes;
    }

    protected static DataLinkInterface[] getDataLinksFromNodeParent(int nodeID,
            int ifindex) throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE NODEPARENTID = ? AND PARENTIFINDEX = ? AND STATUS != 'D' ");
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }
        
        return invertDataLinkInterface(nodes);
    }

    public static DataLinkInterface[] getAllDataLinks() throws SQLException {

        DataLinkInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement("SELECT * FROM DATALINKINTERFACE WHERE STATUS != 'D' ORDER BY NODEID, IFINDEX");
            ResultSet rs = stmt.executeQuery();
            nodes = rs2DataLink(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static Vlan[] getVlansOnNode(int nodeID) throws SQLException {
    	Vlan[] vlans = null;
        Connection conn = Vault.getDbConnection();
        try {

            String sqlQuery = "SELECT * from vlan WHERE status != 'D' AND nodeid = ? order by vlanid;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            vlans = rs2Vlan(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return vlans;
    }
    
    public static StpInterface[] getStpInterface(int nodeID)
            throws SQLException {

        StpInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {

            String sqlQuery = "SELECT DISTINCT(stpnode.nodeid) AS droot, stpinterfacedb.* FROM "
                    + "((SELECT DISTINCT(stpnode.nodeid) AS dbridge, stpinterface.* FROM "
                    + "stpinterface LEFT JOIN stpnode ON SUBSTR(stpportdesignatedbridge,5,16) = stpnode.basebridgeaddress " 
					+ "AND stpportdesignatedbridge != '0000000000000000'"
                    + "WHERE stpinterface.status != 'D' AND stpinterface.nodeid = ?) AS stpinterfacedb "
                    + "LEFT JOIN stpnode ON SUBSTR(stpportdesignatedroot, 5, 16) = stpnode.basebridgeaddress) order by stpinterfacedb.stpvlan, stpinterfacedb.ifindex;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2StpInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static StpInterface[] getStpInterface(int nodeID, int ifindex)
            throws SQLException {

        StpInterface[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            String sqlQuery = "SELECT DISTINCT(stpnode.nodeid) AS droot, stpinterfacedb.* FROM "
                + "((SELECT DISTINCT(stpnode.nodeid) AS dbridge, stpinterface.* FROM "
                + "stpinterface LEFT JOIN stpnode ON SUBSTR(stpportdesignatedbridge,5,16) = stpnode.basebridgeaddress "
				+ "AND stpportdesignatedbridge != '0000000000000000'"
                + "WHERE stpinterface.status != 'D' AND stpinterface.nodeid = ? AND stpinterface.ifindex = ?) AS stpinterfacedb "
                + "LEFT JOIN stpnode ON SUBSTR(stpportdesignatedroot, 5, 16) = stpnode.basebridgeaddress) order by stpinterfacedb.stpvlan, stpinterfacedb.ifindex;";

            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setInt(1, nodeID);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2StpInterface(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    public static StpNode[] getStpNode(int nodeID) throws SQLException {

        StpNode[] nodes = null;
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn
                    .prepareStatement(
                    //		"SELECT * FROM STPNODE WHERE NODEID = ? AND STATUS != 'D'
                    // ORDER BY basevlan");
                    "select distinct(e2.nodeid) as stpdesignatedrootnodeid, e1.* from (stpnode e1 left join stpnode e2 on substr(e1.stpdesignatedroot, 5, 16) = e2.basebridgeaddress) where e1.nodeid = ? AND e1.status != 'D' ORDER BY e1.basevlan");
            stmt.setInt(1, nodeID);
            ResultSet rs = stmt.executeQuery();
            nodes = rs2StpNode(rs);

            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodes;
    }

    /**
     * This method returns the data from the result set as an array of
     * AtInterface objects.
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

    protected static DataLinkInterface[] invertDataLinkInterface(DataLinkInterface[] nodes) {
    	for (int i=0; i<nodes.length;i++) {
    		DataLinkInterface dli = nodes[i];
    		dli.invertNodewithParent();
    		nodes[i] = dli;
    	}
    	
    	return nodes;
    }

    protected static String getIpAddress(int nodeid) throws SQLException {

        String ipaddr = null;
        Connection conn = Vault.getDbConnection();

        try {

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT(IPADDR) FROM IPINTERFACE WHERE NODEID = ?");
            stmt.setInt(1, nodeid);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                 ipaddr = rs.getString("ipaddr");
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return ipaddr;

    }

    protected static String getIpAddress(int nodeid, int ifindex)
            throws SQLException {
        String ipaddr = null;
        Connection conn = Vault.getDbConnection();

        try {

            PreparedStatement stmt = conn
                    .prepareStatement("SELECT DISTINCT(IPADDR) FROM IPINTERFACE WHERE NODEID = ? AND IFINDEX = ? ");
            stmt.setInt(1, nodeid);
            stmt.setInt(2, ifindex);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ipaddr = rs.getString("ipaddr");
            }
            rs.close();
            stmt.close();
        } finally {
            Vault.releaseDbConnection(conn);
        }

        return ipaddr;

    }

    /**
     * Returns all non-deleted nodes with an IP address like the rule given.
     */
    public static List<Integer> getNodeIdsWithIpLike(String iplike) throws SQLException {
        if (iplike == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        List<Integer> nodecont = new ArrayList<Integer>();
        Connection conn = Vault.getDbConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(node.nodeid) FROM NODE,IPINTERFACE WHERE NODE.NODEID=IPINTERFACE.NODEID AND IPLIKE(IPINTERFACE.IPADDR,?) AND NODETYPE != 'D'");
            stmt.setString(1, iplike);
            ResultSet rs = stmt.executeQuery();

            Integer node = null;
    	    while (rs.next()) {
	            Object element = new Integer(rs.getInt("nodeid"));
	            if (element != null) {
	                node = ((Integer) element);
	            }
	            nodecont.add(node);
	        }
	        rs.close();
            stmt.close();

        } finally {
            Vault.releaseDbConnection(conn);
        }

        return nodecont;
    }
    


    public static Node[] getNodesWithCategories(TransactionTemplate transTemplate, final NodeDao nodeDao, final CategoryDao categoryDao, final String[] categories1, final boolean onlyNodesWithDownAggregateStatus) {
    	return (Node[])transTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus arg0) {
				return getNodesWithCategories(nodeDao, categoryDao, categories1, onlyNodesWithDownAggregateStatus);	
			}
    		
    	});
    }
    
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

    public static Node[] getNodesWithCategories(TransactionTemplate transTemplate, final NodeDao nodeDao, final CategoryDao categoryDao, final String[] categories1, final String[] categories2, final boolean onlyNodesWithDownAggregateStatus) {
    	return (Node[])transTemplate.execute(new TransactionCallback() {

			public Object doInTransaction(TransactionStatus arg0) {
				return getNodesWithCategories(nodeDao, categoryDao, categories1, categories2, onlyNodesWithDownAggregateStatus);	
			}
    		
    	});
    }
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
                if (IPSorter.convertToLong(o1.getIpAddress()) > IPSorter.convertToLong(o2.getIpAddress())) {
                    return 1;
                } else if (IPSorter.convertToLong(o1.getIpAddress()) < IPSorter.convertToLong(o2.getIpAddress())) {
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
