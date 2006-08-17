package org.opennms.netmgt.poller.remote;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PollStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class MonitoredServicePollJobTest extends TestCase {
	

	private Scheduler m_scheduler;

	protected void setUp() throws Exception {
		
		SchedulerFactoryBean schedFactory = new SchedulerFactoryBean();
		schedFactory.afterPropertiesSet();
		m_scheduler = (Scheduler) schedFactory.getObject();
		
	}
	
	public static class MonitorServicePollJobChecker extends MonitoredServicePollJob {
		
		static public int callCount = 0;
		static public Exception exception = null;

		protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
			callCount++;
			try {
				super.executeInternal(context);
			} catch (JobExecutionException e) {
				exception = e;
				throw e;
			} catch (Exception e) {
				exception = e;
				throw new JobExecutionException(e);
			}
		}
		
	}
  
	public void testTrigger() throws Exception {
		
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
		
		ServicePollConfiguration svcPollConfig = new ServicePollConfiguration(svc, 100);
		
		PollService pollService = createNiceMock(PollService.class);
		replay(pollService);

		MonitorServicePollDetails pollDetails = new MonitorServicePollDetails(svcPollConfig, pollService);
		pollDetails.setJobClass(MonitorServicePollJobChecker.class);
		
		MonitorServicePollJobChecker.callCount = 0;

		m_scheduler.scheduleJob(pollDetails, pollDetails.getTrigger());
		
		Thread.sleep(200);
		
		assertTrue(MonitorServicePollJobChecker.callCount > 0);
		assertTrue(MonitorServicePollJobChecker.callCount < 5);
		
		verify(pollService);
		
	}
	
	public void testExecuteJob() throws Exception {
		
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
		
		ServicePollConfiguration svcPollConfig = new ServicePollConfiguration(svc, 100);
		
		PollStatus status = PollStatus.up(100L);
		
		PollService pollService = createMock(PollService.class);
		
		expect(pollService.poll(svc)).andReturn(status);
		
		replay(pollService);

		MonitorServicePollDetails pollDetails = new MonitorServicePollDetails(svcPollConfig, pollService);
		
		TriggerFiredBundle bundle = new TriggerFiredBundle(pollDetails, pollDetails.getTrigger(), null, false, new Date(), new Date(), null, new Date());
		
		MonitoredServicePollJob pollJob = new MonitoredServicePollJob();
		
		JobExecutionContext context = new JobExecutionContext(m_scheduler, bundle, pollJob);

		pollJob.execute(context);
		
		verify(pollService);
		
		assertEquals(status, svcPollConfig.getPollModel().getCurrentStatus());
		
		
	}


}
