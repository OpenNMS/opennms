package org.opennms.web.svclayer.catstatus.model;

import java.util.ArrayList;
import java.util.Collection;


public class StatusSection {

	private String m_name;
	private Collection<StatusCategory> m_categorylist;
	
	public StatusSection(){
		
		m_categorylist = new ArrayList<StatusCategory>();
		
	}
	
	public void setName(String name){
		m_name = name;
	}
	
	public String getName() {
		return m_name;
	}
	
	public Collection<StatusCategory> getCategories() {
		return m_categorylist;
	}
	
	public void addCategory(StatusCategory newCategory) {
		m_categorylist.add(newCategory);
	}
	
}
