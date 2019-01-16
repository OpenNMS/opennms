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

import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.MacCloud;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyShared;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologySegment;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;

public class BridgeOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater  {

    public static OnmsTopologyVertex create(MacCloud macCloud, List<MacPort> ports, BridgePort designated ) throws OnmsTopologyException {
        OnmsTopologyVertex vertex = OnmsTopologyVertex.create(Topology.getSharedSegmentId(designated), 
                                         Topology.getMacsIpLabel(), 
                                         Topology.getAddress(macCloud,ports), 
                                         Topology.getDefaultIconKey());
        vertex.setToolTipText(Topology.getMacsIpTextString(macCloud, ports));
        return vertex;
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex ipandmacnonode) throws OnmsTopologyException{
        OnmsTopologyPort   port = OnmsTopologyPort.create(ipandmacnonode.getId(),ipandmacnonode,-1);
        port.setToolTipText(ipandmacnonode.getToolTipText());
        return port;
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source, BridgePort bp, SnmpInterfaceTopologyEntity snmpiface) throws OnmsTopologyException {
        OnmsTopologyPort port = OnmsTopologyPort.create(Topology.getId(bp),source, bp.getBridgePort());
        port.setIfindex(bp.getBridgePortIfIndex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());            
        }
        port.setAddr(Topology.getAddress(bp));
        port.setToolTipText(Topology.getPortTextString(source.getLabel(),port.getIfindex(),port.getAddr(),snmpiface));
        return port;
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source, MacPort mp, SnmpInterfaceTopologyEntity snmpiface) throws OnmsTopologyException {
        OnmsTopologyPort port = OnmsTopologyPort.create(Topology.getId(mp),source, mp.getIfIndex());
        port.setIfindex(mp.getIfIndex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());            
        }
        port.setAddr(Topology.getAddress(mp));
        port.setToolTipText(Topology.getPortTextString(source.getLabel(),mp.getIfIndex(),Topology.getAddress(mp),snmpiface));
        return port;
    }

    private static final Logger LOG = LoggerFactory.getLogger(BridgeOnmsTopologyUpdater.class);

    protected final BridgeTopologyService m_bridgeTopologyService;

    public BridgeOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, BridgeTopologyService bridgeTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(bridgeTopologyService,topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_bridgeTopologyService = bridgeTopologyService;
    }            
    
    @Override
    public String getName() {
        return "BridgeTopologyUpdaters";
    }

    @Override
    public OnmsTopology buildTopology() throws OnmsTopologyException {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();

        for (TopologyShared shared : m_bridgeTopologyService.match()){
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTopology: parsing shared designated: {}", shared.getUpPort().printTopology());
            }
            Set<OnmsTopologyPort> ports = new HashSet<>();
            for(BridgePort bp :shared.getBridgePorts()) {
                NodeTopologyEntity node = nodeMap.get(bp.getNodeId());
                if (topology.getVertex(node.getId().toString()) == null) {
                    topology.getVertices().add(create(node,ipMap.get(node.getId()).getIpAddress()));
                }
                ports.add(create(topology.getVertex(node.getId().toString()), bp, nodeToOnmsSnmpTable.get(bp.getNodeId(), bp.getBridgePortIfIndex())));

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
                    if (topology.getVertex(node.getId().toString()) ==  null) {
                        topology.getVertices().add(create(node,ipMap.get(node.getId()).getIpAddress()));
                    }
                    ports.add(create(topology.getVertex(node.getId().toString()),mp,nodeToOnmsSnmpTable.get(mp.getNodeId(), mp.getIfIndex())));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getTopology: added port: {}", mp.printTopology());
                    }
                }
            }
            
            if (shared.getCloud() != null || portsWithoutNode.size() > 0) {
                OnmsTopologyVertex macVertex = create(shared.getCloud(),portsWithoutNode, shared.getUpPort()) ;
                topology.getVertices().add(macVertex);
                ports.add( create(macVertex));
            }
            OnmsTopologySegment edge = OnmsTopologySegment.create(
                                                          Topology.getId(shared.getUpPort()), 
                                                          ports.toArray(new OnmsTopologyPort[ports.size()]));
            topology.getEdges().add(edge);

        }
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() throws OnmsTopologyException {
        return create(ProtocolSupported.BRIDGE);
    }
            
}

