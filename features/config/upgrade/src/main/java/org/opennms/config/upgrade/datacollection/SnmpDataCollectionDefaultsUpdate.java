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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies shipped-default changes to the DB-resident SNMP data collection
 * config for installations that migrated from XML before those defaults
 * changed (see NMS-18291 review on the original XML-only change).
 *
 * <p>One update == one bundled datacollection-group fragment under
 * {@code datacollection-defaults/} plus an entry in {@link #UPDATES}. The
 * fragment is declarative (see {@link #applyFragment}); all surrounding
 * machinery — ledger, source lookup, archival — is common.
 *
 * <p>Runs at every startup, after {@link SnmpDataCollectionMigration}, so the
 * ordering works out for every install type in a single boot:
 * <ul>
 *   <li>fresh install / first boot after upgrading from a pre-DB 36.x: the
 *       migration has just imported the shipped XML (which already contains
 *       the new defaults), so each update is a content no-op and is only
 *       recorded in the ledger;</li>
 *   <li>already-migrated install: the update applies the delta to the DB.</li>
 * </ul>
 *
 * <p>Each update is identified by an id recorded in
 * {@code snmp_collection_defaults_log} once applied. The ledger — not a
 * content check — provides idempotency, so content an administrator has
 * deliberately deleted afterwards is never resurrected. Updates only ever add
 * missing rows or append missing references; they never overwrite existing
 * rows, which remain owned by the administrator.
 *
 * <p>To keep a git-trackable record of what was changed underneath the
 * archived etc tree, each applied fragment is also copied to
 * {@code etc_archive/datacollection/updates/} (see
 * {@link #archiveAppliedFragments()}, invoked by UpgradeConfigService after
 * the transaction commits).
 */
public class SnmpDataCollectionDefaultsUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpDataCollectionDefaultsUpdate.class);

    /**
     * A single shipped-default update: ledger id, bundled fragment resource,
     * and a human-readable description stored in the ledger.
     */
    static final class DefaultConfigUpdate {
        final String id;
        final String resourcePath;
        final String description;

        DefaultConfigUpdate(final String id, final String resourcePath, final String description) {
            this.id = id;
            this.resourcePath = resourcePath;
            this.description = description;
        }
    }

    /**
     * Registry of shipped-default updates, in application order.
     * Add new entries here together with their fragment resource.
     */
    static final List<DefaultConfigUpdate> UPDATES = List.of(
            new DefaultConfigUpdate(
                    "NMS-18291-ucd-memory-X",
                    "/datacollection-defaults/nms-18291-ucd-memory-x.xml",
                    "Add 64-bit UCD-SNMP memory gauges (ucd-memory-X) to the Net-SNMP source and systemDef")
    );

    static final String ARCHIVE_UPDATES_DIR = "datacollection/updates";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final List<DefaultConfigUpdate> appliedUpdates = new ArrayList<>();

    /**
     * Apply all pending default updates. The caller owns the transaction
     * (commit/rollback), matching {@link SnmpDataCollectionMigration#execute}.
     *
     * @return true if at least one update was applied (and should be archived
     *         via {@link #archiveAppliedFragments()} after commit)
     */
    public boolean execute(final Connection conn) throws SQLException {
        if (!isLedgerAvailable(conn)) {
            LOG.warn("snmp_collection_defaults_log table not found — datacollection default updates skipped. "
                    + "Run the installer (bin/install) to bring the database schema up to date.");
            return false;
        }
        if (!isMigrated(conn)) {
            // XML→DB migration hasn't produced any profiles yet (e.g. empty etc).
            // Don't record anything; retry on a later startup once data exists.
            LOG.info("SNMP data collection tables not populated yet — deferring default updates.");
            return false;
        }

        for (final DefaultConfigUpdate update : UPDATES) {
            if (isApplied(conn, update.id)) {
                continue;
            }
            applyFragment(conn, update);
            recordApplied(conn, update);
            appliedUpdates.add(update);
        }
        return !appliedUpdates.isEmpty();
    }

    /**
     * Apply one bundled datacollection-group fragment. The fragment is
     * declarative:
     * <ul>
     *   <li>the {@code datacollection-group} name selects the target
     *       collection source; if no source of that name exists (removed by an
     *       administrator), the update is a recorded no-op;</li>
     *   <li>each {@code <group>} is inserted iff no MIB group of that name
     *       exists under the source;</li>
     *   <li>each {@code <systemDef>} is a <em>reference stub</em> for an
     *       existing systemDef of the same name: of its includeGroup entries,
     *       only those naming a group defined in this fragment are added to
     *       the systemDef's group list. Entries naming other groups act as
     *       position anchors only — a new reference is inserted directly after
     *       the nearest preceding anchor present in the database list, so
     *       fragment ordering is preserved without ever re-adding entries an
     *       administrator removed.</li>
     * </ul>
     */
    void applyFragment(final Connection conn, final DefaultConfigUpdate update) throws SQLException {
        final DatacollectionGroup fragment = loadFragment(update.resourcePath);

        final String sourceName = fragment.getName();
        final int sourceId = SnmpDataCollectionSqlHelper.findSourceIdByName(conn, sourceName);
        if (sourceId < 0) {
            LOG.info("{}: source '{}' not present (removed by an administrator?) — nothing to update.",
                    update.id, sourceName);
            return;
        }

        // Names of groups this fragment defines: only these may be added to systemDefs.
        final Set<String> fragmentGroupNames = new LinkedHashSet<>();

        for (final Group group : fragment.getGroups()) {
            fragmentGroupNames.add(group.getName());
            if (mibGroupExists(conn, sourceId, group.getName())) {
                LOG.debug("{}: MIB group '{}' already present under source '{}'.",
                        update.id, group.getName(), sourceName);
            } else {
                SnmpDataCollectionSqlHelper.batchInsertMibGroups(conn, sourceId, List.of(group));
                LOG.info("{}: added MIB group '{}' to source '{}'.", update.id, group.getName(), sourceName);
            }
        }

        for (final SystemDef stub : fragment.getSystemDefs()) {
            mergeSystemDefReferences(conn, update, sourceId, stub, fragmentGroupNames);
        }
    }

    /**
     * Merge a fragment systemDef stub into the existing systemDef row of the
     * same name: add references to fragment-defined groups that are missing,
     * positioned after the nearest preceding anchor (see {@link #applyFragment}).
     */
    private void mergeSystemDefReferences(final Connection conn,
                                          final DefaultConfigUpdate update,
                                          final int sourceId,
                                          final SystemDef stub,
                                          final Set<String> fragmentGroupNames) throws SQLException {
        if (stub.getCollect() == null || stub.getCollect().getIncludeGroups() == null
                || stub.getCollect().getIncludeGroups().isEmpty()) {
            return;
        }

        final SystemDefRow row = findSystemDef(conn, sourceId, stub.getName());
        if (row == null) {
            LOG.info("{}: systemDef '{}' not present (removed by an administrator?) — references not added.",
                    update.id, stub.getName());
            return;
        }

        final List<String> dbGroupNames = parseStringArray(row.mibGroupNamesJson);
        final List<String> stubEntries = stub.getCollect().getIncludeGroups();
        boolean changed = false;

        for (int i = 0; i < stubEntries.size(); i++) {
            final String entry = stubEntries.get(i);
            if (!fragmentGroupNames.contains(entry)) {
                continue; // anchor only — never (re-)added
            }
            if (dbGroupNames.contains(entry)) {
                LOG.debug("{}: systemDef '{}' already references '{}'.", update.id, stub.getName(), entry);
                continue;
            }
            // Insert after the nearest preceding stub entry present in the DB list.
            int insertAt = dbGroupNames.size();
            for (int j = i - 1; j >= 0; j--) {
                final int anchorIdx = dbGroupNames.indexOf(stubEntries.get(j));
                if (anchorIdx >= 0) {
                    insertAt = anchorIdx + 1;
                    break;
                }
            }
            dbGroupNames.add(insertAt, entry);
            changed = true;
            LOG.info("{}: referenced MIB group '{}' from systemDef '{}'.", update.id, entry, stub.getName());
        }

        if (changed) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE snmp_collection_systemdefs SET mib_group_names = ? WHERE id = ?")) {
                ps.setString(1, DataCollectionGroupMapper.toJson(dbGroupNames));
                ps.setInt(2, row.id);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Copy the fragments of all updates applied by {@link #execute} into
     * {@code etc_archive/datacollection/updates/}, so the archived etc tree
     * carries a record of what changed in the database (git-based etc).
     * Call after the DB transaction has committed, mirroring
     * {@link SnmpDataCollectionMigration#archiveFiles()}.
     */
    public void archiveAppliedFragments() {
        if (appliedUpdates.isEmpty()) {
            return;
        }
        final String opennmsHome = System.getProperty("opennms.home", "/opt/opennms");
        final Path archiveDir = Paths.get(opennmsHome, "etc_archive").resolve(ARCHIVE_UPDATES_DIR);
        try {
            Files.createDirectories(archiveDir);
        } catch (IOException e) {
            LOG.error("Failed to create archive directory {} — applied default updates are recorded in "
                    + "snmp_collection_defaults_log but not mirrored to the archive: {}",
                    archiveDir, e.getMessage(), e);
            return;
        }
        for (final DefaultConfigUpdate update : appliedUpdates) {
            final String fileName = Paths.get(update.resourcePath).getFileName().toString();
            final Path target = archiveDir.resolve(fileName);
            try (InputStream stream = SnmpDataCollectionDefaultsUpdate.class.getResourceAsStream(update.resourcePath)) {
                if (stream == null) {
                    LOG.warn("{}: bundled fragment {} disappeared from classpath — cannot archive.",
                            update.id, update.resourcePath);
                    continue;
                }
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("{}: archived applied default update fragment to {}", update.id, target);
            } catch (IOException e) {
                LOG.error("{}: failed to archive fragment to {}: {}", update.id, target, e.getMessage(), e);
            }
        }
    }

    /** Load and parse a bundled datacollection-group fragment. */
    DatacollectionGroup loadFragment(final String resourcePath) throws SQLException {
        try (InputStream stream = SnmpDataCollectionDefaultsUpdate.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new SQLException("Bundled datacollection resource not found on classpath: " + resourcePath);
            }
            return SnmpDataCollectionMigration.unmarshal(DatacollectionGroup.class, stream, resourcePath);
        } catch (IOException e) {
            throw new SQLException("Failed to read bundled resource " + resourcePath, e);
        }
    }

    private boolean isLedgerAvailable(final Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM snmp_collection_defaults_log LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isMigrated(final Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM snmp_collection_profiles");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOG.debug("snmp_collection_profiles not accessible — default updates skipped: {}", e.getMessage());
            return false;
        }
    }

    private boolean isApplied(final Connection conn, final String updateId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM snmp_collection_defaults_log WHERE update_id = ?")) {
            ps.setString(1, updateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void recordApplied(final Connection conn, final DefaultConfigUpdate update) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO snmp_collection_defaults_log (update_id, description) VALUES (?, ?)")) {
            ps.setString(1, update.id);
            ps.setString(2, update.description);
            ps.executeUpdate();
        }
        LOG.info("Recorded datacollection default update '{}' as applied.", update.id);
    }

    private boolean mibGroupExists(final Connection conn, final int sourceId, final String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM snmp_collection_mib_groups WHERE collection_source_id = ? AND name = ?")) {
            ps.setInt(1, sourceId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private SystemDefRow findSystemDef(final Connection conn, final int sourceId, final String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id, mib_group_names FROM snmp_collection_systemdefs WHERE collection_source_id = ? AND name = ?")) {
            ps.setInt(1, sourceId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SystemDefRow(rs.getInt(1), rs.getString(2));
                }
            }
        }
        return null;
    }

    /** Parse a JSON array of strings, tolerating null/blank as empty. Package-private for testing. */
    static List<String> parseStringArray(final String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            final List<String> parsed = OBJECT_MAPPER.readValue(json, new TypeReference<List<String>>() {});
            return parsed == null ? new ArrayList<>() : new ArrayList<>(parsed);
        } catch (IOException e) {
            throw new SQLException("Failed to parse mib_group_names JSON: " + json, e);
        }
    }

    private static final class SystemDefRow {
        final int id;
        final String mibGroupNamesJson;

        SystemDefRow(final int id, final String mibGroupNamesJson) {
            this.id = id;
            this.mibGroupNamesJson = mibGroupNamesJson;
        }
    }
}
