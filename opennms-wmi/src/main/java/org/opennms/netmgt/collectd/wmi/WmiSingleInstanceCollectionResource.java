package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceParameters;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.List;
import java.util.Collections;
import java.io.File;

public class WmiSingleInstanceCollectionResource extends WmiCollectionResource {

    public WmiSingleInstanceCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    public String getResourceTypeName() {
        return "node";
    }

    public String getInstance() {
        return null;
    }

    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type[node]";
    }

}
