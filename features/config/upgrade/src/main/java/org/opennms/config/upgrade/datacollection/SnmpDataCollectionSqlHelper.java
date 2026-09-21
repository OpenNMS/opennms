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

import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SystemDef;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Encapsulates raw SQL operations for the SNMP data collection migration.
 * Uses sequences for ID generation and NOW() for timestamps.
 */
public final class SnmpDataCollectionSqlHelper {

    private static final String INSERT_PROFILE =
            "INSERT INTO snmp_collection_profiles " +
            "(id, name, rrd_step, rrd_rras, storage_flag, source_names, max_vars_per_pdu, enabled, created_time) " +
            "VALUES (nextval('snmp_collection_profiles_id_seq'), ?, ?, ?, ?, ?, ?, true, NOW())";

    private static final String INSERT_SOURCE =
            "INSERT INTO snmp_collection_sources " +
            "(id, name, vendor, description, enabled, created_time, last_modified, uploaded_by) " +
            "VALUES (nextval('snmp_collection_sources_id_seq'), ?, ?, ?, true, NOW(), NOW(), ?)";

    private static final String SELECT_SOURCE_ID =
            "SELECT id FROM snmp_collection_sources WHERE name = ?";

    private static final String INSERT_MIB_GROUP =
            "INSERT INTO snmp_collection_mib_groups " +
            "(id, collection_source_id, name, if_type, mib_group_names, mib_objects, mib_obj_properties, enabled) " +
            "VALUES (nextval('snmp_collection_mib_groups_id_seq'), ?, ?, ?, ?, ?, ?, true)";

    private static final String INSERT_RESOURCE_TYPE =
            "INSERT INTO snmp_collection_resource_types " +
            "(id, collection_source_id, name, label, resource_label, " +
            "persistence_selector_strategy, persistence_selector_params, " +
            "storage_strategy, storage_strategy_params, enabled) " +
            "VALUES (nextval('snmp_collection_resource_types_id_seq'), ?, ?, ?, ?, ?, ?, ?, ?, true)";

    private static final String INSERT_SYSTEMDEF =
            "INSERT INTO snmp_collection_systemdefs " +
            "(id, collection_source_id, name, sysoid, sysoid_mask, " +
            "ip_addresses, ip_address_masks, mib_group_names, enabled) " +
            "VALUES (nextval('snmp_collection_systemdefs_id_seq'), ?, ?, ?, ?, ?, ?, ?, true)";

    private SnmpDataCollectionSqlHelper() {
    }

    /**
     * Insert a collection profile and return the generated ID.
     */
    public static int insertProfile(final Connection conn,
                                    final String name,
                                    final int rrdStep,
                                    final String rrdRrasJson,
                                    final String storageFlag,
                                    final String sourceNamesJson,
                                    final Integer maxVarsPerPdu) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_PROFILE, new String[]{"id"})) {
            ps.setString(1, name);
            ps.setInt(2, rrdStep);
            setNullableString(ps, 3, rrdRrasJson);
            ps.setString(4, storageFlag != null ? storageFlag : "select");
            setNullableString(ps, 5, sourceNamesJson);
            if (maxVarsPerPdu != null) {
                ps.setInt(6, maxVarsPerPdu);
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for profile: " + name);
    }

    /**
     * Insert a collection source and return the generated ID.
     */
    public static int insertSource(final Connection conn,
                                   final String name,
                                   final String vendor,
                                   final String description,
                                   final String uploadedBy) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SOURCE, new String[]{"id"})) {
            ps.setString(1, name);
            setNullableString(ps, 2, vendor);
            setNullableString(ps, 3, description);
            setNullableString(ps, 4, uploadedBy);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated ID for source: " + name);
    }

    /**
     * Look up a source ID by name, returns -1 if not found.
     */
    public static int findSourceIdByName(final Connection conn, final String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_SOURCE_ID)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Batch insert MIB groups for a given source.
     */
    public static int batchInsertMibGroups(final Connection conn,
                                           final int sourceId,
                                           final List<Group> groups) throws SQLException {
        if (groups == null || groups.isEmpty()) {
            return 0;
        }
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_MIB_GROUP)) {
            for (final Group group : groups) {
                ps.setInt(1, sourceId);
                ps.setString(2, group.getName());
                setNullableString(ps, 3, group.getIfType());
                setNullableString(ps, 4, DataCollectionGroupMapper.mapIncludeGroups(group));
                ps.setString(5, DataCollectionGroupMapper.mapMibObjects(group));
                setNullableString(ps, 6, DataCollectionGroupMapper.mapMibObjProperties(group));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
        }
        return count;
    }

    /**
     * Batch insert resource types for a given source.
     */
    public static int batchInsertResourceTypes(final Connection conn,
                                               final int sourceId,
                                               final List<ResourceType> resourceTypes) throws SQLException {
        if (resourceTypes == null || resourceTypes.isEmpty()) {
            return 0;
        }
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_RESOURCE_TYPE)) {
            for (final ResourceType rt : resourceTypes) {
                ps.setInt(1, sourceId);
                ps.setString(2, rt.getName());
                // label is NOT NULL in DDL but XSD-loose XML in the wild can omit it.
                // Fall back to name so migration doesn't abort on otherwise-valid rows.
                final String label = (rt.getLabel() != null && !rt.getLabel().isBlank())
                        ? rt.getLabel() : rt.getName();
                ps.setString(3, label);
                setNullableString(ps, 4, rt.getResourceLabel());
                setNullableString(ps, 5, DataCollectionGroupMapper.mapPersistenceSelectorStrategy(rt));
                setNullableString(ps, 6, DataCollectionGroupMapper.mapPersistenceSelectorParams(rt));
                setNullableString(ps, 7, DataCollectionGroupMapper.mapStorageStrategy(rt));
                setNullableString(ps, 8, DataCollectionGroupMapper.mapStorageStrategyParams(rt));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
        }
        return count;
    }

    /**
     * Batch insert system definitions for a given source.
     */
    public static int batchInsertSystemDefs(final Connection conn,
                                            final int sourceId,
                                            final List<SystemDef> systemDefs) throws SQLException {
        if (systemDefs == null || systemDefs.isEmpty()) {
            return 0;
        }
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SYSTEMDEF)) {
            for (final SystemDef sd : systemDefs) {
                ps.setInt(1, sourceId);
                ps.setString(2, sd.getName());
                setNullableString(ps, 3, sd.getSysoid());
                setNullableString(ps, 4, sd.getSysoidMask());
                setNullableString(ps, 5, DataCollectionGroupMapper.mapIpAddresses(sd));
                setNullableString(ps, 6, DataCollectionGroupMapper.mapIpAddressMasks(sd));
                ps.setString(7, DataCollectionGroupMapper.mapSystemDefMibGroupNames(sd));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
        }
        return count;
    }

    /**
     * Update a profile's source_names field.
     */
    public static void updateProfileSourceNames(final Connection conn,
                                                final int profileId,
                                                final String sourceNamesJson) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE snmp_collection_profiles SET source_names = ? WHERE id = ?")) {
            setNullableString(ps, 1, sourceNamesJson);
            ps.setInt(2, profileId);
            ps.executeUpdate();
        }
    }

    private static void setNullableString(final PreparedStatement ps,
                                          final int index,
                                          final String value) throws SQLException {
        if (value != null) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }
}
