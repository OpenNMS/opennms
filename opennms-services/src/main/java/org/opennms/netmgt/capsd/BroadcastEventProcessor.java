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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.sql.Connection;
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
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.dao.hibernate.AlarmDaoHibernate;
import org.opennms.netmgt.dao.hibernate.IpInterfaceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.dao.hibernate.ServerMapDaoHibernate;
import org.opennms.netmgt.dao.hibernate.ServiceMapDaoHibernate;
import org.opennms.netmgt.dao.hibernate.ServiceTypeDaoHibernate;
import org.opennms.netmgt.dao.hibernate.SnmpInterfaceDaoHibernate;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServerMap;
import org.opennms.netmgt.model.OnmsServiceMap;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.capsd.DbIfServiceEntry;
import org.opennms.netmgt.model.capsd.DbIpInterfaceEntry;
import org.opennms.netmgt.model.capsd.DbNodeEntry;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * <p>BroadcastEventProcessor class.</p>
 *
 * @author <a href="mailto:matt@opennms.org">Matt Brozowski </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
@EventListener(name="Capsd:BroadcastEventProcessor", logPrefix="capsd")
public class BroadcastEventProcessor implements InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastEventProcessor.class);

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

    private IpInterfaceDaoHibernate m_ipInterfaceDao; 

    private MonitoredServiceDaoHibernate m_monitoredServiceDao;
    
    private NodeDaoHibernate m_nodeDao;
    
    private ServiceTypeDaoHibernate m_serviceTypeDao;
    
    private AlarmDaoHibernate m_alarmDao;
    
    private ServiceMapDaoHibernate m_serviceMapDao;

    private ServerMapDaoHibernate m_serverMapDao;
    
    private SnmpInterfaceDaoHibernate m_snmpInterfaceDao;

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
        // There is no ipinterface associated with the specified nodeLabel
        // exist in the database. Verify if a node with the nodeLabel already
        // exist in the database. If not, create a node with the nodeLabel and add it
        // to the database, and also add the ipaddress associated with this node to
        // the database. If the node with the nodeLabel exists in the node
        // table, just add the ip address to the database.

        List<OnmsNode> nodeList = m_nodeDao.findByLabel(nodeLabel);
        for(OnmsNode node : nodeList) {
            if (node.getType() == "D") {
                nodeList.remove(node);
            }
        }
        
        List<Event> eventsToSend = new LinkedList<Event>();
        for(OnmsNode node : nodeList) {

            LOG.debug("addInterfaceHandler:  add interface: " + ipaddr + " to the database.");

            // Node already exists. Add the ipaddess to the ipinterface
            // table
            InetAddress ifaddr;
            try {
                ifaddr = InetAddressUtils.addr(ipaddr);
            } catch (final IllegalArgumentException e) {
                throw new FailedOperationException("unable to resolve host " + ipaddr + ": " + e.getMessage(), e);
            }
            int nodeId = node.getId();
            String dpName = node.getDistPoller().getName();

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

        LOG.debug("addNode:  Add a node {} to the database", nodeLabel);

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
            LOG.debug("addNode:  Add an IP Address {} to the database", ipaddr);

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
            LOG.debug("addInterfaceHandler: node {} with IPAddress {} already exist in the database.", nodeLabel, ipaddr);
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
            LOG.debug("doAddNode: node {} with IPAddress {} already exist in the database.", nodeLabel, ipaddr);
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
        OnmsServiceMap serviceMap = new OnmsServiceMap(ipaddr, serviceName);
        m_serviceMapDao.save(serviceMap);
        
        LOG.debug("updateServiceHandler: add service " + serviceName + " to interface: " + ipaddr);

        return doChangeService(dbConn, ipaddr, serviceName, "ADD", txNo);
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

        List<OnmsIpInterface> ipInterfaceList = m_ipInterfaceDao.findByIpAddress(ipaddr);
        
        for (OnmsIpInterface ipInterface : ipInterfaceList) {
            if (ipInterface.getIsManaged() == "D") {
                ipInterfaceList.remove(ipInterface);
            }
        }
        

        List<Event> eventsToSend = new LinkedList<Event>();
        for(OnmsIpInterface ipInterface : ipInterfaceList) {
            LOG.debug("changeServiceHandler: add service " + serviceName + " to interface: " + ipaddr);

            InetAddress inetAddr;
            try {
                inetAddr = InetAddressUtils.addr(ipaddr);
            } catch (final IllegalArgumentException e) {
                throw new FailedOperationException("unable to resolve host " + ipaddr + ": " + e.getMessage(), e);
            }
            final int nodeId = ipInterface.getNode().getId();
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
        OnmsServerMap serverMap = new OnmsServerMap(ipaddr, hostName);
        m_serverMapDao.save(serverMap);
        LOG.debug("updateServerHandler: added interface " + ipaddr + " into NMS server: " + hostName);

        // Create a addInterface event and process it.
        // FIXME: do I need to make a direct call here?
        Event newEvent = EventUtils.createAddInterfaceEvent("OpenNMS.Capsd", nodeLabel, ipaddr, hostName, txNo);
        return Collections.singletonList(newEvent);
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
        if (!EventUtils.isNonIpInterface(ipAddr) && isPropagationEnabled() && m_ipInterfaceDao.getCountOfOtherInterfacesOnNode(nodeid, ipAddr) == 0) {
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
        final DBUtils d = new DBUtils(getClass());
        try {
            List<Event> eventsToSend = new LinkedList<Event>();

            // Delete all services on the specified interface in
            // interface/service
            // mapping
            //
            LOG.debug("updateServer: delete all services on the interface: " + ipaddr + " in the interface/service mapping.");
            List<OnmsServiceMap> serviceMapList = m_serviceMapDao.findbyIpAddr(ipaddr);
            for(OnmsServiceMap serviceMap : serviceMapList) {
                m_serviceMapDao.delete(serviceMap.getId());
            }
            
            // Delete the interface on interface/server mapping
            LOG.debug("updateServer: delete interface: " + ipaddr + " on NMS server: " + hostName);
            List<OnmsServerMap> serverMapList = m_serverMapDao.findByIpAddrAndServerName(ipaddr, hostName);
            for(OnmsServerMap serverMap : serverMapList) {
                m_serverMapDao.delete(serverMap.getId());
            }

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
        List<OnmsAlarm> alarmList = m_alarmDao.findByNodeId(nodeId);
        
        for(OnmsAlarm alarm : alarmList) {
           m_alarmDao.delete(alarm.getId()); 
        }
    }

    private void deleteAlarmsForInterface(Connection dbConn, long nodeId, String ipAddr) throws SQLException {
        List<OnmsAlarm> alarmList = m_alarmDao.findByNodeId(nodeId);
        
        for(OnmsAlarm alarm : alarmList) {
            if(InetAddressUtils.str(alarm.getIpAddr()) == ipAddr) 
                m_alarmDao.delete(alarm.getId()); 
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
        
        List<OnmsAlarm> alarmList = m_alarmDao.findByNodeId(nodeId);
        
        for(OnmsAlarm alarm : alarmList) {
            if(alarm.getIfIndex() == ifIndex)
                m_alarmDao.delete(alarm.getId()); 
        }
    }

    private void deleteAlarmsForService(Connection dbConn, long nodeId, String ipAddr, String service) throws SQLException {
        Integer serviceTypeId = m_serviceTypeDao.findByName(service).getId();
        
        List<OnmsAlarm> alarmList = m_alarmDao.findByNodeId(nodeId);
        
        for(OnmsAlarm alarm : alarmList) {
            if(InetAddressUtils.str(alarm.getIpAddr()) == ipAddr && alarm.getServiceType().getId() == serviceTypeId)
                m_alarmDao.delete(alarm.getId()); 
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
            int otherSvcsOnIfCnt = m_serviceTypeDao.getCountOfServicesOnInterface(nodeid, ipAddr, service);
            if (otherSvcsOnIfCnt == 0 && m_monitoredServiceDao.findByNodeIdAndIpAddr(nodeid, ipAddr).size() == 0) {
                // no services on this interface or any other interface on this
                // node so delete
                // node
                LOG.debug("Propagating service delete to node {}", nodeid);
                eventsToSend.addAll(doDeleteNode(dbConn, source, nodeid, txNo));
            } else if (otherSvcsOnIfCnt == 0) {
                // no services on this interface so delete interface
                LOG.debug("Propagting service delete to interface {}/{}", nodeid, ipAddr);
                eventsToSend.addAll(doDeleteInterface(dbConn, source, nodeid, ipAddr, txNo));
            } else {
                LOG.debug("No need to Propagate service delete {}/{}/{}", nodeid, ipAddr, service);
                // otherwise just mark the service as deleted and send a
                // serviceDeleted event
                eventsToSend.addAll(markServiceDeleted(dbConn, source, nodeid, ipAddr, service, txNo));
            }
        } else {
            LOG.debug("Propagation disabled:  deleting only service {}/{}/{}", nodeid, ipAddr, service);
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
        LOG.debug("handleUpdateService: delete service: " + serviceName + " on IPAddress: " + ipaddr);
        Integer serviceMapId = m_serviceMapDao.findbyIpAddrAndServiceName(ipaddr, serviceName).getId();
        m_serviceMapDao.delete(serviceMapId);

        return doChangeService(dbConn, ipaddr, serviceName, "DELETE", txNo);
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
            LOG.error("updateServerHandler: could not process interface: {} on NMS server: {}: action {} unknown", ipaddr, hostName, action);
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
        /**
         * SQL statement used to query if an interface/server mapping
         * already exists in the database.
         */
        List<OnmsServerMap> serverMapList = m_serverMapDao.findByIpAddrAndServerName(ipaddr, hostName);
        int count = serverMapList.size();

        return count > 0;
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
        List<OnmsMonitoredService> serviceList = m_monitoredServiceDao.getServiceStatus(ipaddr, serviceName);
        int[] nodeIds;
        List<Integer> nodeIdList = new LinkedList<Integer>();
        for(OnmsMonitoredService service : serviceList) {
            LOG.debug("changeService: service " + serviceName + " on IPAddress " + ipaddr + " already exists in the database.");
            int nodeId = service.getNodeId();
            nodeIdList.add(nodeId);
        }
        nodeIds = new int[nodeIdList.size()];
        int i = 0;
        for(Integer n : nodeIdList) {
            nodeIds[i++] = n.intValue();
        }
        return nodeIds;
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
        List<OnmsNode> nodeList = m_nodeDao.findNodeIdsForInterfaceAndLabel(nodeLabel, ipAddr);
        List<Long> nodeIdList = new LinkedList<Long>();
        for(OnmsNode node : nodeList) {
            nodeIdList.add(Long.valueOf(node.getId()));
        }

        long[] nodeIds = new long[nodeIdList.size()];
        int i = 0;
        for(Long nodeId : nodeIdList) {
            nodeIds[i++] = nodeId.longValue();
        }
        return nodeIds;
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
        LOG.debug("addInterfaceHandler:  processing addInterface event for {}", event.getInterface());

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
            LOG.error("addInterfaceHandler: SQLException during add node and ipaddress to the database.", sqlE);
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
                    LOG.error("handleAddInterface: Threw Exception during commit: ", ex);
                    throw new FailedOperationException("Database error: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.setAutoCommit(true); //TODO:verify this
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleAddInterface: Threw Exception during close: ", ex);
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
        LOG.debug("addNodeHandler:  processing addNode event for {}", ipaddr);
        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doAddNode(dbConn, nodeLabel, ipaddr);
        } catch (SQLException sqlE) {
            LOG.error("addNodeHandler: SQLException during add node and ipaddress to tables", sqlE);
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
                    LOG.error("handleAddNode: Threw Exception during commit: ", ex);
                    throw new FailedOperationException("database error: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleAddNode: Threw Exception during close: ", ex);
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

        LOG.debug("changeServiceHandler:  processing changeService event on: {}", event.getInterface());

        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doChangeService(dbConn, event.getInterface(), event.getService(), action, txNo);
        } catch (SQLException sqlE) {
            LOG.error("SQLException during changeService on database.", sqlE);
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
                    LOG.error("handleChangeService: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing changeService: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleChangeService: Exception thrown closing connection: {}", ex);
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
        LOG.debug("handleDeleteInterface: Event\nuei\t\t{}\neventid\t\t{}\nnodeId\t\t{}\nipaddr\t\t{}\nifIndex\t\t{}\neventtime\t{}", event.getUei(), event.getDbid(), event.getNodeid(), (event.getInterface() != null ? event.getInterface() : "N/A" ), (ifIndex > -1 ? ifIndex : "N/A" ), (event.getTime() != null ? event.getTime() : "<null>"));

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
            LOG.error("handleDeleteInterface:  Database error deleting interface on node {} with ip address {} and ifIndex {}", event.getNodeid(), (event.getInterface() != null ? event.getInterface() : "null"), (event.hasIfIndex() ? event.getIfIndex() : "null"), ex);
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
                    LOG.error("handleDeleteInterface: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing delete interface: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleDeleteInterface: Exception thrown closing connection: ", ex);
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
        LOG.debug("handleDeleteNode: Event\nuei\t\t{}\neventid\t\t{}\nnodeId\t\t{}\neventtime\t{}", event.getUei(), event.getDbid(), nodeid, (event.getTime() != null ? event.getTime() : "<null>"));

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
            LOG.error("handleDeleteService:  Database error deleting service {} on ipAddr {} for node {}", event.getService(), event.getInterface(), nodeid, ex);
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
                    LOG.error("handleDeleteNode: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing deleteNode: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleDeleteNode: Exception thrown closing connection: ",ex);
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
        LOG.debug("handleDeleteService: Event\nuei\t\t{}\neventid\t\t{}\nnodeid\t\t{}\nipaddr\t\t{}\nservice\t\t{}\neventtime\t{}", event.getUei(), event.getDbid() , event.getNodeid(), event.getInterface(), event.getService(), (event.getTime() != null ? event.getTime() : "<null>"));

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
            LOG.error("handleDeleteService:  Database error deleting service {} on ipAddr {} for node {}", event.getService(), event.getInterface(), event.getNodeid(), ex);
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
                    LOG.error("handleDeleteService: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("exeption processing deleteService: " + ex.getMessage(), ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleDeleteService: Exception thrown closing connection: ", ex);
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
            
            List<OnmsIpInterface> ipInterfaceList = m_ipInterfaceDao.findByIpAddress(event.getInterface());
            
            for (OnmsIpInterface ipInterface : ipInterfaceList) {
                if(ipInterface.getIsManaged() == "D") 
                    ipInterfaceList.remove(ipInterface);
            }
            
            nodeid = Long.valueOf(ipInterfaceList.get(0).getNode().getId());
        }

        if (nodeid == null || nodeid == -1) {
            LOG.error("handleForceRescan: Nodeid retrieval for interface {} failed.  Unable to perform rescan.", event.getInterface());
            return;
        }

        // discard this forceRescan if one is already enqueued for the same node ID
        if (RescanProcessor.isRescanQueuedForNode(nodeid.intValue())) {
            LOG.info("Ignoring forceRescan event for node {} because a forceRescan for that node already exists in the queue", nodeid);
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
            LOG.info("Ignoring newSuspect event for interface " + interfaceValue + " because a newSuspect scan for that interface already exists in the queue");
            return;
        }

        // new suspect event
        try {
            LOG.debug("onMessage: Adding interface to suspectInterface Q: {}", interfaceValue);
            m_suspectQ.execute(m_suspectEventProcessorFactory.createSuspectEventProcessor(interfaceValue));
        } catch (final Throwable ex) {
            LOG.error("onMessage: Failed to add interface to suspect queue", ex);
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
            LOG.error("onMessage: SQL exception while attempting to schedule node {}", event.getNodeid(), sqlE);
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

        LOG.debug("updateServerHandler:  processing updateServer event for: {} on OpenNMS server: {}", event.getInterface(), m_localServer);

        Connection dbConn = null;
        List<Event> eventsToSend = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doUpdateServer(dbConn, nodeLabel, event.getInterface(), action, m_localServer, txNo);

        } catch (SQLException sqlE) {
            LOG.error("SQLException during updateServer on database.", sqlE);
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
                    LOG.error("handleUpdateServer: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException("SQLException during updateServer on database.", ex);
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleUpdateServer: Exception thrown closing connection: ", ex);
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

        LOG.debug("handleUpdateService:  processing updateService event for : {} on : {}", event.getService(), event.getInterface());

        List<Event> eventsToSend = null;
        Connection dbConn = null;
        try {
            dbConn = getConnection();
            dbConn.setAutoCommit(false);

            eventsToSend = doUpdateService(dbConn, nodeLabel, event.getInterface(), event.getService(), action, txNo);

        } catch (SQLException sqlE) {
            LOG.error("SQLException during handleUpdateService on database.", sqlE);
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
                    LOG.error("handleUpdateService: Exception thrown during commit/rollback: ", ex);
                    throw new FailedOperationException(ex.getMessage());
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (SQLException ex) {
                            LOG.error("handleUpdateService: Exception thrown during close: ",ex);
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
        int count = m_ipInterfaceDao.getIpInterfaceStatus(nodeLabel, ipaddr);
        return count == 0 ? false : true; 
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
        final DBUtils d = new DBUtils(getClass());
        try {
            List<Event> eventsToSend = new LinkedList<Event>();
            List<OnmsServiceType> serviceTypeList = m_serviceTypeDao.findByNodeIdAndIpAddr(nodeId, ipAddr);

            Set<String> services = new HashSet<String>();
            for(OnmsServiceType svcType : serviceTypeList) {
                String serviceName = svcType.getName();
                LOG.debug("found service " + serviceName + " for ipAddr " + ipAddr + " node " + nodeId);
                services.add(serviceName);
            }

            List<OnmsMonitoredService> serviceList = m_monitoredServiceDao.findByNodeIdAndIpAddr(nodeId, ipAddr);
            for(OnmsMonitoredService svc : serviceList) {
                svc.setStatus("D");
                m_monitoredServiceDao.update(svc);
            }
            
            for(String serviceName : services) {
                LOG.debug("creating event for service {} for ipAddr {} node {}", serviceName, ipAddr, nodeId);
                eventsToSend.add(EventUtils.createServiceDeletedEvent(source, nodeId, ipAddr, serviceName, txNo));
            }

            LOG.debug("markServicesDeleted: marked service deleted: {}/{}", nodeId, ipAddr);

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
        int countip = 0;
        int countsnmp = 0;
        if(!EventUtils.isNonIpInterface(ipAddr)) {
            List<OnmsIpInterface> ipInterfaceList = m_ipInterfaceDao.findByIpAddress(ipAddr);
            for(OnmsIpInterface ipInterface : ipInterfaceList) {
                if(ipInterface.getNode().getId() == nodeId && ipInterface.getIsManaged() != "D") {
                    ipInterface.setIsManaged("D");
                    m_ipInterfaceDao.update(ipInterface);
                    countip++;
                }
            }
        }
        if (ifIndex > -1) {
            List<OnmsSnmpInterface> snmpInterfaceList =  m_snmpInterfaceDao.findListByNodeIdAndIfIndex((int)nodeId, ifIndex);
            for(OnmsSnmpInterface snmpInterface : snmpInterfaceList) {
                if(snmpInterface.getCollect() != "D") {
                    snmpInterface.setCollect("D");
                    countsnmp++;
                }
            }
        }

        if (countip > 0) {
            LOG.debug("markInterfaceDeleted: marked ip interface deleted: node = {}, IP address = {}", nodeId, ipAddr);
        }
        if (countsnmp > 0) {
            LOG.debug("markInterfaceDeleted: marked snmp interface deleted: node = {}, ifIndex = {}", nodeId, ifIndex);
        }
        if (countip > 0 || countsnmp > 0) {
            return Collections.singletonList(EventUtils.createInterfaceDeletedEvent(source, nodeId, ipAddr, ifIndex, txNo));
        } else {
            LOG.debug("markInterfaceDeleted: Interface not found: node = {}, with ip address {}, and ifIndex {}", nodeId, (ipAddr != null ? ipAddr : "null"),  (ifIndex > -1 ? ifIndex : "N/A"));
            return Collections.emptyList();
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

        List<OnmsIpInterface> ipInterfaceList = m_ipInterfaceDao.findByNodeId((int)nodeId);
        for(OnmsIpInterface ipInterface : ipInterfaceList) {
            if(ipInterface.getIsManaged() == "D") {
                ipInterfaceList.remove(ipInterface);
            }
        }
        
        Set<String> ipAddrs = new HashSet<String>();
        for(OnmsIpInterface ipInterface : ipInterfaceList) {
            String ipAddr = InetAddressUtils.str(ipInterface.getIpAddress());
            LOG.debug("found interface " + ipAddr + " for node " + nodeId);
            ipAddrs.add(ipAddr);
        }

            for(String ipAddr : ipAddrs) {
                LOG.debug("deleting interface {} for node {}", ipAddr, nodeId);
                eventsToSend.addAll(markAllServicesForInterfaceDeleted(dbConn, source, nodeId, ipAddr, txNo));
                eventsToSend.addAll(markInterfaceDeleted(dbConn, source, nodeId, ipAddr, txNo));
            }

        return eventsToSend;
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
        String label = m_nodeDao.getLabelForId((int)nodeId);
        List<OnmsNode> nodeList = m_nodeDao.findByLabel(label);
        int count = 0;
        for(OnmsNode node : nodeList) {
            if(node.getType() != "D") {
                node.setType("D");
                m_nodeDao.update(node);
                count++;
            }
        }
        LOG.debug("markServicesDeleted: marked service deleted: " + nodeId);

        if (count > 0)
            return Collections.singletonList(EventUtils.createNodeDeletedEvent(source, nodeId, txNo));
        else
            return Collections.emptyList();
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
            int count = m_monitoredServiceDao.markServiceDeleted(nodeId, ipAddr, service);

            LOG.debug("markServiceDeleted: marked service deleted: {}/{}/{}", nodeId, ipAddr, service);

            if (count > 0)
                return Collections.singletonList(EventUtils.createServiceDeletedEvent(source, nodeId, ipAddr, service, txNo));
            else
                return Collections.emptyList();
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
        List<OnmsNode> nodeList = m_nodeDao.findByLabel(nodeLabel);
        if (nodeList != null) {
            for(OnmsNode node : nodeList) {
                if (node.getType() == "D") {
                    nodeList.remove(node);
                }
            }
            if (nodeList.size() != 0) {
                return true;
            }
        }
        return false;
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
        // Verify if the specified service already exists on the
        // interface/service
        // mapping.
        OnmsServiceMap serviceMap = m_serviceMapDao.findbyIpAddrAndServiceName(ipaddr, serviceName);
        mapExists = serviceMap != null;
        return mapExists;
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
        LOG.debug("verifyServiceExists: retrieve serviceid for service " + serviceName);
        
        int serviceId = m_serviceTypeDao.findByName(serviceName).getId();

        if (serviceId < 0) {
            LOG.debug("verifyServiceExists: the specified service: " + serviceName + " does not exist in the database.");
            throw new FailedOperationException("Invalid service: " + serviceName);
        }

        return serviceId;
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


    @Autowired
    public void setIpInterfaceDao(IpInterfaceDaoHibernate ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    @Autowired
    public void setMonitoredServiceDao(
            MonitoredServiceDaoHibernate monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    @Autowired
    public void setNodeDao(NodeDaoHibernate nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Autowired
    public void setServiceTypeDao(ServiceTypeDaoHibernate serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }
    
    @Autowired
    public void setAlarmDao(AlarmDaoHibernate alarmDao) {
        m_alarmDao = alarmDao;
    }

    @Autowired
    public void setServiceMapDao(ServiceMapDaoHibernate serviceMapDao) {
        m_serviceMapDao = serviceMapDao;
    }
    
    @Autowired
    public void setSnmpInterfaceDao(SnmpInterfaceDaoHibernate snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }
}
