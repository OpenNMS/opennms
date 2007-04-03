package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Embeddable
public class PathElement implements Serializable {
	
	private String m_ipAddress;
	private String m_serviceName;
	

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
