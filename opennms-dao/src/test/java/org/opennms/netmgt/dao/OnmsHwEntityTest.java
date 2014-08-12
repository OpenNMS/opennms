/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class OnmsHwEntityTest implements InitializingBean {

    @Autowired
    NodeDao m_nodeDao;

    @Autowired
    HwEntityDao m_hwEntityDao;

    @Autowired
    DatabasePopulator m_populator;

    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    @BeforeTransaction
    public void setUp() {
        m_populator.populateDatabase();
    }

    @AfterTransaction
    public void tearDown() {
        m_populator.resetDatabase();
    }

    public OnmsNode getNode() {
        return m_populator.getNode1();
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public HwEntityDao getHwEntityDao() {
        return m_hwEntityDao;
    }

    @Test
    @Transactional
    public void testFindRootByNodeId() {
        OnmsNode node = getNode();
        assertNotNull(node);
        assertNotNull(node.getId());

        OnmsHwEntity root = new OnmsHwEntity();
        root.setEntPhysicalIndex(1);
        root.setEntPhysicalClass("chassis");
        root.setEntPhysicalName("Chassis");

        OnmsHwEntity m1 = new OnmsHwEntity();
        m1.setEntPhysicalIndex(2);
        m1.setEntPhysicalClass("module");
        m1.setEntPhysicalName("M1");

        OnmsHwEntity m2 = new OnmsHwEntity();
        m2.setEntPhysicalIndex(3);
        m2.setEntPhysicalClass("module");
        m2.setEntPhysicalName("M2");

        root.addChildEntity(m1);
        root.addChildEntity(m2);
        assertNotNull(m1.getParent());
        assertNotNull(m2.getParent());
        node.setRootHwEntity(root);
        assertNotNull(root.getNode());
        assertEquals(2, root.getChildren().size());
        assertNotNull(node.getRootHwEntity());

        getNodeDao().saveOrUpdate(node);
        getNodeDao().flush();

        OnmsHwEntity e = getHwEntityDao().findRootByNodeId(node.getId());
        assertNotNull(e);
        assertEquals(2, e.getChildren().size());
        assertEquals("chassis", e.getEntPhysicalClass());
    }

}
