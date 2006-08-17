package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.PollStatus;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class Poller implements InitializingBean, PollObserver {
	
	private PollerConfiguration m_pollerConfiguration;
	private PollService m_pollService;
	private Scheduler m_scheduler;
	private String m_pollerName;
	private long m_initialSpreadTime = 300000L;
	
	public void setPollerConfiguration(PollerConfiguration pollerConfiguration) {
		m_pollerConfiguration = pollerConfiguration;
	}

	public void setPollService(PollService pollService) {
		m_pollService = pollService;
	}

	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}
	
	public void setPollerName(String pollerName) {
		m_pollerName = pollerName;
	}
	
	public void setInitialSpreadTime(long initialSpreadTime) {
		m_initialSpreadTime = initialSpreadTime;
	}
	

	public void afterPropertiesSet() throws Exception {
		assertNotNull(m_scheduler, "scheduler");
		assertNotNull(m_pollService, "pollService");
		assertNotNull(m_pollerConfiguration, "pollerConfiguration");
		assertNotNull(m_pollerName, "pollerName");
		
		scheduleServicePolls();

	}
	
	private void scheduleServicePolls() throws Exception {
		
		ServicePollConfiguration[] svcPollConfigs = m_pollerConfiguration.getConfigurationForPoller(m_pollerName);

		long startTime = System.currentTimeMillis();
		long scheduleSpacing = m_initialSpreadTime / svcPollConfigs.length;
		
		for (int i = 0; i < svcPollConfigs.length; i++) {
			ServicePollConfiguration svcPollConfig = svcPollConfigs[i];
			
			Trigger pollTrigger = new PollModelTrigger(svcPollConfig.getId(), svcPollConfig.getPollModel());
			pollTrigger.setStartTime(new Date(startTime));
			
			PollJobDetail jobDetail = new PollJobDetail(svcPollConfig.getId(), PollJob.class);
			jobDetail.setMonitoredService(svcPollConfig.getMonitoredService());
			jobDetail.setPollId(svcPollConfig.getId());
			jobDetail.setPollService(m_pollService);
			jobDetail.setPollObserver(this);
			
			m_scheduler.scheduleJob(jobDetail, pollTrigger);
			
			startTime += scheduleSpacing;
		}
		
		
	}

	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.notNull(propertyValue, propertyName+" must be set for instances of "+Poller.class);
	}

	public void pollCompleted(String pollId, PollStatus pollStatus) {
		System.err.println(new Date()+": Complete Poll for "+pollId+" status = "+pollStatus);
	}

	public void pollStarted(String pollId) {
		System.err.println(new Date()+": Begin Poll for "+pollId);
		
	}


}
