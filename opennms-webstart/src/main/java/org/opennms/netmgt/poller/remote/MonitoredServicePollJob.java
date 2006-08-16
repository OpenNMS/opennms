package org.opennms.netmgt.poller.remote;

import java.util.Date;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MonitoredServicePollJob extends QuartzJobBean implements StatefulJob {
	
	private OnmsMonitoredService m_monitoredService;
	private Date m_lastExecuteTime;
	private MonitorServicePollDetails m_monitoredServicePollDetail;
	
	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	
	public void setMonitoredServicePollDetail(MonitorServicePollDetails monitoredServicePollDetail) {
		m_monitoredServicePollDetail = monitoredServicePollDetail;
	}
	
	public void setLastExecuteTime(Date lastExecuteTime) {
		m_lastExecuteTime = lastExecuteTime;
	}
	

	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		System.err.println("POLL: "+m_monitoredServicePollDetail.getServiceName()+" Last Polled: "+(m_lastExecuteTime == null ? "NEVER" : m_lastExecuteTime));
		context.getJobDetail().getJobDataMap().put("lastExecuteTime", new Date());
	}

}
