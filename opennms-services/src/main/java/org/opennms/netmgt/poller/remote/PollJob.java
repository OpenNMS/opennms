package org.opennms.netmgt.poller.remote;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class PollJob extends QuartzJobBean {
	
	private PolledService m_polledService;
	private PollerFrontEnd m_pollerFrontEnd;
	

	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        m_pollerFrontEnd.pollService(m_polledService.getServiceId());

	}

    public void setPolledService(PolledService polledService) {
		m_polledService = polledService;
	}


	public void setPollerFrontEnd(PollerFrontEnd pollerFrontEnd) {
		m_pollerFrontEnd = pollerFrontEnd;
	}




}
