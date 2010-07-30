package org.opennms.netmgt.collectd.jdbc;

import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.ServiceParameters;

public abstract class JdbcCollectionResource extends AbstractCollectionResource {
    
    public JdbcCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    public boolean rescanNeeded() {
        // A rescan is never needed for the JdbcCollector, at least on resources
        return false;
    }
    
    public void setAttributeValue(CollectionAttributeType type, String value) {
        JdbcCollectionAttribute attr = new JdbcCollectionAttribute(this, type, type.getName(), value);
        addAttribute(attr);
    }

    public int getType() {
        return -1; //Is this right?
    }

    public abstract String getResourceTypeName();

    public abstract String getInstance();

}
