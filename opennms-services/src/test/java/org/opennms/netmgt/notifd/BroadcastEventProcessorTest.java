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

import java.io.Reader;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ConfigurationTestUtils;

public class BroadcastEventProcessorTest extends NotificationsTestCase {

    private BroadcastEventProcessor m_processor;
    
    protected void setUp() throws Exception {
    	super.setUp();
    	m_processor = new BroadcastEventProcessor();
        m_processor.initExpandRe();
        
        m_anticipator.setExpectedDifference(3000);
        
        Reader rdr = ConfigurationTestUtils.getReaderForConfigFile("database-schema.xml"); 
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(rdr));
        rdr.close();
    }

    /**
     * Test calling expandNotifParms to see if the regular expression in
     * m_notifdExpandRE is initialized from NOTIFD_EXPANSION_PARM.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testExpandNotifParms() {
        m_processor.expandNotifParms("%foo%", Collections.EMPTY_MAP);
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
