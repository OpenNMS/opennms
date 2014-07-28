package org.opennms.web.bridge;

import java.util.ArrayList;
import java.util.List;

public class BridgeLinkNode {
	
	private String m_bridgeLocalPort;
	private Integer m_bridgeLocalVlan;

	private List<BridgeLinkRemoteNode> m_bridgeLinkRemoteNodes = new ArrayList<BridgeLinkRemoteNode>();

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
	public Integer getBridgeLocalVlan() {
		return m_bridgeLocalVlan;
	}
	public void setBridgeLocalVlan(Integer bridgeLocalVlan) {
		m_bridgeLocalVlan = bridgeLocalVlan;
	}
	public List<BridgeLinkRemoteNode> getBridgeLinkRemoteNodes() {
		return m_bridgeLinkRemoteNodes;
	}
	public void setBridgeLinkRemoteNodes(
			List<BridgeLinkRemoteNode> bridgeLinkRemoteNodes) {
		m_bridgeLinkRemoteNodes = bridgeLinkRemoteNodes;
	}
    

}
