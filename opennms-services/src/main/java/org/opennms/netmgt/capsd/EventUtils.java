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

/*
 * TODO
 * Tremendous amount of code duplication that should be refactored.
 */
package org.opennms.netmgt.capsd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.capsd.DbNodeEntry;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.utils.XmlrpcUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;

/**
 * Provides a collection of utility methods used by the DeleteEvent Processor
 * for dealing with Events
 *
 * @author brozow
 */
public abstract class EventUtils {

    /**
     * Make the given listener object a listener for the list of events
     * referenced in the ueiList.
     *
     * @param listener
     *            the lister to add
     * @param ueiList
     *            the list of events the listener is interested
     */
    public static void addEventListener(EventListener listener, List<String> ueiList) {
        EventIpcManagerFactory.init();
        EventIpcManagerFactory.getIpcManager().addEventListener(listener, ueiList);
    }

    /**
     * Ensures that the event has a database eventId
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if an event id is not evailable
     */
    static public void checkEventId(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (!e.hasDbid())
            throw new InsufficientInformationException("eventID is unavailable");
    }

    /**
     * Ensures the given event has an interface
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if an interface is not available
     */
    static public void checkInterface(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        } else if (e.getInterface() == null) {
            throw new InsufficientInformationException("ipaddr for event is unavailable");
        }
    }
    
    /**
     * Is the given interface a non-IP interface
     *
     * @param intf
     *            the interface
     * @return true/false
     */
    static public boolean isNonIpInterface(String intf) {
        if (intf == null || intf.length() == 0 || "0.0.0.0".equals(intf) ) {
            return true;
        } else {
            return false;
        }
    }
   
    
    /**
     * Ensures the given event has an interface or ifIndex
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if  neither an interface nor an ifIndex is available
     */
    static public void checkInterfaceOrIfIndex(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("event is null");
        } else if (e.getInterface() == null) {
            if (!e.hasIfIndex()) {
                throw new InsufficientInformationException("Neither ipaddr nor ifIndex for the event is available");
            }
        }
    }

    /**
     * Ensures the given event has a host
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if an interface is not available
     */
    static public void checkHost(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        } else if (e.getHost() == null || e.getHost().length() == 0) {
            throw new InsufficientInformationException("host for event is unavailable");
        }
    }

    /**
     * Ensures that the given Event has a node id
     *
     * @param e
     *            the event
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if a node id is not available
     */
    static public void checkNodeId(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        } else if (!e.hasNodeid()) {
            throw new InsufficientInformationException("nodeid for event is unavailable");
        }
    }

    /**
     * Ensures that the given event has a service parameter
     *
     * @param e
     *            the event to check
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the event does not have a service
     */
    public static void checkService(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        } else if (e.getService() == null || e.getService().length() == 0) {
            throw new InsufficientInformationException("service for event is unavailable");
        }
    }

    /**
     * Constructs a deleteInterface event for the given nodeId, ipAddress (or ifIndex) pair.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the nodeId of the node that owns the interface
     * @param ipAddr
     *            the ipAddress of the interface being deleted
     * @param ifIndex
     *            the ifIndex of the interface being deleted
     * @param txNo
     *            the transaction number to use for processing this event
     * @return an Event representing a deleteInterface event for the given
     *         nodeId, ipaddr
     */
    public static Event createDeleteInterfaceEvent(String source, long nodeId, String ipAddr, int ifIndex, long txNo) {
        return createInterfaceEventBuilder(EventConstants.DELETE_INTERFACE_EVENT_UEI, source, nodeId, ipAddr, ifIndex, txNo).getEvent();
    }

    private static EventBuilder createInterfaceEventBuilder(String uei, String source, long nodeId, String ipAddr, int ifIndex, long txNo) {
        EventBuilder bldr = new EventBuilder(uei, source);
        
        if (ipAddr != null && ipAddr.length() != 0) {
            bldr.setInterface(addr(ipAddr));
        }
        
        bldr.setNodeid(nodeId);

        if (ifIndex != -1) {
            bldr.setIfIndex(ifIndex);
        }

        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr;
    }

    /**
     * Construct a deleteNode event for the given nodeId.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the node to be deleted.
     * @param txNo
     *            the transaction number associated with deleting the node
     * @return an Event object representing a delete node event.
     */
    public static Event createDeleteNodeEvent(String source, long nodeId, long txNo) {
        return createNodeEventBuilder(EventConstants.DELETE_NODE_EVENT_UEI, source, nodeId, txNo).getEvent();
    }

    private static EventBuilder createNodeEventBuilder(String uei, String source, long nodeId, long txNo) {
        EventBuilder bldr = new EventBuilder(uei, source);
        
        bldr.setNodeid(nodeId);
        
        if (txNo >= 0) {
            bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);
        }
        return bldr;
    }

    /**
     * Construct a deleteNode event for the given nodeId.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the node to be deleted.
     * @param txNo
     *            the transaction number associated with deleting the node
     * @return an Event object representing a delete node event.
     */
    public static Event createAssetInfoChangedEvent(String source, long nodeId, long txNo) {
        return createNodeEventBuilder(EventConstants.ASSET_INFO_CHANGED_EVENT_UEI, source, nodeId, txNo).getEvent();
    }

    /**
     * Construct an interfaceDeleted event for an interface.
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            the nodeId of the node the interface resides in
     * @param ipAddr
     *            the ipAdddr of the event
     * @param txNo
     *            a transaction number associated with the event
     * @return Event
     *            an interfaceDeleted event for the given interface
     */
    public static Event createInterfaceDeletedEvent(String source, long nodeId, String ipAddr, long txNo) {
        return createInterfaceDeletedEvent(source, nodeId, ipAddr, -1, txNo);
    }
    
    /**
     * Construct an interfaceDeleted event for an interface.
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            the nodeId of the node the interface resides in
     * @param ipAddr
     *            the ipAdddr of the event
     * @param ifIndex
     *            the ifIndex of the event
     * @param txNo
     *            a transaction number associated with the event
     * @return Event
     *            an interfaceDeleted event for the given interface
     */
    public static Event createInterfaceDeletedEvent(String source, long nodeId, String ipAddr, int ifIndex, long txNo) {
        return createInterfaceEventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, source, nodeId, ipAddr, ifIndex, txNo).getEvent();
    }

    /**
     * Construct a nodeDeleteed event for the given nodeId
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the id of the node being deleted
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event representing a nodeDeleted event for the given node
     */
    public static Event createNodeDeletedEvent(String source, long nodeId, long txNo) {
        return createNodeEventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, source, nodeId, txNo).getEvent();
    }

    /**
     * Constructs a serviceDeleted Event for the nodeId, ipAddr, serviceName
     * triple
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            the nodeId that the service resides on
     * @param ipAddr
     *            the interface that the service resides on
     * @param service
     *            the name of the service that was deleted
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event that represents the serviceDeleted event for the give
     *         triple
     */
    public static Event createServiceDeletedEvent(String source, long nodeId, String ipAddr, String service, long txNo) {
        return createServiceEventBuilder(EventConstants.SERVICE_DELETED_EVENT_UEI, source, nodeId, ipAddr, service, txNo).getEvent();
    }

    private static EventBuilder createServiceEventBuilder(String uei, String source, long nodeId, String ipAddr, String service, long txNo) {
        EventBuilder bldr = new EventBuilder(uei, source);

        bldr.setNodeid(nodeId);
        bldr.setInterface(addr(ipAddr));
        bldr.setService(service);
        
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr;
    }

    /**
     * Get the eventId for the given event
     *
     * @param e
     *            the event to get the eventId for
     * @return the eventId of the event or -1 of no eventId is assigned
     */
    public static long getEventID(Event e) {
        // get eventid
        long eventID = -1;
        if (e.hasDbid())
            eventID = e.getDbid();
        return eventID;
    }

    /**
     * Retrieve the value associated with an event parameter and parse it to a
     * long. If the value can not be found, return a default value.
     *
     * @param e
     *            the Event to retrieve the parameter from
     * @param parmName
     *            the name of the parameter to retrieve
     * @param defaultValue
     *            the value to return if the paramter can not be retrieved or
     *            parsed
     * @return the value of the parameter as a long
     */
    public static long getLongParm(Event e, String parmName, long defaultValue) {
        return org.opennms.netmgt.model.events.EventUtils.getLongParm(e, parmName, defaultValue);
    }

    /**
     * Retrieve the value associated with an event parameter and parse it to an
     * int. If the value can not be found, return a default value.
     *
     * @param e
     *            the Event to retrieve the parameter from
     * @param parmName
     *            the name of the parameter to retrieve
     * @param defaultValue
     *            the value to return if the paramter can not be retrieved or
     *            parsed
     * @return the value of the parameter as a long
     */
    public static int getIntParm(Event e, String parmName, int defaultValue) {
        return org.opennms.netmgt.model.events.EventUtils.getIntParm(
                                                                     e,
                                                                     parmName,
                                                                     defaultValue);
    }
    
    /**
     * <p>getIntParm</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param parmName a {@link java.lang.String} object.
     * @return a int.
     */
    public static int getIntParm(Event e, String parmName) {
        return org.opennms.netmgt.model.events.EventUtils.getIntParm(e, parmName, 0);
    }

    /**
     * Return the nodeId of the node associated with and event, or -1 of no node
     * is associated.
     *
     * @param e
     *            the event
     * @return the nodeId or -1 if no nodeId is set
     */
    public static long getNodeId(Event e) {
        // convert the node id
        long nodeID = -1;
        if (e.hasNodeid())
            nodeID = e.getNodeid();
        return nodeID;
    }

    /**
     * Return the value of an event parameter of null if it does not exist.
     *
     * @param e
     *            the Event to get the parameter for
     * @param parmName
     *            the name of the parameter to retrieve
     * @return the value of the parameter, or null of the parameter is not set
     */
    public static String getParm(Event e, String parmName) {
        return org.opennms.netmgt.model.events.EventUtils.getParm(e, parmName);
    }

    /**
     * Retrieve a parameter from and event, returning defaultValue of the
     * parameter is not set.
     *
     * @param e
     *            The Event to retrieve the parameter from
     * @param parmName
     *            the name of the parameter to retrieve
     * @param defaultValue
     *            the default value to return if the parameter is not set
     * @return the value of the parameter, or defalutValue if the parameter is
     *         not set
     */
    public static String getParm(Event e, String parmName, String defaultValue) {
        return org.opennms.netmgt.model.events.EventUtils.getParm(e, parmName, defaultValue);
    }

    /**
     * Throw an exception if an event does have the required parameter
     *
     * @param e
     *            the event the parameter must reside on
     * @throws org.opennms.netmgt.capsd.InsufficientInformationException
     *             if the paramter is not set on the event or if its value has
     *             no content
     * @param parmName a {@link java.lang.String} object.
     */
    public static void requireParm(Event e, String parmName) throws InsufficientInformationException {
        for (Parm parm : e.getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                if (parm.getValue() != null && parm.getValue().getContent() != null) {
                    // we found a matching parm
                    return;
                } else {
                    throw new InsufficientInformationException("parameter " + parmName + " required but only null valued parms available");
                }
            }
        }

        throw new InsufficientInformationException("parameter " + parmName + " required but was not available");

    }

    /**
     * Send an event to the Event manaager to be broadcast to interested
     * listeners
     *
     * @param newEvent
     *            the event to send
     * @param isXmlRpcEnabled a boolean.
     * @param callerUei a {@link java.lang.String} object.
     * @param txNo a long.
     */
    public static void sendEvent(Event newEvent, String callerUei, long txNo, boolean isXmlRpcEnabled) {
        // Send event to Eventd
        ThreadCategory log = ThreadCategory.getInstance(EventUtils.class);
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

            if (log.isDebugEnabled())
                log.debug("sendEvent: successfully sent event " + newEvent);
        } catch (Throwable t) {
            log.warn("run: unexpected throwable exception caught during send to middleware", t);
            if (isXmlRpcEnabled) {
                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, callerUei, "caught unexpected throwable exception.", status, "OpenNMS.Capsd");
            }

        }
    }

    /**
     * This method is responsible for generating a nodeAdded event and sending
     * it to eventd..
     *
     * @param nodeEntry
     *            The node Added.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeAddedEvent(DbNodeEntry nodeEntry) {
        return createNodeAddedEvent(nodeEntry.getNodeId(), nodeEntry.getLabel(), String.valueOf(nodeEntry.getLabelSource()));
    }

	/**
	 * <p>createNodeAddedEvent</p>
	 *
	 * @param nodeId a int.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param labelSource a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event createNodeAddedEvent(int nodeId, String nodeLabel, String labelSource) {
        return createNodeAddedEvent("OpenNMS.Capsd", nodeId, nodeLabel, labelSource);
	}

    /**
     * <p>createNodeAddedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param labelSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeAddedEvent(String source, int nodeId, String nodeLabel, String labelSource) {
		EventBuilder bldr = createNodeEventBuilder(EventConstants.NODE_ADDED_EVENT_UEI, source, nodeId, -1);

        bldr.setHost(Capsd.getLocalHostAddress());
        
        bldr.setParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
        bldr.setParam(EventConstants.PARM_NODE_LABEL_SOURCE, labelSource);

        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a nodeGainedInterface event and
     * sending it to eventd..
     *
     * @param nodeEntry
     *            The node that gained the interface.
     * @param ifaddr
     *            the interface gained on the node.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeGainedInterfaceEvent(DbNodeEntry nodeEntry, InetAddress ifaddr) {
        return createNodeGainedInterfaceEvent("OpenNMS.Capsd", nodeEntry.getNodeId(), ifaddr);
    }

	/**
	 * <p>createNodeGainedInterfaceEvent</p>
	 *
	 * @param source a {@link java.lang.String} object.
	 * @param nodeId a int.
	 * @param ifaddr a {@link java.net.InetAddress} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event createNodeGainedInterfaceEvent(String source, int nodeId, InetAddress ifaddr) {
	    
	    EventBuilder bldr = new EventBuilder(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, source);
	    bldr.setNodeid(nodeId);
	    bldr.setHost(Capsd.getLocalHostAddress());
	    bldr.setInterface(ifaddr);
	    
	    bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifaddr.getHostName());

	    return bldr.getEvent();
	}

    /**
     * This method is responsible for generating a nodeDeleted event and sending
     * it to eventd..
     *
     * @param source
     *            A string representing the source of the event
     * @param nodeId
     *            Nodeid of the node got deleted.
     * @param hostName
     *            the Host server name.
     * @param nodeLabel
     *            the node label of the deleted node.
     * @param txNo a long.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeDeletedEvent(String source, int nodeId, String hostName, String nodeLabel, long txNo) {
        
        EventBuilder bldr = createNodeEventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, source, nodeId, txNo);
        
        if (nodeLabel != null && !"".equals(nodeLabel.trim())) {
            bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
        }
        
        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a deleteNode event and sending
     * it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeLabel
     *            the nodelabel of the deleted node.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the external transaction No of the event.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendDeleteNodeEvent(String source, String nodeLabel, String hostName, long txNo) {
        
        EventBuilder bldr = new EventBuilder(EventConstants.DELETE_NODE_EVENT_UEI, source);

        bldr.setHost(hostName);
        
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a forceRescan event and sending
     * it to eventd..
     *
     * @param hostName
     *            the Host server name.
     * @param nodeId
     *            the node ID of the node to rescan.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createForceRescanEvent(String hostName, long nodeId) {

        EventBuilder bldr = new EventBuilder(EventConstants.FORCE_RESCAN_EVENT_UEI, "OpenNMS.Capsd");
        bldr.setNodeid(nodeId);
        bldr.setHost(hostName);

        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating an interfaceDeleted event and
     * sending it to eventd...
     *
     * @param source
     *            the source of the event
     * @param nodeId
     *            Nodeid of the node that the deleted interface resides on.
     * @param ipaddr
     *            the ipaddress of the deleted Interface.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the external transaction No. of the original event.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendInterfaceDeletedEvent(String source, int nodeId, String ipaddr, String hostName, long txNo) {
        
        return createInterfaceEventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, source, nodeId, ipaddr, -1, txNo)
            .setHost(hostName)
            .getEvent();
    }

    /**
     * This method is responsible for generating a nodeGainedService event and
     * sending it to eventd..
     *
     * @param nodeEntry
     *            The node that gained the service.
     * @param ifaddr
     *            the interface gained the service.
     * @param service
     *            the service gained.
     * @param txNo
     *            the transaction no.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeGainedServiceEvent(DbNodeEntry nodeEntry, InetAddress ifaddr, String service, long txNo) {
        int nodeId = nodeEntry.getNodeId();
        String nodeLabel = nodeEntry.getLabel();
        String labelSource = String.valueOf(nodeEntry.getLabelSource());
        String sysName = nodeEntry.getSystemName();
        String sysDescr = nodeEntry.getSystemDescription();

        return createNodeGainedServiceEvent("OpenNMS.Capsd", nodeId, ifaddr, service, nodeLabel, labelSource, sysName, sysDescr);
    }

	/**
	 * <p>createNodeGainedServiceEvent</p>
	 *
	 * @param source a {@link java.lang.String} object.
	 * @param nodeId a int.
	 * @param ifaddr a {@link java.net.InetAddress} object.
	 * @param service a {@link java.lang.String} object.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param labelSource a {@link java.lang.String} object.
	 * @param sysName a {@link java.lang.String} object.
	 * @param sysDescr a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public static Event createNodeGainedServiceEvent(String source, int nodeId, InetAddress ifaddr, String service, String nodeLabel, String labelSource, String sysName, String sysDescr) {

	    EventBuilder bldr = createServiceEventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, source, nodeId, InetAddressUtils.str(ifaddr), service, -1);

        bldr.setHost(Capsd.getLocalHostAddress());
        
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifaddr.getHostName());
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, labelSource);
        
        // Add sysName if available
		if (sysName != null) {
		    bldr.addParam(EventConstants.PARM_NODE_SYSNAME, sysName);
        }

        // Add sysDescr if available
		if (sysDescr != null) {
		    bldr.addParam(EventConstants.PARM_NODE_SYSDESCRIPTION, sysDescr);
        }

        return bldr.getEvent();
	}

    /**
     * This method is responsible for generating a deleteService event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeEntry
     *            The node that the service to get deleted on.
     * @param ifaddr
     *            the interface the service to get deleted on.
     * @param service
     *            the service to delete.
     * @param hostName
     *            set to the host field in the event
     * @param txNo
     *            the transaction no.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendDeleteServiceEvent(String source, DbNodeEntry nodeEntry, InetAddress ifaddr, String service, String hostName, long txNo) {

        EventBuilder bldr = createServiceEventBuilder(EventConstants.DELETE_SERVICE_EVENT_UEI, source, nodeEntry.getNodeId(), InetAddressUtils.str(ifaddr), service, txNo);

        bldr.setHost(hostName);
        
        bldr.addParam(EventConstants.PARM_IP_HOSTNAME, ifaddr.getHostName());
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeEntry.getLabel());
        bldr.addParam(EventConstants.PARM_NODE_LABEL_SOURCE, nodeEntry.getLabelSource());

        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating an addInterface event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeLabel
     *            the node label of the node where the interface resides.
     * @param ipaddr
     *            IP address of the interface to be added.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the exteranl transaction number
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAddInterfaceEvent(String source, String nodeLabel, String ipaddr, String hostName, long txNo) {

        EventBuilder bldr = new EventBuilder(EventConstants.ADD_INTERFACE_EVENT_UEI, source);
        bldr.setInterface(addr(ipaddr));
        bldr.setHost(hostName);

        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a deleteInterface event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param nodeLabel
     *            the node label of the node where the interface resides.
     * @param ipaddr
     *            IP address of the interface to be deleted.
     * @param hostName
     *            the Host server name.
     * @param txNo
     *            the external transaction No.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createAndSendDeleteInterfaceEvent(String source, String nodeLabel, String ipaddr, String hostName, long txNo) {

        EventBuilder bldr = new EventBuilder(EventConstants.DELETE_INTERFACE_EVENT_UEI, source);
        bldr.setInterface(addr(ipaddr));
        bldr.setHost(hostName);
        
        bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr.getEvent();
    }

    /**
     * This method is responsible for generating a changeService event and
     * sending it to eventd..
     *
     * @param source
     *            the source of the event
     * @param ipaddr
     *            IP address of the interface where the service resides.
     * @param service
     *            the service to be changed(add or remove).
     * @param action
     *            what operation to perform for the service/interface pair.
     * @param hostName
     *            sets the host field of the event
     * @param txNo
     *            the external transaction No.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createChangeServiceEvent(String source, String ipaddr, String service, String action, String hostName, long txNo) {

        EventBuilder bldr = new EventBuilder(EventConstants.CHANGE_SERVICE_EVENT_UEI, source);
        bldr.setInterface(addr(ipaddr));
        bldr.setService(service);
        
        bldr.addParam(EventConstants.PARM_ACTION, action);
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr.getEvent();
    }

    /**
     * Constructs a deleteService event for the given nodeId, ipAddress,
     * serivcename triple.
     *
     * @param source
     *            the source for the event
     * @param nodeId
     *            the nodeId of the node that service resides on
     * @param ipAddr
     *            the ipAddress of the interface the service resides on
     * @param service
     *            the service that is being deleted
     * @param txNo
     *            the transaction number to use for processing this event
     * @return an Event representing a deleteInterface event for the given
     *         nodeId, ipaddr
     */
    public static Event createDeleteServiceEvent(String source, long nodeId, String ipAddr, String service, long txNo) {
        
        return createServiceEventBuilder(EventConstants.DELETE_SERVICE_EVENT_UEI, source, nodeId, ipAddr, service, txNo).getEvent();
        
    }

    /**
     * <p>toString</p>
     *
     * @deprecated Use org.opennms.netmgt.model.events.EventUtils.toString(event) instead.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Event event) {
        return org.opennms.netmgt.model.events.EventUtils.toString(event);
    }
    
    /**
     * <p>toString</p>
     *
     * @deprecated Use org.opennms.netmgt.model.events.EventUtils.toString(value) instead.
     * @param value a {@link org.opennms.netmgt.xml.event.Value} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Value value) {
        return org.opennms.netmgt.model.events.EventUtils.toString(value);
    }

    /**
     * <p>toString</p>
     *
     * @deprecated Use org.opennms.netmgt.model.events.EventUtils.toString(snmp) instead.
     * @param snmp a {@link org.opennms.netmgt.xml.event.Snmp} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Snmp snmp) {
        return org.opennms.netmgt.model.events.EventUtils.toString(snmp);
    }

	/**
	 * <p>addParam</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 * @param parmName a {@link java.lang.String} object.
	 * @param pollResultId a {@link java.lang.Object} object.
	 */
	public static void addParam(Event event, String parmName, Object pollResultId) {
	    final Parm eventParm = new Parm();
        eventParm.setParmName(parmName);
        final Value parmValue = new Value();
        parmValue.setContent(String.valueOf(pollResultId));
        eventParm.setValue(parmValue);

        event.addParm(eventParm);
	}

}
