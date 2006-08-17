package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

public class PollerScheduleServiceTest extends TestCase {
	
	private DefaultPollerScheduleService m_schedService;
	private PollerConfiguration m_pollerConfiguration;
	private PollService m_pollService;
	private Scheduler m_scheduler;
	
	protected void setUp() {
		
		m_pollerConfiguration = createMock(PollerConfiguration.class);
		m_pollService = createMock(PollService.class);
		m_scheduler = createMock(Scheduler.class);
		
		m_schedService = new DefaultPollerScheduleService();
		m_schedService.setPollerConfiguration(m_pollerConfiguration);
		m_schedService.setPollService(m_pollService);
		m_schedService.setPollerName("poller");
		
		
	}
	
	public void testScheduleServicePolls() throws Exception {
		
		OnmsMonitoredService svc = getMonitoredService();

		
		ServicePollConfiguration servicePollConfiguration = new ServicePollConfiguration(svc, 300000);
		ServicePollConfiguration[] svcPollConfigs = new ServicePollConfiguration[] {
				servicePollConfiguration
		};
		
		expect(m_pollerConfiguration.getConfigurationForPoller("poller")).andReturn(svcPollConfigs);
		expect(m_scheduler.scheduleJob(isA(JobDetail.class), isA(Trigger.class))).andReturn(new Date());

		replay(m_pollerConfiguration, m_pollService, m_scheduler);
		
		m_schedService.scheduleServicePolls(m_scheduler);
				
		verify(m_pollerConfiguration, m_pollService, m_scheduler);
		
		
	}

	private OnmsMonitoredService getMonitoredService() {
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
		return svc;
	}

}
