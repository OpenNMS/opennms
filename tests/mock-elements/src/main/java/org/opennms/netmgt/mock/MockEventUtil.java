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

package org.opennms.netmgt.mock;

import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.mock.MockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger LOG = LoggerFactory.getLogger(MockEventUtil.class);
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

    public static Event createOutageCreatedEvent(String source, MockService svc, String reason) {
        return createServiceEvent(source, EventConstants.OUTAGE_CREATED_EVENT_UEI, svc, reason);
    }

    public static Event createOutageResolvedEvent(String source, MockService svc, String reason) {
        return createServiceEvent(source, EventConstants.OUTAGE_RESOLVED_EVENT_UEI, svc, reason);
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
     * <p>createNodeDownEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createNodeDownEventBuilder(String source, OnmsNode node) {
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
     * <p>createNodeEventBuilder</p>
     *
     * @param source a {@link java.lang.String} object.
     * @param uei a {@link java.lang.String} object.
     * @param node a {@link org.opennms.netmgt.mock.MockNode} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public static EventBuilder createNodeEventBuilder(String source, String uei, OnmsNode node) {
        return createEventBuilder(source, uei, node.getId(), null, null, null);
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
        event.setInterface(addr(ipAddr));
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
        
        event.setInterface(addr("1.2.3.4"));
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
        event.setTime(date);
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
        event.setInterface(addr(ipAddr));
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
     * <p>eventsMatch</p>
     *
     * @param e1 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param e2 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     * @deprecated Use {@link EventUtils#eventsMatch(Event,Event)} instead
     */
    public static boolean eventsMatch(final Event e1, final Event e2) {
        return EventUtils.eventsMatch(e1, e2);
    }
    
    /**
     * <p>eventsMatchDeep</p>
     *
     * @param e1 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param e2 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public static boolean eventsMatchDeep(Event e1, Event e2) {
        return MockEventUtil.eventsMatchDeep(e1, e2, 0);
    }

    /**
     * <p>eventsMatchDeep</p>
     *
     * @param e1 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param e2 a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean.
     */
    public static boolean eventsMatchDeep(Event e1, Event e2, long toleratedTimestampOffset) {
        if (e1.getTime() != null || e2.getTime() != null) {
            if (e1.getTime() == null || e2.getTime() == null) {
                return false;
            }
            
            if (toleratedTimestampOffset > 0) {
                final long d1 = e1.getTime().getTime();
                final long d2 = e2.getTime().getTime();
                if ((d2 - toleratedTimestampOffset) < d1 && d1 < (d2 + toleratedTimestampOffset)) {
                    // d1 is within [toleratedTimestampOffset] of d2
                } else if ((d1 - toleratedTimestampOffset) < d2 && d2 < (d1 + toleratedTimestampOffset)) {
                    // d2 is within [toleratedTimestampOffset] of d1
                } else {
                    return false;
                }
            } else if (!e1.getTime().equals(e2.getTime())) {
                return false;
            }
        }
        
        if (!EventUtils.eventsMatch(e1, e2)) {
            return false;
        }
        
        if (e1.getParmCollection() != null || e2.getParmCollection() != null) {
            if (e1.getParmCollection() == null || e2.getParmCollection() == null) {
                return false;
            }
            
            List<Parm> p1 = e1.getParmCollection();
            List<Parm> p2 = e2.getParmCollection();
            
            if (p1.size() != p2.size()) {
                return false;
            }
            
            Map<String, String> m1 = convertParmsToMap(p1);
            Map<String, String> m2 = convertParmsToMap(p2);

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
        LOG.info("{}: {}/{}/{}/{}", prefix, event.getUei(), event.getNodeid(), event.getInterface(), event.getService());
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
