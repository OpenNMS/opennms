package org.opennms.netmgt.collectd;

import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfigFactory;

public abstract class ResourceDef {

    private CollectionAgent m_agent;
    private String m_collectionName;

    public ResourceDef(CollectionAgent agent, String collectionName) {
        m_agent = agent;
        m_collectionName = collectionName;
    }

    public CollectionAgent getAgent() {
        return m_agent;
    }

    public String getCollectionName() {
        return m_collectionName;
    }

    abstract public int getType();

    List getAttributeDefs() {
        return DataCollectionConfigFactory.getInstance().buildCollectionAttributes(getCollectionName(), getAgent().getSysObjectId(), getAgent().getHostAddress(), getType());
    }

    protected boolean hasDataToCollect() {
        return !getAttributeDefs().isEmpty();
    }
}
