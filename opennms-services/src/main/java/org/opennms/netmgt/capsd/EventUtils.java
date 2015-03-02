/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

/*
 * TODO
 * Tremendous amount of code duplication that should be refactored.
 */
package org.opennms.netmgt.capsd;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xmlrpcd.XmlrpcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a collection of utility methods used by the DeleteEvent Processor
 * for dealing with Events
 *
 * @author brozow
 */
public abstract class EventUtils {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(EventUtils.class);

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
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

            LOG.debug("sendEvent: successfully sent event {}", newEvent);
        } catch (Throwable t) {
            LOG.warn("run: unexpected throwable exception caught during send to middleware", t);
            if (isXmlRpcEnabled) {
                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, callerUei, "caught unexpected throwable exception.", status, "OpenNMS.Capsd");
            }

        }
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
}
