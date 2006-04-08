package org.opennms.netmgt.model;

import java.util.List;
import java.util.Set;

public class IpInterfaceSelector {
	
	private String m_filter;
	private Set m_specifics;
	private List m_includeRanges;
	private List m_excludeRanges;
	private List m_includeURLs;
	
	public List getExcludeRanges() {
		return m_excludeRanges;
	}
	public void setExcludeRanges(List excludeRanges) {
		m_excludeRanges = excludeRanges;
	}
	public String getFilter() {
		return m_filter;
	}
	public void setFilter(String filter) {
		m_filter = filter;
	}
	public List getIncludeRanges() {
		return m_includeRanges;
	}
	public void setIncludeRanges(List includeRanges) {
		m_includeRanges = includeRanges;
	}
	public List getIncludeURLs() {
		return m_includeURLs;
	}
	public void setIncludeURLs(List includeURLs) {
		m_includeURLs = includeURLs;
	}
	public Set getSpecifics() {
		return m_specifics;
	}
	public void setSpecifics(Set specifics) {
		m_specifics = specifics;
	}
	


}
