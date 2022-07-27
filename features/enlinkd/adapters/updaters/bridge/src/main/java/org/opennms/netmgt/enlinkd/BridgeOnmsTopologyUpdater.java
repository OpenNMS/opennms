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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyShared;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;

public class BridgeOnmsTopologyUpdater extends TopologyUpdater {

    public static BridgeOnmsTopologyUpdater clone(BridgeOnmsTopologyUpdater bpu) {
        
        BridgeOnmsTopologyUpdater update = new BridgeOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getBridgeTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
    }
    
    public static OnmsTopologyVertex createSegmentVertex(TopologyShared segment) {
        OnmsTopologyVertex cloudVertex = OnmsTopologyVertex.create(Topology.getSharedSegmentId(segment),
                                                                Topology.getSharedSegmentLabel(),
                                                                null,
                                                                Topology.getCloudIconKey());
        cloudVertex.setToolTipText(Topology.getSharedSegmentTextString(segment));
        return cloudVertex;        
    }

    public static OnmsTopologyVertex createMacsCloudVertex(List<MacPort> ports, TopologyShared segment ) {
        OnmsTopologyVertex vertex = OnmsTopologyVertex.create(Topology.getMacsCloudId(segment), 
                                         Topology.getMacsIpLabel(), 
                                         Topology.getAddress(segment.getCloud(),ports), 
                                         Topology.getDefaultIconKey());
        vertex.setToolTipText(Topology.getMacsCloudIpTextString(segment, ports));
        return vertex;
    }

    public static OnmsTopologyPort createVertexPort(OnmsTopologyVertex vertex) {
        OnmsTopologyPort   port = OnmsTopologyPort.create(Topology.getPortId(vertex.getId()),vertex,-1);
        port.setToolTipText(vertex.getToolTipText());
        return port;
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source, BridgePort bp, SnmpInterfaceTopologyEntity snmpiface) {
        OnmsTopologyPort port = OnmsTopologyPort.create(Topology.getId(bp),source, bp.getBridgePort());
        port.setIfindex(bp.getBridgePortIfIndex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());            
        }
        port.setAddr(Topology.getAddress(bp));
        port.setToolTipText(Topology.getPortTextString(source.getLabel(),port.getIfindex(),port.getAddr(),snmpiface));
        return port;
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source, MacPort mp, SnmpInterfaceTopologyEntity snmpiface) {
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

    private final BridgeTopologyService m_bridgeTopologyService;

    public BridgeOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, BridgeTopologyService bridgeTopologyService, NodeTopologyService nodeTopologyService) {
        super(bridgeTopologyService,topologyDao,nodeTopologyService);
        m_bridgeTopologyService = bridgeTopologyService;
    }            
    
    @Override
    public String getName() {
        return "BridgeTopologyUpdaters";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> snmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();

        for (TopologyShared shared : m_bridgeTopologyService.match()){
            if (LOG.isDebugEnabled()) {
                LOG.debug("getTopology: parsing shared designated: {}", shared.printTopology());
            }
            Map<BridgePort,OnmsTopologyVertex> bpVtxMap = new HashMap<>();
            for(BridgePort bp :shared.getBridgePorts()) {
                NodeTopologyEntity node = nodeMap.get(bp.getNodeId());
                if (topology.getVertex(node.getId().toString()) == null) {
                    topology.getVertices().add(create(node,ipMap.get(node.getId())));
                }
                bpVtxMap.put(
                               bp,
                               topology.getVertex(node.getId().toString())
                );
            }
            Map<MacPort,OnmsTopologyVertex> macPortToNodeVertexMap = new HashMap<>();

            List<MacPort> portsWithoutNode = new ArrayList<>();
            for (MacPort mp :shared.getMacPorts()) {
                if (mp.getNodeId() == null) {
                    portsWithoutNode.add(mp);
                } else {
                    NodeTopologyEntity node = nodeMap.get(mp.getNodeId());
                    if (topology.getVertex(node.getId().toString()) ==  null) {
                        topology.getVertices().add(create(node,ipMap.get(node.getId())));
                    }
                    macPortToNodeVertexMap.put(
                               mp,
                               topology.getVertex(node.getId().toString())
                               );
                }
            }
            OnmsTopologyVertex macsVertex = null;
            OnmsTopologyPort macsVertexPort = null;
            if (shared.getCloud() != null || portsWithoutNode.size() > 0) {
                macsVertex = createMacsCloudVertex(portsWithoutNode, shared) ;
                topology.getVertices().add(macsVertex);
                macsVertexPort= createVertexPort(macsVertex);
            }
            
            if (bpVtxMap.size() == 2 && 
                    macPortToNodeVertexMap.size() == 0 && macsVertex == null ) {
                BridgePort sourcebp = null;
                BridgePort targetbp = null;
                OnmsTopologyPort sourceport = null;
                OnmsTopologyPort targetport = null;
                for (BridgePort bp: bpVtxMap.keySet()) {
                    SnmpInterfaceTopologyEntity snmpiface = snmpTable.get(bp.getNodeId(), bp.getBridgePortIfIndex());
                    if (bp.getNodeId().intValue() == shared.getUpPort().getNodeId().intValue()) {
                        sourcebp = bp;
                        sourceport = create(bpVtxMap.get(bp),bp,snmpiface);
                        continue;
                    } 
                    targetbp=bp;
                    targetport = create(bpVtxMap.get(bp),bp,snmpiface);
                }
                topology.getEdges().add(OnmsTopologyEdge.create(Topology.getEdgeId(sourcebp, targetbp), sourceport, targetport));
            } else if (bpVtxMap.size() == 1 && 
                    macPortToNodeVertexMap.size() == 1 && macsVertex == null ){
                BridgePort sourcebp = bpVtxMap.keySet().iterator().next();
                MacPort targetmp = macPortToNodeVertexMap.keySet().iterator().next();
                topology.getEdges().add(
                      OnmsTopologyEdge.create(
                              Topology.getEdgeId(sourcebp, targetmp), 
                              create(
                                     bpVtxMap.get(sourcebp),
                                     sourcebp,
                                     snmpTable.get(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex())
                                 ), 
                              create(
                                     macPortToNodeVertexMap.get(targetmp),
                                     targetmp,
                                     snmpTable.get(targetmp.getNodeId(), targetmp.getIfIndex())
                                 )
                      ) 
                );         
            } else  if (bpVtxMap.size() == 1 && 
                    macPortToNodeVertexMap.size() == 0 && macsVertex != null ) {
                    BridgePort sourcebp = bpVtxMap.keySet().iterator().next();
                    topology.getEdges().add(
                            OnmsTopologyEdge.create(
                                             Topology.getEdgeId(macsVertex.getId(),sourcebp), 
                                             create(
                                                    bpVtxMap.values().iterator().next(),
                                                    sourcebp,
                                                    snmpTable.get(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex())
                                                ),
                                             macsVertexPort
                                         )
                             );


            } else {
                OnmsTopologyVertex segment = createSegmentVertex(shared);
                OnmsTopologyPort segmentPort = createVertexPort(segment);
                topology.getVertices().add(segment);
                for (BridgePort bp: bpVtxMap.keySet()) {
                    topology.getEdges().add(
                             OnmsTopologyEdge.create(
                                              Topology.getEdgeId(segment.getId(), bp), 
                                              segmentPort,
                                              create(
                                                   bpVtxMap.get(bp),
                                                   bp,
                                                   snmpTable.get(bp.getNodeId(), bp.getBridgePortIfIndex())
                                              )
                                          )
                             );
                    
                }
                for (MacPort mp: macPortToNodeVertexMap.keySet()) {
                    topology.getEdges().add(
                             OnmsTopologyEdge.create(
                                              Topology.getEdgeId(segment.getId(), mp), 
                                              segmentPort,
                                              create(
                                                   macPortToNodeVertexMap.get(mp),
                                                   mp,
                                                   snmpTable.get(mp.getNodeId(), mp.getIfIndex())
                                              )
                                          )
                              );
                }
                
                if (macsVertex != null) {
                    topology.getEdges().add(
                             OnmsTopologyEdge.create(
                                                 Topology.getDefaultEdgeId(segment.getId(), macsVertex.getId()), 
                                                 segmentPort,
                                                 macsVertexPort
                                          )
                             );
                    
                }

            }
        }
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.BRIDGE);
    }

    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }

}

