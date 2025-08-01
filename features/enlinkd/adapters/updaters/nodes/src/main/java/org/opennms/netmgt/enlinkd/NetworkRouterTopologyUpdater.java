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

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SubNetwork;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

import com.google.common.collect.Table;

public class NetworkRouterTopologyUpdater extends TopologyUpdater {

    public static OnmsTopologyPort create(OnmsTopologyVertex source,
                                          IpInterfaceTopologyEntity sourceAddr,
                                          SnmpInterfaceTopologyEntity snmpiface) {
        OnmsTopologyPort port = OnmsTopologyPort.create(sourceAddr.getIpAddress().getHostAddress(), source, sourceAddr.getId());
        if (snmpiface != null) {
            port.setIfindex(snmpiface.getIfIndex());
            port.setIfname(snmpiface.getIfName());
        }
        port.setAddr(sourceAddr.getIpAddress().getHostAddress());
        port.setToolTipText(Topology.getPortTextString(source.getLabel(), port.getIndex(), port.getAddr(), snmpiface));
        return port;
    }

    public static OnmsTopologyPort createNetworkPort(OnmsTopologyVertex source, IpInterfaceTopologyEntity target) {
        OnmsTopologyPort port = OnmsTopologyPort.create(source.getId()+"to:"+target.getIpAddress().getHostAddress(), source, target.getId());
        port.setAddr("to: " +target.getIpAddress().getHostAddress());
        port.setToolTipText(Topology.getPortTextString(source.getLabel(), null, port.getAddr(), null));
        return port;
    }

    public static OnmsTopologyVertex createNetworkVertex(SubNetwork network) {
        OnmsTopologyVertex networkVertex = OnmsTopologyVertex.create(network.getCidr(),
                network.getCidr(),
                network.getCidr(),
                Topology.getCloudIconKey());
        networkVertex.setToolTipText("SubNetwork: " + network.getCidr() + ", Nodeids:" + network.getNodeIds());
        return networkVertex;
    }

    public NetworkRouterTopologyUpdater(
            OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService) {
        super(nodeTopologyService, topologyDao,nodeTopologyService);
    }            
    
    @Override
    public String getName() {
        return "NetworkRouterTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        final OnmsTopology topology = new OnmsTopology();
        Map<Integer, IpInterfaceTopologyEntity> ipPrimaryMap = getIpPrimaryMap();
        getNodeMap()
                .values()
                .forEach(element -> topology.getVertices().add(create(element, ipPrimaryMap.get(element.getId()))));

        Table<Integer, InetAddress, IpInterfaceTopologyEntity> ipTable = getIpInterfaceTable();
        Map<Integer, SnmpInterfaceTopologyEntity> snmpMap = getSnmpInterfaceMap();
        getNodeTopologyService()
            .findAllLegalPointToPointSubNetwork()
            .forEach(subnet -> {
                Iterator<Integer> nodeiterator = subnet.getNodeIds().iterator();
                Integer sourceNodeid = nodeiterator.next();
                Integer targetNodeid = nodeiterator.next();
                OnmsTopologyVertex source = topology.getVertex(sourceNodeid.toString());
                OnmsTopologyVertex target = topology.getVertex(targetNodeid.toString());
                IpInterfaceTopologyEntity sourceIp = null;
                IpInterfaceTopologyEntity targetIp = null;
                for (InetAddress inet : ipTable.row(sourceNodeid).keySet()) {
                    if (InetAddressUtils.inSameNetwork(inet, subnet.getNetwork(), subnet.getNetmask())) {
                        sourceIp = ipTable.get(sourceNodeid, inet);
                        break;
                    }
                }
                for (InetAddress inet : ipTable.row(targetNodeid).keySet()) {
                    if (InetAddressUtils.inSameNetwork(inet, subnet.getNetwork(), subnet.getNetmask())) {
                        targetIp = ipTable.get(targetNodeid, inet);
                        break;
                    }
                }
                if (sourceIp == null || targetIp == null) {
                    return;
                }   
                OnmsTopologyPort sourcePort = create(source, sourceIp, (sourceIp.getSnmpInterfaceId() != null ? snmpMap.get(sourceIp.getSnmpInterfaceId()) : null));
                OnmsTopologyPort targetPort = create(target, targetIp, (targetIp.getSnmpInterfaceId() != null ? snmpMap.get(targetIp.getSnmpInterfaceId()) : null));
                topology.getEdges().add(OnmsTopologyEdge.create(sourceIp.getId().toString(), sourcePort, targetPort));
            });

        getNodeTopologyService().findSubNetworkByNetworkPrefixLessThen(30, 126)
            .forEach(subnet -> {
                topology.getVertices().add(createNetworkVertex(subnet));
                OnmsTopologyVertex source = topology.getVertex(subnet.getCidr());
                for (Integer targetNodeid : subnet.getNodeIds()) {
                    OnmsTopologyVertex target = topology.getVertex(targetNodeid.toString());
                    IpInterfaceTopologyEntity targetIp = null;
                    for (InetAddress inet : ipTable.row(targetNodeid).keySet()) {
                        if (InetAddressUtils.inSameNetwork(inet, subnet.getNetwork(), subnet.getNetmask())) {
                            targetIp = ipTable.get(targetNodeid, inet);
                            break;
                        }
                    }
                    if (targetIp == null) {
                        return;
                    }    
                    OnmsTopologyPort sourcePort = createNetworkPort(source, targetIp);
                    OnmsTopologyPort targetPort = create(target, targetIp, (targetIp.getSnmpInterfaceId() != null ? snmpMap.get(targetIp.getSnmpInterfaceId()) : null));
                    topology.getEdges().add(OnmsTopologyEdge.create(targetIp.getId().toString(), sourcePort, targetPort));
                }
            });


        NodeTopologyEntity defaultFocusPoint = getDefaultFocusPoint();
        if (defaultFocusPoint != null) {
            topology.setDefaultVertex(create(defaultFocusPoint,ipPrimaryMap.get(defaultFocusPoint.getId())));
        }
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.NETWORKROUTER);
    }

}

