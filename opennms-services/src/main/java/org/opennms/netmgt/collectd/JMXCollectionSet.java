package org.opennms.netmgt.collectd;

import java.util.Date;

import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;

class JMXCollectionSet implements CollectionSet {
    private int m_status;
    private Date m_timestamp;
    private JMXCollectionResource m_collectionResource;

    JMXCollectionSet(CollectionAgent agent, String resourceName) {
        m_status=ServiceCollector.COLLECTION_FAILED;
        m_collectionResource=new JMXCollectionResource(agent, resourceName);
    }

    public JMXCollectionResource getResource() {
        return m_collectionResource;
    }

    public void setStatus(int status) {
        m_status=status;
    }

    @Override
    public int getStatus() {
        return m_status;
    }

    @Override
    public void visit(CollectionSetVisitor visitor) {
        visitor.visitCollectionSet(this);
        m_collectionResource.visit(visitor);
        visitor.completeCollectionSet(this);
    }

    @Override
	public boolean ignorePersist() {
		return false;
	}

	@Override
	public Date getCollectionTimestamp() {
		return m_timestamp;
	}
    public void setCollectionTimestamp(Date timestamp) {
	this.m_timestamp = timestamp;
	}

}