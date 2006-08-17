package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class PollJob extends QuartzJobBean {
	
	private String pollId;
	private PollService m_pollService;
	private OnmsMonitoredService m_monitoredService;
	private PollObserver m_pollObserver;
	

	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		
		pollStarted();
		PollStatus pollStatus = m_pollService.poll(m_monitoredService);
		pollCompleted(pollStatus);
	}


	private void pollCompleted(PollStatus pollStatus) {
		m_pollObserver.pollCompleted(pollId, pollStatus);
	}


	private void pollStarted() {
		m_pollObserver.pollStarted(pollId);
	}


	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}


	public void setPollObserver(PollObserver pollObserver) {
		m_pollObserver = pollObserver;
	}


	public void setPollService(PollService pollService) {
		m_pollService = pollService;
	}


	public void setPollId(String pollId) {
		this.pollId = pollId;
	}

}
