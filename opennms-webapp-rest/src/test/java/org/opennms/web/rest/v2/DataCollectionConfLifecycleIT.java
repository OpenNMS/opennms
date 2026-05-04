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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.support.SnmpDataCollectionConfigLoader;
import org.opennms.netmgt.model.SnmpCollectionProfile;
import org.opennms.netmgt.model.SnmpCollectionSource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.v2.api.DataCollectionConfRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * End-to-end lifecycle test for the SNMP data collection REST API.
 *
 * <p>Each scenario drives the production REST endpoints (no JDBC shortcuts),
 * which invoke {@link SnmpDataCollectionConfigLoader#scheduleDataCollectionConfigReload()}
 * after every mutation. The reload runs asynchronously on the loader's
 * single-threaded executor; we use {@code awaitility} to poll the in-memory
 * {@link DataCollectionConfigDao} until it reflects the post-reload state.
 *
 * <p>The first scenario uploads the entire shipped baseline
 * ({@code etc/datacollection-config.xml} + every file in {@code etc/datacollection})
 * and asserts equivalence with what the legacy XML loader would produce
 * (using {@link DatacollectionConfigEquivalence}, the same comparator the
 * opennms-dao IT uses). Subsequent scenarios mutate the post-upload state
 * (delete, add-to-profile, enable/disable) and assert the corresponding
 * in-memory transitions.
 */
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
// Intentionally NOT @Transactional: the loader's reload runs on a separate
// thread (single-threaded executor in SnmpDataCollectionConfigLoader) and
// reads from its own connection. Wrapping the test in a transaction means
// the reload thread can't see writes made by REST mutations until commit,
// so awaitility times out. Each REST method handles its own transactions.
public class DataCollectionConfLifecycleIT {

    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final File BASELINE_ETC = REPO_ROOT.resolve("opennms-base-assembly/src/main/filtered/etc").toFile();
    private static final File BASELINE_DC_XML = new File(BASELINE_ETC, "datacollection-config.xml");
    private static final File BASELINE_DC_DIR = new File(BASELINE_ETC, "datacollection");

    private static final Duration RELOAD_TIMEOUT = Duration.ofSeconds(20);

    @Autowired private DataCollectionConfRestApi rest;
    @Autowired private SnmpCollectionSourceDao sourceDao;
    @Autowired private SnmpCollectionProfileDao profileDao;
    @Autowired private DataCollectionConfigDao dataCollectionConfigDao;
    @Autowired private SnmpDataCollectionConfigLoader loader;

    private SecurityContext securityContext;

    @Before
    public void setUp() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("lifecycle-IT");
        securityContext = mock(SecurityContext.class);
        when(securityContext.getUserPrincipal()).thenReturn(principal);
    }

    /**
     * Upload the entire shipped baseline via {@code POST /upload} (the
     * production REST entry point) and assert the resulting in-memory
     * {@link DatacollectionConfig} is equivalent to what the legacy XML
     * loader produces from the same files.
     */
    @Test
    public void bulkUploadShippedBaseline_inMemoryMatchesXmlLoader() throws Exception {
        uploadBaseline();
        // Build the XML-side reference DAO from the same files, and assert
        // runtime parity at the merged-state and DAO API levels.
        final DefaultDataCollectionConfigDao xmlDao = buildXmlReferenceDao(BASELINE_DC_XML, BASELINE_DC_DIR);
        DatacollectionConfigEquivalence.assertEquivalent(
                xmlDao.getRootDataCollection(), dataCollectionConfigDao.getRootDataCollection());
        DatacollectionConfigEquivalence.assertDaoApiEquivalent(xmlDao, dataCollectionConfigDao);
    }

    /**
     * Deleting a source that no profile references is a runtime no-op:
     * the source isn't pulled into the in-memory config in the first place,
     * so deleting it from the DB shouldn't change what collectd sees.
     */
    @Test
    public void deleteUnreferencedSource_doesNotChangeInMemoryConfig() throws Exception {
        uploadBaseline();
        final SnmpCollectionSource unreferenced = pickUnreferencedSource();
        final Set<String> beforeGroups = inMemoryGroupNames();

        final Response resp = rest.deleteSnmpDataCollectionSources(
                List.of(unreferenced.getId()), securityContext);
        assertEquals("Delete should succeed", 200, resp.getStatus());

        // The source is gone from the DB; the loader's reload finds nothing
        // new to materialize. Wait for the DB commit to be visible, then
        // ensure the in-memory snapshot stays equal to before for a window —
        // this catches reloads that *would* have changed in-memory state.
        await().atMost(RELOAD_TIMEOUT)
                .pollInterval(Duration.ofMillis(200))
                .until(() -> sourceDao.findByName(unreferenced.getName()) == null);
        await().pollDelay(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(3))
                .until(() -> inMemoryGroupNames().equals(beforeGroups));
        assertEquals("In-memory groups should be unchanged after deleting an unreferenced source",
                beforeGroups, inMemoryGroupNames());
    }

    /**
     * Adding a source to a profile's source_names brings that source's
     * groups into memory after the reload completes.
     */
    @Test
    public void addSourceToProfile_loadsItIntoMemoryAfterReload() throws Exception {
        uploadBaseline();
        final SnmpCollectionSource unreferenced = pickUnreferencedSource();
        final Set<String> beforeGroups = inMemoryGroupNames();
        final Set<String> sourceGroups = expectedGroupNamesForSource(unreferenced.getName());
        assertTrue("Test fixture sanity: chosen unreferenced source should have at least one group",
                !sourceGroups.isEmpty());

        // Default profile is the conventional one most baselines reference.
        final SnmpCollectionProfile defaultProfile = profileDao.findByName("default");
        assertNotNull("'default' profile should exist after upload", defaultProfile);

        final Response resp = rest.addSourceToProfile(
                defaultProfile.getId(), unreferenced.getName(), securityContext);
        assertEquals("Add-source-to-profile should succeed", 200, resp.getStatus());

        // Reload publishes the new state asynchronously — poll until the
        // newly-pulled-in groups appear in memory.
        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> inMemoryGroupNames().containsAll(sourceGroups));
        assertTrue("Added source's groups should now be present in memory",
                inMemoryGroupNames().containsAll(sourceGroups));
        assertTrue("Pre-existing in-memory groups should still be present",
                inMemoryGroupNames().containsAll(beforeGroups));
    }

    /**
     * Disabling a referenced source removes its groups from memory after
     * reload; re-enabling it restores them.
     */
    @Test
    public void disableThenEnableReferencedSource_togglesInMemoryPresence() throws Exception {
        uploadBaseline();
        final SnmpCollectionSource referenced = pickReferencedSource();
        final Set<String> sourceGroups = expectedGroupNamesForSource(referenced.getName());
        assertTrue("Test fixture sanity: chosen referenced source should have at least one group",
                !sourceGroups.isEmpty());
        assertTrue("Pre-condition: referenced source's groups present in memory",
                inMemoryGroupNames().containsAll(sourceGroups));

        // Disable.
        final Response disableResp = rest.enableDisableSnmpDataCollectionSources(
                false, List.of(referenced.getId()), securityContext);
        assertEquals("Disable should succeed", 200, disableResp.getStatus());

        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> inMemoryGroupNames().stream().noneMatch(sourceGroups::contains));

        // Re-enable.
        final Response enableResp = rest.enableDisableSnmpDataCollectionSources(
                true, List.of(referenced.getId()), securityContext);
        assertEquals("Re-enable should succeed", 200, enableResp.getStatus());

        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> inMemoryGroupNames().containsAll(sourceGroups));
    }

    /**
     * After upload, the {@code /config/download} and
     * {@code /collectsources/{id}/download} endpoints must return XML that
     * (a) re-parses cleanly via the same JAXB types the upload path uses,
     * and (b) re-uploads cleanly into a wiped DB and produces an in-memory
     * config equivalent to the original.
     *
     * <p>This is the operator-visible round-trip the migration enables: pull
     * the post-migration state out as XML, edit, push back via {@code /upload}.
     */
    @Test
    public void downloadConfigAndSources_roundTripsThroughUpload() throws Exception {
        uploadBaseline();

        // Snapshot the baseline in-memory state for comparison after re-upload.
        final java.util.Map<String, Set<String>> beforePerCollection = perCollectionGroupNames();
        assertTrue("Pre-condition: baseline produced at least one user collection",
                !beforePerCollection.isEmpty());

        // 1) GET /config/download — assemble main config from DB.
        final Response cfgResp = rest.downloadDatacollectionConfig("xml", securityContext);
        assertEquals("Config download should return 200", 200, cfgResp.getStatus());
        final byte[] configBytes = (byte[]) cfgResp.getEntity();
        // Sanity: re-parses as DatacollectionConfig and has expected shape.
        final org.opennms.netmgt.config.datacollection.DatacollectionConfig reparsed =
                org.opennms.core.xml.JaxbUtils.unmarshal(
                        org.opennms.netmgt.config.datacollection.DatacollectionConfig.class,
                        new String(configBytes, java.nio.charset.StandardCharsets.UTF_8));
        assertNotNull(reparsed);
        assertTrue("Re-parsed config should have at least one snmp-collection",
                reparsed.getSnmpCollections() != null && !reparsed.getSnmpCollections().isEmpty());

        // 2) GET /collectsources/{id}/download for every source in the DB.
        final java.util.Map<String, byte[]> sourceXmlBySourceName = new java.util.LinkedHashMap<>();
        for (final SnmpCollectionSource src : sourceDao.findAll()) {
            final Response srcResp = rest.downloadSnmpDataCollectionById(src.getId(), "xml", securityContext);
            assertEquals("Source download for '" + src.getName() + "' should return 200",
                    200, srcResp.getStatus());
            sourceXmlBySourceName.put(src.getName(), (byte[]) srcResp.getEntity());
        }
        assertEquals("Should have downloaded one XML per source",
                sourceDao.findAll().size(), sourceXmlBySourceName.size());

        // 3) Wipe the DB so the re-upload starts from empty state. Delete in
        //    dependency order: profiles first (so source_names FKs don't
        //    block sources), then sources.
        rest.deleteSnmpCollectionProfiles(
                profileDao.findAll().stream().map(SnmpCollectionProfile::getId).collect(Collectors.toList()),
                securityContext);
        rest.deleteSnmpDataCollectionSources(
                sourceDao.findAll().stream().map(SnmpCollectionSource::getId).collect(Collectors.toList()),
                securityContext);
        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> sourceDao.findAll().isEmpty() && profileDao.findAll().isEmpty());

        // 4) Re-upload: the downloaded config + every source XML in one batch.
        final List<Attachment> attachments = new ArrayList<>();
        attachments.add(byteAttachment("datacollection-config.xml", configBytes));
        for (final var entry : sourceXmlBySourceName.entrySet()) {
            attachments.add(byteAttachment(entry.getKey() + ".xml", entry.getValue()));
        }
        final Response reuploadResp = rest.uploadSnmpDataCollectionConfFiles(
                attachments, null, securityContext);
        assertEquals("Re-upload should succeed", 200, reuploadResp.getStatus());

        // 5) Wait for the post-reupload reload to publish, then assert the
        //    per-collection group sets match the original snapshot. This is
        //    the round-trip equivalence — what collectd will collect after a
        //    download/edit/upload cycle is unchanged from before.
        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> perCollectionGroupNames().equals(beforePerCollection));
        assertEquals("Round-trip via /config/download + /collectsources/{id}/download + /upload must "
                        + "produce the same per-collection group composition",
                beforePerCollection, perCollectionGroupNames());
    }

    /**
     * Deleting a referenced source removes its groups from memory after
     * reload (the loader simply doesn't find the source row when materializing).
     */
    @Test
    public void deleteReferencedSource_removesItFromMemoryAfterReload() throws Exception {
        uploadBaseline();
        final SnmpCollectionSource referenced = pickReferencedSource();
        final Set<String> sourceGroups = expectedGroupNamesForSource(referenced.getName());
        assertTrue("Pre-condition: referenced source's groups present in memory",
                inMemoryGroupNames().containsAll(sourceGroups));

        final Response resp = rest.deleteSnmpDataCollectionSources(
                List.of(referenced.getId()), securityContext);
        assertEquals("Delete should succeed", 200, resp.getStatus());

        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> inMemoryGroupNames().stream().noneMatch(sourceGroups::contains));
    }

    // ─── Test helpers ──────────────────────────────────────────────────

    /**
     * Upload the baseline and block until the in-memory config has been
     * republished from the DB. Used by every scenario as the common setup.
     */
    private void uploadBaseline() throws Exception {
        final Response uploadResp = rest.uploadSnmpDataCollectionConfFiles(
                buildBaselineAttachments(), null, securityContext);
        assertEquals("Baseline upload should succeed", 200, uploadResp.getStatus());

        // Wait for the async reload to publish a non-empty config. The
        // baseline produces 2 user collections (default, ejn) plus the
        // synthetic __resource_type_collection holder — total >= 1.
        await().atMost(RELOAD_TIMEOUT).pollInterval(Duration.ofMillis(200))
                .until(() -> {
                    final DatacollectionConfig cfg = dataCollectionConfigDao.getRootDataCollection();
                    if (cfg == null || cfg.getSnmpCollections() == null) return false;
                    // At least one user-named (non-synthetic) collection has groups.
                    return cfg.getSnmpCollections().stream()
                            .filter(sc -> !"__resource_type_collection".equals(sc.getName()))
                            .anyMatch(sc -> sc.getGroups() != null
                                    && sc.getGroups().getGroups() != null
                                    && !sc.getGroups().getGroups().isEmpty());
                });
    }

    /** Pick any source name that isn't in any profile's source_names. */
    private SnmpCollectionSource pickUnreferencedSource() {
        final Set<String> referenced = profileDao.findAll().stream()
                .filter(p -> p.getSourceNames() != null)
                .flatMap(p -> parseJsonStringList(p.getSourceNames()).stream())
                .collect(Collectors.toCollection(TreeSet::new));
        return sourceDao.findAll().stream()
                .filter(s -> !referenced.contains(s.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Baseline has no unreferenced sources — fixture must contain at least one."));
    }

    /** Pick any source name that IS in some profile's source_names. */
    private SnmpCollectionSource pickReferencedSource() {
        final Set<String> referenced = profileDao.findAll().stream()
                .filter(p -> p.getSourceNames() != null)
                .flatMap(p -> parseJsonStringList(p.getSourceNames()).stream())
                .collect(Collectors.toCollection(TreeSet::new));
        return sourceDao.findAll().stream()
                .filter(s -> referenced.contains(s.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Baseline has no referenced sources — fixture broken."));
    }

    /**
     * Resolve the set of MIB-group names that the given source declares,
     * straight from the on-disk XML. This is the runtime-irrelevant disk
     * truth — the in-memory side may show a subset (systemDef-reachable),
     * but for the purpose of "did the upload bring this source's content in"
     * a non-empty intersection is enough to assert presence.
     */
    private Set<String> expectedGroupNamesForSource(final String sourceName) {
        for (final File f : sortedXmlFiles(BASELINE_DC_DIR)) {
            try {
                final var dcg = org.opennms.core.xml.JaxbUtils.unmarshal(
                        org.opennms.netmgt.config.datacollection.DatacollectionGroup.class,
                        new FileSystemResource(f));
                if (sourceName.equals(dcg.getName())) {
                    final Set<String> out = new TreeSet<>();
                    if (dcg.getGroups() != null) {
                        for (final Group g : dcg.getGroups()) {
                            if (g.getName() != null) out.add(g.getName());
                        }
                    }
                    return out;
                }
            } catch (Exception ignored) {
                // skip files that don't parse as datacollection-group
            }
        }
        return Set.of();
    }

    /**
     * Profile.source_names is stored as JSON. Use the same Jackson mapper
     * the rest of the persistence layer uses (via DatacollectionJsonHelper)
     * to keep parsing canonical.
     */
    private static List<String> parseJsonStringList(final String json) {
        if (json == null || json.isBlank()) return List.of();
        return org.opennms.netmgt.config.api.DatacollectionJsonHelper.fromJson(
                json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
    }

    // ─── Multipart fixture helpers ─────────────────────────────────────

    private List<Attachment> buildBaselineAttachments() {
        final List<Attachment> out = new ArrayList<>();
        out.add(fileAttachment(BASELINE_DC_XML));
        for (final File f : sortedXmlFiles(BASELINE_DC_DIR)) {
            out.add(fileAttachment(f));
        }
        return out;
    }

    private static File[] sortedXmlFiles(final File dir) {
        final File[] xml = dir.listFiles((d, name) -> name.endsWith(".xml"));
        assertNotNull("baseline datacollection dir not found: " + dir, xml);
        java.util.Arrays.sort(xml);
        return xml;
    }

    /**
     * In-memory equivalent of {@link #fileAttachment} for the round-trip
     * test, where the bytes come back from a download response rather than
     * a disk file.
     */
    private static Attachment byteAttachment(final String filename, final byte[] bytes) {
        final InputStream is = new java.io.ByteArrayInputStream(bytes);
        final Attachment att = mock(Attachment.class);
        final ContentDisposition cd = mock(ContentDisposition.class);
        when(cd.getParameter("filename")).thenReturn(filename);
        when(att.getContentDisposition()).thenReturn(cd);
        when(att.getObject(InputStream.class)).thenReturn(is);
        return att;
    }

    /**
     * Build a CXF {@link Attachment} mock that the upload handler reads via
     * {@code getObject(InputStream.class)} and {@code getContentDisposition().getParameter("filename")}.
     * Mirrors the helper used by {@link DataCollectionConfRestServiceIT}.
     */
    private static Attachment fileAttachment(final File file) {
        try {
            // CXF reads the InputStream once, so each test call needs a fresh stream.
            final InputStream is = new FileInputStream(file);
            final Attachment att = mock(Attachment.class);
            final ContentDisposition cd = mock(ContentDisposition.class);
            when(cd.getParameter("filename")).thenReturn(file.getName());
            when(att.getContentDisposition()).thenReturn(cd);
            when(att.getObject(InputStream.class)).thenReturn(is);
            return att;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build attachment for " + file, e);
        }
    }

    // ─── XML reference loader ──────────────────────────────────────────

    /**
     * Construct a fresh {@link DefaultDataCollectionConfigDao} populated
     * directly from the on-disk XML files. This is the legacy load path —
     * the parity assertion compares it against the post-upload state of the
     * autowired (DB-backed) DAO.
     */
    private static DefaultDataCollectionConfigDao buildXmlReferenceDao(final File configXml, final File dcDir) {
        final DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        dao.setConfigResource(new FileSystemResource(configXml));
        dao.setConfigDirectory(dcDir.getAbsolutePath());
        dao.afterPropertiesSet();
        return dao;
    }

    /**
     * Snapshot the in-memory state per user snmp-collection: collection name
     * → set of group names. Excludes the synthetic
     * {@code __resource_type_collection} since it isn't a runtime user
     * collection. Used by the round-trip test to assert "what each
     * snmp-collection produces" is preserved across download/upload.
     */
    private java.util.Map<String, Set<String>> perCollectionGroupNames() {
        final java.util.Map<String, Set<String>> out = new java.util.TreeMap<>();
        final org.opennms.netmgt.config.datacollection.DatacollectionConfig cfg =
                dataCollectionConfigDao.getRootDataCollection();
        if (cfg == null || cfg.getSnmpCollections() == null) return out;
        for (final SnmpCollection sc : cfg.getSnmpCollections()) {
            if (sc == null || sc.getName() == null) continue;
            if ("__resource_type_collection".equals(sc.getName())) continue;
            final Set<String> groups = new TreeSet<>();
            if (sc.getGroups() != null && sc.getGroups().getGroups() != null) {
                for (final Group g : sc.getGroups().getGroups()) {
                    if (g.getName() != null) groups.add(g.getName());
                }
            }
            out.put(sc.getName(), groups);
        }
        return out;
    }

    /** Snapshot of every group name visible in the in-memory config, across all snmp-collections. */
    private Set<String> inMemoryGroupNames() {
        final Set<String> out = new TreeSet<>();
        final DatacollectionConfig cfg = dataCollectionConfigDao.getRootDataCollection();
        if (cfg == null || cfg.getSnmpCollections() == null) return out;
        for (final SnmpCollection sc : cfg.getSnmpCollections()) {
            if (sc.getGroups() == null || sc.getGroups().getGroups() == null) continue;
            for (final Group g : sc.getGroups().getGroups()) {
                if (g.getName() != null) out.add(g.getName());
            }
        }
        return out;
    }
}
