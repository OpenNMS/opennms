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

package org.opennms.netmgt.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class HwEntityDaoTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class HwEntityDaoTest implements InitializingBean {

    /** The node DAO. */
    @Autowired
    NodeDao m_nodeDao;

    /** The hardware entity DAO. */
    @Autowired
    HwEntityDao m_hwEntityDao;

    /** The hardware entity attribute type DAO. */
    @Autowired
    HwEntityAttributeTypeDao m_hwEntityAttributeTypeDao;

    /** The database populator. */
    @Autowired
    DatabasePopulator m_populator;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        org.opennms.core.spring.BeanUtils.assertAutowiring(this);
    }

    /**
     * Sets the up.
     */
    @BeforeTransaction
    public void setUp() {
        m_populator.populateDatabase();
    }

    /**
     * Tear down.
     */
    @AfterTransaction
    public void tearDown() {
        m_populator.resetDatabase();
    }

    /**
     * Gets the node.
     *
     * @return the node
     */
    public OnmsNode getNode() {
        return m_populator.getNode1();
    }

    /**
     * Test find entity.
     */
    @Test
    @Transactional
    public void testEntityCycle() {
        HwEntityAttributeType ram = new HwEntityAttributeType(".1.3.6.1.4.1.9.9.195.1.1.1.1", "ram", "integer");
        m_hwEntityAttributeTypeDao.save(ram);
        m_hwEntityAttributeTypeDao.flush();

        OnmsNode node = getNode();
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getId());

        OnmsHwEntity root = new OnmsHwEntity();
        root.setEntPhysicalIndex(1);
        root.setEntPhysicalClass("chassis");
        root.setEntPhysicalName("Chassis");

        OnmsHwEntity m1 = new OnmsHwEntity();
        m1.setEntPhysicalIndex(2);
        m1.setEntPhysicalClass("module");
        m1.setEntPhysicalName("M1");
        m1.addAttribute(ram, "4");

        OnmsHwEntity m2 = new OnmsHwEntity();
        m2.setEntPhysicalIndex(3);
        m2.setEntPhysicalClass("module");
        m2.setEntPhysicalName("M2");
        m2.addAttribute(ram, "2");

        root.addChildEntity(m1);
        root.addChildEntity(m2);
        Assert.assertNotNull(m1.getParent());
        Assert.assertNotNull(m2.getParent());

        root.setNode(node);
        Assert.assertNotNull(root.getNode());
        Assert.assertEquals(2, root.getChildren().size());

        // Test saving root entity
        m_hwEntityDao.saveOrUpdate(root);
        m_hwEntityDao.flush();

        // Test valid findRootByNodeId
        OnmsHwEntity e1 = m_hwEntityDao.findRootByNodeId(node.getId());
        Assert.assertNotNull(e1);
        Assert.assertNotNull(e1.getNode());
        Assert.assertEquals(e1.getNode().getId(), node.getId());
        Assert.assertEquals(2, e1.getChildren().size());
        Assert.assertEquals("chassis", e1.getEntPhysicalClass());
        OnmsHwEntity c = e1.getChildren().iterator().next();
        Assert.assertEquals("4", c.getAttributeValue("ram"));

        // Test invalid findRootByNodeId
        Assert.assertNull(m_hwEntityDao.findRootByNodeId(10000));

        // Test findEntityByIndex
        OnmsHwEntity e2 = m_hwEntityDao.findEntityByIndex(node.getId(), e1.getEntPhysicalIndex());
        Assert.assertTrue(e1.equals(e2));

        // Test findEntityByName
        OnmsHwEntity e3 = m_hwEntityDao.findEntityByName(node.getId(), e1.getEntPhysicalName());
        Assert.assertTrue(e1.equals(e3));

        // Test getAttributeValue
        Assert.assertEquals("Chassis", m_hwEntityDao.getAttributeValue(node.getId(), 1, "entPhysicalName"));
        Assert.assertEquals("4", m_hwEntityDao.getAttributeValue(node.getId(), 2, "ram"));
        Assert.assertEquals("chassis", m_hwEntityDao.getAttributeValue(node.getId(), "Chassis", "entPhysicalClass"));
        Assert.assertEquals("4", m_hwEntityDao.getAttributeValue(node.getId(), "~^M1", "ram"));

        // Test delete
        m_hwEntityDao.flush();
        m_hwEntityDao.delete(e2.getId());
        m_hwEntityDao.flush();
        Assert.assertNull(m_hwEntityDao.get(e2.getId()));
    }

}
