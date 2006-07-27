package org.opennms.web.svclayer.catstatus.model;


import java.util.Collection;
import java.util.ArrayList;


public class StatusNode {
	private String m_label;
	private Boolean m_outagestatus;
	private Collection<StatusInterface> m_ipinterfaces;
	private Integer m_nodeid;
	
	
	public StatusNode(){
		
		m_ipinterfaces = new ArrayList<StatusInterface>();
		
	}
	
	public void addIpInterface(StatusInterface ipInterface){
		
		m_ipinterfaces.add(ipInterface);
		
	}
	
	public Collection<StatusInterface> getIpInterfaces(){
		
		return m_ipinterfaces;
		
	}
	
	
	public String getLlabel() {
		return m_label;
	}
	public void setLabel(String m_label) {
		this.m_label = m_label;
	}
	public Boolean getOutagestatus() {
		return m_outagestatus;
	}
	public void setOutagestatus(Boolean m_outagestatus) {
		this.m_outagestatus = m_outagestatus;
	}

	public Integer getNodeid() {
		return m_nodeid;
	}

	public void setNodeid(Integer nodeid) {
		m_nodeid = nodeid;
	}
	

	
}
