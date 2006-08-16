package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.model.OnmsMonitoredService;

public class ServicePollConfiguration {
	
	private OnmsMonitoredService m_monitoredService;
	private OnmsPollModel m_pollModel;
	
	public ServicePollConfiguration(OnmsMonitoredService monitoredService, long pollInterval) {
		m_monitoredService = monitoredService;
		m_pollModel = new OnmsPollModel();
		m_pollModel.setPollInterval(pollInterval);
	}

	public OnmsMonitoredService getMonitoredService() {
		return m_monitoredService;
	}

	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}

	public OnmsPollModel getPollModel() {
		return m_pollModel;
	}

	public void setPollModel(OnmsPollModel pollModel) {
		m_pollModel = pollModel;
	}

}
