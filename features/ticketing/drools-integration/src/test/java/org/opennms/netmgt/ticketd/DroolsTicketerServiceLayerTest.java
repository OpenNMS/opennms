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

package org.opennms.netmgt.ticketd;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.test.mock.EasyMockUtils;

import org.opennms.api.integration.ticketing.*;
import org.opennms.core.test.MockLogAppender;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public class DroolsTicketerServiceLayerTest extends TestCase {

    private DefaultTicketerServiceLayer m_droolsTicketerServiceLayer;
    private EasyMockUtils m_easyMockUtils;
    private DroolsTicketerConfigDao m_configDao;
    private AlarmDao m_alarmDao;
    private Plugin m_ticketerPlugin;
    private OnmsAlarm m_alarm;
    private Ticket m_ticket;
    private MockEventIpcManager m_eventIpcManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource("classpath:drools-ticketer-rules.drl");
        
        m_easyMockUtils = new EasyMockUtils();
        m_configDao = m_easyMockUtils.createMock(DroolsTicketerConfigDao.class);
        EasyMock.expect(m_configDao.getRulesFile()).andReturn(resource.getFile()).times(1);
        EasyMock.replay(m_configDao);
        
        m_alarmDao = m_easyMockUtils.createMock(AlarmDao.class);
        m_ticketerPlugin = m_easyMockUtils.createMock(Plugin.class);
        
        m_droolsTicketerServiceLayer = new DroolsTicketerServiceLayer(m_configDao);
        m_droolsTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_droolsTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
        
        EasyMock.reset(m_configDao);
        
        m_alarm = new OnmsAlarm();
        m_alarm.setId(1);
        m_alarm.setLogMsg("Test Logmsg");
        m_alarm.setDescription("Test Description");
        m_alarm.setUei("uei.opennms.org/nodes/nodeDown");
        
        m_ticket = new Ticket();
        m_ticket.setId("4");
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
    private void expectNewTicket() {
        try {
            m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        } catch (PluginException e) {
            e.printStackTrace();
        }
        EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Ticket ticket = (Ticket) EasyMock.getCurrentArguments()[0];
                assertNull(ticket.getId());
                ticket.setId("7");
                
                //
                // Verify the properties as generated by the Drools engine
                //
                assertEquals("Not Test Logmsg", ticket.getSummary());
                assertEquals("Not Test Description", ticket.getDetails());
                assertEquals("Jesse", ticket.getUser());
                return null;
            }
        });
    }

    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#createTicketForAlarm(int)}.
     */
    public void testCreateTicketForAlarm() {
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);

        expectNewTicket();

        expectNewAlarmState(TroubleTicketState.OPEN);

        m_easyMockUtils.replayAll();

        m_droolsTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), new HashMap<String, String>());

        m_easyMockUtils.verifyAll();
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.ticketd.DefaultTicketerServiceLayer#createTicketForAlarm(int)}.
     * Tests for correct alarm TroubleTicketState set as CREATE_FAILED when ticketer plugin fails
     */
    public void testFailedCreateTicketForAlarm() {
    	
        EasyMock.expect(m_alarmDao.get(m_alarm.getId())).andReturn(m_alarm);
        
        try {
            m_ticketerPlugin.saveOrUpdate(EasyMock.isA(Ticket.class));
        } catch (PluginException e) {
            e.printStackTrace();
        }
        
        EasyMock.expectLastCall().andThrow(new PluginException("Failed Create"));

        expectNewAlarmState(TroubleTicketState.CREATE_FAILED);
        
        m_easyMockUtils.replayAll();
        
        m_droolsTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(),new HashMap<String, String>());
        
        m_easyMockUtils.verifyAll();
    }
}
