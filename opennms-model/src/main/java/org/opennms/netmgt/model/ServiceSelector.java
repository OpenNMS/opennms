package org.opennms.netmgt.model;

import java.util.List;

public class ServiceSelector {
	
	private String m_filterRule;
	private List<String> m_serviceNames;
	
	
	public ServiceSelector(String filterRule, List<String>serviceNames) {
		m_filterRule = filterRule;
		m_serviceNames = serviceNames;
	}
	
	public String getFilterRule() {
		return m_filterRule;
	}
	
	public List<String> getServiceNames() {
		return m_serviceNames;
	}
}
