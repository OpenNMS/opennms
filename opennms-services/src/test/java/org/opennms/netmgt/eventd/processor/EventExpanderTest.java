/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 15, 2008
 *
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
package org.opennms.netmgt.eventd.processor;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.config.EventConfDao;
import org.opennms.netmgt.eventd.processor.EventExpander;
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
