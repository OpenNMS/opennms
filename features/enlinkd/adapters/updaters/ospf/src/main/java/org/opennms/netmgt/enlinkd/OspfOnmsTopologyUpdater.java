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
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class OspfOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater {

    protected final OspfTopologyService m_ospfTopologyService;

    public OspfOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, OspfTopologyService ospfTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, ospfTopologyService,topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_ospfTopologyService = ospfTopologyService;
    }            
    
    @Override
    public String getName() {
        return "OspfTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, Node> nodeMap=getNodeMap();
        OnmsTopology topology = new OnmsTopology();
        m_ospfTopologyService.findAllOspfElements().stream().forEach(element -> {
            topology.getVertices().add(create(nodeMap.get(element.getNode().getId()),ProtocolSupported.OSPF));
        });
        
        for(Pair<OspfLink, OspfLink> pair : m_ospfTopologyService.matchOspfLinks()) {
            OspfLink sourceLink = pair.getLeft();
            OspfLink targetLink = pair.getRight();
            OnmsTopologyVertex source = topology.getVertex(sourceLink.getNode().getId().toString());
            OnmsTopologyVertex target = topology.getVertex(targetLink.getNode().getId().toString());
            OnmsTopologyPort sourcePort = OnmsTopologyPort.create(source, sourceLink.getOspfIfIndex());
            sourcePort.setPort(InetAddressUtils.str(sourceLink.getOspfIpAddr()));
            sourcePort.setAddr(InetAddressUtils.str(targetLink.getOspfRemIpAddr()));
            OnmsTopologyPort targetPort = OnmsTopologyPort.create(target, targetLink.getOspfIfIndex());
            targetPort.setPort(InetAddressUtils.str(targetLink.getOspfIpAddr()));
            targetPort.setAddr(InetAddressUtils.str(sourceLink.getOspfRemIpAddr()));
            String id = Topology.getDefaultEdgeId(sourceLink.getId(), targetLink.getId());
            topology.getEdges().add(OnmsTopologyEdge.create(id,sourcePort, targetPort));
       }
        
        return topology;
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.OSPF.name();
    }
            
}

