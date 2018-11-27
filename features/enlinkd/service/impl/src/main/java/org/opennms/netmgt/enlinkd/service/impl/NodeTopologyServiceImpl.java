/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.PrimaryType;

public class NodeTopologyServiceImpl implements org.opennms.netmgt.enlinkd.service.api.NodeTopologyService {

    private NodeDao m_nodeDao;
    
    @Override
    public List<Node> findAllSnmpNode() {
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "ipInterfaces",
                                                                  "iface",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.isSnmpPrimary",
                                                  PrimaryType.PRIMARY));
        return m_nodeDao.findMatching(criteria).stream().map(Node::create).collect(Collectors.toList());

    }

    @Override
    public Node getSnmpNode(final int nodeid) {
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "ipInterfaces",
                                                                  "iface",
                                                                  JoinType.LEFT_JOIN) }));
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.isSnmpPrimary",
                                                  PrimaryType.PRIMARY));
        criteria.addRestriction(new EqRestriction("id", nodeid));
        final List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);

        if (nodes.size() > 0) {
            return Node.create(nodes.get(0));
        } else {
            return null;
        }
    }
    
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Override
    public List<Node> findAll() {
        final Criteria criteria = new Criteria(OnmsNode.class);
        criteria.setAliases(Arrays.asList(new Alias[] { new Alias(
                                                                  "ipInterfaces",
                                                                  "iface",
                                                                  JoinType.LEFT_JOIN) }));
        return m_nodeDao.findMatching(criteria).stream().map(Node::create).collect(Collectors.toList());
    }

    @Override
    public Node getDefaultFocusPoint() {
        return Node.create(m_nodeDao.getDefaultFocusPoint());
    }
        
}
