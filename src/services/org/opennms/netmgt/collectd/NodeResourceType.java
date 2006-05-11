package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.snmp.SnmpInstId;

public class NodeResourceType extends ResourceType {
    
    private NodeInfo m_nodeInfo;

    public NodeResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        super(agent, snmpCollection);
        m_nodeInfo = new NodeInfo(this, agent);
    }

    public NodeInfo getNodeInfo() {
        return m_nodeInfo;
    }

    public int getType() {
        return DataCollectionConfig.NODE_ATTRIBUTES;
    }

    public CollectionResource findResource(SnmpInstId inst) {
        return m_nodeInfo;
    }

    
}
