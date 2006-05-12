package org.opennms.netmgt.collectd;

import java.util.Collection;


public abstract class DbCollectionResource extends CollectionResource {

    private CollectionAgent m_agent;

    public DbCollectionResource(ResourceType def, CollectionAgent agent) {
        super(def);
        m_agent = agent;
    }

    public Collection getAttributeList() {
        return getResourceType().getAttributeTypes();
    }

    public CollectionAgent getCollectionAgent() {
    	return m_agent;
    }

    public abstract int getType();


}
