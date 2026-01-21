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

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.*;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.DataCollectionConfRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

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
public class DataCollectionConfRestServiceIT {

    private static final String FILENAME = "dell.xml";
    private static final String RESOURCE_PATH = "/DATACOLLECTION/";
    private SecurityContext securityContext;

    @Autowired
    private DataCollectionConfRestApi dataCollectionConfRestApi;

    @Autowired
    private SnmpCollectionSourceDao snmpCollectionSourceDao;

    @Autowired
    private SnmpCollectionResourceTypeDao snmpCollectionResourceTypeDao;

    @Autowired
    private SnmpCollectionMibGroupDao snmpCollectionMibGroupDao;

    @Autowired
    private SnmpCollectionSystemDefDao snmpCollectionSystemDefDao;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("integration-user");

        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
    }

    @Test
    public void testUploadSnmpDataCollectionConfFiles_Success() throws Exception {
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(attachments, securityContext);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertEquals("Should be one successful upload", 1, success.size());
        assertEquals("Uploaded file key should match", "dell", success.get(0).get("file"));
        assertTrue("Error list should be empty", errors.isEmpty());
    }

    @Test
    public void testEmptyAttachments_ShouldReturnEmptyLists() throws Exception {
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(Collections.emptyList(), securityContext);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        assertTrue("Success list should be empty when no attachments", ((List<?>) entity.get("success")).isEmpty());
        assertTrue("Errors list should be empty when no attachments", ((List<?>) entity.get("errors")).isEmpty());
    }

    @Test
    public void testNullSecurityContext_ShouldUseUnknownUser() throws Exception {
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(attachments, null);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());
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
        Response resp = dataCollectionConfRestApi.filterSnmpCollectionSources("opennms.test.snmp", "name", "asc", 0, 0, 10, securityContext);
        Map<String, Object> respMap = (Map<String, Object>) resp.getEntity();
        Assert.assertEquals(1, respMap.get("totalRecords"));
        List<SnmpCollectionSourceDto> list1 = (List<SnmpCollectionSourceDto>) respMap.get("snmpCollectionSourceList");
        Assert.assertEquals(1, list1.size());

        // 2. Partial filter, ascending by name
        Response result = dataCollectionConfRestApi.filterSnmpCollectionSources("test.snmp", "name", "asc", 0, 0, 10, securityContext);
        Map<String, Object> map2 = (Map<String, Object>) result.getEntity();
        Assert.assertEquals(2, map2.get("totalRecords"));
        List<SnmpCollectionSourceDto> list2 = (List<SnmpCollectionSourceDto>) map2.get("snmpCollectionSourceList");
        Assert.assertEquals(2, list2.size());

        // 3. Partial filter, descending by name
        Response resultDesc = dataCollectionConfRestApi.filterSnmpCollectionSources("test.snmp", "name", "desc", 0, 0, 10, securityContext);
        Map<String, Object> map3 = (Map<String, Object>) resultDesc.getEntity();
        Assert.assertEquals(2, map3.get("totalRecords"));
        List<SnmpCollectionSourceDto> list3 = (List<SnmpCollectionSourceDto>) map3.get("snmpCollectionSourceList");
        Assert.assertEquals(2, list3.size());

        // 4. Filter by vendor (case-insensitive)
        Response vendorResp = dataCollectionConfRestApi.filterSnmpCollectionSources("CISCO", "name", "asc", 0, 0, 10, securityContext);
        Map<String, Object> map4 = (Map<String, Object>) vendorResp.getEntity();
        Assert.assertEquals(1, map4.get("totalRecords"));
        List<SnmpCollectionSourceDto> list4 = (List<SnmpCollectionSourceDto>) map4.get("snmpCollectionSourceList");
        Assert.assertEquals(1, list4.size());
        Assert.assertEquals("cisco.test.snmp", ((SnmpCollectionSourceDto) list4.get(0)).getName());

        // 5. Pagination (only second record returned)
        Response pagedResp = dataCollectionConfRestApi.filterSnmpCollectionSources("test.snmp", "name", "asc", 0, 1, 1, securityContext);
        Map<String, Object> map5 = (Map<String, Object>) pagedResp.getEntity();
        Assert.assertEquals(2, map5.get("totalRecords")); // total filtered, not paged
        List<SnmpCollectionSourceDto> list5 = (List<SnmpCollectionSourceDto>) map5.get("snmpCollectionSourceList");
        Assert.assertEquals(1, list5.size());
        Assert.assertEquals("opennms.test.snmp", ((SnmpCollectionSourceDto) list5.get(0)).getName());

        // 6. Filter by vendor substring
        Response vendorSubstrResp = dataCollectionConfRestApi.filterSnmpCollectionSources("open", "vendor", "asc", 0, 0, 10, securityContext);
        Map<String, Object> map6 = (Map<String, Object>) vendorSubstrResp.getEntity();
        Assert.assertEquals(1, map6.get("totalRecords"));
        List<?> list6 = (List<?>) map6.get("snmpCollectionSourceList");
        Assert.assertEquals(1, list6.size());
        Assert.assertEquals("opennms.test.snmp", ((SnmpCollectionSourceDto) list6.get(0)).getName());

    }

    @Test
    @Transactional
    public void testFilterDataCollectionMibGroupByCollectionSourceId() {
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
        Response resp1 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "if-mib-interfaces", "name", "ASC", 0, 0, 10, securityContext);
        Map<String, Object> map1 = (Map<String, Object>) resp1.getEntity();
        Assert.assertEquals(1, map1.get("totalRecords"));
        List<?> list1 = (List<?>) map1.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, list1.size());
        Assert.assertEquals("if-mib-interfaces", ((SnmpCollectionMibGroupDto) list1.get(0)).getName());

        // 2. Partial filter ("mib"), ascending by name
        Response resp2 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "mib", "name", "ASC", 0, 0, 10, securityContext);
        Map<String, Object> map2 = (Map<String, Object>) resp2.getEntity();
        Assert.assertEquals(2, map2.get("totalRecords"));
        List<?> list2 = (List<?>) map2.get("dataCollectionMibGroupList");
        Assert.assertEquals(2, list2.size());
        // asc: "if-mib-interfaces" comes first
        Assert.assertEquals("if-mib-interfaces", ((SnmpCollectionMibGroupDto) list2.get(0)).getName());
        Assert.assertEquals("ip-mib", ((SnmpCollectionMibGroupDto) list2.get(1)).getName());

        // 3. Partial filter, descending by name
        Response resp3 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "mib", "name", "DESC", 0, 0, 10, securityContext);
        Map<String, Object> map3 = (Map<String, Object>) resp3.getEntity();
        Assert.assertEquals(2, map3.get("totalRecords"));
        List<?> list3 = (List<?>) map3.get("dataCollectionMibGroupList");
        Assert.assertEquals(2, list3.size());
        // desc: "ip-mib" comes first
        Assert.assertEquals("ip-mib", ((SnmpCollectionMibGroupDto) list3.get(0)).getName());
        Assert.assertEquals("if-mib-interfaces", ((SnmpCollectionMibGroupDto) list3.get(1)).getName());

        // 4. Filter by ifType substring, ascending
        Response resp4 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "Ethernet", "ifType", "ASC", 0, 0, 10, securityContext);
        Map<String, Object> map4 = (Map<String, Object>) resp4.getEntity();
        Assert.assertEquals(1, map4.get("totalRecords"));
        List<?> list4 = (List<?>) map4.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, list4.size());
        Assert.assertEquals("if-mib-interfaces", ((SnmpCollectionMibGroupDto) list4.get(0)).getName());

        // 5. Filter by ifType substring, descending
        Response resp5 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "Loopback", "ifType", "DESC", 0, 0, 10, securityContext);
        Map<String, Object> map5 = (Map<String, Object>) resp5.getEntity();
        Assert.assertEquals(1, map5.get("totalRecords"));
        List<?> list5 = (List<?>) map5.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, list5.size());
        Assert.assertEquals("ip-mib", ((SnmpCollectionMibGroupDto) list5.get(0)).getName());

        // 6. Case-insensitive filter (should match "ip-MIB")
        Response resp6 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "IP-MIB", "name", "ASC", 0, 0, 10, securityContext);
        Map<String, Object> map6 = (Map<String, Object>) resp6.getEntity();
        Assert.assertEquals(1, map6.get("totalRecords"));
        List<?> list6 = (List<?>) map6.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, list6.size());
        Assert.assertEquals("ip-mib", ((SnmpCollectionMibGroupDto) list6.get(0)).getName());

        // 7. Pagination - only second result returned
        Response resp7 = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "mib", "name", "ASC", 0, 1, 1, securityContext);
        Map<String, Object> map7 = (Map<String, Object>) resp7.getEntity();
        Assert.assertEquals(2, map7.get("totalRecords"));
        List<?> pagedList = (List<?>) map7.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, pagedList.size());
        Assert.assertEquals("ip-mib", ((SnmpCollectionMibGroupDto) pagedList.get(0)).getName());


    }

    @Test
    @Transactional
    public void testFilterDataCollectionResourceTypeByCollectionSourceId() {
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
        Response rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), "cpu-resource", "name", "ASC", 0, 0, 10, securityContext);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) rs.getEntity();
        Number totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(1, totalRecords.intValue());
        List<?> dtoList = (List<SnmpCollectionResourceTypeDto>) result.get("dataCollectionResourceTypeList");
        Assert.assertEquals(1, dtoList.size());

        // 2. Partial filter ("resource"), ascending by name
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), "resource", "name", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());

        // 3. Partial filter, descending by name
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), "resource", "name", "DESC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());

        // 4. Filter by label substring, ascending
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), "Disk", "label", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(1, totalRecords.intValue());

        // 5. Filter by label substring (case-insensitive), descending
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), "cpu utilization", "label", "DESC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(1, totalRecords.intValue());

        // 6. Pagination: only second returned
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), "resource", "name", "ASC", 0, 1, 1, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());
        dtoList = (List<SnmpCollectionResourceTypeDto>) result.get("dataCollectionResourceTypeList");
        Assert.assertEquals(1, dtoList.size());


        // 8. Null filter (should return all for group), ascending by label
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), null, "label", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());
        dtoList = (List<SnmpCollectionResourceTypeDto>) result.get("dataCollectionResourceTypeList");
        Assert.assertEquals(2, dtoList.size());


        // 9. Invalid sortBy field defaults to name ascending
        rs = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(src.getId(), null, "invalidSort", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());


    }
    @Test
    @Transactional
    public void testFilterDataCollectionSystemDefByCollectionSourceId() {
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
        def1.setName("LinuxSystem");
        def1.setSysoid(".1.3.6.1.2.1.1");
        def1.setSysoidMask("255.255.255.0");
        def1.setIpAddresses("192.168.1.0,10.0.0.1");
        def1.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def1.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        snmpCollectionSystemDefDao.saveOrUpdate(def1);

        // SystemDef 2, matches filter "WindowsSystem"
        SnmpCollectionSystemDef def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(src);
        def2.setName("WindowsSystem");
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        snmpCollectionSystemDefDao.saveOrUpdate(def2);

        snmpCollectionSystemDefDao.flush();

        // 1. Exact filter by name ASC
        Response rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), "LinuxSystem", "name", "ASC", 0, 0, 10, securityContext);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) rs.getEntity();
        Number totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(1, totalRecords.intValue());
        List<?> dtoList = (List<SnmpCollectionSystemDefDto>) result.get("dataCollectionSystemDefsList");
        Assert.assertEquals(1, dtoList.size());

        // 2. Partial filter ("System"), ascending by name
        rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), "System", "name", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());

        // 3. Partial filter, descending by name
        rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), "System", "name", "DESC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());

        // 4. Case-insensitive filter
        rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), "LINUXSYSTEM", "name", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(1, totalRecords.intValue());

        // 5. Pagination - only second returned
        rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), "System", "name", "ASC", 0, 1, 1, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());
        dtoList = (List<SnmpCollectionSystemDefDto>) result.get("dataCollectionSystemDefsList");
        Assert.assertEquals(1, dtoList.size());


        // 7. Null filter - should return all for group, ascending
        rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), null, "name", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());
        dtoList = (List<SnmpCollectionSystemDefDto>) result.get("dataCollectionSystemDefsList");
        Assert.assertEquals(2, dtoList.size());


        // 8. Invalid sortBy field defaults to name ascending
        rs = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(src.getId(), null, "invalidSort", "ASC", 0, 0, 10, securityContext);
        result = (Map<String, Object>) rs.getEntity();
        totalRecords = (Number) result.get("totalRecords");
        Assert.assertEquals(2, totalRecords.intValue());
        dtoList = (List<SnmpCollectionSystemDefDto>) result.get("dataCollectionSystemDefsList");

    }

    @Test
    @Transactional
    public void testGetSnmpDataCollectionSourceById() {
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
        Response response = dataCollectionConfRestApi.getSnmpDataCollectionSourceById(source2.getId(), securityContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        // If using DTOs, cast as such:
        SnmpCollectionSourceDto ciscoCollectionSource = (SnmpCollectionSourceDto) response.getEntity();
        Assert.assertNotNull("Should retrieve Cisco collection source by id", ciscoCollectionSource);
        Assert.assertEquals("Names should match", "cisco.test.snmp", ciscoCollectionSource.getName());
        Assert.assertEquals("Vendors should match", "cisco", ciscoCollectionSource.getVendor());
        Assert.assertEquals("Descriptions should match", "Cisco SNMP Data Source", ciscoCollectionSource.getDescription());

        // Act & Assert: source1
        response = dataCollectionConfRestApi.getSnmpDataCollectionSourceById(source1.getId(), securityContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        SnmpCollectionSourceDto opennmsCollectionSource = (SnmpCollectionSourceDto) response.getEntity();
        Assert.assertNotNull("Should retrieve OpenNMS collection source by id", opennmsCollectionSource);
        Assert.assertEquals("Names should match", "opennms.test.snmp", opennmsCollectionSource.getName());
        Assert.assertEquals("Vendors should match", "opennms", opennmsCollectionSource.getVendor());
        Assert.assertEquals("Descriptions should match", "Open Network Monitoring System SNMP", opennmsCollectionSource.getDescription());
    }

    /** Helper to create a mocked Attachment for a given file */
    private Attachment createMockedAttachment(String name) {
        InputStream is = getClass().getResourceAsStream(RESOURCE_PATH + name);
        assertNotNull("Test resource not found: " + name, is);

        Attachment att = mock(Attachment.class);
        ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(name);
        when(att.getContentDisposition()).thenReturn(cd);
        when(att.getObject(InputStream.class)).thenReturn(is);
        return att;
    }
}
