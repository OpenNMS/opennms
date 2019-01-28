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
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class LinkdVertex extends SimpleLeafVertex {

    public static LinkdVertex create(OnmsTopologyVertex tvertex) {
        LinkdVertex vertex = new LinkdVertex(tvertex.getId());
        vertex.setNodeID(tvertex.getNodeid());
        vertex.setLabel(tvertex.getLabel());
        vertex.setIpAddress(tvertex.getAddress());
        vertex.setIconKey(tvertex.getIconKey());
        vertex.setTooltipText(tvertex.getToolTipText());
        return vertex;
    }

    private Set<ProtocolSupported> m_protocolSupported = EnumSet.noneOf(ProtocolSupported.class);

    public LinkdVertex(String id) {
        super(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD, id, 0, 0);
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
