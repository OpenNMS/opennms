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
package org.opennms.netmgt.ha;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Owns the {@code ha_instance_status} schema. The table is created and
 * upgraded at HA startup rather than through the core Liquibase changelog so
 * that non-HA installations never grow the table.
 *
 * <p>Schema evolution is an append-only list of idempotent steps
 * ({@code IF NOT EXISTS} DDL), replayed on every startup and serialized
 * across concurrent bootstrappers by a Postgres advisory lock. Shipped steps
 * are never edited or reordered — new changes append a new step. Evolution
 * policy is additive-only: new columns are nullable or defaulted; nothing is
 * renamed or retyped.
 *
 * <p>This is a shared contract: an external HA agent supervising the pair
 * (see {@link HaMode#HEARTBEAT_ONLY}) carries the same step list and the
 * same lock id, and whichever process starts first performs the bootstrap.
 * The two step lists must be kept literally identical.
 *
 * <p>Runtime column ownership: {@code last_heartbeat} is written by OpenNMS
 * (this daemon); {@code current_state}/{@code active_since}/{@code agent_last_seen}
 * are written by whichever supervisor is in charge (this coordinator in
 * {@code coordinator} mode, the external agent in {@code heartbeat-only} mode).
 */
public final class HaStatusSchema {

    /** Advisory-lock key serializing schema bootstrap: 8980, the OpenNMS web
     * port. Must match the external HA agent's schema code. */
    static final long SCHEMA_LOCK_ID = 8980;

    /** Append-only, idempotent schema steps. Never edit or reorder shipped
     * entries — append new ones. */
    static final String[] STEPS = {
            // v1: initial shape
            """
            CREATE TABLE IF NOT EXISTS ha_instance_status (
                instance_id     TEXT PRIMARY KEY,
                configured_role TEXT NOT NULL,
                current_state   TEXT NOT NULL,
                last_heartbeat  TIMESTAMPTZ NOT NULL DEFAULT now(),
                agent_last_seen TIMESTAMPTZ,
                active_since    TIMESTAMPTZ,
                hostname        TEXT
            )""",
            // v2: config-sync observability and boot-config staleness
            """
            ALTER TABLE ha_instance_status
                ADD COLUMN IF NOT EXISTS last_sync_attempt      TIMESTAMPTZ,
                ADD COLUMN IF NOT EXISTS last_sync_success      TIMESTAMPTZ,
                ADD COLUMN IF NOT EXISTS last_sync_error        TEXT,
                ADD COLUMN IF NOT EXISTS boot_config_changed_at TIMESTAMPTZ""",
    };

    private HaStatusSchema() {}

    /** Creates/upgrades the table. Safe to call from any number of nodes and
     * processes concurrently: the advisory lock serializes bootstrappers, and
     * every step is an idempotent no-op once applied. */
    public static void ensureSchema(DbConnectionFactory dbFactory) throws Exception {
        // All statements must run on the same connection: the advisory lock
        // is session-scoped.
        try (Connection c = dbFactory.getConnection()) {
            execute(c, "SELECT pg_advisory_lock(" + SCHEMA_LOCK_ID + ")");
            try {
                for (String step : STEPS) {
                    execute(c, step);
                }
            } finally {
                execute(c, "SELECT pg_advisory_unlock(" + SCHEMA_LOCK_ID + ")");
            }
        }
    }

    private static void execute(Connection c, String sql) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.execute();
        }
    }
}
