package org.opennms.netmgt.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import org.opennms.netmgt.model.topology.Topology;

public class OnmsTopologyVertex extends OnmsTopologyRef {

    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
    private static final EnumMap<OnmsNode.NodeType, String> s_nodeStatusMap;

    static {
        s_nodeStatusMap = new EnumMap<>(OnmsNode.NodeType.class);
        s_nodeStatusMap.put(OnmsNode.NodeType.ACTIVE, "Active");
        s_nodeStatusMap.put(OnmsNode.NodeType.UNKNOWN, "Unknown");
        s_nodeStatusMap.put(OnmsNode.NodeType.DELETED, "Deleted");
    }

    public static OnmsTopologyVertex create(OnmsNode node) {
        if (node != null) {
            return new OnmsTopologyVertex(node);
        }
        return null;
    }
    
    private final OnmsNode m_node;
    private Set<Topology.ProtocolSupported> m_protocolSupported = EnumSet.noneOf(Topology.ProtocolSupported.class);

    private OnmsTopologyVertex(OnmsNode node) {
        super(node.getNodeId());
        m_node=node;
    }

    public OnmsNode getNode() {
        return m_node;
    }

    public String getIconKey() {
        if (m_node.getSysObjectId() == null) {
            return "linkd.system";
        }
        if (m_node.getSysObjectId().startsWith(".")) {
            return "linkd.system.snmp" + m_node.getSysObjectId();
        }
        return "linkd.system.snmp." + m_node.getSysObjectId();
    }

    public String getTooltipText() {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(m_node.getLabel());
        tooltipText.append(": ");
        //FIXME add ip address (shold be loopback first
//        tooltipText.append("(");
//        tooltipText.append(getIpAddress());
//        tooltipText.append(")");
        tooltipText.append("(");
        tooltipText.append(s_nodeStatusMap.get(m_node.getType()));
        tooltipText.append("/Managed");
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if (m_node.getLocation() != null && m_node.getLocation().getLocationName().trim().length() > 0) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(m_node.getLocation().getLocationName());
                tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        
        if (m_protocolSupported.size() > 0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append(m_protocolSupported.toString());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        return tooltipText.toString();

    }

    public Set<Topology.ProtocolSupported> getProtocolSupported() {
        return m_protocolSupported;
    }

    public void setProtocolSupported(
            Set<Topology.ProtocolSupported> protocolSupported) {
        m_protocolSupported = protocolSupported;
    }

}
