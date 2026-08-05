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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.Supplier;

/**
 * Publishes this instance's liveness by updating {@code last_heartbeat} —
 * and <em>only</em> that column. State columns ({@code current_state},
 * {@code active_since}) belong to whichever supervisor is in charge; writing
 * them here would let a stale in-JVM view overwrite an external demotion.
 *
 * <p>Runs in both HA modes: it is the entire in-JVM HA surface in
 * {@code heartbeat-only} mode, and the liveness half of {@code coordinator}
 * mode.
 */
public class HaHeartbeatWriter {

    private static final Logger LOG = LoggerFactory.getLogger(HaHeartbeatWriter.class);

    private final DbConnectionFactory dbFactory;
    private final Supplier<String> instanceId;

    public HaHeartbeatWriter(DbConnectionFactory dbFactory, Supplier<String> instanceId) {
        this.dbFactory = dbFactory;
        this.instanceId = instanceId;
    }

    public void write() {
        try (Connection conn = dbFactory.getConnection()) {
            String sql = "UPDATE ha_instance_status SET last_heartbeat = NOW() WHERE instance_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, instanceId.get());
                if (ps.executeUpdate() == 0) {
                    // Without our row, split-brain arbitration cannot fire on
                    // either side while the partner promotes on its absence.
                    LOG.error("HA: heartbeat matched no row for instance {} — the status row is "
                            + "missing; HA arbitration is disabled until it is restored", instanceId.get());
                }
            }
        } catch (Exception e) {
            LOG.warn("HA: failed to write heartbeat for instance {}", instanceId.get(), e);
        }
    }
}
