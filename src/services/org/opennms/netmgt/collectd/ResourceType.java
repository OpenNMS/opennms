package org.opennms.netmgt.collectd;

import java.util.Collection;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.SnmpInstId;

public abstract class ResourceType {
    
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
    
    protected OnmsSnmpCollection getCollection() {
        return m_snmpCollection;
    }

    abstract public Collection getAttributeTypes();

    protected boolean hasDataToCollect() {
        return !getAttributeTypes().isEmpty();
    }

    public abstract CollectionResource findResource(SnmpInstId inst);
    
    public abstract Collection getResources();
    
    public Category log() { return ThreadCategory.getInstance(getClass()); }
}
