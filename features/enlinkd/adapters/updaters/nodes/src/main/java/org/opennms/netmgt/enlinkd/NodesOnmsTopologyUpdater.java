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
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;

public class NodesOnmsTopologyUpdater extends TopologyUpdater {

    public NodesOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService) {
        super(nodeTopologyService, topologyDao,nodeTopologyService);
    }            
    
    @Override
    public String getName() {
        return "NodesTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        OnmsTopology topology = new OnmsTopology();
        for (NodeTopologyEntity element: getNodeMap().values()) {
            topology.getVertices().add(create(element,ipMap.get(element.getId())));
        }
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.NODES);
    }

}

