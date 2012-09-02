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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.utils.RowProcessor;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Tticket;
/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class NotifdTest extends NotificationsTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // XXX Bogus.. need to rework these tests
        m_anticipator.setExpectedDifference(5000);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * see http://bugzilla.opennms.org/cgi-bin/bugzilla/show_bug.cgi?id=1022
     * @throws Exception
     */
    @Test
    public void testWicktorBug_1022_1031() throws Exception {
        
        Date date = new Date();
        
        long finished = anticipateNotificationsForGroup("High loadavg5 Threshold exceeded", "High loadavg5 Threshold exceeded on 192.168.1.1, loadavg5 with ", "InitialGroup", date, 0);

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");
        EventBuilder e = MockEventUtil.createInterfaceEventBuilder("test", EventConstants.HIGH_THRESHOLD_EVENT_UEI, iface);
        e.setTime(date);
        e.addParam("ds", "loadavg5");
        m_eventMgr.sendEventToListeners(e.getEvent());
        
        /*
         * This is the notification config that Wicktor sent when reporting this bug.
         * 
         * We need to create and Threshold Exceeded event for loadavg5 to match his
         * notification name = "SNMP High loadavg5 Threshold Exceeded" correctly parsing the varbind.
         * 
         * What happens (he sent us a patch for this) is that the code does a return instead of a continue
         * when going through the notification names.
         */
        
        verifyAnticipated(finished, 1000);
        
        
    }

    // FIXME: latest notifd code seems to fail on this kind of notification
    // Bug 1954
    @Test
    @Ignore
    public void testNewSuspect() throws Exception {
        
        Date date = new Date();
        
        long finished = anticipateNotificationsForGroup("A new interface (10.1.1.1) has been discovered and is being queued for a services scan.", "A new interface (10.1.1.1) has been discovered and is being queued for a services scan.", "InitialGroup", date, 0);

        EventBuilder e = MockEventUtil.createNewSuspectEventBuilder("test", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "10.1.1.1");
        e.setTime(date);
        m_eventMgr.sendEventToListeners(e.getEvent());
        
        verifyAnticipated(finished, 1000);
        
    }
    
    @Test
    public void testNotifdStatus() throws Exception {
        
        //test for off status passed in config XML string
        assertEquals(m_notifdConfig.getNotificationStatus(), "on");

        //test for on status set here
        m_notifdConfig.turnNotifdOff();
        assertEquals(m_notifdConfig.getNotificationStatus(), "off");

        //test for off status set here
        m_notifdConfig.turnNotifdOn();
        assertEquals(m_notifdConfig.getNotificationStatus(), "on");
            
    }
    
    @Test
    public void testMockNotificationBasic() throws Exception {

        MockNode node = m_network.getNode(1);

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("node 1 down.", "All services are down on node 1.", "InitialGroup", downDate, 0);

        //bring node down now
        m_eventMgr.sendEventToListeners(node.createDownEvent(downDate));

        verifyAnticipated(finishedDowns, 3000);
        
        m_anticipator.reset();
        
        Date upDate = new Date();
        anticipateNotificationsForGroup("RESOLVED: node 1 down.", "RESOLVED: All services are down on node 1.", "InitialGroup", upDate, 0);
        long finishedUps = anticipateNotificationsForGroup("node 1 up.", "The node which was previously down is now up.", "UpGroup", upDate, 0);

        //bring node back up now
        m_eventMgr.sendEventToListeners(node.createUpEvent(upDate));

        verifyAnticipated(finishedUps, 3000);

    }
    
    @Test
    public void testMockNotificationInitialDelay() throws Exception {

        m_destinationPathManager.getPath("NoEscalate").setInitialDelay("1800ms");
        
        MockNode node = m_network.getNode(1);

        Date downDate = new Date(new Date().getTime()+1800);
        long finished = anticipateNotificationsForGroup("node 1 down.", "All services are down on node 1.", "InitialGroup", downDate, 0);

        m_eventMgr.sendEventToListeners(node.createDownEvent(downDate));

        verifyAnticipated(finished, 3000);

    }
    
    @Test
    public void testInterval() throws Exception {
        
        MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        
        Date date = new Date();
        
        long interval = computeInterval();

        long endTime = anticipateNotificationsForGroup("service ICMP on 192.168.1.1 down.", "Service ICMP is down on interface 192.168.1.1.", "InitialGroup", date, interval);
        
        m_eventMgr.sendEventToListeners(svc.createDownEvent(date));
        
        verifyAnticipated(endTime, 1000);
        
    }
    

    @Test
    public void testEscalate() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date now = new Date();

        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", now, 0);
        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "EscalationGroup", now.getTime()+2500, 0);

        m_eventMgr.sendEventToListeners(iface.createDownEvent(now));

        verifyAnticipated(endTime, 3000);
    }
    
    @Test
    public void testManualAcknowledge1() throws Exception {

        m_destinationPathManager.getPath("NoEscalate").setInitialDelay("2000ms");
        
        MockNode node = m_network.getNode(1);
        
        Event e = node.createDownEvent();

        m_eventMgr.sendEventToListeners(e);

        m_db.acknowledgeNoticesForEvent(e);
        
        verifyAnticipated(0, 0, 7000);
    }

    @Test
    public void testManualAcknowledge2() throws Exception {

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        m_db.acknowledgeNoticesForEvent(event);
        sleep(5000);

        verifyAnticipated(finishedDowns, 1000);
                
    }

    @Test
    public void testAutoAcknowledge1() throws Exception {

        m_destinationPathManager.getPath("NoEscalate").setInitialDelay("2000ms");
        
        MockNode node = m_network.getNode(1);
        
        Event downEvent = node.createDownEvent();
        Tticket tticket = new Tticket();
        tticket.setContent("777");
        tticket.setState("1");
        downEvent.setTticket(tticket);

        m_eventMgr.sendEventToListeners(downEvent);
        
        sleep(1000);
        Date date = new Date();
        Event upEvent = node.createUpEvent(date);
        long endTime = anticipateNotificationsForGroup("node 1 up.", "The node which was previously down is now up.", "UpGroup", date, 0);
        
        m_eventMgr.sendEventToListeners(upEvent);
                
        verifyAnticipated(endTime, 1000, 5000);
    }

    @Test
    public void testAutoAcknowledge2() throws Exception {

        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        Date date = new Date();
        Event upEvent = iface.createUpEvent(date);
        anticipateNotificationsForGroup("RESOLVED: interface 192.168.1.1 down.", "RESOLVED: All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", date, 0);
        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 up.", "The interface which was previously down is now up.", "UpGroup", date, 0);
        
        m_eventMgr.sendEventToListeners(upEvent);
                
        verifyAnticipated(endTime, 1000, 5000);
                
    }
    
    /**
     * see http://bugzilla.opennms.org/cgi-bin/bugzilla/show_bug.cgi?id=731
     * @throws Exception
     */
    @Test
    public void testBug731() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        Date date = new Date();
        Event upEvent = iface.createUpEvent(date);
        anticipateNotificationsForGroup("RESOLVED: interface 192.168.1.1 down.", "RESOLVED: All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", date, 0);
        long endTime = anticipateNotificationsForGroup("interface 192.168.1.1 up.", "The interface which was previously down is now up.", "UpGroup", date, 0);
        m_eventMgr.sendEventToListeners(upEvent);
        verifyAnticipated(endTime, 1000, 5000);
    }

    @Test
    public void testBug1114() throws Exception {
        // XXX Needing to bump up this number is bogus
        m_anticipator.setExpectedDifference(5000);
               
        MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        
        long interval = computeInterval();

        Event event = MockEventUtil.createServiceEvent("Test", "uei.opennms.org/tests/nodeTimeTest", svc, null);
        
        Date date = EventConstants.parseToDate(event.getTime());
        String dateString = DateFormat.getDateTimeInstance(DateFormat.FULL,
                       DateFormat.FULL).format(date);
        long endTime = anticipateNotificationsForGroup("time " + dateString + ".", "Timestamp: " + dateString + ".", "InitialGroup", date, interval);
  
        m_eventMgr.sendEventToListeners(event);

        // XXX Needing to decrease the end time is bogus
        verifyAnticipated(endTime - 5000, 1000);
    }
    
    @Test
    public void testRebuildParameterMap() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        
        Collection<Integer> notifIds = m_db.findNoticesForEvent(event);
        
        Notification[] notification = m_notificationManager.getNotifForEvent(event);
        
        int index = 0;
        for (Integer notifId : notifIds) {
            Map<String, String> originalMap = BroadcastEventProcessor.buildParameterMap(notification[index], event, notifId.intValue());
            
            Map<String, String> resolutionMap = new HashMap<String, String>(originalMap);
            resolutionMap.put(NotificationManager.PARAM_SUBJECT, "RESOLVED: "+resolutionMap.get(NotificationManager.PARAM_SUBJECT));
            resolutionMap.put(NotificationManager.PARAM_TEXT_MSG, "RESOLVED: "+resolutionMap.get(NotificationManager.PARAM_TEXT_MSG));
            resolutionMap.put(NotificationManager.PARAM_NUM_MSG, "RESOLVED: "+resolutionMap.get(NotificationManager.PARAM_NUM_MSG));
           
            Map<String, String> rebuiltMap = m_notifd.getBroadcastEventProcessor().rebuildParameterMap(notifId.intValue(), "RESOLVED: ", m_notifd.getConfigManager().getConfiguration().isNumericSkipResolutionPrefix());
            
            assertEquals(resolutionMap, rebuiltMap);
            
            index++;
        }
    }
    
    @Test
    public void testGetUsersNotified() throws Exception {
        MockInterface iface = m_network.getInterface(1, "192.168.1.1");

        Date downDate = new Date();
        anticipateNotificationsForGroup("interface 192.168.1.1 down.", "All services are down on interface 192.168.1.1, dot1 interface alias.", "InitialGroup", downDate, 0);

        //bring node down now
        Event event = iface.createDownEvent(downDate);
        m_eventMgr.sendEventToListeners(event);

        sleep(1000);
        
        Collection<List<String>> expectedResults = new LinkedList<List<String>>();
        Collection<String> users = getUsersInGroup("InitialGroup");
        for (String userID : users) {
            List<String> cmdList = new LinkedList<String>();
            cmdList.add(userID);
            cmdList.add("mockNotifier");
            expectedResults.add(cmdList);
        }
        
        Collection<Integer> notifIds = m_db.findNoticesForEvent(event);
        assertEquals("notification ID size", 1, notifIds.size());
        
        Integer notifId = notifIds.iterator().next();
        assertNotNull("first notifId should not be null", notifId);
        
        final Collection<List<String>> actualResults = new LinkedList<List<String>>();
        RowProcessor rp = new RowProcessor() {
            public void processRow(ResultSet rs) throws SQLException {
                List<String> cmdList = new LinkedList<String>();
                cmdList.add(rs.getString("userID"));
                cmdList.add(rs.getString("media"));
                actualResults.add(cmdList);
            }
        };
        m_notificationManager.forEachUserNotification(notifId.intValue(), rp);

        /*
         * This test does not work reliably because notifications within a
         * group are not guaranteed to be in a certain order.
         */
        //assertEquals("Notifications", expectedResults, actualResults);
        
        // Use a set instead so we don't care about ordering.
        Set<List<String>> expectedSet = new HashSet<List<String>>(expectedResults);
        Set<List<String>> actualSet = new HashSet<List<String>>(actualResults);

        assertEquals("Notifications as a set", expectedSet, actualSet);

    }

    @Test
    public void testRoleNotification() throws Exception {
        
        MockNode node = m_network.getNode(1);

        Date downDate = new Date();
        long finishedDowns = anticipateNotificationsForRole("notification test", "Notification Test", "oncall", downDate, 0);

        m_eventMgr.sendEventToListeners(MockEventUtil.createNodeEvent("Test", "uei.opennms.org/test/roleTestEvent", node));

        verifyAnticipated(finishedDowns, 1000);
        
        m_anticipator.reset();
        
        
    }

}
