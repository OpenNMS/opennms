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

import java.util.Map;

import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
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

public class OspfOnmsTopologyUpdater extends TopologyUpdater {

    public static OspfOnmsTopologyUpdater clone (OspfOnmsTopologyUpdater bpu) {
        OspfOnmsTopologyUpdater update = new OspfOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getOspfTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
 
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source,
                                            OspfLinkTopologyEntity sourcelink, 
                                            OspfLinkTopologyEntity targetlink,
                                            SnmpInterfaceTopologyEntity snmpiface) {
        OnmsTopologyPort port = OnmsTopologyPort.create(sourcelink.getId().toString(),source, sourcelink.getOspfIfIndex());
        port.setIfindex(sourcelink.getOspfIfIndex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());            
        }
        port.setAddr(Topology.getRemoteAddress(targetlink));
        port.setToolTipText(Topology.getPortTextString(source.getLabel(), port.getIndex(), port.getAddr(), snmpiface));
        return port;
    }

    private final OspfTopologyService m_ospfTopologyService;

    public OspfOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, OspfTopologyService ospfTopologyService, NodeTopologyService nodeTopologyService) {
        super(ospfTopologyService,topologyDao,nodeTopologyService);
        m_ospfTopologyService = ospfTopologyService;
    }            
    
    @Override
    public String getName() {
        return "OspfTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap=getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();
        for (OspfElement element: m_ospfTopologyService.findAllOspfElements()) {
            topology.getVertices().add(create(nodeMap.get(element.getNode().getId()),ipMap.get(element.getNode().getId())));
        }
        
        for(TopologyConnection<OspfLinkTopologyEntity, OspfLinkTopologyEntity> pair : m_ospfTopologyService.match()) {
            OspfLinkTopologyEntity left = pair.getLeft();
            if (topology.getVertex(left.getNodeIdAsString()) == null) {
                topology.getVertices().add(create(nodeMap.get(left.getNodeId()),ipMap.get(left.getNodeId())));                
            }
            OspfLinkTopologyEntity right = pair.getRight();
            if (topology.getVertex(right.getNodeIdAsString()) == null) {
                topology.getVertices().add(create(nodeMap.get(right.getNodeId()),ipMap.get(right.getNodeId())));                
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
        return create(ProtocolSupported.OSPF);
    }

    public OspfTopologyService getOspfTopologyService() {
        return m_ospfTopologyService;
    }
            
}

