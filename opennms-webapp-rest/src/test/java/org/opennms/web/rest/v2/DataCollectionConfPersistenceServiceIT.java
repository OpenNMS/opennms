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
package org.opennms.web.rest.v2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.PageResponse;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;
import org.opennms.netmgt.model.SnmpCollectionMibGroupDto;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class DataCollectionConfPersistenceServiceIT {

    @Autowired
    private DataCollectionConfPersistenceService dataCollectionConfPersistenceService;

    @Autowired
    private SnmpCollectionSourceDao snmpCollectionSourceDao;

    @Autowired
    private SnmpCollectionResourceTypeDao snmpCollectionResourceTypeDao;

    @Autowired
    private SnmpCollectionMibGroupDao snmpCollectionMibGroupDao;

    @Autowired
    private SnmpCollectionSystemDefDao snmpCollectionSystemDefDao;

    private int defaultSourceCount, defaultResourceTypeCount, defaultMibGroupCount, defaultSystemDefCount;

    @Before
    @Transactional
    public void setUp() {
        defaultSourceCount = snmpCollectionSourceDao.findAll().size();
        defaultResourceTypeCount = snmpCollectionResourceTypeDao.findAll().size();
        defaultMibGroupCount = snmpCollectionMibGroupDao.findAll().size();
        defaultSystemDefCount = snmpCollectionSystemDefDao.findAll().size();
    }

    @Test
    @JUnitTemporaryDatabase
    @Transactional
    public void testAddDataCollectionConfigWith3ComData() {
        String fileName = "3com-datacollection.xml";
        String userName = "testuser";
        Date now = new Date();

        DatacollectionGroup dataCollectionGroup = build3ComDatacollectionGroup();

        Integer srcId = dataCollectionConfPersistenceService.addDataCollectionConfig(
                fileName, userName, dataCollectionGroup, now, java.util.List.of());

        // Persisted source
        List<SnmpCollectionSource> sources = snmpCollectionSourceDao.findAll();
        assertEquals(defaultSourceCount + 1, sources.size());
        SnmpCollectionSource persistedSource = snmpCollectionSourceDao.get(srcId);
        assertNotNull(persistedSource);
        assertEquals(fileName, persistedSource.getName());
        assertEquals("3Com", persistedSource.getVendor());
        assertEquals(userName, persistedSource.getUploadedBy());
        assertTrue(persistedSource.getEnabled());

        // Mib Groups
        List<SnmpCollectionMibGroup> mibGroups = snmpCollectionMibGroupDao.findAll();
        assertEquals(defaultMibGroupCount + 2, mibGroups.size());
        boolean foundGroup1 = false, foundGroup2 = false;
        for (SnmpCollectionMibGroup m : mibGroups) {
            if (m.getName().equals("3com-router-perf")) foundGroup1 = true;
            if (m.getName().equals("3com-router-sys")) foundGroup2 = true;
        }
        assertTrue(foundGroup1);
        assertTrue(foundGroup2);

        // SystemDefs
        List<SnmpCollectionSystemDef> systemDefs = snmpCollectionSystemDefDao.findAll();
        assertEquals(defaultSystemDefCount + 1, systemDefs.size());
        SnmpCollectionSystemDef sysDef = systemDefs.get(systemDefs.size()-1);
        assertEquals("3Com Routers", sysDef.getName());
        assertEquals(".1.3.6.1.4.1.43.", sysDef.getSysoidMask());
        assertNotNull(sysDef.getMibGroupNames());

        // No resource types in data
        List<SnmpCollectionResourceType> resourceTypes = snmpCollectionResourceTypeDao.findAll();
        assertEquals(defaultResourceTypeCount, resourceTypes.size());
    }

    public static DatacollectionGroup build3ComDatacollectionGroup() {
        Group group1 = new Group();
        group1.setName("3com-router-perf");
        group1.setIfType("ignore");
        group1.setMibObjs(Arrays.asList(
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.2.1.4", "0", "a3perfBufMemAvail", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.2.1.5", "0", "a3perfBufMemFailed", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.2.1.3", "0", "a3perfBufMemTotal", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.33.1.1.1",     "0", "a3perfBufMemTotAvl", "integer")
        ));

        Group group2 = new Group();
        group2.setName("3com-router-sys");
        group2.setIfType("ignore");
        group2.setMibObjs(Arrays.asList(
                createMibObj(".1.3.6.1.4.1.43.2.13.3.1.1.5", "0", "a3sysMemSize", "integer"),
                createMibObj(".1.3.6.1.4.1.43.2.13.8.4",     "0", "a3sysCpuUtil", "integer")
        ));

        SystemDef systemDef = new SystemDef();
        systemDef.setName("3Com Routers");
        systemDef.setSysoidMask(".1.3.6.1.4.1.43.");


        DatacollectionGroup group = new DatacollectionGroup();
        group.setName("3Com");
        group.setGroups(Arrays.asList(group1, group2));
        group.setSystemDefs(List.of(systemDef));
        return group;
    }

    @Test
    @Transactional
    public void testFilterSnmpCollectionSources() {
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

        snmpCollectionSourceDao.saveOrUpdate(source1);
        snmpCollectionSourceDao.saveOrUpdate(source2);
        snmpCollectionSourceDao.flush();


        // 1. Exact filter, ascending by name
        PageResponse<SnmpCollectionSource> result = dataCollectionConfPersistenceService.filterSnmpCollectionSources("opennms.test.snmp", "name", "asc", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());


        // 2. Partial filter, ascending by name
        result = dataCollectionConfPersistenceService.filterSnmpCollectionSources("test.snmp", "name", "asc", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 3. Partial filter, descending by name
        result = dataCollectionConfPersistenceService.filterSnmpCollectionSources("test.snmp", "name", "desc", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 4. Filter by vendor (case-insensitive)
        result = dataCollectionConfPersistenceService.filterSnmpCollectionSources("CISCO", "name", "asc", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Pagination (only second record returned)
        result = dataCollectionConfPersistenceService.filterSnmpCollectionSources("test.snmp", "name", "asc", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("opennms.test.snmp", (result.getRecords().get(0)).getName());

        // 6. Filter by vendor substring
        result = dataCollectionConfPersistenceService.filterSnmpCollectionSources("open", "vendor", "asc", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());
    }

    @Test
    @Transactional
    public void shouldRetrieveSnmpCollectionSourceById() {
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

        snmpCollectionSourceDao.saveOrUpdate(source1);
        snmpCollectionSourceDao.saveOrUpdate(source2);
        snmpCollectionSourceDao.flush();

        // Act & Assert: source2
        SnmpCollectionSource ciscoCollectionSource =
                dataCollectionConfPersistenceService.getSnmpCollectionSourceById(source2.getId());

        assertNotNull(ciscoCollectionSource, "Should retrieve Cisco collection source by id");
        assertEquals("Names should match","cisco.test.snmp", ciscoCollectionSource.getName());
        assertEquals("Vendors should match","cisco", ciscoCollectionSource.getVendor());
        assertEquals("Descriptions should match","Cisco SNMP Data Source", ciscoCollectionSource.getDescription());

        // Act & Assert: source1
        SnmpCollectionSource opennmsCollectionSource =
                dataCollectionConfPersistenceService.getSnmpCollectionSourceById(source1.getId());

        assertNotNull(opennmsCollectionSource, "Should retrieve OpenNMS collection source by id");
        assertEquals("Names should match","opennms.test.snmp", opennmsCollectionSource.getName());
        assertEquals( "Vendors should match","opennms", opennmsCollectionSource.getVendor());
        assertEquals("Descriptions should match","Open Network Monitoring System SNMP", opennmsCollectionSource.getDescription());
    }


    @Test
    @Transactional
    public void shouldReturnCorrectSnmpCollectionSourceIdsAndNames() {
        // Arrange
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

        snmpCollectionSourceDao.saveOrUpdate(source1);
        snmpCollectionSourceDao.saveOrUpdate(source2);

        snmpCollectionSourceDao.flush();

        // Act
        Map<Integer, String> idsAndNamesMap =
                dataCollectionConfPersistenceService.getSnmpCollectionSourceNamesAndIds();

        // Assert the IDs are present and mapped to the correct names
        assertTrue("Map should contain source1 ID", idsAndNamesMap.containsKey(source1.getId()));
        assertTrue("Map should contain source2 ID", idsAndNamesMap.containsKey(source2.getId()));
        assertEquals("opennms.test.snmp", idsAndNamesMap.get(source1.getId()));
        assertEquals("cisco.test.snmp", idsAndNamesMap.get(source2.getId()));
    }

    @Test
    @Transactional
    public void testFilterMibGroupByCollectionSourceId() {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for MIB groups");
        src.setCreatedTime(new Date());
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        // Mib Group 1: Matches "interfaces"
        SnmpCollectionMibGroup group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(src);
        group1.setName("if-mib-interfaces");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group1.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group1);
        snmpCollectionMibGroupDao.flush();
        // Mib Group 2: Matches "ip"
        SnmpCollectionMibGroup group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(src);
        group2.setName("ip-mib");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group2.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group2);
        snmpCollectionMibGroupDao.flush();

        // 1. Exact filter by name ASC
        PageResponse<SnmpCollectionMibGroup> result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "if-mib-interfaces", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 2. Partial filter ("mib"), ascending by name
        result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "mib", "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        // asc: if-mib-interfaces comes first

        // 3. Partial filter, descending by name
         result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "mib", "name", "DESC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 4. Filter by ifType substring, ascending
        result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "Ethernet", "ifType", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Filter by ifType substring, descending
        result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "Loopback", "ifType", "DESC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 6. Case-insensitive filter (should match "ip-MIB")
        result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "IP-MIB", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 7. Pagination - only second result returned
        result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "mib", "name", "ASC", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("ip-mib", result.getRecords().get(0).getName());

        // 8. Filter with no match
        result = dataCollectionConfPersistenceService.filterMibGroupByCollectionSourceId(src.getId(), "not-found", "name", "ASC", 0, 0, 10);
        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getRecords().isEmpty());

    }

    @Test
    @Transactional
    public void testFilterResourceTypeByCollectionSourceId() {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.source.name");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Group Source for SNMP");
        snmpCollectionSourceDao.saveOrUpdate(src);

        // Resource type 1, matches filter "cpu"
        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(src);
        rt1.setName("cpu-resource");
        rt1.setLabel("CPU Utilization");
        rt1.setResourceLabel("CPU Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2, matches filter "disk"
        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(src);
        rt2.setName("disk-resource");
        rt2.setLabel("Disk Usage");
        rt2.setResourceLabel("Disk Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        // 1. Exact filter by name, ascending by name
        PageResponse<SnmpCollectionResourceType> result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "cpu-resource", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 2. Partial filter ("resource"), ascending by name
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "resource", "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 3. Partial filter, descending by name
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "resource", "name", "DESC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());


        // 4. Filter by label substring, ascending
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "Disk", "label", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());

        // 5. Filter by label substring (case-insensitive), descending
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "cpu utilization", "label", "DESC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());


        // 6. Pagination: only second returned
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "resource", "name", "ASC", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("disk-resource", (result.getRecords().get(0)).getName());

        // 7. Filter with no match
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), "notfound", "name", "ASC", 0, 0, 10);
        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getRecords().isEmpty());

        // 8. Null filter (should return all for group), ascending by label
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), null, "label", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals(2, result.getRecords().size());
        assertEquals("cpu-resource", (result.getRecords().get(0)).getName());
        assertEquals("disk-resource", (result.getRecords().get(1)).getName());

        // 9. Invalid sortBy field defaults to name ascending
        result = dataCollectionConfPersistenceService.filterResourceTypeByCollectionSourceId(src.getId(), null, "invalidSort", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals("cpu-resource", (result.getRecords().get(0)).getName());
        assertEquals("disk-resource", (result.getRecords().get(1)).getName());
    }

    @Test
    @Transactional
    public void testFilterSystemDefByCollectionSourceId() {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("core-snmp");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Core data source for SNMP collection");
        snmpCollectionSourceDao.saveOrUpdate(src);

        // SystemDef 1, matches filter "LinuxSystem"
        SnmpCollectionSystemDef def1 = new SnmpCollectionSystemDef();
        def1.setCollectionSource(src);
        def1.setName("LinuxSystem"); // <--- Name matches test expectation
        def1.setSysoid(".1.3.6.1.2.1.1");
        def1.setSysoidMask("255.255.255.0");
        def1.setIpAddresses("192.168.1.0,10.0.0.1");
        def1.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def1.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        snmpCollectionSystemDefDao.saveOrUpdate(def1);

        // SystemDef 2, matches filter "WindowsSystem"
        SnmpCollectionSystemDef def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(src);
        def2.setName("WindowsSystem"); // <--- Name matches test expectation
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        snmpCollectionSystemDefDao.saveOrUpdate(def2);

        snmpCollectionSystemDefDao.flush();


        // 1. Exact filter by name ASC
        PageResponse<SnmpCollectionSystemDef> result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), "LinuxSystem", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());


        // 2. Partial filter ("System"), ascending by name
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), "System", "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 3. Partial filter, descending by name
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), "System", "name", "DESC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());

        // 4. Case-insensitive filter
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), "LINUXSYSTEM", "name", "ASC", 0, 0, 10);
        assertEquals(1, result.getTotalRecords());


        // 5. Pagination - only second returned
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), "System", "name", "ASC", 0, 1, 1);
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getRecords().size());
        assertEquals("WindowsSystem", (result.getRecords().get(0)).getName());

        // 6. Filter with no match
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), "Solaris", "name", "ASC", 0, 0, 10);
        assertEquals(0, result.getTotalRecords());
        assertTrue(result.getRecords().isEmpty());

        // 7. Null filter - should return all for group, ascending
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), null, "name", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals("LinuxSystem", (result.getRecords().get(0)).getName());
        assertEquals("WindowsSystem", (result.getRecords().get(1)).getName());

        // 8. Invalid sortBy field defaults to name ascending
        result = dataCollectionConfPersistenceService.filterSystemDefByCollectionSourceId(src.getId(), null, "invalidSort", "ASC", 0, 0, 10);
        assertEquals(2, result.getTotalRecords());
        assertEquals("LinuxSystem", (result.getRecords().get(0)).getName());
        assertEquals("WindowsSystem", (result.getRecords().get(1)).getName());
    }

    @Test
    @Transactional
    public void testGetAllResourceTypeNames() {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("resource-type-source");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Source for Resource Types");
        snmpCollectionSourceDao.saveOrUpdate(src);

        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(src);
        rt1.setName("memory-resource");
        rt1.setLabel("Memory Utilization");
        rt1.setResourceLabel("Memory Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(src);
        rt2.setName("storage-resource");
        rt2.setLabel("Storage Usage");
        rt2.setResourceLabel("Storage Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        List<String> resourceTypeNames = dataCollectionConfPersistenceService.getAllResourceTypeNames();

        assertEquals("0", resourceTypeNames.get(0));
        assertEquals("ifIndex", resourceTypeNames.get(1));
        assertTrue(resourceTypeNames.contains("memory-resource"));
        assertTrue(resourceTypeNames.contains("storage-resource"));
    }

    @Test
    @Transactional
    public void testGetAllMibGroupNames() {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("mib-group-source");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Source for MIB Groups");
        snmpCollectionSourceDao.saveOrUpdate(src);

        SnmpCollectionMibGroup group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(src);
        group1.setName("system-mib-group");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("SYSTEM-MIB::sysEntry");
        group1.setMibObjects("sysDescr,sysUpTime");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group1);

        SnmpCollectionMibGroup group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(src);
        group2.setName("interface-mib-group");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry");
        group2.setMibObjects("ifIndex,ifDescr");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group2);
        snmpCollectionMibGroupDao.flush();

        List<String> mibGroupNames = dataCollectionConfPersistenceService.getAllMibGroupNames();

        assertTrue(mibGroupNames.contains("system-mib-group"));
        assertTrue(mibGroupNames.contains("interface-mib-group"));
    }

    @Test
    @Transactional
    public void shouldAddMibGroupToSnmpCollectionSource() {
        final var src = new SnmpCollectionSource();
        src.setName("test-source.snmp");
        src.setVendor("test-vendor");
        src.setDescription("Test SNMP source");
        src.setCreatedTime(new Date());
        src.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        final int before = snmpCollectionMibGroupDao.findAll().size();

        final var dto = new SnmpCollectionMibGroupDto();
        dto.setName("if-mib-interfaces");
        dto.setIfType("Ethernet");
        dto.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        dto.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        dto.setMibObjProperties("{\"property\":\"value\"}");
        dto.setEnabled(true);

        final Integer id = dataCollectionConfPersistenceService.addMibGroupToSnmpCollectionSources(src, dto);

        assertNotNull(id);
        assertEquals(before + 1, snmpCollectionMibGroupDao.findAll().size());

        final SnmpCollectionMibGroup persisted = snmpCollectionMibGroupDao.get(id);
        assertNotNull(persisted);

        assertNotNull(persisted.getCollectionSource());
        assertEquals(src.getId(), persisted.getCollectionSource().getId());

        assertEquals("if-mib-interfaces", persisted.getName());
        assertEquals("Ethernet", persisted.getIfType());
        assertEquals("IF-MIB::ifEntry,IF-MIB::ifXEntry", persisted.getMibGroupNames());
        assertEquals("ifIndex,ifDescr,ifOperStatus", persisted.getMibObjects());
        assertEquals("{\"property\":\"value\"}", persisted.getMibObjProperties());
    }

    @Test
    @Transactional
    public void shouldAddResourceTypeToSnmpCollectionSource() {
        // Arrange: create a source
        final var src = new SnmpCollectionSource();
        src.setName("resource-source.snmp");
        src.setVendor("test-vendor");
        src.setDescription("Test source for resource types");
        src.setCreatedTime(new Date());
        src.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        final int before = snmpCollectionResourceTypeDao.findAll().size();

        // Arrange: build request DTO
        final var dto = new SnmpCollectionResourceTypeDto();
        dto.setName("cpu-resource");
        dto.setLabel("CPU Utilization");
        dto.setResourceLabel("CPU Resource Label");
        dto.setPersistenceSelectorStrategy("default");
        dto.setStorageStrategy("db");
        dto.setEnabled(true);

        // Act
        final Integer id = dataCollectionConfPersistenceService.addResourceTypeToSnmpCollectionSources(src, dto);

        // Assert
        assertNotNull(id);
        assertEquals(before + 1, snmpCollectionResourceTypeDao.findAll().size());

        final SnmpCollectionResourceType persisted = snmpCollectionResourceTypeDao.get(id);
        assertNotNull(persisted);

        // linked source
        assertNotNull(persisted.getCollectionSource());
        assertEquals(src.getId(), persisted.getCollectionSource().getId());

        // mapped fields
        assertEquals("cpu-resource", persisted.getName());
        assertEquals("CPU Utilization", persisted.getLabel());
        assertEquals("CPU Resource Label", persisted.getResourceLabel());
        assertEquals("default", persisted.getPersistenceSelectorStrategy());
        assertEquals("db", persisted.getStorageStrategy());
        assertTrue(persisted.getEnabled());
    }
    @Test
    @Transactional
    public void shouldAddSystemDefToSnmpCollectionSource() {
        final var src = new SnmpCollectionSource();
        src.setName("systemdef-source.snmp");
        src.setVendor("test-vendor");
        src.setDescription("Test source for system defs");
        src.setCreatedTime(new Date());
        src.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        final int before = snmpCollectionSystemDefDao.findAll().size();

        final var dto = new SnmpCollectionSystemDefDto();
        dto.setName("LinuxSystem");
        dto.setSysoid(".1.3.6.1.2.1.1");
        dto.setSysoidMask("255.255.255.0");
        dto.setIpAddresses(java.util.List.of("192.168.1.0", "10.0.0.1"));
        dto.setIpAddressMasks(java.util.List.of("255.255.255.0", "255.0.0.0"));
        dto.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        dto.setEnabled(true);

        final Integer id = dataCollectionConfPersistenceService.addSystemDefToSnmpCollectionSources(src, dto);

        assertNotNull(id);
        assertEquals(before + 1, snmpCollectionSystemDefDao.findAll().size());

        final SnmpCollectionSystemDef persisted = snmpCollectionSystemDefDao.get(id);
        assertNotNull(persisted);

        assertNotNull(persisted.getCollectionSource());
        assertEquals(src.getId(), persisted.getCollectionSource().getId());

        assertEquals("LinuxSystem", persisted.getName());
        assertEquals(".1.3.6.1.2.1.1", persisted.getSysoid());
        assertEquals("255.255.255.0", persisted.getSysoidMask());
        // The persistence service serialises ipAddresses + ipAddressMasks
        // into the canonical IpList JSON the runtime loader expects, so the
        // entity column now holds JSON, not the raw comma-separated string.
        // Assert the round-trip rather than the exact bytes.
        final org.opennms.netmgt.config.datacollection.IpList persistedIpList =
                org.opennms.netmgt.config.api.DatacollectionJsonHelper
                        .fromJsonToIpList(persisted.getIpAddresses());
        assertNotNull(persistedIpList);
        assertEquals(java.util.List.of("192.168.1.0", "10.0.0.1"), persistedIpList.getIpAddresses());
        assertEquals(java.util.List.of("255.255.255.0", "255.0.0.0"), persistedIpList.getIpAddressMasks());
        assertEquals("MIB-GROUP-1,MIB-GROUP-2", persisted.getMibGroupNames());
    }

    @Test
    @Transactional
    public void shouldUpdateMibGroupForGivenSourceAndId() {
        final var src = new SnmpCollectionSource();
        src.setName("update-mibgroup-source.snmp");
        src.setVendor("test-vendor");
        src.setDescription("Source for updateMibGroup test");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        final var existing = new SnmpCollectionMibGroup();
        existing.setCollectionSource(src);
        existing.setName("old-mib-group");
        existing.setIfType("OldIfType");
        existing.setMibGroupNames("OLD-MIB::oldEntry");
        existing.setMibObjects("oldIndex,oldDescr");
        existing.setMibObjProperties("{\"old\":\"value\"}");
        existing.setEnabled(true);
        snmpCollectionMibGroupDao.saveOrUpdate(existing);
        snmpCollectionMibGroupDao.flush();

        final int before = snmpCollectionMibGroupDao.findAll().size();

        final var dto = new SnmpCollectionMibGroupDto();
        dto.setName("new-mib-group");
        dto.setIfType("NewIfType");
        dto.setMibGroupNames("NEW-MIB::newEntry");
        dto.setMibObjects("newIndex,newDescr");
        dto.setMibObjProperties("{\"new\":\"value\"}");
        dto.setEnabled(true);

        dataCollectionConfPersistenceService.updateMibGroup(existing.getId(), src.getId(), dto);

        assertEquals(before, snmpCollectionMibGroupDao.findAll().size());

        final SnmpCollectionMibGroup updated = snmpCollectionMibGroupDao.get(existing.getId());
        assertNotNull(updated);
        assertNotNull(updated.getCollectionSource());
        assertEquals(src.getId(), updated.getCollectionSource().getId());

        assertEquals("new-mib-group", updated.getName());
        assertEquals("NewIfType", updated.getIfType());
        assertEquals("NEW-MIB::newEntry", updated.getMibGroupNames());
        assertEquals("newIndex,newDescr", updated.getMibObjects());
        assertEquals("{\"new\":\"value\"}", updated.getMibObjProperties());
    }

    @Test
    @Transactional
    public void shouldUpdateResourceTypeForGivenSourceAndId() {
        final var src = new SnmpCollectionSource();
        src.setName("update-resourcetype-source.snmp");
        src.setVendor("test-vendor");
        src.setDescription("Source for updateResourceType test");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        final var existing = new SnmpCollectionResourceType();
        existing.setCollectionSource(src);
        existing.setName("old-resource");
        existing.setLabel("Old Label");
        existing.setResourceLabel("Old Resource Label");
        existing.setPersistenceSelectorStrategy("oldSelector");
        existing.setStorageStrategy("oldStorage");
        existing.setEnabled(false);
        snmpCollectionResourceTypeDao.saveOrUpdate(existing);
        snmpCollectionResourceTypeDao.flush();

        final int before = snmpCollectionResourceTypeDao.findAll().size();
        final var dto = new SnmpCollectionResourceTypeDto();
        dto.setName("new-resource");
        dto.setLabel("New Label");
        dto.setResourceLabel("New Resource Label");
        dto.setPersistenceSelectorStrategy("newSelector");
        dto.setStorageStrategy("newStorage");
        dto.setEnabled(true);

        dataCollectionConfPersistenceService.updateResourceType(existing.getId(), src.getId(), dto);

        assertEquals(before, snmpCollectionResourceTypeDao.findAll().size());

        final SnmpCollectionResourceType updated = snmpCollectionResourceTypeDao.get(existing.getId());
        assertNotNull(updated);
        assertNotNull(updated.getCollectionSource());
        assertEquals(src.getId(), updated.getCollectionSource().getId());

        assertEquals("new-resource", updated.getName());
        assertEquals("New Label", updated.getLabel());
        assertEquals("New Resource Label", updated.getResourceLabel());
        assertEquals("newSelector", updated.getPersistenceSelectorStrategy());
        assertEquals("newStorage", updated.getStorageStrategy());
        assertTrue(updated.getEnabled());
    }

    @Test
    @Transactional
    public void shouldUpdateSystemDefForGivenSourceAndId() {
        final var src = new SnmpCollectionSource();
        src.setName("update-systemdef-source.snmp");
        src.setVendor("test-vendor");
        src.setDescription("Source for updateSystemDef test");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        final var existing = new SnmpCollectionSystemDef();
        existing.setCollectionSource(src);
        existing.setName("OldSystem");
        existing.setSysoid(".1.3.6.1.2.1.1");
        existing.setSysoidMask("255.255.0.0");
        existing.setIpAddresses("10.0.0.1");
        existing.setIpAddressMasks("255.0.0.0");
        existing.setMibGroupNames("OLD-GROUP");
        existing.setEnabled(true);
        snmpCollectionSystemDefDao.saveOrUpdate(existing);
        snmpCollectionSystemDefDao.flush();

        final int before = snmpCollectionSystemDefDao.findAll().size();

        final var dto = new SnmpCollectionSystemDefDto();
        dto.setName("NewSystem");
        dto.setSysoid(".1.3.6.1.2.1.2");
        dto.setSysoidMask("255.255.255.0");
        dto.setIpAddresses(java.util.List.of("192.168.1.0", "10.0.0.2"));
        dto.setIpAddressMasks(java.util.List.of("255.255.255.0", "255.0.0.0"));
        dto.setMibGroupNames("NEW-GROUP-1,NEW-GROUP-2");
        dto.setEnabled(true);

        dataCollectionConfPersistenceService.updateSystemDef(existing.getId(), src.getId(), dto);
        assertEquals(before, snmpCollectionSystemDefDao.findAll().size());

        final SnmpCollectionSystemDef updated = snmpCollectionSystemDefDao.get(existing.getId());
        assertNotNull(updated);
        assertNotNull(updated.getCollectionSource());
        assertEquals(src.getId(), updated.getCollectionSource().getId());

        assertEquals("NewSystem", updated.getName());
        assertEquals(".1.3.6.1.2.1.2", updated.getSysoid());
        assertEquals("255.255.255.0", updated.getSysoidMask());
        // ipAddresses column now holds canonical IpList JSON (see addSystemDef
        // assertions above) — round-trip via the helper instead of comparing bytes.
        final org.opennms.netmgt.config.datacollection.IpList updatedIpList =
                org.opennms.netmgt.config.api.DatacollectionJsonHelper
                        .fromJsonToIpList(updated.getIpAddresses());
        assertNotNull(updatedIpList);
        assertEquals(java.util.List.of("192.168.1.0", "10.0.0.2"), updatedIpList.getIpAddresses());
        assertEquals(java.util.List.of("255.255.255.0", "255.0.0.0"), updatedIpList.getIpAddressMasks());
        assertEquals("NEW-GROUP-1,NEW-GROUP-2", updated.getMibGroupNames());
    }
    @Test
    @Transactional
    public void testDeleteSnmpDataCollectionSources_BadRequest_AndSuccess() throws Exception {
        // --- Setup: 2 sources
        final var now = new Date();

        SnmpCollectionSource s1 = new SnmpCollectionSource();
        s1.setName("delete.source.combo.1");
        s1.setVendor("opennms");
        s1.setDescription("source 1");
        s1.setCreatedTime(now);
        s1.setEnabled(true);

        SnmpCollectionSource s2 = new SnmpCollectionSource();
        s2.setName("delete.source.combo.2");
        s2.setVendor("opennms");
        s2.setDescription("source 2");
        s2.setCreatedTime(now);
        s2.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(s1);
        snmpCollectionSourceDao.saveOrUpdate(s2);
        snmpCollectionSourceDao.flush();
        // --- SUCCESS: delete both
        dataCollectionConfPersistenceService.deleteSnmpDataCollectionSources(List.of(s1.getId(), s2.getId()));

        snmpCollectionSourceDao.flush();
        snmpCollectionSourceDao.clear();

        assertNull(snmpCollectionSourceDao.get(s1.getId()));
        assertNull(snmpCollectionSourceDao.get(s2.getId()));
    }

    @Test
    @Transactional
    public void testDeleteSnmpDataCollectionMibGroupsForSource_BadRequest_AndSuccess() throws Exception {
        // --- Setup: source + 2 mib groups
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("delete.mibgroup.combo.source");
        src.setVendor("opennms");
        src.setDescription("source for mib group delete");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionMibGroup g1 = new SnmpCollectionMibGroup();
        g1.setCollectionSource(src);
        g1.setName("delete-me-mibgroup-1");
        g1.setIfType("Ethernet");
        g1.setMibGroupNames("IF-MIB::ifEntry");
        g1.setMibObjects("ifIndex");
        g1.setMibObjProperties("{\"k\":\"v\"}");
        g1.setEnabled(true);

        SnmpCollectionMibGroup g2 = new SnmpCollectionMibGroup();
        g2.setCollectionSource(src);
        g2.setName("delete-me-mibgroup-2");
        g2.setIfType("Ethernet");
        g2.setMibGroupNames("IP-MIB::ipAddrTable");
        g2.setMibObjects("ipAdEntAddr");
        g2.setMibObjProperties("{\"k\":\"v\"}");
        g2.setEnabled(true);

        snmpCollectionMibGroupDao.saveOrUpdate(g1);
        snmpCollectionMibGroupDao.saveOrUpdate(g2);
        snmpCollectionMibGroupDao.flush();

        // --- BAD_REQUEST/NOT_FOUND: missing source id
        try {
            dataCollectionConfPersistenceService.deleteSnmpDataCollectionMibGroups(999999, List.of(g1.getId()));
            Assert.fail("Expected EntityNotFoundException for missing source");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("SnmpDataCollectionSource not found for id: 999999"));
        }

        // --- SUCCESS: delete both
        dataCollectionConfPersistenceService.deleteSnmpDataCollectionMibGroups(src.getId(), List.of(g1.getId(), g2.getId()));

        snmpCollectionMibGroupDao.flush();
        snmpCollectionMibGroupDao.clear();

        assertEquals(null, snmpCollectionMibGroupDao.get(g1.getId()));
        assertEquals(null, snmpCollectionMibGroupDao.get(g2.getId()));
    }

    @Test
    @Transactional
    public void testDeleteSnmpDataCollectionResourceTypesForSource_BadRequest_AndSuccess() throws Exception {
        // --- Setup: source + 2 resource types
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("delete.resourcetype.combo.source");
        src.setVendor("opennms");
        src.setDescription("source for resource type delete");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionResourceType r1 = new SnmpCollectionResourceType();
        r1.setCollectionSource(src);
        r1.setName("delete-me-rt-1");
        r1.setLabel("Label1");
        r1.setResourceLabel("RL1");
        r1.setPersistenceSelectorStrategy("default");
        r1.setStorageStrategy("db");
        r1.setEnabled(true);

        SnmpCollectionResourceType r2 = new SnmpCollectionResourceType();
        r2.setCollectionSource(src);
        r2.setName("delete-me-rt-2");
        r2.setLabel("Label2");
        r2.setResourceLabel("RL2");
        r2.setPersistenceSelectorStrategy("default");
        r2.setStorageStrategy("db");
        r2.setEnabled(true);

        snmpCollectionResourceTypeDao.saveOrUpdate(r1);
        snmpCollectionResourceTypeDao.saveOrUpdate(r2);
        snmpCollectionResourceTypeDao.flush();

        // --- BAD_REQUEST/NOT_FOUND: missing source id

        try {
            dataCollectionConfPersistenceService.deleteSnmpDataCollectionResourceTypes(999999, List.of(r1.getId()));
            Assert.fail("Expected EntityNotFoundException for missing source");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("SnmpDataCollectionSource not found for id: 999999"));
        }

        // --- SUCCESS: delete both
        dataCollectionConfPersistenceService.deleteSnmpDataCollectionResourceTypes(src.getId(), List.of(r1.getId(), r2.getId()));

        snmpCollectionResourceTypeDao.flush();
        snmpCollectionResourceTypeDao.clear();

        assertNull(snmpCollectionResourceTypeDao.get(r1.getId()));
        assertNull(snmpCollectionResourceTypeDao.get(r2.getId()));
    }

    @Test
    @Transactional
    public void testDeleteSnmpDataCollectionSystemDefsForSource_BadRequest_AndSuccess() throws Exception {
        // --- Setup: source + 2 system defs
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("delete.systemdef.combo.source");
        src.setVendor("opennms");
        src.setDescription("source for system def delete");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionSystemDef d1 = new SnmpCollectionSystemDef();
        d1.setCollectionSource(src);
        d1.setName("delete-me-systemdef-1");
        d1.setSysoid(".1.3.6.1.2.1.1");
        d1.setSysoidMask("255.255.255.0");
        d1.setIpAddresses("192.168.1.0");
        d1.setIpAddressMasks("255.255.255.0");
        d1.setMibGroupNames("MIB-GROUP-1");
        d1.setEnabled(true);

        SnmpCollectionSystemDef d2 = new SnmpCollectionSystemDef();
        d2.setCollectionSource(src);
        d2.setName("delete-me-systemdef-2");
        d2.setSysoid(".1.3.6.1.2.1.2");
        d2.setSysoidMask("255.255.255.0");
        d2.setIpAddresses("10.0.0.0");
        d2.setIpAddressMasks("255.0.0.0");
        d2.setMibGroupNames("MIB-GROUP-2");
        d2.setEnabled(true);

        snmpCollectionSystemDefDao.saveOrUpdate(d1);
        snmpCollectionSystemDefDao.saveOrUpdate(d2);
        snmpCollectionSystemDefDao.flush();

        // --- BAD_REQUEST/NOT_FOUND: missing source id
        try {
            dataCollectionConfPersistenceService.deleteSnmpDataCollectionSystemDefs(999999, List.of(d1.getId()));
            Assert.fail("Expected EntityNotFoundException for missing source");
        } catch (EntityNotFoundException e) {
            assertTrue(e.getMessage().contains("SnmpDataCollectionSource not found for id: 999999"));
        }

        // --- SUCCESS: delete both
        dataCollectionConfPersistenceService.deleteSnmpDataCollectionSystemDefs(src.getId(), List.of(d1.getId(), d2.getId()));

        snmpCollectionSystemDefDao.flush();
        snmpCollectionSystemDefDao.clear();

        assertNull(snmpCollectionSystemDefDao.get(d1.getId()));
        assertNull(snmpCollectionSystemDefDao.get(d2.getId()));
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpDataCollectionSources_success_enable() throws Exception {
        final var s1 = new SnmpCollectionSource();
        s1.setName("enable.test.snmp.1");
        s1.setVendor("v1");
        s1.setDescription("desc1");
        s1.setCreatedTime(new Date());
        s1.setEnabled(false);

        final var s2 = new SnmpCollectionSource();
        s2.setName("enable.test.snmp.2");
        s2.setVendor("v2");
        s2.setDescription("desc2");
        s2.setCreatedTime(new Date());
        s2.setEnabled(false);

        snmpCollectionSourceDao.saveOrUpdate(s1);
        snmpCollectionSourceDao.saveOrUpdate(s2);
        snmpCollectionSourceDao.flush();

        final var ids = Arrays.asList(s1.getId(), s2.getId());

        dataCollectionConfPersistenceService.enableDisableSnmpDataCollectionSources(true, ids);

        snmpCollectionSourceDao.flush();
        snmpCollectionSourceDao.clear();

        final var r1 = snmpCollectionSourceDao.get(s1.getId());
        final var r2 = snmpCollectionSourceDao.get(s2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpDataCollectionSources_success_disable() throws Exception {
        final var  s1 = new SnmpCollectionSource();
        s1.setName("disable.test.snmp.1");
        s1.setVendor("v1");
        s1.setDescription("desc1");
        s1.setCreatedTime(new Date());
        s1.setEnabled(true);

        final var s2 = new SnmpCollectionSource();
        s2.setName("disable.test.snmp.2");
        s2.setVendor("v2");
        s2.setDescription("desc2");
        s2.setCreatedTime(new Date());
        s2.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(s1);
        snmpCollectionSourceDao.saveOrUpdate(s2);
        snmpCollectionSourceDao.flush();

        final var ids = Arrays.asList(s1.getId(), s2.getId());

        dataCollectionConfPersistenceService.enableDisableSnmpDataCollectionSources(false, ids);

        snmpCollectionSourceDao.flush();
        snmpCollectionSourceDao.clear();

        final var  r1 = snmpCollectionSourceDao.get(s1.getId());
        final var  r2 = snmpCollectionSourceDao.get(s2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpMibGroups_success_enable() throws Exception {
        final var now = new Date();
        final var  source = new SnmpCollectionSource();
        source.setName("mibgroups.enable.source");
        source.setVendor("v1");
        source.setDescription("source for mib group enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        final var group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(source);
        group1.setName("if-mib-interfaces");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group1.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        group1.setEnabled(false);
        snmpCollectionMibGroupDao.saveOrUpdate(group1);
        snmpCollectionMibGroupDao.flush();

        final var group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(source);
        group2.setName("ip-mib");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group2.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        group2.setEnabled(false);

        snmpCollectionMibGroupDao.saveOrUpdate(group2);
        snmpCollectionMibGroupDao.flush();

        final var  sourceId = source.getId();
        final var  ids = Arrays.asList(group1.getId(), group2.getId());

        dataCollectionConfPersistenceService.enableDisableMibGroups(sourceId,true,ids);

        snmpCollectionMibGroupDao.flush();
        snmpCollectionMibGroupDao.clear();

        final var r1 = snmpCollectionMibGroupDao.get(group1.getId());
        final var r2 = snmpCollectionMibGroupDao.get(group2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpMibGroups_success_disable() throws Exception {
        final var now = new Date();
        final var source = new SnmpCollectionSource();
        source.setName("mibgroups.enable.source");
        source.setVendor("v1");
        source.setDescription("source for mib group enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();


        final var group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(source);
        group1.setName("if-mib-interfaces");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group1.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        group1.setEnabled(true);
        snmpCollectionMibGroupDao.saveOrUpdate(group1);
        snmpCollectionMibGroupDao.flush();

        final var group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(source);
        group2.setName("ip-mib");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group2.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        group2.setEnabled(true);
        snmpCollectionMibGroupDao.saveOrUpdate(group2);
        snmpCollectionMibGroupDao.flush();


        final var sourceId = source.getId();
        final var ids = Arrays.asList(group1.getId(), group2.getId());

        dataCollectionConfPersistenceService.enableDisableMibGroups(sourceId,false,ids);

        snmpCollectionMibGroupDao.flush();
        snmpCollectionMibGroupDao.clear();

        final var r1 = snmpCollectionMibGroupDao.get(group1.getId());
        final var  r2 = snmpCollectionMibGroupDao.get(group2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpResourceTypes_success_enable() throws Exception {
        final var now = new Date();

        final var source = new SnmpCollectionSource();
        source.setName("resourcetypes.enable.source");
        source.setVendor("v1");
        source.setDescription("source for resourceTypes enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // Resource type 1, matches filter "cpu"
        final var rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(source);
        rt1.setName("cpu-resource");
        rt1.setLabel("CPU Utilization");
        rt1.setResourceLabel("CPU Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(false);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2, matches filter "disk"
        final var rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(source);
        rt2.setName("disk-resource");
        rt2.setLabel("Disk Usage");
        rt2.setResourceLabel("Disk Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(false);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        final var  sourceId = source.getId();
        final var  ids = Arrays.asList(rt1.getId(), rt2.getId());

        dataCollectionConfPersistenceService.enableDisableResourceTypes(sourceId, true, ids);

        snmpCollectionResourceTypeDao.flush();
        snmpCollectionResourceTypeDao.clear();

        final var r1 = snmpCollectionResourceTypeDao.get(rt1.getId());
        final var  r2 = snmpCollectionResourceTypeDao.get(rt2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpResourceTypes_success_disable() throws Exception {
        final var now = new Date();

        final var source = new SnmpCollectionSource();
        source.setName("resourcetypes.disable.source");
        source.setVendor("v1");
        source.setDescription("source for resourceTypes disable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // Resource type 1, matches filter "cpu"
        final var rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(source);
        rt1.setName("cpu-resource");
        rt1.setLabel("CPU Utilization");
        rt1.setResourceLabel("CPU Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2, matches filter "disk"
        final var rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(source);
        rt2.setName("disk-resource");
        rt2.setLabel("Disk Usage");
        rt2.setResourceLabel("Disk Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        final var sourceId = source.getId();
        final var ids = Arrays.asList(rt1.getId(), rt2.getId());

        dataCollectionConfPersistenceService.enableDisableResourceTypes(sourceId, false, ids);

        snmpCollectionResourceTypeDao.flush();
        snmpCollectionResourceTypeDao.clear();

        final var r1 = snmpCollectionResourceTypeDao.get(rt1.getId());
        final var r2 = snmpCollectionResourceTypeDao.get(rt2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpSystemDefs_success_enable() throws Exception {
        final var now = new Date();

        final var source = new SnmpCollectionSource();
        source.setName("systemdefs.enable.source");
        source.setVendor("v1");
        source.setDescription("source for systemDefs enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // SystemDef 1, matches filter "LinuxSystem"
        final var def1 = new SnmpCollectionSystemDef();
        def1.setCollectionSource(source);
        def1.setName("LinuxSystem");
        def1.setSysoid(".1.3.6.1.2.1.1");
        def1.setSysoidMask("255.255.255.0");
        def1.setIpAddresses("192.168.1.0,10.0.0.1");
        def1.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def1.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        def1.setEnabled(false);
        snmpCollectionSystemDefDao.saveOrUpdate(def1);

        // SystemDef 2, matches filter "WindowsSystem"
        final var def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(source);
        def2.setName("WindowsSystem");
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        def2.setEnabled(false);
        snmpCollectionSystemDefDao.saveOrUpdate(def2);

        final var sourceId = source.getId();
        final var ids = Arrays.asList(def1.getId(), def2.getId());

        dataCollectionConfPersistenceService.enableDisableSystemDefs(sourceId, true, ids);

        snmpCollectionSystemDefDao.flush();
        snmpCollectionSystemDefDao.clear();


        final var r1 = snmpCollectionSystemDefDao.get(def1.getId());
        final var r2 = snmpCollectionSystemDefDao.get(def2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpSystemDefs_success_disable() throws Exception {
        final var now = new Date();

        final var source = new SnmpCollectionSource();
        source.setName("systemdefs.disable.source");
        source.setVendor("v1");
        source.setDescription("source for systemDefs disable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // SystemDef 1, matches filter "LinuxSystem"
        final var def1 = new SnmpCollectionSystemDef();
        def1.setCollectionSource(source);
        def1.setName("LinuxSystem");
        def1.setSysoid(".1.3.6.1.2.1.1");
        def1.setSysoidMask("255.255.255.0");
        def1.setIpAddresses("192.168.1.0,10.0.0.1");
        def1.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def1.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        def1.setEnabled(true);
        snmpCollectionSystemDefDao.saveOrUpdate(def1);

        // SystemDef 2, matches filter "WindowsSystem"
        final var def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(source);
        def2.setName("WindowsSystem");
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        def2.setEnabled(true);

        snmpCollectionSystemDefDao.saveOrUpdate(def2);

        final var sourceId = source.getId();
        final var ids = Arrays.asList(def1.getId(), def2.getId());

        dataCollectionConfPersistenceService.enableDisableSystemDefs(sourceId, false, ids);

        snmpCollectionSystemDefDao.flush();
        snmpCollectionSystemDefDao.clear();

        final var r1 = snmpCollectionSystemDefDao.get(def1.getId());
        final var r2 = snmpCollectionSystemDefDao.get(def2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }

    private static MibObj createMibObj(String oid, String instance, String alias, String type) {
        MibObj m = new MibObj();
        m.setOid(oid);
        m.setInstance(instance);
        m.setAlias(alias);
        m.setType(type);
        return m;
    }
}
