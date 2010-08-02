package org.opennms.netmgt.collectd.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.CollectionSet;
import org.opennms.netmgt.collectd.CollectionSetVisitor;
import org.opennms.netmgt.collectd.ServiceCollector;

public class JdbcCollectionSet implements CollectionSet {
    private int m_status;
    private List<JdbcCollectionResource> m_collectionResources;
    
    public JdbcCollectionSet(CollectionAgent agent) {
        m_status = ServiceCollector.COLLECTION_FAILED;
        m_collectionResources = new ArrayList<JdbcCollectionResource>();
    }
    
    public int getStatus() {
        return m_status;
    }
    
    public void setStatus(int status) {
        m_status = status;
    }

    public List<JdbcCollectionResource> getCollectionResources() {
        return m_collectionResources;
    }

    public void setCollectionResources(List<JdbcCollectionResource> collectionResources) {
        m_collectionResources = collectionResources;
    }

    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);

        for(CollectionResource resource : getCollectionResources())
                resource.visit(visitor);

        visitor.completeCollectionSet(this);
    }

    public boolean ignorePersist() {
        return false;
    }

}
