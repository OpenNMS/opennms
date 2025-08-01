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
package org.opennms.netmgt.ticketd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;

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
import org.opennms.netmgt.model.TroubleTicketState;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public class DroolsTicketerServiceLayerTest {

    private DefaultTicketerServiceLayer m_droolsTicketerServiceLayer;
    private DroolsTicketerConfigDao m_configDao;
    private AlarmDao m_alarmDao;
    private Plugin m_ticketerPlugin;
    private AlarmEntityNotifier m_alarmEntityNotifier;
    private OnmsAlarm m_alarm;
    private Ticket m_ticket;
    private MockEventIpcManager m_eventIpcManager;

    @Before
    public void setUp() throws Exception {
        m_eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(m_eventIpcManager);
        MockLogAppender.setupLogging();
        
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource("classpath:/drools-ticketer-rules.drl");
        
        m_configDao = mock(DroolsTicketerConfigDao.class);
        when(m_configDao.getRulesFile()).thenReturn(resource.getFile());
        
        m_alarmDao = mock(AlarmDao.class);
        m_ticketerPlugin = mock(Plugin.class);
        m_alarmEntityNotifier = mock(AlarmEntityNotifier.class);
        
        m_droolsTicketerServiceLayer = new DroolsTicketerServiceLayer(m_configDao);
        m_droolsTicketerServiceLayer.setAlarmDao(m_alarmDao);
        m_droolsTicketerServiceLayer.setTicketerPlugin(m_ticketerPlugin);
        m_droolsTicketerServiceLayer.setAlarmEntityNotifier(m_alarmEntityNotifier);
        
        m_alarm = new OnmsAlarm();
        m_alarm.setId(1);
        m_alarm.setLogMsg("Test Logmsg");
        m_alarm.setDescription("Test Description");
        m_alarm.setUei("uei.opennms.org/nodes/nodeDown");
        
        m_ticket = new Ticket();
        m_ticket.setId("4");
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_configDao);
        verifyNoMoreInteractions(m_alarmDao);
        verifyNoMoreInteractions(m_ticketerPlugin);
        verifyNoMoreInteractions(m_alarmEntityNotifier);
    }

    @Test
    public void testCreateTicketForAlarm() throws PluginException {
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        expectNewTicket();

        expectNewAlarmState(TroubleTicketState.OPEN);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.OPEN);

        m_droolsTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), new HashMap<>());

        verify(m_configDao, atLeastOnce()).getRulesFile();
        verify(m_alarmDao, atLeastOnce()).get(anyInt());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(any(Ticket.class));
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(any(OnmsAlarm.class), isNull());
    }
    
    /**
     * Tests for correct alarm TroubleTicketState set as CREATE_FAILED when ticketer plugin fails
     */
    @Test
    public void testFailedCreateTicketForAlarm() throws PluginException {
    	
        when(m_alarmDao.get(m_alarm.getId())).thenReturn(m_alarm);

        doThrow(new PluginException("Failed to create")).when(m_ticketerPlugin).saveOrUpdate(any(Ticket.class));

        expectNewAlarmState(TroubleTicketState.CREATE_FAILED);

        expectAnUpdateToAlarmNotifier(null, TroubleTicketState.CREATE_FAILED);
        
        m_droolsTicketerServiceLayer.createTicketForAlarm(m_alarm.getId(), new HashMap<>());

        verify(m_configDao, atLeastOnce()).getRulesFile();
        verify(m_alarmDao, atLeastOnce()).get(anyInt());
        verify(m_alarmDao, atLeastOnce()).saveOrUpdate(m_alarm);
        verify(m_ticketerPlugin, atLeastOnce()).saveOrUpdate(any(Ticket.class));
        verify(m_alarmEntityNotifier, atLeastOnce()).didChangeTicketStateForAlarm(any(OnmsAlarm.class), isNull());
    }

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

    private void expectNewTicket() throws PluginException {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                Ticket ticket = (Ticket) invocation.getArgument(0);
                assertNull(ticket.getId());
                ticket.setId("7");

                // Verify the properties as generated by the Drools engine
                assertEquals("Not Test Logmsg", ticket.getSummary());
                assertEquals("Not Test Description", ticket.getDetails());
                assertEquals("Jesse", ticket.getUser());
                assertEquals(
                        ImmutableMap.of("custom-key", "custom-value"),
                        ticket.getAttributes());
                return null;
            }
        }).when(m_ticketerPlugin).saveOrUpdate(any(Ticket.class));
    }

    private void expectAnUpdateToAlarmNotifier(TroubleTicketState prevState, TroubleTicketState newState) {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                OnmsAlarm alarm = (OnmsAlarm) invocation.getArgument(0);
                TroubleTicketState ticketState = invocation.getArgument(1);
                TroubleTicketState expectedState = alarm.getTTicketState();
                assertEquals(prevState, ticketState);
                assertNotNull(expectedState);
                assertEquals(newState, expectedState);
                return null;
            }            
        }).when(m_alarmEntityNotifier).didChangeTicketStateForAlarm(m_alarm, prevState);
    }
}
