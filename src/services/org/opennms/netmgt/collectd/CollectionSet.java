package org.opennms.netmgt.collectd;

import java.util.Map;

public class CollectionSet {
	
	private NodeInfo m_nodeInfo;
	private Map m_ifMap;
	
	public Map getIfMap() {
		return m_ifMap;
	}
	public void setIfMap(Map ifMap) {
		m_ifMap = ifMap;
	}
	public NodeInfo getNodeInfo() {
		return m_nodeInfo;
	}
	public void setNodeInfo(NodeInfo nodeInfo) {
		m_nodeInfo = nodeInfo;
	}

}
