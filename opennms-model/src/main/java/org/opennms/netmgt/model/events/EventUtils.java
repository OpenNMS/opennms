/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.core.utils.InetAddressUtils.str;
import static org.opennms.netmgt.events.api.EventConstants.INTERFACE_DELETED_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_ADDED_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_DELETED_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_GAINED_SERVICE_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.NODE_UPDATED_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.PARM_IP_HOSTNAME;
import static org.opennms.netmgt.events.api.EventConstants.PARM_NODE_LABEL;
import static org.opennms.netmgt.events.api.EventConstants.PARM_NODE_LABEL_SOURCE;
import static org.opennms.netmgt.events.api.EventConstants.PARM_NODE_SYSDESCRIPTION;
import static org.opennms.netmgt.events.api.EventConstants.PARM_NODE_SYSNAME;
import static org.opennms.netmgt.events.api.EventConstants.PARM_RESCAN_EXISTING;
import static org.opennms.netmgt.events.api.EventConstants.SERVICE_DELETED_EVENT_UEI;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.xml.event.Autoaction;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Forward;
import org.opennms.netmgt.xml.event.Operaction;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Script;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract EventUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class EventUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventUtils.class);

    
    /**
     * <p>createNodeAddedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param labelSource a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeAddedEvent(String source, int nodeId, String nodeLabel, NodeLabelSource labelSource) {
        
        debug("CreateNodeAddedEvent: nodedId: %d", nodeId);
        
        EventBuilder bldr = new EventBuilder(NODE_ADDED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.addParam(PARM_NODE_LABEL, WebSecurityUtils.sanitizeString(nodeLabel));
        if (labelSource != null) {
            bldr.addParam(PARM_NODE_LABEL_SOURCE, labelSource.toString());
        }
        
        return bldr.getEvent();

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
        
        debug("createNodeGainedInterfaceEvent:  %d / %s", nodeId, str(ifaddr));
        
        EventBuilder bldr = new EventBuilder(NODE_GAINED_INTERFACE_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ifaddr);
        bldr.addParam(PARM_IP_HOSTNAME, ifaddr.getHostName());

        return bldr.getEvent();
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
    public static Event createNodeGainedServiceEvent(String source, int nodeId, InetAddress ifaddr, String service, String nodeLabel, NodeLabelSource labelSource, String sysName, String sysDescr) {
        
        debug("createAndSendNodeGainedServiceEvent:  nodeId/interface/service  %d/%s/%s", nodeId, str(ifaddr), service);

        EventBuilder bldr = new EventBuilder(NODE_GAINED_SERVICE_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ifaddr);
        bldr.setService(service);
        bldr.setParam(PARM_IP_HOSTNAME, ifaddr.getHostName());
        bldr.setParam(PARM_NODE_LABEL, nodeLabel);
        if (labelSource != null) {
            bldr.setParam(PARM_NODE_LABEL_SOURCE, labelSource.toString());
        }

        // Add sysName if available
        if (sysName != null) {
            bldr.setParam(PARM_NODE_SYSNAME, sysName);
        }

        // Add sysDescr if available
        if (sysDescr != null) {
            bldr.setParam(PARM_NODE_SYSDESCRIPTION, sysDescr);
        }

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
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeDeletedEvent(String source, int nodeId, String hostName, String nodeLabel) {
        
        debug("createNodeDeletedEvent for nodeid:  %d", nodeId);

        EventBuilder bldr = new EventBuilder(NODE_DELETED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setHost(hostName);

        if (nodeLabel != null) {
            bldr.addParam(PARM_NODE_LABEL, nodeLabel);
        }

        return bldr.getEvent();
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
     * @return an Event represent an interfaceDeleted event for the given
     *         interface
     */
    public static Event createInterfaceDeletedEvent(String source, int nodeId, InetAddress addr) {
        debug("createInterfaceDeletedEvent for nodeid/ipaddr:  %d/%s", nodeId, str(addr));

        EventBuilder bldr = new EventBuilder(INTERFACE_DELETED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(addr);
        
        return bldr.getEvent();
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
     * @return an Event that represents the serviceDeleted event for the give
     *         triple
     */
    public static Event createServiceDeletedEvent(String source, int nodeId, InetAddress addr, String service) {
        debug("createServiceDeletedEvent for nodeid/ipaddr/service:  %d/%s", nodeId, str(addr), service);

        EventBuilder bldr = new EventBuilder(SERVICE_DELETED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(addr);
        bldr.setService(service);

        return bldr.getEvent();
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
        String longVal = EventUtils.getParm(e, parmName);
    
        if (longVal == null)
            return defaultValue;
    
        try {
            return Long.parseLong(longVal);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
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
        String intVal = EventUtils.getParm(e, parmName);
    
        if (intVal == null)
            return defaultValue;
    
        try {
            return Integer.parseInt(intVal);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
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
        if (e.getParmCollection().size() < 1)
            return defaultValue;
    
        for (Parm parm : e.getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                if (parm.getValue() != null && parm.getValue().getContent() != null) {
                    return parm.getValue().getContent();
                } else {
                    return defaultValue;
                }
            }
        }
    
        return defaultValue;
    
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
        return EventUtils.getParm(e, parmName, null);
    }
    
    private static void debug(String format, Object... args) {
            LOG.debug(String.format(format, args));
    }


    /**
     * <p>createNodeUpdatedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param nodeId a {@link java.lang.Integer} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param labelSource a {@link java.lang.String} object.
     * @param rescanExisting a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeUpdatedEvent(String source, Integer nodeId, String nodeLabel, NodeLabelSource labelSource, String rescanExisting) {
        debug("CreateNodeUpdatedEvent: nodedId: %d", nodeId);
        EventBuilder bldr = new EventBuilder(NODE_UPDATED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.addParam(PARM_NODE_LABEL, nodeLabel);
        if (labelSource != null) {
            bldr.addParam(PARM_NODE_LABEL_SOURCE, labelSource.toString());
        }
        if (rescanExisting != null) {
            bldr.addParam(PARM_RESCAN_EXISTING, rescanExisting);
        }
        return bldr.getEvent();
    }


    public static Event createNodeCategoryMembershipChangedEvent(final String source, final Integer nodeId, final String nodeLabel, String[] categoriesAdded, String[] categoriesDeleted) {
        EventBuilder bldr = new EventBuilder(NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.addParam(PARM_NODE_LABEL, nodeLabel);
        if (categoriesAdded != null && categoriesAdded.length > 0) {
            bldr.addParam(EventConstants.PARM_CATEGORIES_ADDED, String.join(",", categoriesAdded));
        }
        if (categoriesDeleted != null && categoriesDeleted.length > 0) {
            bldr.addParam(EventConstants.PARM_CATEGORIES_DELETED, String.join(",", categoriesDeleted));
        }
        return bldr.getEvent();
    }

    /**
     * <p>toString</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Event event) {
        final StringBuilder b = new StringBuilder("Event: ");
        b.append("\n");
        if (event.getAutoacknowledge() != null) {
            b.append(" Autoacknowledge: " + event.getAutoacknowledge() + "\n");
        }
        if (event.getAutoactionCount() > 0) {
            b.append(" Autoactions:");
            for (Iterator<Autoaction> i = event.getAutoactionCollection().iterator(); i.hasNext(); ) {
                b.append(" " + i.next().toString());
            }
            b.append("\n");
        }
        if (event.getCreationTime() != null) {
            b.append(" CreationTime: " + event.getCreationTime() + "\n");
        }
        b.append(" Dbid: " + event.getDbid() + "\n");
        if (event.getDescr() != null) {
            b.append(" Descr: " + event.getDescr() + "\n");
        }
        if (event.getDistPoller() != null) {
            b.append(" DistPoller: " + event.getDistPoller() + "\n");
        }
        if (event.getForwardCount() > 0) {
            b.append(" Forwards:");
            for (Iterator<Forward> i = event.getForwardCollection().iterator(); i.hasNext(); ) {
                b.append(" " + i.next().toString());
            }
            b.append("\n");
        }
        if (event.getHost() != null) {
            b.append(" Host: " + event.getHost() + "\n");
        }
        if (event.getInterface() != null) {
            b.append(" Interface: " + event.getInterface() + "\n");
        }
        if (event.getLoggroupCount() > 0) {
            b.append(" Loggroup:");
            for (Iterator<String> i = event.getLoggroupCollection().iterator(); i.hasNext(); ) {
                b.append(" " + i.next());
            }
            b.append("\n");
        }
        if (event.getLogmsg() != null) {
            b.append(" Logmsg: " + event.getLogmsg() + "\n");
        }
        if (event.getMask() != null) {
            b.append(" Mask: " + event.getMask() + "\n");
        }
        if (event.getMasterStation() != null) {
            b.append(" MasterStation: " + event.getMasterStation() + "\n");
        }
        if (event.getMouseovertext() != null) {
            b.append(" Mouseovertext: " + event.getMouseovertext() + "\n");
        }
        b.append(" Nodeid: " + event.getNodeid() + "\n");
        if (event.getOperactionCount() > 0) {
            b.append(" Operaction:");
            for (Iterator<Operaction> i = event.getOperactionCollection().iterator(); i.hasNext(); ) {
                b.append(" " + i.next().toString());
            }
            b.append("\n");
        }
        if (event.getOperinstruct() != null) {
            b.append(" Operinstruct: " + event.getOperinstruct() + "\n");
        }
        if (event.getParmCollection().size() > 0) {
            b.append(" Parms: " + toString(event.getParmCollection()) + "\n");
        }
        if (event.getScriptCount() > 0) {
            b.append(" Script:");
            for (Iterator<Script> i = event.getScriptCollection().iterator(); i.hasNext(); ) {
                b.append(" " + i.next().toString());
            }
            b.append("\n");
        }
        if (event.getService() != null) {
            b.append(" Service: " + event.getService() + "\n");
        }
        if (event.getSeverity() != null) {
            b.append(" Severity: " + event.getSeverity() + "\n");
        }
        if (event.getSnmp() != null) {
            b.append(" Snmp: " + toString(event.getSnmp()) + "\n");
        }
        if (event.getSnmphost() != null) {
            b.append(" Snmphost: " + event.getSnmphost() + "\n");
        }
        if (event.getSource() != null) {
            b.append(" Source: " + event.getSource() + "\n");
        }
        if (event.getTime() != null) {
            b.append(" Time: " + event.getTime() + "\n");
        }
        if (event.getTticket() != null) {
            b.append(" Tticket: " + event.getTticket() + "\n");
        }
        if (event.getUei() != null) {
            b.append(" Uei: " + event.getUei() + "\n");
        }
        if (event.getUuid() != null) {
            b.append(" Uuid: " + event.getUuid() + "\n");
        }
        
        b.append("End Event\n");
        return b.toString();
    }

    public static String toString(Collection<Parm> parms) {
        if (parms.size() == 0) {
            return "{}\n";
        }
        
        final StringBuilder b = new StringBuilder();
        b.append("{\n");
        for (Parm p : parms) {
            b.append("  ");
            b.append(p.getParmName());
            b.append(" = ");
            b.append(toString(p.getValue()));
            b.append("\n");
        }
        b.append(" }");
        return b.toString();
    }
    
    /**
     * <p>toString</p>
     *
     * @param value a {@link org.opennms.netmgt.xml.event.Value} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Value value) {
        return value.getType() + "(" + value.getEncoding() + "): " + value.getContent();
    }

    /**
     * <p>toString</p>
     *
     * @param snmp a {@link org.opennms.netmgt.xml.event.Snmp} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Snmp snmp) {
        StringBuffer b = new StringBuffer("Snmp: ");
    
        if (snmp.getVersion() != null) {
            b.append("Version: " + snmp.getVersion() + "\n");
        }
        
        b.append("TimeStamp: " + new Date(snmp.getTimeStamp()) + "\n");
        
        if (snmp.getCommunity() != null) {
            b.append("Community: " + snmp.getCommunity() + "\n");
        }
    
        b.append("Generic: " + snmp.getGeneric() + "\n");
        b.append("Specific: " + snmp.getSpecific() + "\n");
        
        if (snmp.getId() != null) {
            b.append("Id: " + snmp.getId() + "\n");
        }
        if (snmp.getIdtext() != null) {
            b.append("Idtext: " + snmp.getIdtext() + "\n");
        }
        
        b.append("End Snmp\n");
        return b.toString();
    }


    /**
     * <p>eventsMatch</p>
     *
     * @param e1 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param e2 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public static boolean eventsMatch(final Event e1, final Event e2) {
        if (e1 == e2) {
            return true;
        }
        if (e1 == null || e2 == null) {
            return false;
        }
        if (!Objects.equals(e1.getUei(), e2.getUei())) {
            return false;
        }
        if (!Objects.equals(e1.getNodeid(), e2.getNodeid())) {
            return false;
        }
        if (!Objects.equals(e1.getInterface(), e2.getInterface())) {
            return false;
        }
        if (!Objects.equals(e1.getService(), e2.getService())) {
            return false;
        }
    
        return true;
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
            throw new NullPointerException("Event is null");
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

    private static EventBuilder createServiceEventBuilder(String uei, String source, long nodeId, String ipAddr, String service, long txNo) {
        EventBuilder bldr = new EventBuilder(uei, source);

        bldr.setNodeid(nodeId);
        bldr.setInterface(addr(ipAddr));
        bldr.setService(service);
        
        bldr.addParam(EventConstants.PARM_TRANSACTION_NO, txNo);

        return bldr;
    }

    /**
     * Constructs a deleteService event for the given nodeId, ipAddress,
     * serviceName triple.
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

}
