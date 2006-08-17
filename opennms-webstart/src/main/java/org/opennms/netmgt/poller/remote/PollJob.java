package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.PollStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class PollJob extends QuartzJobBean {
	
	private PolledService m_polledService;
	private PollService m_pollService;
	private PolledServicesModel m_polledServicesModel;
	

	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		System.err.println("Polling "+m_polledService.getId());
		PollStatus pollStatus = m_pollService.poll(m_polledService.getMonitoredService());
		m_polledServicesModel.updateServiceStatus(m_polledService.getId(), pollStatus, context.getFireTime());

	}


	public void setPolledService(PolledService polledService) {
		m_polledService = polledService;
	}


	public void setPolledServicesModel(PolledServicesModel polledServicesModel) {
		m_polledServicesModel = polledServicesModel;
	}


	public void setPollService(PollService pollService) {
		m_pollService = pollService;
	}



}
