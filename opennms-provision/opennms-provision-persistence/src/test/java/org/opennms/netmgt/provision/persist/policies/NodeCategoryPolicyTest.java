/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventd.xml"
})
@JUnitConfigurationEnvironment
public class NodeCategoryPolicyTest implements InitializingBean {
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private DatabasePopulator m_populator;

    private List<OnmsNode> m_nodes;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_populator.populateDatabase();
        m_nodes = m_nodeDao.findAll();
    }
    
    @After
    public void tearDown() {
        m_populator.resetDatabase();
    }

    @Test
    @Transactional
    public void testMatchingLabel() {
        NodeCategorySettingPolicy p = new NodeCategorySettingPolicy();
        p.setForeignId("1");
        p.setCategory("PolicyTest");

        List<OnmsNode> matchedNodes = matchPolicy(p, "1");
        assertTrue(matchedNodes.get(0).getCategories().contains(new OnmsCategory("PolicyTest")));
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
        List<OnmsNode> populatedNodes = new ArrayList<OnmsNode>();
        List<OnmsNode> matchedNodes = new ArrayList<OnmsNode>();
        
        for (OnmsNode node : m_nodes) {
            System.err.println(node);
            o = p.apply(node);
            if (o != null && o.getCategories().contains(new OnmsCategory(p.getCategory()))) {
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
