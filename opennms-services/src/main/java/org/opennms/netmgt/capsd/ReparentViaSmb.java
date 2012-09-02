/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.capsd.DbNodeEntry;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * This class is designed to reparent interfaces in the database based on the
 * SMB protocol. Specifically, if two nodes in the 'node' table have identical
 * NetBIOS names it is assumed that those two nodes actually represent different
 * interfaces (physical or alias'd) on the same box. The node with the lowest
 * nodeID becomes the "reparent node" and the other nodes are considered
 * duplicates. All interfaces under each duplicate node are then reparented
 * under the "reparent node" and the duplicate node(s) are flagged in the
 * database as deleted (nodeType='D').
 *
 * @author <A HREF="mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 */
public final class ReparentViaSmb {
    /**
     * SQL Statements
     */
    final static String SQL_DB_RETRIEVE_NODES = "SELECT nodeid,nodenetbiosname FROM node WHERE nodeType!='D' AND nodenetbiosname is not null ORDER BY nodeid";

    final static String SQL_DB_RETRIEVE_NODE = "SELECT nodesysname,nodesysdescription,nodelabel,nodelabelsource FROM node WHERE nodeid=? AND nodeType!='D'";

    final static String SQL_DB_RETRIEVE_INTERFACES = "SELECT ipaddr,iphostname FROM ipinterface WHERE nodeid=? AND isManaged!='D'";

    final static String SQL_DB_REPARENT_IP_INTERFACE = "UPDATE ipinterface SET nodeID=? WHERE nodeID=? AND isManaged!='D'";

    final static String SQL_DB_REPARENT_SNMP_INTERFACE = "UPDATE snmpinterface SET nodeID=? WHERE nodeID=?";

    final static String SQL_DB_REPARENT_IF_SERVICES = "UPDATE ifservices SET nodeID=? WHERE nodeID=? AND status!='D'";

    final static String SQL_DB_DELETE_NODE = "UPDATE node SET nodeType='D' WHERE nodeID=?";

    /**
     * Database connection
     */
    private java.sql.Connection m_connection;

    /**
     * List of LightWeightNodeEntry objects intialized from the content of the
     * 'node' table.
     */
    private List<LightWeightNodeEntry> m_existingNodeList;

    /**
     * Contains a mapping of reparent nodes and the list of interfaces which
     * were reparented under them.
     */
    private Map<LightWeightNodeEntry, List<LightWeightIfEntry>> m_reparentedIfMap;

    /**
     * Contains of mapping of reparent nodes and the list of duplicate nodes
     * associated with them.
     */
    private Map<LightWeightNodeEntry, List<LightWeightNodeEntry>> m_reparentNodeMap;

    /**
     * Contains hard-coded list of NetBIOS names which are not subject to
     * reparenting via SMB.
     */
    private static List<String> m_netbiosNamesToSkip;

    //
    // Static initialization block to initialize list of NetBIOS names
    // which should not be considered for reparenting
    //
    static {
        m_netbiosNamesToSkip = new ArrayList<String>(4);
        m_netbiosNamesToSkip.add("WORKSTATION");
        m_netbiosNamesToSkip.add("DEFAULT");
        m_netbiosNamesToSkip.add("OEMCOMPUTER");
        m_netbiosNamesToSkip.add("COMPUTER");
    }

    /**
     * <P>
     * LightWeightIfEntry is designed to hold specific information about an IP
     * interface in the database such as its IP address, its parent node id, and
     * its managed status and represents a lighter weight version of the
     * DbIpInterfaceEntry class.
     * </P>
     */
    private static final class LightWeightIfEntry {
        private String m_address;

        private String m_hostname;

        private int m_nodeId;

        private int m_oldNodeId;

        /**
         * <P>
         * Constructs a new LightWeightIfEntry object.
         * </P>
         * 
         * @param address
         *            Interface's ip address
         * @param hostname
         *            Interface's ip host name
         * @param nodeId
         *            Interface's parent node id
         * @param oldNodeId
         *            Interface's original parent node id
         */
        public LightWeightIfEntry(String address, String hostname, int nodeId, int oldNodeId) {
            m_address = address;
            m_hostname = hostname;
            m_nodeId = nodeId;
            m_oldNodeId = oldNodeId;
        }

        /**
         * <P>
         * Returns the IP address of the interface.
         * </P>
         */
        public String getAddress() {
            return m_address;
        }

        /**
         * <P>
         * Returns the IP hostname of the interface.
         * </P>
         */
        public String getHostName() {
            return m_hostname;
        }

        /**
         * <P>
         * Returns the parent node id of the interface.
         * </P>
         */
        public int getParentNodeId() {
            return m_nodeId;
        }

        /**
         * <P>
         * Returns the old parent node id of the interface.
         * </P>
         */
        public int getOldParentNodeId() {
            return m_oldNodeId;
        }
    }

    /**
     * This class is a lighter weight version of the DbNodeEntry class for use
     * in SMB reparenting.
     */
    private static final class LightWeightNodeEntry {
        private int m_nodeId;

        private String m_netbiosName;

        private boolean m_duplicate;

        private DbNodeEntry m_hwNodeEntry;

        /**
         * <P>
         * Constructs a new LightWeightNodeEntry object.
         * </P>
         * 
         * @param nodeID
         *            Node's identifier
         * @param netbiosName
         *            Node's NetBIOS name
         */
        LightWeightNodeEntry(int nodeID, String netbiosName) {
            m_nodeId = nodeID;
            if (netbiosName != null)
                m_netbiosName = netbiosName.toUpperCase();
            else
                m_netbiosName = null;
            m_duplicate = false;
            m_hwNodeEntry = null;
        }

        /**
         * <P>
         * Returns the node identifer.
         * </P>
         */
        int getNodeId() {
            return m_nodeId;
        }

        /**
         * <P>
         * Returns the NetBIOS name of the node.
         * </P>
         */
        String getNetbiosName() {
            return m_netbiosName;
        }

        /**
         * <P>
         * Sets the duplicate flag for the node..
         * </P>
         * 
         * @param dupFlag
         *            the state for the duplicate flag
         */
        void setDuplicate(boolean dupFlag) {
            m_duplicate = dupFlag;
        }

        /**
         * <P>
         * Returns true if this LightWeightNodeEntry object has been marked as a
         * duplicate, false otherwise.
         * </P>
         */
        boolean isDuplicate() {
            return m_duplicate;
        }

        /**
         * 
         */
        void setHeavyWeightNodeEntry(DbNodeEntry hwNodeEntry) {
            m_hwNodeEntry = hwNodeEntry;
        }

        /**
         * 
         */
        DbNodeEntry getHeavyWeightNodeEntry() {
            return m_hwNodeEntry;
        }

        /**
         * 
         */
        boolean hasHeavyWeightNodeEntry() {
            if (m_hwNodeEntry == null)
                return false;
            else
                return true;
        }

        /**
         * <P>
         * Node equality test...currently returns true if the
         * LightWeightNodeEntry objects have the same NetBIOS name.
         * </P>
         * 
         * @return true if this and the passed object are equivalent.
         */
        @Override
        public boolean equals(final Object o) {
            if (o == null) return false;
            if (!(o instanceof LightWeightNodeEntry)) return false;
            LightWeightNodeEntry node = (LightWeightNodeEntry) o;

            if (m_netbiosName == null || node.getNetbiosName() == null)
                return false;
            else if (node.getNetbiosName().equals(m_netbiosName))
                return true;
            else
                return false;
        }
        
        @Override
        public int hashCode() {
            return new HashCodeBuilder(7, 23)
                .append(m_nodeId)
                .append(m_netbiosName)
                .append(m_duplicate)
                .append(m_hwNodeEntry)
                .toHashCode();
        }
    }

    /**
     * Class constructor.
     *
     * @param connection
     *            Database connection
     */
    public ReparentViaSmb(java.sql.Connection connection) {
        m_connection = connection;

        m_existingNodeList = null;
        m_reparentedIfMap = null;
        m_reparentNodeMap = null;
    }

    /**
     * This method is responsible for building a list of existing nodes from the
     * 'node' table and then processing that list of nodes in order to determine
     * if there are any nodes which must be reparented because they share the
     * same NetBIOS name with another node. During this processing the reparent
     * node map is built which contains a mapping of reparent nodes to their
     * duplicate node lists.
     * 
     * @throws SQLException
     *             if an error occurs querying the database.
     */
    private void buildNodeLists() throws SQLException {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        m_existingNodeList = new ArrayList<LightWeightNodeEntry>();
        final DBUtils d = new DBUtils(getClass());

        try {
            PreparedStatement stmt = m_connection.prepareStatement(SQL_DB_RETRIEVE_NODES);
            d.watch(stmt);
            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            // Process result set
            // Build list of LightWeightNodeEntry objects representing each of
            // the
            // nodes pulled from the 'node' table
            while (rs.next()) {
                m_existingNodeList.add(new LightWeightNodeEntry(rs.getInt(1), rs.getString(2)));
            }
        } finally {
            d.cleanUp();
        }

        // 
        // Loop through node list and verify that all of the nodes
        // have unique NetBIOS names. If any two nodes have the same
        // NetBIOS name then an entry will be added to the reparenting
        // map.
        //
        // Currently the nodeID with the lowest nodeID will have all
        // the interfaces associated with the other node(s) reparented
        // under it and it's LightWeightNodeEntry object will serve as the map
        // key.
        // Each of the other (duplicate) nodes will be added to a reparent
        // list and then added to the map under the reparent node key.
        //
        Iterator<LightWeightNodeEntry> outer = m_existingNodeList.iterator();

        while (outer.hasNext()) {
            LightWeightNodeEntry outerEntry = outer.next();
            String outerNetbiosName = outerEntry.getNetbiosName();

            // Skip this node if NetBIOS name is null or is in list to skip
            if (outerNetbiosName == null || m_netbiosNamesToSkip.contains(outerNetbiosName))
                continue;

            // If node is already marked as a duplicate just move on
            if (outerEntry.isDuplicate())
                continue;

            List<LightWeightNodeEntry> duplicateNodeList = null;

            Iterator<LightWeightNodeEntry> inner = m_existingNodeList.iterator();
            while (inner.hasNext()) {
                LightWeightNodeEntry innerEntry = inner.next();
                String innerNetbiosName = innerEntry.getNetbiosName();

                // Skip if inner node id is less than or equal to
                // the current outer node id (since these have already
                // been processed as an outer node).
                if (innerEntry.getNodeId() <= outerEntry.getNodeId())
                    continue;

                // Skip this node if NetBIOS name is null or is in list to skip
                if (innerNetbiosName == null || m_netbiosNamesToSkip.contains(innerNetbiosName))
                    continue;

                // Skip if current node is already marked as a duplicate
                if (innerEntry.isDuplicate())
                    continue;

                if (innerNetbiosName.equals(outerNetbiosName)) {
                    // We've found two nodes with same NetBIOS name
                    // Add innerEntry to duplicate node list
                    if (duplicateNodeList == null)
                        duplicateNodeList = new ArrayList<LightWeightNodeEntry>();

                    innerEntry.setDuplicate(true); // mark node as duplicate
                    duplicateNodeList.add(innerEntry); // add to current dup
                                                        // list
                    if (log.isDebugEnabled())
                        log.debug("ReparentViaSmb.retrieveNodeData: found that nodeid " + innerEntry.getNodeId() + " is a duplicate of nodeid " + outerEntry.getNodeId());
                }
            } // end inner while()

            // Anything need reparenting?
            if (duplicateNodeList != null) {
                // We found duplicates...add to reparent map
                if (m_reparentNodeMap == null)
                    m_reparentNodeMap = new HashMap<LightWeightNodeEntry, List<LightWeightNodeEntry>>();

                if (log.isDebugEnabled())
                    log.debug("ReparentViaSmb.retrieveNodeData: adding dup list w/ " + duplicateNodeList.size() + " to reparent Map for reparent nodeid " + outerEntry.getNodeId());
                m_reparentNodeMap.put(outerEntry, duplicateNodeList);
            }
        }// end outer while()
    }

    /**
     * Performs reparenting if necessary and generates appropriate events to
     * inform other OpenNMS processes of any database changes..
     *
     * @throws java.sql.SQLException
     *             if error occurs updating the database
     */
    public void sync() throws SQLException {
        // Build node lists
        buildNodeLists();

        // Reparent interfaces if necessary
        if (m_reparentNodeMap != null && !m_reparentNodeMap.isEmpty()) {
            reparentInterfaces();

            // Generate 'interfaceReparented' events if necessary
            if (m_reparentedIfMap != null && !m_reparentedIfMap.isEmpty())
                generateEvents();
        }
    }

    /**
     * This method is responsible for reparenting interfaces belonging to
     * duplicate nodes under the appropriate reparent node id. During this
     * processing the reparented interface map is generated. This map contains a
     * list of reparented interfaces associated with each reparent node. This
     * list will make it possible to generate 'interfaceReparented' events for
     * each reparented interface.
     * 
     * During reparenting the 'ipInterface', 'snmpInterface', and 'ifServices'
     * tables are all updated to reflect the new parent node id for the
     * reparented interface.
     * 
     * @throws SQLException
     *             if error occurs updating the database
     */
    private void reparentInterfaces() throws SQLException {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        List<LightWeightIfEntry> reparentedIfList = null;
        m_reparentedIfMap = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            PreparedStatement ipInterfaceStmt = m_connection.prepareStatement(SQL_DB_REPARENT_IP_INTERFACE);
            d.watch(ipInterfaceStmt);
            PreparedStatement snmpInterfaceStmt = m_connection.prepareStatement(SQL_DB_REPARENT_SNMP_INTERFACE);
            d.watch(snmpInterfaceStmt);
            PreparedStatement ifServicesStmt = m_connection.prepareStatement(SQL_DB_REPARENT_IF_SERVICES);
            d.watch(ifServicesStmt);

            Set<LightWeightNodeEntry> keys = m_reparentNodeMap.keySet();
            Iterator<LightWeightNodeEntry> iter = keys.iterator();

            while (iter.hasNext()) {
                LightWeightNodeEntry reparentNode = iter.next();
                int reparentNodeID = reparentNode.getNodeId();

                // Now construct a "heavier weight" DbNodeEntry object for this
                // node...sysName, sysDescription and other fields from the node
                // table will be necessary later when the reparentInterface
                // event is generated.
                reparentNode.setHeavyWeightNodeEntry(DbNodeEntry.get(reparentNodeID));

                // Retrieve duplicate node list for this reparent node key
                List<LightWeightNodeEntry> dupList = m_reparentNodeMap.get(reparentNode);
                log.debug("ReparentViaSmb.retrieveNodeData: duplicate node list retrieved, list size=" + dupList.size());

                Iterator<LightWeightNodeEntry> dupIter = dupList.iterator();
                while (dupIter.hasNext()) {
                    LightWeightNodeEntry dupNode = dupIter.next();
                    int dupNodeID = dupNode.getNodeId();

                    try {
                        if (log.isDebugEnabled())
                            log.debug("reparentInterfaces: reparenting all interfaces/services for nodeID " + dupNodeID + " under reparent nodeID " + reparentNodeID);

                        //
                        // Prior to reparenting the interfaces associated with the
                        // duplicate node retrieve a list of the node's interface
                        // IP addresses and add them to the m_reparentedIfMap. This
                        // list will allow us to generate 'interfaceReparented'
                        // events for each one
                        //
                        PreparedStatement stmt = m_connection.prepareStatement(SQL_DB_RETRIEVE_INTERFACES);
                        d.watch(stmt);
                        stmt.setInt(1, dupNodeID);

                        // Issue database query
                        if (log.isDebugEnabled())
                            log.debug("reparentInterfaces: issuing db query...");
                        ResultSet rs = stmt.executeQuery();
                        d.watch(rs);

                        // Process result set
                        // Build list of LightWeightIfEntry objects representing
                        // each of the
                        // interfaces pulled from the 'ipInterface' table
                        while (rs.next()) {
                            String ifAddress = rs.getString(1);
                            String hostName = rs.getString(2);

                            LightWeightIfEntry lwIfEntry = new LightWeightIfEntry(ifAddress, hostName, reparentNodeID, dupNodeID);

                            if (reparentedIfList == null) {
                                reparentedIfList = new ArrayList<LightWeightIfEntry>();
                            }
                            reparentedIfList.add(lwIfEntry);

                            if (log.isDebugEnabled())
                                log.debug("reparentInterfaces: will reparent " + lwIfEntry.getAddress() + " : oldNodeId: " + lwIfEntry.getOldParentNodeId() + " newNodeId: " + lwIfEntry.getParentNodeId());
                        }


                        // Update the 'ipInterface' table so that all interfaces
                        // associated with the duplicate node are reparented.
                        ipInterfaceStmt.setInt(1, reparentNodeID);
                        ipInterfaceStmt.setInt(2, dupNodeID);

                        // execute and log
                        ipInterfaceStmt.executeUpdate();

                        // Update the 'snmpinterface' table so that all interfaces
                        // associated with the duplicate node are reparented
                        snmpInterfaceStmt.setInt(1, reparentNodeID);
                        snmpInterfaceStmt.setInt(2, dupNodeID);

                        // execute and log
                        snmpInterfaceStmt.executeUpdate();

                        // Update the 'ifservices' table so that all services
                        // associated
                        // with the duplicate node are reparented
                        ifServicesStmt.setInt(1, reparentNodeID);
                        ifServicesStmt.setInt(2, dupNodeID);

                        // execute and log
                        ifServicesStmt.executeUpdate();
                    } catch (SQLException sqlE) {
                        log.error("SQLException while reparenting duplicate node w/ nodeID " + dupNodeID);
                        throw sqlE;
                    }

                    // 
                    // Now that all the interfaces have been reparented...lets
                    // delete this duplicate node from the 'node' table
                    //
                    if (log.isDebugEnabled())
                        log.debug("reparentInterfaces: deleting duplicate node id: " + dupNodeID);
                    PreparedStatement deleteNodeStmt = m_connection.prepareStatement(SQL_DB_DELETE_NODE);
                    d.watch(deleteNodeStmt);
                    deleteNodeStmt.setInt(1, dupNodeID);

                    // execute update
                    deleteNodeStmt.executeUpdate();

                } // end while(dupIter.hasNext())

                // Should have a reparented interface list now...add it to
                // the reparented interface map with the reparent node as the key
                if (reparentedIfList != null && !reparentedIfList.isEmpty()) {
                    if (m_reparentedIfMap == null) {
                        m_reparentedIfMap = new HashMap<LightWeightNodeEntry, List<LightWeightIfEntry>>();
                    }

                    m_reparentedIfMap.put(reparentNode, reparentedIfList);
                }
            } // end while(iter.hasNext())
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Generates appropriate events to inform other OpenNMS processes of the
     * database changes. Loops through the keys of the reparent interface and
     * generates 'interfaceReparented' events for each reparented interface.
     */
    private void generateEvents() {
        //
        // iterate through the reparent interface list
        //
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("generateEvents:  Generating reparent events...reparentedIfMap size: " + m_reparentedIfMap.size());

        Set<LightWeightNodeEntry> keys = m_reparentedIfMap.keySet();
        Iterator<LightWeightNodeEntry> iter = keys.iterator();

        while (iter.hasNext()) {
            // Get reparent node object
            LightWeightNodeEntry reparentNode = iter.next();
            if (!reparentNode.hasHeavyWeightNodeEntry()) {
                log.warn("generateEvents:  No valid reparent node entry for node " + reparentNode.getNodeId() + ". Unable to generate reparenting events.");
                continue;
            }

            if (log.isDebugEnabled())
                log.debug("generateEvents: generating events for reparent node w/ id/netbiosName: " + reparentNode.getNodeId() + "/" + reparentNode.getNetbiosName());

            // Get list of interface objects associated with this reparent node
            List<LightWeightIfEntry> ifList = m_reparentedIfMap.get(reparentNode);
            if (ifList != null && !ifList.isEmpty()) {
                Iterator<LightWeightIfEntry> ifIter = ifList.iterator();

                while (ifIter.hasNext()) {
                    LightWeightIfEntry lwIfEntry = ifIter.next();

                    // Generate interfaceReparented event
                    sendInterfaceReparentedEvent(lwIfEntry.getAddress(), lwIfEntry.getHostName(), lwIfEntry.getParentNodeId(), lwIfEntry.getOldParentNodeId(), reparentNode.getHeavyWeightNodeEntry());

                    if (log.isDebugEnabled())
                        log.debug("generateEvents: sent interfaceReparented event for interface " + lwIfEntry.getAddress());
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("generateEvents: completed all event generation...");
    }

    /**
     * This method is responsible for generating a interfaceReparented event and
     * sending it to Eventd.
     * 
     * @param ipAddr
     *            IP address of interface which was reparented
     * @param ipHostName
     *            IP Host Name for the interface
     * @param newNodeId
     *            Interface's new nodeID
     * @param oldNodeId
     *            Interface's old nodeID
     * @param reparentNodeEntry
     *            DbNodeEntry object with all info associated with the reparent
     *            node
     */
    private synchronized void sendInterfaceReparentedEvent(String ipAddr, String ipHostName, int newNodeId, int oldNodeId, DbNodeEntry reparentNodeEntry) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());
        if (log.isDebugEnabled())
            log.debug("sendInterfaceReparentedEvent: ipAddr: " + ipAddr + " ipHostName: " + ipHostName + " newNodeId: " + newNodeId + " oldNodeId: " + oldNodeId);

        // Make sure host name not null
        if (ipHostName == null)
            ipHostName = "";

        // create the event to be sent
        EventBuilder bldr = new EventBuilder(EventConstants.INTERFACE_REPARENTED_EVENT_UEI, "OpenNMS.Capsd");

        bldr.setNodeid(newNodeId);
        bldr.setHost(Capsd.getLocalHostAddress());
        bldr.setInterface(addr(ipAddr));
        
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ipHostName);
        bldr.addParam(EventConstants.PARM_OLD_NODEID, oldNodeId);
        bldr.addParam(EventConstants.PARM_NEW_NODEID, newNodeId);
        bldr.addParam(EventConstants.PARM_NODE_LABEL, reparentNodeEntry.getLabel());
        bldr.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, reparentNodeEntry.getLabelSource());
        
        if (reparentNodeEntry.getSystemName() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSNAME, reparentNodeEntry.getSystemName());
        }

        if (reparentNodeEntry.getSystemDescription() != null) {
            bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, reparentNodeEntry.getSystemDescription());
        }

        // Send event to Eventd
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(bldr.getEvent());

        } catch (Throwable t) {
            log.warn("run: unexpected throwable exception caught during send to middleware", t);
        }
    }
}
