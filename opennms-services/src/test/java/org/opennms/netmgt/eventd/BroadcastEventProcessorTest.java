/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Jan 08: Add a few more tests and make existing tests work with
 *              EventConfigurationManager -> EventConfDao rework. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.eventd;

import junit.framework.TestCase;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.mock.MockEventIpcManager;
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
