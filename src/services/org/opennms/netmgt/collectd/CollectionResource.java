package org.opennms.netmgt.collectd;

import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;

public abstract class CollectionResource extends AbstractResource {

    private CollectionAgent m_agent;
    private String m_collectionName;

    public CollectionResource(CollectionAgent agent, String collectionName) {
        m_agent = agent;
        m_collectionName = collectionName;
    }

    public List getAttributeList() {
        return DataCollectionConfigFactory.getInstance()
        .buildCollectionAttributes(m_collectionName, m_agent.getSysObjectId(),
                m_agent.getHostAddress(), getType());
    }

    public abstract int getType();

    public CollectionAgent getCollectionAgent() {
    	return m_agent;
    }
    
    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
