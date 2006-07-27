package org.opennms.web.svclayer.catstatus.model;

import java.util.Collection;
import java.util.ArrayList;


public class StatusCategory {

	private String m_label;
	private String m_comment;
	private Float m_normal;
	private Integer m_warning;
	private Boolean m_outagestatus;
	private String  m_filter;
	private Collection<StatusNode> m_nodelist;
	
	
	public StatusCategory(){
	
		m_nodelist = new ArrayList<StatusNode>();
	
	}
	
	public String getFilter() {
		
		return m_filter;
		
	}
	
	public void setFilter(String filter) {
		
		m_filter = filter;
				
	}
	
	
	
	
	public Collection<StatusNode> getNodes() {
	
		return m_nodelist;
	
	}
	
	
	public void addNode(StatusNode NewNode){
		
		m_nodelist.add(NewNode);
		
	}
	

	public Boolean getOutageStatus() {
		return m_outagestatus;
	}

	public void setOutageStatus(Boolean OutageStatus) {
		m_outagestatus = OutageStatus;
	}
	
	public String getComment() {
		return m_comment;
	}


	public void setComment(String m_comment) {
		this.m_comment = m_comment;
	}


	public String getLabel() {
		return m_label;
	}


	public void setLabel(String m_label) {
		this.m_label = m_label;
	}


	public Float getNormal() {
		return m_normal;
	}


	public void setNormal(Float m_normal) {
		this.m_normal = m_normal;
	}


	public Integer getWarning() {
		return m_warning;
	}


	public void setWarning(Integer m_warning) {
		this.m_warning = m_warning;
	}
	
}
