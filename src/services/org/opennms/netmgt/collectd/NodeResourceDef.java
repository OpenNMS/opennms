package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.DataCollectionConfig;

public class NodeResourceDef extends ResourceDef {
    
    private NodeInfo m_nodeInfo;

    public NodeResourceDef(CollectionAgent agent, String collectionName) {
        super(agent, collectionName);
        m_nodeInfo = new NodeInfo(this, agent, collectionName);
    }

    public NodeInfo getNodeInfo() {
        return m_nodeInfo;
    }

    public int getType() {
        return DataCollectionConfig.NODE_ATTRIBUTES;
    }

    
}
