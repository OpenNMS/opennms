package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

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
		PollerFrontEnd pollerFrontEnd = createMock(PollerFrontEnd.class);
		
		OnmsMonitoredService svc = getMonitoredService();
        svc.setId(7);
		
		PollConfiguration pollConfig = new PollConfiguration(svc, new HashMap(), 300000);
		
		PolledService polledService = new PolledService(pollConfig.getMonitoredService(), pollConfig.getMonitorConfiguration(), pollConfig.getPollModel());
		
		Set<PolledService> polledServices = Collections.singleton(polledService);

        Poller poller = new Poller();

        pollerFrontEnd.addConfigurationChangedListener(poller);
		expect(pollerFrontEnd.getPolledServices()).andReturn(polledServices);
        expect(pollerFrontEnd.isRegistered()).andReturn(true);
		pollerFrontEnd.setInitialPollTime(eq(svc.getId()), isA(Date.class));
		expect(scheduler.scheduleJob(isA(PollJobDetail.class), isA(PolledServiceTrigger.class))).andReturn(new Date());
		
		replay(scheduler, pollService, pollerFrontEnd);
		
		poller.setScheduler(scheduler);
		poller.setPollService(pollService);
		poller.setPollerFrontEnd(pollerFrontEnd);
		
		poller.afterPropertiesSet();
		
		verify(scheduler, pollService, pollerFrontEnd);
		
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
