package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Leaf vertex representing a node without links that defers the lookup of the icon key,
 * IP address and tooltip text until one of these is requested.
 *
 * This provides significant performance enhancements on systems with thousands of nodes.
 *
 * Used as an alternative to {@link org.opennms.features.topology.api.topo.SimpleLeafVertex}
 *
 * @author jwhite
 */
public class DeferedNodeLeafVertex extends AbstractVertex {
    private static Logger LOG = LoggerFactory.getLogger(DeferedNodeLeafVertex.class);
    private final AbstractLinkdTopologyProvider m_topologyProvider;
    private boolean m_onGetAlreadyCalled = false;

    public DeferedNodeLeafVertex(String namespace, Integer nodeId, String nodeLabel,
            AbstractLinkdTopologyProvider topologyProvider) {
        super(namespace, nodeId.toString(), nodeLabel);
        setNodeID(nodeId);
        setX(0);
        setY(0);

        m_topologyProvider = topologyProvider;
    }

    private synchronized void onGet() {
        // Load the properties on the first get
        if (m_onGetAlreadyCalled) {
            return;
        } else {
            m_onGetAlreadyCalled = true;
        }

        LOG.debug("Loading node details for vertex: {}", this.getLabel());

        OnmsNode node = m_topologyProvider.getNodeDao().get(getNodeID());
        setIconKey(AbstractLinkdTopologyProvider.getIconName(node));

        OnmsIpInterface ip = m_topologyProvider.getAddress(node);
        setIpAddress(ip == null ? null : ip.getIpAddress().getHostAddress());

        setTooltipText(AbstractLinkdTopologyProvider.getNodeTooltipText(node, this, ip));
    }

    public String getIconKey() {
        onGet();
        return super.getIconKey();
    }

    public String getIpAddress() {
        onGet();
        return super.getIpAddress();
    }

    public String getTooltipText() {
        onGet();
        return super.getTooltipText();
    }
}
