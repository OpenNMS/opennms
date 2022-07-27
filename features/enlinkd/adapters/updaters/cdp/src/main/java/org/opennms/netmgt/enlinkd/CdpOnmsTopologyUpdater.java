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

import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

import com.google.common.collect.Table;

public class CdpOnmsTopologyUpdater extends TopologyUpdater {

    public static CdpOnmsTopologyUpdater clone (CdpOnmsTopologyUpdater bpu) {
        CdpOnmsTopologyUpdater update = new CdpOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getCdpTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
 
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex vertex,CdpLinkTopologyEntity cdpLink, SnmpInterfaceTopologyEntity snmpiface ) {
        OnmsTopologyPort port = OnmsTopologyPort.create(cdpLink.getId().toString(),vertex, cdpLink.getCdpCacheIfIndex());
        port.setIfindex(cdpLink.getCdpCacheIfIndex());
        port.setIfname(cdpLink.getCdpInterfaceName());
        port.setAddr(Topology.getAddress(cdpLink));
        port.setToolTipText(Topology.getPortTextString(vertex.getLabel(),port.getIfindex(),port.getAddr(),snmpiface));
        return port;
    }
    
    private final CdpTopologyService m_cdpTopologyService;

    public CdpOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, CdpTopologyService cdpTopologyService, NodeTopologyService nodeTopologyService) {
        super(cdpTopologyService,topologyDao,nodeTopologyService);
        m_cdpTopologyService = cdpTopologyService;
    }            
    
    @Override
    public String getName() {
        return "CdpTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();
        for (CdpElementTopologyEntity element: m_cdpTopologyService.findAllCdpElements()) {
            topology.getVertices().add(create(nodeMap.get(element.getNodeId()),ipMap.get(element.getNodeId())));
        }
        
        for(TopologyConnection<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair : m_cdpTopologyService.match()) {
            topology.getEdges().add(
                OnmsTopologyEdge.create(
                        Topology.getDefaultEdgeId(pair.getLeft().getId().intValue(), pair.getRight().getId().intValue()),
                        create(topology.getVertex(pair.getLeft().getNodeIdAsString()),
                               pair.getLeft(),
                               nodeToOnmsSnmpTable.get(pair.getLeft().getNodeId(), pair.getLeft().getCdpCacheIfIndex())),
                        create(topology.getVertex(pair.getRight().getNodeIdAsString()),
                               pair.getRight(),
                               nodeToOnmsSnmpTable.get(pair.getRight().getNodeId(), pair.getRight().getCdpCacheIfIndex())
                               )
                        )
                );
       }
        
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.CDP);
    }

    public CdpTopologyService getCdpTopologyService() {
        return m_cdpTopologyService;
    }

}

