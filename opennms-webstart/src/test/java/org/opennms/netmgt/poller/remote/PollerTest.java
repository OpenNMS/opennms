package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.*;

import java.util.Date;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.quartz.Scheduler;

import junit.framework.TestCase;

public class PollerTest extends TestCase {
	
	public void testSchedule() throws Exception {
		
		Scheduler scheduler = createMock(Scheduler.class);
		PollService pollService = createNiceMock(PollService.class);
		PollerConfiguration pollerConfiguration = createMock(PollerConfiguration.class);
		
		OnmsMonitoredService svc = getMonitoredService();
		
		ServicePollConfiguration servicePollConfiguration = new ServicePollConfiguration(svc, 300000);
		ServicePollConfiguration[] svcPollConfigs = new ServicePollConfiguration[] {
				servicePollConfiguration
		};

		expect(pollerConfiguration.getConfigurationForPoller("poller")).andReturn(svcPollConfigs);
		expect(scheduler.scheduleJob(isA(PollJobDetail.class), isA(PollModelTrigger.class))).andReturn(new Date());
		
		replay(scheduler, pollService, pollerConfiguration);
		
		Poller poller = new Poller();
		poller.setScheduler(scheduler);
		poller.setPollService(pollService);
		poller.setPollerConfiguration(pollerConfiguration);
		poller.setPollerName("poller");
		
		poller.afterPropertiesSet();
		
		verify(scheduler, pollService, pollerConfiguration);
		
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
