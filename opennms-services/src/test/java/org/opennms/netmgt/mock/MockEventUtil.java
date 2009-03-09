//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Apr 29: Add eventsMatchDeep. - dj@opennms.org
// 2008 Feb 09: Indent, organize imports, use Java 5 generics and loops. - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.mock;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.mock.MockUtil;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MockEventUtil {
    public static void addEventParm(Event event, String parmName, int parmValue) {
        addEventParm(event, parmName, String.valueOf(parmValue));
    }

    public static void addEventParm(Event event, String parmName, String parmValue) {
        Parms parms = event.getParms();
        if (parms == null) {
            parms = new Parms();
            event.setParms(parms);
        }
        Parm parm = new Parm();
        parm.setParmName(parmName);
        Value value = new Value();
        value.setContent(parmValue);
        parm.setValue(value);
        parms.addParm(parm);
    }
    
    public static Event createNodeLostServiceEvent(String source, MockService svc, String reason) {
        return createServiceEvent(source, EventConstants.NODE_LOST_SERVICE_EVENT_UEI, svc, reason);
    }
    
    public static Event createNodeLostServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.NODE_LOST_SERVICE_EVENT_UEI, svc, null);
    }

    public static Event createNodeRegainedServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, svc, null);
    }
    
    public static Event createServiceUnresponsiveEvent(String source, MockService svc, String reason) {
        return createServiceEvent(source, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, svc, reason);
    }

    public static Event createServiceResponsiveEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, svc, null);
    }
    
    public static Event createDemandPollServiceEvent(String source, MockService svc, int demandPollId) {
        Event event = createServiceEvent(source, EventConstants.DEMAND_POLL_SERVICE_EVENT_UEI, svc, null);
        addEventParm(event, EventConstants.PARM_DEMAND_POLL_ID, demandPollId);
        return event;
    }

    
    public static Event createNodeGainedServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, svc, null);
    }
    
    public static Event createServiceDeletedEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.SERVICE_DELETED_EVENT_UEI, svc, null);
    }
    
    public static Event createSuspendPollingServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, svc, null);
    }
    
    public static Event createResumePollingServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, svc, null);
    }
    
    public static Event createServiceEvent(String source, String uei, MockService svc, String reason) {
        return createEvent(source, uei, svc.getNodeId(), svc.getIpAddr(), svc.getSvcName(), reason);
    }
    
    public static Event createInterfaceDownEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.INTERFACE_DOWN_EVENT_UEI, iface);
    }
    
    public static Event createInterfaceUpEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.INTERFACE_UP_EVENT_UEI, iface);
    }
    
    public static Event createNodeGainedInterfaceEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, iface);
    }
    
    public static Event createInterfaceDeletedEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.INTERFACE_DELETED_EVENT_UEI, iface);
    }
    
    public static Event createInterfaceEvent(String source, String uei, MockInterface iface) {
        return createEvent(source, uei, iface.getNodeId(), iface.getIpAddr(), null, null);
    }
    
    public static Event createNodeDownEvent(String source, MockNode node) {
        return createNodeEvent(source, EventConstants.NODE_DOWN_EVENT_UEI, node);
    }
    
    public static Event createNodeDownEventWithReason(String source, MockNode node, String reason) {
        return createNodeEventWithReason(source, EventConstants.NODE_DOWN_EVENT_UEI, node, reason);
    }
    
    public static Event createNodeUpEvent(String source, MockNode node) {
        return createNodeEvent(source, EventConstants.NODE_UP_EVENT_UEI, node);
    }
    
    public static Event createNodeAddedEvent(String source, MockNode node) {
        return createNodeEvent(source, EventConstants.NODE_ADDED_EVENT_UEI, node);
    }
    
    public static Event createNodeDeletedEvent(String source, MockNode node) {
        return createNodeEvent(source, EventConstants.NODE_DELETED_EVENT_UEI, node);
    }
    
    public static Event createNodeEvent(String source, String uei, MockNode node) {
        return createEvent(source, uei, node.getNodeId(), null, null, null);
    }
    
    public static Event createNodeEventWithReason(String source, String uei, MockNode node, String reason) {
        return createEvent(source, uei, node.getNodeId(), null, null, reason);
    }
    
    public static Event createNewSuspectEvent(String source, String uei, String ipAddr) {
        Event event = createEvent(source, uei);
        
        event.setInterface(ipAddr);
        return event;
    }
    
    public static Event createBgpBkTnEvent(String source, MockNode node, String ipAddr, int peerState) {
        Event event = createEvent(source, "http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition", node.getNodeId(), null, null, null);
        
        event.setInterface("1.2.3.4");
        addEventParm(event, ".1.3.6.1.2.1.15.3.1.7." + ipAddr, peerState);
        return event;
    }
    
    public static void setEventTime(Event event, Date date) {
        event.setTime(EventConstants.formatToString(date));
    }
    
    public static Event createEvent(String source, String uei, int nodeId, String ipAddr, String svcName, String reason) {
        
        Event event = createEvent(source, uei);
        event.setNodeid(nodeId);
        event.setInterface(ipAddr);
        event.setService(svcName);
        
        if (reason != null) {
            Parms eventParms = new Parms();
            Parm eventParm = new Parm();
            Value parmValue = new Value();
            eventParm.setParmName(EventConstants.PARM_LOSTSERVICE_REASON);
            parmValue.setContent(reason);
            eventParm.setValue(parmValue);
            eventParms.addParm(eventParm);
            event.setParms(eventParms);
        }
        return event;
    }

    public static Event createEvent(String source, String uei) {
        Event event = new Event();
        event.setSource(source);
        event.setUei(uei);
        String eventTime = EventConstants.formatToString(new Date());
        event.setCreationTime(eventTime);
        event.setTime(eventTime);
        return event;
    }

    public static Event createReparentEvent(String source, String ipAddr, int oldNode, int newNode) {
        Event event = createEvent(source, EventConstants.INTERFACE_REPARENTED_EVENT_UEI, oldNode, ipAddr, null, null);
        
        addEventParm(event, EventConstants.PARM_OLD_NODEID, oldNode);
        addEventParm(event, EventConstants.PARM_NEW_NODEID, newNode);
        return event;
    }
    
    public static Timestamp convertEventTimeIntoTimestamp(String eventTime) {
        Timestamp timestamp = null;
        try {
            Date date = EventConstants.parseToDate(eventTime);
            timestamp = new Timestamp(date.getTime());
        } catch (ParseException e) {
            ThreadCategory.getInstance(MockEventUtil.class).warn("Failed to convert event time " + eventTime + " to timestamp.", e);
    
            timestamp = new Timestamp((new Date()).getTime());
        }
        return timestamp;
    }


    public static boolean eventsMatch(Event e1, Event e2) {
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
    
    public static boolean eventsMatchDeep(Event e1, Event e2) {
        if (e1.getTime() != null || e2.getTime() != null) {
            if (e1.getTime() == null || e2.getTime() == null) {
                return false;
            }
            
            if (!e1.getTime().equals(e2.getTime())) {
                return false;
            }
        }
        
        if (!eventsMatch(e1, e2)) {
            return false;
        }
        
        if (e1.getParms() != null || e2.getParms() != null) {
            if (e1.getParms() == null || e2.getParms() == null) {
                return false;
            }
            
            Parms p1 = e1.getParms();
            Parms p2 = e2.getParms();
            
            if (p1.getParmCount() != p2.getParmCount()) {
                return false;
            }
            
            Map<String, String> m1 = convertParmsToMap(p1.getParmCollection());
            Map<String, String> m2 = convertParmsToMap(p2.getParmCollection());

            if (!m1.equals(m2)) {
                return false;
            }
        }
        
        return true;
    }

    private static Map<String, String> convertParmsToMap(List<Parm> parms) {
        Map<String, String> map = new HashMap<String, String>();
        for (Parm p : parms) {
            // XXX not doing encoding or type!
            map.put(p.getParmName(), p.getValue().getContent());
        }
        
        return map;
    }

    public static void printEvent(String prefix, Event event) {
        if (!MockUtil.printEnabled()) {
            return;
        }
        if (prefix == null) {
            prefix = "Event";
        }
        ThreadCategory.getInstance(MockEventUtil.class).info(prefix + ": " + event.getUei() + "/" + event.getNodeid() + "/" + event.getInterface() + "/" + event.getService());
    }

    public static void printEvents(String prefix, Collection<Event> events) {
        if (!MockUtil.printEnabled()) {
            return;
        }

        for (Event event : events) {
            printEvent(prefix, event);
        }
    }



}
