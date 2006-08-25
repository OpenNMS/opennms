package org.opennms.netmgt.model;

public class OnmsLocationSpecificStatusChange {
	
	private Integer m_id;
	private OnmsLocationMonitor m_locationMonitor;
	private OnmsMonitoredService m_monitoredService;
	private PollStatus m_newStatus;
	
	public Integer getId() {
		return m_id;
	}
	
	public void setId(Integer id) {
		m_id = id;
	}
	
	public OnmsLocationMonitor getLocationMonitor() {
		return m_locationMonitor;
	}
	
	public void setLocationMonitor(OnmsLocationMonitor locationMonitor) {
		m_locationMonitor = locationMonitor;
	}
	
	public OnmsMonitoredService getMonitoredService() {
		return m_monitoredService;
	}
	
	public void setMonitoredService(OnmsMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	
	public PollStatus getNewStatus() {
		return m_newStatus;
	}
	
	public void setNewStatus(PollStatus newStatus) {
		m_newStatus = newStatus;
	}
	
	

}
