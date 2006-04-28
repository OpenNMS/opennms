package org.opennms.netmgt.model;

public class OnmsAgent extends Object {

	private Integer m_id;
	private OnmsServiceType m_serviceType;
	private String m_ipAddress;
	private OnmsNode m_node;

	public OnmsAgent() {
		super();
	}
	
	public Integer getId() {
		return m_id;
	}
	
	public void setId(Integer id) {
		m_id = id;
	}

	public OnmsServiceType getServiceType() {
		return m_serviceType;
	}

	public void setServiceType(OnmsServiceType serviceType) {
		m_serviceType = serviceType;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public OnmsNode getNode() {
		return m_node;
	}

	public void setNode(OnmsNode node) {
		m_node = node;
	}

}