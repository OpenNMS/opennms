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
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.netmgt.model.SnmpCollectionSystemDef;
import org.opennms.netmgt.model.SnmpCollectionResourceType;
import org.opennms.netmgt.model.SnmpCollectionMibGroup;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.netmgt.model.SnmpCollectionSourceDto;
import org.opennms.netmgt.model.SnmpCollectionMibGroupDto;
import org.opennms.netmgt.model.SnmpCollectionSystemDefDto;
import org.opennms.netmgt.model.SnmpCollectionResourceTypeDto;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.DataCollectionConfRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dataCollectionConfig-test.xml",
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

    @Autowired
    private SessionFactory sessionFactory;

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
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                attachments, List.of(profileNamesPart("default")), securityContext);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> success = (List<Map<String, Object>>) entity.get("success");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) entity.get("errors");

        assertEquals("Should be one successful upload", 1, success.size());
        assertEquals("Uploaded file key should match", "Dell", success.get(0).get("file"));
        assertTrue("Error list should be empty", errors.isEmpty());
    }

    @Test
    public void testEmptyAttachments_ShouldReturnEmptyLists() throws Exception {
        // No attachments at all → the profileNames guard does NOT fire because
        // there are no parsed sources to attach.
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                Collections.emptyList(), null, securityContext);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = (Map<String, Object>) resp.getEntity();
        assertTrue("Success list should be empty when no attachments", ((List<?>) entity.get("success")).isEmpty());
        assertTrue("Errors list should be empty when no attachments", ((List<?>) entity.get("errors")).isEmpty());
    }

    @Test
    public void testNullSecurityContext_ShouldUseUnknownUser() throws Exception {
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                attachments, List.of(profileNamesPart("default")), null);

        assertEquals("Expected OK status", Response.Status.OK.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testSourceUpload_RequiresProfileNames() throws Exception {
        // Source-only uploads with at least one *new* source must include
        // profileNames; the server returns 400 to prevent orphaned sources.
        // (The fresh DB has no source named "Dell" yet, so this is new.)
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));
        Response resp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                attachments, null, securityContext);

        assertEquals("Expected 400 when source-only upload of a new source omits profileNames",
                Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Test
    public void testPureUpdateUpload_AllowsEmptyProfileNames() throws Exception {
        // Once a source already exists in the DB, re-uploading the same XML
        // is an update — the server should accept it without profileNames
        // since the source's existing profile memberships are preserved.
        List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));

        // First upload: new source, requires profileNames
        Response createResp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                attachments, List.of(profileNamesPart("default")), securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), createResp.getStatus());

        // Second upload: same source name, no profileNames → 200 (pure update)
        List<Attachment> reuploadAttachments = List.of(createMockedAttachment(FILENAME));
        Response updateResp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                reuploadAttachments, null, securityContext);
        assertEquals("Pure update should be allowed without profileNames",
                Response.Status.OK.getStatusCode(), updateResp.getStatus());
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

    @Test
    @Transactional
    public void testGetSnmpDataCollectionResourceTypeNames() throws Exception {
        // Setup source entity
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("resource-type-source");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Source for Resource Type Names");
        snmpCollectionSourceDao.saveOrUpdate(src);

        // Resource type 1
        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(src);
        rt1.setName("resource-type-one");
        rt1.setLabel("Resource Type One");
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2
        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(src);
        rt2.setName("resource-type-two");
        rt2.setLabel("Resource Type Two");
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        // Act
        Response response = dataCollectionConfRestApi.getDataCollectionResourceTypeNames(securityContext);

        // Assert
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<String> resourceTypeNames = (List<String>) response.getEntity();
        Assert.assertEquals(4, resourceTypeNames.size());
        Assert.assertEquals("0", resourceTypeNames.get(0));
        Assert.assertEquals("ifIndex", resourceTypeNames.get(1));
        Assert.assertTrue(resourceTypeNames.contains("resource-type-one"));
        Assert.assertTrue(resourceTypeNames.contains("resource-type-two"));
    }

    @Test
    @Transactional
    public void testGetSnmpDataCollectionMibGroupNames() throws Exception {

        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("mib-group-source");
        src.setVendor("opennms");
        src.setCreatedTime(new Date());
        src.setDescription("Source for MIB Group Names");
        snmpCollectionSourceDao.saveOrUpdate(src);

        SnmpCollectionMibGroup mg1 = new SnmpCollectionMibGroup();
        mg1.setCollectionSource(src);
        mg1.setName("mib-group-one");
        mg1.setMibObjects("ifIndex,ifDescr");
        mg1.setMibGroupNames("IF-MIB::ifEntry");
        snmpCollectionMibGroupDao.saveOrUpdate(mg1);

        SnmpCollectionMibGroup mg2 = new SnmpCollectionMibGroup();
        mg2.setCollectionSource(src);
        mg2.setName("mib-group-two");
        mg2.setMibObjects("sysUpTime,sysDescr");
        mg2.setMibGroupNames("SNMPv2-MIB::system");
        snmpCollectionMibGroupDao.saveOrUpdate(mg2);
        snmpCollectionMibGroupDao.flush();

        Response response = dataCollectionConfRestApi.getDataCollectionMibGroupNames(securityContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<String> mibGroupNames = (List<String>) response.getEntity();
        Assert.assertTrue(mibGroupNames.contains("mib-group-one"));
        Assert.assertTrue(mibGroupNames.contains("mib-group-two"));
    }

    @Test
    @Transactional
    public void testAddMibGroupToSnmpCollectionSources() throws Exception {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source.add");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for addMibGroup tests");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionMibGroupDto reqBad1 = new SnmpCollectionMibGroupDto();
        reqBad1.setName("if-mib-interfaces");
        Response respBad1 = dataCollectionConfRestApi.addMibGroupToSnmpCollectionSources(
                null, reqBad1, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad1.getStatus());
        Assert.assertEquals(
                "Invalid snmpCollectionSourceId: null. It must be a positive integer.",
                respBad1.getEntity()
        );
        SnmpCollectionMibGroupDto addReq = new SnmpCollectionMibGroupDto();
        addReq.setName("if-mib-interfaces");
        addReq.setIfType("Ethernet");
        addReq.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        addReq.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        addReq.setMibObjProperties("{\"property\":\"value\"}");
        addReq.setEnabled(true);

        Response addResp = dataCollectionConfRestApi.addMibGroupToSnmpCollectionSources(
                src.getId(), addReq, securityContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), addResp.getStatus());
        Assert.assertNotNull(addResp.getEntity());

        Integer createdId;
        if (addResp.getEntity() instanceof Integer) {
            createdId = (Integer) addResp.getEntity();
        } else if (addResp.getEntity() instanceof Long) {
            createdId = ((Long) addResp.getEntity()).intValue();
        } else if (addResp.getEntity() instanceof String) {
            createdId = Integer.valueOf((String) addResp.getEntity());
        } else {
            throw new AssertionError("Unexpected ID type: " + addResp.getEntity().getClass());
        }
        Assert.assertTrue(createdId > 0);
        snmpCollectionMibGroupDao.flush();

        Response filterResp = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "if-mib-interfaces", "name", "ASC", 0, 0, 10, securityContext);

        Map<String, Object> filterMap = (Map<String, Object>) filterResp.getEntity();
        Assert.assertEquals(1, filterMap.get("totalRecords"));
        List<?> resultList = (List<?>) filterMap.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, resultList.size());

        SnmpCollectionMibGroupDto created = (SnmpCollectionMibGroupDto) resultList.get(0);
        Assert.assertEquals("if-mib-interfaces", created.getName());
        Assert.assertEquals("Ethernet", created.getIfType());
        Assert.assertEquals("IF-MIB::ifEntry,IF-MIB::ifXEntry", created.getMibGroupNames());
        Assert.assertEquals("ifIndex,ifDescr,ifOperStatus", created.getMibObjects());
        Assert.assertEquals("{\"property\":\"value\"}", created.getMibObjProperties());

    }

    @Test
    @Transactional
    public void testAddResourceTypeToSnmpCollectionSources() throws Exception {

        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source.add.resourceType");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for addResourceType tests");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionResourceTypeDto reqBad1 = new SnmpCollectionResourceTypeDto();
        reqBad1.setName("interface");

        Response respBad1 = dataCollectionConfRestApi.addResourceTypeToSnmpCollectionSources(
                null, reqBad1, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad1.getStatus());
        Assert.assertEquals(
                "Invalid snmpCollectionSourceId: null. It must be a positive integer.",
                respBad1.getEntity()
        );

        SnmpCollectionResourceTypeDto reqBad2 = new SnmpCollectionResourceTypeDto();
        reqBad2.setName("interface");

        Response respBad2 = dataCollectionConfRestApi.addResourceTypeToSnmpCollectionSources(
                0, reqBad2, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad2.getStatus());
        Assert.assertEquals(
                "Invalid snmpCollectionSourceId: 0. It must be a positive integer.",
                respBad2.getEntity()
        );

        SnmpCollectionResourceTypeDto addReq = new SnmpCollectionResourceTypeDto();
        addReq.setName("interface");
        addReq.setLabel("Interface");
        addReq.setResourceLabel("Interface ${ifDescr}");
        addReq.setPersistenceSelectorStrategy("default");
        addReq.setEnabled(true);

        Response addResp = dataCollectionConfRestApi.addResourceTypeToSnmpCollectionSources(
                src.getId(), addReq, securityContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), addResp.getStatus());
        Assert.assertNotNull(addResp.getEntity());

        Integer createdId;
        if (addResp.getEntity() instanceof Integer) {
            createdId = (Integer) addResp.getEntity();
        } else if (addResp.getEntity() instanceof Long) {
            createdId = ((Long) addResp.getEntity()).intValue();
        } else if (addResp.getEntity() instanceof String) {
            createdId = Integer.valueOf((String) addResp.getEntity());
        } else {
            throw new AssertionError("Unexpected ID type: " + addResp.getEntity().getClass());
        }
        Assert.assertTrue(createdId > 0);

        snmpCollectionResourceTypeDao.flush();

        Response filterResp = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(
                src.getId(), "interface", "name", "ASC", 0, 0, 10, securityContext);

        Map<String, Object> filterMap = (Map<String, Object>) filterResp.getEntity();
        Assert.assertEquals(1, filterMap.get("totalRecords"));
        List<?> resultList = (List<?>) filterMap.get("dataCollectionResourceTypeList");
        Assert.assertEquals(1, resultList.size());

        SnmpCollectionResourceTypeDto created = (SnmpCollectionResourceTypeDto) resultList.get(0);
        Assert.assertEquals("interface", created.getName());
        Assert.assertEquals("Interface", created.getLabel());
        Assert.assertEquals("Interface ${ifDescr}", created.getResourceLabel());
    }

    @Test
    @Transactional
    public void testAddSystemDefToSnmpCollectionSources() throws Exception {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source.add.systemDef");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for addSystemDef tests");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionSystemDefDto reqBad1 = new SnmpCollectionSystemDefDto();
        reqBad1.setName("systemdef-1");

        Response respBad1 = dataCollectionConfRestApi.addSystemDefToSnmpCollectionSources(
                null, reqBad1, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad1.getStatus());
        Assert.assertEquals(
                "Invalid snmpCollectionSourceId: null. It must be a positive integer.",
                respBad1.getEntity()
        );

        SnmpCollectionSystemDefDto addReq = new SnmpCollectionSystemDefDto();
        addReq.setName("systemdef-1");
        addReq.setSysoidMask(".1.3.6.1.4.1");
        addReq.setSysoid(".1.3.6.1.4.1.8072.3.2.10");
        addReq.setEnabled(true);
        addReq.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");

        Response addResp = dataCollectionConfRestApi.addSystemDefToSnmpCollectionSources(
                src.getId(), addReq, securityContext);

        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), addResp.getStatus());
        Assert.assertNotNull(addResp.getEntity());

        Integer createdId;
        if (addResp.getEntity() instanceof Integer) {
            createdId = (Integer) addResp.getEntity();
        } else if (addResp.getEntity() instanceof Long) {
            createdId = ((Long) addResp.getEntity()).intValue();
        } else if (addResp.getEntity() instanceof String) {
            createdId = Integer.valueOf((String) addResp.getEntity());
        } else {
            throw new AssertionError("Unexpected ID type: " + addResp.getEntity().getClass());
        }
        Assert.assertTrue(createdId > 0);

        snmpCollectionSystemDefDao.flush();

        Response filterResp = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(
                src.getId(), "systemdef-1", "name", "ASC", 0, 0, 10, securityContext);

        Map<String, Object> filterMap = (Map<String, Object>) filterResp.getEntity();
        Assert.assertEquals(1, filterMap.get("totalRecords"));
        List<?> resultList = (List<?>) filterMap.get("dataCollectionSystemDefsList");
        Assert.assertEquals(1, resultList.size());

        SnmpCollectionSystemDefDto created = (SnmpCollectionSystemDefDto) resultList.get(0);
        Assert.assertEquals("systemdef-1", created.getName());
        Assert.assertEquals(".1.3.6.1.4.1", created.getSysoidMask());
        Assert.assertEquals(".1.3.6.1.4.1.8072.3.2.10", created.getSysoid());
    }

    @Test
    @Transactional
    public void testUpdateMibGroupInSnmpCollectionSources() throws Exception {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source.update.mibgroup");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for update mib group tests");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionMibGroup group = new SnmpCollectionMibGroup();
        group.setCollectionSource(src);
        group.setName("if-mib-interfaces");
        group.setIfType("Ethernet");
        group.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group.setMibObjProperties("{\"property\":\"value\"}");
        group.setEnabled(true);
        snmpCollectionMibGroupDao.saveOrUpdate(group);
        snmpCollectionMibGroupDao.flush();

        Response respBad = dataCollectionConfRestApi.updateMibGroupInSnmpCollectionSources(
                src.getId(), group.getId(), null, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad.getStatus());
        Assert.assertEquals("Request body cannot be null", respBad.getEntity());

        SnmpCollectionMibGroupDto updateReq = new SnmpCollectionMibGroupDto();
        updateReq.setName("if-mib-interfaces-updated");
        updateReq.setIfType("EthernetUpdated");
        updateReq.setMibGroupNames("IF-MIB::ifEntry");
        updateReq.setMibObjects("ifIndex,ifDescr");
        updateReq.setEnabled(true);
        updateReq.setMibObjProperties("{\"updated\":\"true\"}");

        Response respOk = dataCollectionConfRestApi.updateMibGroupInSnmpCollectionSources(
                src.getId(), group.getId(), updateReq, securityContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), respOk.getStatus());
        Assert.assertEquals("MibGroup updated successfully.", respOk.getEntity());
        snmpCollectionMibGroupDao.flush();
        Response filterResp = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "if-mib-interfaces-updated", "name", "ASC", 0, 0, 10, securityContext);

        Map<String, Object> filterMap = (Map<String, Object>) filterResp.getEntity();
        Assert.assertEquals(1, filterMap.get("totalRecords"));
        List<?> list = (List<?>) filterMap.get("dataCollectionMibGroupList");
        Assert.assertEquals(1, list.size());

        SnmpCollectionMibGroupDto updated = (SnmpCollectionMibGroupDto) list.get(0);
        Assert.assertEquals("if-mib-interfaces-updated", updated.getName());
        Assert.assertEquals("EthernetUpdated", updated.getIfType());
        Assert.assertEquals("IF-MIB::ifEntry", updated.getMibGroupNames());
        Assert.assertEquals("ifIndex,ifDescr", updated.getMibObjects());
        Assert.assertEquals("{\"updated\":\"true\"}", updated.getMibObjProperties());
    }

    @Test
    @Transactional
    public void testUpdateResourceTypeInSnmpCollectionSources() throws Exception {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source.update.resourcetype");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for update resource type tests");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionResourceType rt = new SnmpCollectionResourceType();
        rt.setCollectionSource(src);
        rt.setName("interface");
        rt.setLabel("Interface");
        rt.setResourceLabel("Interface ${ifDescr}");
        rt.setPersistenceSelectorStrategy("default");
        rt.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt);
        snmpCollectionResourceTypeDao.flush();

        Response respBad = dataCollectionConfRestApi.updateResourceTypeInSnmpCollectionSources(
                src.getId(), rt.getId(), null, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad.getStatus());
        Assert.assertEquals("Request body cannot be null", respBad.getEntity());
        SnmpCollectionResourceTypeDto updateReq = new SnmpCollectionResourceTypeDto();
        updateReq.setName("interface-updated");
        updateReq.setLabel("Interface Updated");
        updateReq.setResourceLabel("Interface Updated ${ifDescr}");
        updateReq.setPersistenceSelectorStrategy("default");
        updateReq.setEnabled(true);

        Response respOk = dataCollectionConfRestApi.updateResourceTypeInSnmpCollectionSources(
                src.getId(), rt.getId(), updateReq, securityContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), respOk.getStatus());
        Assert.assertEquals("ResourceType updated successfully.", respOk.getEntity());

        snmpCollectionResourceTypeDao.flush();
        Response filterResp = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(
                src.getId(), "interface-updated", "name", "ASC", 0, 0, 10, securityContext);

        Map<String, Object> filterMap = (Map<String, Object>) filterResp.getEntity();
        Assert.assertEquals(1, filterMap.get("totalRecords"));
        List<?> list = (List<?>) filterMap.get("dataCollectionResourceTypeList");
        Assert.assertEquals(1, list.size());

        SnmpCollectionResourceTypeDto updated = (SnmpCollectionResourceTypeDto) list.get(0);
        Assert.assertEquals("interface-updated", updated.getName());
        Assert.assertEquals("Interface Updated", updated.getLabel());
        Assert.assertEquals("Interface Updated ${ifDescr}", updated.getResourceLabel());
    }

    @Test
    @Transactional
    public void testUpdateSystemDefInSnmpCollectionSources() throws Exception {
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("group.snmp.source.update.systemdef");
        src.setVendor("opennms");
        src.setDescription("SNMP Source for update systemDef tests");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionSystemDef sys = new SnmpCollectionSystemDef();
        sys.setCollectionSource(src);
        sys.setName("systemdef-1");
        sys.setSysoidMask(".1.3.6.1.4.1");
        sys.setSysoid(".1.3.6.1.4.1.8072.3.2.10");
        sys.setEnabled(true);
        sys.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        snmpCollectionSystemDefDao.saveOrUpdate(sys);
        snmpCollectionSystemDefDao.flush();

        Response respBad = dataCollectionConfRestApi.updateSystemDefInSnmpCollectionSources(
                src.getId(), sys.getId(), null, securityContext);

        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), respBad.getStatus());
        Assert.assertEquals("Request body cannot be null", respBad.getEntity());

        SnmpCollectionSystemDefDto updateReq = new SnmpCollectionSystemDefDto();
        updateReq.setName("systemdef-1-updated");
        updateReq.setSysoidMask(".1.3.6.1.4.1.9999");
        updateReq.setSysoid(".1.3.6.1.4.1.9999.1");
        updateReq.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        updateReq.setEnabled(true);

        Response respOk = dataCollectionConfRestApi.updateSystemDefInSnmpCollectionSources(
                src.getId(), sys.getId(), updateReq, securityContext);

        Assert.assertEquals(Response.Status.OK.getStatusCode(), respOk.getStatus());
        Assert.assertEquals("SystemDef updated successfully.", respOk.getEntity());

        snmpCollectionSystemDefDao.flush();

        Response filterResp = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(
                src.getId(), "systemdef-1-updated", "name", "ASC", 0, 0, 10, securityContext);

        Map<String, Object> filterMap = (Map<String, Object>) filterResp.getEntity();
        Assert.assertEquals(1, filterMap.get("totalRecords"));
        List<?> list = (List<?>) filterMap.get("dataCollectionSystemDefsList");
        Assert.assertEquals(1, list.size());

        SnmpCollectionSystemDefDto updated = (SnmpCollectionSystemDefDto) list.get(0);
        Assert.assertEquals("systemdef-1-updated", updated.getName());
        Assert.assertEquals(".1.3.6.1.4.1.9999", updated.getSysoidMask());
        Assert.assertEquals(".1.3.6.1.4.1.9999.1", updated.getSysoid());

    }
    @Test
    @Transactional
    public void testDownloadDataCollectionXmlBySourceId() throws Exception {
        final String format = "xml";

        final String xmlResourcePath = RESOURCE_PATH + FILENAME;

        final List<Attachment> attachments = List.of(createMockedAttachment(FILENAME));

        Response uploadResp = dataCollectionConfRestApi.uploadSnmpDataCollectionConfFiles(
                attachments, List.of(profileNamesPart("default")), securityContext);
        assertEquals(Response.Status.OK.getStatusCode(), uploadResp.getStatus());

        SnmpCollectionSource dataCollectionSource = snmpCollectionSourceDao.findByName("Dell");
        assertNotNull("DataCollectionSource should exist after upload", dataCollectionSource);

        Response response = dataCollectionConfRestApi.downloadSnmpDataCollectionById(
                dataCollectionSource.getId(), format, securityContext);

        assertNotNull("Response should not be null", response);
        assertEquals("Expected HTTP 200 OK", 200, response.getStatus());
        assertNotNull("MediaType should not be null", response.getMediaType());
        assertTrue("Expected application/xml but was: " + response.getMediaType(),
                response.getMediaType().isCompatible(MediaType.APPLICATION_XML_TYPE));

        final Object entity = response.getEntity();
        assertNotNull("Response entity should not be null", entity);

        String downloadedXml = stripUtf8Bom(readEntityAsString(entity)).trim();
        assertLooksLikeXml("downloadedXml", downloadedXml);

        final String uploadedXml;
        try (InputStream is2 = getClass().getResourceAsStream(xmlResourcePath)) {
            assertNotNull("Resource not found: " + xmlResourcePath, is2);
            uploadedXml = stripUtf8Bom(new String(is2.readAllBytes(), StandardCharsets.UTF_8)).trim();
        }
        assertLooksLikeXml("uploadedXml", uploadedXml);

        final DatacollectionGroup uploaded =
                JaxbUtils.unmarshal(DatacollectionGroup.class, new StringReader(uploadedXml));
        final DatacollectionGroup downloaded =
                JaxbUtils.unmarshal(DatacollectionGroup.class, new StringReader(downloadedXml));

        final var uploadedGroups = new ArrayList<>(uploaded.getGroups());
        final var downloadedGroups = new ArrayList<>(downloaded.getGroups());
        assertEquals(uploadedGroups.size(), downloadedGroups.size());
        for (int i = 0; i < uploadedGroups.size(); i++) {
            assertEquals(uploadedGroups.get(i), downloadedGroups.get(i));
        }

        // Validate Resource types
        final var uploadedResourceTypes = new ArrayList<>(uploaded.getResourceTypes());
        final var downloadedResourceTypes = new ArrayList<>(downloaded.getResourceTypes());
        assertEquals(uploadedResourceTypes.size(), downloadedResourceTypes.size());
        for (int i = 0; i < uploadedResourceTypes.size(); i++) {
            assertEquals(uploadedResourceTypes.get(i), downloadedResourceTypes.get(i));
        }

        // Validate System defs
        final var uploadedSystemDefs = new ArrayList<>(uploaded.getSystemDefs());
        final var downloadedSystemDefs = new ArrayList<>(downloaded.getSystemDefs());
        assertEquals(uploadedSystemDefs.size(), downloadedSystemDefs.size());
        for (int i = 0; i < uploadedSystemDefs.size(); i++) {
            assertEquals(uploadedSystemDefs.get(i), downloadedSystemDefs.get(i));
        }
    }

    private static String readEntityAsString(Object entity) throws Exception {
        if (entity instanceof StreamingOutput) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ((StreamingOutput) entity).write(baos);
            return baos.toString(StandardCharsets.UTF_8);
        }
        if (entity instanceof InputStream) {
            return new String(((InputStream) entity).readAllBytes(), StandardCharsets.UTF_8);
        }
        if (entity instanceof byte[]) {
            return new String((byte[]) entity, StandardCharsets.UTF_8);
        }
        if (entity instanceof String) {
            return (String) entity;
        }
        throw new AssertionError("Unexpected entity type: " + entity.getClass());
    }


    private static void assertLooksLikeXml(String label, String xml) {
        assertNotNull(label + " should not be null", xml);
        /** Helper to create a mocked Attachment for a given file */
        String normalized = stripUtf8Bom(xml).trim();
        assertFalse(label + " should not be empty", normalized.isEmpty());
        assertTrue(label + " should start with '<' but starts with: " +
                        normalized.substring(0, Math.min(80, normalized.length())),
                normalized.startsWith("<"));
    }

    @Test
    @Transactional
    public void testDeleteSnmpDataCollectionSources_BadRequest_Success_AndInternalServerError() throws Exception {
        // --- BAD_REQUEST: null payload
        Response bad1 = dataCollectionConfRestApi.deleteSnmpDataCollectionSources(null, securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad1.getStatus());
        Assert.assertEquals("Snmp Data Collection IDs to delete must not be empty", bad1.getEntity());

        // --- BAD_REQUEST: empty ids
        Response bad2 = dataCollectionConfRestApi.deleteSnmpDataCollectionSources(Collections.emptyList(), securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad2.getStatus());
        Assert.assertEquals("Snmp Data Collection IDs to delete must not be empty", bad2.getEntity());

        // --- SUCCESS
        SnmpCollectionSource src1 = new SnmpCollectionSource();
        src1.setName("delete.source.combo.1");
        src1.setVendor("opennms");
        src1.setCreatedTime(new Date());
        src1.setDescription("to be deleted 1");
        src1.setEnabled(true);

        SnmpCollectionSource src2 = new SnmpCollectionSource();
        src2.setName("delete.source.combo.2");
        src2.setVendor("opennms");
        src2.setCreatedTime(new Date());
        src2.setDescription("to be deleted 2");
        src2.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(src1);
        snmpCollectionSourceDao.saveOrUpdate(src2);
        snmpCollectionSourceDao.flush();

        Response ok = dataCollectionConfRestApi.deleteSnmpDataCollectionSources(List.of(src1.getId(), src2.getId()), securityContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), ok.getStatus());
        Assert.assertEquals("Snmp Data Collection deleted successfully", ok.getEntity());

    }

    @Test
    @Transactional
    public void testDeleteMibGroupsForSource_BadRequest_AndSuccess() throws Exception {
        // --- Setup source + 2 mib groups
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
        g1.setMibObjProperties("{\"property\":\"value\"}");
        g1.setEnabled(true);

        SnmpCollectionMibGroup g2 = new SnmpCollectionMibGroup();
        g2.setCollectionSource(src);
        g2.setName("delete-me-mibgroup-2");
        g2.setIfType("Loopback");
        g2.setMibGroupNames("IP-MIB::ip");
        g2.setMibObjects("ipAdEntAddr");
        g2.setMibObjProperties("{\"property\":\"value\"}");
        g2.setEnabled(true);

        snmpCollectionMibGroupDao.saveOrUpdate(g1);
        snmpCollectionMibGroupDao.saveOrUpdate(g2);
        snmpCollectionMibGroupDao.flush();

        // --- BAD_REQUEST: null ids
        Response bad1 = dataCollectionConfRestApi.deleteMibGroupsForSource(src.getId(), null, securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad1.getStatus());
        Assert.assertEquals("MIB Group IDs to delete must not be empty", bad1.getEntity());

        // --- BAD_REQUEST: empty ids
        Response bad2 = dataCollectionConfRestApi.deleteMibGroupsForSource(src.getId(), Collections.emptyList(), securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad2.getStatus());
        Assert.assertEquals("MIB Group IDs to delete must not be empty", bad2.getEntity());

        // --- SUCCESS: delete both
        Response ok = dataCollectionConfRestApi.deleteMibGroupsForSource(src.getId(), List.of(g1.getId(), g2.getId()), securityContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), ok.getStatus());
        Assert.assertEquals("Snmp Data Collection Mib Groups deleted successfully", ok.getEntity());

        // verify via filter endpoint
        Response filter = dataCollectionConfRestApi.filterDataCollectionMibGroupByCollectionSourceId(
                src.getId(), "delete-me-mibgroup", "name", "ASC", 0, 0, 10, securityContext);
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), filter.getStatus());
    }

    @Test
    @Transactional
    public void testDeleteResourceTypesForSource_BadRequest_AndSuccess() throws Exception {
        // --- Setup source + 2 resource types
        SnmpCollectionSource src = new SnmpCollectionSource();
        src.setName("delete.resourcetype.combo.source");
        src.setVendor("opennms");
        src.setDescription("source for resource type delete");
        src.setCreatedTime(new Date());
        src.setEnabled(true);
        snmpCollectionSourceDao.saveOrUpdate(src);
        snmpCollectionSourceDao.flush();

        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(src);
        rt1.setName("delete-me-resourcetype-1");
        rt1.setLabel("Delete Me 1");
        rt1.setResourceLabel("Delete Me 1 ${ifDescr}");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(true);

        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(src);
        rt2.setName("delete-me-resourcetype-2");
        rt2.setLabel("Delete Me 2");
        rt2.setResourceLabel("Delete Me 2 ${ifDescr}");
        rt2.setPersistenceSelectorStrategy("default");
        rt2.setStorageStrategy("db");
        rt2.setEnabled(true);

        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        // --- BAD_REQUEST: null ids
        Response bad1 = dataCollectionConfRestApi.deleteResourceTypesForSource(src.getId(), null, securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad1.getStatus());
        Assert.assertEquals("Resource Type IDs to delete must not be empty", bad1.getEntity());

        // --- BAD_REQUEST: empty ids
        Response bad2 = dataCollectionConfRestApi.deleteResourceTypesForSource(src.getId(), Collections.emptyList(), securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad2.getStatus());
        Assert.assertEquals("Resource Type IDs to delete must not be empty", bad2.getEntity());

        // --- SUCCESS: delete both
        Response ok = dataCollectionConfRestApi.deleteResourceTypesForSource(src.getId(), List.of(rt1.getId(), rt2.getId()), securityContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), ok.getStatus());
        Assert.assertEquals("Snmp Data Collection Resource Types deleted successfully", ok.getEntity());

        // verify via filter endpoint
        Response filter = dataCollectionConfRestApi.filterDataCollectionResourceTypeByCollectionSourceId(
                src.getId(), "delete-me-resourcetype", "name", "ASC", 0, 0, 10, securityContext);
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), filter.getStatus());
    }

    @Test
    @Transactional
    public void testDeleteSystemDefsForSource_BadRequest_AndSuccess() throws Exception {
        // --- Setup source + 2 system defs
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

        SnmpCollectionSystemDef d2 = new SnmpCollectionSystemDef();
        d2.setCollectionSource(src);
        d2.setName("delete-me-systemdef-2");
        d2.setSysoid(".1.3.6.1.2.1.2");
        d2.setSysoidMask("255.255.255.0");
        d2.setIpAddresses("10.0.0.0");
        d2.setIpAddressMasks("255.0.0.0");
        d2.setMibGroupNames("MIB-GROUP-2");

        snmpCollectionSystemDefDao.saveOrUpdate(d1);
        snmpCollectionSystemDefDao.saveOrUpdate(d2);
        snmpCollectionSystemDefDao.flush();

        // --- BAD_REQUEST: null ids
        Response bad1 = dataCollectionConfRestApi.deleteSystemDefsForSource(src.getId(), null, securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad1.getStatus());
        Assert.assertEquals("System Def IDs to delete must not be empty", bad1.getEntity());

        // --- BAD_REQUEST: empty ids
        Response bad2 = dataCollectionConfRestApi.deleteSystemDefsForSource(src.getId(), Collections.emptyList(), securityContext);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), bad2.getStatus());
        Assert.assertEquals("System Def IDs to delete must not be empty", bad2.getEntity());

        // --- SUCCESS: delete both
        Response ok = dataCollectionConfRestApi.deleteSystemDefsForSource(src.getId(), List.of(d1.getId(), d2.getId()), securityContext);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), ok.getStatus());
        Assert.assertEquals("Snmp Data Collection System Def deleted successfully", ok.getEntity());

        // verify via filter endpoint
        Response filter = dataCollectionConfRestApi.filterDataCollectionSystemDefByCollectionSourceId(
                src.getId(), "delete-me-systemdef", "name", "ASC", 0, 0, 10, securityContext);
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), filter.getStatus());

    }
    @Test
    @Transactional
    public void testEnableDisableSnmpDataCollectionSources_success_enable() throws Exception {
        SnmpCollectionSource s1 = new SnmpCollectionSource();
        s1.setName("enable.test.snmp.1");
        s1.setVendor("v1");
        s1.setDescription("desc1");
        s1.setCreatedTime(new Date());
        s1.setEnabled(false);

        SnmpCollectionSource s2 = new SnmpCollectionSource();
        s2.setName("enable.test.snmp.2");
        s2.setVendor("v2");
        s2.setDescription("desc2");
        s2.setCreatedTime(new Date());
        s2.setEnabled(false);

        snmpCollectionSourceDao.saveOrUpdate(s1);
        snmpCollectionSourceDao.saveOrUpdate(s2);
        snmpCollectionSourceDao.flush();

        List<Integer> ids = Arrays.asList(s1.getId(), s2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpDataCollectionSources(true, ids, securityContext);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP data collection sources updated successfully.", resp.getEntity());

        SnmpCollectionSource r1 = snmpCollectionSourceDao.get(s1.getId());
        SnmpCollectionSource r2 = snmpCollectionSourceDao.get(s2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpDataCollectionSources_success_disable() throws Exception {
        SnmpCollectionSource s1 = new SnmpCollectionSource();
        s1.setName("disable.test.snmp.1");
        s1.setVendor("v1");
        s1.setDescription("desc1");
        s1.setCreatedTime(new Date());
        s1.setEnabled(true);

        SnmpCollectionSource s2 = new SnmpCollectionSource();
        s2.setName("disable.test.snmp.2");
        s2.setVendor("v2");
        s2.setDescription("desc2");
        s2.setCreatedTime(new Date());
        s2.setEnabled(true);

        snmpCollectionSourceDao.saveOrUpdate(s1);
        snmpCollectionSourceDao.saveOrUpdate(s2);
        snmpCollectionSourceDao.flush();

        List<Integer> ids = Arrays.asList(s1.getId(), s2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpDataCollectionSources(false, ids, securityContext);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP data collection sources updated successfully.", resp.getEntity());

        SnmpCollectionSource r1 = snmpCollectionSourceDao.get(s1.getId());
        SnmpCollectionSource r2 = snmpCollectionSourceDao.get(s2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpMibGroups_success_enable() throws Exception {
        final var now = new Date();
        SnmpCollectionSource source = new SnmpCollectionSource();
        source.setName("mibgroups.enable.source");
        source.setVendor("v1");
        source.setDescription("source for mib group enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();


        SnmpCollectionMibGroup group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(source);
        group1.setName("if-mib-interfaces");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group1.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group1);
        snmpCollectionMibGroupDao.flush();

        SnmpCollectionMibGroup group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(source);
        group2.setName("ip-mib");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group2.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group2);
        snmpCollectionMibGroupDao.flush();


        Integer sourceId = source.getId();
        List<Integer> ids = Arrays.asList(group1.getId(), group2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpMibGroups(sourceId, true, ids, securityContext);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP MIB groups updated successfully.", resp.getEntity());

        SnmpCollectionMibGroup r1 = snmpCollectionMibGroupDao.get(group1.getId());
        SnmpCollectionMibGroup r2 = snmpCollectionMibGroupDao.get(group2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpMibGroups_success_disable() throws Exception {
        final var now = new Date();
        SnmpCollectionSource source = new SnmpCollectionSource();
        source.setName("mibgroups.disable.source");
        source.setVendor("v1");
        source.setDescription("source for mib group disable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();


        SnmpCollectionMibGroup group1 = new SnmpCollectionMibGroup();
        group1.setCollectionSource(source);
        group1.setName("if-mib-interfaces");
        group1.setIfType("Ethernet");
        group1.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group1.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group1.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group1);
        snmpCollectionMibGroupDao.flush();

        SnmpCollectionMibGroup group2 = new SnmpCollectionMibGroup();
        group2.setCollectionSource(source);
        group2.setName("ip-mib");
        group2.setIfType("Loopback");
        group2.setMibGroupNames("IF-MIB::ifEntry,IF-MIB::ifXEntry");
        group2.setMibObjects("ifIndex,ifDescr,ifOperStatus");
        group2.setMibObjProperties("{\"property\":\"value\"}");
        snmpCollectionMibGroupDao.saveOrUpdate(group2);
        snmpCollectionMibGroupDao.flush();


        Integer sourceId = source.getId();
        List<Integer> ids = Arrays.asList(group1.getId(), group2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpMibGroups(sourceId, false, ids, securityContext);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP MIB groups updated successfully.", resp.getEntity());

        SnmpCollectionMibGroup r1 = snmpCollectionMibGroupDao.get(group1.getId());
        SnmpCollectionMibGroup r2 = snmpCollectionMibGroupDao.get(group2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }


    @Test
    @Transactional
    public void testEnableDisableSnmpResourceTypes_success_enable() throws Exception {
        // Arrange
        final var now = new Date();

        SnmpCollectionSource source = new SnmpCollectionSource();
        source.setName("resourcetypes.enable.source");
        source.setVendor("v1");
        source.setDescription("source for resourceTypes enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // Resource type 1, matches filter "cpu"
        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(source);
        rt1.setName("cpu-resource");
        rt1.setLabel("CPU Utilization");
        rt1.setResourceLabel("CPU Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(false);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2, matches filter "disk"
        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(source);
        rt2.setName("disk-resource");
        rt2.setLabel("Disk Usage");
        rt2.setResourceLabel("Disk Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(false);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        Integer sourceId = source.getId();
        List<Integer> ids = Arrays.asList(rt1.getId(), rt2.getId());

        // Act
        Response resp = dataCollectionConfRestApi.enableDisableSnmpResourceTypes(sourceId, true, ids, securityContext);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // Assert
        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP resource types updated successfully.", resp.getEntity());

        SnmpCollectionResourceType r1 = snmpCollectionResourceTypeDao.get(rt1.getId());
        SnmpCollectionResourceType r2 = snmpCollectionResourceTypeDao.get(rt2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpResourceTypes_success_disable() throws Exception {
        // Arrange
        final var now = new Date();

        SnmpCollectionSource source = new SnmpCollectionSource();
        source.setName("resourcetypes.disable.source");
        source.setVendor("v1");
        source.setDescription("source for resourceTypes disable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // Resource type 1, matches filter "cpu"
        SnmpCollectionResourceType rt1 = new SnmpCollectionResourceType();
        rt1.setCollectionSource(source);
        rt1.setName("cpu-resource");
        rt1.setLabel("CPU Utilization");
        rt1.setResourceLabel("CPU Resource Label");
        rt1.setPersistenceSelectorStrategy("default");
        rt1.setStorageStrategy("db");
        rt1.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt1);

        // Resource type 2, matches filter "disk"
        SnmpCollectionResourceType rt2 = new SnmpCollectionResourceType();
        rt2.setCollectionSource(source);
        rt2.setName("disk-resource");
        rt2.setLabel("Disk Usage");
        rt2.setResourceLabel("Disk Resource Label");
        rt2.setPersistenceSelectorStrategy("custom");
        rt2.setStorageStrategy("fs");
        rt2.setEnabled(true);
        snmpCollectionResourceTypeDao.saveOrUpdate(rt2);
        snmpCollectionResourceTypeDao.flush();

        Integer sourceId = source.getId();
        List<Integer> ids = Arrays.asList(rt1.getId(), rt2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpResourceTypes(sourceId, false, ids, securityContext);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP resource types updated successfully.", resp.getEntity());

        SnmpCollectionResourceType r1 = snmpCollectionResourceTypeDao.get(rt1.getId());
        SnmpCollectionResourceType r2 = snmpCollectionResourceTypeDao.get(rt2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpSystemDefs_success_enable() throws Exception {
        final var now = new Date();

        SnmpCollectionSource source = new SnmpCollectionSource();
        source.setName("systemdefs.enable.source");
        source.setVendor("v1");
        source.setDescription("source for systemDefs enable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // SystemDef 1, matches filter "LinuxSystem"
        SnmpCollectionSystemDef def1 = new SnmpCollectionSystemDef();
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
        SnmpCollectionSystemDef def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(source);
        def2.setName("WindowsSystem");
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        def2.setEnabled(false);
        snmpCollectionSystemDefDao.saveOrUpdate(def2);

        Integer sourceId = source.getId();
        List<Integer> ids = Arrays.asList(def1.getId(), def2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpSystemDefs(sourceId, true, ids, securityContext);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP system defs updated successfully.", resp.getEntity());

        SnmpCollectionSystemDef r1 = snmpCollectionSystemDefDao.get(def1.getId());
        SnmpCollectionSystemDef r2 = snmpCollectionSystemDefDao.get(def2.getId());
        Assert.assertTrue(r1.getEnabled());
        Assert.assertTrue(r2.getEnabled());
    }

    @Test
    @Transactional
    public void testEnableDisableSnmpSystemDefs_success_disable() throws Exception {
        final var now = new Date();

        SnmpCollectionSource source = new SnmpCollectionSource();
        source.setName("systemdefs.disable.source");
        source.setVendor("v1");
        source.setDescription("source for systemDefs disable test");
        source.setEnabled(true);
        source.setCreatedTime(now);
        snmpCollectionSourceDao.saveOrUpdate(source);
        snmpCollectionSourceDao.flush();

        // SystemDef 1, matches filter "LinuxSystem"
        SnmpCollectionSystemDef def1 = new SnmpCollectionSystemDef();
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
        SnmpCollectionSystemDef def2 = new SnmpCollectionSystemDef();
        def2.setCollectionSource(source);
        def2.setName("WindowsSystem");
        def2.setSysoid(".1.3.6.1.2.1.2");
        def2.setSysoidMask("255.255.255.0");
        def2.setIpAddresses("192.168.1.0,10.0.0.1");
        def2.setIpAddressMasks("255.255.255.0,255.0.0.0");
        def2.setMibGroupNames("MIB-GROUP-1,MIB-GROUP-2");
        def2.setEnabled(true);
        snmpCollectionSystemDefDao.saveOrUpdate(def2);

        Integer sourceId = source.getId();
        List<Integer> ids = Arrays.asList(def1.getId(), def2.getId());

        Response resp = dataCollectionConfRestApi.enableDisableSnmpSystemDefs(sourceId, false, ids, securityContext);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Assert.assertEquals("SNMP system defs updated successfully.", resp.getEntity());

        SnmpCollectionSystemDef r1 = snmpCollectionSystemDefDao.get(def1.getId());
        SnmpCollectionSystemDef r2 = snmpCollectionSystemDefDao.get(def2.getId());
        Assert.assertFalse(r1.getEnabled());
        Assert.assertFalse(r2.getEnabled());
    }



    private static String stripUtf8Bom(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        // UTF-8 BOM char (U+FEFF) sometimes appears as the first char and breaks XML parsing
        return (s.charAt(0) == '\uFEFF') ? s.substring(1) : s;
    }


    /**
     * Helper to create a mocked Attachment for a given file
     */
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

    /** Builds a multipart "profileNames" part whose String value is the given name. */
    private Attachment profileNamesPart(final String value) {
        Attachment att = mock(Attachment.class);
        when(att.getObject(String.class)).thenReturn(value);
        return att;
    }
}
