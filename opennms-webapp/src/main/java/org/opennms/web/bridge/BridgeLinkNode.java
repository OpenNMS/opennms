package org.opennms.web.bridge;

public class BridgeLinkNode {
	
	private String m_bridgeLocalPort;

	private String m_bridgeRemoteNode;
	private String m_bridgeRemoteUrl;
	private String m_bridgeRemotePort;
	private String m_bridgeRemotePortUrl;

	private String m_bridgeLinkCreateTime;
    private String m_bridgeLinkLastPollTime;
    
    public String getBridgeLinkCreateTime() {
		return m_bridgeLinkCreateTime;
	}
	public void setBridgeLinkCreateTime(String bridgeLinkCreateTime) {
		m_bridgeLinkCreateTime = bridgeLinkCreateTime;
	}
	public String getBridgeLinkLastPollTime() {
		return m_bridgeLinkLastPollTime;
	}
	public void setBridgeLinkLastPollTime(String bridgeLinkLastPollTime) {
		m_bridgeLinkLastPollTime = bridgeLinkLastPollTime;
	}
	public String getBridgeLocalPort() {
		return m_bridgeLocalPort;
	}
	public void setBridgeLocalPort(String bridgeLocalPort) {
		m_bridgeLocalPort = bridgeLocalPort;
	}
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

    

}
