package org.opennms.provisiond.utils;

public class SimpleNode {

	private Integer m_nodeId;
	private String m_label;
	private String m_foreignSource;
	private String m_foreignId;
	private String m_ipaddr;
	private String m_primaryFlag;

	public SimpleNode(Integer nodeId, String label, String foreignSource, String foreignId, String ipaddr, String primaryFlag) {
		
		m_nodeId = nodeId;
		m_label = label;
		m_foreignSource = foreignSource;
		m_foreignId = foreignId;
		m_ipaddr = ipaddr;
		m_primaryFlag = primaryFlag;
	}

	public Integer getNodeId() {
		return m_nodeId;
	}

	public void setNodeId(Integer m_nodeId) {
		this.m_nodeId = m_nodeId;
	}

	public String getLabel() {
		return m_label;
	}

	public void setLabel(String m_label) {
		this.m_label = m_label;
	}
	
	public String getForeignSource() {
		return m_foreignSource;
	}
	
	public void setForeignSource(String foreignSource) {
		m_foreignSource = foreignSource;
	}

	public String getForeignId() {
		return m_foreignId;
	}
	
	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}

	public String getIpaddr() {
		return m_ipaddr;
	}

	public void setIpaddr(String m_ipaddr) {
		this.m_ipaddr = m_ipaddr;
	}

	public String getPrimaryFlag() {
		return m_primaryFlag;
	}

	public void setPrimaryFlag(String primaryFlag) {
		this.m_primaryFlag = primaryFlag;
	}

}
