/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.util.ArrayList;
import java.util.List;
import org.opennms.netmgt.EventConstants;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.notifd.TicketNotificationStrategy.AlarmType;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.Argument;

import junit.framework.TestCase;
import javax.sql.DataSource;

/**
 * Basic test cases for the TicketNotificationStrategy.
 * 
 * @author <a href="mailto:jwhite@datavlaet.com">Jesse White</a>
 * @version $Id: $
 */
public class TicketNotificationStrategyTest extends TestCase {

    private EasyMockUtils m_easyMockUtils;
    private MockEventIpcManager m_eventIpcManager;
    private EventAnticipator m_anticipator;
    private MockTicketNotificationStrategy m_ticketNotificationStrategy;
    private DataSource m_dataSource;
    
    private class MockTicketNotificationStrategy extends TicketNotificationStrategy {
    	AlarmState m_alarmState;
    	AlarmType m_alarmType;

    	public MockTicketNotificationStrategy() {
    		m_alarmState = new AlarmState(0,"",0);
    		m_alarmType = AlarmType.NOT_AN_ALARM;
    	}
    	
    	public void setAlarmState(AlarmState alarmState) {
    		m_alarmState = alarmState;
    	}
    	
    	@SuppressWarnings("unused")
		public AlarmState getAlarmState() {
    		return m_alarmState;
    	}
    	
    	public void setAlarmType(AlarmType alarmType) {
    		m_alarmType = alarmType;
    	}
    	
    	@SuppressWarnings("unused")
    	public AlarmType getAlarmType(AlarmType alarmType) {
    		return m_alarmType;
    	}
    	
    	@Override
    	protected AlarmState getAlarmStateFromEvent(int eventID) {
    		return m_alarmState;
    	}
    	
    	@Override
    	protected AlarmType getAlarmTypeFromUEI(String eventUEI) {
    		return m_alarmType;
    	}
    };

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        m_anticipator = new EventAnticipator();
        m_eventIpcManager = new MockEventIpcManager();
        m_eventIpcManager.setEventAnticipator(m_anticipator);
        m_eventIpcManager.setSynchronous(true);
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        m_ticketNotificationStrategy = new MockTicketNotificationStrategy();
        m_easyMockUtils = new EasyMockUtils();
        m_dataSource = m_easyMockUtils.createMock(DataSource.class);
        DataSourceFactory.setInstance(m_dataSource);
    }

    /** {@inheritDoc} */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
        
    public void testNoticeWithNoEventID() {
    	assertEquals("Strategy should fail if no event id is given.", 1, m_ticketNotificationStrategy.send(new ArrayList<Argument>()));
    }
    
    public void testNoticeWithNoAlarmID() {
    	m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(0));
    	m_ticketNotificationStrategy.setAlarmType(AlarmType.NOT_AN_ALARM);
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should fail silently if the event has no alarm id.", 0, m_ticketNotificationStrategy.send(arguments));
    	assertTrue("Strategy should log a warning if the event has no alarm id.", !MockLogAppender.noWarningsOrHigherLogged());
    }
    
    public void testCreateTicket() {
        // Setup the event anticipator
    	EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, m_ticketNotificationStrategy.getName());
        newSuspectBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
        newSuspectBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
        newSuspectBuilder.setParam(EventConstants.PARM_USER, "admin");
        m_anticipator.anticipateEvent(newSuspectBuilder.getEvent());
        
        m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1));
        m_ticketNotificationStrategy.setAlarmType(AlarmType.PROBLEM);
        List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
        
        assertEquals(0, m_ticketNotificationStrategy.send(arguments));
	    assertTrue("Expected events not forthcoming", m_anticipator.waitForAnticipated(0).isEmpty());
	    assertEquals("Received unexpected events", 0, m_anticipator.unanticipatedEvents().size());
    }
    
    protected List<Argument> buildArguments(String eventID, String eventUEI) 
    {
		List<Argument> arguments = new ArrayList<Argument>();
		arguments.add(new Argument("eventID", null, eventID, false));
		arguments.add(new Argument("eventUEI", null, eventUEI, false));
		return arguments;
    }
}
