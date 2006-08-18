package org.opennms.netmgt.poller.remote;

import java.util.Map;

import org.opennms.netmgt.model.OnmsMonitoredService;

public class PollConfiguration {
	
	private OnmsMonitoredService m_monitoredService;
	private OnmsPollModel m_pollModel;
	private Map m_monitorConfiguration;
	
	public PollConfiguration(OnmsMonitoredService monitoredService, Map monitorConfiguration, long pollInterval) {
		m_monitoredService = monitoredService;
		m_monitorConfiguration = monitorConfiguration;
		m_pollModel = new OnmsPollModel();
		m_pollModel.setPollInterval(pollInterval);
	}

	public OnmsMonitoredService getMonitoredService() {
		return m_monitoredService;
	}

	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	
	public Map getMonitorConfiguration() {
		return m_monitorConfiguration;
	}

	public OnmsPollModel getPollModel() {
		return m_pollModel;
	}

	public void setPollModel(OnmsPollModel pollModel) {
		m_pollModel = pollModel;
	}

	public String getId() {
		return m_monitoredService.getNodeId()+":"+m_monitoredService.getIpAddress()+":"+m_monitoredService.getServiceName();
	}

}
