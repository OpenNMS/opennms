package org.opennms.netmgt.enlinkd;

public class NodeDiscoveryBridgeTopology extends NodeDiscovery {

    public NodeDiscoveryBridgeTopology(EnhancedLinkd linkd, Node node) {
        super(linkd, node);
    }

    @Override
    protected void runCollection() {
        m_linkd.getQueryManager().storeBridgeTopology(getNodeId());
    }

    @Override
    public String getInfo() {
        return "ReadyRunnable DiscoveryBridgeTopology" + " node=" + getNodeId();
    }

    @Override
    public String getName() {
        return "DiscoveryBridgeTopology";
    }

}
