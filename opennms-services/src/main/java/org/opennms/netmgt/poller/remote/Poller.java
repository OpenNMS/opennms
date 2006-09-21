package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class Poller implements InitializingBean, PollObserver {
	
	private PollService m_pollService;
	private PolledServicesModel m_polledServicesModel;
	private Scheduler m_scheduler;
	private long m_initialSpreadTime = 300000L;
	
	public void setPollService(PollService pollService) {
		m_pollService = pollService;
	}
	
	public void setPolledServicesModel(PolledServicesModel polledServicesModel) {
		m_polledServicesModel = polledServicesModel;
	}

	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}
	
	public void setInitialSpreadTime(long initialSpreadTime) {
		m_initialSpreadTime = initialSpreadTime;
	}
	

	public void afterPropertiesSet() throws Exception {
		assertNotNull(m_scheduler, "scheduler");
		assertNotNull(m_pollService, "pollService");
		assertNotNull(m_polledServicesModel, "polledServicesModel");
		
		schedulePolls();

	}
	
	private void schedulePolls() throws Exception {
		
		PolledService[] polledServices = m_polledServicesModel.getPolledServices();

		if (polledServices == null || polledServices.length == 0) {
			log().warn("No polling scheduled.");
			return;
		}

		long startTime = System.currentTimeMillis();
		long scheduleSpacing = m_initialSpreadTime / polledServices.length;
		
		for (int i = 0; i < polledServices.length; i++) {
			PolledService polledService = polledServices[i];
			
			Date initialPollTime = new Date(startTime);
			
			m_polledServicesModel.setInitialPollTime(polledService.getId(), initialPollTime);
			
			Trigger pollTrigger = new PolledServiceTrigger(polledService.getId(), polledService);
			pollTrigger.setStartTime(initialPollTime);
			
			PollJobDetail jobDetail = new PollJobDetail(polledService.getId(), PollJob.class);
			jobDetail.setPolledService(polledService);
			jobDetail.setPolledServicesModel(m_polledServicesModel);
			jobDetail.setPollService(m_pollService);
			
			m_scheduler.scheduleJob(jobDetail, pollTrigger);
			
			startTime += scheduleSpacing;
		}
		
		
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
	}

	public void pollCompleted(String pollId, PollStatus pollStatus) {
		System.err.println(new Date()+": Complete Poll for "+pollId+" status = "+pollStatus);
	}

	public void pollStarted(String pollId) {
		System.err.println(new Date()+": Begin Poll for "+pollId);
		
	}


}
