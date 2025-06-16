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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.UserDefinedLinkTopologyService;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class UserDefinedLinkTopologyUpdater extends TopologyUpdater {

    private final UserDefinedLinkTopologyService udlTopologyService;

    public UserDefinedLinkTopologyUpdater(UserDefinedLinkTopologyService udlTopologyService, OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService) {
        super(udlTopologyService, topologyDao, nodeTopologyService);
        this.udlTopologyService = Objects.requireNonNull(udlTopologyService);
    }

    @Override
    public String getName() {
        return UserDefinedLinkTopologyUpdater.class.getName();
    }

    @Override
    public OnmsTopology buildTopology() {
        final OnmsTopology topology = new OnmsTopology();

        // Load all of the UDLs
        final List<UserDefinedLink> udls = udlTopologyService.findAllUserDefinedLinks();

        // Determine the set of nodes that are referenced by the UDLs
        final Set<Integer> referencedNodes = new HashSet<>();
        // Sources
        referencedNodes.addAll(udls.stream()
                .map(UserDefinedLink::getNodeIdA)
                .collect(Collectors.toSet()));
        // Targets
        referencedNodes.addAll(udls.stream()
                .map(UserDefinedLink::getNodeIdZ)
                .collect(Collectors.toSet()));

        // Add vertices for all of the nodes
        final Map<Integer, NodeTopologyEntity> nodeMap = getNodeMap();
        final Map<Integer, IpInterfaceTopologyEntity> ipMap = getIpPrimaryMap();
        final Map<Integer, OnmsTopologyVertex> nodeVertexMap = new LinkedHashMap<>();
        for (Integer nodeId: referencedNodes) {
            final OnmsTopologyVertex nodeVertex = create(nodeMap.get(nodeId), ipMap.get(nodeId));
            nodeVertexMap.put(nodeId, nodeVertex);
            topology.addVertex(nodeVertex);
        }

        // Now create the edges
        for (UserDefinedLink udl : udls) {
            final String uniqueLinkId = Integer.toString(udl.getDbId());
            final OnmsTopologyPort sourcePort = OnmsTopologyPort.create(uniqueLinkId + "|A", nodeVertexMap.get(udl.getNodeIdA()), null);
            sourcePort.setToolTipText(udl.getComponentLabelA());

            final OnmsTopologyPort targetPort = OnmsTopologyPort.create(uniqueLinkId + "|Z", nodeVertexMap.get(udl.getNodeIdZ()), null);
            targetPort.setToolTipText(udl.getComponentLabelZ());

            final OnmsTopologyEdge edge = OnmsTopologyEdge.create(uniqueLinkId, sourcePort, targetPort);
            edge.setToolTipText(udl.getLinkLabel());
            topology.addEdge(edge);
        }

        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.USERDEFINED);
    }

}

