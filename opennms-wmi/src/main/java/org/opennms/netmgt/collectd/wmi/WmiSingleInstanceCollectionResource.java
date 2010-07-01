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

/**
 * <p>WmiSingleInstanceCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiSingleInstanceCollectionResource extends WmiCollectionResource {

    /**
     * <p>Constructor for WmiSingleInstanceCollectionResource.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public WmiSingleInstanceCollectionResource(CollectionAgent agent) {
        super(agent);
    }

    /**
     * <p>getResourceTypeName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName() {
        return "node";
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Node[" + m_agent.getNodeId() + "]/type[node]";
    }

}
