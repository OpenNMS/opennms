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
package org.opennms.config.upgrade.datacollection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.TemporaryDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.TemporaryDatabaseExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * DB-level tests for {@link SnmpDataCollectionDefaultsUpdate}: ledger
 * semantics, fragment application to a migrated database, the
 * no-resurrection guarantees, and fragment archival.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({TemporaryDatabaseExecutionListener.class})
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"})
@JUnitTemporaryDatabase
public class SnmpDataCollectionDefaultsUpdateIT implements TemporaryDatabaseAware<TemporaryDatabase> {

    private TemporaryDatabase database;
    private Connection connection;
    private File tempHome;
    private String previousHome;

    @Override
    public void setTemporaryDatabase(final TemporaryDatabase database) {
        this.database = database;
    }

    @Before
    public void setUp() throws Exception {
        connection = database.getConnection();
        connection.setAutoCommit(false);
        tempHome = Files.createTempDirectory(getClass().getSimpleName()).toFile();
        previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tempHome.getAbsolutePath());
    }

    @After
    public void tearDown() throws SQLException {
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
        if (connection != null) {
            connection.rollback();
            connection.close();
        }
    }

    @Test
    public void appliesDeltaToMigratedDatabaseAndIsIdempotent() throws SQLException {
        seedMigratedDatabaseWithoutUcdMemoryX();

        new SnmpDataCollectionDefaultsUpdate().execute(connection);

        final int sourceId = SnmpDataCollectionSqlHelper.findSourceIdByName(connection, "Net-SNMP");
        assertEquals(1, countMibGroups(sourceId, "ucd-memory-X"));

        final String mibObjects = selectMibGroupObjects(sourceId, "ucd-memory-X");
        assertNotNull(mibObjects);
        assertTrue(mibObjects.contains("memAvailRealX"));
        assertTrue(mibObjects.contains(".1.3.6.1.4.1.2021.4.27"));

        final List<String> groupNames = SnmpDataCollectionDefaultsUpdate.parseStringArray(
                selectSystemDefGroupNames(sourceId, "Net-SNMP"));
        final int memIdx = groupNames.indexOf("ucd-memory");
        assertTrue(memIdx >= 0);
        assertEquals("ucd-memory-X inserted directly after the ucd-memory anchor",
                memIdx + 1, groupNames.indexOf("ucd-memory-X"));

        assertTrue(isLedgerRecorded("NMS-18291-ucd-memory-X"));
        assertEquals("Add 64-bit UCD-SNMP memory gauges (ucd-memory-X) to the Net-SNMP source and systemDef",
                selectLedgerDescription("NMS-18291-ucd-memory-X"));

        // Second run must not duplicate anything.
        new SnmpDataCollectionDefaultsUpdate().execute(connection);
        assertEquals(1, countMibGroups(sourceId, "ucd-memory-X"));
        final List<String> groupNamesAfter = SnmpDataCollectionDefaultsUpdate.parseStringArray(
                selectSystemDefGroupNames(sourceId, "Net-SNMP"));
        assertEquals(groupNames, groupNamesAfter);
    }

    @Test
    public void doesNotResurrectContentDeletedByAdmin() throws SQLException {
        seedMigratedDatabaseWithoutUcdMemoryX();

        new SnmpDataCollectionDefaultsUpdate().execute(connection);
        final int sourceId = SnmpDataCollectionSqlHelper.findSourceIdByName(connection, "Net-SNMP");
        assertEquals(1, countMibGroups(sourceId, "ucd-memory-X"));

        // Admin deletes the group and the systemDef reference.
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM snmp_collection_mib_groups WHERE collection_source_id = ? AND name = ?")) {
            ps.setInt(1, sourceId);
            ps.setString(2, "ucd-memory-X");
            ps.executeUpdate();
        }
        updateSystemDefGroupNames(sourceId, "Net-SNMP", "[\"ucd-memory\"]");

        // The ledger must prevent re-application.
        new SnmpDataCollectionDefaultsUpdate().execute(connection);
        assertEquals(0, countMibGroups(sourceId, "ucd-memory-X"));
        assertFalse(SnmpDataCollectionDefaultsUpdate.parseStringArray(
                selectSystemDefGroupNames(sourceId, "Net-SNMP")).contains("ucd-memory-X"));
    }

    /**
     * Anchor entries in the fragment stub (ucd-memory) must never be
     * (re-)added: if the admin removed the anchor itself, the new reference is
     * simply appended at the end.
     */
    @Test
    public void anchorEntriesAreNeverReAdded() throws SQLException {
        seedMigratedDatabaseWithoutUcdMemoryX();
        final int sourceId = SnmpDataCollectionSqlHelper.findSourceIdByName(connection, "Net-SNMP");
        // Admin removed ucd-memory before this upgrade.
        updateSystemDefGroupNames(sourceId, "Net-SNMP", "[\"mib2-interfaces\",\"ucd-sysstat\"]");

        new SnmpDataCollectionDefaultsUpdate().execute(connection);

        final List<String> groupNames = SnmpDataCollectionDefaultsUpdate.parseStringArray(
                selectSystemDefGroupNames(sourceId, "Net-SNMP"));
        assertFalse("removed anchor must not be resurrected", groupNames.contains("ucd-memory"));
        assertEquals("new reference appended at the end when no anchor is present",
                List.of("mib2-interfaces", "ucd-sysstat", "ucd-memory-X"), groupNames);
    }

    @Test
    public void recordsNoopWhenContentAlreadyPresent() throws SQLException {
        // Fresh-install shape: migration already imported the new XML.
        seedMigratedDatabaseWithoutUcdMemoryX();
        final int sourceId = SnmpDataCollectionSqlHelper.findSourceIdByName(connection, "Net-SNMP");
        final SnmpDataCollectionDefaultsUpdate update = new SnmpDataCollectionDefaultsUpdate();
        SnmpDataCollectionSqlHelper.batchInsertMibGroups(connection, sourceId, List.of(
                update.loadFragment(SnmpDataCollectionDefaultsUpdate.UPDATES.get(0).resourcePath).getGroups().get(0)));
        updateSystemDefGroupNames(sourceId, "Net-SNMP", "[\"ucd-memory\",\"ucd-memory-X\",\"ucd-sysstat\"]");

        update.execute(connection);

        assertEquals(1, countMibGroups(sourceId, "ucd-memory-X"));
        assertEquals(List.of("ucd-memory", "ucd-memory-X", "ucd-sysstat"),
                SnmpDataCollectionDefaultsUpdate.parseStringArray(selectSystemDefGroupNames(sourceId, "Net-SNMP")));
        assertTrue(isLedgerRecorded("NMS-18291-ucd-memory-X"));
    }

    @Test
    public void defersUntilMigrationHasPopulatedTables() throws SQLException {
        // No profiles seeded: the XML->DB migration hasn't produced data yet.
        assertFalse(new SnmpDataCollectionDefaultsUpdate().execute(connection));
        assertFalse(isLedgerRecorded("NMS-18291-ucd-memory-X"));
    }

    @Test
    public void recordsAppliedWhenSourceWasRemovedByAdmin() throws SQLException {
        insertProfile();
        // No Net-SNMP source at all — admin removed it. Update must be a no-op
        // but still be recorded so it never runs again.
        assertTrue(new SnmpDataCollectionDefaultsUpdate().execute(connection));
        assertTrue(isLedgerRecorded("NMS-18291-ucd-memory-X"));
        assertEquals(-1, SnmpDataCollectionSqlHelper.findSourceIdByName(connection, "Net-SNMP"));
    }

    /**
     * Applied fragments are mirrored to etc_archive/datacollection/updates/
     * for git-based etc tracking.
     */
    @Test
    public void archivesAppliedFragments() throws SQLException {
        seedMigratedDatabaseWithoutUcdMemoryX();

        final SnmpDataCollectionDefaultsUpdate update = new SnmpDataCollectionDefaultsUpdate();
        assertTrue(update.execute(connection));
        update.archiveAppliedFragments();

        final File archived = new File(tempHome,
                "etc_archive/" + SnmpDataCollectionDefaultsUpdate.ARCHIVE_UPDATES_DIR + "/nms-18291-ucd-memory-x.xml");
        assertTrue("fragment should be archived at " + archived, archived.exists());

        // Already-applied updates are not re-archived on the next run.
        final File marker = new File(archived.getParentFile(), "marker");
        assertTrue(archived.delete());
        final SnmpDataCollectionDefaultsUpdate secondRun = new SnmpDataCollectionDefaultsUpdate();
        assertFalse(secondRun.execute(connection));
        secondRun.archiveAppliedFragments();
        assertFalse("nothing applied -> nothing archived", archived.exists());
        assertFalse(marker.exists());
    }

    // --- seeding helpers ---

    /** Mimic a pre-NMS-18291 migrated database: profile + Net-SNMP source/systemDef, no ucd-memory-X. */
    private void seedMigratedDatabaseWithoutUcdMemoryX() throws SQLException {
        insertProfile();
        final int sourceId = SnmpDataCollectionSqlHelper.insertSource(
                connection, "Net-SNMP", "Net-SNMP", null, "system-migration");
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO snmp_collection_mib_groups "
                        + "(id, collection_source_id, name, if_type, mib_objects, enabled) "
                        + "VALUES (nextval('snmp_collection_mib_groups_id_seq'), ?, 'ucd-memory', 'ignore', '[]', true)")) {
            ps.setInt(1, sourceId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO snmp_collection_systemdefs "
                        + "(id, collection_source_id, name, sysoid_mask, mib_group_names, enabled) "
                        + "VALUES (nextval('snmp_collection_systemdefs_id_seq'), ?, 'Net-SNMP', '.1.3.6.1.4.1.8072.3.', ?, true)")) {
            ps.setInt(1, sourceId);
            ps.setString(2, "[\"mib2-interfaces\",\"ucd-memory\",\"ucd-sysstat\"]");
            ps.executeUpdate();
        }
    }

    private void insertProfile() throws SQLException {
        SnmpDataCollectionSqlHelper.insertProfile(connection, "default", 300, null, "select",
                "[\"Net-SNMP\"]", null);
    }

    private void updateSystemDefGroupNames(final int sourceId, final String name, final String json) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE snmp_collection_systemdefs SET mib_group_names = ? WHERE collection_source_id = ? AND name = ?")) {
            ps.setString(1, json);
            ps.setInt(2, sourceId);
            ps.setString(3, name);
            ps.executeUpdate();
        }
    }

    // --- assertion helpers ---

    private int countMibGroups(final int sourceId, final String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM snmp_collection_mib_groups WHERE collection_source_id = ? AND name = ?")) {
            ps.setInt(1, sourceId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private String selectMibGroupObjects(final int sourceId, final String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT mib_objects FROM snmp_collection_mib_groups WHERE collection_source_id = ? AND name = ?")) {
            ps.setInt(1, sourceId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private String selectSystemDefGroupNames(final int sourceId, final String name) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT mib_group_names FROM snmp_collection_systemdefs WHERE collection_source_id = ? AND name = ?")) {
            ps.setInt(1, sourceId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private boolean isLedgerRecorded(final String updateId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM snmp_collection_defaults_log WHERE update_id = ?")) {
            ps.setString(1, updateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String selectLedgerDescription(final String updateId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT description FROM snmp_collection_defaults_log WHERE update_id = ?")) {
            ps.setString(1, updateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }
}
