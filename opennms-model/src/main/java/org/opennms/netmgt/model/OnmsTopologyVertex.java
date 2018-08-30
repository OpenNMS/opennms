/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
