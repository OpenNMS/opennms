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

package org.opennms.netmgt.enlinkd;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;

public class IsisOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater {


    private final IsisTopologyService m_isisTopologyService;

    public IsisOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, IsisTopologyService isisTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_isisTopologyService = isisTopologyService;
    }            
    
    @Override
    public String getName() {
        return "IsIsTopologyUpdater";
    }

    @Override
    public OnmsTopology getTopology() {
        Map<Integer, OnmsNode> nodeMap= getNodeMap();
        OnmsTopology topology = new OnmsTopology();
        m_isisTopologyService.findAllIsIsElements().stream().forEach(element -> {
            OnmsTopologyVertex vertex = OnmsTopologyVertex.create(nodeMap.get(element.getNode().getId()));
            vertex.getProtocolSupported().add(ProtocolSupported.ISIS.name());
            topology.getVertices().add(vertex);
        });
        
        for(Pair<IsIsLink, IsIsLink> pair : m_isisTopologyService.matchIsIsLinks()) {
            IsIsLink sourceLink = pair.getLeft();
            IsIsLink targetLink = pair.getRight();

            OnmsTopologyVertex source = topology.getVertex(sourceLink.getNode().getId().toString());
            OnmsTopologyVertex target = topology.getVertex(targetLink.getNode().getId().toString());
            OnmsTopologyEdge edge = OnmsTopologyEdge.create(source, target, sourceLink.getIsisCircIfIndex(),targetLink.getIsisCircIfIndex());
            edge.setSourcePort(targetLink.getIsisISAdjNeighSNPAAddress());
            edge.setSourceIfIndex(sourceLink.getIsisCircIfIndex());
            edge.setSourceAddr(targetLink.getIsisISAdjNeighSNPAAddress());
            edge.setTargetPort(sourceLink.getIsisISAdjNeighSNPAAddress());
            edge.setTargetIfIndex(targetLink.getIsisCircIfIndex());
            edge.setTargetAddr(sourceLink.getIsisISAdjNeighSNPAAddress());
            edge.setDiscoveredBy(ProtocolSupported.ISIS.name());
            topology.getEdges().add(edge);
       }
        
        return topology;
    }

    @Override
    public String getId() {
        return ProtocolSupported.ISIS.name();
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.ISIS.name();
    }
            
}

