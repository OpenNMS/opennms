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
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
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
        "classpath:/META-INF/opennms/applicationContext-mockEventForwarder.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpCollectionMibGroupDaoIT {

    @Autowired
    private SnmpCollectionMibGroupDao mibGroupDao;

    @Autowired
    private SnmpCollectionSourceDao snmpSourceDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionSource source;
    private SnmpCollectionMibGroup mibGroup;

    @Before
    @Transactional
    public void setUp() {
        mibGroupDao.deleteAll(mibGroupDao.findAll());
        mibGroupDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();

        source = new SnmpCollectionSource();
        source.setName("JUnit Source");
        source.setEnabled(true);
        source.setDescription("JUnit Description");
        source.setCreatedTime(new Date());
        snmpSourceDao.saveOrUpdate(source);
        snmpSourceDao.flush();

        mibGroup = new SnmpCollectionMibGroup();
        mibGroup.setName("Mib-Group-1");
        mibGroup.setEnabled(true);
        mibGroup.setIfType("ethernetCsmacd");
        mibGroup.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        mibGroup.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        mibGroup.setMibObjProperties("{\"property\":\"value\"}");
        mibGroup.setCollectionSource(source);
        mibGroupDao.saveOrUpdate(mibGroup);
        mibGroupDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        mibGroupDao.deleteAll(mibGroupDao.findAll());
        mibGroupDao.flush();
        snmpSourceDao.deleteAll(snmpSourceDao.findAll());
        snmpSourceDao.flush();
    }

    @Test
    @Transactional
    public void testFindByNameAndSource() {
        SnmpCollectionMibGroup found = mibGroupDao.findByNameAndSource("Mib-Group-1", source.getId());
        assertNotNull(found);
        assertEquals("Mib-Group-1", found.getName());
        assertEquals(source.getId(), found.getCollectionSource().getId());
        assertEquals("ethernetCsmacd", found.getIfType());
        assertEquals("IF-MIB::ifEntry,IF-MIB::ifXEntry", found.getMibGroupNames());
        assertEquals("ifIndex,ifDescr,ifOperStatus", found.getMibObjects());
        assertEquals("{\"property\":\"value\"}", found.getMibObjProperties());
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testFindByNameAndSourceReturnsNullIfNotExist() {
        SnmpCollectionMibGroup found = mibGroupDao.findByNameAndSource("Nonexistent", source.getId());
        assertNull(found);
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionMibGroup found = mibGroupDao.get(mibGroup.getId());
        assertNotNull(found);
        assertEquals(mibGroup.getName(), found.getName());
        assertEquals(mibGroup.getMibObjects(), found.getMibObjects());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionMibGroup> enabledList = mibGroupDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(mg -> "Mib-Group-1".equals(mg.getName())));
        assertTrue(enabledList.stream().allMatch(SnmpCollectionMibGroup::getEnabled));
    }

    @Test
    @Transactional
    public void testFindAllBySource() {
        List<SnmpCollectionMibGroup> bySourceList = mibGroupDao.findAllBySource(source.getId());
        assertNotNull(bySourceList);
        assertFalse(bySourceList.isEmpty());
        assertTrue(bySourceList.stream().allMatch(mg -> mg.getCollectionSource().getId().equals(source.getId())));
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        // Add a disabled group
        SnmpCollectionMibGroup disabled = new SnmpCollectionMibGroup();
        disabled.setName("DisabledGroup");
        disabled.setEnabled(false);
        disabled.setIfType("loopback");
        disabled.setMibGroupNames("IF-MIB::ifLoopback");
        disabled.setMibObjects("ifIndex,ifOperStatus");
        disabled.setMibObjProperties("{}");
        disabled.setCollectionSource(source);
        mibGroupDao.saveOrUpdate(disabled);
        mibGroupDao.flush();

        List<SnmpCollectionMibGroup> enabledList = mibGroupDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionMibGroup::getEnabled));
        assertFalse(enabledList.stream().anyMatch(g -> "DisabledGroup".equals(g.getName())));
    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionMibGroup> all = mibGroupDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(mg -> "Mib-Group-1".equals(mg.getName())));
    }

    @Test
    @Transactional
    public void testFindByCollectionSourceId_ReturnsValidMibGroups() {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for MIB groups");
        src.setCreatedTime(new Date());
        snmpSourceDao.saveOrUpdate(src);
        snmpSourceDao.flush();

        // Mib Group 1: Matches "interfaces"
        SnmpCollectionMibGroup group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(src);
        group1.setName("if-mib-interfaces");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group1.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        mibGroupDao.saveOrUpdate(group1);
        mibGroupDao.flush();
        // Mib Group 2: Matches "ip"
        SnmpCollectionMibGroup group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(src);
        group2.setName("ip-mib");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group2.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        mibGroupDao.saveOrUpdate(group2);

        mibGroupDao.flush();

        // 1. Exact filter by name ASC
        PageResponse<SnmpCollectionMibGroup> result = mibGroupDao.findByCollectionSourceId(src.getId(), "if-mib-interfaces", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 2. Partial filter ("mib"), ascending by name
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "mib", "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        // asc: if-mib-interfaces comes first

        // 3. Partial filter, descending by name
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "mib", "name", "DESC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        // desc: ip-mib comes first

        // 4. Filter by ifType substring, ascending
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "Ethernet", "ifType", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Filter by ifType substring, descending
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "Loopback", "ifType", "DESC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 6. Case-insensitive filter (should match "ip-MIB")
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "IP-MIB", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 7. Pagination - only second result returned
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "mib", "name", "ASC", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("ip-mib", (result.getRecords().get(0)).getName());

        // 8. Filter with no match
        result = mibGroupDao.findByCollectionSourceId(src.getId(), "not-found", "name", "ASC", 0, 0, 10);
        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getRecords().isEmpty());

    }


}


