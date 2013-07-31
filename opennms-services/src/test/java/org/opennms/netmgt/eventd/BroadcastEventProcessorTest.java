/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import junit.framework.TestCase;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * Test case for BroadcastEventProcessor.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class BroadcastEventProcessorTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private EventConfDao m_eventConfDao = m_mocks.createMock(EventConfDao.class);
    
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
    
    public void testInstantiateAndClose() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);
        processor.close();
    }
    
    public void testReload() {
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        BroadcastEventProcessor processor = new BroadcastEventProcessor(eventIpcManager, m_eventConfDao);
        
        EventBuilder eventBuilder = new EventBuilder(EventConstants.EVENTSCONFIG_CHANGED_EVENT_UEI, "dunno");
        
        // Expect a call to reload the EventConfDao
        m_eventConfDao.reload();
        
        m_mocks.replayAll();
        
        processor.onEvent(eventBuilder.getEvent());
        
        m_mocks.verifyAll();
    }
}
