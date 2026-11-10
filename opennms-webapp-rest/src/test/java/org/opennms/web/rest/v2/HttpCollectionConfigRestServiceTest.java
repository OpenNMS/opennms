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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.dao.api.HttpCollectionConfigDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.HttpAttribDto;
import org.opennms.web.rest.v2.model.HttpCollectionDto;
import org.opennms.web.rest.v2.model.HttpCollectionMetadataDto;
import org.opennms.web.rest.v2.model.HttpCollectionSettingsDto;
import org.opennms.web.rest.v2.model.HttpCollectionSummaryDto;
import org.opennms.web.rest.v2.model.HttpUriDto;

public class HttpCollectionConfigRestServiceTest {

    private static final String CONFIG_XML = """
            <http-datacollection-config xmlns="http://xmlns.opennms.org/xsd/config/http-datacollection" rrdRepository="/opt/opennms/share/rrd/snmp/">
              <http-collection name="doc-count">
                <rrd step="300"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>
                <uris>
                  <uri name="document-counts">
                    <url path="/test/resources/httpcolltest.html" user-agent="Opera/9.80" matches="(?s).*?Document Count:\\s+([0-9]+).*?Document Errors:\\s+([0-9]+).*" response-range="100-399" dotall="true">
                      <headers><header name="Accept" value="text/html"/></headers>
                    </url>
                    <attributes>
                      <attrib alias="documentCount" match-group="1" type="counter32"/>
                      <attrib alias="documentErrors" match-group="2" type="gauge"/>
                    </attributes>
                  </uri>
                </uris>
              </http-collection>
              <http-collection name="unused">
                <rrd step="60"><rra>RRA:AVERAGE:0.5:1:100</rra></rrd>
                <uris>
                  <uri name="status"><url path="/status"/></uri>
                </uris>
              </http-collection>
            </http-datacollection-config>""";

    private HttpCollectionConfigRestService restService;
    private FakeHttpCollectionConfigDao dao;
    private CollectdConfigFactory collectdConfigFactory;

    @Before
    public void setUp() throws Exception {
        restService = new HttpCollectionConfigRestService();
        dao = new FakeHttpCollectionConfigDao(JaxbUtils.unmarshal(HttpDatacollectionConfig.class, CONFIG_XML));
        collectdConfigFactory = mock(CollectdConfigFactory.class);
        // Package example1 collects doc-count with the HTTP collector. The SNMP service has a "collection"
        // parameter of its own, which must not count as a use of an HTTP collection called "unused".
        when(collectdConfigFactory.getCollectors()).thenReturn(List.of(
                collector("DocCount", HttpCollectionConfigRestService.HTTP_COLLECTOR_CLASS),
                collector("SNMP", "org.opennms.netmgt.collectd.SnmpCollector")));
        when(collectdConfigFactory.getPackages()).thenReturn(List.of(
                pkg("example1", service("DocCount", "collection", "doc-count"), service("SNMP", "collection", "unused"))));

        setField(restService, "httpCollectionConfigDao", dao);
        setField(restService, "collectdConfigFactory", collectdConfigFactory);
    }

    // ------------------------------------------------------------------- reads

    @Test
    @SuppressWarnings("unchecked")
    public void listsCollectionSummaries() {
        try (Response response = restService.getCollections(adminContext())) {
            assertEquals(200, response.getStatus());
            final List<HttpCollectionSummaryDto> summaries = (List<HttpCollectionSummaryDto>) response.getEntity();
            assertEquals(List.of("doc-count", "unused"), summaries.stream().map(HttpCollectionSummaryDto::getName).toList());
            assertEquals(Integer.valueOf(300), summaries.get(0).getRrdStep());
            assertEquals(Integer.valueOf(1), summaries.get(0).getUriCount());
            assertEquals(Integer.valueOf(2), summaries.get(0).getAttributeCount());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void listsNothingWhenNoConfigurationIsStored() {
        dao.config = null;
        try (Response response = restService.getCollections(adminContext())) {
            assertEquals(200, response.getStatus());
            assertTrue(((List<HttpCollectionSummaryDto>) response.getEntity()).isEmpty());
        }
    }

    @Test
    public void returnsACollectionWithItsDefaultsAndAnEtag() {
        try (Response response = restService.getCollection("DOC-COUNT", adminContext())) {
            assertEquals(200, response.getStatus());
            final HttpCollectionDto dto = (HttpCollectionDto) response.getEntity();
            assertEquals("doc-count", dto.getName());
            assertNotNull(dto.getVersion());
            assertEquals(dto.getVersion(), response.getEntityTag().getValue());

            final HttpUriDto uri = dto.getUris().get(0);
            assertEquals("GET", uri.getUrl().getMethod());
            assertEquals(Integer.valueOf(80), uri.getUrl().getPort());
            assertEquals("${ipaddr}", uri.getUrl().getHost());
            assertEquals("Opera/9.80", uri.getUrl().getUserAgent());
            assertEquals(Boolean.TRUE, uri.getUrl().getDotall());
            assertEquals("Accept", uri.getUrl().getHeaders().get(0).getName());
            // counter32 is an alias; the API reports the type the collector uses.
            assertEquals("counter", uri.getAttributes().get(0).getType());
        }
    }

    @Test
    public void returnsNotFoundForAnUnknownCollection() {
        try (Response response = restService.getCollection("nope", adminContext())) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void returnsTheEditorMetadata() {
        try (Response response = restService.getMetadata(adminContext())) {
            final HttpCollectionMetadataDto dto = (HttpCollectionMetadataDto) response.getEntity();
            assertEquals(List.of("gauge", "counter", "string"), dto.getAttributeTypes());
            assertEquals(List.of("GET", "POST"), dto.getMethods());
            assertEquals(Integer.valueOf(300), dto.getDefaultRrd().getStep());
        }
    }

    // ---------------------------------------------------------------- settings

    @Test
    public void updatesTheSettingsAndKeepsTheCollections() {
        final HttpCollectionSettingsDto settings;
        try (Response response = restService.getSettings(adminContext())) {
            settings = (HttpCollectionSettingsDto) response.getEntity();
        }
        final String version = settings.getVersion();
        settings.setRrdRepository("/var/lib/rrd/");

        try (Response response = restService.updateSettings(settings, "\"" + version + "\"", adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals("/var/lib/rrd/", dao.config.getRrdRepository());
        assertEquals(2, dao.config.getHttpCollection().size());

        // The old version is stale now.
        try (Response response = restService.updateSettings(settings, version, adminContext())) {
            assertEquals(412, response.getStatus());
        }
    }

    @Test
    public void rejectsSettingsWithoutRrdRepository() {
        try (Response response = restService.updateSettings(new HttpCollectionSettingsDto(), null, adminContext())) {
            assertEquals(400, response.getStatus());
        }
        assertEquals(0, dao.writes);
    }

    // ------------------------------------------------------------------ writes

    @Test
    public void createsACollection() {
        final HttpCollectionDto created = collection("new-one");
        try (Response response = restService.createCollection(created, adminContext())) {
            assertEquals(201, response.getStatus());
            assertEquals("httpcollection/collections/new-one", response.getLocation().toString());
        }
        assertEquals(3, dao.config.getHttpCollection().size());
        assertEquals("new-one", dao.config.getHttpCollection().get(2).getName());
        assertEquals(AttributeType.GAUGE, dao.config.getHttpCollection().get(2).getUris().get(0).getAttributes().get(0).getType());
    }

    @Test
    public void refusesToCreateACollectionWhoseNameDiffersOnlyInCase() {
        try (Response response = restService.createCollection(collection("Doc-Count"), adminContext())) {
            assertEquals(409, response.getStatus());
        }
        assertEquals(0, dao.writes);
    }

    @Test
    public void refusesAnInvalidCollection() {
        final HttpCollectionDto invalid = collection("bad");
        invalid.getUris().get(0).getAttributes().get(0).setMatchGroup(2);
        try (Response response = restService.createCollection(invalid, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(response.getEntity().toString().contains("matchGroup 2 does not exist"));
        }
        assertEquals(0, dao.writes);
    }

    @Test
    public void updatesACollectionWithoutTouchingTheCachedConfig() {
        final HttpDatacollectionConfig before = dao.config;
        final HttpCollectionDto dto = read("unused");
        dto.getRrd().setStep(120);

        try (Response response = restService.updateCollection("unused", dto, dto.getVersion(), adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals(120, dao.config.getHttpCollection().get(1).getRrd().getStep());
        assertEquals("the DAO's object is shared with the collector and must not be modified",
                60, before.getHttpCollection().get(1).getRrd().getStep());
    }

    @Test
    public void refusesAStaleUpdate() {
        final HttpCollectionDto dto = read("unused");
        final String version = dto.getVersion();
        dto.getRrd().setStep(120);
        try (Response response = restService.updateCollection("unused", dto, version, adminContext())) {
            assertEquals(204, response.getStatus());
        }
        dto.getRrd().setStep(180);
        try (Response response = restService.updateCollection("unused", dto, version, adminContext())) {
            assertEquals(412, response.getStatus());
        }
        assertEquals(120, dao.config.getHttpCollection().get(1).getRrd().getStep());
    }

    @Test
    public void renamesAnUnusedCollection() {
        final HttpCollectionDto dto = read("unused");
        dto.setName("renamed");
        try (Response response = restService.updateCollection("unused", dto, null, adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals("renamed", dao.config.getHttpCollection().get(1).getName());
    }

    @Test
    public void refusesToRenameACollectionCollectdUses() {
        final HttpCollectionDto dto = read("doc-count");
        dto.setName("renamed");
        try (Response response = restService.updateCollection("doc-count", dto, null, adminContext())) {
            assertEquals(409, response.getStatus());
            assertTrue(response.getEntity().toString().contains("example1"));
        }
        assertEquals(0, dao.writes);
    }

    @Test
    public void refusesToRenameOntoAnotherCollection() {
        final HttpCollectionDto dto = read("unused");
        dto.setName("DOC-COUNT");
        try (Response response = restService.updateCollection("unused", dto, null, adminContext())) {
            assertEquals(409, response.getStatus());
        }
    }

    @Test
    public void returnsNotFoundWhenUpdatingAnUnknownCollection() {
        try (Response response = restService.updateCollection("nope", collection("nope"), null, adminContext())) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void deletesAnUnusedCollection() {
        try (Response response = restService.deleteCollection("unused", null, adminContext())) {
            assertEquals(204, response.getStatus());
        }
        assertEquals(List.of("doc-count"), dao.config.getHttpCollection().stream().map(c -> c.getName()).toList());
    }

    @Test
    public void refusesToDeleteACollectionCollectdUses() {
        try (Response response = restService.deleteCollection("doc-count", null, adminContext())) {
            assertEquals(409, response.getStatus());
            assertEquals("HTTP collection 'doc-count' is used by collectd packages: example1.", response.getEntity());
        }
        assertEquals(0, dao.writes);
    }

    @Test
    public void deletesWhenTheCollectdConfigurationCannotBeRead() {
        when(collectdConfigFactory.getPackages()).thenThrow(new IllegalStateException("not loaded"));
        try (Response response = restService.deleteCollection("unused", null, adminContext())) {
            assertEquals(204, response.getStatus());
        }
    }

    @Test
    public void mapsASchemaFailureOfTheStoreToABadRequest() {
        dao.failure = new org.opennms.features.config.exception.ValidationException("schema says no");
        try (Response response = restService.createCollection(collection("new-one"), adminContext())) {
            assertEquals(400, response.getStatus());
            assertEquals("schema says no", response.getEntity());
        }
    }

    // ------------------------------------------------------- download / upload

    @Test
    public void downloadsXmlThatUploadsBackUnchanged() throws Exception {
        final String xml;
        try (Response response = restService.downloadConfiguration("xml", adminContext())) {
            assertEquals(200, response.getStatus());
            assertTrue(response.getHeaderString("Content-Disposition").contains("http-datacollection-config.xml"));
            xml = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
        }
        final HttpDatacollectionConfig before = dao.config;
        try (Response response = restService.uploadConfigurationXml(attachment(xml), adminContext())) {
            assertEquals(200, response.getStatus());
        }
        assertEquals(before.getHttpCollection().size(), dao.config.getHttpCollection().size());
        assertEquals(before.getHttpCollection().get(0).getUris().get(0).getAttributes(),
                dao.config.getHttpCollection().get(0).getUris().get(0).getAttributes());
    }

    @Test
    public void downloadsJsonThatUploadsBackUnchanged() throws Exception {
        final String json;
        try (Response response = restService.downloadConfiguration("json", adminContext())) {
            json = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
        }
        try (Response response = restService.uploadConfiguration(attachment(json), adminContext())) {
            assertEquals(200, response.getStatus());
        }
        final String again;
        try (Response response = restService.downloadConfiguration("json", adminContext())) {
            again = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
        }
        assertEquals(json, again);
    }

    @Test
    public void refusesDownloadAndUploadWithoutTheAdminRole() {
        try (Response response = restService.downloadConfiguration("xml", userContext())) {
            assertEquals(403, response.getStatus());
        }
        try (Response response = restService.uploadConfigurationXml(attachment(CONFIG_XML), userContext())) {
            assertEquals(403, response.getStatus());
        }
    }

    @Test
    public void refusesAnUnknownDownloadFormat() {
        try (Response response = restService.downloadConfiguration("yaml", adminContext())) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    public void refusesAnUploadThatDropsACollectionInUse() {
        final String xml = "<http-datacollection-config xmlns=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" rrdRepository=\"/tmp/\"/>";
        try (Response response = restService.uploadConfigurationXml(attachment(xml), adminContext())) {
            assertEquals(409, response.getStatus());
            assertTrue(response.getEntity().toString().contains("'doc-count' (used by example1)"));
        }
        assertEquals(0, dao.writes);
    }

    @Test
    public void refusesSchemaInvalidXml() {
        final String xml = CONFIG_XML.replace("RRA:AVERAGE:0.5:1:2016", "NOT-AN-RRA");
        try (Response response = restService.uploadConfigurationXml(attachment(xml), adminContext())) {
            assertEquals(400, response.getStatus());
        }
        assertEquals(0, dao.writes);
    }

    @Test
    public void refusesAnUploadWithDuplicateCollections() {
        final String xml = CONFIG_XML.replace("name=\"unused\"", "name=\"Doc-Count\"");
        try (Response response = restService.uploadConfigurationXml(attachment(xml), adminContext())) {
            assertEquals(400, response.getStatus());
        }
    }

    // ------------------------------------------------------------------ helpers

    private HttpCollectionDto read(final String name) {
        try (Response response = restService.getCollection(name, adminContext())) {
            assertEquals(200, response.getStatus());
            return (HttpCollectionDto) response.getEntity();
        }
    }

    private static HttpCollectionDto collection(final String name) {
        final HttpCollectionDto dto = new HttpCollectionDto();
        dto.setName(name);
        final org.opennms.web.rest.v2.model.HttpRrdDto rrd = new org.opennms.web.rest.v2.model.HttpRrdDto();
        rrd.setStep(300);
        rrd.setRras(List.of("RRA:AVERAGE:0.5:1:2016"));
        dto.setRrd(rrd);

        final org.opennms.web.rest.v2.model.HttpUrlDto url = new org.opennms.web.rest.v2.model.HttpUrlDto();
        url.setPath("/status");
        url.setMatches("Uptime: ([0-9]+)");
        final HttpAttribDto attrib = new HttpAttribDto();
        attrib.setAlias("uptime");
        attrib.setMatchGroup(1);
        attrib.setType("gauge");
        final HttpUriDto uri = new HttpUriDto();
        uri.setName("status");
        uri.setUrl(url);
        uri.setAttributes(new ArrayList<>(List.of(attrib)));
        dto.setUris(new ArrayList<>(List.of(uri)));
        return dto;
    }

    private static Collector collector(final String service, final String className) {
        final Collector collector = new Collector();
        collector.setService(service);
        collector.setClassName(className);
        return collector;
    }

    private static Service service(final String name, final String key, final String value) {
        final Service service = new Service();
        service.setName(name);
        service.setInterval(300000L);
        service.addParameter(key, value);
        return service;
    }

    private static Package pkg(final String name, final Service... services) {
        final Package pkg = new Package();
        pkg.setName(name);
        pkg.setServices(List.of(services));
        return pkg;
    }

    private static Attachment attachment(final String body) {
        final Attachment attachment = mock(Attachment.class);
        final InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        when(attachment.getObject(InputStream.class)).thenReturn(stream);
        return attachment;
    }

    private static SecurityContext adminContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        return context;
    }

    private static SecurityContext userContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(false);
        return context;
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /** Stores a copy, the way the configuration manager hands back a freshly deserialized object after a write. */
    private static final class FakeHttpCollectionConfigDao implements HttpCollectionConfigDao {
        private HttpDatacollectionConfig config;
        private RuntimeException failure;
        private int writes;

        private FakeHttpCollectionConfigDao(final HttpDatacollectionConfig config) {
            this.config = config;
        }

        @Override
        public HttpDatacollectionConfig getConfig() {
            return config;
        }

        @Override
        public void updateConfig(final HttpDatacollectionConfig config) {
            replaceConfig(config);
        }

        @Override
        public void replaceConfig(final HttpDatacollectionConfig config) {
            if (failure != null) {
                throw failure;
            }
            assertNull("the service must hand over a new object", config == this.config ? "same" : null);
            assertFalse(config.getRrdRepository().isEmpty());
            writes++;
            this.config = JaxbUtils.duplicateObject(config, HttpDatacollectionConfig.class);
        }
    }
}
