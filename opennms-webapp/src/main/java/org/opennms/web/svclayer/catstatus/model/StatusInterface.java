package org.opennms.web.svclayer.catstatus.model;

import java.util.Collection;
import java.util.ArrayList;

public class StatusInterface {
	
	private String m_ipaddress;
	private String m_interfacename;
	private Collection<StatusService> m_services;
	
	public StatusInterface(){
		
		m_services = new ArrayList<StatusService>();
		
	}
	
	public void addService ( StatusService service ) {	
		m_services.add(service);
	}
	
	public Collection<StatusService> getServices(){
		return m_services;
	}
	
	public String getInterfacename() {
		return m_interfacename;
	}
	
	public void setInterfacename(String m_interfacename) {
		this.m_interfacename = m_interfacename;
	}
	
	public String getIpAddress() {
		return m_ipaddress;
	}
	
	public void setIpAddress(String m_ipaddress) {
		this.m_ipaddress = m_ipaddress;
	}
	
	

}
