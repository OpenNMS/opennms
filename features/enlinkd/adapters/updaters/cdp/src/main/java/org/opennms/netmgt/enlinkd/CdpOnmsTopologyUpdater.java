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
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
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

public class CdpOnmsTopologyUpdater extends TopologyUpdater {

    public static CdpOnmsTopologyUpdater clone (CdpOnmsTopologyUpdater bpu) {
        CdpOnmsTopologyUpdater update = new CdpOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getCdpTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
 
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex vertex,CdpLinkTopologyEntity cdpLink, SnmpInterfaceTopologyEntity snmpiface ) {
        OnmsTopologyPort port = OnmsTopologyPort.create(cdpLink.getId().toString(),vertex, cdpLink.getCdpCacheIfIndex());
        port.setIfindex(cdpLink.getCdpCacheIfIndex());
        port.setIfname(cdpLink.getCdpInterfaceName());
        port.setAddr(Topology.getAddress(cdpLink));
        port.setToolTipText(Topology.getPortTextString(vertex.getLabel(),port.getIfindex(),port.getAddr(),snmpiface));
        return port;
    }
    
    private final CdpTopologyService m_cdpTopologyService;

    public CdpOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, CdpTopologyService cdpTopologyService, NodeTopologyService nodeTopologyService) {
        super(cdpTopologyService,topologyDao,nodeTopologyService);
        m_cdpTopologyService = cdpTopologyService;
    }            
    
    @Override
    public String getName() {
        return "CdpTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();
        for (CdpElementTopologyEntity element: m_cdpTopologyService.findAllCdpElements()) {
            topology.getVertices().add(create(nodeMap.get(element.getNodeId()),ipMap.get(element.getNodeId())));
        }
        
        for(TopologyConnection<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair : m_cdpTopologyService.match()) {
            topology.getEdges().add(
                OnmsTopologyEdge.create(
                        Topology.getDefaultEdgeId(pair.getLeft().getId(), pair.getRight().getId()),
                        create(topology.getVertex(pair.getLeft().getNodeIdAsString()),
                               pair.getLeft(),
                               nodeToOnmsSnmpTable.get(pair.getLeft().getNodeId(), pair.getLeft().getCdpCacheIfIndex())),
                        create(topology.getVertex(pair.getRight().getNodeIdAsString()),
                               pair.getRight(),
                               nodeToOnmsSnmpTable.get(pair.getRight().getNodeId(), pair.getRight().getCdpCacheIfIndex())
                               )
                        )
                );
       }
        
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.CDP);
    }

    public CdpTopologyService getCdpTopologyService() {
        return m_cdpTopologyService;
    }

}

