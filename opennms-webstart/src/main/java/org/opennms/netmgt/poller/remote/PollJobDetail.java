package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

public class PollJobDetail extends JobDetail {

	private static final long serialVersionUID = -6499411861193543030L;
	
	public PollJobDetail(String name, Class jobClass) {
		super(name, Scheduler.DEFAULT_GROUP, jobClass);
	}
	
	public void setPollService(PollService pollService) {
		getJobDataMap().put("pollService", pollService);
	}
	
	public void setPollObserver(PollObserver pollObserver) {
		getJobDataMap().put("pollObserver", pollObserver);
	}
	
	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		getJobDataMap().put("monitoredService", monitoredService);
	}
	
	public void setPollId(String pollId) {
		getJobDataMap().put("pollId", pollId);
	}
	
}
