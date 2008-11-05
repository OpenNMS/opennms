/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.events;

import static org.opennms.netmgt.EventConstants.INTERFACE_DELETED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_ADDED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_DELETED_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.NODE_GAINED_SERVICE_EVENT_UEI;
import static org.opennms.netmgt.EventConstants.PARM_IP_HOSTNAME;
import static org.opennms.netmgt.EventConstants.PARM_NODE_LABEL;
import static org.opennms.netmgt.EventConstants.PARM_NODE_LABEL_SOURCE;
import static org.opennms.netmgt.EventConstants.PARM_NODE_SYSDESCRIPTION;
import static org.opennms.netmgt.EventConstants.PARM_NODE_SYSNAME;
import static org.opennms.netmgt.EventConstants.SERVICE_DELETED_EVENT_UEI;

import java.net.InetAddress;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;

public abstract class EventUtils {
    
    public static Event createNodeAddedEvent(String source, int nodeId,
            String nodeLabel, String labelSource) {
        
        debug("CreateNodeAddedEvent: nodedId: %d", nodeId);
        
        EventBuilder bldr = new EventBuilder(NODE_ADDED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.addParam(PARM_NODE_LABEL, nodeLabel);
        bldr.addParam(PARM_NODE_LABEL_SOURCE, labelSource);
        
        return bldr.getEvent();

    }


    public static Event createNodeGainedInterfaceEvent(String source, int nodeId, InetAddress ifaddr) {
        
        debug("createNodeGainedInterfaceEvent:  %d / %s", nodeId, ifaddr.getHostAddress());
        
        EventBuilder bldr = new EventBuilder(NODE_GAINED_SERVICE_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ifaddr.getHostAddress());
        bldr.addParam(PARM_IP_HOSTNAME, ifaddr.getHostName());

        return bldr.getEvent();
    }
    
    public static Event createNodeGainedServiceEvent(String source, int nodeId, InetAddress ifaddr, String service, String nodeLabel, String labelSource, String sysName, String sysDescr) {
        
        debug("createAndSendNodeGainedServiceEvent:  nodeId/interface/service  %d/%s/%s", nodeId, ifaddr.getHostAddress(), service);

        EventBuilder bldr = new EventBuilder(NODE_GAINED_SERVICE_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ifaddr.getHostAddress());
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
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event represent an interfaceDeleted event for the given
     *         interface
     */
    public static Event createInterfaceDeletedEvent(String source, int nodeId, String ipAddr) {
        
        debug("createInterfaceDeletedEvent for nodeid/ipaddr:  %d/%s", nodeId, ipAddr);

        EventBuilder bldr = new EventBuilder(INTERFACE_DELETED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ipAddr);
        
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
     * @param txNo
     *            a transaction number associated with the event
     * @return an Event that represents the serviceDeleted event for the give
     *         triple
     */
    public static Event createServiceDeletedEvent(String source, int nodeId, String ipAddr, String service) {

        debug("createServiceDeletedEvent for nodeid/ipaddr/service:  %d/%s", nodeId, ipAddr, service);

        EventBuilder bldr = new EventBuilder(SERVICE_DELETED_EVENT_UEI, source);
        bldr.setNodeid(nodeId);
        bldr.setInterface(ipAddr);
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
        Parms parms = e.getParms();
        if (parms == null)
            return defaultValue;
    
        Enumeration<Parm> parmEnum = parms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = parmEnum.nextElement();
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
        Category log = ThreadCategory.getInstance(EventUtils.class);
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }
    

}
