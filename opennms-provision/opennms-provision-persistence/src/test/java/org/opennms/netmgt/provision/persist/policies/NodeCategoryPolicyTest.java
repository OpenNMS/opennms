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
package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
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
            o = p.apply(node, new HashMap<>());
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
