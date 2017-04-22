/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-correlator.xml",
        "classpath*:META-INF/opennms/correlation-engine.xml"
})
@JUnitConfigurationEnvironment
public class CorrelatorIT implements InitializingBean {

    @Autowired
    private MockEventIpcManager m_eventIpcMgr;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @After
    public void tearDown() {
        // Reset the event anticipator
        m_eventIpcMgr.getEventAnticipator().reset();
    }

    @Test
    public void testIt() throws Exception {

        anticipateEvent(createEvent("testDownReceived", "TestEngine"));
        anticipateEvent(createEvent("testUpReceived", "TestEngine"));

        m_eventIpcMgr.broadcastNow(createEvent("testDown", "Test"));
        m_eventIpcMgr.broadcastNow(createEvent("testUp", "Test"));

        Thread.sleep(1000);

        verifyAnticipated();

    }

    /**
     * Test that the timer goes off as expected after 1000ms.
     * 
     * @throws Exception
     */
    @Test
    public void testTimer() throws Exception {

        anticipateEvent(createEvent("timerExpired", "TestEngine"));

        m_eventIpcMgr.broadcastNow(createEvent("timed", "Test"));

        Thread.sleep(1500);

        verifyAnticipated();
    }

    /**
     * Test that the timer does not go off before it is cancelled.
     * 
     * @throws Exception
     */
    @Test
    public void testTimerCancel() throws Exception {

        m_eventIpcMgr.broadcastNow(createEvent("timed", "Test"));

        // Sleep for less than 1000ms
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
