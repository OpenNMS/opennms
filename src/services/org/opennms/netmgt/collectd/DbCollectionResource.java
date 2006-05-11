package org.opennms.netmgt.collectd;

import java.util.Collection;


public abstract class DbCollectionResource extends CollectionResource {

    private CollectionAgent m_agent;
    private ResourceType m_resourceDef;

    public DbCollectionResource(ResourceType def, CollectionAgent agent) {
        m_resourceDef = def;
        m_agent = agent;
    }

    public Collection getAttributeList() {
        return m_resourceDef.getAttributeDefs();
    }

    public CollectionAgent getCollectionAgent() {
    	return m_agent;
    }

    public abstract int getType();


}
