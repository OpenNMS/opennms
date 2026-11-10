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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Parameter;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.JdbcCollectionDto;
import org.opennms.web.rest.v2.model.JdbcColumnDto;
import org.opennms.web.rest.v2.model.JdbcDataCollectionMetadataDto;
import org.opennms.web.rest.v2.model.JdbcQueryDto;
import org.opennms.web.rest.v2.model.JdbcSourceDto;
import org.opennms.web.rest.v2.model.JdbcSourceSummaryDto;

public class JdbcDataCollectionConfigRestServiceTest {

    private JdbcDataCollectionConfigRestService restService;
    private FakeJdbcDataCollectionConfigDao dao;
    private CollectdConfigFactory collectdConfigFactory;

    @Before
    public void setUp() throws Exception {
        restService = new JdbcDataCollectionConfigRestService();
        dao = new FakeJdbcDataCollectionConfigDao();
        dao.put("default", shipped("jdbc-datacollection-config.xml"));
        dao.put("postgresql", shipped("jdbc-datacollection.d/postgresql.xml"));
        dao.put("mysql", shipped("jdbc-datacollection.d/mysql.xml"));

        collectdConfigFactory = mock(CollectdConfigFactory.class);
        when(collectdConfigFactory.getCollectors()).thenReturn(List.of(collector("PostgreSQL", "org.opennms.netmgt.collectd.JdbcCollector")));
        when(collectdConfigFactory.getPackages()).thenReturn(List.of(pkg("example1", service("PostgreSQL", "PostgreSQL"))));

        setField(restService, "jdbcDataCollectionConfigDao", dao);
        setField(restService, "collectdConfigFactory", collectdConfigFactory);
        setField(restService, "resourceDao", mock(ResourceDao.class));
    }

    // ------------------------------------------------------------------- reads

    @Test
    @SuppressWarnings("unchecked")
    public void listsSourcesInMergeOrder() {
        try (Response response = restService.getSources(adminContext())) {
            assertEquals(200, response.getStatus());
            final List<JdbcSourceSummaryDto> sources = (List<JdbcSourceSummaryDto>) response.getEntity();
            assertEquals(List.of("default", "mysql", "postgresql"), sources.stream().map(JdbcSourceSummaryDto::getId).toList());
            assertEquals(List.of("PostgreSQL"), sources.get(2).getCollectionNames());
            assertEquals(Integer.valueOf(1), sources.get(2).getCollectionCount());
        }
    }

    @Test
    public void getsASourceWithItsVersionAndWhoUsesItsCollections() {
        try (Response response = restService.getSource("postgresql", adminContext())) {
            assertEquals(200, response.getStatus());
            final JdbcSourceDto source = (JdbcSourceDto) response.getEntity();
            assertNotNull(source.getVersion());
            assertEquals(source.getVersion(), response.getEntityTag().getValue());
            assertEquals("${install.share.dir}/rrd/snmp/", source.getEffectiveRrdRepository());

            final JdbcCollectionDto collection = source.getCollections().get(0);
            assertEquals("PostgreSQL", collection.getName());
            assertEquals(List.of("example1/PostgreSQL"), collection.getUsedBy());
            assertNull(collection.getShadowedBy());
            assertEquals(2, collection.getQueries().size());
        }
    }

    @Test
    public void marksACollectionThatAnEarlierSourceShadows() {
        // stored behind the back of the REST API, e.g. through /rest/cm
        final JdbcDataCollectionConfig duplicate = shipped("jdbc-datacollection.d/postgresql.xml");
        dao.put("zzz", duplicate);

        try (Response response = restService.getSource("zzz", adminContext())) {
            assertEquals("postgresql", ((JdbcSourceDto) response.getEntity()).getCollections().get(0).getShadowedBy());
        }
    }

    @Test
    public void unknownSourcesAndCollectionsAreNotFound() {
        assertStatus(404, restService.getSource("nope", adminContext()));
        assertStatus(404, restService.getCollection("nope", "PostgreSQL", adminContext()));
        assertStatus(404, restService.getCollection("postgresql", "nope", adminContext()));
    }

    @Test
    public void getsACollectionWithTheVersionOfItsSource() {
        final String version = version("postgresql");
        try (Response response = restService.getCollection("postgresql", "PostgreSQL", adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals(version, response.getEntityTag().getValue());
            assertEquals("PostgreSQL", ((JdbcCollectionDto) response.getEntity()).getName());
        }
    }

    @Test
    public void metadataListsTheTypesAndDefaults() {
        try (Response response = restService.getMetadata(adminContext())) {
            final JdbcDataCollectionMetadataDto metadata = (JdbcDataCollectionMetadataDto) response.getEntity();
            assertEquals(List.of("gauge", "counter", "string"), metadata.getAttributeTypes());
            assertEquals(Integer.valueOf(300), metadata.getDefaultRrdStep());
            assertEquals(5, metadata.getDefaultRras().size());
            assertTrue(metadata.getResourceTypes().isEmpty());
        }
    }

    // ---------------------------------------------------------- source writes

    @Test
    public void createsAnEmptySource() {
        final JdbcSourceDto source = new JdbcSourceDto();
        source.setId("custom");
        source.setRrdRepository("/tmp/rrd");
        try (Response response = restService.createSource(source, adminContext())) {
            assertEquals(201, response.getStatus());
            assertEquals("jdbc-datacollection/sources/custom", response.getLocation().toString());
        }
        assertTrue(dao.getConfigIds().contains("custom"));
        assertEquals("/tmp/rrd", dao.getConfig("custom").getRrdRepository());
    }

    @Test
    public void refusesASourceIdThatIsTakenOrInvalid() {
        final JdbcSourceDto taken = new JdbcSourceDto();
        taken.setId("mysql");
        assertStatus(409, restService.createSource(taken, adminContext()));

        final JdbcSourceDto invalid = new JdbcSourceDto();
        invalid.setId("../etc");
        assertStatus(400, restService.createSource(invalid, adminContext()));
        assertEquals(0, dao.getWrites());
    }

    @Test
    public void refusesASourceWithACollectionNameAnotherSourceUses() {
        final JdbcSourceDto source = new JdbcSourceDto();
        source.setId("custom");
        source.setCollections(List.of(collection("PostgreSQL")));
        try (Response response = restService.createSource(source, adminContext())) {
            assertEquals(409, response.getStatus());
            assertEquals("Collection 'PostgreSQL' already exists in source 'postgresql'.", response.getEntity());
        }
    }

    @Test
    public void updatesTheRepositoryOfASource() {
        final JdbcSourceDto source = new JdbcSourceDto();
        source.setRrdRepository(" /var/rrd ");
        assertStatus(204, restService.updateSource("mysql", source, quoted(version("mysql")), adminContext()));
        assertEquals("/var/rrd", dao.getConfig("mysql").getRrdRepository());
        assertEquals("the collections are kept", 1, dao.getConfig("mysql").getJdbcDataCollections().size());
    }

    @Test
    public void refusesAWriteWithAStaleVersion() {
        final String stale = version("mysql");
        final JdbcSourceDto source = new JdbcSourceDto();
        source.setRrdRepository("/var/rrd");
        assertStatus(204, restService.updateSource("mysql", source, quoted(stale), adminContext()));

        source.setRrdRepository("/var/other");
        assertStatus(412, restService.updateSource("mysql", source, quoted(stale), adminContext()));
        assertStatus(412, restService.deleteCollection("mysql", "MySQL-Global-Stats", quoted(stale), adminContext()));
        assertStatus(412, restService.deleteSource("mysql", quoted(stale), adminContext()));
        assertEquals("/var/rrd", dao.getConfig("mysql").getRrdRepository());
    }

    @Test
    public void deletesASourceButNotTheDefault() {
        assertStatus(400, restService.deleteSource("default", null, adminContext()));
        assertStatus(204, restService.deleteSource("mysql", null, adminContext()));
        assertStatus(404, restService.deleteSource("mysql", null, adminContext()));
        assertEquals(List.of("default", "postgresql"), dao.getConfigIds());
    }

    // ------------------------------------------------------ collection writes

    @Test
    public void createsACollection() {
        try (Response response = restService.createCollection("mysql", collection("Custom"), quoted(version("mysql")), adminContext())) {
            assertEquals(201, response.getStatus());
            assertEquals("jdbc-datacollection/sources/mysql/collections/Custom", response.getLocation().toString());
        }
        assertNotNull(dao.getConfig().getDataCollectionByName("Custom"));
        assertEquals(2, dao.getConfig("mysql").getJdbcDataCollections().size());
    }

    @Test
    public void refusesACollectionNameInUseInAnySource() {
        assertStatus(409, restService.createCollection("mysql", collection("MySQL-Global-Stats"), null, adminContext()));
        assertStatus(409, restService.createCollection("mysql", collection("PostgreSQL"), null, adminContext()));
        assertEquals(0, dao.getWrites());
    }

    @Test
    public void refusesAnInvalidCollection() {
        final JdbcCollectionDto collection = collection("Custom");
        collection.getQueries().get(0).getColumns().get(0).setAlias("anAliasLongerThanNineteen");
        try (Response response = restService.createCollection("mysql", collection, null, adminContext())) {
            assertEquals(400, response.getStatus());
            assertTrue(String.valueOf(response.getEntity()), String.valueOf(response.getEntity()).contains("longer than 19"));
        }
    }

    @Test
    public void replacesAndRenamesACollection() {
        final JdbcCollectionDto collection = collection("PostgreSQL-Renamed");
        assertStatus(204, restService.updateCollection("postgresql", "PostgreSQL", collection, quoted(version("postgresql")), adminContext()));

        final JdbcDataCollectionConfig stored = dao.getConfig("postgresql");
        assertEquals(1, stored.getJdbcDataCollections().size());
        assertEquals("PostgreSQL-Renamed", stored.getJdbcDataCollections().get(0).getName());
        assertEquals("the queries are replaced", 1, stored.getJdbcDataCollections().get(0).getQueries().size());
    }

    @Test
    public void keepingTheNameIsNoConflictButTakingAnotherIs() {
        assertStatus(204, restService.updateCollection("postgresql", "PostgreSQL", collection("PostgreSQL"), null, adminContext()));
        assertStatus(409, restService.updateCollection("postgresql", "PostgreSQL", collection("MySQL-Global-Stats"), null, adminContext()));
        assertStatus(404, restService.updateCollection("postgresql", "nope", collection("nope"), null, adminContext()));
    }

    @Test
    public void deletesACollection() {
        assertStatus(204, restService.deleteCollection("postgresql", "PostgreSQL", quoted(version("postgresql")), adminContext()));
        assertTrue(dao.getConfig("postgresql").getJdbcDataCollections().isEmpty());
        assertNull(dao.getConfig().getDataCollectionByName("PostgreSQL"));
    }

    // ------------------------------------------------------- download / upload

    @Test
    public void downloadsXmlThatUnmarshalsToTheStoredSource() {
        try (Response response = restService.downloadSource("postgresql", null, adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals("attachment; filename=postgresql.xml", response.getHeaderString("Content-Disposition"));
            final String xml = new String((byte[]) response.getEntity(), StandardCharsets.UTF_8);
            assertEquals(JaxbUtils.marshal(dao.getConfig("postgresql")),
                    JaxbUtils.marshal(JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, xml)));
        }
    }

    @Test
    public void downloadAndUploadRequireTheAdminRole() {
        assertStatus(403, restService.downloadSource("postgresql", "xml", userContext()));
        assertStatus(403, restService.uploadSource(attachment("{}", "x.json"), "x", false, userContext()));
        assertStatus(400, restService.downloadSource("postgresql", "yaml", adminContext()));
    }

    @Test
    public void uploadsAShippedFileUnderTheIdOfItsName() throws IOException {
        final String xml = shippedXml("jdbc-datacollection.d/oracle.xml");
        try (Response response = restService.uploadSource(attachment(xml, "oracle.xml.20261110120000"), null, false, adminContext())) {
            assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
            assertEquals("oracle", ((JdbcSourceSummaryDto) response.getEntity()).getId());
        }
        assertEquals(JaxbUtils.marshal(shipped("jdbc-datacollection.d/oracle.xml")), JaxbUtils.marshal(dao.getConfig("oracle")));
    }

    @Test
    public void uploadReplacesOnlyWhenAsked() throws IOException {
        final String xml = shippedXml("jdbc-datacollection.d/mysql.xml");
        assertStatus(409, restService.uploadSource(attachment(xml, "mysql.xml"), null, false, adminContext()));
        assertStatus(200, restService.uploadSource(attachment(xml, "mysql.xml"), null, true, adminContext()));
        // the same collections under another id would shadow the stored ones
        assertStatus(409, restService.uploadSource(attachment(xml, "mysql.xml"), "mysql2", false, adminContext()));
    }

    @Test
    public void uploadsTheJsonDownload() {
        final byte[] json;
        try (Response response = restService.downloadSource("postgresql", "json", adminContext())) {
            json = (byte[]) response.getEntity();
        }
        assertStatus(204, restService.deleteSource("postgresql", null, adminContext()));

        try (Response response = restService.uploadSource(attachment(new String(json, StandardCharsets.UTF_8), "export.json"), null, false, adminContext())) {
            assertEquals(200, response.getStatus());
            assertEquals("the id of the body wins over the file name", "postgresql", ((JdbcSourceSummaryDto) response.getEntity()).getId());
        }
        assertEquals(JaxbUtils.marshal(shipped("jdbc-datacollection.d/postgresql.xml")), JaxbUtils.marshal(dao.getConfig("postgresql")));
    }

    @Test
    public void refusesAnUnreadableUpload() {
        assertStatus(400, restService.uploadSource(attachment("<not-jdbc", "broken.xml"), null, false, adminContext()));
        assertStatus(400, restService.uploadSource(null, "x", false, adminContext()));
        assertEquals(0, dao.getWrites());
    }

    @Test
    public void derivesTheIdFromFileNames() {
        assertEquals("mysql", JdbcDataCollectionConfigRestService.idFromFileName("mysql.xml"));
        assertEquals("mysql", JdbcDataCollectionConfigRestService.idFromFileName("C:\\etc\\mysql.XML"));
        assertEquals("oracle", JdbcDataCollectionConfigRestService.idFromFileName("etc_archive/jdbc-datacollection.d/oracle.xml.20261110120000"));
        assertEquals("export", JdbcDataCollectionConfigRestService.idFromFileName("export.json"));
        assertNull(JdbcDataCollectionConfigRestService.idFromFileName(null));
    }

    // ------------------------------------------------------------------ helpers

    private String version(final String sourceId) {
        try (Response response = restService.getSource(sourceId, adminContext())) {
            return ((JdbcSourceDto) response.getEntity()).getVersion();
        }
    }

    private static String quoted(final String version) {
        return "\"" + version + "\"";
    }

    private static void assertStatus(final int expected, final Response response) {
        try (response) {
            assertEquals(String.valueOf(response.getEntity()), expected, response.getStatus());
        }
    }

    static JdbcCollectionDto collection(final String name) {
        final JdbcColumnDto column = new JdbcColumnDto();
        column.setName("nodeCount");
        column.setDataSourceName("node_count");
        column.setType("gauge");
        column.setAlias("nodeCount");

        final JdbcQueryDto query = new JdbcQueryDto();
        query.setName("nodes");
        query.setIfType("ignore");
        query.setRecheckInterval(0);
        query.setQueryString("SELECT COUNT(*) AS node_count FROM node");
        query.setColumns(List.of(column));

        final JdbcCollectionDto collection = new JdbcCollectionDto();
        collection.setName(name);
        collection.setRrdStep(300);
        collection.setRras(List.of("RRA:AVERAGE:0.5:1:2016"));
        collection.setQueries(List.of(query));
        return collection;
    }

    private static JdbcDataCollectionConfig shipped(final String file) {
        try {
            return JaxbUtils.unmarshal(JdbcDataCollectionConfig.class, shippedXml(file));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String shippedXml(final String file) throws IOException {
        final File f = ConfigurationTestUtils.getFileForConfigFile(file);
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    private static Attachment attachment(final String body, final String fileName) {
        final Attachment attachment = mock(Attachment.class);
        final InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        when(attachment.getObject(InputStream.class)).thenReturn(stream);
        when(attachment.getContentDisposition()).thenReturn(new ContentDisposition("form-data; name=\"upload\"; filename=\"" + fileName + "\""));
        return attachment;
    }

    private static Collector collector(final String service, final String className) {
        final Collector collector = new Collector();
        collector.setService(service);
        collector.setClassName(className);
        return collector;
    }

    private static Service service(final String name, final String collection) {
        final Parameter parameter = new Parameter();
        parameter.setKey("collection");
        parameter.setValue(collection);
        final Service service = new Service();
        service.setName(name);
        service.setParameters(List.of(parameter));
        return service;
    }

    private static Package pkg(final String name, final Service... services) {
        final Package pkg = new Package();
        pkg.setName(name);
        pkg.setServices(List.of(services));
        return pkg;
    }

    private static SecurityContext adminContext() {
        final SecurityContext context = mock(SecurityContext.class);
        when(context.isUserInRole(Authentication.ROLE_ADMIN)).thenReturn(true);
        return context;
    }

    private static SecurityContext userContext() {
        return mock(SecurityContext.class);
    }

    private static void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
