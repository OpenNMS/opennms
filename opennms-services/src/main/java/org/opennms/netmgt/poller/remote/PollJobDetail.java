package org.opennms.netmgt.poller.remote;

import org.quartz.JobDetail;

public class PollJobDetail extends JobDetail {
    
    public static final String GROUP = "pollJobGroup";

	private static final long serialVersionUID = -6499411861193543030L;
	
	public PollJobDetail(String name, Class jobClass) {
		super(name, GROUP, jobClass);
	}
	
	public void setPollService(PollService pollService) {
		getJobDataMap().put("pollService", pollService);
	}
	
	public void setPolledService(PolledService polledService) {
		getJobDataMap().put("polledService", polledService);
	}
	
	public void setPollerFrontEnd(PollerFrontEnd pollerFrontEnd) {
		getJobDataMap().put("pollerFrontEnd", pollerFrontEnd);
	}
	
}
