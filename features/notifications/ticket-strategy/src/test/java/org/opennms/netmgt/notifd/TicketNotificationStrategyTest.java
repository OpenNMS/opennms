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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;

/**
 * Basic test cases for the TicketNotificationStrategy.
 *
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class TicketNotificationStrategyTest {

    private MockEventIpcManager m_eventIpcManager;
    private MockTicketNotificationStrategy m_ticketNotificationStrategy;
    private EventConfDao m_eventConfDao;
    private DataSource m_dataSource;

    private class MockTicketNotificationStrategy extends TicketNotificationStrategy {
    	AlarmState m_alarmState;

    	public MockTicketNotificationStrategy() {
    		m_alarmState = new AlarmState(0,"",0);
    	}

    	public void setAlarmState(AlarmState alarmState) {
    		m_alarmState = alarmState;
    	}

    	@Override
    	protected AlarmState getAlarmStateFromEvent(long eventID) {
    		return m_alarmState;
    	}
    };

    @Before
    public void setUp() throws Exception {
        m_eventIpcManager = new MockEventIpcManager();
        m_eventIpcManager.setSynchronous(true);
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        m_eventConfDao = mock(EventConfDao.class);
        m_ticketNotificationStrategy = new MockTicketNotificationStrategy();
        m_ticketNotificationStrategy.setEventConfDao(m_eventConfDao);
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
    public void testNoticeWithNonAlarmEvent() {
    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(null);
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should fail silently if the event is not an alarm.", 0, m_ticketNotificationStrategy.send(arguments));
    	assertTrue("Strategy should log a warning if the event is not an alarm.", !MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void testNoticeWithNoAlarmID() {
    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
    	m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(0));
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should fail if the event has no alarm id.", 1, m_ticketNotificationStrategy.send(arguments));
    	assertTrue("Strategy should log an error if the event has no alarm id.", !MockLogAppender.noWarningsOrHigherLogged());
    }

    @Test
    public void testCreateTicket() {
        // Setup the event anticipator
    	EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, m_ticketNotificationStrategy.getName());
        newSuspectBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
        newSuspectBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
        newSuspectBuilder.setParam(EventConstants.PARM_USER, "admin");
        m_eventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
        m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1));
        List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);

        assertEquals(0, m_ticketNotificationStrategy.send(arguments));
	    assertTrue("Expected events not forthcoming", m_eventIpcManager.getEventAnticipator().waitForAnticipated(0).isEmpty());
	    assertEquals("Received unexpected events", 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    @Test
    public void testNoticeWithOpenTicketDoesNotCreateAnother() {
    	assertTicketStateBlocksCreate(TroubleTicketState.OPEN);
    }

    @Test
    public void testNoticeWithCreatePendingTicketDoesNotCreateAnother() {
    	assertTicketStateBlocksCreate(TroubleTicketState.CREATE_PENDING);
    }

    @Test
    public void testNoticeWithUpdatePendingTicketDoesNotCreateAnother() {
    	assertTicketStateBlocksCreate(TroubleTicketState.UPDATE_PENDING);
    }

    @Test
    public void testNoticeWithCreatePendingAndNoTicketIdDoesNotCreateAnother() {
    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
    	m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1, "", TroubleTicketState.CREATE_PENDING.getValue()));
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should succeed while a ticket creation is in flight.", 0, m_ticketNotificationStrategy.send(arguments));
    	assertEquals("Strategy should not create a second ticket while creation is in flight.", 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    @Test
    public void testNoticeWithClosedTicketCreatesNewTicket() {
    	assertTicketStateAllowsCreate(TroubleTicketState.CLOSED);
    }

    @Test
    public void testNoticeWithCancelledTicketCreatesNewTicket() {
    	assertTicketStateAllowsCreate(TroubleTicketState.CANCELLED);
    }

    @Test
    public void testNoticeWithCreateFailedTicketCreatesNewTicket() {
    	assertTicketStateAllowsCreate(TroubleTicketState.CREATE_FAILED);
    }

    private void assertTicketStateBlocksCreate(TroubleTicketState state) {
    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
    	m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1, "TICKET-1", state.getValue()));
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should succeed without sending an event for ticket state " + state, 0, m_ticketNotificationStrategy.send(arguments));
    	assertEquals("Strategy should not send a create-ticket event for ticket state " + state, 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    private void assertTicketStateAllowsCreate(TroubleTicketState state) {
    	EventBuilder createTicketBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, m_ticketNotificationStrategy.getName());
    	createTicketBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
    	createTicketBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
    	createTicketBuilder.setParam(EventConstants.PARM_USER, TicketNotificationStrategy.DEFAULT_TICKET_USER);
    	m_eventIpcManager.getEventAnticipator().anticipateEvent(createTicketBuilder.getEvent());

    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
    	m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1, "TICKET-1", state.getValue()));
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should succeed for ticket state " + state, 0, m_ticketNotificationStrategy.send(arguments));
    	assertTrue("Strategy should send a create-ticket event for ticket state " + state, m_eventIpcManager.getEventAnticipator().waitForAnticipated(0).isEmpty());
    	assertEquals("Received unexpected events for ticket state " + state, 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    @Test
    public void testCreateTicketWithCustomUser() {
        EventBuilder createTicketBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, m_ticketNotificationStrategy.getName());
        createTicketBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
        createTicketBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
        createTicketBuilder.setParam(EventConstants.PARM_USER, "noc");
        m_eventIpcManager.getEventAnticipator().anticipateEvent(createTicketBuilder.getEvent());

        when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
        m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1));
        List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
        arguments.add(new Argument("ticketUser", null, "noc", false));

        assertEquals(0, m_ticketNotificationStrategy.send(arguments));
        assertTrue("Expected events not forthcoming", m_eventIpcManager.getEventAnticipator().waitForAnticipated(0).isEmpty());
        assertEquals("Received unexpected events", 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    @Test
    public void testCreateTicketWithBlankUserFallsBackToDefault() {
        EventBuilder createTicketBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, m_ticketNotificationStrategy.getName());
        createTicketBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
        createTicketBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
        createTicketBuilder.setParam(EventConstants.PARM_USER, TicketNotificationStrategy.DEFAULT_TICKET_USER);
        m_eventIpcManager.getEventAnticipator().anticipateEvent(createTicketBuilder.getEvent());

        when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
        m_ticketNotificationStrategy.setAlarmState(new TicketNotificationStrategy.AlarmState(1));
        List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
        arguments.add(new Argument("ticketUser", null, "", false));

        assertEquals(0, m_ticketNotificationStrategy.send(arguments));
        assertTrue("Expected events not forthcoming", m_eventIpcManager.getEventAnticipator().waitForAnticipated(0).isEmpty());
        assertEquals("Received unexpected events", 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    @Test
    public void testClearedAlarmWithOpenTicketClosesIt() {
    	assertClearedStateClosesTicket(TroubleTicketState.OPEN);
    }

    @Test
    public void testClearedAlarmWithCloseFailedTicketClosesItAgain() {
    	assertClearedStateClosesTicket(TroubleTicketState.CLOSE_FAILED);
    }

    @Test
    public void testClearedAlarmWithoutTicketSendsNothing() {
    	assertClearedStateSendsNothing("", TroubleTicketState.OPEN);
    }

    @Test
    public void testClearedAlarmWithClosedTicketSendsNothing() {
    	assertClearedStateSendsNothing("TICKET-1", TroubleTicketState.CLOSED);
    }

    @Test
    public void testClearedAlarmWithClosePendingTicketSendsNothing() {
    	assertClearedStateSendsNothing("TICKET-1", TroubleTicketState.CLOSE_PENDING);
    }

    private void assertClearedStateClosesTicket(TroubleTicketState state) {
    	EventBuilder closeTicketBuilder = new EventBuilder(EventConstants.TROUBLETICKET_CLOSE_UEI, m_ticketNotificationStrategy.getName());
    	closeTicketBuilder.setParam(EventConstants.PARM_ALARM_ID, "1");
    	closeTicketBuilder.setParam(EventConstants.PARM_ALARM_UEI, EventConstants.NODE_DOWN_EVENT_UEI);
    	closeTicketBuilder.setParam(EventConstants.PARM_USER, "noc");
    	closeTicketBuilder.setParam(EventConstants.PARM_TROUBLE_TICKET, "TICKET-1");
    	m_eventIpcManager.getEventAnticipator().anticipateEvent(closeTicketBuilder.getEvent());

    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
    	m_ticketNotificationStrategy.setAlarmState(buildClearedAlarmState("TICKET-1", state));
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	arguments.add(new Argument("ticketUser", null, "noc", false));

    	assertEquals("Strategy should succeed for ticket state " + state, 0, m_ticketNotificationStrategy.send(arguments));
    	assertTrue("Strategy should send a close-ticket event for ticket state " + state, m_eventIpcManager.getEventAnticipator().waitForAnticipated(0).isEmpty());
    	assertEquals("Received unexpected events for ticket state " + state, 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    private void assertClearedStateSendsNothing(String ticketID, TroubleTicketState state) {
    	when(m_eventConfDao.findByUei(EventConstants.NODE_DOWN_EVENT_UEI)).thenReturn(buildAlarmEvent(1));
    	m_ticketNotificationStrategy.setAlarmState(buildClearedAlarmState(ticketID, state));
    	List<Argument> arguments = buildArguments("1", EventConstants.NODE_DOWN_EVENT_UEI);
    	assertEquals("Strategy should succeed for ticket state " + state, 0, m_ticketNotificationStrategy.send(arguments));
    	assertEquals("Strategy should not send an event for ticket state " + state, 0, m_eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size());
    }

    private TicketNotificationStrategy.AlarmState buildClearedAlarmState(String ticketID, TroubleTicketState state) {
    	return new TicketNotificationStrategy.AlarmState(1, ticketID, state.getValue(), OnmsSeverity.CLEARED.getId());
    }

    protected Event buildAlarmEvent(int alarmType) {
		Event event = new Event();
		AlarmData alarmData = new AlarmData();
		alarmData.setAlarmType(alarmType);
		event.setAlarmData(alarmData);
		return event;
    }

    protected List<Argument> buildArguments(String eventID, String eventUEI)
    {
		List<Argument> arguments = new ArrayList<>();
		arguments.add(new Argument("eventID", null, eventID, false));
		arguments.add(new Argument("eventUEI", null, eventUEI, false));
		return arguments;
    }
}
