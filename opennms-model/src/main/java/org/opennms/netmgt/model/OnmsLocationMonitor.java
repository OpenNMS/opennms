package org.opennms.netmgt.model;

public class OnmsLocationMonitor {
	
	private Integer m_id;
	private String m_label;
	private OnmsMonitoringLocationDefinition m_locationDefinition;
	
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
