/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.opennms.core.fiber.Fiber;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;

import static org.easymock.EasyMock.*;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class CorrelatorTest extends TestCase {
	
	List<Object> mocks = new ArrayList<>();
	private EventIpcManager m_eventIpcManager;
	private CorrelationEngine m_engine;
	private Correlator m_correlator;

	@Override
	protected void setUp() throws Exception {
		m_eventIpcManager = createMock(EventIpcManager.class);
		m_engine = createMock(CorrelationEngine.class);
		
		List<String> interestingEvents = Collections.singletonList("uei.opennms.org:/testEvent");

		expect(m_engine.getName()).andStubReturn("myMockEngine");
		expect(m_engine.getInterestingEvents()).andReturn(interestingEvents);
		m_engine.tearDown();
		EasyMock.expectLastCall().anyTimes();

		m_eventIpcManager.addEventListener(isA(EventListener.class), same(interestingEvents));
		m_eventIpcManager.addEventListener(isA(EventListener.class), same(EventConstants.RELOAD_DAEMON_CONFIG_UEI));
		replayMocks();
	}

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
		
		verifyMocks();
	}
	
	public void testRegisterForEvents() throws Exception {

		m_correlator = new Correlator();
		m_correlator.setEventIpcManager(m_eventIpcManager);
		m_correlator.setCorrelationEngines(Collections.singletonList(m_engine));
		m_correlator.afterPropertiesSet();
		
		verifyMocks();
	}
	
	private <T> T createMock(Class<T> clazz) {
		T mock = EasyMock.createMock(clazz);
		mocks.add(mock);
		return mock;
	}
	
	private void replayMocks() {
		EasyMock.replay(mocks.toArray());
	}
	
	private void verifyMocks() {
		EasyMock.verify(mocks.toArray());
		EasyMock.reset(mocks.toArray());
	}
}
