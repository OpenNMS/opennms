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

import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
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

public class LldpOnmsTopologyUpdater extends TopologyUpdater {

    public static LldpOnmsTopologyUpdater clone (LldpOnmsTopologyUpdater bpu) {
        LldpOnmsTopologyUpdater update = new LldpOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getLldpTopologyService(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
 
    }

    public static OnmsTopologyPort create(OnmsTopologyVertex source,LldpLinkTopologyEntity sourceLink, 
                                                                    LldpLinkTopologyEntity targetlink,
                                                                    SnmpInterfaceTopologyEntity snmpiface) {
        OnmsTopologyPort port = OnmsTopologyPort.create(sourceLink.getId().toString(), source, sourceLink.getLldpPortIfindex());
        port.setIfindex(sourceLink.getLldpPortIfindex());
        if (snmpiface != null) {
            port.setIfname(snmpiface.getIfName());
        } else if (sourceLink.getLldpPortIdSubType() == LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME) {
            port.setIfname(sourceLink.getLldpPortId());
        } else if (targetlink.getLldpRemPortIdSubType() == LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME) {
            port.setIfname(targetlink.getLldpRemPortId());
        } else if (!"".equals(sourceLink.getLldpPortDescr())) {
            port.setIfname(sourceLink.getLldpPortDescr());
        }  else if (!"".equals(targetlink.getLldpRemPortDescr())) {
            port.setIfname(targetlink.getLldpRemPortDescr());
        } else {
            port.setIfname(sourceLink.getLldpPortId());
        }
        port.setAddr(Topology.getRemoteAddress(targetlink));
        port.setToolTipText(Topology.getPortTextString(source.getLabel(),port.getIfindex(), port.getAddr(), snmpiface));
        return port;
    }

    private final LldpTopologyService m_lldpTopologyService;

    public LldpOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, LldpTopologyService lldpTopologyService, NodeTopologyService nodeTopologyService) {
        super(lldpTopologyService, topologyDao,nodeTopologyService);
        m_lldpTopologyService = lldpTopologyService;
    }            
    
    @Override
    public String getName() {
        return "LldpTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, NodeTopologyEntity> nodeMap= getNodeMap();
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = getSnmpInterfaceTable();
        OnmsTopology topology = new OnmsTopology();
        for (LldpElementTopologyEntity element: m_lldpTopologyService.findAllLldpElements()) {
            topology.getVertices().add(create(nodeMap.get(element.getNodeId()),ipMap.get(element.getNodeId())));
        }
        
    for (TopologyConnection<LldpLinkTopologyEntity, LldpLinkTopologyEntity> pair : m_lldpTopologyService.match()) {
            topology.getEdges().add(
                                    OnmsTopologyEdge.create(
                                                            Topology.getDefaultEdgeId(pair.getLeft().getId(), pair.getRight().getId()),
                                                            create(
                                                                   topology.getVertex(pair.getLeft().getNodeIdAsString()), 
                                                                   pair.getLeft(),
                                                                   pair.getRight(),
                                                                   nodeToOnmsSnmpTable.get(pair.getLeft().getNodeId(), pair.getLeft().getLldpPortIfindex())
                                                                   ), 
                                                            create(
                                                                   topology.getVertex(pair.getRight().getNodeIdAsString()), 
                                                                   pair.getRight(),
                                                                   pair.getLeft(),
                                                                   nodeToOnmsSnmpTable.get(pair.getRight().getNodeId(), pair.getRight().getLldpPortIfindex())
                                                                   )
                                                            )
                                    );
       }
        
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.LLDP);
    }

    public LldpTopologyService getLldpTopologyService() {
        return m_lldpTopologyService;
    }

}

