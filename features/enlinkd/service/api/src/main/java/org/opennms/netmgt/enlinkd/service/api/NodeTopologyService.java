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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;

public interface NodeTopologyService extends TopologyService {

    List<Node> findAllSnmpNode();
    Set<SubNetwork> findAllSubNetwork();
    Set<SubNetwork> findAllLegalSubNetwork();
    Set<SubNetwork> findSubNetworkByNetworkPrefixLessThen(int ipv4prefix, int ipv6prefix);
    Set<SubNetwork> findAllPointToPointSubNetwork();
    Set<SubNetwork> findAllLegalPointToPointSubNetwork();
    Set<SubNetwork> findAllLoopbacks();
    Set<SubNetwork> findAllLegalLoopbacks();

    Map<Integer, Integer> getNodeidPriorityMap(ProtocolSupported protocol);

    Node getSnmpNode(String nodeCriteria);
    Node getSnmpNode(int nodeid);
    Set<SubNetwork> getSubNetworks(int nodeid);
    Set<SubNetwork> getLegalSubNetworks(int nodeid);

    List<NodeTopologyEntity> findAllNode();

    List<IpInterfaceTopologyEntity> findAllIp();
    List<SnmpInterfaceTopologyEntity> findAllSnmp();
    NodeTopologyEntity getDefaultFocusPoint();
        
}
