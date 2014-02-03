package org.opennms.netmgt.xml.bind;

import org.opennms.netmgt.model.OnmsNode.NodeType;

/**
 */
public class NodeTypeXmlAdapter extends EnumToStringXmlAdapter<NodeType> {
    
    public NodeTypeXmlAdapter() {
        super(NodeType.class, NodeType.UNKNOWN);
    }
}
