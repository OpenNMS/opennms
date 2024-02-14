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
package org.opennms.netmgt.eventd;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.ImmutableMapper;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.ThrowableAnticipator;

/**
 * Test case for BroadcastEventProcessor.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class BroadcastEventProcessorTest {
    private EventConfDao m_eventConfDao = mock(EventConfDao.class);

    @Test
    public void testInstantiateWithNullEventIpcManager() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument eventIpcManager must not be null"));
        
        try {
            new BroadcastEventProcessor(null, m_eventConfDao);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    @Test
    public void testInstantiateWithNullEventConfDao() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument eventConfDao must not be null"));
        
        try {
            new BroadcastEventProcessor(new MockEventIpcManager(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    @Test
    public void testInstantiateAndClose() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);
        processor.close();
    }

    @Test
    public void testReload() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "dunno");
        
        // Expect a call to reload the EventConfDao
        m_eventConfDao.reload();

        processor.onEvent(ImmutableMapper.fromMutableEvent(eventBuilder.getEvent()));
    }

    @Test
    public void testReloadDaemonConfig() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);

        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "dunno");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, "Eventd");

        // Expect a call to reload the EventConfDao
        m_eventConfDao.reload();

        processor.onEvent(ImmutableMapper.fromMutableEvent(eventBuilder.getEvent()));
    }

}
