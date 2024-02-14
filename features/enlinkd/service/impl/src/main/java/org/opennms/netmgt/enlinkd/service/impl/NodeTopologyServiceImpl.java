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
package org.opennms.netmgt.enlinkd.service.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SubNetwork;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.PrimaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeTopologyServiceImpl extends TopologyServiceImpl implements NodeTopologyService {
    private final static Logger LOG = LoggerFactory.getLogger(TopologyServiceImpl.class);

    private NodeDao m_nodeDao;
    @Override
    public List<Node> findAllSnmpNode() {
        final List<Node> nodes = new ArrayList<>();
        
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(List.of(new Alias(
                "ipInterfaces",
                "iface",
                JoinType.LEFT_JOIN)));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.snmpPrimary",
                                                  PrimaryType.PRIMARY.getCharCode()));
        for (final OnmsNode node : m_nodeDao.findMatching(criteria)) {
            nodes.add(new Node(node.getId(), node.getLabel(),
                               node.getPrimaryInterface().getIpAddress(),
                               node.getSysObjectId(), node.getSysName(),node.getLocation() == null ? null : node.getLocation().getLocationName()));
        }
        return nodes;
                                                                                                       
    }

    @Override
    public Set<SubNetwork> findAllLegalSubNetwork() {
        return findAllSubNetwork()
                .stream()
                .filter(s -> !s.hasDuplicatedAddress())
                .filter(s ->!InetAddressUtils.inSameNetwork(s.getNetwork(),InetAddress.getLoopbackAddress(), s.getNetmask()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SubNetwork> findSubNetworkByNetworkPrefixLessThen(int ipv4prefix, int ipv6prefix) {
        return findAllLegalSubNetwork()
                .stream()
                .filter(s -> (s.isIpV4Subnetwork() && s.getNetworkPrefix() < ipv4prefix) ||(!s.isIpV4Subnetwork() && s.getNetworkPrefix() < ipv6prefix)).collect(Collectors.toSet());
    }

    @Override
    public Set<SubNetwork> findAllPointToPointSubNetwork() {
        return findAllSubNetwork()
                .stream()
                .filter(s -> InetAddressUtils.isPointToPointMask(s.getNetmask()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SubNetwork> findAllLegalPointToPointSubNetwork() {
        return findAllLegalSubNetwork()
                .stream()
                .filter(s -> InetAddressUtils.isPointToPointMask(s.getNetmask()) && s.getNodeIds().size() == 2)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SubNetwork> findAllLoopbacks() {
        return findAllSubNetwork()
                .stream()
                .filter(s -> InetAddressUtils.isLoopbackMask(s.getNetmask()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<SubNetwork> findAllLegalLoopbacks() {
        return findAllSubNetwork()
                .stream()
                .filter(s -> InetAddressUtils.isLoopbackMask(s.getNetmask()) && s.getNodeIds().size() == 1)
                .collect(Collectors.toSet());
    }

    private static SubNetwork getNextSubnetwork(final Set<SubNetwork> subNetworks) {
        SubNetwork starting = null;
        for (final var subnet : subNetworks) {
            if (starting == null) {
                starting=subnet;
            } else  if (starting.getNodeIds().size() < subnet.getNodeIds().size()) {
                starting = subnet;
            } else {
                if (starting.getNodeIds().size() == subnet.getNodeIds().size()
                        && InetAddressUtils.difference(starting.getNetwork(), subnet.getNetwork()).signum() > 0) {
                    starting = subnet;
                }
            }
        }
        return starting;
    }

    private int getConnected(final SubNetwork starting, final Set<SubNetwork> subnetworks, final Map<Integer, Integer> priorityMap,  int priority) {
        Set<SubNetwork> downlevels = new HashSet<>();
        for (SubNetwork subnet: subnetworks) {
            Set<Integer> intersection = new HashSet<>(subnet.getNodeIds());
            intersection.retainAll(starting.getNodeIds());
            if (intersection.size() > 0) {
                LOG.info("getConnected: match: {}, {} {}",intersection, subnet, starting);
                downlevels.add(subnet);
            }
        }
        downlevels.forEach(subnetworks::remove);
        LOG.info("getConnected subnetworks.size: {}",  subnetworks.size());
        for (SubNetwork subnetowrk: downlevels) {
            LOG.info("getConnected: parsing: {}",  subnetowrk);
            LOG.info("getConnected: priority: {}",  priority);
            Set<Integer> addingNodes = new HashSet<>(subnetowrk.getNodeIds());
            addingNodes.removeAll(priorityMap.keySet());
            LOG.info("getConnected: adding: {}",  addingNodes);
            if (addingNodes.isEmpty()) {
                continue;
            }
            for (Integer nodeid: addingNodes) {
                priorityMap.put(nodeid,priority);
            }
            LOG.info("getConnected: priorityMap: {}",  priorityMap);
            priority++;
        }

        if (!downlevels.isEmpty() && subnetworks.size() > 0) {
            for (SubNetwork level : downlevels) {
                LOG.info("getConnected: iterating on: " + level);
                priority = getConnected(level, subnetworks, priorityMap, priority);
            }
        }
        return priority;
    }

    @Override
    public Map<Integer, Integer> getNodeidPriorityMap(ProtocolSupported protocol) {
        final Map<Integer, Integer> priorityMap = new HashMap<>();
        Set<SubNetwork> allLegalSubnets = findAllLegalSubNetwork().stream().filter(s -> s.getNodeIds().size() > 1).collect(Collectors.toSet());
        LOG.info("getNodeidPriorityMap: subnetworks.size: {}",  allLegalSubnets.size());
        int priority = 0;
        int loop = 0;
        while (!allLegalSubnets.isEmpty()) {
            loop++;
            final var start = getNextSubnetwork(allLegalSubnets);
            if (start == null) {
                LOG.warn("List of legal subnets isn't completely processed, but we were unable to match next subnetwork. Stopping processing. remainder={}", allLegalSubnets);
                break;
            }
            allLegalSubnets.remove(start);
            LOG.info("getNodeidPriorityMap: loop-{}: start: {}", loop,  start);
            LOG.info("getNodeidPriorityMap: loop-{}: priority: {}", loop,  priority);
            LOG.info("getNodeidPriorityMap: loop-{}: subnetworks.size: {}",  loop, allLegalSubnets.size());
            final int p = priority;
            start.getNodeIds().forEach(n-> priorityMap.put(n, p));
            LOG.info("getNodeidPriorityMap: loop-{}: priorityMap: {}", loop, priorityMap);
            priority = getConnected(start,allLegalSubnets, priorityMap, ++priority);
        }
        return priorityMap;
    }

    @Override
    public Set<SubNetwork> findAllSubNetwork() {
        final Set<SubNetwork> subnets = new HashSet<>();
        final List<IpInterfaceTopologyEntity> ips = findAllIp();
        ips.stream().filter(ip -> ip.isManaged() && ip.getNetMask() != null ).forEach(ip -> {
            boolean found = false;
            for (SubNetwork s: subnets) {
                if (s.isInRange(ip.getIpAddress()) && s.getNetmask().equals(ip.getNetMask())) {
                    found=true;
                    s.add(ip.getNodeId(),ip.getIpAddress());
                    break;
                }
            }
            if (!found) {
                subnets.add(SubNetwork.createSubNetwork(ip));
            }
        });
        ips.stream().filter(ip -> ip.isManaged() && ip.getNetMask() == null).forEach(ip -> {
            for (SubNetwork s: subnets) {
                if (s.isInRange(ip.getIpAddress())) {
                    s.add(ip.getNodeId(),ip.getIpAddress());
                }
            }
        });
        return subnets;
    }

    @Override
    public Node getSnmpNode(final String nodeCriteria) {
        LOG.info("getSnmpNode: nodeCriteria {}", nodeCriteria);
        try {
            return getSnmpNode(Integer.parseInt(nodeCriteria));
        } catch (NumberFormatException e) {
            LOG.info("getSnmpNode: not nodeId");
        }
        String[] values = nodeCriteria.split(":");
        LOG.info("getSnmpNode: foreignSource: {}, foreignId: {} ", values[0], values[1]);
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(List.of(new Alias(
                "ipInterfaces",
                "iface",
                JoinType.LEFT_JOIN)));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.snmpPrimary",
                PrimaryType.PRIMARY.getCharCode()));
        criteria.addRestriction(new EqRestriction("foreignId", values[1]));
        criteria.addRestriction(new EqRestriction("foreignSource", values[0]));
        return getNodebyCriteria(criteria);
    }

    private Node getNodebyCriteria(Criteria criteria) {
        final List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);

        if (nodes.size() > 0) {
            final OnmsNode node = nodes.get(0);
            return new Node(node.getId(), node.getLabel(),
                    node.getPrimaryInterface().getIpAddress(),
                    node.getSysObjectId(), node.getSysName(),node.getLocation() == null ? null : node.getLocation().getLocationName());
        }
        return null;
    }
    @Override
    public Node getSnmpNode(final int nodeid) {
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(List.of(new Alias(
                "ipInterfaces",
                "iface",
                JoinType.LEFT_JOIN)));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.snmpPrimary",
                                                  PrimaryType.PRIMARY.getCharCode()));
        criteria.addRestriction(new EqRestriction("id", nodeid));
        return getNodebyCriteria(criteria);
    }

    @Override
    public Set<SubNetwork> getLegalSubNetworks(int nodeid) {
        return findAllLegalSubNetwork().stream().filter(s -> s.getNodeIds().contains(nodeid)).collect(Collectors.toSet());
    }

    @Override
    public Set<SubNetwork> getSubNetworks(int nodeid) {
        return findAllSubNetwork().stream().filter(s -> s.getNodeIds().contains(nodeid)).collect(Collectors.toSet());
    }

    @Override
    public List<NodeTopologyEntity> findAllNode() {
        return getTopologyEntityCache().getNodeTopologyEntities();
    }

    @Override
    public List<IpInterfaceTopologyEntity> findAllIp() {
        return getTopologyEntityCache().getIpInterfaceTopologyEntities();
    }

    @Override
    public List<SnmpInterfaceTopologyEntity> findAllSnmp() {
        return getTopologyEntityCache().getSnmpInterfaceTopologyEntities();
    }


    @Override
    public NodeTopologyEntity getDefaultFocusPoint() {
        OnmsNode node = m_nodeDao.getDefaultFocusPoint();
        if ( node != null) {
            return NodeTopologyEntity.toNodeTopologyInfo(node);
        }
        return null;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
        
}
