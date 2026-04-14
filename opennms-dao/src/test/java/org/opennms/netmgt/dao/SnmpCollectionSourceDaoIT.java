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
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.model.PageResponse;
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
public class SnmpCollectionSourceDaoIT {

    @Autowired
    private SnmpCollectionSourceDao snmpDao;

    @Autowired
    private SessionFactory sessionFactory;

    private SnmpCollectionSource source;

    @Before
    @Transactional
    public void setUp() {
        // Clean up previous sources
        snmpDao.deleteAll(snmpDao.findAll());
        snmpDao.flush();

        // Add default SNMP source
        source = new SnmpCollectionSource();
        Date now = new Date();
        source.setName("JUnit Source");
        source.setEnabled(true);
        source.setDescription("JUnit Description");
        source.setCreatedTime(now);
        source.setLastModified(now);
        snmpDao.saveOrUpdate(source);
        snmpDao.flush();
    }

    @After
    @Transactional
    public void tearDown() {
        snmpDao.deleteAll(snmpDao.findAll());
        snmpDao.flush();
    }

    @Test
    @Transactional
    public void testFindByName() {
        SnmpCollectionSource found = snmpDao.findByName("JUnit Source");
        assertNotNull(found);
        assertEquals("JUnit Description", found.getDescription());
    }

    @Test
    @Transactional
    public void testEnabledIsPersisted() {
        SnmpCollectionSource found = snmpDao.findByName("JUnit Source");
        assertNotNull(found);
        assertTrue(found.getEnabled());
    }

    @Test
    @Transactional
    public void testGetById() {
        SnmpCollectionSource found = snmpDao.get(source.getId());
        assertNotNull(found);
        assertEquals(source.getName(), found.getName());
    }

    @Test
    @Transactional
    public void testFindAllEnabled() {
        List<SnmpCollectionSource> enabledList = snmpDao.findAllEnabled();
        assertFalse(enabledList.isEmpty());
        assertTrue(enabledList.stream().anyMatch(s -> "JUnit Source".equals(s.getName())));
    }

    @Test
    @Transactional
    public void testFindByNameReturnsNullIfNotExist() {
        SnmpCollectionSource found = snmpDao.findByName("Nonexistent Source");
        assertNull(found);
    }

    @Test
    @Transactional
    public void testFindAllEnabledOnlyReturnsEnabled() {
        SnmpCollectionSource disabled = new SnmpCollectionSource();
        disabled.setName("Disabled Source");
        disabled.setEnabled(false);
        disabled.setDescription("Should not appear in enabled list");
        disabled.setCreatedTime(new Date());
        disabled.setLastModified(new Date());
        snmpDao.saveOrUpdate(disabled);
        snmpDao.flush();

        List<SnmpCollectionSource> enabledList = snmpDao.findAllEnabled();
        assertTrue(enabledList.stream().allMatch(SnmpCollectionSource::getEnabled));
        assertFalse(enabledList.stream().anyMatch(s -> "Disabled Source".equals(s.getName())));
    }

    @Test
    public void testFilterDataCollectionSource_ReturnsValidRecords() {
        final var now = new Date();
        SnmpCollectionSource source1 = new SnmpCollectionSource();
        source1.setName("opennms.test.snmp");
        source1.setVendor("opennms");
        source1.setDescription("Open Network Monitoring System SNMP");
        source1.setCreatedTime(now);
        source1.setEnabled(true);

        SnmpCollectionSource source2 = new SnmpCollectionSource();
        source2.setName("cisco.test.snmp");
        source2.setVendor("cisco");
        source2.setDescription("Cisco SNMP Data Source");
        source2.setCreatedTime(now);
        source2.setEnabled(false);

        snmpDao.saveOrUpdate(source1);
        snmpDao.saveOrUpdate(source2);
        snmpDao.flush();


        // 1. Exact filter, ascending by name
        PageResponse<SnmpCollectionSource> result = snmpDao.filterDataCollectionSource("opennms.test.snmp", "name", "asc", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 2. Partial filter, ascending by name
        result = snmpDao.filterDataCollectionSource("test.snmp", "name", "asc", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 3. Partial filter, descending by name
        result = snmpDao.filterDataCollectionSource("test.snmp", "name", "desc", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 4. Filter by vendor (case-insensitive)
        result = snmpDao.filterDataCollectionSource("CISCO", "name", "asc", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Pagination (only second record returned)
        result = snmpDao.filterDataCollectionSource("test.snmp", "name", "asc", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("opennms.test.snmp", (result.getRecords().get(0)).getName());

        // 6. Filter by vendor substring
        result = snmpDao.filterDataCollectionSource("open", "vendor", "asc", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

    }

    @Test
    @Transactional
    public void testFindAll() {
        List<SnmpCollectionSource> all = snmpDao.findAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
    }
}
