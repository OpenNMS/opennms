package org.opennms.netmgt.collectd.jdbc;

import org.opennms.netmgt.collectd.CollectionAgent;

public class JdbcSingleInstanceCollectionResource extends JdbcCollectionResource {
    
    public JdbcSingleInstanceCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    @Override
    public String getResourceTypeName() {
        return "node";
    }

    @Override
    public String getInstance() {
        return null;
    }

    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type[node]";
    }
}
