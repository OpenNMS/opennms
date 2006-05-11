package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.opennms.netmgt.snmp.SnmpInstId;

public abstract class ResourceType {
    
    static ResourceType getResourceType(String instanceName, CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        if ("ifIndex".equals(instanceName)) {
            return new IfResourceType(agent, snmpCollection);
        } else {
            return new NodeResourceType(agent, snmpCollection);
        }
    }

    private CollectionAgent m_agent;
    private OnmsSnmpCollection m_snmpCollection;

    public ResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
        m_agent = agent;
        m_snmpCollection = snmpCollection;
    }

    public CollectionAgent getAgent() {
        return m_agent;
    }
    
    protected String getCollectionName() {
        return m_snmpCollection.getName();
    }

    abstract public int getType();

    Collection getAttributeDefs() {
        LinkedHashSet attrList = new LinkedHashSet(m_snmpCollection.getAttributeTypes(m_agent, getType()));
        attrList.addAll(identityAttributeTypes());
        return attrList;
    }
    
    protected Collection identityAttributeTypes() {
        return Collections.EMPTY_SET;
    }

    protected boolean hasDataToCollect() {
        return !getAttributeDefs().isEmpty();
    }

    public abstract CollectionResource findResource(SnmpInstId inst);
}
