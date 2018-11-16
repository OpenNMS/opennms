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
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;

public class LldpOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater {

    private final LldpTopologyService m_lldpTopologyService;

    public LldpOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, LldpTopologyService lldpTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_lldpTopologyService = lldpTopologyService;
    }            
    
    @Override
    public String getName() {
        return "LldpTopologyUpdater";
    }

    @Override
    public OnmsTopology getTopology() {
        Map<Integer, OnmsNode> nodeMap= getNodeMap();
        OnmsTopology topology = new OnmsTopology();
        m_lldpTopologyService.findAllLldpElements().stream().forEach(element -> {
            OnmsTopologyVertex vertex = OnmsTopologyVertex.create(nodeMap.get(element.getNode().getId()));
            vertex.getProtocolSupported().add(ProtocolSupported.LLDP.name());
            topology.getVertices().add(vertex);
        });
        

        for (Pair<LldpLink, LldpLink> pair : m_lldpTopologyService.matchLldpLinks()) {
            LldpLink sourceLink = pair.getLeft();
            LldpLink targetLink = pair.getRight();
            OnmsTopologyVertex source = topology.getVertex(sourceLink.getNode().getId().toString());
            OnmsTopologyVertex target = topology.getVertex(targetLink.getNode().getId().toString());
            OnmsTopologyEdge edge = OnmsTopologyEdge.create(source, target, sourceLink.getLldpPortIfindex(),targetLink.getLldpPortIfindex());
            edge.setSourcePort(sourceLink.getLldpPortDescr());
            edge.setSourceIfIndex(sourceLink.getLldpPortIfindex());
            edge.setSourceAddr(sourceLink.getLldpPortId());
            edge.setTargetPort(targetLink.getLldpPortDescr());
            edge.setTargetIfIndex(targetLink.getLldpPortIfindex());
            edge.setTargetAddr(targetLink.getLldpRemPortId());
            edge.setDiscoveredBy(ProtocolSupported.LLDP.name());
            topology.getEdges().add(edge);
       }
        
        return topology;
    }

    @Override
    public String getId() {
        return ProtocolSupported.LLDP.name();
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.LLDP.name();
    }
            
}

