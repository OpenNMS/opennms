package org.opennms.netmgt.poller.remote;

import java.text.ParseException;

import org.opennms.netmgt.model.OnmsMonitoredService;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.SimpleTriggerBean;

public class MonitorServicePollDetails extends JobDetailBean {

	private static final long serialVersionUID = -6317676727942162470L;
	
	public MonitorServicePollDetails(ServicePollConfiguration svcPollConfiguration, PollService pollService) throws ParseException {
		super();

		setServicePollConfiguration(svcPollConfiguration);
		setPollService(pollService);
		setMonitoredService(svcPollConfiguration.getMonitoredService());
		
		setName("detailsFor:"+getServiceName());
		setJobClass(MonitoredServicePollJob.class);

		getJobDataMap().put("monitoredServicePollDetail", this);

		afterPropertiesSet();
		
	}
	
	private void setPollService(PollService pollService) {
		getJobDataMap().put("pollService", pollService);
	}
	
	private void setServicePollConfiguration(ServicePollConfiguration svcPollConfiguration) {
		getJobDataMap().put("servicePollConfiguration", svcPollConfiguration);
	}
	
	
	public ServicePollConfiguration getServicePollConfiguration() {
		return (ServicePollConfiguration)getJobDataMap().get("servicePollConfiguration");
	}

	public OnmsMonitoredService getMonitoredService() {
		return (OnmsMonitoredService)getJobDataMap().get("monitoredService");
	}
	
	private void setMonitoredService(OnmsMonitoredService monitoredService) {
		getJobDataMap().put("monitoredService", monitoredService);
	}
	
	public Trigger getTrigger() throws ParseException {
		
		
		// TODO create a PollModelTrigger that takes the pollModel directly
		SimpleTriggerBean trigger = new SimpleTriggerBean();
		trigger.setBeanName("triggerFor:"+getServiceName());
		trigger.setRepeatInterval(getServicePollConfiguration().getPollModel().getPollInterval());
		trigger.setJobDetail(this);
		trigger.afterPropertiesSet();
		
		return trigger;
		
	}


	public String getServiceName() {
		return getMonitoredService().getNodeId()+":"+getMonitoredService().getIpAddress()+":"+getMonitoredService().getServiceName();
	}
	
}
