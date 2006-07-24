package org.opennms.web.services;

import static org.easymock.EasyMock.*;

import org.easymock.IAnswer;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

import junit.framework.TestCase;

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
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		iface.setIfIndex(1);
		final OnmsMonitoredService monSvc = new OnmsMonitoredService(iface, svcType);

		m_eventProxy.send(isA(Event.class));
		expectLastCall().andAnswer(new IAnswer<Object>() {

			public Object answer() throws Throwable {
				Event event = (Event)getCurrentArguments()[0];
				assertEquals("Incorrect uei for demandPollService event", "uei.opennms.org/internal/demandPollService", event.getUei());
				assertEquals("Incorrect nodeid for demandPollService event", monSvc.getNodeId().longValue(), event.getNodeid());
				assertEquals("Incorrect ipadr for demandPollService event", monSvc.getIpAddress(), event.getInterface());
				assertEquals("Incorrect ifIndex for demandPollService event", monSvc.getIfIndex().toString(), event.getIfIndex());
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

