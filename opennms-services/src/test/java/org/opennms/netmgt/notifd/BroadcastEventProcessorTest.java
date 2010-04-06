//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 25: Initialize DatabaseSchemaConfigFactory. - dj@opennms.org
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
package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public class BroadcastEventProcessorTest extends NotificationsTestCase {

    private BroadcastEventProcessor m_processor;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        m_processor = new BroadcastEventProcessor();
        m_processor.initExpandRe();

        m_anticipator.setExpectedDifference(3000);
    }

    @After
    public void tearDown() throws Exception {
        // super.tearDown();
    }

    /**
     * Test calling expandNotifParms to see if the regular expression in
     * m_notifdExpandRE is initialized from {@link BroadcastEventProcessor.NOTIFD_EXPANSION_PARM}.
     */
    @Test
    public void testExpandNotifParms() throws Exception {
        String expandResult = BroadcastEventProcessor.expandNotifParms("%foo%", new TreeMap<String,String>());
        assertEquals("%foo%", expandResult);
        // <notification name="Disk Threshold" status="on"> from bug 2888
        expandResult = BroadcastEventProcessor.expandNotifParms("Notice %noticeid%: Disk threshold exceeded on %nodelabel%: %parm[all]%.", new TreeMap<String,String>());
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
        Event event = new Event();
        event.setUei("uei.opennms.org/threshold/highThresholdExceeded");
        event.setDescr("High threshold exceeded for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
        Logmsg logmsg = new Logmsg();
        logmsg.setContent("High threshold exceeded for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%");
        logmsg.setNotify(true);
        event.setLogmsg(logmsg);
        event.setNodeid(0);
        event.setInterface("0.0.0.0");

        Parms parms = new Parms();

        Parm parm = new Parm();
        parm.setParmName("ds");
        Value value = new Value();
        value.setContent("dsk-usr-pcent");
        parm.setValue(value);
        parms.addParm(parm);

        parm = new Parm();
        parm.setParmName("value");
        value = new Value();
        value.setContent("Crap! There's only 15% free on the SAN and we need 20%! RUN AWAY!");
        parm.setValue(value);
        parms.addParm(parm);

        parm = new Parm();
        parm.setParmName("threshold");
        value = new Value();
        value.setContent("");
        parm.setValue(value);
        parms.addParm(parm);

        parm = new Parm();
        parm.setParmName("trigger");
        value = new Value();
        value.setContent("");
        parm.setValue(value);
        parms.addParm(parm);

        parm = new Parm();
        parm.setParmName("rearm");
        value = new Value();
        value.setContent("");
        parm.setValue(value);
        parms.addParm(parm);

        parm = new Parm();
        parm.setParmName("label");
        value = new Value();
        value.setContent("");
        parm.setValue(value);
        parms.addParm(parm);

        parm = new Parm();
        parm.setParmName("ifIndex");
        value = new Value();
        value.setContent("");
        parm.setValue(value);
        parms.addParm(parm);

        event.setParms(parms);

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
        notifications = m_notificationManager.getNotifForEvent(event);
        assertNotNull(notifications);
        assertEquals(1, notifications.length);
        Map<String,String> paramMap = BroadcastEventProcessor.buildParameterMap(notifications[0], event, 9999);
        /*
        for (Map.Entry<String,String> entry : paramMap.entrySet()) {
            System.out.println(entry.getKey() + " => " + entry.getValue());
        }
         */
        assertEquals("High disk Threshold exceeded on 0.0.0.0, dsk-usr-pcent with Crap! There's only 15% free on the SAN and we need 20%! RUN AWAY!", paramMap.get("-tm"));
        expandResult = BroadcastEventProcessor.expandNotifParms("Notice #%noticeid%: Disk threshold exceeded on %nodelabel%: %parm[all]%.", paramMap);
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
