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

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpCollectionResourceTypeDaoIT {

    @Autowired
    private SnmpCollectionResourceTypeDao resourceTypeDao;

    @Autowired
    private SnmpCollectionSourceDao snmpSourceDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionSource source;
    private SnmpCollectionResourceType resourceType;

    @Before
    @Transactional
    public void setUp() {
        resourceTypeDao.deleteAll(resourceTypeDao.findAll());
        resourceTypeDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();

        source = new SnmpCollectionSource();
        source.setName("JUnit Source");
        source.setEnabled(true);
        source.setDescription("JUnit Source Description");
        source.setCreatedTime(new Date());
        snmpSourceDao.saveOrUpdate(source);
        snmpSourceDao.flush();

        resourceType = new SnmpCollectionResourceType();
        resourceType.setCollectionSource(source);
        resourceType.setName("TestResourceType");
        resourceType.setLabel("JUnitLabel");
        resourceType.setResourceLabel("Test Resource Label");
        resourceType.setPersistenceSelectorStrategy("strategyA");
        resourceType.setPersistenceSelectorParams("paramX,paramY");
        resourceType.setStorageStrategy("storageXYZ");
        resourceType.setStorageStrategyParams("x=1;y=2");
        resourceType.setEnabled(true);
        resourceTypeDao.saveOrUpdate(resourceType);
        resourceTypeDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        resourceTypeDao.deleteAll(resourceTypeDao.findAll());
        resourceTypeDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();
    }

    @Test
    @Transactional
    public void testFindByNameAndSource() {
        SnmpCollectionResourceType found = resourceTypeDao.findByNameAndSource("TestResourceType", source.getId());
        assertNotNull(found);
        assertEquals("TestResourceType", found.getName());
        assertEquals(source.getId(), found.getCollectionSource().getId());
        assertEquals("JUnitLabel", found.getLabel());
        assertEquals("Test Resource Label", found.getResourceLabel());
        assertEquals("strategyA", found.getPersistenceSelectorStrategy());
        assertEquals("paramX,paramY", found.getPersistenceSelectorParams());
        assertEquals("storageXYZ", found.getStorageStrategy());
        assertEquals("x=1;y=2", found.getStorageStrategyParams());
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testFindByNameAndSourceReturnsNullIfNotExist() {
        SnmpCollectionResourceType found = resourceTypeDao.findByNameAndSource("Nonexistent", source.getId());
        assertNull(found);
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionResourceType found = resourceTypeDao.get(resourceType.getId());
        assertNotNull(found);
        assertEquals(resourceType.getName(), found.getName());
        assertEquals(resourceType.getLabel(), found.getLabel());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionResourceType> enabledList = resourceTypeDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(rt -> "TestResourceType".equals(rt.getName())));
        assertTrue(enabledList.stream().allMatch(SnmpCollectionResourceType::getEnabled));
    }

    @Test
    @Transactional
    public void testFindAllBySource() {
        List<SnmpCollectionResourceType> bySourceList = resourceTypeDao.findAllBySource(source.getId());
        assertNotNull(bySourceList);
        assertFalse(bySourceList.isEmpty());
        assertTrue(bySourceList.stream().allMatch(rt -> rt.getCollectionSource().getId().equals(source.getId())));
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        // Add a disabled resource type
        SnmpCollectionResourceType disabled = new SnmpCollectionResourceType();
        disabled.setName("DisabledResourceType");
        disabled.setEnabled(false);
        disabled.setCollectionSource(source);
        disabled.setLabel("DisabledLabel");
        disabled.setResourceLabel("Disabled Resource Label");
        disabled.setPersistenceSelectorStrategy("disabledStrategy");
        disabled.setPersistenceSelectorParams("disabledParam");
        disabled.setStorageStrategy("disabledStorage");
        disabled.setStorageStrategyParams("z=9");
        resourceTypeDao.saveOrUpdate(disabled);
        resourceTypeDao.flush();

        List<SnmpCollectionResourceType> enabledList = resourceTypeDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionResourceType::getEnabled));
        assertFalse(enabledList.stream().anyMatch(rt -> "DisabledResourceType".equals(rt.getName())));
    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionResourceType> all = resourceTypeDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(rt -> "TestResourceType".equals(rt.getName())));
    }
}
