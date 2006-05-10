package org.opennms.netmgt.collectd;

import java.util.List;


public abstract class DbCollectionResource extends CollectionResource {

    private CollectionAgent m_agent;
    private String m_collectionName;
    private ResourceDef m_resourceDef;

    public DbCollectionResource(ResourceDef def, CollectionAgent agent, String collectionName) {
        m_resourceDef = def;
        m_agent = agent;
        m_collectionName = collectionName;
    }

    public List getAttributeList() {
        return m_resourceDef.getAttributeDefs();
    }

    public CollectionAgent getCollectionAgent() {
    	return m_agent;
    }

    public abstract int getType();


}
