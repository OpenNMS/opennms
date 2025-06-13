/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
