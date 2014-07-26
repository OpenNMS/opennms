package org.opennms.web.bridge;

public class BridgeLinkRemoteNode {
	private String m_bridgeRemoteNode;
	private String m_bridgeRemoteUrl;
	private String m_bridgeRemotePort;
	private String m_bridgeRemotePortUrl;
	private Integer m_bridgeRemoteVlan;
	
	public String getBridgeRemoteNode() {
		return m_bridgeRemoteNode;
	}
	public void setBridgeRemoteNode(String bridgeRemoteNode) {
		m_bridgeRemoteNode = bridgeRemoteNode;
	}
	public String getBridgeRemoteUrl() {
		return m_bridgeRemoteUrl;
	}
	public void setBridgeRemoteUrl(String bridgeRemoteUrl) {
		m_bridgeRemoteUrl = bridgeRemoteUrl;
	}
	public String getBridgeRemotePort() {
		return m_bridgeRemotePort;
	}
	public void setBridgeRemotePort(String bridgeRemotePort) {
		m_bridgeRemotePort = bridgeRemotePort;
	}
	public String getBridgeRemotePortUrl() {
		return m_bridgeRemotePortUrl;
	}
	public void setBridgeRemotePortUrl(String bridgeRemotePortUrl) {
		m_bridgeRemotePortUrl = bridgeRemotePortUrl;
	}
	public Integer getBridgeRemoteVlan() {
		return m_bridgeRemoteVlan;
	}
	public void setBridgeRemoteVlan(Integer bridgeRemoteVlan) {
		m_bridgeRemoteVlan = bridgeRemoteVlan;
	}

}
