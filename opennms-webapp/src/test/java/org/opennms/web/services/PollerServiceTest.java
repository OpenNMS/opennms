/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.services;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;

import org.easymock.IAnswer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

public class PollerServiceTest extends TestCase {
	
	DefaultPollerService m_pollerService;
	EventProxy m_eventProxy;
	
	
	
	
	@Override
	protected void setUp() throws Exception {
		m_eventProxy = createMock(EventProxy.class);
		
		m_pollerService = new DefaultPollerService();
		m_pollerService.setEventProxy(m_eventProxy);
	}

	public void testPoll() throws EventProxyException {
		
		final int expectedPolldId = 7;
		
		OnmsServiceType svcType = new OnmsServiceType();
		svcType.setId(3);
		svcType.setName("HTTP");
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsSnmpInterface snmpIface = new OnmsSnmpInterface(node, 1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		iface.setSnmpInterface(snmpIface);
		final OnmsMonitoredService monSvc = new OnmsMonitoredService(iface, svcType);

		m_eventProxy.send(isA(Event.class));
		expectLastCall().andAnswer(new IAnswer<Object>() {

                        @Override
			public Object answer() throws Throwable {
				Event event = (Event)getCurrentArguments()[0];
				assertEquals("Incorrect uei for demandPollService event", EventConstants.DEMAND_POLL_SERVICE_EVENT_UEI, event.getUei());
				assertEquals("Incorrect nodeid for demandPollService event", monSvc.getNodeId().longValue(), event.getNodeid().longValue());
				assertEquals("Incorrect ipadr for demandPollService event", InetAddressUtils.str(monSvc.getIpAddress()), event.getInterface());
				assertEquals("Incorrect ifIndex for demandPollService event", monSvc.getIfIndex(), Integer.valueOf(event.getIfIndex()));
				assertEquals("Incorrect service for demandPollService event", monSvc.getServiceType().getName(), event.getService());
				EventUtils.requireParm(event, EventConstants.PARM_DEMAND_POLL_ID);
				assertEquals(expectedPolldId, EventUtils.getIntParm(event, EventConstants.PARM_DEMAND_POLL_ID, -1));
				return null;
			}
			
		});
		
		replay(m_eventProxy);
		
		m_pollerService.poll(monSvc, expectedPolldId);
		
		verify(m_eventProxy);
	}

}

