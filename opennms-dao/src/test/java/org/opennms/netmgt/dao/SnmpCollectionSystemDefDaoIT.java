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
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
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
public class SnmpCollectionSystemDefDaoIT {

    @Autowired
    private SnmpCollectionSystemDefDao systemDefDao;

    @Autowired
    private SnmpCollectionSourceDao snmpSourceDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionSource source;
    private SnmpCollectionSystemDef systemDef;

    @Before
    @Transactional
    public void setUp() {
        systemDefDao.deleteAll(systemDefDao.findAll());
        systemDefDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();

        source = new SnmpCollectionSource();
        source.setName("JUnit Source");
        source.setEnabled(true);
        source.setDescription("JUnit Source Description");
        source.setCreatedTime(new Date());
        snmpSourceDao.saveOrUpdate(source);
        snmpSourceDao.flush();

        systemDef = new SnmpCollectionSystemDef();
        systemDef.setCollectionSource(source);
        systemDef.setName("TestSystemDef");
        systemDef.setSysoid(".1.3.6.1.2.1.1");
        systemDef.setSysoidMask("255.255.255.0");
        systemDef.setIpAddresses("192.168.1.0,10.0.0.1");
        systemDef.setIpAddressMasks("255.255.255.0,255.0.0.0");
        systemDef.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        systemDef.setEnabled(true);
        systemDefDao.saveOrUpdate(systemDef);
        systemDefDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        systemDefDao.deleteAll(systemDefDao.findAll());
        systemDefDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();
    }

    @Test
    @Transactional
    public void testFindByNameAndSource() {
        SnmpCollectionSystemDef found = systemDefDao.findByNameAndSource("TestSystemDef", source.getId());
        assertNotNull(found);
        assertEquals("TestSystemDef", found.getName());
        assertEquals(source.getId(), found.getCollectionSource().getId());
        assertEquals(".1.3.6.1.2.1.1", found.getSysoid());
        assertEquals("255.255.255.0", found.getSysoidMask());
        assertEquals("192.168.1.0,10.0.0.1", found.getIpAddresses());
        assertEquals("255.255.255.0,255.0.0.0", found.getIpAddressMasks());
        assertEquals("MIB-GROUP-1,MIB-GROUP-2", found.getMibGroupNames());
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testFindByNameAndSourceReturnsNullIfNotExist() {
        SnmpCollectionSystemDef found = systemDefDao.findByNameAndSource("Nonexistent", source.getId());
        assertNull(found);
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionSystemDef found = systemDefDao.get(systemDef.getId());
        assertNotNull(found);
        assertEquals(systemDef.getName(), found.getName());
        assertEquals(systemDef.getSysoid(), found.getSysoid());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionSystemDef> enabledList = systemDefDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(def -> "TestSystemDef".equals(def.getName())));
        assertTrue(enabledList.stream().allMatch(SnmpCollectionSystemDef::getEnabled));
    }

    @Test
    @Transactional
    public void testFindAllBySource() {
        List<SnmpCollectionSystemDef> bySourceList = systemDefDao.findAllBySource(source.getId());
        assertNotNull(bySourceList);
        assertFalse(bySourceList.isEmpty());
        assertTrue(bySourceList.stream().allMatch(def -> def.getCollectionSource().getId().equals(source.getId())));
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        // Add a disabled system def
        SnmpCollectionSystemDef disabled = new SnmpCollectionSystemDef();
        disabled.setCollectionSource(source);
        disabled.setName("DisabledSystemDef");
        disabled.setSysoid(".1.3.6.1.2.1.2");
        disabled.setSysoidMask("255.255.0.0");
        disabled.setIpAddresses("172.16.0.1");
        disabled.setIpAddressMasks("255.240.0.0");
        disabled.setMibGroupNames("MIB-DISABLED");
        disabled.setEnabled(false);
        systemDefDao.saveOrUpdate(disabled);
        systemDefDao.flush();

        List<SnmpCollectionSystemDef> enabledList = systemDefDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionSystemDef::getEnabled));
        assertFalse(enabledList.stream().anyMatch(def -> "DisabledSystemDef".equals(def.getName())));
    }

    @Test
    @Transactional
    public void testFindByDataCollectionGroupId_ReturnsValidSystemDefs() {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("core-snmp");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Core data source for SNMP collection");
        snmpSourceDao.saveOrUpdate(src);

        // SystemDef 1, matches filter "LinuxSystem"
        SnmpCollectionSystemDef def1 = new SnmpCollectionSystemDef();
        def1.setCollectionSource(src);
        def1.setName("LinuxSystem"); // <--- Name matches test expectation
        def1.setSysoid(".1.3.6.1.2.1.1");
        def1.setSysoidMask("255.255.255.0");
        def1.setIpAddresses("192.168.1.0,10.0.0.1");
        def1.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def1.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        systemDefDao.saveOrUpdate(def1);

        // SystemDef 2, matches filter "WindowsSystem"
        SnmpCollectionSystemDef def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(src);
        def2.setName("WindowsSystem"); // <--- Name matches test expectation
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        systemDefDao.saveOrUpdate(def2);

        systemDefDao.flush();

        // 1. Exact filter by name ASC
        PageResponse<SnmpCollectionSystemDef> result = systemDefDao.findByDataCollectionGroupId(src.getId(), "LinuxSystem", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 2. Partial filter ("System"), ascending by name
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), "System", "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 3. Partial filter, descending by name
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), "System", "name", "DESC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 4. Case-insensitive filter
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), "LINUXSYSTEM", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Pagination - only second returned
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), "System", "name", "ASC", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("WindowsSystem", (result.getRecords().get(0)).getName());

        // 6. Filter with no match
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), "Solaris", "name", "ASC", 0, 0, 10);
        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getRecords().isEmpty());

        // 7. Null filter - should return all for group, ascending
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), null, "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals("LinuxSystem", (result.getRecords().get(0)).getName());
        assertEquals("WindowsSystem", (result.getRecords().get(1)).getName());

        // 8. Invalid sortBy field defaults to name ascending
        result = systemDefDao.findByDataCollectionGroupId(src.getId(), null, "invalidSort", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals("LinuxSystem", (result.getRecords().get(0)).getName());
        assertEquals("WindowsSystem", (result.getRecords().get(1)).getName());
    }

    @Test
    @Transactional
    public void testDeleteBySystemDefIds_BadRequestAndSuccess() {
        final var now = new Date();

        SnmpCollectionSource src1 = new SnmpCollectionSource();
        src1.setName("delete.systemdef.dao.src1");
        src1.setVendor("opennms");
        src1.setDescription("src1");
        src1.setCreatedTime(now);
        src1.setEnabled(true);
        snmpSourceDao.saveOrUpdate(src1);

        SnmpCollectionSource src2 = new SnmpCollectionSource();
        src2.setName("delete.systemdef.dao.src2");
        src2.setVendor("opennms");
        src2.setDescription("src2");
        src2.setCreatedTime(now);
        src2.setEnabled(true);
        snmpSourceDao.saveOrUpdate(src2);

        snmpSourceDao.flush();

        SnmpCollectionSystemDef s1d1 = new SnmpCollectionSystemDef();
        s1d1.setCollectionSource(src1);
        s1d1.setName("src1-delete-me-systemdef-1");
        s1d1.setSysoid(".1.3.6.1.2.1.1");
        s1d1.setSysoidMask("255.255.255.0");
        s1d1.setIpAddresses("192.168.1.0");
        s1d1.setIpAddressMasks("255.255.255.0");
        s1d1.setMibGroupNames("MIB-GROUP-1");
        s1d1.setEnabled(true);

        SnmpCollectionSystemDef s1d2 = new SnmpCollectionSystemDef();
        s1d2.setCollectionSource(src1);
        s1d2.setName("src1-delete-me-systemdef-2");
        s1d2.setSysoid(".1.3.6.1.2.1.2");
        s1d2.setSysoidMask("255.255.255.0");
        s1d2.setIpAddresses("10.0.0.0");
        s1d2.setIpAddressMasks("255.0.0.0");
        s1d2.setMibGroupNames("MIB-GROUP-2");
        s1d2.setEnabled(true);

        SnmpCollectionSystemDef s2d1 = new SnmpCollectionSystemDef();
        s2d1.setCollectionSource(src2);
        s2d1.setName("src2-keep-me-systemdef-1");
        s2d1.setSysoid(".1.3.6.1.2.1.3");
        s2d1.setSysoidMask("255.255.255.0");
        s2d1.setIpAddresses("172.16.0.0");
        s2d1.setIpAddressMasks("255.255.0.0");
        s2d1.setMibGroupNames("MIB-GROUP-KEEP");
        s2d1.setEnabled(true);

        systemDefDao.saveOrUpdate(s1d1);
        systemDefDao.saveOrUpdate(s1d2);
        systemDefDao.saveOrUpdate(s2d1);
        systemDefDao.flush();

        systemDefDao.deleteBySystemDefIds(999999, List.of(s1d1.getId(), s1d2.getId()));
        systemDefDao.flush();
        systemDefDao.clear();

        assertNotNull("SystemDef should still exist when source id doesn't match", systemDefDao.get(s1d1.getId()));
        assertNotNull("SystemDef should still exist when source id doesn't match", systemDefDao.get(s1d2.getId()));
        assertNotNull("Other source system def should still exist", systemDefDao.get(s2d1.getId()));

        systemDefDao.deleteBySystemDefIds(src1.getId(), List.of(111111, 222222));
        systemDefDao.flush();
        systemDefDao.clear();

        assertNotNull(systemDefDao.get(s1d1.getId()));
        assertNotNull(systemDefDao.get(s1d2.getId()));
        assertNotNull(systemDefDao.get(s2d1.getId()));

        systemDefDao.deleteBySystemDefIds(src1.getId(), List.of(s1d1.getId(), s1d2.getId()));
        systemDefDao.flush();
        systemDefDao.clear();

        assertNull(systemDefDao.get(s1d1.getId()));
        assertNull(systemDefDao.get(s1d2.getId()));
        assertNotNull("Must not delete system defs belonging to other sources", systemDefDao.get(s2d1.getId()));
    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionSystemDef> all = systemDefDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(def -> "TestSystemDef".equals(def.getName())));
    }
}
