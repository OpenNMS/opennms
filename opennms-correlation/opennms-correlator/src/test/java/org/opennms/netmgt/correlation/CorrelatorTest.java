/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.fiber.Fiber;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class CorrelatorTest {
	private EventIpcManager m_eventIpcManager;
	private CorrelationEngine m_engine;
	private Correlator m_correlator;
	private List<String> m_interestingEvents;

	@Before
	public void setUp() throws Exception {
		m_eventIpcManager = mock(EventIpcManager.class);
		m_engine = mock(CorrelationEngine.class);
		
		m_interestingEvents = Collections.singletonList("uei.opennms.org:/testEvent");

		when(m_engine.getName()).thenReturn("myMockEngine");
		when(m_engine.getInterestingEvents()).thenReturn(Collections.singletonList("uei.opennms.org:/testEvent"));
		m_engine.tearDown();

		m_eventIpcManager.addEventListener(isA(EventListener.class), same(Collections.singletonList("uei.opennms.org:/testEvent")));
		m_eventIpcManager.addEventListener(isA(EventListener.class), same(EventConstants.RELOAD_DAEMON_CONFIG_UEI));

                verify(m_eventIpcManager, times(1)).addEventListener((EventListener)null, (List<String>)null);
                verify(m_eventIpcManager, times(1)).addEventListener((EventListener)null, (String)null);
                verify(m_engine, times(1)).tearDown();
	}

	@After
	public void tearDown() {
            verifyNoMoreInteractions(m_eventIpcManager);
            verifyNoMoreInteractions(m_engine);
	}

	@Test
	public void testStartStop() throws Exception {

		m_correlator = new Correlator();
		m_correlator.setEventIpcManager(m_eventIpcManager);
		m_correlator.setCorrelationEngines(Collections.singletonList(m_engine));
		m_correlator.afterPropertiesSet();

		assertEquals("Expected the correlator to be init'd", Fiber.START_PENDING, m_correlator.getStatus());
		m_correlator.start();
		assertEquals("Expected the correlator to be running", Fiber.RUNNING, m_correlator.getStatus());
		m_correlator.stop();
		assertEquals("Expected the correlator to be stopped", Fiber.STOPPED, m_correlator.getStatus());

                verify(m_eventIpcManager, times(1)).addEventListener(isA(Correlator.EngineAdapter.class), eq(m_interestingEvents));
                verify(m_eventIpcManager, times(1)).addEventListener(isA(Correlator.EngineAdapter.class), eq(EventConstants.RELOAD_DAEMON_CONFIG_UEI));
                verify(m_engine, times(2)).getName();
                verify(m_engine, times(1)).getInterestingEvents();
                verify(m_engine, times(2)).tearDown();
	}

	@Test
	public void testRegisterForEvents() throws Exception {
		m_correlator = new Correlator();
		m_correlator.setEventIpcManager(m_eventIpcManager);
		m_correlator.setCorrelationEngines(Collections.singletonList(m_engine));
		m_correlator.afterPropertiesSet();

                verify(m_eventIpcManager, times(1)).addEventListener(isA(Correlator.EngineAdapter.class), eq(m_interestingEvents));
                verify(m_eventIpcManager, times(1)).addEventListener(isA(Correlator.EngineAdapter.class), eq(EventConstants.RELOAD_DAEMON_CONFIG_UEI));
                verify(m_engine, times(2)).getName();
                verify(m_engine, times(1)).getInterestingEvents();
                verify(m_engine, times(1)).tearDown();
	}
	
}
