package org.opennms.netmgt.model;

import org.springframework.core.io.Resource;

public class OnmsMonitoringLocationDefinition {
	
	private Integer m_id;
	private String m_area;
	private String m_label;
	private Resource m_pollConfiguration;
	
	public String getArea() {
		return m_area;
	}
	public void setArea(String area) {
		m_area = area;
	}
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
	public Resource getPollConfiguration() {
		return m_pollConfiguration;
	}
	public void setPollConfiguration(Resource pollConfiguration) {
		m_pollConfiguration = pollConfiguration;
	}
	
	
	

}
