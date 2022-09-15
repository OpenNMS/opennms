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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Table;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

import static org.opennms.core.utils.InetAddressUtils.str;

public class OspfAreaOnmsTopologyUpdater extends TopologyUpdater {

    public static OspfAreaOnmsTopologyUpdater clone (OspfAreaOnmsTopologyUpdater bpu) {
        OspfAreaOnmsTopologyUpdater update = new OspfAreaOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getOspfTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;

    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source,
                                          OspfLinkTopologyEntity sourcelink,
                                          OspfLinkTopologyEntity targetlink,
                                          SnmpInterfaceTopologyEntity snmpiface) {
        OnmsTopologyPort port = OnmsTopologyPort.create(str(sourcelink.getOspfIfAreaId()),source, sourcelink.getOspfIfIndex());
        port.setIfindex(sourcelink.getOspfIfIndex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());
        }
        port.setAddr(str(targetlink.getOspfIfAreaId()));
        port.setToolTipText("");
        return port;
    }

    public static OnmsTopologyVertex createAreaVertex(InetAddress ospfAreaAddress) {
        OnmsTopologyVertex ospfVertex = OnmsTopologyVertex.create(
                "o:"+ospfAreaAddress.toString(),
                "Ospf Area: ",
                str(ospfAreaAddress),
                Topology.getCloudIconKey());
        ospfVertex.setToolTipText("");
        return ospfVertex;
    }

    public static OnmsTopologyVertex createOspfNodeVertex(NodeTopologyEntity node, OspfElement ospfElement) {
        OnmsTopologyVertex ospfNodeVertex = OnmsTopologyVertex.create(
                node.getId().toString(),
                node.getLabel(),
                str(ospfElement.getOspfRouterId()),
                Topology.getIconKey(node)
        );
        ospfNodeVertex.setToolTipText( node.getLabel() + "/" + ospfElement.getNode().getNodeId() + "ospf stat: " + ospfElement.getOspfAdminStat());
        return ospfNodeVertex;
    }
    private final OspfTopologyService m_ospfTopologyService;

    public OspfAreaOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, OspfTopologyService ospfTopologyService, NodeTopologyService nodeTopologyService) {
        super(ospfTopologyService, topologyDao, nodeTopologyService);
        m_ospfTopologyService = ospfTopologyService;
    }            

    @Override
    public String getName() {
        return "OspfAreaTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap=getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer, SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();
        final Map<Integer, OnmsTopologyVertex> ospfElementMap = new HashMap<>();
        m_ospfTopologyService.
                findAllOspfElements().
                forEach(e-> ospfElementMap.put(e.getNode().getId(),createOspfNodeVertex(nodeMap.get(e.getNode().getId()),e)));

        final Map<InetAddress, OnmsTopologyVertex> areaMap = new HashMap<>();

        // Use ospfAreas to associate more Area info to the map
//        final List<OspfAreaTopologyEntity> ospfAreas = m_ospfTopologyService.findAllOspfAreas();
//        ospfAreas
//                .stream()
//                .map(a -> a.getOspfAreaId())
//                .collect(Collectors.toSet())
//                .forEach(ip -> areaMap.put(ip, createAreaVertex(ip)));

//        areaMap.values().forEach(v -> topology.getVertices().add(v));
//        ospfAreas
//                .forEach(a -> {
//                    topology.getEdges().add(
//                        createAreaEdge(ospfElementMap.get(a.getNodeId()),areaMap.get(a.getOspfAreaId())));
//                });

        for(TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity> pair : m_ospfTopologyService.match()) {
            OspfLinkTopologyEntity left = pair.getLeft();
            if (topology.getVertex(left.getNodeIdAsString()) == null) {
                topology.getVertices().add(createAreaVertex(left.getOspfIfAreaId()));
            }
            OspfLinkTopologyEntity right = pair.getRight();
            if (topology.getVertex(right.getNodeIdAsString()) == null) {
                topology.getVertices().add(createAreaVertex(right.getOspfIfAreaId()));
            }

            topology.getEdges().add(OnmsTopologyEdge.create(
                            Topology.getDefaultEdgeId(left.getId(), right.getId()),
                    create(
                                    topology.getVertex(left.getNodeIdAsString()),
                                    left,
                                    right,
                                    nodeToOnmsSnmpTable.get(left.getNodeId(), left.getOspfIfIndex())
                            ),
                            create(
                                    topology.getVertex(right.getNodeIdAsString()),
                                    right,
                                    left,
                                    nodeToOnmsSnmpTable.get(right.getNodeId(), right.getOspfIfIndex())
                            )
                    )
            );
        }

        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.OSPFAREA);
    }

    public OspfTopologyService getOspfTopologyService() {
        return m_ospfTopologyService;
    }
            
}

