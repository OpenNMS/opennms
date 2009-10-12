//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Oct 01: Add ability to update database when an interface is deleted. - ayres@opennms.org
// 2004 Oct 07: Added code to support RTC rescan on asset update
// Aug 24, 2004: Created this file.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Tab Size = 8
//

package org.opennms.netmgt.linkd;

import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.utils.XmlrpcUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;

/**
 * Provides a collection of utility methods used by the DeleteEvent Processor
 * for dealing with Events
 * 
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * 
 */
public class EventUtils {

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
     * @throws InsufficientInformationException
     *             if an event id is not available
     */
    static public void checkEventId(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (!e.hasDbid()) {
            throw new InsufficientInformationException("eventID is unavailable");
        }
    }

    /**
     * Ensures the given event has an interface
     * 
     * @param e
     *            the event
     * @throws InsufficientInformationException
     *             if an interface is not available
     */
    static public void checkInterface(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (e.getInterface() == null || e.getInterface().length() == 0) {
            throw new InsufficientInformationException("ipaddr for event is unavailable");
        }
    }
    
    /**
     * Is the given interface a non-IP interface
     * 
     * @param intf
     *            the interface
     *            
     * @return true/false
     *
     */
    static public boolean isNonIpInterface(String intf) {
        if (intf == null || intf.length() == 0 || "0.0.0.0".equals(intf) ) {
            return true;
        }
        return false;
    }
   
    
    /**
     * Ensures the given event has an interface or ifIndex
     * 
     * @param e
     *            the event
     * @throws InsufficientInformationException
     *             if  neither an interface nor an ifIndex is available
     */
    static public void checkInterfaceOrIfIndex(Event e) throws InsufficientInformationException {
        if (e == null)
            throw new NullPointerException("e is null");

        if (e.getInterface() == null || e.getInterface().length() == 0) {
            if (!e.hasIfIndex()) {
                throw new InsufficientInformationException("Both ipaddr and ifIndex for event are unavailable");
            }
        }
    }

    /**
     * Ensures the given event has a host
     * 
     * @param e
     *            the event
     * @throws InsufficientInformationException
     *             if an interface is not available
     */
    static public void checkHost(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (e.getHost() == null || e.getHost().length() == 0) {
            throw new InsufficientInformationException("host for event is unavailable");
        }
    }

    /**
     * Ensures that the given Event has a node id
     * 
     * @param e
     *            the event
     * @throws InsufficientInformationException
     *             if a node id is not available
     */
    static public void checkNodeId(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (!e.hasNodeid()) {
            throw new InsufficientInformationException("nodeid for event is unavailable");
        }
    }

    /**
     * Ensures that the given event has a service parameter
     * 
     * @param e
     *            the event to check
     * @throws InsufficientInformationException
     *             if the event does not have a service
     */
    public static void checkService(Event e) throws InsufficientInformationException {
        if (e == null) {
            throw new NullPointerException("e is null");
        }

        if (e.getService() == null || e.getService().length() == 0) {
            throw new InsufficientInformationException("service for event is unavailable");
        }
    }

    /**
     * Get the eventId for the given event
     * 
     * @param e
     *            the event to get the eventId for
     * @return the eventId of the event or -1 of no eventId is assigned
     */
    public static long getEventID(Event e) {
        // get event ID
        long eventID = -1;
        if (e.hasDbid()) {
            eventID = e.getDbid();
        }
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
     *            the value to return if the parameter can not be retrieved or
     *            parsed
     * @return the value of the parameter as a long
     */
    public static long getLongParm(Event e, String parmName, long defaultValue) {
        String longVal = getParm(e, parmName);

        if (longVal == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(longVal);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
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
        if (e.hasNodeid()) {
            nodeID = e.getNodeid();
        }
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
        return getParm(e, parmName, null);
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
        if (parms == null) {
            return defaultValue;
        }

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
     * Throw an exception if an event does have the required parameter
     * 
     * @param e
     *            the event the parameter must reside on
     * @param parmname
     *            the name of the parameter
     * @throws InsufficientInformationException
     *             if the parameter is not set on the event or if its value has
     *             no content
     */
    public static void requireParm(Event e, String parmName) throws InsufficientInformationException {
        Parms parms = e.getParms();
        if (parms == null) {
            throw new InsufficientInformationException("parameter " + parmName + " required but but no parms are available.");
        }

        Enumeration<Parm> parmEnum = parms.enumerateParm();
        while (parmEnum.hasMoreElements()) {
            Parm parm = parmEnum.nextElement();
            if (parmName.equals(parm.getParmName())) {
                if (parm.getValue() != null && parm.getValue().getContent() != null) {
                    // we found a matching parameter
                    return;
                } else {
                    throw new InsufficientInformationException("parameter " + parmName + " required but only null valued parms available");
                }
            }
        }

        throw new InsufficientInformationException("parameter " + parmName + " required but was not available");

    }

    /**
     * Send an event to the Event manager to be broadcast to interested
     * listeners
     * 
     * @param newEvent
     *            the event to send
     * @param isXmlRpcEnabled
     *            whether or not an XMLRPC event should be sent
     */
    public static void sendEvent(Event newEvent, String callerUei, long txNo, boolean isXmlRpcEnabled) {
        // Send event to Eventd
        Category log = ThreadCategory.getInstance(EventUtils.class);
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(newEvent);

            if (log.isDebugEnabled()) {
                log.debug("sendEvent: successfully sent event " + newEvent);
            }
        } catch (Throwable t) {
            log.warn("run: unexpected throwable exception caught during send to middleware", t);
            if (isXmlRpcEnabled) {
                int status = EventConstants.XMLRPC_NOTIFY_FAILURE;
                XmlrpcUtil.createAndSendXmlrpcNotificationEvent(txNo, callerUei, "caught unexpected throwable exception.", status, "OpenNMS.Capsd");
            }

        }
    }

}
