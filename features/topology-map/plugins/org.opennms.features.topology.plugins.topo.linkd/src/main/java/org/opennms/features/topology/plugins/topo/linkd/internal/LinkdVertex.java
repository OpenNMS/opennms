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

import java.util.EnumSet;
import java.util.Set;

import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;

public class LinkdVertex extends SimpleLeafVertex {

    public static LinkdVertex create(BridgePort designated) {
        LinkdVertex cloudVertex = new LinkdVertex(Topology.getId(designated));
        cloudVertex.setLabel("Shared Segment");
        cloudVertex.setIconKey("cloud");
        cloudVertex.setTooltipText("'Shared Segment' designated port: " +designated.printTopology());
        return cloudVertex;
        
    }

    public static LinkdVertex create(MacPort port) {
        LinkdVertex vertex = new LinkdVertex(Topology.getId(port));
        vertex.setLabel(Topology.getId(port));
        vertex.setIconKey(Topology.getIconKey(port));
        vertex.setTooltipText(Topology.getToolTipText(port));
        return vertex;
        
    }
    
    public static LinkdVertex create(NodeTopologyEntity node) {
        LinkdVertex vertex = new LinkdVertex(node.getId());
        vertex.setNodeID(node.getNodeId());
        vertex.setLabel(node.getLabel());
        vertex.setIpAddress(node.getAddress());
        vertex.setIconKey(Topology.getIconKey(node));
        vertex.setTooltipText(Topology.getToolTipText(node));
        return vertex;
    }
    private Set<ProtocolSupported> m_protocolSupported = EnumSet.noneOf(ProtocolSupported.class);

    private LinkdVertex(String id) {
        super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id, 0, 0);
    }
    
    
    @Override
    public String getTooltipText() {
        StringBuffer tooltipText = new StringBuffer();
        tooltipText.append("<p>");
        tooltipText.append(super.getTooltipText());
        tooltipText.append("</p>");
        if (m_protocolSupported.size() > 0) {
            tooltipText.append("<p>");
            tooltipText.append(m_protocolSupported.toString());
            tooltipText.append("</p>");
        }
        return tooltipText.toString();
    }

    public Set<ProtocolSupported> getProtocolSupported() {
        return m_protocolSupported;
    }

}
