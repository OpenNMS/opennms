package org.opennms.netmgt.collectd;

import java.util.List;

import org.opennms.netmgt.config.DataCollectionConfigFactory;

public abstract class DbCollectionResource extends CollectionResource {

    private CollectionAgent m_agent;
    private String m_collectionName;

    public DbCollectionResource(CollectionAgent agent, String collectionName) {
        m_agent = agent;
        m_collectionName = collectionName;
    }

    public List getAttributeList() {
        return DataCollectionConfigFactory.getInstance()
        .buildCollectionAttributes(m_collectionName, m_agent.getSysObjectId(),
                m_agent.getHostAddress(), getType());
    }

    public CollectionAgent getCollectionAgent() {
    	return m_agent;
    }

    public abstract int getType();


}
