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
import org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
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

public class IsisOnmsTopologyUpdater extends TopologyUpdater {

    public static IsisOnmsTopologyUpdater clone (IsisOnmsTopologyUpdater bpu) {
        IsisOnmsTopologyUpdater update = new IsisOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getIsisTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
 
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source,
            IsIsLinkTopologyEntity sourceLink,
            IsIsLinkTopologyEntity targetLink,
            SnmpInterfaceTopologyEntity snmpiface) {
        OnmsTopologyPort port= OnmsTopologyPort.create(sourceLink.getId().toString(),source, targetLink.getIsisISAdjIndex());
        port.setIfindex(sourceLink.getIsisCircIfIndex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());            
        }
        port.setIfname(sourceLink.getIsisCircIfIndex().toString());
        port.setAddr(Topology.getRemoteAddress(targetLink));
        port.setToolTipText(Topology.getPortTextString(source.getLabel(),port.getIfindex(),port.getAddr(),snmpiface));
        return port;
    }


    private final IsisTopologyService m_isisTopologyService;

    public IsisOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, IsisTopologyService isisTopologyService, NodeTopologyService nodeTopologyService) {
        super(isisTopologyService, topologyDao,nodeTopologyService);
        m_isisTopologyService = isisTopologyService;
    }            
    
    @Override
    public String getName() {
        return "IsIsTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();

        OnmsTopology topology = new OnmsTopology();
        for ( IsIsElementTopologyEntity element: m_isisTopologyService.findAllIsIsElements()) {
            topology.getVertices().add(create(nodeMap.get(element.getNodeId()),ipMap.get(element.getNodeId())));
        }
        
    for(TopologyConnection<IsIsLinkTopologyEntity, IsIsLinkTopologyEntity> pair : m_isisTopologyService.match()){
        topology.getEdges().add(
                                OnmsTopologyEdge.create(
                                                            Topology.getDefaultEdgeId(pair.getLeft().getId(), pair.getRight().getId()),
                                                            create(topology.getVertex(pair.getLeft().getNodeIdAsString()), 
                                                                   pair.getLeft(), 
                                                                   pair.getRight(),
                                                                   nodeToOnmsSnmpTable.get(pair.getLeft().getNodeId(), pair.getLeft().getIsisCircIfIndex())
                                                                   ), 
                                                            create(topology.getVertex(pair.getRight().getNodeIdAsString()), 
                                                                   pair.getRight(), 
                                                                   pair.getLeft(),
                                                                   nodeToOnmsSnmpTable.get(pair.getRight().getNodeId(), pair.getRight().getIsisCircIfIndex())
                                                                   )
                                                            )
                                );
       }
        
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.ISIS);
    }

    public IsisTopologyService getIsisTopologyService() {
        return m_isisTopologyService;
    }

}

