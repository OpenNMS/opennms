package org.opennms.web.enlinkd;

import java.util.ArrayList;
import java.util.List;

public class NodeLinkBridge {
	
	private List<String> m_nodeLocalPorts = new ArrayList<String>();
	private BridgeLinkRemoteNode m_bridgeLinkRemoteNode;

	private String m_bridgeLinkCreateTime;
    private String m_bridgeLinkLastPollTime;

	public List<String> getNodeLocalPorts() {
		return m_nodeLocalPorts;
	}
	public void setNodeLocalPorts(List<String> nodeLocalPorts) {
		m_nodeLocalPorts = nodeLocalPorts;
	}
	public BridgeLinkRemoteNode getBridgeLinkRemoteNode() {
		return m_bridgeLinkRemoteNode;
	}
	public void setBridgeLinkRemoteNode(BridgeLinkRemoteNode bridgeLinkRemoteNode) {
		m_bridgeLinkRemoteNode = bridgeLinkRemoteNode;
	}
    
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
    

}
