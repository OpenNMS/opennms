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
package org.opennms.netmgt.dao;

import org.hamcrest.Matchers;
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
import org.opennms.netmgt.model.OnmsHwEntityAlias;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.TreeSet;

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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class HwEntityDaoIT implements InitializingBean {

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
        OnmsHwEntityAlias onmsHwEntityAlias = new OnmsHwEntityAlias(1, "0.1.12.3.4");
        onmsHwEntityAlias.setHwEntity(m2);
        m2.addEntAliases(new TreeSet<>(Arrays.asList(onmsHwEntityAlias)));

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
        OnmsHwEntity chassisEntity = m_hwEntityDao.findEntityByIndex(node.getId(), 3);
        Assert.assertThat(chassisEntity.getEntAliases(), Matchers.hasSize(1));
        // Test valid findRootEntityByNodeId
        OnmsHwEntity entity1 = m_hwEntityDao.findRootEntityByNodeId(node.getId());
        Assert.assertNotNull(entity1);
        Assert.assertNotNull(entity1.getNodeId());
        Assert.assertEquals(entity1.getNodeId(), node.getId());
        Assert.assertEquals(2, entity1.getChildren().size());
        Assert.assertEquals("chassis", entity1.getEntPhysicalClass());
        OnmsHwEntity m2Entity = entity1.getChildren().stream().filter(child -> child.getEntPhysicalIndex() == 3).findFirst().get();
        Assert.assertThat(m2Entity.getEntAliases(), Matchers.hasSize(1));

        // Test invalid findRootByNodeId
        Assert.assertNull(m_hwEntityDao.findRootByNodeId(10000));
        m_hwEntityDao.findRootByNodeId(10000);

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
