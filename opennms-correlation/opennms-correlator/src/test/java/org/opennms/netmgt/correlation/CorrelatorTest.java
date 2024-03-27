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
