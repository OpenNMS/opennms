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
package org.opennms.netmgt.dao.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.config.upgrade.datacollection.SnmpDataCollectionMigration;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.dao.api.SnmpCollectionMibGroupDao;
import org.opennms.netmgt.dao.api.SnmpCollectionProfileDao;
import org.opennms.netmgt.dao.api.SnmpCollectionResourceTypeDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSourceDao;
import org.opennms.netmgt.dao.api.SnmpCollectionSystemDefDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.FileSystemUtils;

/**
 * End-to-end parity check between the legacy XML-driven SNMP data collection
 * config loader and the DB-driven loader that replaces it on upgrade.
 *
 * <p>For each fixture, the same {@code datacollection-config.xml} +
 * {@code datacollection/*.xml} tree is fed through both paths:
 *
 * <ol>
 *   <li>{@link DefaultDataCollectionConfigDao} parses the XML directly →
 *       {@code fromXml}.</li>
 *   <li>{@link SnmpDataCollectionMigration#execute(Connection)} writes the same
 *       tree into the {@code snmp_collection_*} tables, then
 *       {@link SnmpDataCollectionConfigLoader#materializeFromDb()} reads it back
 *       → {@code fromDb}.</li>
 * </ol>
 *
 * <p>{@link DatacollectionConfigEquivalence#assertEquivalent} then compares the
 * two configs at the merged-state level (what each {@code <snmp-collection>}
 * actually exposes to collectd). A mismatch fails the test with a structured
 * field-path diff so a CI failure points directly at the divergent field.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockEventForwarder.xml",
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=osgi")
@JUnitTemporaryDatabase
public class SnmpDataCollectionXmlToDbParityIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    /**
     * Repo root, relative to the {@code opennms-dao} module's working dir
     * (Maven runs from the module's pom directory).
     */
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();

    private static final Path BASELINE_ETC = REPO_ROOT.resolve("opennms-base-assembly/src/main/filtered/etc");
    private static final Path BASELINE_DC_DIR = BASELINE_ETC.resolve("datacollection");
    private static final Path FIXTURE_DIR =
            REPO_ROOT.resolve("opennms-config/src/test/resources/org/opennms/netmgt/config");
    /**
     * Smaller, internally-consistent {@code datacollection/*.xml} set used by
     * the small-fixture tests. Paired with the matching small main-config
     * files so resource types referenced by the group XML are actually
     * declared in the {@code <resource-types>} section.
     */
    private static final Path PARSER_TEST_DC_DIR = FIXTURE_DIR.resolve("datacollection-config-parser-test/datacollection");

    @Autowired private SnmpCollectionProfileDao profileDao;
    @Autowired private SnmpCollectionSourceDao sourceDao;
    @Autowired private SnmpCollectionResourceTypeDao resourceTypeDao;
    @Autowired private SnmpCollectionMibGroupDao mibGroupDao;
    @Autowired private SnmpCollectionSystemDefDao systemDefDao;

    private DataSource dataSource;
    private Path opennmsHome;
    private String opennmsHomeOrig;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        this.dataSource = database;
    }

    @Before
    public void setUp() throws Exception {
        opennmsHomeOrig = System.getProperty("opennms.home");
        opennmsHome = Files.createTempDirectory("snmp-parity-IT-");
        System.setProperty("opennms.home", opennmsHome.toString());
        Files.createDirectories(opennmsHome.resolve("etc"));

        // @JUnitTemporaryDatabase gives each method a fresh DB, but the
        // Spring context is cached across methods, so the autowired DAOs
        // can hold a session pointing at the previous test's DB. Truncate
        // explicitly so the migration's emptiness check is deterministic.
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM snmp_collection_mib_groups");
            st.execute("DELETE FROM snmp_collection_resource_types");
            st.execute("DELETE FROM snmp_collection_systemdefs");
            st.execute("DELETE FROM snmp_collection_profiles");
            st.execute("DELETE FROM snmp_collection_sources");
        }
    }

    @After
    public void tearDown() {
        if (opennmsHomeOrig == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", opennmsHomeOrig);
        }
        if (opennmsHome != null) {
            FileSystemUtils.deleteRecursively(opennmsHome.toFile());
        }
    }

    /**
     * The ~80-source production tree shipped with the assembly. The most
     * realistic check; catches regressions against the actual content
     * operators run.
     */
    @Test
    public void shippedBaselineProducesEquivalentConfig() throws Exception {
        stage(BASELINE_ETC.resolve("datacollection-config.xml"), BASELINE_DC_DIR);
        runParityCheck();
    }

    /**
     * Plain {@code <include-collection dataCollectionGroup="...">} only —
     * the most common path.
     */
    @Test
    public void plainIncludesProduceEquivalentConfig() throws Exception {
        stage(FIXTURE_DIR.resolve("datacollection-config-onlyimports.xml"), PARSER_TEST_DC_DIR);
        runParityCheck();
    }

    // Edge-case fixtures (datacollection-config-excludes.xml and
    // datacollection-config-single-systemdef.xml) are intentionally not
    // exercised here yet: their <resource-types> sections are empty, so
    // they can't be paired with the parser-test datacollection/*.xml files
    // (which reference resource types those fixtures don't declare). The
    // shipped baseline already exercises both <exclude-filter> and
    // systemDef= paths against the production data, so coverage is not
    // lost. Adding tailored synthetic group XML so the small fixtures
    // can stand alone is a follow-up.

    // ─── Fixture helpers ───────────────────────────────────────────────

    private void stage(final Path mainConfig, final Path datacollectionDir) throws IOException {
        Files.copy(mainConfig,
                opennmsHome.resolve("etc/datacollection-config.xml"),
                StandardCopyOption.REPLACE_EXISTING);
        final Path destDir = Files.createDirectories(opennmsHome.resolve("etc/datacollection"));
        try (Stream<Path> files = Files.list(datacollectionDir)) {
            files.filter(p -> p.getFileName().toString().endsWith(".xml"))
                 .forEach(p -> {
                     try {
                         Files.copy(p, destDir.resolve(p.getFileName()),
                                 StandardCopyOption.REPLACE_EXISTING);
                     } catch (IOException e) {
                         throw new UncheckedIOException(e);
                     }
                 });
        }
    }

    // ─── Parity orchestration ──────────────────────────────────────────

    private void runParityCheck() throws Exception {
        final DefaultDataCollectionConfigDao xmlDao = loadFromXml();

        try (Connection conn = dataSource.getConnection()) {
            final boolean migrated = new SnmpDataCollectionMigration().execute(conn);
            if (!migrated) {
                throw new AssertionError("Migration declined to run — DB likely already populated, "
                        + "indicating a temp-DB isolation bug.");
            }
        }

        // 1) Write-path completeness: every *.xml in etc/datacollection/
        //    must land as a row in snmp_collection_sources. The migration
        //    dedupes by <datacollection-group name> via LinkedHashMap.put
        //    and swallows per-file parse exceptions (logs only), so two
        //    files with the same group name or one with malformed XML
        //    would silently reduce the source count. Assert it explicitly.
        assertAllSourcesPersisted(opennmsHome.resolve("etc/datacollection"));

        final DefaultDataCollectionConfigDao dbDao = loadFromDb();
        if (dbDao == null) {
            throw new AssertionError("DB loader produced no config — migration must have inserted "
                    + "no profiles, but the XML side has "
                    + xmlDao.getRootDataCollection().getSnmpCollections().size()
                    + " snmp-collection(s).");
        }

        // 2) Referenced-source coverage: every <include-collection
        //    dataCollectionGroup="X"> in the original config must be
        //    actually pulled in on both sides. Without this check, both
        //    paths could silently agree on "X contributed nothing"
        //    (e.g., file misnamed, parse failed, group name typo) and
        //    the per-collection content comparison would still pass.
        assertReferencedSourcesProcessed(xmlDao, dbDao);

        // 3) In-memory shape parity (systemDef-reachable view).
        DatacollectionConfigEquivalence.assertEquivalent(
                xmlDao.getRootDataCollection(), dbDao.getRootDataCollection());

        // 4) Public DAO API parity — what collectd actually calls.
        DatacollectionConfigEquivalence.assertDaoApiEquivalent(xmlDao, dbDao);
    }

    private void assertReferencedSourcesProcessed(final DefaultDataCollectionConfigDao xmlDao,
                                                  final DefaultDataCollectionConfigDao dbDao) throws Exception {
        // Extract the set of dataCollectionGroup= references from the raw
        // snmp-collection blocks in the staged datacollection-config.xml.
        // (We can't read them off the post-load DAO because the XML loader
        // erases <include-collection> entries during translateConfig.)
        final var rawConfig = org.opennms.core.xml.JaxbUtils.unmarshal(
                org.opennms.netmgt.config.datacollection.DatacollectionConfig.class,
                new FileSystemResource(opennmsHome.resolve("etc/datacollection-config.xml").toFile()));
        final Set<String> referenced = new TreeSet<>();
        for (final var sc : rawConfig.getSnmpCollections()) {
            if (sc == null || sc.getIncludeCollections() == null) continue;
            for (final var ic : sc.getIncludeCollections()) {
                if (ic.getDataCollectionGroup() != null && !ic.getDataCollectionGroup().isBlank()) {
                    referenced.add(ic.getDataCollectionGroup());
                }
            }
        }
        if (referenced.isEmpty()) {
            // No dataCollectionGroup= refs in this fixture (e.g., a
            // systemDef-only fixture). Nothing to assert here.
            return;
        }

        // XML side: getAvailableDataCollectionGroups() returns the names
        // of every file the parser successfully unmarshaled. A reference
        // to a missing/misnamed group never lands here.
        final Set<String> xmlAvailable = new TreeSet<>(xmlDao.getAvailableDataCollectionGroups());
        final TreeSet<String> missingFromXml = new TreeSet<>(referenced);
        missingFromXml.removeAll(xmlAvailable);

        // DB side: every referenced source must have a row in
        // snmp_collection_sources (the migration writes one row per parsed
        // file, keyed on <datacollection-group name>).
        final Set<String> dbSources = new TreeSet<>();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM snmp_collection_sources")) {
            while (rs.next()) dbSources.add(rs.getString(1));
        }
        final TreeSet<String> missingFromDb = new TreeSet<>(referenced);
        missingFromDb.removeAll(dbSources);

        if (!missingFromXml.isEmpty() || !missingFromDb.isEmpty()) {
            throw new AssertionError("Referenced datacollection-group(s) were not actually loaded:\n"
                    + "  referenced in datacollection-config.xml: " + referenced + "\n"
                    + "  missing from XML side (externalGroupsMap): " + missingFromXml + "\n"
                    + "  missing from DB (snmp_collection_sources): " + missingFromDb);
        }
    }

    private void assertAllSourcesPersisted(final Path datacollectionDir) throws Exception {
        // Count *.xml files on disk and the distinct group names they declare.
        final List<Path> xmlFiles;
        try (Stream<Path> s = Files.list(datacollectionDir)) {
            xmlFiles = s.filter(p -> p.getFileName().toString().endsWith(".xml"))
                        .sorted()
                        .toList();
        }
        final Set<String> groupNamesOnDisk = new TreeSet<>();
        final List<String> parseFailures = new ArrayList<>();
        for (final Path p : xmlFiles) {
            try {
                final var dcg = org.opennms.core.xml.JaxbUtils.unmarshal(
                        org.opennms.netmgt.config.datacollection.DatacollectionGroup.class,
                        new FileSystemResource(p.toFile()));
                if (dcg.getName() == null || dcg.getName().isBlank()) {
                    parseFailures.add(p.getFileName() + " (no <datacollection-group name=\"...\">)");
                } else if (!groupNamesOnDisk.add(dcg.getName())) {
                    parseFailures.add(p.getFileName() + " (duplicate group name '" + dcg.getName() + "')");
                }
            } catch (Exception e) {
                parseFailures.add(p.getFileName() + " (parse error: " + e.getMessage() + ")");
            }
        }
        if (!parseFailures.isEmpty()) {
            // Surface these as part of the parity contract: the migration
            // would silently drop them, leaving operator-visible XML files
            // with no DB representation. Better to fail loudly here.
            throw new AssertionError("etc/datacollection/ has files the migration would silently skip:\n  - "
                    + String.join("\n  - ", parseFailures));
        }

        // Now confirm every group name on disk has a matching DB row, and
        // there's nothing in the DB that didn't come from disk.
        final Set<String> groupNamesInDb = new TreeSet<>();
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM snmp_collection_sources")) {
            while (rs.next()) {
                groupNamesInDb.add(rs.getString(1));
            }
        }
        final TreeSet<String> diskOnly = new TreeSet<>(groupNamesOnDisk);
        diskOnly.removeAll(groupNamesInDb);
        final TreeSet<String> dbOnly = new TreeSet<>(groupNamesInDb);
        dbOnly.removeAll(groupNamesOnDisk);
        if (!diskOnly.isEmpty() || !dbOnly.isEmpty()) {
            throw new AssertionError("snmp_collection_sources does not match etc/datacollection/ contents.\n"
                    + "  on disk but not in DB: " + diskOnly + "\n"
                    + "  in DB but not on disk: " + dbOnly);
        }
    }

    private DefaultDataCollectionConfigDao loadFromXml() {
        // Materialize the XML side directly (don't use the autowired
        // dataCollectionConfigDao bean — it was init'd against an empty etc
        // dir). Constructing a fresh DAO per test keeps the comparison
        // hermetic.
        final Path etcDir = opennmsHome.resolve("etc");
        final DefaultDataCollectionConfigDao xmlDao = new DefaultDataCollectionConfigDao();
        xmlDao.setConfigResource(new FileSystemResource(
                etcDir.resolve("datacollection-config.xml").toFile()));
        xmlDao.setConfigDirectory(etcDir.resolve("datacollection").toString());
        xmlDao.afterPropertiesSet();
        return xmlDao;
    }

    private DefaultDataCollectionConfigDao loadFromDb() {
        // Use a fresh DAO populated via the production publish path
        // (loadFromDatabase) so the API surface — getMibObjectList,
        // getConfiguredResourceTypes, getSnmpStorageFlag, etc. — exercises
        // the same code path collectd hits in production.
        final DefaultDataCollectionConfigDao dbDao = new DefaultDataCollectionConfigDao();
        dbDao.afterPropertiesSet();

        final SnmpDataCollectionConfigLoader loader = new SnmpDataCollectionConfigLoader();
        loader.setSnmpCollectionProfileDao(profileDao);
        loader.setSnmpCollectionSourceDao(sourceDao);
        loader.setSnmpCollectionResourceTypeDao(resourceTypeDao);
        loader.setSnmpCollectionMibGroupDao(mibGroupDao);
        loader.setSnmpCollectionSystemDefDao(systemDefDao);
        loader.setDataCollectionConfigDao(dbDao);

        final SnmpDataCollectionConfigLoader.MaterializedConfig m = loader.materializeFromDb();
        if (m == null) return null;
        dbDao.loadFromDatabase(m.config, m.allResourceTypes, m.allGroups);
        return dbDao;
    }
}
