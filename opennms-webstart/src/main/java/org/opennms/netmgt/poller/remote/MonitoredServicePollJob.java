package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MonitoredServicePollJob extends QuartzJobBean implements StatefulJob {
	
	private OnmsMonitoredService m_monitoredService;
	private PollService m_pollService;
	private Date m_lastExecuteTime;
	private MonitorServicePollDetails m_monitoredServicePollDetail;
	private ServicePollConfiguration m_servicePollConfiguration;
	
	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	
	public void setMonitoredServicePollDetail(MonitorServicePollDetails monitoredServicePollDetail) {
		m_monitoredServicePollDetail = monitoredServicePollDetail;
	}
	
	public void setLastExecuteTime(Date lastExecuteTime) {
		m_lastExecuteTime = lastExecuteTime;
	}
	
	public void setPollService(PollService pollService) {
		m_pollService = pollService;
	}
	
	public void setServicePollConfiguration(ServicePollConfiguration servicePollConfiguration) {
		m_servicePollConfiguration = servicePollConfiguration;
	}
	

	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		Date pollTime = new Date();
		PollStatus pollStatus = m_pollService.poll(m_monitoredService);
		
		m_servicePollConfiguration.getPollModel().setCurrentStatus(pollStatus);
		m_servicePollConfiguration.getPollModel().setLastPollTime(pollTime);
		context.getJobDetail().getJobDataMap().put("lastExecuteTime", pollTime);
	}

}
