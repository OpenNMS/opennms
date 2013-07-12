/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events;

import static org.opennms.core.utils.InetAddressUtils.str;
import static org.opennms.netmgt.EventConstants.INTERFACE_DELETED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_ADDED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_DELETED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_GAINED_SERVICE_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_UPDATED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.PARM_IP_HOSTNAME;
import static org.opennms.netmgt.EventConstants.PARM_NODE_LABEL;
import static org.opennms.netmgt.EventConstants.PARM_NODE_LABEL_SOURCE;
import static org.opennms.netmgt.EventConstants.PARM_NODE_SYSDESCRIPTION;
import static org.opennms.netmgt.EventConstants.PARM_NODE_SYSNAME;
import static org.opennms.netmgt.EventConstants.SERVICE_DELETED_EVENT_UEI;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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
    public static Event createNodeAddedEvent(String source, int nodeId, String nodeLabel, String labelSource) {
        
        debug("CreateNodeAddedEvent: nodedId: %d", nodeId);
        
        EventBuilder bldr = new EventBuilder(NODE_ADDED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.addParam(PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(PARM_NODE_LABEL_SOURCE, labelSource);
        
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
    public static Event createNodeGainedServiceEvent(String source, int nodeId, InetAddress ifaddr, String service, String nodeLabel, String labelSource, String sysName, String sysDescr) {
        
        debug("createAndSendNodeGainedServiceEvent:  nodeId/interface/service  %d/%s/%s", nodeId, str(ifaddr), service);

        EventBuilder bldr = new EventBuilder(NODE_GAINED_SERVICE_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ifaddr);
        bldr.setService(service);
        bldr.setParam(PARM_IP_HOSTNAME, ifaddr.getHostName());
        bldr.setParam(PARM_NODE_LABEL, nodeLabel);
        bldr.setParam(PARM_NODE_LABEL_SOURCE, labelSource);

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
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeUpdatedEvent(String source, Integer nodeId, String nodeLabel, String labelSource) {
        debug("CreateNodeUpdatedEvent: nodedId: %d", nodeId);
        EventBuilder bldr = new EventBuilder(NODE_UPDATED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.addParam(PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(PARM_NODE_LABEL_SOURCE, labelSource);
        return bldr.getEvent();
    }

    /**
     * <p>toString</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toString(Event event) {
        StringBuffer b = new StringBuffer("Event: ");
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
            return "Parms: (none)\n";
        }
        
        StringBuffer b = new StringBuffer();
        b.append("Parms:\n");
        for (Parm p : parms) {
            b.append(" ");
            b.append(p.getParmName());
            b.append(" = ");
            b.append(toString(p.getValue()));
            b.append("\n");
        }
        b.append("End Parms\n");
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
        if (e1.getUei() != e2.getUei() && (e1.getUei() == null || e2.getUei() == null || !e1.getUei().equals(e2.getUei()))) {
        		return false;
        }
    
        if (e1.getNodeid() != e2.getNodeid()) {
            return false;
        }
        if (e1.getInterface() != e2.getInterface() && (e1.getInterface() == null || e2.getInterface() == null || !e1.getInterface().equals(e2.getInterface()))) {
            return false;
        }
        if (e1.getService() != e2.getService() && (e1.getService() == null || e2.getService() == null || !e1.getService().equals(e2.getService()))) {
            return false;
        }
    
        return true;
    }

}
