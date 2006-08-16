package org.opennms.netmgt.poller.remote;

import java.text.ParseException;

import junit.framework.TestCase;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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

	public void testPoll() throws Exception {
		
		OnmsNode node = new OnmsNode();
		node.setId(1);
		OnmsIpInterface iface = new OnmsIpInterface("192.168.1.1", node);
		OnmsServiceType svcType = new OnmsServiceType("HTTP");
		OnmsMonitoredService svc = new OnmsMonitoredService(iface, svcType);
		
		ServicePollConfiguration svcPollConfig = new ServicePollConfiguration(svc, 100);

		MonitorServicePollDetails pollDetails = new MonitorServicePollDetails(svcPollConfig);
		pollDetails.setJobClass(MonitorServicePollJobChecker.class);
		
		MonitorServicePollJobChecker.callCount = 0;

		m_scheduler.scheduleJob(pollDetails, pollDetails.getTrigger());
		
		Thread.sleep(200);
		
		assertTrue(MonitorServicePollJobChecker.callCount > 0);
		assertTrue(MonitorServicePollJobChecker.callCount < 5);
		
	}

}
