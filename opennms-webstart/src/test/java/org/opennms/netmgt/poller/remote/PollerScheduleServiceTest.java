package org.opennms.netmgt.poller.remote;

import java.text.ParseException;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

public class PollerScheduleServiceTest extends TestCase {
	
	DefaultPollerScheduleService m_schedService;
	PollerConfiguration m_config;
	
	protected void setUp() {
		
		m_config = createMock(PollerConfiguration.class);
		
		m_schedService = new DefaultPollerScheduleService();
		m_schedService.setPollerConfiguration(m_config);
	}
	
	public void testScheduleServicePolls() throws ParseException {
		
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);

		
		ServicePollConfiguration servicePollConfiguration = new ServicePollConfiguration(svc, 300000);
		ServicePollConfiguration[] svcPollConfigs = new ServicePollConfiguration[] {
				servicePollConfiguration
		};
		
		expect(m_config.getConfigurationForPoller("poller")).andReturn(svcPollConfigs);

		replay(m_config);
		
		MonitorServicePollDetails[] jobDetails = m_schedService.getServicePollDetails();
		
		verify(m_config);
		
		assertNotNull(jobDetails);
		assertEquals(1, jobDetails.length);
		assertNotNull(jobDetails[0]);
		
		assertSame(svc, jobDetails[0].getMonitoredService());
		
		
		
		
		
	}

}
