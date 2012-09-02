/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.config.EventExpander;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventExpanderTest extends TestCase {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    private EventConfDao m_eventConfDao = m_mocks.createMock(EventConfDao.class);

    @Override
    protected void runTest() throws Throwable {
        super.runTest();
        
        m_mocks.verifyAll();
    }
    
    public void testAfterPropertiesSetWithNoEventConfDao() {
        m_mocks.replayAll();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property eventConfDao must be set"));

        EventExpander expander = new EventExpander();

        try {
            expander.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }

        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSet() {
        m_mocks.replayAll();

        EventExpander expander = new EventExpander();
        expander.setEventConfDao(m_eventConfDao);
        expander.afterPropertiesSet();
    }
    
    public void testExpandEventWithNoDaoMatches() {

        String uei = "uei.opennms.org/internal/capsd/snmpConflictsWithDb";

        EventBuilder builder = new EventBuilder(uei, "something");

        EventExpander expander = new EventExpander();
        expander.setEventConfDao(m_eventConfDao);
        expander.afterPropertiesSet();
        
        Event event = builder.getEvent();
        assertNull("event description should be null before expandEvent is called", event.getDescr());

        EasyMock.expect(m_eventConfDao.findByEvent(event)).andReturn(null);
        EasyMock.expect(m_eventConfDao.findByUei("uei.opennms.org/default/event")).andReturn(null);
        m_mocks.replayAll();

        expander.expandEvent(event);
        
        assertEquals("event UEI", uei, event.getUei());
        //assertNotNull("event description should not be null after expandEvent is called", event.getDescr());
        //
        //String matchText = "During a rescan";
        //assertTrue("event description should contain '" + matchText + "'", event.getDescr().contains(matchText));
    }
}
