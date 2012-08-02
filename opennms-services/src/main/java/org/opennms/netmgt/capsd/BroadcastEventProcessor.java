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

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.model.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.model.capsd.DbNodeEntry;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>BroadcastEventProcessor class.</p>
 *
 * @author <a href="mailto:matt@opennms.org">Matt Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
@EventListener(name="Capsd:BroadcastEventProcessor")
public class BroadcastEventProcessor implements InitializingBean {

    /**
     * SQL statement used to add an interface/server mapping into the database;
     */
    private static String SQL_ADD_INTERFACE_TO_SERVER = "INSERT INTO serverMap VALUES (?, ?)";

    /**
     * SQL statement used to add an interface/service mapping into the database.
     */
    private static String SQL_ADD_SERVICE_TO_MAPPING = "INSERT INTO serviceMap VALUES (?, ?)";

    /**
     * SQL statement used to delete all services mapping to a specified
     * interface from the database.
     */
    private static String SQL_DELETE_ALL_SERVICES_INTERFACE_MAPPING = "DELETE FROM serviceMap WHERE ipaddr = ?";

    /**
     * SQL statement used to delete an interface/server mapping from the
     * database.
     */
    private static String SQL_DELETE_INTERFACE_ON_SERVER = "DELETE FROM serverMap WHERE ipaddr = ? AND servername = ?";

    /**
     * SQL statement used to delete an interface/service mapping from the
     * database.
     */
    private static String SQL_DELETE_SERVICE_INTERFACE_MAPPING = "DELETE FROM serviceMap WHERE ipaddr = ? AND servicemapname = ?";

    /**
     * SQL statement used to verify if an ipinterface with the specified ip
     * address exists in the database and retrieve the nodeid if exists.
     */
    private static String SQL_QUERY_IPADDRESS_EXIST = "SELECT nodeid FROM ipinterface WHERE ipaddr = ? AND isManaged !='D'";

    /**
     * SQL statement used to query the 'node' and 'ipinterface' tables to verify
     * if a specified ipaddr and node label have already exist in the database.
     */
    private static String SQL_QUERY_IPINTERFACE_EXIST = "SELECT nodelabel, ipaddr FROM node, ipinterface WHERE node.nodeid = ipinterface.nodeid AND node.nodelabel = ? AND ipinterface.ipaddr = ? AND isManaged !='D' AND nodeType !='D'";

    /**
     * SQL statement used to query if a node with the specified nodelabel exist
     * in the database, and the nodeid from the database if exists.
     */
    private static String SQL_QUERY_NODE_EXIST = "SELECT nodeid, dpname FROM node WHERE nodelabel = ? AND nodeType !='D'";

    /**
     * SQL statement used to verify if an ifservice with the specified ip
     * address and service name exists in the database.
     */
    private static String SQL_QUERY_SERVICE_EXIST = "SELECT nodeid FROM ifservices, service WHERE ifservices.serviceid = service.serviceid AND ipaddr = ? AND servicename = ? AND status !='D'";

    /**
     * SQL statement used to query if an interface/service mapping already
     * exists in the database.
     */
    private static String SQL_QUERY_SERVICE_MAPPING_EXIST = "SELECT * FROM serviceMap WHERE ipaddr = ? AND servicemapname = ?";

    /**
     * SQL query to retrieve nodeid of a particulary interface address
     */
    private static String SQL_RETRIEVE_NODEID = "select nodeid from ipinterface where ipaddr=? and isManaged!='D'";

    /**
     * SQL statement used to retrieve the serviced id from the database with a
     * specified service name.
     */
    private static String SQL_RETRIEVE_SERVICE_ID = "SELECT serviceid FROM service WHERE servicename = ?";

    /**
     * Determines if deletePropagation is enabled in the Outage Manager.
     *
     * @return true if deletePropagation is enable, false otherwise
     */
    public static boolean isPropagationEnabled() {
        return CapsdConfigFactory.getInstance().getDeletePropagationEnabled();
    }

    /**
     * Convenience method checking Capsd's config for status of XmlRpc API
     *
     * @return Returns the xmlrpc.
     */
    public static boolean isXmlRpcEnabled() {
        return CapsdConfigFactory.getInstance().getXmlrpc().equals("true");
    }

    /**
     * local openNMS server name
     */
    private String m_localServer = null;

    /**
     * The Capsd rescan scheduler
     */
    private Scheduler m_scheduler;

    /**
     * The location where suspectInterface events are enqueued for processing.
     */
    private ExecutorService m_suspectQ;
    
    private SuspectEventProcessorFactory m_suspectEventProcessorFactory;

    /**
     * Counts the number of interfaces on the node other than a given interface
     * 
     * @param dbConn
     *            the database connection
     * @param nodeid
     *            the node to check interfaces for
     * @param ipAddr
     *            the interface not to include in the count
     * @return the numer of interfaces other than the given one
     * @throws SQLException
     *             if an error occurs talking to the database
     */
    private int countOtherInterfacesOnNode(Connection dbConn, long nodeId, String ipAddr) throws SQLException {
        final String DB_COUNT_OTHER_INTERFACES_ON_NODE = "SELECT count(*) FROM ipinterface WHERE nodeID=? and ipAddr != ? and isManaged != 'D'";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = dbConn.prepareStatement(DB_COUNT_OTHER_INTERFACES_ON_NODE);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            rs = stmt.executeQuery();
            d.watch(rs);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (log().isDebugEnabled())
                log().debug("countServicesForInterface: count services for interface " + nodeId + "/" + ipAddr + ": found " + count);

            return count;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Counts the number of other non deleted services associated with the
     * interface defined by nodeid/ipAddr
     * 
     * @param dbConn
     *            the database connection
     * @param nodeId
     *            the node to chck
     * @param ipAddr
     *            the interface to check
     * @param service
     *            the name of the service not to include
     * @return the number of non deleted services, other than serviceId
     */
    private int countOtherServicesOnInterface(Connection dbConn, long nodeId, String ipAddr, String service) throws SQLException {

        final String DB_COUNT_OTHER_SERVICES_ON_IFACE = "SELECT count(*) FROM ifservices, service " + "WHERE ifservices.serviceId = service.serviceId AND ifservices.status != 'D' " + "AND ifservices.nodeID=? AND ifservices.ipAddr=? AND service.servicename != ?";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        
        try {
            stmt = dbConn.prepareStatement(DB_COUNT_OTHER_SERVICES_ON_IFACE);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setString(3, service);
            rs = stmt.executeQuery();
            d.watch(rs);
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (log().isDebugEnabled())
                log().debug("countServicesForInterface: count services for interface " + nodeId + "/" + ipAddr + ": found " + count);

            return count;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Counts the number of non deleted services on a node on interfaces other
     * than a given interface
     * 
     * @param dbConn
     *            the database connection
     * @param nodeId
     *            the nodeid to check
     * @param ipAddr
     *            the address of the interface not to include
     * @return the number of non deleted services on other interfaces
     */
    private int countServicesOnOtherInterfaces(Connection dbConn, long nodeId, String ipAddr) throws SQLException {
        final String DB_COUNT_SERVICES_ON_OTHER_INTERFACES = "SELECT count(*) FROM ifservices WHERE nodeID=? and ipAddr != ? and status != 'D'";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(DB_COUNT_SERVICES_ON_OTHER_INTERFACES);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            rs = stmt.executeQuery();
            d.watch(rs);

            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            if (log().isDebugEnabled())
                log().debug("countServicesOnOtherInterfaces: count services for node " + nodeId + ": found " + count);

            return count;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Helper method used to create add an interface to a node.
     * 
     * @param dbConn
     * @param nodeLabel
     * @param ipaddr
     * @return a LinkedList of events to be sent
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> createInterfaceOnNode(Connection dbConn, String nodeLabel, String ipaddr) throws SQLException, FailedOperationException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            // There is no ipinterface associated with the specified nodeLabel
            // exist in the database. Verify if a node with the nodeLabel already
            // exist in the database. If not, create a node with the nodeLabel and add it
            // to the database, and also add the ipaddress associated with this node to
            // the database. If the node with the nodeLabel exists in the node
            // table, just add the ip address to the database.
            stmt = dbConn.prepareStatement(SQL_QUERY_NODE_EXIST);
            d.watch(stmt);

            stmt.setString(1, nodeLabel);

            rs = stmt.executeQuery();
            d.watch(rs);
            List<Event> eventsToSend = new LinkedList<Event>();
            while (rs.next()) {

                if (log().isDebugEnabled())
                    log().debug("addInterfaceHandler:  add interface: " + ipaddr + " to the database.");

                // Node already exists. Add the ipaddess to the ipinterface
                // table
                InetAddress ifaddr;
				try {
					ifaddr = InetAddressUtils.addr(ipaddr);
				} catch (final IllegalArgumentException e) {
					throw new FailedOperationException("unable to resolve host " + ipaddr + ": " + e.getMessage(), e);
				}
                int nodeId = rs.getInt(1);
                String dpName = rs.getString(2);

                DbIpInterfaceEntry ipInterface = DbIpInterfaceEntry.create(nodeId, ifaddr);
                ipInterface.setHostname(ifaddr.getHostName());
                ipInterface.setManagedState(DbIpInterfaceEntry.STATE_MANAGED);
                ipInterface.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
                ipInterface.store(dbConn);

                // create a nodeEntry
                DbNodeEntry nodeEntry = DbNodeEntry.get(nodeId, dpName);
                Event newEvent = EventUtils.createNodeGainedInterfaceEvent(nodeEntry, ifaddr);
                eventsToSend.add(newEvent);

            }
            return eventsToSend;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * This method add a node with the specified node label to the database. If
     * also adds in interface with the given ipaddress to the node, if the
     * ipaddr is not null
     * 
     * @param conn
     *            The JDBC Database connection.
     * @param nodeLabel
     *            the node label to identify the node to create.
     * @param ipaddr
     *            the ipaddress to be added into the ipinterface table.
     * @throws SQLException
     *             if a database error occurs
     * @throws FailedOperationException
     *             if the ipaddr is not resolvable
     */
    private List<Event> createNodeWithInterface(Connection conn, String nodeLabel, String ipaddr) throws SQLException, FailedOperationException {
        if (nodeLabel == null)
            return Collections.emptyList();

        if (log().isDebugEnabled())
            log().debug("addNode:  Add a node " + nodeLabel + " to the database");

        List<Event> eventsToSend = new LinkedList<Event>();
        DbNodeEntry node = DbNodeEntry.create();
        Date now = new Date();
        node.setCreationTime(now);
        node.setNodeType(DbNodeEntry.NODE_TYPE_ACTIVE);
        node.setLabel(nodeLabel);
        node.setLabelSource(DbNodeEntry.LABEL_SOURCE_USER);
        node.store(conn);

        Event newEvent = EventUtils.createNodeAddedEvent(node);
        eventsToSend.add(newEvent);

        if (ipaddr != null)
            if (log().isDebugEnabled())
                log().debug("addNode:  Add an IP Address " + ipaddr + " to the database");

            // add the ipaddess to the database
            InetAddress ifaddress;
			try {
				ifaddress = InetAddressUtils.addr(ipaddr);
			} catch (final IllegalArgumentException e) {
				throw new FailedOperationException("unable to resolve host " + ipaddr + ": " + e.getMessage(), e);
			}
            DbIpInterfaceEntry ipInterface = DbIpInterfaceEntry.create(node.getNodeId(), ifaddress);
            ipInterface.setHostname(ifaddress.getHostName());
            ipInterface.setManagedState(DbIpInterfaceEntry.STATE_MANAGED);
            ipInterface.setPrimaryState(DbIpInterfaceEntry.SNMP_NOT_ELIGIBLE);
            ipInterface.store(conn);

            Event gainIfEvent = EventUtils.createNodeGainedInterfaceEvent(node, ifaddress);
            eventsToSend.add(gainIfEvent);
        return eventsToSend;
    }

    /**
     * Helper method to add an interface to a node.
     * 
     * @param dbConn
     * @param nodeLabel
     * @param ipaddr
     * @return eventsToSend
     *          A List Object containing events to be sent
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> doAddInterface(Connection dbConn, String nodeLabel, String ipaddr) throws SQLException, FailedOperationException {
        List<Event> eventsToSend;
        if (interfaceExists(dbConn, nodeLabel, ipaddr)) {
            if (log().isDebugEnabled()) {
                log().debug("addInterfaceHandler: node " + nodeLabel + " with IPAddress " + ipaddr + " already exist in the database.");
            }
            eventsToSend = Collections.emptyList();
        }

        else if (nodeExists(dbConn, nodeLabel)) {
            eventsToSend = createInterfaceOnNode(dbConn, nodeLabel, ipaddr);
        } else {
            // The node does not exist in the database, add the node and
            // the ipinterface into the database.
            eventsToSend = createNodeWithInterface(dbConn, nodeLabel, ipaddr);
        }
        return eventsToSend;
    }

    /**
     * Perform the buld of the work for processing an addNode event
     * 
     * @param dbConn
     *            the database connection
     * @param nodeLabel
     *            the label for the node to add
     * @param ipaddr
     *            an interface on the node (may be null if no interface is
     *            supplied)
     * @return a list of events that need to be sent in response to these
     *         changes
     * @throws SQLException
     *             if a database error occurrs
     * @throws FailedOperationException
     *             if other errors occur
     */
    private List<Event> doAddNode(Connection dbConn, String nodeLabel, String ipaddr) throws SQLException, FailedOperationException {
        List<Event> eventsToSend;
        if (!nodeExists(dbConn, nodeLabel)) {
            // the node does not exist in the database. Add the node with the
            // specified
            // node label and add the ipaddress to the database.
            eventsToSend = createNodeWithInterface(dbConn, nodeLabel, ipaddr);
        } else {
            eventsToSend = Collections.emptyList();
            if (log().isDebugEnabled()) {
                log().debug("doAddNode: node " + nodeLabel + " with IPAddress " + ipaddr + " already exist in the database.");
            }
        }
        return eventsToSend;
    }

    /**
     * Helper method used to update the mapping of interfaces to services.
     * 
     * @param dbConn
     * @param ipaddr
     * @param serviceName
     * @param action
     * @param txNo
     * @return a list of events that need to be sent in response to these
     *         changes
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> doAddServiceMapping(Connection dbConn, String ipaddr, String serviceName, long txNo) throws SQLException, FailedOperationException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_ADD_SERVICE_TO_MAPPING);
            d.watch(stmt);

            stmt.setString(1, ipaddr);
            stmt.setString(2, serviceName);
            stmt.executeUpdate();

            if (log().isDebugEnabled()) {
                log().debug("updateServiceHandler: add service " + serviceName + " to interface: " + ipaddr);
            }

            return doChangeService(dbConn, ipaddr, serviceName, "ADD", txNo);
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Helper method used to add a service to an interface.
     * 
     * @param dbConn
     * @param sourceUei
     * @param ipaddr
     * @param serviceName
     * @param serviceId
     * @param txNo
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> doAddServiceToInterface(Connection dbConn, String ipaddr, String serviceName, int serviceId, long txNo) throws SQLException, FailedOperationException {

        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_QUERY_IPADDRESS_EXIST);
            d.watch(stmt);
            stmt.setString(1, ipaddr);
            rs = stmt.executeQuery();
            d.watch(rs);
            
            List<Event> eventsToSend = new LinkedList<Event>();
            while (rs.next()) {
                if (log().isDebugEnabled()) {
                    log().debug("changeServiceHandler: add service " + serviceName + " to interface: " + ipaddr);
                }

                InetAddress inetAddr;
				try {
					inetAddr = InetAddressUtils.addr(ipaddr);
				} catch (final IllegalArgumentException e) {
					throw new FailedOperationException("unable to resolve host " + ipaddr + ": " + e.getMessage(), e);
				}
                final int nodeId = rs.getInt(1);
                // insert service
				DbIfServiceEntry service = DbIfServiceEntry.create(nodeId, inetAddr, serviceId);
                service.setSource(DbIfServiceEntry.SOURCE_PLUGIN);
                service.setStatus(DbIfServiceEntry.STATUS_ACTIVE);
                service.setNotify(DbIfServiceEntry.NOTIFY_ON);
                service.store(dbConn, true);

                // Create a nodeGainedService event to eventd.
                DbNodeEntry nodeEntry = DbNodeEntry.get(nodeId);
                Event newEvent = EventUtils.createNodeGainedServiceEvent(nodeEntry, inetAddr, serviceName, txNo);
                eventsToSend.add(newEvent);
            }
            return eventsToSend;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Helper method used to change the state of a service for an interface.  Currently, add or delete.
     * 
     * @param dbConn
     * @param sourceUei
     * @param ipaddr
     * @param serviceName
     * @param action
     * @param txNo
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> doChangeService(Connection dbConn, String ipaddr, String serviceName, String action, long txNo) throws SQLException, FailedOperationException {
        List<Event> eventsToSend = null;
        int serviceId = verifyServiceExists(dbConn, serviceName);

        if (action.equalsIgnoreCase("DELETE")) {
            eventsToSend = new LinkedList<Event>();
            // find the node Id associated with the serviceName and interface
            int[] nodeIds = findNodeIdForServiceAndInterface(dbConn, ipaddr, serviceName);
            for (int i = 0; i < nodeIds.length; i++) {
                int nodeId = nodeIds[i];
                // delete the service from the database
                eventsToSend.addAll(doDeleteService(dbConn, "OpenNMS.Capsd", nodeId, ipaddr, serviceName, txNo));
            }
        } else if (action.equalsIgnoreCase("ADD")) {
            eventsToSend = doAddServiceToInterface(dbConn, ipaddr, serviceName, serviceId, txNo);
        } else {
            eventsToSend = Collections.emptyList();
        }
        return eventsToSend;
    }

    /**
     * Helper method used to create mapping of interface to service.
     * 
     * @param dbConn
     * @param nodeLabel
     * @param ipaddr
     * @param hostName
     * @param txNo
     * @return Collections.singletonList()
     *         A List containing event(s) to send.
     * @throws SQLException
     */
    private List<Event> doCreateInterfaceMappings(Connection dbConn, String nodeLabel, String ipaddr, String hostName, long txNo) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_ADD_INTERFACE_TO_SERVER);
            d.watch(stmt);

            stmt.setString(1, ipaddr);
            stmt.setString(2, hostName);
            stmt.executeUpdate();

            if (log().isDebugEnabled()) {
                log().debug("updateServerHandler: added interface " + ipaddr + " into NMS server: " + hostName);
            }

            // Create a addInterface event and process it.
            // FIXME: do I need to make a direct call here?
            Event newEvent = EventUtils.createAddInterfaceEvent("OpenNMS.Capsd", nodeLabel, ipaddr, hostName, txNo);
            return Collections.singletonList(newEvent);
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Mark as deleted the specified interface and its associated services, if
     * delete propagation is enable and the interface is the only one on the
     * node, delete the node as well.
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source for any events that must be sent
     * @param nodeid
     *            the id of the node the interface resides on
     * @param ipAddr
     *            the ip address of the interface to be deleted
     * @param txNo
     *            a transaction number to associate with the deletion
     * @return a list of events that need to be sent w.r.t. this deletion
     * @throws SQLException
     *             if any database errors occur
     */
  
    private List<Event> doDeleteInterface(Connection dbConn, String source, long nodeid, String ipAddr, long txNo) throws SQLException {
        return doDeleteInterface( dbConn, source, nodeid, ipAddr, -1, txNo);
    }
    
    /**
     * Mark as deleted the specified interface and its associated services, and/or
     * also the snmpinterface, if it exists. If delete propagation is enabled and
     * the interface is the only one on the node, delete the node as well.
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source for any events that must be sent
     * @param nodeid
     *            the id of the node the interface resides on
     * @param ipAddr
     *            the ip address of the interface to be deleted
     * @param ifIndex
     *             the ifIndex of the interface to be deleted
     * @param txNo
     *            a transaction number to associate with the deletion
     * @return a list of events that need to be sent w.r.t. this deletion
     * @throws SQLException
     *             if any database errors occur
     */
    private List<Event> doDeleteInterface(Connection dbConn, String source, long nodeid, String ipAddr, int ifIndex, long txNo) throws SQLException {
        List<Event> eventsToSend = new LinkedList<Event>();

        // if this is the last ip interface for the node then delete the node
        // instead
        if (!EventUtils.isNonIpInterface(ipAddr) && isPropagationEnabled() && countOtherInterfacesOnNode(dbConn, nodeid, ipAddr) == 0) {
            // there are no other ifs for this node so delete the node
            eventsToSend = doDeleteNode(dbConn, source, nodeid, txNo);
        } else {
            if (!EventUtils.isNonIpInterface(ipAddr)) {
                eventsToSend.addAll(markAllServicesForInterfaceDeleted(dbConn, source, nodeid, ipAddr, txNo));
            }
            eventsToSend.addAll(markInterfaceDeleted(dbConn, source, nodeid, ipAddr, ifIndex, txNo));
        }
        deleteAlarmsForInterface(dbConn, nodeid, ipAddr);
        if (ifIndex > -1) {
            deleteAlarmsForSnmpInterface(dbConn, nodeid, ifIndex);
        }
        return eventsToSend;
    }

    /**
     * Helper method to remove all mappings of services to @param nodeLabel, @param ipaddr
     * 
     * @param dbConn
     * @param nodeLabel
     * @param ipaddr
     * @param hostName
     * @param txNo
     * @param log
     * @return eventsToSend
     *         a list of events to be sent.
     * @throws SQLException
     */
    private List<Event> doDeleteInterfaceMappings(Connection dbConn, String nodeLabel, String ipaddr, String hostName, long txNo) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            List<Event> eventsToSend = new LinkedList<Event>();

            // Delete all services on the specified interface in
            // interface/service
            // mapping
            //
            if (log().isDebugEnabled()) {
                log().debug("updateServer: delete all services on the interface: " + ipaddr + " in the interface/service mapping.");
            }
            stmt = dbConn.prepareStatement(SQL_DELETE_ALL_SERVICES_INTERFACE_MAPPING);
            d.watch(stmt);
            stmt.setString(1, ipaddr);
            stmt.executeUpdate();

            // Delete the interface on interface/server mapping
            if (log().isDebugEnabled()) {
                log().debug("updateServer: delete interface: " + ipaddr + " on NMS server: " + hostName);
            }
            stmt = dbConn.prepareStatement(SQL_DELETE_INTERFACE_ON_SERVER);
            stmt.setString(1, ipaddr);
            stmt.setString(2, hostName);
            stmt.executeUpdate();

            // Now mark the interface as deleted (and its services as well)
            long[] nodeIds = findNodeIdsForInterfaceAndLabel(dbConn, nodeLabel, ipaddr);
            for (int i = 0; i < nodeIds.length; i++) {
                long nodeId = nodeIds[i];
                eventsToSend.addAll(doDeleteInterface(dbConn, "OpenNMS.Capsd", nodeId, ipaddr, txNo));
            }
            return eventsToSend;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Mark as deleted the specified node, its associated interfaces and
     * services.
     * 
     * @param dbConn
     *            the connection to the database
     * @param source
     *            the source for any events to send
     * @param nodeid
     *            the nodeid to be deleted
     * @param txNo
     *            a transaction id to associate with this deletion
     * 
     * @return the list of events that need to be sent in response to the node
     *         being deleted
     * @throws SQLException
     *             if any exception occurs communicating with the database
     */
    private List<Event> doDeleteNode(Connection dbConn, String source, long nodeid, long txNo) throws SQLException {
        List<Event> eventsToSend = new LinkedList<Event>();
        eventsToSend.addAll(markInterfacesAndServicesDeleted(dbConn, source, nodeid, txNo));
        eventsToSend.addAll(markNodeDeleted(dbConn, source, nodeid, txNo));
        
        //Note: left this call to deleteAlarmsForNode because I wanted to indicate that alarms are now 
        //deleted by the DB with a delete cascade fk constraint on the alarm table
        //when the node is actually deleted.  We have to leave this in here for Capsd because
        //it only flags the node as deleted whereas the provisioner actually deletes the node.
        deleteAlarmsForNode(dbConn, nodeid);
        return eventsToSend;
    }

    private void deleteAlarmsForNode(Connection dbConn, long nodeId) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement("DELETE FROM alarms WHERE nodeid = ?");
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            int count = stmt.executeUpdate();

            log().debug("deleteAlarmsForNode: deleted: "+count+" alarms for node: "+nodeId);

        } finally {
            d.cleanUp();
        }
    }

    private void deleteAlarmsForInterface(Connection dbConn, long nodeId, String ipAddr) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement("DELETE FROM alarms WHERE nodeid = ? AND ipaddr = ?");
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            int count = stmt.executeUpdate();

            log().debug("deleteAlarmsForInterace: deleted: "+count+" alarms for interface: "+ipAddr);

        } finally {
            d.cleanUp();
        }
    }

    /**
     * Delete alarms for the specified snmp interface
     * 
     * @param dbConn
     *            the connection to the database
     * @param nodeid
     *            the nodeid for the interface to be deleted
     * @param ifIndex
     *            the ifIndex for the interface to be deleted
     * @throws SQLException
     *             if any exception occurs communicating with the database
     */
    private void deleteAlarmsForSnmpInterface(Connection dbConn, long nodeId, int ifIndex) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement("DELETE FROM alarms WHERE nodeid = ? AND ifindex = ?");
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setInt(2, ifIndex);
            int count = stmt.executeUpdate();

            if (log().isDebugEnabled()) {
                log().debug("deleteAlarmsForSnmpInterace: deleted: "+count+" alarms for node " + nodeId
                            + "ifIndex: "+ifIndex);
            }

        } finally {
            d.cleanUp();
        }
    }
    
    private void deleteAlarmsForService(Connection dbConn, long nodeId, String ipAddr, String service) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = dbConn.prepareStatement("DELETE FROM alarms " +
                                            "WHERE nodeid = ? " +
                                             " AND ipaddr = ? " +
                                             " AND serviceid " +
                                             "  IN (SELECT serviceid " +
                                             "FROM service " +
                                            "WHERE servicename = ?)");
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setString(3, service);
            int count = stmt.executeUpdate();

            log().debug("deleteAlarmsForService: deleted: "+count+" alarms for service: "+service);

        } finally {
            d.cleanUp();
        }
    }
    
    /**
     * Mark as deleted the specified service, if this is the only service on an
     * interface or node and deletePropagation is enabled, the interface or node
     * is marked as deleted as well.
     * 
     * @param dbConn
     *            the connection to the database
     * @param source
     *            the source for any events to send
     * @param nodeid
     *            the nodeid that the service resides on
     * @param ipAddr
     *            the interface that the service resides on
     * @param service
     *            the name of the service
     * @param txNo
     *            a transaction id to associate with this deletion
     * 
     * @return the list of events that need to be sent in response to the
     *         service being deleted
     * @throws SQLException
     *             if any exception occurs communicating with the database
     */
    private List<Event> doDeleteService(Connection dbConn, String source, long nodeid, String ipAddr, String service, long txNo) throws SQLException {
        List<Event> eventsToSend = new LinkedList<Event>();

        if (isPropagationEnabled()) {
            // if this is the last service for the interface or the last service
            // for the node then send delete events for the interface or node
            // instead
            int otherSvcsOnIfCnt = countOtherServicesOnInterface(dbConn, nodeid, ipAddr, service);
            if (otherSvcsOnIfCnt == 0 && countServicesOnOtherInterfaces(dbConn, nodeid, ipAddr) == 0) {
                // no services on this interface or any other interface on this
                // node so delete
                // node
                log().debug("Propagating service delete to node " + nodeid);
                eventsToSend.addAll(doDeleteNode(dbConn, source, nodeid, txNo));
            } else if (otherSvcsOnIfCnt == 0) {
                // no services on this interface so delete interface
                log().debug("Propagting service delete to interface " + nodeid + "/" + ipAddr);
                eventsToSend.addAll(doDeleteInterface(dbConn, source, nodeid, ipAddr, txNo));
            } else {
                log().debug("No need to Propagate service delete " + nodeid + "/" + ipAddr + "/" + service);
                // otherwise just mark the service as deleted and send a
                // serviceDeleted event
                eventsToSend.addAll(markServiceDeleted(dbConn, source, nodeid, ipAddr, service, txNo));
            }
        } else {
            log().debug("Propagation disabled:  deleting only service " + nodeid + "/" + ipAddr + "/" + service);
            // otherwise just mark the service as deleted and send a
            // serviceDeleted event
            eventsToSend.addAll(markServiceDeleted(dbConn, source, nodeid, ipAddr, service, txNo));
        }
        deleteAlarmsForService(dbConn, nodeid, ipAddr, service);
        return eventsToSend;
    }

    /**
     * Helper method to handle the processes involved after receiving a delete service event.
     * FIXME: see FIXME in javadoc of the doUpdateService method.
     * 
     * @param dbConn
     * @param ipaddr
     * @param serviceName
     * @param action
     * @param txNo
     * @return An object of type List that may contain events that need to be sent.
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> doDeleteServiceMapping(Connection dbConn, String ipaddr, String serviceName, long txNo) throws SQLException, FailedOperationException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            if (log().isDebugEnabled()) {
                log().debug("handleUpdateService: delete service: " + serviceName + " on IPAddress: " + ipaddr);
            }
            stmt = dbConn.prepareStatement(SQL_DELETE_SERVICE_INTERFACE_MAPPING);
            d.watch(stmt);

            stmt.setString(1, ipaddr);
            stmt.setString(2, serviceName);

            stmt.executeUpdate();

            return doChangeService(dbConn, ipaddr, serviceName, "DELETE", txNo);
        } finally {
            d.cleanUp();
        }
    }

    private List<Event> doUpdateServer(Connection dbConn, String nodeLabel, String ipaddr, String action, String hostName, long txNo) throws SQLException, FailedOperationException {

        boolean exists = existsInServerMap(dbConn, hostName, ipaddr);

        //TODO: this logic changed from stable, verify that it should not be backported
        if ("DELETE".equalsIgnoreCase(action)) {
            return doDeleteInterfaceMappings(dbConn, nodeLabel, ipaddr, hostName, txNo);
        } else if ("ADD".equalsIgnoreCase(action)) {
            if (exists)
                throw new FailedOperationException("Could not add interface "+ipaddr+" to NMS server: "+hostName+" because it already exists!");
            else
                return doCreateInterfaceMappings(dbConn, nodeLabel, ipaddr, hostName, txNo);
        } else {
            log().error("updateServerHandler: could not process interface: " + ipaddr + " on NMS server: " + hostName+": action "+action+" unknown");
            throw new FailedOperationException("Undefined operation "+action+" for updateServer event!");
        }
    }

    /**
     * This method adds/updates a service for @param nodeLabel, @param ipaddr, @param serviceName.
     * FIXME: Found inconsistency in the sub-types of List returned.  Need to verify issues with jUnit tests.
     * 
     * @param dbConn
     * @param ipaddr
     * @param serviceName
     * @param action
     * @param txNo
     * @return An object of type List containing events to be sent.
     * @throws SQLException
     * @throws FailedOperationException
     */
    private List<Event> doUpdateService(Connection dbConn, String nodeLabel, String ipaddr, String serviceName, String action, long txNo) throws SQLException, FailedOperationException {
        List<Event> eventsToSend;
        verifyServiceExists(dbConn, serviceName);
        verifyInterfaceExists(dbConn, nodeLabel, ipaddr);

        boolean mapExists = serviceMappingExists(dbConn, ipaddr, serviceName);

        if (mapExists && "DELETE".equalsIgnoreCase(action)) {
            // the mapping exists and should be deleted
            eventsToSend = doDeleteServiceMapping(dbConn, ipaddr, serviceName, txNo);
        } else if (!mapExists && "ADD".equalsIgnoreCase(action)) {
            // we need to add the mapping, it doesn't exist
            eventsToSend = doAddServiceMapping(dbConn, ipaddr, serviceName, txNo);
        } else {
            eventsToSend = Collections.emptyList();
        }
        return eventsToSend;
    }

    /**
     * Helper method to check if a service exists for a host and IP address.
     * 
     * FIXME: Very inconsistent naming in the current "model": nodelabel, hostname, servername, etc.
     * right here this simple method indicates a problem with the model.
     * 
     * @param dbConn
     * @param hostName
     * @param ipaddr
     * @return A logical evaluation of a service existing for hostname and IP address
     * @throws SQLException
     */
    private boolean existsInServerMap(Connection dbConn, String hostName, String ipaddr) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            /**
             * SQL statement used to query if an interface/server mapping
             * already exists in the database.
             */
            final String SQL_QUERY_INTERFACE_ON_SERVER = "SELECT count(*)  FROM serverMap WHERE ipaddr = ? AND servername = ?";

            // Verify if the interface already exists on the NMS server
            stmt = dbConn.prepareStatement(SQL_QUERY_INTERFACE_ON_SERVER);
            d.watch(stmt);

            stmt.setString(1, ipaddr);
            stmt.setString(2, hostName);

            ResultSet rs = stmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                count = rs.getInt(1);
            }

            return count > 0;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Helper method that returns the node id for the @param ipaddr and @param serviceName.
     * FIXME: Notice how some of these methods return node id arrays as int(s) and some as long(s).
     * 
     * @param dbConn
     * @param ipaddr
     * @param serviceName
     * @return An int Array of node ids.
     * @throws SQLException
     */
    private int[] findNodeIdForServiceAndInterface(Connection dbConn, String ipaddr, String serviceName) throws SQLException {
        int[] nodeIds;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        
        try {
            // Verify if the specified service already exist.
            stmt = dbConn.prepareStatement(SQL_QUERY_SERVICE_EXIST);
            d.watch(stmt);

            stmt.setString(1, ipaddr);
            stmt.setString(2, serviceName);

            rs = stmt.executeQuery();
            d.watch(rs);
            List<Integer> nodeIdList = new LinkedList<Integer>();
            while (rs.next()) {
                if (log().isDebugEnabled()) {
                    log().debug("changeService: service " + serviceName + " on IPAddress " + ipaddr + " already exists in the database.");
                }
                int nodeId = rs.getInt(1);
                nodeIdList.add(nodeId);
            }
            nodeIds = new int[nodeIdList.size()];
            int i = 0;
            for(Integer n : nodeIdList) {
                nodeIds[i++] = n.intValue();
            }
            return nodeIds;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Helper method for retrieving list of node ids that match @param nodeLabel and @param ipAddr
     * 
     * @param dbConn
     * @param nodeLabel
     * @param ipAddr
     * @return Array of node ids from the db (JDBC)
     * @throws SQLException
     */
    private long[] findNodeIdsForInterfaceAndLabel(Connection dbConn, String nodeLabel, String ipAddr) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());

        try {
            stmt = dbConn.prepareStatement("SELECT node.nodeid FROM node, ipinterface WHERE node.nodeid = ipinterface.nodeid AND node.nodelabel = ? AND ipinterface.ipaddr = ? AND isManaged !='D' AND nodeType !='D'");
            d.watch(stmt);
            stmt.setString(1, nodeLabel);
            stmt.setString(2, ipAddr);

            rs = stmt.executeQuery();
            d.watch(rs);
            List<Long> nodeIdList = new LinkedList<Long>();
            while (rs.next()) {
                nodeIdList.add(rs.getLong(1));
            }

            long[] nodeIds = new long[nodeIdList.size()];
            int i = 0;
            for(Long nodeId : nodeIdList) {
                nodeIds[i++] = nodeId.longValue();
            }
            return nodeIds;
        } finally {
            d.cleanUp();
        }        
    }

    
    

    /**
     * Return an id for this event listener
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return "Capsd:BroadcastEventProcessor";
    }

    /**
     * Process the event, add the specified interface into database. If the
     * associated node does not exist in the database yet, add a node into the
     * database.
     *
     * @param event
     *            The event to process.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the event is missing essential information
     * @throws org.opennms.netmgt.capsd.FailedOperationException
     *             if the operation fails (because of database error for
     *             example)
     */
    @EventHandler(uei=EventConstants.ADD_INTERFACE_EVENT_UEI)
    public void handleAddInterface(Event event) throws InsufficientInformationException, FailedOperationException {
        EventUtils.checkInterface(event);
        EventUtils.requireParm(event, EventConstants.PARM_NODE_LABEL);
        if (isXmlRpcEnabled())
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);
        if (log().isDebugEnabled())
            log().debug("addInterfaceHandler:  processing addInterface event for " + event.getInterface());

        String nodeLabel = EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL);
        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        // First make sure the specified node label and ipaddress do not exist
        // in the database
        // before trying to add them in.
        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doAddInterface(dbConn, nodeLabel, event.getInterface());
        } catch (SQLException sqlE) {
            log().error("addInterfaceHandler: SQLException during add node and ipaddress to the database.", sqlE);
            throw new FailedOperationException("Database error: " + sqlE.getMessage(), sqlE);
        } finally {
            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleAddInterface: Threw Exception during commit: ", ex);
                    throw new FailedOperationException("Database error: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.setAutoCommit(true); //TODO:verify this
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleAddInterface: Threw Exception during close: ", ex);
                        }
                }
        }
    }

    private Connection getConnection() throws SQLException {
        return DataSourceFactory.getInstance().getConnection();
    }

    /**
     * Process an addNode event.
     *
     * @param event
     *            The event to process.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the event is missing information
     * @throws org.opennms.netmgt.capsd.FailedOperationException
     *             if an error occurs during processing
     */
    @EventHandler(uei=EventConstants.ADD_NODE_EVENT_UEI)
    public void handleAddNode(Event event) throws InsufficientInformationException, FailedOperationException {

        EventUtils.requireParm(event, EventConstants.PARM_NODE_LABEL);
        if (isXmlRpcEnabled()) {
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);
        }

        String ipaddr = event.getInterface();
        String nodeLabel = EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL);
        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);
        log().debug("addNodeHandler:  processing addNode event for " + ipaddr);
        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doAddNode(dbConn, nodeLabel, ipaddr);
        } catch (SQLException sqlE) {
            log().error("addNodeHandler: SQLException during add node and ipaddress to tables", sqlE);
            throw new FailedOperationException("database error: " + sqlE.getMessage(), sqlE);
        } finally {
            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleAddNode: Threw Exception during commit: ", ex);
                    throw new FailedOperationException("database error: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleAddNode: Threw Exception during close: ", ex);
                        }
                }
        }

    }

    /**
     * Process the event, add or remove a specified service from an interface.
     * An 'action' parameter wraped in the event will tell which action to take
     * to the service.
     *
     * @param event
     *            The event to process.
     * @throws org.opennms.netmgt.capsd.FailedOperationException if any.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     */
    @EventHandler(uei=EventConstants.CHANGE_SERVICE_EVENT_UEI)
    public void handleChangeService(Event event) throws InsufficientInformationException, FailedOperationException {
        EventUtils.checkInterface(event);
        EventUtils.checkService(event);
        EventUtils.requireParm(event, EventConstants.PARM_ACTION);
        if (isXmlRpcEnabled()) {
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);
        }

        String action = EventUtils.getParm(event, EventConstants.PARM_ACTION);
        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        log().debug("changeServiceHandler:  processing changeService event on: " + event.getInterface());

        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doChangeService(dbConn, event.getInterface(), event.getService(), action, txNo);
        } catch (SQLException sqlE) {
            log().error("SQLException during changeService on database.", sqlE);
            throw new FailedOperationException("exeption processing changeService: " + sqlE.getMessage(), sqlE);
        } finally {
            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleChangeService: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing changeService: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleChangeService: Exception thrown closing connection: "+ex);
                        }
                }
        }

    }

    /**
     * Handle a deleteInterface Event. Here we process the event by marking all
     * the appropriate data rows as deleted.
     *
     * @param event
     *            The event indicating what interface to delete
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the required information is not part of the event
     * @throws org.opennms.netmgt.capsd.FailedOperationException if any.
     */
    @EventHandler(uei=EventConstants.DELETE_INTERFACE_EVENT_UEI)
    public void handleDeleteInterface(Event event) throws InsufficientInformationException, FailedOperationException {
        // validate event
        EventUtils.checkEventId(event);
        EventUtils.checkInterfaceOrIfIndex(event);
        EventUtils.checkNodeId(event);
        int ifIndex = -1;
        if(event.hasIfIndex()) {
            ifIndex = event.getIfIndex();
        }
        if (isXmlRpcEnabled())
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);

        // log the event
        if (log().isDebugEnabled())
            log().debug("handleDeleteInterface: Event\n" + "uei\t\t" + event.getUei()
                        + "\neventid\t\t" + event.getDbid() + "\nnodeId\t\t" + event.getNodeid()
                        + "\nipaddr\t\t" + (event.getInterface() != null ? event.getInterface() : "N/A" )
                        + "\nifIndex\t\t" + (ifIndex > -1 ? ifIndex : "N/A" )
                        + "\neventtime\t" + (event.getTime() != null ? event.getTime() : "<null>"));

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        // update the database
        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            String source = (event.getSource() == null ? "OpenNMS.Capsd" : event.getSource());
            
            eventsToSend = doDeleteInterface(dbConn, source, event.getNodeid(), event.getInterface(), ifIndex, txNo);

        } catch (SQLException ex) {
            log().error("handleDeleteInterface:  Database error deleting interface on node " + event.getNodeid()
                        + " with ip address " + (event.getInterface() != null ? event.getInterface() : "null")
                        + " and ifIndex "+ (event.hasIfIndex() ? event.getIfIndex() : "null"), ex);
            throw new FailedOperationException("database error: " + ex.getMessage(), ex);
        } finally {
            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleDeleteInterface: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing delete interface: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleDeleteInterface: Exception thrown closing connection: ", ex);
                        }
                }
        }
    }

    /**
     * Handle a deleteNode Event. Here we process the event by marking all the
     * appropriate data rows as deleted.
     *
     * @param event
     *            The event indicating what node to delete
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the required information is not part of the event
     * @throws org.opennms.netmgt.capsd.FailedOperationException if any.
     */
    @EventHandler(uei=EventConstants.DELETE_NODE_EVENT_UEI)
    public void handleDeleteNode(Event event) throws InsufficientInformationException, FailedOperationException {
        // validate event
        EventUtils.checkEventId(event);
        EventUtils.checkNodeId(event);
        if (isXmlRpcEnabled())
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);

        // log the event
        long nodeid = event.getNodeid();
        if (log().isDebugEnabled())
            log().debug("handleDeleteNode: Event\n" + "uei\t\t" + event.getUei() + "\neventid\t\t" + event.getDbid() + "\nnodeId\t\t" + nodeid + "\neventtime\t" + (event.getTime() != null ? event.getTime() : "<null>"));

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        // update the database
        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            String source = (event.getSource() == null ? "OpenNMS.Capsd" : event.getSource());

            eventsToSend = doDeleteNode(dbConn, source, nodeid, txNo);
        } catch (SQLException ex) {
            log().error("handleDeleteService:  Database error deleting service " + event.getService() + " on ipAddr " + event.getInterface() + " for node " + nodeid, ex);
            throw new FailedOperationException("database error: " + ex.getMessage(), ex);

        } finally {

            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleDeleteNode: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing deleteNode: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleDeleteNode: Exception thrown closing connection: ",ex);
                        }
                }
        }
    }

    /**
     * Handle a deleteService Event. Here we process the event by marking all
     * the appropriate data rows as deleted.
     *
     * @param event
     *            The event indicating what service to delete
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the required information is not part of the event
     * @throws org.opennms.netmgt.capsd.FailedOperationException if any.
     */
    @EventHandler(uei=EventConstants.DELETE_SERVICE_EVENT_UEI)
    public void handleDeleteService(Event event) throws InsufficientInformationException, FailedOperationException {

        // validate event
        EventUtils.checkEventId(event);
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);
        EventUtils.checkService(event);

        // log the event
        if (log().isDebugEnabled())
            log().debug("handleDeleteService: Event\nuei\t\t" + event.getUei() + "\neventid\t\t" + event.getDbid() + "\nnodeid\t\t" + event.getNodeid() + "\nipaddr\t\t" + event.getInterface() + "\nservice\t\t" + event.getService() + "\neventtime\t" + (event.getTime() != null ? event.getTime() : "<null>"));

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        // update the database
        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);
            String source = (event.getSource() == null ? "OpenNMS.Capsd" : event.getSource());
            eventsToSend = doDeleteService(dbConn, source, event.getNodeid(), event.getInterface(), event.getService(), txNo);
        } catch (SQLException ex) {
            log().error("handleDeleteService:  Database error deleting service " + event.getService() + " on ipAddr " + event.getInterface() + " for node " + event.getNodeid(), ex);
            throw new FailedOperationException("database error: " + ex.getMessage(), ex);
        } finally {

            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleDeleteService: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing deleteService: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleDeleteService: Exception thrown closing connection: ", ex);
                        }
                }
        }
    }

    /**
     * This helper method removes a deleted node from Capsd's re-scan schedule.  Doesn't remove it
     * from the new suspect scan schedule.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     */
    @EventHandler(uei=EventConstants.DUP_NODE_DELETED_EVENT_UEI)
    public void handleDupNodeDeleted(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        // Remove the deleted node from the scheduler
        m_scheduler.unscheduleNode(event.getNodeid().intValue());
    }

    /**
     * Helper method that takes IP address from the force rescan event,
     * retrieves the nodeid (JDBC) and adds it to the rescan schedule for immediate
     * processing when the next thread is available.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     */
    @EventHandler(uei=EventConstants.FORCE_RESCAN_EVENT_UEI)
    public void handleForceRescan(Event event) throws InsufficientInformationException {
        // If the event has a node identifier use it otherwise
        // will need to use the interface to lookup the node id
        // from the database
    	Long nodeid = -1L;

        if (event.hasNodeid())
            nodeid = event.getNodeid();
        else {
            // Extract interface from the event and use it to
            // lookup the node identifier associated with the
            // interface from the database.
            //

            // ensure the ipaddr is set
            EventUtils.checkInterface(event);

            // Get database connection and retrieve nodeid
            Connection dbc = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            final DBUtils d = new DBUtils(getClass());
            try {
                dbc = getConnection();
                d.watch(dbc);

                // Retrieve node id
                stmt = dbc.prepareStatement(SQL_RETRIEVE_NODEID);
                d.watch(stmt);
                stmt.setString(1, event.getInterface());
                rs = stmt.executeQuery();
                d.watch(rs);
                if (rs.next()) {
                    nodeid = rs.getLong(1);
                }
            } catch (SQLException sqlE) {
                log().error("handleForceRescan: Database error during nodeid retrieval for interface " + event.getInterface(), sqlE);
            } finally {
                d.cleanUp();
            }

        }

        if (nodeid == null || nodeid == -1) {
            log().error("handleForceRescan: Nodeid retrieval for interface " + event.getInterface() + " failed.  Unable to perform rescan.");
            return;
        }
        
        // discard this forceRescan if one is already enqueued for the same node ID
        if (RescanProcessor.isRescanQueuedForNode(nodeid.intValue())) {
            log().info("Ignoring forceRescan event for node " + nodeid + " because a forceRescan for that node already exists in the queue");
            return;
        }
        
        // Rescan the node.
        m_scheduler.forceRescan(nodeid.intValue());
    }

    /**
     * Helper method to add a node from the new suspect event Event to the suspect scan schedule.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     */
    @EventHandler(uei=EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI)
    public void handleNewSuspect(final Event event) throws InsufficientInformationException {
        // ensure the event has an interface
        EventUtils.checkInterface(event);

        final String interfaceValue = event.getInterface();

        // discard this newSuspect if one is already enqueued for the same IP address
        if (SuspectEventProcessor.isScanQueuedForAddress(interfaceValue)) {
        	log().info("Ignoring newSuspect event for interface " + interfaceValue + " because a newSuspect scan for that interface already exists in the queue");
        	return;
        }
        
        // new suspect event
        try {
            if (log().isDebugEnabled()) log().debug("onMessage: Adding interface to suspectInterface Q: " + interfaceValue);
            m_suspectQ.execute(m_suspectEventProcessorFactory.createSuspectEventProcessor(interfaceValue));
        } catch (final Throwable ex) {
            log().error("onMessage: Failed to add interface to suspect queue", ex);
        }
    }

    /**
     * Adds the node from the event to the rescan schedule.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     */
    @EventHandler(uei=EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAdded(Event event) throws InsufficientInformationException {
        EventUtils.checkNodeId(event);

        // Schedule the new node.
        try {
            m_scheduler.scheduleNode(event.getNodeid().intValue());
        } catch (SQLException sqlE) {
            log().error("onMessage: SQL exception while attempting to schedule node " + event.getNodeid(), sqlE);
        }
    }

    /**
     * Handles the process of removing a deleted node from the rescan schedule.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     */
    @EventHandler(uei=EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeleted(Event event) throws InsufficientInformationException {

        EventUtils.checkNodeId(event);

        // Remove the deleted node from the scheduler
        m_scheduler.unscheduleNode(event.getNodeid().intValue());
    }

    /**
     * Handles the process of adding/updating a node.
     * TODO: Change the name of this to something that makes more sense.  This impacts the UEI of the named event
     * for consistency and clearity, however, being called "Server" is unclear to itself.  Change the event from
     * "Server" to "Node" and all related methods.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException if any.
     * @throws org.opennms.netmgt.capsd.FailedOperationException if any.
     */
    @EventHandler(uei=EventConstants.UPDATE_SERVER_EVENT_UEI)
    public void handleUpdateServer(Event event) throws InsufficientInformationException, FailedOperationException {
        // If there is no interface or NMS server found then it cannot be
        // processed
        EventUtils.checkInterface(event);
        EventUtils.checkHost(event);
        EventUtils.requireParm(event, EventConstants.PARM_ACTION);
        EventUtils.requireParm(event, EventConstants.PARM_NODE_LABEL);
        if (isXmlRpcEnabled()) {
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);
        }

        String action = EventUtils.getParm(event, EventConstants.PARM_ACTION);
        String nodeLabel = EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL);
        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);

        if (log().isDebugEnabled())
            log().debug("updateServerHandler:  processing updateServer event for: " + event.getInterface() + " on OpenNMS server: " + m_localServer);

        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doUpdateServer(dbConn, nodeLabel, event.getInterface(), action, m_localServer, txNo);

        } catch (SQLException sqlE) {
            log().error("SQLException during updateServer on database.", sqlE);
            throw new FailedOperationException("SQLException during updateServer on database.", sqlE);
        } finally {
            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleUpdateServer: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("SQLException during updateServer on database.", ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleUpdateServer: Exception thrown closing connection: ", ex);
                        }
                }
        }

    }

    /**
     * Process the event, add or remove a specified interface/service pair into
     * the database. this event will cause an changeService event with the
     * specified action. An 'action' parameter wraped in the event will tell
     * which action to take to the service on the specified interface. The
     * ipaddress of the interface, the service name must be included in the
     * event.
     *
     * @param event
     *            The event to process.
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if there is missing information in the event
     * @throws org.opennms.netmgt.capsd.FailedOperationException
     *             if the operation fails for some reason
     */
    @EventHandler(uei=EventConstants.UPDATE_SERVICE_EVENT_UEI)
    public void handleUpdateService(Event event) throws InsufficientInformationException, FailedOperationException {

        EventUtils.checkInterface(event);
        EventUtils.checkService(event);
        EventUtils.requireParm(event, EventConstants.PARM_NODE_LABEL);
        EventUtils.requireParm(event, EventConstants.PARM_ACTION);
        if (isXmlRpcEnabled()) {
            EventUtils.requireParm(event, EventConstants.PARM_TRANSACTION_NO);
        }

        long txNo = EventUtils.getLongParm(event, EventConstants.PARM_TRANSACTION_NO, -1L);
        String action = EventUtils.getParm(event, EventConstants.PARM_ACTION);
        String nodeLabel = EventUtils.getParm(event, EventConstants.PARM_NODE_LABEL);

        if (log().isDebugEnabled())
            log().debug("handleUpdateService:  processing updateService event for : " + event.getService() + " on : " + event.getInterface());

        List<Event> eventsToSend = null;
        Connection dbConn = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doUpdateService(dbConn, nodeLabel, event.getInterface(), event.getService(), action, txNo);

        } catch (SQLException sqlE) {
            log().error("SQLException during handleUpdateService on database.", sqlE);
            throw new FailedOperationException(sqlE.getMessage());
        } finally {

            if (dbConn != null)
                try {
                    if (eventsToSend != null) {
                        dbConn.commit();
                        for(Event e : eventsToSend) {
                            EventUtils.sendEvent(e, event.getUei(), txNo, isXmlRpcEnabled());
                        }
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                    log().error("handleUpdateService: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException(ex.getMessage());
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            log().error("handleUpdateService: Exception thrown during close: ",ex);
                        }
                }
        }

    }

    /**
     * Returns true if and only an interface with the given ipaddr on a node
     * with the give label exists
     * 
     * @param dbConn
     *            a database connection
     * @param nodeLabel
     *            the label of the node the interface must reside on
     * @param ipaddr
     *            the ip address the interface should have
     * @return true iff the interface exists
     * @throws SQLException
     *             if a database error occurs
     */
    private boolean interfaceExists(Connection dbConn, String nodeLabel, String ipaddr) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_QUERY_IPINTERFACE_EXIST);
            d.watch(stmt);

            stmt.setString(1, nodeLabel);
            stmt.setString(2, ipaddr);

            rs = stmt.executeQuery();
            d.watch(rs);
            return rs.next();
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Mark all the services associated with a given interface as deleted and
     * create service deleted events for each one that gets deleted
     * 
     * @param dbConn
     *            the database connection
     * @param nodeId
     *            the node that interface resides on
     * @param ipAddr
     *            the ipAddress of the interface
     * @param txNo
     *            a transaction number that can be associated with this deletion
     * @return a List of serviceDeleted events, one for each service marked
     * @throws SQLException
     *             if a database error occurs
     */
    private List<Event> markAllServicesForInterfaceDeleted(Connection dbConn, String source, long nodeId, String ipAddr, long txNo) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            List<Event> eventsToSend = new LinkedList<Event>();

            final String DB_FIND_SERVICES_FOR_INTERFACE = "SELECT DISTINCT service.serviceName FROM ifservices as ifservices, service as service WHERE ifservices.nodeID = ? and ifservices.ipAddr = ? and ifservices.status != 'D' and ifservices.serviceID = service.serviceID";
            stmt = dbConn.prepareStatement(DB_FIND_SERVICES_FOR_INTERFACE);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            rs = stmt.executeQuery();
            d.watch(rs);

            Set<String> services = new HashSet<String>();
            while (rs.next()) {
                String serviceName = rs.getString(1);
                log().debug("found service " + serviceName + " for ipAddr " + ipAddr + " node " + nodeId);
                services.add(serviceName);
            }

            final String DB_MARK_SERVICES_FOR_INTERFACE = "UPDATE ifservices SET status = 'D' where ifservices.nodeID = ? and ifservices.ipAddr = ?";
            stmt = dbConn.prepareStatement(DB_MARK_SERVICES_FOR_INTERFACE);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);

            stmt.executeUpdate();

            for(String serviceName : services) {
                log().debug("creating event for service " + serviceName + " for ipAddr " + ipAddr + " node " + nodeId);
                eventsToSend.add(EventUtils.createServiceDeletedEvent(source, nodeId, ipAddr, serviceName, txNo));
            }

            if (log().isDebugEnabled())
                log().debug("markServicesDeleted: marked service deleted: " + nodeId + "/" + ipAddr);

            return eventsToSend;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Mark the given interface deleted
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source for any events set
     * @param nodeId
     *            the id the interface resides on
     * @param ipAddr
     *            the ipAddress of the interface
     * @param txNo
     *            a transaction no to associate with this deletion
     * @return a List containing an interfaceDeleted event for the interface if
     *         it was actually marked
     * @throws SQLException
     *             if a database error occurs
     */
    private List<Event> markInterfaceDeleted(Connection dbConn, String source, long nodeId, String ipAddr, long txNo) throws SQLException {
       return markInterfaceDeleted(dbConn, source, nodeId, ipAddr, -1, txNo);
    }
    
    /**
     * Mark the given interface deleted
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source for any events set
     * @param nodeId
     *            the id the interface resides on
     * @param ipAddr
     *            the ipAddress of the interface
     * @param ifIndex
     *            the ifIndex of the interface            
     * @param txNo
     *            a transaction no to associate with this deletion
     * @return a List containing an interfaceDeleted event for the interface if
     *         it was actually marked
     * @throws SQLException
     *             if a database error occurs
     */
    private List<Event> markInterfaceDeleted(Connection dbConn, String source, long nodeId, String ipAddr, int ifIndex, long txNo) throws SQLException {
        final String DB_FIND_INTERFACE = "UPDATE ipinterface SET isManaged = 'D' WHERE nodeid = ? and ipAddr = ? and isManaged != 'D'";
        final String DB_FIND_SNMPINTERFACE = "UPDATE snmpinterface SET snmpcollect = 'D' WHERE nodeid = ? and snmpifindex = ? and snmpcollect != 'D'";
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        int countip = 0;
        int countsnmp = 0;
        try {

            if(!EventUtils.isNonIpInterface(ipAddr)) {
                stmt = dbConn.prepareStatement(DB_FIND_INTERFACE);
                d.watch(stmt);
                stmt.setLong(1, nodeId);
                stmt.setString(2, ipAddr);
                countip = stmt.executeUpdate();
            }
            if (ifIndex > -1) {
                stmt = dbConn.prepareStatement(DB_FIND_SNMPINTERFACE);
                d.watch(stmt);
                stmt.setLong(1, nodeId);
                stmt.setInt(2, ifIndex);
                countsnmp = stmt.executeUpdate();
            }
            
            if (countip > 0) {
                if (log().isDebugEnabled()) {
                    log().debug("markInterfaceDeleted: marked ip interface deleted: node = " + nodeId + ", IP address = " + ipAddr);
                }
            }
            if (countsnmp > 0) {
                if (log().isDebugEnabled()) {       
                    log().debug("markInterfaceDeleted: marked snmp interface deleted: node = " + nodeId + ", ifIndex = " + ifIndex);
                }
            }
            if (countip > 0 || countsnmp > 0) {
                return Collections.singletonList(EventUtils.createInterfaceDeletedEvent(source, nodeId, ipAddr, ifIndex, txNo));
            } else {
                if (log().isDebugEnabled()) {
                    log().debug("markInterfaceDeleted: Interface not found: node = " + nodeId
                                            + ", with ip address " + (ipAddr != null ? ipAddr : "null")
                                            + ", and ifIndex " + (ifIndex > -1 ? ifIndex : "N/A"));

                }
                return Collections.emptyList();
            }
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Marks all the interfaces and services for a given node deleted and
     * constructs events for each. The order of events is significant
     * representing the hierarchy, service events preceed the event for the
     * interface the service is on
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source for use in the constructed events
     * @param nodeId
     *            the node whose interfaces and services are to be deleted
     * @param txNo
     *            a transaction number to associate with this deletion
     * @return a List of events indicating which nodes and services have been
     *         deleted
     * 
     * @throws SQLException
     */
    private List<Event> markInterfacesAndServicesDeleted(Connection dbConn, String source, long nodeId, long txNo) throws SQLException {
        List<Event> eventsToSend = new LinkedList<Event>();

        final String DB_FIND_IFS_FOR_NODE = "SELECT ipinterface.ipaddr FROM ipinterface WHERE ipinterface.nodeid = ? and ipinterface.ismanaged != 'D'";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(DB_FIND_IFS_FOR_NODE);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            rs = stmt.executeQuery();
            d.watch(rs);

            Set<String> ipAddrs = new HashSet<String>();
            while (rs.next()) {
                String ipAddr = rs.getString(1);
                log().debug("found interface " + ipAddr + " for node " + nodeId);
                ipAddrs.add(ipAddr);
            }

            for(String ipAddr : ipAddrs) {
                log().debug("deleting interface " + ipAddr + " for node " + nodeId);
                eventsToSend.addAll(markAllServicesForInterfaceDeleted(dbConn, source, nodeId, ipAddr, txNo));
                eventsToSend.addAll(markInterfaceDeleted(dbConn, source, nodeId, ipAddr, txNo));
            }

            return eventsToSend;
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Marks a node deleted and creates an event for it if necessary.
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source to use for constructed events
     * @param nodeId
     *            the node to delete
     * @param txNo
     *            a transaction number to associate with this deletion
     * @return a List containing the node deleted event if necessary
     * @throws SQLException
     *             if a database error occurs
     */
    private List<Event> markNodeDeleted(Connection dbConn, String source, long nodeId, long txNo) throws SQLException {
        final String DB_FIND_INTERFACE = "UPDATE node SET nodeType = 'D' WHERE nodeid = ? and nodeType != 'D'";
        PreparedStatement stmt = null;

        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(DB_FIND_INTERFACE);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            int count = stmt.executeUpdate();

            log().debug("markServicesDeleted: marked service deleted: " + nodeId);

            if (count > 0)
                return Collections.singletonList(EventUtils.createNodeDeletedEvent(source, nodeId, txNo));
            else
                return Collections.emptyList();
        } finally {
            d.cleanUp();
        }
    }

    /**
     * Marks the service deleted in the database and returns a serviceDeleted
     * event for the service, if and only if the service existed
     * 
     * @param dbConn
     *            the database connection
     * @param source
     *            the source for any events sent
     * @param nodeId
     *            the node the service resides on
     * @param ipAddr
     *            the interface the service resides on
     * @param service
     *            the name of the service
     * @param txNo
     *            a transaction number to associate with this deletion
     * @return a List containing a service deleted event.
     * @throws SQLException
     *             if an error occurs communicating with the database
     */
    private List<Event> markServiceDeleted(Connection dbConn, String source, long nodeId, String ipAddr, String service, long txNo) throws SQLException {
        PreparedStatement stmt = null;

        final String DB_MARK_SERVICE_DELETED =
            "UPDATE ifservices SET status='D' "
            + "FROM service "
            + "WHERE ifservices.serviceID = service.serviceID "
            + "AND ifservices.nodeID=? AND ifservices.ipAddr=? AND service.serviceName=?";

        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(DB_MARK_SERVICE_DELETED);
            d.watch(stmt);
            stmt.setLong(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setString(3, service);
            int count = stmt.executeUpdate();

            if (log().isDebugEnabled())
                log().debug("markServiceDeleted: marked service deleted: " + nodeId + "/" + ipAddr + "/" + service);

            if (count > 0)
                return Collections.singletonList(EventUtils.createServiceDeletedEvent(source, nodeId, ipAddr, service, txNo));
            else
                return Collections.emptyList();
        } finally {
            d.cleanUp();
        }        
    }

    /**
     * Returns true if and only a node with the give label exists
     * 
     * @param dbConn
     *            a database connection
     * @param nodeLabel
     *            the label to check
     * @return true iff the node exists
     * @throws SQLException
     *             if a database error occurs
     */
    private boolean nodeExists(Connection dbConn, String nodeLabel) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_QUERY_NODE_EXIST);
            d.watch(stmt);
            stmt.setString(1, nodeLabel);

            rs = stmt.executeQuery();
            d.watch(rs);
            return rs.next();
        } finally {
            d.cleanUp();
        }

    }

     /**
     * JDBC Query to service map table.  This will soon be cleaned up with Hibernate/DAO code.
     * 
     * @param dbConn
     * @param ipaddr
     * @param serviceName
     * @return
     * @throws SQLException
     */
    private boolean serviceMappingExists(Connection dbConn, String ipaddr, String serviceName) throws SQLException {
        boolean mapExists;
        PreparedStatement stmt = null;
        // Verify if the specified service already exists on the
        // interface/service
        // mapping.
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = dbConn.prepareStatement(SQL_QUERY_SERVICE_MAPPING_EXIST);
            d.watch(stmt);
            
            stmt.setString(1, ipaddr);
            stmt.setString(2, serviceName);

            ResultSet rs = stmt.executeQuery();
            d.watch(rs);
            
            mapExists = rs.next();
            return mapExists;
        } finally {
            d.cleanUp();
        }        
   }

    /**
     * JDBC Query using @param serviceName to determine if the serviceName is indeed as
     * service name in the service table.
     * 
     * FIXME: This sucks:
     *  1) No transaction management
     *  2) Such a small table should be cached and accessed via a synchronized call.  Quit
     *  going to the DB for such trivial information.  This is a performance killer.  Especially
     *  since our current db factory (factories) use the synchonized DriverManager JDBC class. 
     * 
     * @param dbConn
     * @param serviceName
     * @return
     * @throws SQLException
     * @throws FailedOperationException
     */
    private int verifyServiceExists(Connection dbConn, String serviceName) throws SQLException, FailedOperationException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            // Retrieve the serviceId
            stmt = dbConn.prepareStatement(SQL_RETRIEVE_SERVICE_ID);
            d.watch(stmt);
            stmt.setString(1, serviceName);

            rs = stmt.executeQuery();
            d.watch(rs);
            int serviceId = -1;
            while (rs.next()) {
                log().debug("verifyServiceExists: retrieve serviceid for service " + serviceName);
                serviceId = rs.getInt(1);
            }

            if (serviceId < 0) {
                if (log().isDebugEnabled())
                    log().debug("verifyServiceExists: the specified service: " + serviceName + " does not exist in the database.");
                throw new FailedOperationException("Invalid service: " + serviceName);
            }

            return serviceId;
        } finally {
            d.cleanUp();
        }        
    }
    
    private void verifyInterfaceExists(Connection dbConn, String nodeLabel, String ipaddr) throws SQLException, FailedOperationException {
        if (!interfaceExists(dbConn, nodeLabel, ipaddr))
            throw new FailedOperationException("Interface "+ipaddr+" does not exist on a node with nodeLabel "+nodeLabel);
    }

    /**
     * <p>setSuspectEventProcessorFactory</p>
     *
     * @param suspectEventProcessorFactory a {@link org.opennms.netmgt.capsd.SuspectEventProcessorFactory} object.
     */
    public void setSuspectEventProcessorFactory(SuspectEventProcessorFactory suspectEventProcessorFactory) {
        m_suspectEventProcessorFactory = suspectEventProcessorFactory;
    }

    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.opennms.netmgt.capsd.Scheduler} object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>setSuspectQueue</p>
     *
     * @param suspectQ a {@link java.util.concurrent.ExecutorService} object.
     */
    public void setSuspectQueue(ExecutorService suspectQ) {
        m_suspectQ = suspectQ;
    }

    /**
     * <p>setLocalServer</p>
     *
     * @param localServer a {@link java.lang.String} object.
     */
    public void setLocalServer(String localServer) {
        m_localServer = localServer;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_suspectEventProcessorFactory != null, "The suspectEventProcessor must be set");
        Assert.state(m_scheduler != null, "The schedule must be set");
        Assert.state(m_suspectQ != null, "The suspectQueue must be set");
        Assert.state(m_localServer != null, "The localServer must be set");
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }


}
