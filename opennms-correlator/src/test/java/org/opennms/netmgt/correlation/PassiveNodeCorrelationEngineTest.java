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
