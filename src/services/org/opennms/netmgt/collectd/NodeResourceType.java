package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.snmp.SnmpInstId;

public class NodeResourceType extends DbResourceType {
    
    private NodeInfo m_nodeInfo;
    private AttributeType m_ifNumberType;

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

    public Collection getResources() {
        return Collections.singleton(m_nodeInfo);
    }

    public Collection getAttributeTypes() {
        List attrTypes = new ArrayList(super.getAttributeTypes());
        attrTypes.add(getIfNumberAttributeType());
        return attrTypes;
        
    }

    private AttributeType getIfNumberAttributeType() {
        if (m_ifNumberType == null) {
            MibObject ifNumberMibObject = new MibObject();
            ifNumberMibObject.setOid(SnmpCollector.INTERFACES_IFNUMBER);
            ifNumberMibObject.setAlias("ifNumber");
            ifNumberMibObject.setType("gauge");
            ifNumberMibObject.setInstance("0");

            m_ifNumberType = AttributeType.create(this, getCollectionName(), ifNumberMibObject);
        }
        return m_ifNumberType;
    }
    
    

    
}
