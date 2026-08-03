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

import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.netmgt.ha.DbConnectionFactory;
import org.opennms.netmgt.ha.HaConfiguration;
import org.opennms.netmgt.ha.HaStartupCoordinator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Karaf shell command: {@code opennms:ha-status}
 *
 * <p>Displays a formatted table of all HA instance rows from the
 * {@code ha_instance_status} database table, mirroring the output of
 * {@code GET /rest/ha/status}. Heartbeat ages are computed on the database
 * server (clock-skew safe) and an instance is reported as DEGRADED if its
 * heartbeat age exceeds the configured failover threshold or if its current
 * role/state combination indicates a degraded cluster condition.
 */
@Command(scope = "opennms", name = "ha-status", description = "Display HA cluster status for all instances.")
@Service
public class HaStatusCommand implements Action {

    @Override
    public Object execute() throws Exception {
        // Failover threshold for staleness detection comes from the live local config.
        // If HA isn't enabled on this node, fall back to the schema default.
        int failoverThresholdSeconds = new HaConfiguration().getFailoverThresholdSeconds();
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord != null) {
            failoverThresholdSeconds = coord.getConfig().getFailoverThresholdSeconds();
        }

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
                     "SELECT instance_id, configured_role, current_state, active_since, " +
                     "EXTRACT(EPOCH FROM (NOW() - last_heartbeat)) AS age_seconds, hostname " +
                     "FROM ha_instance_status ORDER BY configured_role")) {
            
            ShellTable haTable = new ShellTable();
            haTable.column("INSTANCE");
            haTable.column("ROLE");
            haTable.column("STATE");
            haTable.column("ACTIVE SINCE");
            haTable.column("HEARTBEAT AGE");
            haTable.column("HOSTNAME");
            haTable.column("DEGRADED");

            boolean anyRows = false;
            while (rs.next()) {
                anyRows = true;
                String instanceId     = rs.getString("instance_id");
                String configuredRole = rs.getString("configured_role");
                String currentState   = rs.getString("current_state");
                String activeSince    = rs.getString("active_since");
                String hostname       = rs.getString("hostname");

                long ageSeconds       = rs.getLong("age_seconds");
                boolean heartbeatKnown = !rs.wasNull();
                boolean heartbeatStale = heartbeatKnown && ageSeconds > failoverThresholdSeconds;

                boolean stateBasedDegraded =
                        ("SECONDARY".equals(configuredRole) && "ACTIVE".equals(currentState))
                        || "DEGRADED".equals(currentState);
                boolean degraded = stateBasedDegraded || heartbeatStale;
                haTable.addRow().addContent(nvl(instanceId), nvl(configuredRole), nvl(currentState),
                        nvl(activeSince), heartbeatKnown ? formatAge(ageSeconds) : "--",
                        nvl(hostname), degraded ? "YES" : "no");
            }
            
            if (anyRows) {
                System.out.println("HA Cluster Status");
                System.out.println("=================");
                haTable.print(System.out);
                System.out.println();
            } else {
                System.out.println("(HA table is empty or HA is not configured)");
            }

        } catch (Exception e) {
            // undefined_table: HA has never been enabled here
            if (e instanceof SQLException sql && "42P01".equals(sql.getSQLState())) {
                System.out.println("HA is not enabled on this instance.");
            } else {
                System.err.println("ERROR: Failed to query HA status: " + e.getMessage());
            }
        }

        return null;
    }

    private static String formatAge(long ageSeconds) {
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
