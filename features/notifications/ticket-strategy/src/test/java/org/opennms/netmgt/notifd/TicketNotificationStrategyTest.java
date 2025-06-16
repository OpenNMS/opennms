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
package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.notifd.TicketNotificationStrategy.AlarmType;

/**
 * Basic test cases for the TicketNotificationStrategy.
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class TicketNotificationStrategyTest {

    private MockEventIpcManager m_eventIpcManager;
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

    @Before
    public void setUp() throws Exception {
        m_eventIpcManager = new MockEventIpcManager();
        m_eventIpcManager.setSynchronous(true);
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        m_ticketNotificationStrategy = new MockTicketNotificationStrategy();
        m_dataSource = mock(DataSource.class);
        DataSourceFactory.setInstance(m_dataSource);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_dataSource);
    }

    @Test
    public void testNoticeWithNoEventID() {
    	assertEquals("Strategy should fail if no event id is given.", 1, m_ticketNotificationStrategy.send(new ArrayList<Argument>()));
    }

    @Test
    public void testNoticeWithNoAlarmID() {
    	m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(0));
    	m_ticketNotificationStrategy.setAlarmType(AlarmType.NOT_AN_ALARM);
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should fail silently if the event has no alarm id.", 0, m_ticketNotificationStrategy.send(arguments));
    	assertTrue("Strategy should log a warning if the event has no alarm id.", !MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void testCreateTicket() {
        // Setup the event anticipator
    	EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, m_ticketNotificationStrategy.getName());
        newSuspectBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
        newSuspectBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
        newSuspectBuilder.setParam(EventConstants.PARM_USER, "admin");
        m_eventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());
        
        m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1));
        m_ticketNotificationStrategy.setAlarmType(AlarmType.PROBLEM);
        List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
        
        assertEquals(0, m_ticketNotificationStrategy.send(arguments));
	    assertTrue("Expected events not forthcoming", m_eventIpcManager.getEventAnticipator().waitForAnticipated(0).isEmpty());
	    assertEquals("Received unexpected events", 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }
    
    protected List<Argument> buildArguments(String eventID, String eventUEI) 
    {
		List<Argument> arguments = new ArrayList<>();
		arguments.add(new Argument("eventID", null, eventID, false));
		arguments.add(new Argument("eventUEI", null, eventUEI, false));
		return arguments;
    }
}
