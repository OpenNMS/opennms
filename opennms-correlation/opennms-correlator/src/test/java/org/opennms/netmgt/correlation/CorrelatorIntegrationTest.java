//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 23: Use AbstractTransactionalTemporaryDatabaseSpringContextTests,
//              mockEventIpcManager.xml Spring context, and move
//              DaoTestConfigBean setup into a constructor. - dj@opennms.org
//
// Create: January 26, 2007
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
package org.opennms.netmgt.correlation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:META-INF/opennms/applicationContext-daemon.xml",
        "classpath:META-INF/opennms/mockEventIpcManager.xml",
        "classpath:META-INF/opennms/applicationContext-correlator.xml",
        "classpath*:META-INF/opennms/correlation-engine.xml"
})
public class CorrelatorIntegrationTest {

    @Autowired
    private MockEventIpcManager m_eventIpcMgr;

    @Test
    public void testIt() throws Exception {

        anticipateEvent(createEvent("testDownReceived", "TestEngine"));
        anticipateEvent(createEvent("testUpReceived", "TestEngine"));

        m_eventIpcMgr.broadcastNow(createEvent("testDown", "Test"));
        m_eventIpcMgr.broadcastNow(createEvent("testUp", "Test"));

        Thread.sleep(1000);

        verifyAnticipated();

    }

    @Test
    public void testTimer() throws Exception {

        anticipateEvent(createEvent("timerExpired", "TestEngine"));

        m_eventIpcMgr.broadcastNow(createEvent("timed", "Test"));

        Thread.sleep(1500);

        verifyAnticipated();
    }

    @Test
    public void testTimerCancel() throws Exception {

        m_eventIpcMgr.broadcastNow(createEvent("timed", "Test"));

        Thread.sleep(500);

        m_eventIpcMgr.broadcastNow(createEvent("cancelTimer", "Test"));

        Thread.sleep(1500);

        verifyAnticipated();

    }

    @Test
    public void testListEngineLoaded() throws Exception {

        anticipateEvent(createEvent("listLoaded", "TestEngine"));

        m_eventIpcMgr.broadcastNow(createEvent("isListLoaded", "Test"));

        verifyAnticipated();

    }

    private void verifyAnticipated() {
        m_eventIpcMgr.getEventAnticipator().verifyAnticipated(0, 0, 0, 0, 0);
    }

    private static Event createEvent(String uei, String source) {
        EventBuilder bldr = new EventBuilder(uei, source);
        return bldr.getEvent();
    }

    private void anticipateEvent(Event e) {
        m_eventIpcMgr.getEventAnticipator().anticipateEvent(e);
    }

}
