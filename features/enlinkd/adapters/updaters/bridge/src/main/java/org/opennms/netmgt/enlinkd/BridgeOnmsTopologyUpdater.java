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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyShared;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
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
        super(eventforwarder, bridgeTopologyService,topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_bridgeTopologyService = bridgeTopologyService;
    }            
    
    @Override
    public String getName() {
        return "BridgeTopologyUpdaters";
    }

    @Override
    public OnmsTopology buildTopology() throws OnmsTopologyException {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        OnmsTopology topology = new OnmsTopology();

        for (TopologyShared shared : m_bridgeTopologyService.match()){
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTopology: parsing shared dsesignated: {}", shared.getUpPort().printTopology());
            }
            Set<OnmsTopologyPort> ports = new HashSet<>();
            for(BridgePort bp :shared.getBridgePorts()) {
                NodeTopologyEntity node = nodeMap.get(bp.getNodeId());
                if (topology.getVertex(node.getId()) == null) {
                    topology.getVertices().add(create(node));
                }
                OnmsTopologyVertex vertex = topology.getVertex(node.getId());
                OnmsTopologyPort port = OnmsTopologyPort.create(vertex, bp.getBridgePortIfIndex());
                port.setAddr(Topology.getAddress(bp));
                port.setPort(bp.printTopology());
                ports.add(port);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getTopology: added port: {}", bp.printTopology());
                }
            }
            List<MacPort> portsWithoutNode = new ArrayList<>();
            for (MacPort mp :shared.getMacPorts()) {
                if (mp.getNodeId() == null) {
                    portsWithoutNode.add(mp);
                } else {
                    NodeTopologyEntity node = nodeMap.get(mp.getNodeId());
                    if (topology.getVertex(node.getId()) ==  null) {
                        topology.getVertices().add(create(node));
                    }
                    OnmsTopologyVertex vertex = topology.getVertex(node.getId());
                    OnmsTopologyPort port = OnmsTopologyPort.create(vertex, mp.getMacPortIfIndex());
                    port.setAddr(Topology.getAddress(mp));
                    port.setPort(mp.printTopology());
                    ports.add(port);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getTopology: added port: {}", mp.printTopology());
                    }
                }
            }
            if (shared.getCloud() != null || portsWithoutNode.size() > 0) {
                OnmsTopologyVertex cloudMacVertex = create(shared.getCloud(),portsWithoutNode, shared.getUpPort()) ;
                topology.getVertices().add(cloudMacVertex);
                if (shared.getCloud() != null ) {
                    OnmsTopologyPort   cloudMacPort = OnmsTopologyPort.create(cloudMacVertex, -1);
                    cloudMacPort.setAddr(Topology.getAddress(shared.getCloud()));
                    cloudMacPort.setPort("Cloud representing mac addresses");
                    ports.add(cloudMacPort);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getTopology: added port: {}", shared.getCloud().getMacsInfo());
                    }
                }
                for (MacPort mp: portsWithoutNode) {
                    OnmsTopologyPort   macPort = OnmsTopologyPort.create(cloudMacVertex, -1);
                    macPort.setAddr(Topology.getAddress(mp));
                    macPort.setPort(mp.printTopology());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getTopology: added port: {}", mp.getPortMacInfo());
                    }
                    ports.add(macPort);
                }
            }
            OnmsTopologyShared edge = OnmsTopologyShared.create(
                                                          Topology.getId(shared.getUpPort()), 
                                                          ports.toArray(new OnmsTopologyPort[ports.size()]));
            topology.getEdges().add(edge);

        }
        return topology;
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.BRIDGE.name();
    }
            
}

