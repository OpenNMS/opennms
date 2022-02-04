/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public class DefaultTicketerServiceLayerTest {

    private DefaultTicketerServiceLayer m_defaultTicketerServiceLayer;
    private AlarmDao m_alarmDao;
    private Plugin m_ticketerPlugin;
    private OnmsAlarm m_alarm;
    private Ticket m_ticket;
    private MockEventIpcManager m_eventIpcManager;

    private AlarmEntityNotifier m_alarmEntityNotifier;

    @Before
    public void setUp() throws Exception {
        System.clearProperty(DefaultTicketerServiceLayer.SKIP_CREATE_WHEN_CLEARED_SYS_PROP);
        System.clearProperty(DefaultTicketerServiceLayer.SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP);

        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_alarmDao = mock(AlarmDao.class);
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_ticketerPlugin = mock(Plugin.class);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
        m_alarmEntityNotifier = mock(AlarmEntityNotifier.class);
        m_defaultTicketerServiceLayer.setAlarmEntityNotifier(m_alarmEntityNotifier);
        m_alarm = new OnmsAlarm();
        m_alarm.setId(1);
        m_alarm.setLogMsg("Test Logmsg");
        m_alarm.setDescription("Test Description");

        m_ticket = new Ticket();
        m_ticket.setId("4");
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_alarmDao);
        verifyNoMoreInteractions(m_ticketerPlugin);
        verifyNoMoreInteractions(m_alarmEntityNotifier);
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)}.
     */
    @Test
    public void testCancelTicketForAlarm() throws Exception {
        
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);
        
        try {
            when(m_ticketerPlugin.get(m_ticket.getId())).thenReturn(m_ticket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        expectNewTicketState(Ticket.State.CANCELLED);

        expectNewAlarmState(TroubleTicketState.CANCELLED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CANCELLED);

        m_defaultTicketerServiceLayer.cancelTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(m_ticket);
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)}.
     * Tests for correct alarm TroubleTicketState set as CANCEL_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedCancelTicketForAlarm() throws Exception {
        doReturn(m_alarm).when(m_alarmDao).get(m_alarm.getId());

        doThrow(new PluginException("Failed Cancel")).when(m_ticketerPlugin).get(m_ticket.getId());

        expectNewAlarmState(TroubleTicketState.CANCEL_FAILED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CANCEL_FAILED);

        m_defaultTicketerServiceLayer.cancelTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
    }
    

    /**
     * @param state
     */
    private void expectNewAlarmState(final TroubleTicketState state) {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                OnmsAlarm alarm = (OnmsAlarm) invocation.getArgument(0);
                assertEquals(state, alarm.getTTicketState());
                return null;
            }
        }).when(m_alarmDao).saveOrUpdate(m_alarm);
    }

    /**
     * @param state
     */
    private void expectNewTicketState(final Ticket.State state) throws PluginException {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Ticket ticket = (Ticket) invocation.getArgument(0);
                assertEquals(state, ticket.getState());
                return null;
            }
        }).when(m_ticketerPlugin).saveOrUpdate(m_ticket);
    }


    private void expectAnUpdateToAlarmNotifier(TroubleTicketState prevState, TroubleTicketState newState) {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                OnmsAlarm alarm = (OnmsAlarm) invocation.getArgument(0);
                TroubleTicketState ticketState = (TroubleTicketState) invocation.getArgument(1);
                TroubleTicketState expectedState = alarm.getTTicketState();
                assertEquals(prevState, ticketState);
                assertNotNull(expectedState);
                assertEquals(newState, expectedState);
                return null;
            }
        }).when(m_alarmEntityNotifier).didChangeTicketStateForAlarm(m_alarm, prevState);
    }
    
    

    private void expectNewTicket() throws PluginException {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Ticket ticket = (Ticket) invocation.getArgument(0);
                assertNull(ticket.getId());
                ticket.setId("7");
                assertEquals(m_alarm.getLogMsg(), ticket.getSummary());
                assertEquals(m_alarm.getDescription(), ticket.getDetails());
                return null;
            }
        }).when(m_ticketerPlugin).saveOrUpdate(any(Ticket.class));
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)}.
     * @throws PluginException 
     */
    @Test
    public void testCloseTicketForAlarm() throws PluginException {
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);
        when(m_ticketerPlugin.get(m_ticket.getId())).thenReturn(m_ticket);

        expectNewTicketState(Ticket.State.CLOSED);

        expectNewAlarmState(TroubleTicketState.CLOSED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CLOSED);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(m_ticket);
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)}.
     * Tests for correct alarm TroubleTicketState set as CLOSE_FAILED when ticketer plugin fails
     * @throws PluginException 
     */
    @Test
    public void testFailedCloseTicketForAlarm() throws PluginException {
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        doThrow(new PluginException("Failed Close")).when(m_ticketerPlugin).get(m_ticket.getId());

        expectNewAlarmState(TroubleTicketState.CLOSE_FAILED);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CLOSE_FAILED);

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#cancelTicketForAlarm(int, String)}}.
     */
    @Test
    public void testCreateTicketForAlarm() throws Exception {
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        expectNewTicket();

        expectNewAlarmState(TroubleTicketState.OPEN);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.OPEN);

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), new HashMap<String, String>());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(any(Ticket.class));
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#createTicketForAlarm(int, Map)}.
     * Tests for correct alarm TroubleTicketState set as CREATE_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedCreateTicketForAlarm() throws Exception {
        
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        doThrow(new PluginException("Failed Create")).when(m_ticketerPlugin).saveOrUpdate(any(Ticket.class));

        expectNewAlarmState(TroubleTicketState.CREATE_FAILED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CREATE_FAILED);

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(),new HashMap<String, String>());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(any(Ticket.class));
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)}.
     */
    @Test
    public void testUpdateTicketForAlarm() throws PluginException {

        m_ticket.setState(Ticket.State.CANCELLED);

        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);
        try {
            when(m_ticketerPlugin.get(m_ticket.getId())).thenReturn(m_ticket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //expectUpdatedTicket();

        expectNewAlarmState(TroubleTicketState.CANCELLED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CANCELLED);

        m_defaultTicketerServiceLayer.updateTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)}.
     * Tests for correct alarm TroubleTicketState set as CANCEL_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedUpdateTicketForAlarm() throws Exception {

        m_ticket.setState(Ticket.State.CANCELLED);

        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        doThrow(new PluginException("Failed Update")).when(m_ticketerPlugin).get(m_ticket.getId());

        expectNewAlarmState(TroubleTicketState.UPDATE_FAILED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.UPDATE_FAILED);

        m_defaultTicketerServiceLayer.updateTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
    }

    @Test
    public void createIsSkippedWhenAlarmIsCleared() {
        // This should be the default behavior
        //System.setProperty(DefaultTicketerServiceLayer.SKIP_CREATE_WHEN_CLEARED_SYS_PROP, Boolean.TRUE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), Collections.emptyMap());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
    }

    @Test
    public void createIsNotSkippedWhenAlarmIsCleared() throws PluginException {
        // This should only happen when skipCreateWhenCleared=false
        System.setProperty(DefaultTicketerServiceLayer.SKIP_CREATE_WHEN_CLEARED_SYS_PROP, Boolean.FALSE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
        m_defaultTicketerServiceLayer.setAlarmEntityNotifier(m_alarmEntityNotifier);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.OPEN);

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), Collections.emptyMap());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(any(OnmsAlarm.class));
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(any(Ticket.class));
    }

    @Test
    public void closedIsSkippedWhenAlarmIsNotCleared() {
        // This should be the default behavior
        //System.setProperty(DefaultTicketerServiceLayer.SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP, Boolean.TRUE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);

        m_alarm.setSeverity(OnmsSeverity.CRITICAL);
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), "id");

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
    }

    @Test
    public void closedIsNotSkippedWhenAlarmIsNotCleared() throws PluginException {
        // This should only happen when skipCreateWhenCleared=false
        System.setProperty(DefaultTicketerServiceLayer.SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP, Boolean.FALSE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
        m_defaultTicketerServiceLayer.setAlarmEntityNotifier(m_alarmEntityNotifier);

        m_alarm.setSeverity(OnmsSeverity.CRITICAL);

        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);
        when(m_ticketerPlugin.get(m_ticket.getId())).thenReturn(m_ticket);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CLOSED);

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        verify(m_alarmDao, atLeastOnce()).get(m_alarm.getId());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(eq(m_alarm), isNull());
        verify(m_ticketerPlugin, atLeastOnce()).get(m_ticket.getId());
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(any(Ticket.class));
    }
}
