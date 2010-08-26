package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collectd.CollectionAgent;

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
    public WmiSingleInstanceCollectionResource(final CollectionAgent agent) {
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
