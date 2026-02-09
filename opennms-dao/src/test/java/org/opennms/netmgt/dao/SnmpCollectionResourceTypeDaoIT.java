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
import org.opennms.netmgt.model.PageResponse;
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

    @Test
    @Transactional
    public void testFindByDataCollectionGroupId_ReturnsValidRecords() {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.source.name");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Group Source for SNMP");
        snmpSourceDao.saveOrUpdate(src);

        // Resource type 1, matches filter "cpu"
        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(src);
        rt1.setName("cpu-resource");
        rt1.setLabel("CPU Utilization");
        rt1.setResourceLabel("CPU Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(true);
        resourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2, matches filter "disk"
        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(src);
        rt2.setName("disk-resource");
        rt2.setLabel("Disk Usage");
        rt2.setResourceLabel("Disk Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(true);
        resourceTypeDao.saveOrUpdate(rt2);
        resourceTypeDao.flush();


        // 1. Exact filter by name, ascending by name
        PageResponse<SnmpCollectionResourceType> result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "cpu-resource", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 2. Partial filter ("resource"), ascending by name
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "resource", "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        // asc: cpu-resource comes first

        // 3. Partial filter, descending by name
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "resource", "name", "DESC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        // desc: disk-resource comes first

        // 4. Filter by label substring, ascending
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "Disk", "label", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Filter by label substring (case-insensitive), descending
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "cpu utilization", "label", "DESC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 6. Pagination: only second returned
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "resource", "name", "ASC", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("disk-resource", (result.getRecords().get(0)).getName());

        // 7. Filter with no match
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), "notfound", "name", "ASC", 0, 0, 10);
        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getRecords().isEmpty());

        // 8. Null filter (should return all for group), ascending by label
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), null, "label", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getRecords().size());
        assertEquals("cpu-resource", (result.getRecords().get(0)).getName());
        assertEquals("disk-resource", (result.getRecords().get(1)).getName());

        // 9. Invalid sortBy field defaults to name ascending
        result = resourceTypeDao.findByDataCollectionGroupId(src.getId(), null, "invalidSort", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals("cpu-resource", (result.getRecords().get(0)).getName());
        assertEquals("disk-resource", (result.getRecords().get(1)).getName());
    }

    @Test
    @Transactional
    public void testDeleteByResourceTypeIds_BadRequestAndSuccess() {
        final var now = new Date();

        SnmpCollectionSource src1 = new SnmpCollectionSource();
        src1.setName("delete.resourcetype.dao.src1");
        src1.setVendor("opennms");
        src1.setDescription("src1");
        src1.setCreatedTime(now);
        src1.setEnabled(true);
        snmpSourceDao.saveOrUpdate(src1);

        SnmpCollectionSource src2 = new SnmpCollectionSource();
        src2.setName("delete.resourcetype.dao.src2");
        src2.setVendor("opennms");
        src2.setDescription("src2");
        src2.setCreatedTime(now);
        src2.setEnabled(true);
        snmpSourceDao.saveOrUpdate(src2);

        snmpSourceDao.flush();

        SnmpCollectionResourceType s1r1 = new SnmpCollectionResourceType();
        s1r1.setCollectionSource(src1);
        s1r1.setName("src1-delete-me-rt-1");
        s1r1.setLabel("Label 1");
        s1r1.setResourceLabel("Resource Label 1");
        s1r1.setPersistenceSelectorStrategy("default");
        s1r1.setStorageStrategy("db");
        s1r1.setEnabled(true);

        SnmpCollectionResourceType s1r2 = new SnmpCollectionResourceType();
        s1r2.setCollectionSource(src1);
        s1r2.setName("src1-delete-me-rt-2");
        s1r2.setLabel("Label 2");
        s1r2.setResourceLabel("Resource Label 2");
        s1r2.setPersistenceSelectorStrategy("default");
        s1r2.setStorageStrategy("db");
        s1r2.setEnabled(true);

        SnmpCollectionResourceType s2r1 = new SnmpCollectionResourceType();
        s2r1.setCollectionSource(src2);
        s2r1.setName("src2-keep-me-rt-1");
        s2r1.setLabel("Keep Label");
        s2r1.setResourceLabel("Keep Resource Label");
        s2r1.setPersistenceSelectorStrategy("default");
        s2r1.setStorageStrategy("db");
        s2r1.setEnabled(true);

        resourceTypeDao.saveOrUpdate(s1r1);
        resourceTypeDao.saveOrUpdate(s1r2);
        resourceTypeDao.saveOrUpdate(s2r1);
        resourceTypeDao.flush();

        resourceTypeDao.deleteByResourceTypeIds(999999, List.of(s1r1.getId(), s1r2.getId()));
        resourceTypeDao.flush();
        resourceTypeDao.clear();

        assertNotNull("ResourceType should still exist when source id doesn't match", resourceTypeDao.get(s1r1.getId()));
        assertNotNull("ResourceType should still exist when source id doesn't match", resourceTypeDao.get(s1r2.getId()));
        assertNotNull("Other source resource type should still exist", resourceTypeDao.get(s2r1.getId()));

        resourceTypeDao.deleteByResourceTypeIds(src1.getId(), List.of(111111, 222222));
        resourceTypeDao.flush();
        resourceTypeDao.clear();

        assertNotNull(resourceTypeDao.get(s1r1.getId()));
        assertNotNull(resourceTypeDao.get(s1r2.getId()));
        assertNotNull(resourceTypeDao.get(s2r1.getId()));

        resourceTypeDao.deleteByResourceTypeIds(src1.getId(), List.of(s1r1.getId(), s1r2.getId()));
        resourceTypeDao.flush();
        resourceTypeDao.clear();

        assertNull(resourceTypeDao.get(s1r1.getId()));
        assertNull(resourceTypeDao.get(s1r2.getId()));
        assertNotNull("Must not delete resource types belonging to other sources", resourceTypeDao.get(s2r1.getId()));
    }
}
