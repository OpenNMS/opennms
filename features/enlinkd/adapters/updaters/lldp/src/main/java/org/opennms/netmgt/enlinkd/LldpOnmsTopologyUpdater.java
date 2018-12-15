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

import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class LldpOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater {

    public static OnmsTopologyPort create(OnmsTopologyVertex source,LldpLink sourceLink, LldpLink targetlink ) throws OnmsTopologyException {
        OnmsTopologyPort port = OnmsTopologyPort.create(sourceLink.getId().toString(),source, sourceLink.getLldpPortIfindex());
        port.setPort(sourceLink.getLldpPortDescr());
        port.setAddr(Topology.getRemoteAddress(targetlink));
        port.setToolTipText(Topology.getToolTipText(source.getLabel(), port.getIndex(), port.getPort(), port.getAddr(), null));
        return port;
    }

    private final LldpTopologyService m_lldpTopologyService;

    public LldpOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, LldpTopologyService lldpTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, lldpTopologyService, topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_lldpTopologyService = lldpTopologyService;
    }            
    
    @Override
    public String getName() {
        return "LldpTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() throws OnmsTopologyException {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        OnmsTopology topology = new OnmsTopology();
        for (LldpElement element: m_lldpTopologyService.findAllLldpElements()) {
            topology.getVertices().add(create(nodeMap.get(element.getNode().getId())));
        }
        
        for (TopologyConnection<LldpLink, LldpLink> pair : m_lldpTopologyService.match()) {
            topology.getEdges().add(
                                    OnmsTopologyEdge.create(
                                                            Topology.getDefaultEdgeId(pair.getLeft().getId(), pair.getRight().getId()),
                                                            create(
                                                                   topology.getVertex(pair.getLeft().getNode().getId().toString()), 
                                                                   pair.getLeft(),
                                                                   pair.getRight()
                                                                   ), 
                                                            create(
                                                                   topology.getVertex(pair.getRight().getNode().getId().toString()), 
                                                                   pair.getRight(),
                                                                   pair.getLeft()
                                                                   )
                                                            )
                                    );
       }
        
        return topology;
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.LLDP.name();
    }
            
}

