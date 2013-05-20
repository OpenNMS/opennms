/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class BroadcastEventProcessorTest extends NotificationsTestCase {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        m_anticipator.setExpectedDifference(3000);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown(true);
    }

    /**
     * Test calling expandNotifParms to see if the regular expression in
     * m_notifdExpandRE is initialized from {@link BroadcastEventProcessor.NOTIFD_EXPANSION_PARM}.
     */
    @Test
    public void testExpandNotifParms() throws Exception {
        String expandResult = NotificationManager.expandNotifParms("%foo%", new TreeMap<String,String>());
        assertEquals("%foo%", expandResult);

        // This is kinda non-intuitive... but expandNotifParms() only works on whitelisted expansion params
        expandResult = NotificationManager.expandNotifParms("%foo%", Collections.singletonMap("foo", "bar"));
        assertEquals("%foo%", expandResult);

        // The 'noticeid' param is in the whitelist
        expandResult = NotificationManager.expandNotifParms("Notice #%noticeid% RESOLVED: ", Collections.singletonMap("noticeid", "999"));
        assertEquals("Notice #999 RESOLVED: ", expandResult);

        expandResult = NotificationManager.expandNotifParms("RESOLVED: ", Collections.singletonMap("noticeid", "999"));
        assertEquals("RESOLVED: ", expandResult);

        // <notification name="Disk Threshold" status="on"> from bug 2888
        expandResult = NotificationManager.expandNotifParms("Notice %noticeid%: Disk threshold exceeded on %nodelabel%: %parm[all]%.", new TreeMap<String,String>());
        assertEquals("Notice %noticeid%: Disk threshold exceeded on %nodelabel%: %parm[all]%.", expandResult);
        /*
        <event>
            <uei xmlns="">uei.opennms.org/abian/hr-dsk-full</uei>
            <event-label xmlns="">Disk Full</event-label>
            <descr xmlns="">Threshold exceeded for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%</descr>
            <logmsg dest="logndisplay">Threshold exceeded for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%</logmsg>
            <severity xmlns="">Minor</severity>
            <alarm-data reduction-key="%uei%!%nodeid%!%parm[label]%" alarm-type="1" auto-clean="false" />
        </event>
         */
        
        EventBuilder bldr = new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "testExpandNotifParms");

        bldr.setDescription("High threshold exceeded for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
        bldr.setLogMessage("High threshold exceeded for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
        bldr.setNodeid(0);
        bldr.setInterface(addr("0.0.0.0"));
        
        bldr.addParam("ds", "dsk-usr-pcent");
        bldr.addParam("value", "Crap! There's only 15% free on the SAN and we need 20%! RUN AWAY!");
        bldr.addParam("threshold", "");
        bldr.addParam("trigger", "");
        bldr.addParam("rearm", "");
        bldr.addParam("label", "");
        bldr.addParam("ifIndex", "");

        /*
        List<String> names = m_notificationManager.getNotificationNames();
        Collections.sort(names);
        for (String name : names) {
            System.out.println(name);
        }
        */
        Notification[] notifications = null;
        notifications = m_notificationManager.getNotifForEvent(null);
        assertNull(notifications);
        notifications = m_notificationManager.getNotifForEvent(bldr.getEvent());
        assertNotNull(notifications);
        assertEquals(1, notifications.length);
        Map<String,String> paramMap = BroadcastEventProcessor.buildParameterMap(notifications[0], bldr.getEvent(), 9999);
        /*
        for (Map.Entry<String,String> entry : paramMap.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
         */
        assertEquals("High disk Threshold exceeded on 0.0.0.0, dsk-usr-pcent with Crap! There's only 15% free on the SAN and we need 20%! RUN AWAY!", paramMap.get("-tm"));
        expandResult = NotificationManager.expandNotifParms("Notice #%noticeid%: Disk threshold exceeded on %nodelabel%: %parm[all]%.", paramMap);
        assertEquals("Notice #9999: Disk threshold exceeded on %nodelabel%: %parm[all]%.", expandResult);
    }

    /**
     * Trip a notification and see if the %noticeid% token gets expanded to a numeric
     * value in the subject and text message
     * 
     * @author Jeff Gehlbach <jeffg@jeffg.org>
     */
    @Test
    public void testExpandNoticeId_Bug1745() throws Exception {
        MockService svc = m_network.getService(1, "192.168.1.1", "ICMP");
        Event event = MockEventUtil.createServiceEvent("Test", "uei.opennms.org/test/noticeIdExpansion", svc, null);

        // We need to know what noticeID to expect -- whatever the NotificationManager
        // gives us, the next notice to come out will have noticeID n+1.  This isn't
        // foolproof, but it should work within the confines of JUnit as long as all
        // previous cases have torn down the mock Notifd.
        String antNID = Integer.toString(m_notificationManager.getNoticeId() + 1);

        Date testDate = new Date();
        long finishedNotifs = anticipateNotificationsForGroup("notification '" + antNID + "'", "Notification '" + antNID + "'", "InitialGroup", testDate, 0);
        MockEventUtil.setEventTime(event, testDate);

        m_eventMgr.sendEventToListeners(event);

        verifyAnticipated(finishedNotifs, 1000);
    }
}
