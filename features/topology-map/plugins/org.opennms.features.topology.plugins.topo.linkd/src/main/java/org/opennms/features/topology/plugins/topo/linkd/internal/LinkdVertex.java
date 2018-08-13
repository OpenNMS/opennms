/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.ProtocolSupported;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class LinkdVertex extends SimpleLeafVertex {

    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";

    private static final EnumMap<OnmsNode.NodeType, String> s_nodeStatusMap;

    static {
        s_nodeStatusMap = new EnumMap<>(OnmsNode.NodeType.class);
        s_nodeStatusMap.put(OnmsNode.NodeType.ACTIVE, "Active");
        s_nodeStatusMap.put(OnmsNode.NodeType.UNKNOWN, "Unknown");
        s_nodeStatusMap.put(OnmsNode.NodeType.DELETED, "Deleted");
    }

    public static LinkdVertex create(OnmsNode node, OnmsIpInterface primary) {
        LinkdVertex vertex = new LinkdVertex(node);
        vertex.setNodeID(node.getId());
        vertex.setLabel(node.getLabel());
        vertex.setNodeType(s_nodeStatusMap.get(node.getType()));
        vertex.setSysObjectId(node.getSysObjectId());
        if (node.getLocation() != null) {
            vertex.setLocation(node.getLocation().getLocationName());
        }
        vertex.setIpAddress("no ip address");
        if (primary != null) {
            vertex.setIpAddress(InetAddressUtils.str(primary.getIpAddress()));
        }
        vertex.setManaged("Unmanaged");
        if (primary != null && primary.isManaged()) {
            vertex.setManaged("Managed");
        }
        return vertex;
    }

    private String m_nodeType;
    private String m_location;
    private String m_sysObjectId;
    private String m_isManaged;

    private Set<ProtocolSupported> m_protocolSupported = EnumSet.noneOf(ProtocolSupported.class);
    
    
    public Set<ProtocolSupported> getProtocolSupported() {
        return m_protocolSupported;
    }

    public String getSysObjectId() {
        return m_sysObjectId;
    }

    public void setSysObjectId(String sysObjectId) {
        m_sysObjectId = sysObjectId;
    }

    public String getNodeType() {
        return m_nodeType;
    }

    public void setNodeType(String nodeType) {
        m_nodeType = nodeType;
    }

    public String getLocation() {
        return m_location;
    }

    public void setLocation(String location) {
        m_location = location;
    }

    public String getManaged() {
        return m_isManaged;
    }

    public void setManaged(String isManaged) {
        m_isManaged = isManaged;
    }

    private LinkdVertex(OnmsNode node) {
        super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, node.getNodeId(), 0, 0);
    }
    
    @Override
    public String getIconKey() {
        if (m_sysObjectId == null) {
            return "linkd.system";
        }
        if (m_sysObjectId.startsWith(".")) {
            return "linkd.system.snmp" + m_sysObjectId;
        }
        return "linkd.system.snmp." + m_sysObjectId;
    }
    
    @Override
    public String getTooltipText() {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(getLabel());
        tooltipText.append(": ");
        tooltipText.append("(");
        tooltipText.append(getIpAddress());
        tooltipText.append(")");
        tooltipText.append("(");
        tooltipText.append(m_nodeType);
        tooltipText.append("/");
        tooltipText.append(m_isManaged);
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if (m_location != null && m_location.trim().length() > 0) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(m_location);
                tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        
        if (m_protocolSupported.size() > 0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append(m_protocolSupported.toString());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        return tooltipText.toString();

    }

}
