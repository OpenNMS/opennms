package org.opennms.netmgt.model;

public class OnmsTopologyVertex extends OnmsTopologyRef {

    public static OnmsTopologyVertex create(OnmsNode node) {
        if (node != null) {
            return new OnmsTopologyVertex(node);
        }
        return null;
    }
    
    private final OnmsNode m_node;
    
    private OnmsTopologyVertex(OnmsNode node) {
        super(node.getNodeId());
        m_node=node;
    }

    public OnmsNode getNode() {
        return m_node;
    }

    
}
