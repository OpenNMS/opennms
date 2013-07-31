/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.utils;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class contains convenience functions for retrieving and modifying the
 * label associated with a managed node. The 'node' table contains a 'nodelabel'
 * and 'nodelabelsource' field. The 'nodelabel' is a user-friendly name
 * associated with the node. This name can be user-defined (via the WEB UI) or
 * can be auto-generated based on what OpenNMS knows about the node and its
 * interfaces. The 'nodelabelsource' field is a single character flag which
 * indicates what the source for the node label was.
 * </P>
 *
 * <PRE>
 *
 * Valid values for node label source are: 'U' User defined 'H' Primary
 * interface's IP host name 'S' Node's MIB-II sysName 'A' Primary interface's IP
 * address
 *
 * </PRE>
 *
 * @author <A HREF="mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class NodeLabel {
	
	private final static Logger LOG = LoggerFactory.getLogger(NodeLabel.class);
	
    /**
     * The SQL statement to update the 'nodelabel' and 'nodelabelsource' fields
     * of 'node' table
     */
    private final static String SQL_DB_UPDATE_NODE_LABEL = "UPDATE node SET nodelabel=?,nodelabelsource=? WHERE nodeid=?";

    /**
     * The SQL statement to retrieve the NetBIOS name associated with a
     * particular nodeID
     */
    private final static String SQL_DB_RETRIEVE_NETBIOS_NAME = "SELECT nodenetbiosname FROM node WHERE nodeid=?";

    /**
     * The SQL statement to retrieve all managed IP address & hostName values
     * associated with a particular nodeID
     */
    private final static String SQL_DB_RETRIEVE_MANAGED_INTERFACES = "SELECT ipaddr,iphostname FROM ipinterface WHERE nodeid=? AND ismanaged='M'";

    /**
     * The SQL statement to retrieve all non-managed IP address & hostName
     * values associated with a particular nodeID
     */
    private final static String SQL_DB_RETRIEVE_NON_MANAGED_INTERFACES = "SELECT ipaddr,iphostname FROM ipinterface WHERE nodeid=? AND ismanaged!='M'";

    /**
     * The SQL statement to retrieve the MIB-II sysname field from the node
     * table
     */
    private final static String SQL_DB_RETRIEVE_SYSNAME = "SELECT nodesysname FROM node WHERE nodeid=?";

    /**
     * The SQL statement to retrieve the current node label and node label
     * source values associated with a node.
     */
    private final static String SQL_DB_RETRIEVE_NODELABEL = "SELECT nodelabel,nodelabelsource FROM node WHERE nodeid=?";

    /**
     * Valid values for node label source flag
     */
    public final static char SOURCE_USERDEFINED = 'U';

    /** Constant <code>SOURCE_NETBIOS='N'</code> */
    public final static char SOURCE_NETBIOS = 'N';

    /** Constant <code>SOURCE_HOSTNAME='H'</code> */
    public final static char SOURCE_HOSTNAME = 'H';

    /** Constant <code>SOURCE_SYSNAME='S'</code> */
    public final static char SOURCE_SYSNAME = 'S';

    /** Constant <code>SOURCE_ADDRESS='A'</code> */
    public final static char SOURCE_ADDRESS = 'A';

    /**
     * Initialization value for node label source flag
     */
    public final static char SOURCE_UNKNOWN = 'X';

    /**
     * Maximum length for node label
     */
    public final static int MAX_NODE_LABEL_LENGTH = 256;

    /**
     * Primary interface selection method MIN. Using this selection method the
     * interface with the smallest numeric IP address is considered the primary
     * interface.
     */
    private final static String SELECT_METHOD_MIN = "min";

    /**
     * Primary interface selection method MAX. Using this selection method the
     * interface with the greatest numeric IP address is considered the primary
     * interface.
     */
    private final static String SELECT_METHOD_MAX = "max";

    /**
     * Default primary interface select method.
     */
    private final static String DEFAULT_SELECT_METHOD = SELECT_METHOD_MIN;

    /**
     * Node label
     */
    private final String m_nodeLabel;

    /**
     * Flag describing source of node label
     */
    private final char m_nodeLabelSource;

    /**
     * The property string in the properties file which specifies the method to
     * use for determining which interface is primary on a multi-interface box.
     */
    public static final String PROP_PRIMARY_INTERFACE_SELECT_METHOD = "org.opennms.bluebird.dp.primaryInterfaceSelectMethod";

    /**
     * Default constructor
     */
    public NodeLabel() {
        m_nodeLabel = null;
        m_nodeLabelSource = SOURCE_UNKNOWN;
    }

    public NodeLabel(String nodeLabel, String nodeLabelSource) {
        this(nodeLabel, nodeLabelSource.charAt(0));
    }

    /**
     * Constructor
     *
     * @param nodeLabel
     *            Node label
     * @param nodeLabelSource
     *            Flag indicating source of node label
     */
    public NodeLabel(String nodeLabel, char nodeLabelSource) {
        switch(nodeLabelSource) {
            case SOURCE_ADDRESS:
            case SOURCE_HOSTNAME:
            case SOURCE_NETBIOS:
            case SOURCE_SYSNAME:
            case SOURCE_UNKNOWN:
            case SOURCE_USERDEFINED:
                break;
            default:
                throw new IllegalArgumentException("Invalid value for node label source: " + nodeLabelSource);
        }
        m_nodeLabel = nodeLabel;
        m_nodeLabelSource = nodeLabelSource;
    }

    /**
     * Returns the node label .
     *
     * @return node label
     */
    public String getLabel() {
        return m_nodeLabel;
    }

    /**
     * Returns the node label source flag .
     *
     * @return node label source flag
     */
    public char getSource() {
        return m_nodeLabelSource;
    }

    /**
     * This method queries the 'node' table for the value of the 'nodelabel' and
     * 'nodelabelsource' fields for the node with the provided nodeID. A
     * NodeLabel object is returned initialized with the retrieved values.
     *
     * WARNING: A properly instantiated and initialized Vault class object is
     * required prior to calling this method. This method will initially only be
     * called from the WEB UI.
     *
     * @param nodeID
     *            Unique identifier of the node to be updated.
     * @return Object containing label and source values.
     * @throws java.sql.SQLException if any.
     * 
     * @deprecated Use a {@link NodeDao#load(Integer)} method call instead
     */
    public static NodeLabel retrieveLabel(int nodeID) throws SQLException {
        NodeLabel label = null;
        Connection dbConnection = Vault.getDbConnection();

        try {
            label = retrieveLabel(nodeID, dbConnection);
        } finally {
            Vault.releaseDbConnection(dbConnection);
        }

        return label;
    }

    /**
     * This method queries the 'node' table for the value of the 'nodelabel' and
     * 'nodelabelsource' fields for the node with the provided nodeID. A
     * NodeLabel object is returned initialized with the retrieved values.
     *
     * @param nodeID
     *            Unique ID of node whose label info is to be retrieved
     * @param dbConnection
     *            SQL database connection
     * @return object initialized with node label & source flag
     * @throws java.sql.SQLException if any.
     * 
     * @deprecated Use a {@link NodeDao#load(Integer)} method call instead
     */
    public static NodeLabel retrieveLabel(int nodeID, Connection dbConnection) throws SQLException {
        String nodeLabel = null;
        String nodeLabelSource = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(NodeLabel.class);

        LOG.debug("NodeLabel.retrieveLabel: sql: {} node id: {}", SQL_DB_RETRIEVE_NODELABEL, nodeID);

        try {
            stmt = dbConnection.prepareStatement(SQL_DB_RETRIEVE_NODELABEL);
            d.watch(stmt);
            stmt.setInt(1, nodeID);

            // Issue database query
            rs = stmt.executeQuery();
            d.watch(rs);

            // Process result set, retrieve node's sysname
            if (rs.next()) {
                nodeLabel = rs.getString(1);
                nodeLabelSource = rs.getString(2);
            }
        } finally {
            d.cleanUp();
        }

        if (nodeLabelSource != null) {
            char[] temp = nodeLabelSource.toCharArray();
            return new NodeLabel(nodeLabel, temp[0]);
        } else
            return new NodeLabel(nodeLabel, SOURCE_UNKNOWN);
    }

    /**
     * This method updates the 'nodelabel' and 'nodelabelsource' fields of the
     * 'node' table for the specified nodeID. A database connection is retrieved
     * from the Vault.
     *
     * WARNING: A properly instantiated and initialized Vault class object is
     * required prior to calling this method. This method will initially only be
     * called from the WEB UI.
     *
     * @param nodeID
     *            Unique identifier of the node to be updated.
     * @param nodeLabel
     *            Object containing label and source values.
     * @throws java.sql.SQLException if any.
     * 
     * @deprecated Use a {@link NodeDao#update(org.opennms.netmgt.model.OnmsNode)} method call instead
     */
    public static void assignLabel(int nodeID, NodeLabel nodeLabel) throws SQLException {
        Connection dbConnection = Vault.getDbConnection();

        try {
            assignLabel(nodeID, nodeLabel, dbConnection);
        } finally {
            Vault.releaseDbConnection(dbConnection);
        }
    }

    /**
     * This method updates the 'nodelabel' and 'nodelabelsource' fields of the
     * 'node' table for the specified nodeID.
     *
     * If nodeLabel parameter is NULL the method will first call computeLabel()
     * and use the resulting NodeLabel object to update the database.
     *
     * @param nodeID
     *            Unique identifier of the node to be updated.
     * @param nodeLabel
     *            Object containing label and source values.
     * @param dbConnection
     *            SQL database connection
     * @throws java.sql.SQLException if any.
     * 
     * @deprecated Use a {@link NodeDao#update(org.opennms.netmgt.model.OnmsNode)} method call instead
     */
    public static void assignLabel(int nodeID, NodeLabel nodeLabel, Connection dbConnection) throws SQLException {
        if (nodeLabel == null) {
            nodeLabel = computeLabel(nodeID, dbConnection);
        }

        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(NodeLabel.class);

        try {
            // Issue SQL update to assign the 'nodelabel' && 'nodelabelsource' fields of the 'node' table
            stmt = dbConnection.prepareStatement(SQL_DB_UPDATE_NODE_LABEL);
            d.watch(stmt);
            int column = 1;

            // Node Label
            LOG.debug("NodeLabel.assignLabel: Node label: {} source: {}", nodeLabel.getLabel(), nodeLabel.getSource());

            if (nodeLabel.getLabel() != null) {
                // nodeLabel may not exceed MAX_NODELABEL_LEN.if it does truncate it
                String label = nodeLabel.getLabel();
                if (label.length() > MAX_NODE_LABEL_LENGTH) {
                    label = label.substring(0, MAX_NODE_LABEL_LENGTH);
                }
                stmt.setString(column++, label);
            } else {
                stmt.setNull(column++, java.sql.Types.VARCHAR);
            }

            // Node Label Source
            stmt.setString(column++, String.valueOf(nodeLabel.getSource()));

            // Node ID
            stmt.setInt(column++, nodeID);

            stmt.executeUpdate();
        } finally {
            d.cleanUp();
        }
    }

    /**
     * This method determines what label should be associated with a particular
     * node. A database connection is retrieved from the Vault.
     *
     * WARNING: A properly instantiated and initialized Vault class object is
     * required prior to calling this method. This method will initially only be
     * called from the WEB UI.
     *
     * @param nodeID
     *            Unique identifier of the node to be updated.
     * @return NodeLabel Object containing label and source values
     * @throws java.sql.SQLException if any.
     * 
     * @deprecated Update this to use modern DAO methods instead of raw SQL
     */
    public static NodeLabel computeLabel(int nodeID) throws SQLException {
        Connection dbConnection = Vault.getDbConnection();

        try {
            return computeLabel(nodeID, dbConnection);
        } finally {
            Vault.releaseDbConnection(dbConnection);
        }

    }

    /**
     * This method determines what label should be associated with a particular
     * node.
     *
     * Algorithm for determining a node's label is as follows: 1) If node has a
     * NetBIOS name associated with it, the NetBIOS name is used as the node's
     * label. 2) If no NetBIOS name available, retrieve all the 'ipinterface'
     * table entries associated with the node with an 'isManaged' field value of
     * 'M' 3) Find the primary interface where "primary" is defined as the
     * managed interface with the smallest IP address (each IP address is
     * converted to an integer value -- the IP address with the smallest integer
     * value wins). 4) IF the primary interface's IP host name is known it
     * becomes the node's label. ELSE IF the node's MIB-II sysName value is
     * known it becomes the node's label ELSE the primary interface's IP address
     * becomes the node's label.
     *
     * NOTE: If for some reason a node has no "managed" interfaces null is
     * returned for the NodeLabel.
     *
     * @param nodeID
     *            Unique identifier of the node to be updated.
     * @param dbConnection
     *            SQL database connection
     * @return NodeLabel Object containing label and source values or null if
     *         node does not have a primary interface.
     * @throws java.sql.SQLException if any.
     * 
     * @deprecated Update this to use modern DAO methods instead of raw SQL
     */
    public static NodeLabel computeLabel(int nodeID, Connection dbConnection) throws SQLException {
        // Issue SQL query to retrieve NetBIOS name associated with the node
        String netbiosName = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(NodeLabel.class);

        try {
            stmt = dbConnection.prepareStatement(SQL_DB_RETRIEVE_NETBIOS_NAME);
            d.watch(stmt);
            stmt.setInt(1, nodeID);

            rs = stmt.executeQuery();
            d.watch(rs);

            // Process result set, retrieve node's sysname
            while (rs.next()) {
                netbiosName = rs.getString(1);
            }

            if (netbiosName != null) {
                // Truncate sysName if it exceeds max node label length
                if (netbiosName.length() > MAX_NODE_LABEL_LENGTH) {
                    netbiosName = netbiosName.substring(0, MAX_NODE_LABEL_LENGTH);
                }

                LOG.debug("NodeLabel.computeLabel: returning NetBIOS name as nodeLabel: {}", netbiosName);
                    
                NodeLabel nodeLabel = new NodeLabel(netbiosName, SOURCE_NETBIOS);
                return nodeLabel;
            }
        } finally {
            d.cleanUp();
        }

        // OK, if we get this far the node has no NetBIOS name associated with it so,
        // retrieve the primary interface select method property which indicates
        // the method to use for determining which interface on a multi-interface
        // system is to be deemed the primary interface. The primary interface
        // will then determine what the node's label is.
        String method = System.getProperty(NodeLabel.PROP_PRIMARY_INTERFACE_SELECT_METHOD);
        if (method == null) {
            method = DEFAULT_SELECT_METHOD;
        }

        if (!method.equals(SELECT_METHOD_MIN) && !method.equals(SELECT_METHOD_MAX)) {
		LOG.warn("Interface selection method is '{}'.  Valid values are 'min' & 'max'.  Will use default value: {}", method, DEFAULT_SELECT_METHOD);
            method = DEFAULT_SELECT_METHOD;
        }

        List<InetAddress> ipv4AddrList = new ArrayList<InetAddress>();
        List<String> ipHostNameList = new ArrayList<String>();

        // Issue SQL query to retrieve all managed interface IP addresses from 'ipinterface' table
        try {
            stmt = dbConnection.prepareStatement(SQL_DB_RETRIEVE_MANAGED_INTERFACES);
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            rs = stmt.executeQuery();
            d.watch(rs);

            // Process result set, store retrieved addresses/host names in lists
            loadAddressList(rs, ipv4AddrList, ipHostNameList);
        } catch (Throwable e) {
            LOG.warn("Exception thrown while fetching managed interfaces: {}", e.getMessage(), e);
        } finally {
            d.cleanUp();
        }

        InetAddress primaryAddr = selectPrimaryAddress(ipv4AddrList, method);

        // Make sure we found a primary address!!!
        // If no primary address was found it means that this node has no
        // managed interfaces. So lets go after all the non-managed interfaces
        // and select the primary interface from them.
        if (primaryAddr == null) {
        	LOG.debug("NodeLabel.computeLabel: unable to find a primary address for node {}, returning null", nodeID);

            ipv4AddrList.clear();
            ipHostNameList.clear();

            try {
                // retrieve all non-managed interface IP addresses from 'ipinterface' table
                stmt = dbConnection.prepareStatement(SQL_DB_RETRIEVE_NON_MANAGED_INTERFACES);
                d.watch(stmt);
                stmt.setInt(1, nodeID);
                rs = stmt.executeQuery();
                d.watch(rs);
                loadAddressList(rs, ipv4AddrList, ipHostNameList);
            } catch (Throwable e) {
                LOG.warn("Exception thrown while fetching managed interfaces: {}", e.getMessage(), e);
            } finally {
                d.cleanUp();
            }

            primaryAddr = selectPrimaryAddress(ipv4AddrList, method);
        }

        if (primaryAddr == null) {
            LOG.warn("Could not find primary interface for node {}, cannot compute nodelabel", nodeID);
            return new NodeLabel("Unknown", SOURCE_UNKNOWN);
        }

        // We now know the IP address of the primary interface so
        // now see if it has a IP host name
        int index = ipv4AddrList.indexOf(primaryAddr);
        String primaryHostName = ipHostNameList.get(index);

        // If length of string is > 0 then the primary interface has a hostname
        if (primaryHostName.length() != 0) {
            // Truncate host name if it exceeds max node label length
            if (primaryHostName.length() > MAX_NODE_LABEL_LENGTH) {
                primaryHostName = primaryHostName.substring(0, MAX_NODE_LABEL_LENGTH);
            }

            return new NodeLabel(primaryHostName, SOURCE_HOSTNAME);
        }

        // If we get this far either the primary interface does not have
        // a host name or the node does not have a primary interface...
        // so we need to use the node's sysName if available...

        // retrieve sysName for the node
        String primarySysName = null;
        try {
            stmt = dbConnection.prepareStatement(SQL_DB_RETRIEVE_SYSNAME);
            d.watch(stmt);
            stmt.setInt(1, nodeID);
            rs = stmt.executeQuery();
            d.watch(rs);
            while (rs.next()) {
                primarySysName = rs.getString(1);
            }
        } finally {
            d.cleanUp();
        }

        if (primarySysName != null && primarySysName.length() > 0) {
            // Truncate sysName if it exceeds max node label length
            if (primarySysName.length() > MAX_NODE_LABEL_LENGTH) {
                primarySysName = primarySysName.substring(0, MAX_NODE_LABEL_LENGTH);
            }

            NodeLabel nodeLabel = new NodeLabel(primarySysName, SOURCE_SYSNAME);
            return nodeLabel;
        }

        // If we get this far the node has no sysName either so we need to
        // use the ipAddress as the nodeLabel
        NodeLabel nodeLabel = new NodeLabel(primaryAddr.toString(), SOURCE_ADDRESS);
        return nodeLabel;
    }

    /**
     * Utility method for loading the address and host name lists from a result
     * set retrieved from the 'ipInterface' table of the database.
     * 
     * @param rs
     *            Database result set
     * @param ipv4AddrList
     *            List of InetAddress objects representing the node's interfaces
     * @param ipHostNameList
     *            List of IP host names associated with the node's interfaces.
     * 
     * @throws SQLException
     *             if there is any problem processing the information in the
     *             result set.
     */
    private static void loadAddressList(ResultSet rs, List<InetAddress> ipv4AddrList, List<String> ipHostNameList) throws SQLException {

        // Process result set, store retrieved addresses/host names in lists
        while (rs.next()) {
            InetAddress inetAddr = InetAddressUtils.getInetAddress(rs.getString(1));
            ipv4AddrList.add(inetAddr);
            String hostName = rs.getString(2);

            // As a hack to get around the fact that the 'iphostname' field
            // will contain the IP address of the interface if the IP hostname
            // was not available we check to see if the hostname and address
            // are equivalent. The hostname is only added if they are different.
            // If the are the same, an empty string is added to the host name
            // list.
            if (hostName == null || hostName.equals(inetAddr.toString()))
                ipHostNameList.add("");
            else
                ipHostNameList.add(hostName);

            LOG.debug("NodeLabel.computeLabel: adding address {} with hostname: {}", inetAddr, hostName);
        }
    }

    /**
     * Returns the primary interface from a list of addresses based on the
     * specified selection method.
     * 
     * @param ipv4AddrList
     *            List of addresses from which to select the primary interface.
     * @param method
     *            String (either "min" or "max") which indicates how the primary
     *            interface is to be selected.
     * 
     * @return The InetAddress object from the address list which has been
     *         selected as the primary interface.
     */
    private static InetAddress selectPrimaryAddress(List<InetAddress> ipv4AddrList, String method) {
        // Determine which interface is the primary interface
        // (ie, the interface whose IP address when converted to an
        // integer is the smallest or largest depending upon the
        // configured selection method.)
        InetAddress primaryAddr = null;

        Iterator<InetAddress> iter = ipv4AddrList.iterator();
        while (iter.hasNext()) {
            if (primaryAddr == null) {
                primaryAddr = iter.next();
            } else {
                InetAddress currentAddr = iter.next();

                byte[] current = currentAddr.getAddress();
                byte[] primary = primaryAddr.getAddress();

                if (method.equals(SELECT_METHOD_MIN)) {
                    // Smallest address wins
                    if (new ByteArrayComparator().compare(current, primary) < 0) {
                        primaryAddr = currentAddr;
                    }
                } else {
                    // Largest address wins
                    if (new ByteArrayComparator().compare(current, primary) > 0) {
                        primaryAddr = currentAddr;
                    }
                }
            }
        }

        return primaryAddr;
    }

    /**
     * This method is responsible for returning a String object which represents
     * the content of this NodeLabel. Primarily used for debugging purposes.
     *
     * @return String which represents the content of this NodeLabel
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // Build the buffer
        buffer.append(m_nodeLabel);
        buffer.append(":");
        buffer.append(m_nodeLabelSource);

        return buffer.toString();
    }
}
