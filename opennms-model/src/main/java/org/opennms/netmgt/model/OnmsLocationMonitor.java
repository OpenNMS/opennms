package org.opennms.netmgt.model;

import java.util.List;

public class OnmsLocationMonitor {
	
	private Integer m_id;
	private String m_label;
	private OnmsMonitoringLocationDefinition m_locationDefinition;
	private List<OnmsLocationSpecificStatusChange> m_mostRecentStatusChanges;		
	
	public Integer getId() {
		return m_id;
	}
	
	public void setId(Integer id) {
		m_id = id;
	}
	
	public String getLabel() {
		return m_label;
	}
	
	public void setLabel(String label) {
		m_label = label;
	}
	
	public OnmsMonitoringLocationDefinition getLocationDefinition() {
		return m_locationDefinition;
	}
	
	public void setLocationDefinition(OnmsMonitoringLocationDefinition locationDefinition) {
		m_locationDefinition = locationDefinition;
	}

}
