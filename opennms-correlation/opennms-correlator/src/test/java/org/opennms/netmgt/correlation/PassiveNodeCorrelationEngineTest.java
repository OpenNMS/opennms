//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.correlation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class PassiveNodeCorrelationEngineTest extends TestCase {
	
	private PassiveNodeCorrelationEngine m_correlationEngine;
	private CorrelationServices m_correlationServices;
	private List<Object> m_mocks = new ArrayList<Object>();
	


	protected void setUp() throws Exception {
		m_correlationServices = createMock(CorrelationServices.class);
		
		m_correlationEngine = new PassiveNodeCorrelationEngine();
		m_correlationEngine.setCorrelationServices(m_correlationServices);
		m_correlationEngine.afterPropertiesSet();
	}
	
	protected <T> T createMock(Class<T> toMock) {
		T mock = EasyMock.createMock(toMock);
		m_mocks.add(mock);
		return mock;
	}
	
	protected void replayMocks() {
		EasyMock.replay(m_mocks.toArray());
	}
	
	protected void verifyMocks() {
		EasyMock.verify(m_mocks.toArray());
	}


	public void testGetInterestingEvents() {
		replayMocks(); 
		
		List<String> interestingEvents = m_correlationEngine.getInterestingEvents();
		assertEquals("expected one interesting event", 1, interestingEvents.size());
		assertEquals(EventConstants.NODE_DOWN_EVENT_UEI, interestingEvents.get(0));
		
		verifyMocks();
	}
	
	public void testCorrelate() {
		
		
		EventBuilder bldr = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, "Test");
		bldr.setNodeid(1);

		Event e = bldr.getEvent();
		
		replayMocks();

		m_correlationEngine.correlate(e);
		
		verifyMocks();
		
	}

}
