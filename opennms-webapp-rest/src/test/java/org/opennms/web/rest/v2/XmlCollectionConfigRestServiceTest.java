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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.XmlCollectionConfigDto;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlCollectionMetadataDto;
import org.opennms.web.rest.v2.model.XmlCollectionSettingsDto;
import org.opennms.web.rest.v2.model.XmlCollectionSummaryDto;
import org.opennms.web.rest.v2.model.XmlGroupDto;
import org.opennms.web.rest.v2.model.XmlGroupFileDto;
import org.opennms.web.rest.v2.model.XmlGroupFileSummaryDto;
import org.opennms.web.rest.v2.model.XmlObjectDto;
import org.opennms.web.rest.v2.model.XmlRequestDto;
import org.opennms.web.rest.v2.model.XmlRrdDto;
import org.opennms.web.rest.v2.model.XmlSourceDto;

public class XmlCollectionConfigRestServiceTest {

    private static final String CONFIG = XmlCollectionConfigRestService.CONFIG_NAME;
    private static final String GROUPS = XmlCollectionConfigRestService.GROUPS_CONFIG_NAME;
    private static final String DEFAULT = XmlCollectionConfigRestService.CONFIG_ID;

    /** As imported from the shipped etc/xml-datacollection-config.xml: the import is the former file path. */
    private static final String STORED_CONFIG = """
            {
              "rrdRepository": "/opt/opennms/share/rrd/snmp/",
              "xml-collection": [ {
                "name": "xml-elasticsearch-cluster-stats",
                "rrd": { "step": 300, "rra": [ "RRA:AVERAGE:0.5:1:2016", "RRA:MAX:0.5:288:366" ] },
                "xml-source": [ {
                  "url": "http://{ipaddr}:9200/_cluster/stats",
                  "import-groups": [ "xml-datacollection/elasticsearch-cluster-stats.xml" ],
                  "request": { "parameter": [], "header": [] }
                } ]
              } ]
            }""";

    private static final String STORED_GROUPS = """
            { "xml-group": [ {
                "name": "cluster", "resource-type": "node", "resource-xpath": "/",
                "xml-object": [ { "name": "clusterName", "type": "string", "xpath": "cluster_name" } ]
            } ] }""";

    private XmlCollectionConfigRestService restService;
    private InMemoryConfigurationManager cm;

    @Before
    public void setUp() throws Exception {
        cm = new InMemoryConfigurationManager();
        cm.put(CONFIG, DEFAULT, STORED_CONFIG);
        cm.put(GROUPS, "elasticsearch-cluster-stats", STORED_GROUPS);

        restService = new XmlCollectionConfigRestService();
        setField(restService, "configurationManagerService", cm.service());
        setField(restService, "resourceDao", mock(ResourceDao.class));
    }

    // ------------------------------------------------------------------- reads

    @Test
    public void readsTheSettings() {
        final XmlCollectionSettingsDto settings = (XmlCollectionSettingsDto) restService.getSettings(admin()).getEntity();
        assertEquals("/opt/opennms/share/rrd/snmp/", settings.getRrdRepository());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listsCollectionsWithImportsAsGroupFileIds() {
        final List<XmlCollectionSummaryDto> summaries = (List<XmlCollectionSummaryDto>) restService.getCollections(admin()).getEntity();
        assertEquals(1, summaries.size());
        assertEquals("xml-elasticsearch-cluster-stats", summaries.get(0).getName());
        assertEquals(Integer.valueOf(300), summaries.get(0).getStep());
        assertEquals(List.of("elasticsearch-cluster-stats"), summaries.get(0).getImportGroups());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listsNothingWhenNoConfigurationIsStored() {
        cm.configs(CONFIG).clear();
        assertTrue(((List<XmlCollectionSummaryDto>) restService.getCollections(admin()).getEntity()).isEmpty());
    }

    @Test
    public void returnsACollectionWithAnEtag() {
        final Response response = restService.getCollection("xml-elasticsearch-cluster-stats", admin());
        assertEquals(200, response.getStatus());
        final XmlCollectionDto dto = (XmlCollectionDto) response.getEntity();
        assertNotNull(response.getEntityTag());
        assertEquals(response.getEntityTag().getValue(), dto.getVersion());
        assertEquals(2, dto.getRrd().getRras().size());
        // The empty request the configuration manager fills in must not turn into a request.
        assertNull(dto.getSources().get(0).getRequest());
        assertEquals(List.of("elasticsearch-cluster-stats"), dto.getSources().get(0).getImportGroups());
    }

    @Test
    public void unknownCollectionIs404() {
        assertEquals(404, restService.getCollection("nope", admin()).getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listsGroupFilesWithTheirUsers() {
        final List<XmlGroupFileSummaryDto> summaries = (List<XmlGroupFileSummaryDto>) restService.getGroupFiles(admin()).getEntity();
        assertEquals(1, summaries.size());
        assertEquals("elasticsearch-cluster-stats", summaries.get(0).getId());
        assertEquals(Integer.valueOf(1), summaries.get(0).getGroupCount());
        assertEquals(List.of("xml-elasticsearch-cluster-stats"), summaries.get(0).getUsedBy());
    }

    @Test
    public void returnsMetadata() {
        final XmlCollectionMetadataDto metadata = (XmlCollectionMetadataDto) restService.getMetadata(admin()).getEntity();
        assertEquals(List.of("gauge", "counter", "string"), metadata.getAttributeTypes());
        assertEquals("node", metadata.getResourceTypes().get(0).getName());
        assertEquals(List.of("elasticsearch-cluster-stats"), metadata.getGroupFiles());
    }

    // ---------------------------------------------------------------- settings

    @Test
    public void updatesTheSettings() {
        final XmlCollectionSettingsDto settings = new XmlCollectionSettingsDto();
        settings.setRrdRepository(" /var/rrd/ ");
        assertEquals(204, restService.updateSettings(settings, admin()).getStatus());
        assertEquals("/var/rrd/", cm.get(CONFIG, DEFAULT).getString("rrdRepository"));
        // The collections are kept.
        assertEquals(1, cm.get(CONFIG, DEFAULT).getJSONArray("xml-collection").length());
    }

    @Test
    public void rejectsABlankRrdRepository() {
        assertEquals(400, restService.updateSettings(new XmlCollectionSettingsDto(), admin()).getStatus());
    }

    @Test
    public void registersTheConfigurationWhenNoneIsStored() {
        cm.configs(CONFIG).clear();
        final XmlCollectionSettingsDto settings = new XmlCollectionSettingsDto();
        settings.setRrdRepository("/var/rrd/");
        assertEquals(204, restService.updateSettings(settings, admin()).getStatus());
        assertEquals("/var/rrd/", cm.get(CONFIG, DEFAULT).getString("rrdRepository"));
    }

    // ------------------------------------------------------------- collections

    @Test
    public void createsACollection() {
        final Response response = restService.createCollection(collection("new"), admin());
        assertEquals(201, response.getStatus());
        assertTrue(response.getLocation().toString().endsWith("xml-collection/collections/new"));

        final XmlCollectionDto stored = (XmlCollectionDto) restService.getCollection("new", admin()).getEntity();
        final XmlSourceDto source = stored.getSources().get(0);
        assertEquals("http://{ipaddr}/status", source.getUrl());
        assertEquals("POST", source.getRequest().getMethod());
        assertEquals("timeout", source.getRequest().getParameters().get(0).getKey());
        assertEquals("application/json", source.getRequest().getContentType());
        assertEquals("{}", source.getRequest().getContent());
        assertEquals("gauge", source.getGroups().get(0).getObjects().get(0).getType());
        assertEquals(List.of("elasticsearch-cluster-stats"), source.getImportGroups());
    }

    @Test
    public void storesTheSameJsonShapeTheUpgradeImports() {
        restService.createCollection(collection("new"), admin());
        final JSONObject stored = cm.get(CONFIG, DEFAULT).getJSONArray("xml-collection").getJSONObject(1);
        final JSONObject source = stored.getJSONArray("xml-source").getJSONObject(0);
        assertEquals("http://{ipaddr}/status", source.getString("url"));
        assertEquals("elasticsearch-cluster-stats", source.getJSONArray("import-groups").getString(0));
        assertEquals("gauge", source.getJSONArray("xml-group").getJSONObject(0).getJSONArray("xml-object").getJSONObject(0).getString("type"));
        assertEquals(300, stored.getJSONObject("rrd").getInt("step"));
    }

    @Test
    public void refusesADuplicateCollection() {
        assertEquals(409, restService.createCollection(collection("xml-elasticsearch-cluster-stats"), admin()).getStatus());
    }

    @Test
    public void refusesAnImportOfAnUnknownGroupFile() {
        final XmlCollectionDto dto = collection("new");
        dto.getSources().get(0).setImportGroups(List.of("missing"));
        final Response response = restService.createCollection(dto, admin());
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("'missing' does not exist"));
    }

    @Test
    public void refusesAnInvalidCollection() {
        final XmlCollectionDto dto = collection("new");
        dto.getRrd().setRras(List.of("RRA:SUM:0.5:1:10"));
        assertEquals(400, restService.createCollection(dto, admin()).getStatus());
        assertEquals(0, cm.writes());
    }

    @Test
    public void updatesACollectionInPlace() {
        restService.createCollection(collection("second"), admin());
        final XmlCollectionDto dto = get("xml-elasticsearch-cluster-stats");
        dto.getRrd().setStep(60);

        assertEquals(204, restService.updateCollection("xml-elasticsearch-cluster-stats", dto, quoted(dto.getVersion()), admin()).getStatus());
        assertEquals(Integer.valueOf(60), get("xml-elasticsearch-cluster-stats").getRrd().getStep());
        // Order is kept.
        assertEquals("xml-elasticsearch-cluster-stats", cm.get(CONFIG, DEFAULT).getJSONArray("xml-collection").getJSONObject(0).getString("name"));
    }

    @Test
    public void renamesACollection() {
        final XmlCollectionDto dto = get("xml-elasticsearch-cluster-stats");
        dto.setName("renamed");
        assertEquals(204, restService.updateCollection("xml-elasticsearch-cluster-stats", dto, null, admin()).getStatus());
        assertEquals(404, restService.getCollection("xml-elasticsearch-cluster-stats", admin()).getStatus());
        assertEquals(200, restService.getCollection("renamed", admin()).getStatus());
    }

    @Test
    public void refusesARenameOntoAnotherCollection() {
        restService.createCollection(collection("second"), admin());
        final XmlCollectionDto dto = get("xml-elasticsearch-cluster-stats");
        dto.setName("second");
        assertEquals(409, restService.updateCollection("xml-elasticsearch-cluster-stats", dto, null, admin()).getStatus());
    }

    @Test
    public void refusesAStaleUpdate() {
        final XmlCollectionDto dto = get("xml-elasticsearch-cluster-stats");
        final String version = dto.getVersion();

        final XmlCollectionDto other = get("xml-elasticsearch-cluster-stats");
        other.getRrd().setStep(60);
        assertEquals(204, restService.updateCollection(other.getName(), other, quoted(other.getVersion()), admin()).getStatus());

        dto.getRrd().setStep(120);
        assertEquals(412, restService.updateCollection(dto.getName(), dto, quoted(version), admin()).getStatus());
        assertEquals(Integer.valueOf(60), get("xml-elasticsearch-cluster-stats").getRrd().getStep());
    }

    @Test
    public void updatingAnUnknownCollectionIs404() {
        assertEquals(404, restService.updateCollection("nope", collection("nope"), null, admin()).getStatus());
    }

    @Test
    public void deletesACollection() {
        assertEquals(204, restService.deleteCollection("xml-elasticsearch-cluster-stats", null, admin()).getStatus());
        assertEquals(404, restService.getCollection("xml-elasticsearch-cluster-stats", admin()).getStatus());
    }

    @Test
    public void refusesAStaleDelete() {
        assertEquals(412, restService.deleteCollection("xml-elasticsearch-cluster-stats", "\"outdated\"", admin()).getStatus());
    }

    // ------------------------------------------------------------- group files

    @Test
    public void createsAndReadsAGroupFile() {
        final Response response = restService.createGroupFile(groupFile("shared"), admin());
        assertEquals(201, response.getStatus());

        final Response read = restService.getGroupFile("shared", admin());
        assertEquals(200, read.getStatus());
        final XmlGroupFileDto dto = (XmlGroupFileDto) read.getEntity();
        assertEquals("shared", dto.getId());
        assertEquals("g1", dto.getGroups().get(0).getName());
        assertEquals(read.getEntityTag().getValue(), dto.getVersion());
    }

    @Test
    public void refusesADuplicateGroupFile() {
        assertEquals(409, restService.createGroupFile(groupFile("elasticsearch-cluster-stats"), admin()).getStatus());
    }

    @Test
    public void refusesAnInvalidGroupFileId() {
        assertEquals(400, restService.createGroupFile(groupFile("../etc/passwd"), admin()).getStatus());
    }

    @Test
    public void updatesAGroupFile() {
        final XmlGroupFileDto dto = (XmlGroupFileDto) restService.getGroupFile("elasticsearch-cluster-stats", admin()).getEntity();
        dto.getGroups().get(0).setResourceXpath("/cluster");
        assertEquals(204, restService.updateGroupFile("elasticsearch-cluster-stats", dto, quoted(dto.getVersion()), admin()).getStatus());
        assertEquals("/cluster", cm.get(GROUPS, "elasticsearch-cluster-stats").getJSONArray("xml-group").getJSONObject(0).getString("resource-xpath"));
    }

    @Test
    public void refusesToRenameAGroupFile() {
        final XmlGroupFileDto dto = groupFile("other");
        assertEquals(400, restService.updateGroupFile("elasticsearch-cluster-stats", dto, null, admin()).getStatus());
    }

    @Test
    public void refusesAStaleGroupFileUpdate() {
        final XmlGroupFileDto dto = groupFile("elasticsearch-cluster-stats");
        assertEquals(412, restService.updateGroupFile("elasticsearch-cluster-stats", dto, "\"outdated\"", admin()).getStatus());
    }

    @Test
    public void refusesToDeleteAnImportedGroupFile() {
        final Response response = restService.deleteGroupFile("elasticsearch-cluster-stats", null, admin());
        assertEquals(409, response.getStatus());
        assertTrue(response.getEntity().toString().contains("xml-elasticsearch-cluster-stats"));
        assertNotNull(cm.get(GROUPS, "elasticsearch-cluster-stats"));
    }

    @Test
    public void deletesAnUnusedGroupFile() {
        restService.createGroupFile(groupFile("shared"), admin());
        assertEquals(204, restService.deleteGroupFile("shared", null, admin()).getStatus());
        assertNull(cm.get(GROUPS, "shared"));
        assertEquals(404, restService.deleteGroupFile("shared", null, admin()).getStatus());
    }

    // ------------------------------------------------------- download / upload

    @Test
    public void downloadRequiresAdmin() {
        assertEquals(403, restService.downloadConfiguration(user()).getStatus());
    }

    @Test
    public void downloadedConfigurationUploadsUnchanged() throws Exception {
        final Response download = restService.downloadConfiguration(admin());
        assertEquals(200, download.getStatus());
        final byte[] body = (byte[]) download.getEntity();
        final XmlCollectionConfigDto dto = new ObjectMapper().readValue(body, XmlCollectionConfigDto.class);
        assertEquals(1, dto.getCollections().size());
        assertEquals(1, dto.getGroupFiles().size());

        final XmlCollectionDto before = get("xml-elasticsearch-cluster-stats");
        assertEquals(200, restService.uploadConfiguration(attachment(body, "xml-collection.json"), admin()).getStatus());
        assertEquals(before.getVersion(), get("xml-elasticsearch-cluster-stats").getVersion());
    }

    @Test
    public void uploadReplacesGroupFilesAndDropsMissingOnes() throws Exception {
        restService.createGroupFile(groupFile("obsolete"), admin());

        final XmlCollectionConfigDto dto = new XmlCollectionConfigDto();
        dto.setRrdRepository("/var/rrd/");
        dto.setGroupFiles(List.of(groupFile("shared")));
        final XmlCollectionDto collection = collection("only");
        collection.getSources().get(0).setImportGroups(List.of("shared"));
        dto.setCollections(List.of(collection));

        final byte[] body = new ObjectMapper().writeValueAsBytes(dto);
        assertEquals(200, restService.uploadConfiguration(attachment(body, "x.json"), admin()).getStatus());
        assertEquals(List.of("shared"), new ArrayList<>(cm.configs(GROUPS).keySet()));
        assertEquals("only", cm.get(CONFIG, DEFAULT).getJSONArray("xml-collection").getJSONObject(0).getString("name"));
    }

    @Test
    public void uploadRefusesImportsOfGroupFilesItDoesNotContain() throws Exception {
        final XmlCollectionConfigDto dto = new XmlCollectionConfigDto();
        dto.setRrdRepository("/var/rrd/");
        dto.setCollections(List.of(collection("only")));
        final byte[] body = new ObjectMapper().writeValueAsBytes(dto);
        assertEquals(400, restService.uploadConfiguration(attachment(body, "x.json"), admin()).getStatus());
        assertEquals(0, cm.writes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void importsALegacyGroupFileNamedAfterTheFile() {
        final String xml = """
                <xml-groups>
                    <xml-group name="cluster-indices" resource-type="node" resource-xpath="/indices">
                        <xml-object name="indicesCount" type="GAUGE" xpath="count"/>
                        <xml-object name="status" type="gauge" xpath="status">
                            <xml-mapping from="green" to="0"/>
                            <xml-mapping to="9"/>
                        </xml-object>
                    </xml-group>
                </xml-groups>""";
        final Response response = restService.uploadXml(attachment(xml.getBytes(StandardCharsets.UTF_8), "es-indices.xml"), null, admin());
        assertEquals(response.getEntity().toString(), 200, response.getStatus());
        assertEquals("es-indices", ((Map<String, Object>) response.getEntity()).get("id"));

        final XmlGroupFileDto dto = (XmlGroupFileDto) restService.getGroupFile("es-indices", admin()).getEntity();
        final XmlObjectDto status = dto.getGroups().get(0).getObjects().get(1);
        assertEquals("gauge", status.getType());
        assertEquals("green", status.getMappings().get(0).getFrom());
        assertNull(status.getMappings().get(1).getFrom());
        assertEquals("9", status.getMappings().get(1).getTo());
    }

    @Test
    public void importsALegacyGroupFileUnderTheGivenId() {
        final String xml = "<xml-groups xmlns=\"http://xmlns.opennms.org/xsd/config/xml-datacollection\">"
                + "<xml-group name=\"g\" resource-type=\"node\" resource-xpath=\"/\"><xml-object name=\"o\" type=\"counter\" xpath=\"o\"/></xml-group>"
                + "</xml-groups>";
        assertEquals(200, restService.uploadXml(attachment(xml.getBytes(StandardCharsets.UTF_8), "whatever.xml"), "my-id", admin()).getStatus());
        assertNotNull(cm.get(GROUPS, "my-id"));
    }

    @Test
    public void importsALegacyCollectionConfiguration() {
        final String xml = """
                <xml-datacollection-config rrdRepository="/var/rrd/" xmlns="http://xmlns.opennms.org/xsd/config/xml-datacollection">
                    <xml-collection name="imported">
                        <rrd step="300">
                            <rra>RRA:AVERAGE:0.5:1:2016</rra>
                        </rrd>
                        <xml-source url="http://{ipaddr}/stats">
                            <request method="GET">
                                <parameter name="timeout" value="3000"/>
                                <header name="Accept" value="application/json"/>
                            </request>
                            <import-groups>xml-datacollection/elasticsearch-cluster-stats.xml</import-groups>
                        </xml-source>
                    </xml-collection>
                </xml-datacollection-config>""";
        final Response response = restService.uploadXml(attachment(xml.getBytes(StandardCharsets.UTF_8), "xml-datacollection-config.xml"), null, admin());
        assertEquals(response.getEntity().toString(), 200, response.getStatus());

        final XmlCollectionDto dto = get("imported");
        assertEquals("Accept", dto.getSources().get(0).getRequest().getHeaders().get(0).getKey());
        assertEquals(List.of("elasticsearch-cluster-stats"), dto.getSources().get(0).getImportGroups());
        assertEquals(404, restService.getCollection("xml-elasticsearch-cluster-stats", admin()).getStatus());
        assertEquals("/var/rrd/", cm.get(CONFIG, DEFAULT).getString("rrdRepository"));
    }

    @Test
    public void refusesALegacyCollectionConfigurationImportingMissingGroupFiles() {
        final String xml = """
                <xml-datacollection-config rrdRepository="/var/rrd/">
                    <xml-collection name="imported">
                        <rrd step="300"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>
                        <xml-source url="http://{ipaddr}/stats">
                            <import-groups>xml-datacollection/missing.xml</import-groups>
                        </xml-source>
                    </xml-collection>
                </xml-datacollection-config>""";
        final Response response = restService.uploadXml(attachment(xml.getBytes(StandardCharsets.UTF_8), "c.xml"), null, admin());
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Upload the group files first"));
    }

    @Test
    public void refusesAnUnknownXmlDocument() {
        final Response response = restService.uploadXml(attachment("<foo/>".getBytes(StandardCharsets.UTF_8), "foo.xml"), null, admin());
        assertEquals(400, response.getStatus());
    }

    @Test
    public void refusesXmlWithADoctype() {
        final String xml = "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><xml-groups>&e;</xml-groups>";
        assertEquals(400, restService.uploadXml(attachment(xml.getBytes(StandardCharsets.UTF_8), "x.xml"), null, admin()).getStatus());
        assertTrue(cm.configs(GROUPS).containsKey("elasticsearch-cluster-stats"));
        assertFalse(cm.configs(GROUPS).containsKey("x"));
    }

    // ------------------------------------------------------------------ helpers

    private XmlCollectionDto get(final String name) {
        return (XmlCollectionDto) restService.getCollection(name, admin()).getEntity();
    }

    static XmlCollectionDto collection(final String name) {
        final XmlRrdDto rrd = new XmlRrdDto();
        rrd.setStep(300);
        rrd.setRras(List.of("RRA:AVERAGE:0.5:1:2016"));

        final ParameterDto timeout = new ParameterDto();
        timeout.setKey("timeout");
        timeout.setValue("3000");
        final XmlRequestDto request = new XmlRequestDto();
        request.setMethod("post");
        request.setParameters(List.of(timeout));
        request.setContentType("application/json");
        request.setContent("{}");

        final XmlSourceDto source = new XmlSourceDto();
        source.setUrl(" http://{ipaddr}/status ");
        source.setRequest(request);
        source.setImportGroups(List.of("elasticsearch-cluster-stats"));
        source.setGroups(List.of(group("status")));

        final XmlCollectionDto dto = new XmlCollectionDto();
        dto.setName(name);
        dto.setRrd(rrd);
        dto.setSources(List.of(source));
        return dto;
    }

    static XmlGroupDto group(final String name) {
        final XmlObjectDto object = new XmlObjectDto();
        object.setName("value");
        object.setType("GAUGE");
        object.setXpath("value");

        final XmlGroupDto group = new XmlGroupDto();
        group.setName(name);
        group.setResourceType("node");
        group.setResourceXpath("/");
        group.setObjects(List.of(object));
        return group;
    }

    private static XmlGroupFileDto groupFile(final String id) {
        final XmlGroupFileDto dto = new XmlGroupFileDto();
        dto.setId(id);
        dto.setGroups(List.of(group("g1")));
        return dto;
    }

    private static String quoted(final String etag) {
        return "\"" + etag + "\"";
    }

    private static Attachment attachment(final byte[] body, final String fileName) {
        final Attachment attachment = mock(Attachment.class);
        when(attachment.getObject(java.io.InputStream.class)).thenReturn(new ByteArrayInputStream(body));
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("form-data; name=\"upload\"; filename=\"" + fileName + "\""));
        return attachment;
    }

    private static SecurityContext admin() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        return context;
    }

    private static SecurityContext user() {
        return mock(SecurityContext.class);
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
