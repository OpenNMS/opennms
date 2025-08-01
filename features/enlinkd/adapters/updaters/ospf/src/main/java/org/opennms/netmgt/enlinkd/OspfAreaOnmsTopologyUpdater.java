/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

