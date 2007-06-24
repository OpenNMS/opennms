package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PathElement implements Serializable {
    private static final long serialVersionUID = 2437052160139305371L;
    
    private String m_ipAddress;
	private String m_serviceName;
	
	public PathElement() {
		
	}

	public PathElement(String ipAddress, String serviceName) {
		m_ipAddress = ipAddress;
		m_serviceName = serviceName;
	}

	@Column(name="criticalPathIp")
	public String getIpAddress() {
		return m_ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}
	
	@Column(name="criticalPathServiceName")
	public String getServiceName() {
		return m_serviceName;
	}
	
	public void setServiceName(String serviceName) {
		m_serviceName = serviceName;
	}


}
