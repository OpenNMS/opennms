package org.opennms.netmgt.model;

import java.util.Set;

public class OnmsApplication {
	
	private Integer m_id;
	private String m_label;
	private Set<OnmsMonitoredService> m_memberServices;
	
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
	public Set<OnmsMonitoredService> getMemberServices() {
		return m_memberServices;
	}
	public void setMemberServices(Set<OnmsMonitoredService> memberServices) {
		m_memberServices = memberServices;
	}

}
