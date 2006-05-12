package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.snmp.SnmpInstId;

public class IfAliasResourceType extends ResourceType {

    private IfResourceType m_ifResourceType;
    private Map m_aliasedIfs = new HashMap();
    private ServiceParameters m_params;

    public IfAliasResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, ServiceParameters params, IfResourceType ifResourceType) {
        super(agent, snmpCollection);
        m_ifResourceType = ifResourceType;
        m_params = params;
    }

    public CollectionResource findResource(SnmpInstId inst) {
        Integer key = new Integer(inst.toInt());
        AliasedResource resource = (AliasedResource) m_aliasedIfs.get(key);
        if (resource == null) {
            IfInfo ifInfo = (IfInfo)m_ifResourceType.findResource(inst);
            
            log().info("Creating an aliased resource for "+ifInfo);
            
            resource = new AliasedResource(this, m_params.getDomain(), ifInfo, m_params.getIfAliasComment());
            
            m_aliasedIfs.put(key, resource);
        }
        return resource;
    }

    public Collection getAttributeTypes() {
        MibObject ifAliasMibObject = new MibObject();
        ifAliasMibObject.setOid(".1.3.6.1.2.1.31.1.1.1.18");
        ifAliasMibObject.setAlias("ifAlias");
        ifAliasMibObject.setType("string");
        ifAliasMibObject.setInstance("ifIndex");

        AttributeType type = new AttributeType(this, getCollectionName(), ifAliasMibObject);
        return Collections.singleton(type);
   }

    public Collection getResources() {
        return m_aliasedIfs.values();
    }
    

}
