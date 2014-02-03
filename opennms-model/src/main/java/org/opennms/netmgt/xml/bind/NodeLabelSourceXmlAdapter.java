package org.opennms.netmgt.xml.bind;

import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;

/**
 */
public class NodeLabelSourceXmlAdapter extends EnumToStringXmlAdapter<NodeLabelSource> {
    
    public NodeLabelSourceXmlAdapter() {
        super(NodeLabelSource.class, NodeLabelSource.UNKNOWN);
    }
}
