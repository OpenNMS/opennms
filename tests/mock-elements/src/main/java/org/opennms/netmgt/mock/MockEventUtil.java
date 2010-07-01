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
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.test.mock.MockUtil;

/**
 * <p>Abstract MockEventUtil class.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public abstract class MockEventUtil {
    /**
     * <p>createNodeLostServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeLostServiceEvent(String source, MockService svc, String reason) {
        return createServiceEvent(source, EventConstants.NODE_LOST_SERVICE_EVENT_UEI, svc, reason);
    }
    
    /**
     * <p>createNodeLostServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeLostServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.NODE_LOST_SERVICE_EVENT_UEI, svc, null);
    }

    /**
     * <p>createNodeRegainedServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeRegainedServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, svc, null);
    }
    
    /**
     * <p>createServiceUnresponsiveEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createServiceUnresponsiveEvent(String source, MockService svc, String reason) {
        return createServiceUnresponsiveEventBuilder(source, svc, reason).getEvent();
    }
    
    /**
     * <p>createServiceUnresponsiveEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createServiceUnresponsiveEventBuilder(String source, MockService svc, String reason) {
        return createServiceEventBuilder(source, EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, svc, reason);
    }
    
    /**
     * <p>createServiceResponsiveEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createServiceResponsiveEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, svc, null);
    }
    
    /**
     * <p>createDemandPollServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @param demandPollId a int.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createDemandPollServiceEvent(String source, MockService svc, int demandPollId) {
        EventBuilder event = createServiceEventBuilder(source, EventConstants.DEMAND_POLL_SERVICE_EVENT_UEI, svc, null);
        event.addParam(EventConstants.PARM_DEMAND_POLL_ID, demandPollId);
        return event.getEvent();
    }
    
    /**
     * <p>createNodeGainedServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeGainedServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, svc, null);
    }
    
    /**
     * <p>createServiceDeletedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createServiceDeletedEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.SERVICE_DELETED_EVENT_UEI, svc, null);
    }
    
    /**
     * <p>createSuspendPollingServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createSuspendPollingServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, svc, null);
    }
    
    /**
     * <p>createResumePollingServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createResumePollingServiceEvent(String source, MockService svc) {
        return createServiceEvent(source, EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, svc, null);
    }
    
    /**
     * <p>createServiceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createServiceEvent(String source, String uei, MockService svc, String reason) {
        return createServiceEventBuilder(source, uei, svc, reason).getEvent();
    }
    
    /**
     * <p>createServiceEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param svc a {@link org.opennms.netmgt.mock.MockService} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createServiceEventBuilder(String source, String uei, MockService svc, String reason) {
        return createEventBuilder(source, uei, svc.getNodeId(), svc.getIpAddr(), svc.getSvcName(), reason);
    }
    
    /**
     * <p>createInterfaceDownEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createInterfaceDownEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.INTERFACE_DOWN_EVENT_UEI, iface);
    }
    
    /**
     * <p>createInterfaceUpEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createInterfaceUpEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.INTERFACE_UP_EVENT_UEI, iface);
    }
    
    /**
     * <p>createNodeGainedInterfaceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeGainedInterfaceEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, iface);
    }
    
    /**
     * <p>createInterfaceDeletedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createInterfaceDeletedEvent(String source, MockInterface iface) {
        return createInterfaceEvent(source, EventConstants.INTERFACE_DELETED_EVENT_UEI, iface);
    }
    
    /**
     * <p>createInterfaceEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createInterfaceEvent(String source, String uei, MockInterface iface) {
        return createInterfaceEventBuilder(source, uei, iface).getEvent();
    }
    
    /**
     * <p>createInterfaceEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createInterfaceEventBuilder(String source, String uei, MockInterface iface) {
        return createEventBuilder(source, uei, iface.getNodeId(), iface.getIpAddr(), null, null);
    }
    
    /**
     * <p>createNodeDownEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeDownEvent(String source, MockNode node) {
        return createNodeDownEventBuilder(source, node).getEvent();
    }
    
    /**
     * <p>createNodeDownEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createNodeDownEventBuilder(String source, MockNode node) {
        EventBuilder event = createNodeEventBuilder(source, EventConstants.NODE_DOWN_EVENT_UEI, node);
        event.setSeverity(OnmsSeverity.MAJOR.getLabel());
        // <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="1" auto-clean="false" />
        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("%uei%:%dpname%:%nodeid%");
        alarmData.setAlarmType(1);
        alarmData.setAutoClean(false);
        event.setAlarmData(alarmData);
        return event;
    }
    
    /**
     * <p>createNodeDownEventWithReason</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeDownEventWithReason(String source, MockNode node, String reason) {
        Event event = createNodeEventWithReason(source, EventConstants.NODE_DOWN_EVENT_UEI, node, reason);
        event.setSeverity(OnmsSeverity.MAJOR.getLabel());
        // <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="1" auto-clean="false" />
        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("%uei%:%dpname%:%nodeid%");
        alarmData.setAlarmType(1);
        alarmData.setAutoClean(false);
        event.setAlarmData(alarmData);
        return event;
    }
    
    /**
     * <p>createNodeUpEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeUpEvent(String source, MockNode node) {
        EventBuilder event = createNodeEventBuilder(source, EventConstants.NODE_UP_EVENT_UEI, node);
        event.setSeverity(OnmsSeverity.NORMAL.getLabel());
        // <alarm-data reduction-key="%uei%:%dpname%:%nodeid%" alarm-type="2" clear-key="uei.opennms.org/nodes/nodeDown:%dpname%:%nodeid%" auto-clean="false" />
        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("%uei%:%dpname%:%nodeid%");
        alarmData.setAlarmType(2);
        alarmData.setClearKey("uei.opennms.org/nodes/nodeDown:%dpname%:%nodeid%");
        alarmData.setAutoClean(false);
        event.setAlarmData(alarmData);
        return event.getEvent();
    }
    
    /**
     * <p>createNodeAddedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeAddedEvent(String source, MockNode node) {
        return createNodeEventBuilder(source, EventConstants.NODE_ADDED_EVENT_UEI, node).getEvent();
    }
    
    /**
     * <p>createNodeDeletedEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeDeletedEvent(String source, MockNode node) {
        return createNodeEventBuilder(source, EventConstants.NODE_DELETED_EVENT_UEI, node).getEvent();
    }
    
    /**
     * <p>createNodeEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeEvent(String source, String uei, MockNode node) {
        return createNodeEventBuilder(source, uei, node).getEvent();
    }
    
    /**
     * <p>createNodeEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createNodeEventBuilder(String source, String uei, MockNode node) {
        return createEventBuilder(source, uei, node.getNodeId(), null, null, null);
    }
    
    /**
     * <p>createNodeEventWithReason</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createNodeEventWithReason(String source, String uei, MockNode node, String reason) {
        return createEventBuilder(source, uei, node.getNodeId(), null, null, reason).getEvent();
    }
    
    /**
     * <p>createNewSuspectEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createNewSuspectEventBuilder(String source, String uei, String ipAddr) {
        EventBuilder event = createEventBuilder(source, uei);
        event.setInterface(ipAddr);
        return event;
    }
    
    /**
     * <p>createBgpBkTnEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param peerState a int.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createBgpBkTnEvent(String source, MockNode node, String ipAddr, int peerState) {
        EventBuilder event = createEventBuilder(source, "http://uei.opennms.org/standards/rfc1657/traps/bgpBackwardTransition", node.getNodeId(), null, null, null);
        
        event.setInterface("1.2.3.4");
        event.addParam(".1.3.6.1.2.1.15.3.1.7." + ipAddr, peerState);
        return event.getEvent();
    }
    
    /**
     * <p>setEventTime</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param date a {@link java.util.Date} object.
     */
    public static void setEventTime(Event event, Date date) {
        event.setTime(EventConstants.formatToString(date));
    }
    
    /**
     * <p>createEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createEvent(String source, String uei, int nodeId, String ipAddr, String svcName, String reason) {
        return createEventBuilder(source, uei, nodeId, ipAddr, svcName, reason).getEvent();
    }
    
    /**
     * <p>createEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createEventBuilder(String source, String uei, int nodeId, String ipAddr, String svcName, String reason) {
        
        EventBuilder event = createEventBuilder(source, uei);
        event.setNodeid(nodeId);
        event.setInterface(ipAddr);
        event.setService(svcName);
        
        if (reason != null) {
            event.addParam(EventConstants.PARM_LOSTSERVICE_REASON, reason);
        }
        return event;
    }

    /**
     * <p>createEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createEventBuilder(String source, String uei) {
        EventBuilder builder = new EventBuilder(uei, source);
        Date currentTime = new Date();
        builder.setCreationTime(currentTime);
        builder.setTime(currentTime);
        return builder;
    }

    /**
     * <p>createReparentEvent</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param oldNode a int.
     * @param newNode a int.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static Event createReparentEvent(String source, String ipAddr, int oldNode, int newNode) {
        EventBuilder event = createEventBuilder(source, EventConstants.INTERFACE_REPARENTED_EVENT_UEI, oldNode, ipAddr, null, null);
        
        event.addParam(EventConstants.PARM_OLD_NODEID, oldNode);
        event.addParam(EventConstants.PARM_NEW_NODEID, newNode);
        return event.getEvent();
    }
    
    /**
     * <p>convertEventTimeIntoTimestamp</p>
     *
     * @param eventTime a {@link java.lang.String} object.
     * @return a {@link java.sql.Timestamp} object.
     */
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


    /**
     * <p>eventsMatch</p>
     *
     * @param e1 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param e2 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
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
    
    /**
     * <p>eventsMatchDeep</p>
     *
     * @param e1 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param e2 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
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

    /**
     * <p>printEvent</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public static void printEvent(String prefix, Event event) {
        if (!MockUtil.printEnabled()) {
            return;
        }
        if (prefix == null) {
            prefix = "Event";
        }
        ThreadCategory.getInstance(MockEventUtil.class).info(prefix + ": " + event.getUei() + "/" + event.getNodeid() + "/" + event.getInterface() + "/" + event.getService());
    }

    /**
     * <p>printEvents</p>
     *
     * @param prefix a {@link java.lang.String} object.
     * @param events a {@link java.util.Collection} object.
     */
    public static void printEvents(String prefix, Collection<Event> events) {
        if (!MockUtil.printEnabled()) {
            return;
        }

        for (Event event : events) {
            printEvent(prefix, event);
        }
    }
}
