package org.opennms.netmgt.poller.remote;

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
	
	public void setPolledService(PolledService polledService) {
		getJobDataMap().put("polledService", polledService);
	}
	
	public void setPolledServicesModel(PolledServicesModel polledServicesModel) {
		getJobDataMap().put("polledServicesModel", polledServicesModel);
	}
	
}
