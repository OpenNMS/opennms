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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyShared;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(BridgeOnmsTopologyUpdater.class);

    protected final BridgeTopologyService m_bridgeTopologyService;

    public BridgeOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, BridgeTopologyService bridgeTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_bridgeTopologyService = bridgeTopologyService;
    }            
    
    @Override
    public String getName() {
        return "BridgeTopologyUpdaters";
    }

    @Override
    public OnmsTopology getTopology() {
        Map<Integer, Node> nodeMap= getNodeMap();
        OnmsTopology topology = new OnmsTopology();
        for (Triple<List<BridgePort>, List<MacPort>, BridgePort> shared: m_bridgeTopologyService.matchBridgeLinks()) {
            Set<OnmsTopologyPort> ports = new HashSet<>();
            for (BridgePort bp: shared.getLeft()) {
                Node node = nodeMap.get(bp.getNodeId());
                if (topology.getVertex(node.getId()) == null) {
                    topology.getVertices().add(create(node));
                }
                OnmsTopologyVertex vertex = topology.getVertex(node.getId());
                vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE.name());
                OnmsTopologyPort port = OnmsTopologyPort.create(vertex, bp.getBridgePortIfIndex());
                port.setAddr(Integer.toString(bp.getBridgePort()));
                port.setPort(bp.printTopology());
                ports.add(port);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getTopology: adding: {}", bp.printTopology());
                }
            }
            for (MacPort macPort: shared.getMiddle()) {
                String id = Topology.getId(macPort);
                if (topology.getVertex(id) ==  null) {
                    if (macPort.getNodeId() != null) {
                        Node node = nodeMap.get(macPort.getNodeId());
                        if (topology.getVertex(node.getId()) == null) {
                            topology.getVertices().add(create(node));
                        }
                    } else {
                        topology.getVertices().add(create(macPort));
                    }
                }
                OnmsTopologyVertex vertex = topology.getVertex(id);
                vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE.name());
                OnmsTopologyPort port = OnmsTopologyPort.create(vertex, macPort.getMacPortIfIndex());
                port.setAddr(macPort.getIpMacInfo());
                if (macPort.getMacPortIfIndex() != null) {
                    port.setPort(Integer.toString(macPort.getMacPortIfIndex()));
                } else {
                    port.setPort("-1");
                }
                ports.add(port);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getTopology: adding: {}", macPort.printTopology());
                }
            }
            OnmsTopologyShared edge = OnmsTopologyShared.create(
                                                          Topology.getId(shared.getRight()), 
                                                          ports.toArray(new OnmsTopologyPort[ports.size()]));
            edge.setDiscoveredBy(ProtocolSupported.BRIDGE.name());
            topology.getEdges().add(edge);

        }
        return topology;
    }

    @Override
    public String getId() {
        return ProtocolSupported.BRIDGE.name();
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.BRIDGE.name();
    }
            
}

