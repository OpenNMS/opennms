/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.SubNetwork;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.PrimaryType;

public class NodeTopologyServiceImpl extends TopologyServiceImpl implements NodeTopologyService {

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
        return findAllSubNetwork().stream().filter(s -> s.getNodeIds().size() > 1 && !s.hasDuplicatedAddress()).collect(Collectors.toSet());
    }
        @Override
    public Set<SubNetwork> findAllSubNetwork() {
        final Set<SubNetwork> subnets = new HashSet<>();
        final List<IpInterfaceTopologyEntity> ips = findAllIp();
        ips.stream().filter(ip -> ip.isManaged() && ip.getNetMask() != null ).forEach(ip -> {
            boolean found = false;
            for (SubNetwork s: subnets) {
                if (s.isInRange(ip.getIpAddress())) {
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
        final List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);

        if (nodes.size() > 0) {
            final OnmsNode node = nodes.get(0);
            return new Node(node.getId(), node.getLabel(),
                            node.getPrimaryInterface().getIpAddress(),
                            node.getSysObjectId(), node.getSysName(),node.getLocation() == null ? null : node.getLocation().getLocationName());
        } else {
            return null;
        }
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
