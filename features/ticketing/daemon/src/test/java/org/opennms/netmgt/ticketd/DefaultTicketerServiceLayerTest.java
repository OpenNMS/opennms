/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.test.mock.EasyMockUtils;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public class DefaultTicketerServiceLayerTest {

    private DefaultTicketerServiceLayer m_defaultTicketerServiceLayer;
    private EasyMockUtils m_easyMockUtils;
    private AlarmDao m_alarmDao;
    private Plugin m_ticketerPlugin;
    private OnmsAlarm m_alarm;
    private Ticket m_ticket;
    private MockEventIpcManager m_eventIpcManager;

    @Before
    public void setUp() throws Exception {
        System.clearProperty(DefaultTicketerServiceLayer.SKIP_CREATE_WHEN_CLEARED_SYS_PROP);
        System.clearProperty(DefaultTicketerServiceLayer.SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP);

        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_easyMockUtils = new EasyMockUtils();
        m_alarmDao = m_easyMockUtils.createMock(AlarmDao.class);
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_ticketerPlugin = m_easyMockUtils.createMock(Plugin.class);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
        m_alarm = new OnmsAlarm();
        m_alarm.setId(1);
        m_alarm.setLogMsg("Test Logmsg");
        m_alarm.setDescription("Test Description");

        m_ticket = new Ticket();
        m_ticket.setId("4");
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)}.
     */
    @Test
    public void testCancelTicketForAlarm() {
        
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        
        try {
            EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        expectNewTicketState(Ticket.State.CANCELLED);

        expectNewAlarmState(TroubleTicketState.CANCELLED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.cancelTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)}.
     * Tests for correct alarm TroubleTicketState set as CANCEL_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedCancelTicketForAlarm() {
        
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        
        try {
            m_ticketerPlugin.get(m_ticket.getId());
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        EasyMock.expectLastCall().andThrow(new PluginException("Failed Cancel"));

        expectNewAlarmState(TroubleTicketState.CANCEL_FAILED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.cancelTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
    }
    

    /**
     * @param state
     */
    private void expectNewAlarmState(final TroubleTicketState state) {
        m_alarmDao.saveOrUpdate(m_alarm);
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                OnmsAlarm alarm = (OnmsAlarm) EasyMock.getCurrentArguments()[0];
                assertEquals(state, alarm.getTTicketState());
                return null;
            }

        });
    }

    /**
     * @param state
     */
    private void expectNewTicketState(final Ticket.State state) {
        try {
            m_ticketerPlugin.saveOrUpdate(m_ticket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertEquals(state, ticket.getState());
                return null;
            }

        });
    }
    
    

    /**
     * @param state
     */
    private void expectNewTicket() {
        try {
            m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertNull(ticket.getId());
                ticket.setId("7");
                assertEquals(m_alarm.getLogMsg(), ticket.getSummary());
                assertEquals(m_alarm.getDescription(), ticket.getDetails());
                return null;
            }

        });
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)}.
     * @throws PluginException 
     */
    @Test
    public void testCloseTicketForAlarm() throws PluginException {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);

        expectNewTicketState(Ticket.State.CLOSED);

        expectNewAlarmState(TroubleTicketState.CLOSED);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)}.
     * Tests for correct alarm TroubleTicketState set as CLOSE_FAILED when ticketer plugin fails
     * @throws PluginException 
     */
    @Test
    public void testFailedCloseTicketForAlarm() throws PluginException {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        m_ticketerPlugin.get(m_ticket.getId());

        EasyMock.expectLastCall().andThrow(new PluginException("Failed Close"));

        expectNewAlarmState(TroubleTicketState.CLOSE_FAILED);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#createTicketForAlarm(int)}.
     */
    @Test
    public void testCreateTicketForAlarm() {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        expectNewTicket();

        expectNewAlarmState(TroubleTicketState.OPEN);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), new HashMap<String, String>());

        m_easyMockUtils.verifyAll();
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#createTicketForAlarm(int)}.
     * Tests for correct alarm TroubleTicketState set as CREATE_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedCreateTicketForAlarm() {
        
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        
        try {
            m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        } catch (PluginException e) {
            //e.printStackTrace();
        }
        
        EasyMock.expectLastCall().andThrow(new PluginException("Failed Create"));

        expectNewAlarmState(TroubleTicketState.CREATE_FAILED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(),new HashMap<String, String>());

        m_easyMockUtils.verifyAll();
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)}.
     */
    @Test
    public void testUpdateTicketForAlarm() {

        m_ticket.setState(Ticket.State.CANCELLED);

        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        try {
            EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //expectUpdatedTicket();

        expectNewAlarmState(TroubleTicketState.CANCELLED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.updateTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)}.
     * Tests for correct alarm TroubleTicketState set as CANCEL_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedUpdateTicketForAlarm() {

        m_ticket.setState(Ticket.State.CANCELLED);

        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        try {
            m_ticketerPlugin.get(m_ticket.getId());
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }

        //expectUpdatedTicket();
        
        EasyMock.expectLastCall().andThrow(new PluginException("Failed Update"));

        expectNewAlarmState(TroubleTicketState.UPDATE_FAILED);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.updateTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        m_easyMockUtils.verifyAll();
    }

    @Test
    public void createIsSkippedWhenAlarmIsCleared() {
        // This should be the default behavior
        //System.setProperty(DefaultTicketerServiceLayer.SKIP_CREATE_WHEN_CLEARED_SYS_PROP, Boolean.TRUE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), Collections.emptyMap());

        // Verify that no additional calls where made (such as trying to open the ticket)
        m_easyMockUtils.verifyAll();
    }

    @Test
    public void createIsNotSkippedWhenAlarmIsCleared() throws PluginException {
        // This should only happen when skipCreateWhenCleared=false
        System.setProperty(DefaultTicketerServiceLayer.SKIP_CREATE_WHEN_CLEARED_SYS_PROP, Boolean.FALSE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);

        m_alarm.setSeverity(OnmsSeverity.CLEARED);
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        EasyMock.expectLastCall();

        m_alarmDao.saveOrUpdate(m_alarm);
        EasyMock.expectLastCall();

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), Collections.emptyMap());

        m_easyMockUtils.verifyAll();
    }

    @Test
    public void closedIsSkippedWhenAlarmIsNotCleared() {
        // This should be the default behavior
        //System.setProperty(DefaultTicketerServiceLayer.SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP, Boolean.TRUE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);

        m_alarm.setSeverity(OnmsSeverity.CRITICAL);
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), "id");

        // Verify that no additional calls where made (such as trying to close the ticket)
        m_easyMockUtils.verifyAll();
    }

    @Test
    public void closedIsNotSkippedWhenAlarmIsNotCleared() throws PluginException {
        // This should only happen when skipCreateWhenCleared=false
        System.setProperty(DefaultTicketerServiceLayer.SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP, Boolean.FALSE.toString());

        m_defaultTicketerServiceLayer = new DefaultTicketerServiceLayer();
        m_defaultTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_defaultTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);

        m_alarm.setSeverity(OnmsSeverity.CRITICAL);
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        EasyMock.expect(m_ticketerPlugin.get(m_ticket.getId())).andReturn(m_ticket);
        EasyMock.expectLastCall();

        m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        EasyMock.expectLastCall();

        m_alarmDao.saveOrUpdate(m_alarm);
        EasyMock.expectLastCall();

        m_easyMockUtils.replayAll();

        m_defaultTicketerServiceLayer.closeTicketForAlarm(m_alarm.getId(), m_ticket.getId());

        // Verify that no additional calls where made (such as trying to close the ticket)
        m_easyMockUtils.verifyAll();
    }

    @SuppressWarnings("unused")
    private void expectUpdatedTicket() {
        try {
            m_ticketerPlugin.saveOrUpdate(m_ticket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {

            @Override
            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertEquals(Ticket.State.OPEN, ticket.getState());
                assertEquals(m_alarm.getDescription(), ticket.getDetails());
                return null;
            }

        });
    }

}
