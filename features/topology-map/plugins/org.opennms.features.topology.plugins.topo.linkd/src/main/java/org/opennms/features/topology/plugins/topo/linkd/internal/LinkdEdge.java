/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

public class LinkdEdge extends AbstractEdge implements Edge {

    public static LinkdEdge create(String id,
            LinkdPort sourceport, LinkdPort targetport,
            ProtocolSupported discoveredBy) {
        
        SimpleConnector source = new SimpleConnector(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD, sourceport.getVertex().getId()+"-"+id+"-connector", sourceport.getVertex());
        SimpleConnector target = new SimpleConnector(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD, targetport.getVertex().getId()+"-"+id+"-connector", targetport.getVertex());

        LinkdEdge edge = new LinkdEdge(id, sourceport, targetport,source, target,discoveredBy);
        
        return edge;
    }
    
    private final LinkdPort m_sourcePort;
    private final LinkdPort m_targetPort;
    private final ProtocolSupported m_discoveredBy;
    
    public LinkdEdge(String id, LinkdPort source, LinkdPort target, ProtocolSupported discoveredBy) {
        super(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD, id, source.getVertex(), target.getVertex());
        m_discoveredBy = discoveredBy;
        m_sourcePort = source;
        m_targetPort = target;
    }

    public LinkdEdge(String id, LinkdPort sourcePort, LinkdPort targetPort, SimpleConnector source,
            SimpleConnector target, ProtocolSupported discoveredBy) {
        super(OnmsTopology.TOPOLOGY_NAMESPACE_LINKD, id, source, target);
        m_sourcePort = sourcePort;
        m_targetPort = targetPort;
        m_discoveredBy = discoveredBy;
    }

    // Constructor to make cloneable easier for sub classes
    private LinkdEdge(LinkdEdge edgeToClone) {
            this(edgeToClone.getId(), 
                 edgeToClone.getSourcePort().clone(), 
                 edgeToClone.getTargetPort().clone(),
                 edgeToClone.getSource().clone(),
                 edgeToClone.getTarget().clone(),
                 edgeToClone.getDiscoveredBy());

            setLabel(edgeToClone.getLabel());
            setStyleName(edgeToClone.getStyleName());
            setTooltipText(edgeToClone.getTooltipText());
    }

    @Override
    public LinkdEdge clone() {
            return new LinkdEdge(this);
    }

    @Override
    public String  getTooltipText() {       
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("<p>");
        tooltipText.append("discovery by: ");
        tooltipText.append(m_discoveredBy.toString());
        tooltipText.append("</p>");
    
        tooltipText.append("<p>");
        tooltipText.append(m_sourcePort.getToolTipText());
        tooltipText.append("</p>");
        
        tooltipText.append("<p>");
        tooltipText.append(m_targetPort.getToolTipText());
        tooltipText.append("</p>");
        return tooltipText.toString();
    }

    
    public ProtocolSupported getDiscoveredBy() {
        return m_discoveredBy;
    }

    public LinkdPort getSourcePort() {
        return m_sourcePort;
    }

    public LinkdPort getTargetPort() {
        return m_targetPort;
    }
    
}
