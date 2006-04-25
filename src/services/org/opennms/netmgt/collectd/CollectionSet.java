package org.opennms.netmgt.collectd;

import java.util.Map;
import java.util.TreeMap;

public class CollectionSet {
	
	private CollectionInterface m_collectionInterface;
	private String m_collectionName;
	
	private Map m_ifMap = new TreeMap();
	
	public CollectionSet(CollectionInterface collectionInterface, String collectionName) {
		m_collectionInterface = collectionInterface;
		m_collectionName = collectionName;
	}
	
	public Map getIfMap() {
		return m_ifMap;
	}
	public void setIfMap(Map ifMap) {
		m_ifMap = ifMap;
	}
	public NodeInfo getNodeInfo() {
		return new NodeInfo(m_collectionInterface, m_collectionName);
	}

	void addIfInfo(IfInfo ifInfo) {
		getIfMap().put(new Integer(ifInfo.getIndex()), ifInfo);
	}

}
