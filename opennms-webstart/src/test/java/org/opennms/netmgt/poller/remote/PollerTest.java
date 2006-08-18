package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.*;

import java.util.Date;
import java.util.HashMap;

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
		PolledServicesModel polledServicesModel = createMock(PolledServicesModel.class);
		
		OnmsMonitoredService svc = getMonitoredService();
		
		PollConfiguration pollConfig = new PollConfiguration(svc, new HashMap(), 300000);
		
		PolledService polledService = new PolledService("id", pollConfig.getMonitoredService(), pollConfig.getMonitorConfiguration(), pollConfig.getPollModel());
		
		PolledService[] polledServices = new PolledService[] {
				polledService
		};

		expect(polledServicesModel.getPolledServices()).andReturn(polledServices);
		polledServicesModel.setInitialPollTime(eq("id"), isA(Date.class));
		expect(scheduler.scheduleJob(isA(PollJobDetail.class), isA(PolledServiceTrigger.class))).andReturn(new Date());
		
		replay(scheduler, pollService, polledServicesModel);
		
		Poller poller = new Poller();
		poller.setScheduler(scheduler);
		poller.setPollService(pollService);
		poller.setPolledServicesModel(polledServicesModel);
		
		poller.afterPropertiesSet();
		
		verify(scheduler, pollService, polledServicesModel);
		
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
