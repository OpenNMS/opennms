/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

public class NodeCategoryPolicyTest implements InitializingBean {
    private List<OnmsNode> m_nodes;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_nodes = new ArrayList<>();
        final OnmsNode node1 = new OnmsNode();
        node1.setNodeId("1");
        node1.setForeignSource("a");
        node1.setForeignId("1");
        node1.setLabel("Node 1");
        m_nodes.add(node1);
    }

    @Test
    @Transactional
    public void testMatchingLabel() {
        NodeCategorySettingPolicy p = new NodeCategorySettingPolicy();
        p.setForeignId("1");
        p.setCategory("PolicyTest");

        List<OnmsNode> matchedNodes = matchPolicy(p, "1");
        assertTrue(matchedNodes.get(0).getRequisitionedCategories().contains("PolicyTest"));
    }

    @Test
    @Transactional
    public void testMatchingNothing() {
        NodeCategorySettingPolicy p = new NodeCategorySettingPolicy();
        p.setLabel("~^wankerdoodle$");
        p.setCategory("PolicyTest");

        List<OnmsNode> matchedNodes = matchPolicy(p, null);
        assertEquals(0, matchedNodes.size());
    }

    private List<OnmsNode> matchPolicy(NodeCategorySettingPolicy p, String matchingId) {
        OnmsNode o;
        final List<OnmsNode> populatedNodes = new ArrayList<>();
        final List<OnmsNode> matchedNodes = new ArrayList<>();

        for (final OnmsNode node : m_nodes) {
            System.err.println(node);
            o = p.apply(node);
            if (o != null && o.getRequisitionedCategories().contains(p.getCategory())) {
                matchedNodes.add(o);
            }
            if (node.getNodeId().equals(matchingId)) {
                populatedNodes.add(node);
            }
        }

        assertEquals(populatedNodes, matchedNodes);
        return matchedNodes;
    }

}
