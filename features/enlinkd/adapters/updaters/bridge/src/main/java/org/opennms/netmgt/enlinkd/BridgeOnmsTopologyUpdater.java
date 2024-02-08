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
                assert sourcebp != null;
                assert targetbp != null;
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

