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
package org.opennms.netmgt.ha.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.netmgt.ha.DbConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Karaf shell command: {@code opennms:ha-status}
 *
 * <p>Displays a formatted table of all HA instance rows from the
 * {@code ha_instance_status} database table, equivalent to
 * {@code GET /rest/ha/status}.
 */
@Command(scope = "opennms", name = "ha-status", description = "Display HA cluster status for all instances.")
@Service
public class    HaStatusCommand implements Action {

    private static final String FMT = "%-22s %-10s %-14s %-22s %-26s %s%n";

    @Override
    public Object execute() throws Exception {
        DbConnectionFactory dbFactory;
        try {
            dbFactory = DbConnectionFactory.fromDatasourcesXml();
        } catch (Exception e) {
            System.err.println("ERROR: Cannot connect to database: " + e.getMessage());
            return null;
        }

        try (Connection conn = dbFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT instance_id, configured_role, current_state, last_heartbeat, hostname " +
                     "FROM ha_instance_status ORDER BY configured_role")) {

            System.out.println("HA Cluster Status");
            System.out.println("=================");
            System.out.printf(FMT, "INSTANCE", "ROLE", "STATE", "HEARTBEAT AGE", "HOSTNAME", "DEGRADED");
            System.out.printf(FMT,
                    "----------------------", "----------", "--------------",
                    "----------------------", "--------------------------", "--------");

            boolean anyRows = false;
            while (rs.next()) {
                anyRows = true;
                String instanceId    = rs.getString("instance_id");
                String configuredRole = rs.getString("configured_role");
                String currentState  = rs.getString("current_state");
                Timestamp ts         = rs.getTimestamp("last_heartbeat");
                String hostname      = rs.getString("hostname");

                String heartbeatAge  = formatAge(ts);
                boolean degraded     = ("SECONDARY".equals(configuredRole) && "ACTIVE".equals(currentState))
                                    || "DEGRADED".equals(currentState);

                System.out.printf(FMT,
                        nvl(instanceId),
                        nvl(configuredRole),
                        nvl(currentState),
                        heartbeatAge,
                        nvl(hostname),
                        degraded ? "YES" : "no");
            }

            if (!anyRows) {
                System.out.println("  (no rows — HA table is empty or HA is not configured)");
            }

        } catch (Exception e) {
            System.err.println("ERROR: Failed to query HA status: " + e.getMessage());
        }

        return null;
    }

    private static String formatAge(Timestamp ts) {
        if (ts == null) {
            return "--";
        }
        long ageSeconds = Instant.now().getEpochSecond() - ts.toInstant().getEpochSecond();
        if (ageSeconds < 60) {
            return ageSeconds + "s ago";
        } else if (ageSeconds < 3600) {
            return (ageSeconds / 60) + "m ago";
        } else {
            return (ageSeconds / 3600) + "h ago";
        }
    }

    private static String nvl(String s) {
        return s != null ? s : "--";
    }
}
