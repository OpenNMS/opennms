/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

import com.google.common.collect.Table;

public class OspfAreaOnmsTopologyUpdater extends TopologyUpdater {

    public static OspfAreaOnmsTopologyUpdater clone(OspfAreaOnmsTopologyUpdater bpu) {
        OspfAreaOnmsTopologyUpdater update = new OspfAreaOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getOspfTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;

    }

    public static OnmsTopologyPort createNodePort(OnmsTopologyVertex source,
                                          OspfElement sourceElement,
                                          OspfAreaTopologyEntity targetArea) {
        if(Objects.isNull(sourceElement))
            throw new IllegalArgumentException("No OspfElement associated to area : " + targetArea.getOspfAreaId());
        OnmsTopologyPort port = OnmsTopologyPort.create(targetArea.getNodeIdAsString(), source, null);

        port.setAddr(str(targetArea.getOspfAreaId()));
        port.setToolTipText("");
        return port;
    }

    public static OnmsTopologyPort createAreaPort(OnmsTopologyVertex source,
                                                  OspfAreaTopologyEntity sourceArea,
                                                  OspfElement targetElement){
        OnmsTopologyPort port = OnmsTopologyPort.create(str(sourceArea.getOspfAreaId()), source, null);

        port.setAddr(str(sourceArea.getOspfAreaId()));
        port.setToolTipText("");
        return port;
    }

    public static OnmsTopologyVertex createAreaVertex(OspfAreaTopologyEntity area) {
        Objects.requireNonNull(area);

        OnmsTopologyVertex ospfVertex = OnmsTopologyVertex.create(
                area.getOspfAreaId().getHostAddress(),
                "Ospf Area: " + area.getOspfAreaId().getHostAddress(),
                str(area.getOspfAreaId()),
                Topology.getCloudIconKey());
        ospfVertex.setToolTipText("TBD");
        return ospfVertex;
    }

    public static OnmsTopologyVertex createOspfNodeVertex(NodeTopologyEntity node, OspfElement ospfElement) {
        OnmsTopologyVertex ospfNodeVertex = OnmsTopologyVertex.create(
                node.getId().toString(),
                node.getLabel(),
                str(ospfElement.getOspfRouterId()),
                Topology.getIconKey(node)
        );
        ospfNodeVertex.setToolTipText(node.getLabel() + "/" + node.getId() + "ospf stat: " + ospfElement.getOspfAdminStat());
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
        Map<Integer, NodeTopologyEntity> nodeMap = getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap = getIpPrimaryMap();
        Table<Integer, Integer, SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();
        final Map<Integer, OspfElement> ospfElementMap =
        m_ospfTopologyService.
                findAllOspfElements().stream().collect(Collectors.toMap(e->e.getNode().getId(), Function.identity()));



        for (OspfAreaTopologyEntity area : getOspfTopologyService().findAllOspfAreas()) {

            if (topology.getVertex(area.getNodeIdAsString()) == null) {
                topology.getVertices().add(createOspfNodeVertex(nodeMap.get(area.getNodeId()), ospfElementMap.get(area.getNodeId())));
            }
            if (topology.getVertex(area.getOspfAreaId().getHostAddress()) == null) {
                topology.getVertices().add(createAreaVertex(area));
            }

            OnmsTopologyVertex nodeVertex = topology.getVertex(area.getNodeIdAsString());
            OnmsTopologyVertex areaVertex = topology.getVertex(area.getOspfAreaId().getHostAddress());
            topology.getEdges().add(OnmsTopologyEdge.create(
                            Topology.getDefaultEdgeId(nodeVertex.getId(), areaVertex.getId()),
                            createNodePort(nodeVertex, ospfElementMap.get(area.getNodeId()), area),
                            createAreaPort(areaVertex, area, ospfElementMap.get(area.getNodeId()))
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

