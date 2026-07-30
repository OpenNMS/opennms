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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.ha.HaConfiguration;
import org.opennms.netmgt.ha.HaInstanceState;
import org.opennms.netmgt.ha.HaStartupCoordinator;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.List;

/**
 * Karaf shell command: {@code opennms:ha-failover}
 *
 * <p>Initiates a graceful failover: this instance transitions to STANDBY
 * and all OpenNMS services stop, allowing the partner to promote to ACTIVE.
 * Equivalent to {@code POST /rest/ha/failover}.
 *
 * <p>Requires {@code --force} to execute; without it the command prints
 * a warning and exits safely.
 */
@Command(scope = "opennms", name = "ha-failover",
        description = "Initiate a graceful HA failover: stop this instance and yield to the partner.")
@Service
public class HaFailoverCommand implements Action {

    private static final Logger LOG = LoggerFactory.getLogger(HaFailoverCommand.class);

    @Option(name = "--force",
            description = "Required confirmation flag. Without this the command is a no-op.")
    private boolean force = false;

    @Override
    public Object execute() throws Exception {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            System.out.println("HA is not enabled on this instance.");
            return null;
        }

        HaConfiguration cfg = coord.getConfig();

        if (cfg.getMode() == org.opennms.netmgt.ha.HaMode.HEARTBEAT_ONLY) {
            System.out.println("HA is in heartbeat-only mode; failover is controlled by the external HA agent.");
            return null;
        }

        HaInstanceState state = coord.getCurrentState();

        if (state != HaInstanceState.ACTIVE) {
            System.out.printf("This instance (%s, role=%s) is currently %s — not ACTIVE.%n",
                    cfg.getInstanceId(), cfg.getRole(), state);
            System.out.println("Failover is only applicable when this instance is ACTIVE.");
            return null;
        }

        if (!force) {
            System.out.println();
            System.out.println("  WARNING: HA Failover");
            System.out.println("  ====================");
            System.out.printf("  Instance:  %s%n", cfg.getInstanceId());
            System.out.printf("  Role:      %s%n", cfg.getRole());
            System.out.printf("  State:     %s%n", state);
            System.out.println();
            System.out.println("  This will stop all OpenNMS services on this instance and");
            System.out.println("  mark it STANDBY so the partner can promote to ACTIVE.");
            System.out.println();
            System.out.println("  Re-run with --force to confirm:");
            System.out.println("    opennms:ha-failover --force");
            System.out.println();
            return null;
        }

        LOG.warn("HA failover initiated via Karaf shell on {} ({})", cfg.getInstanceId(), cfg.getRole());

        // 1. Write STANDBY to DB and stop the heartbeat scheduler so the partner detects immediately.
        coord.initiateFailover();
        System.out.printf("Failover initiated: %s (%s) is now STANDBY.%n",
                cfg.getInstanceId(), cfg.getRole());
        System.out.println("Stopping OpenNMS services...");

        // 2. Stop all services via MBean on a short delay to allow this output to flush.
        Thread stopThread = new Thread(() -> {
            try {
                Thread.sleep(500);
                List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
                if (!servers.isEmpty()) {
                    servers.get(0).invoke(
                            ObjectName.getInstance("OpenNMS:Name=Manager"), "stop",
                            new Object[0], new String[0]);
                } else {
                    LOG.error("HA failover: no MBeanServer found; services may not stop cleanly");
                    System.err.println("WARNING: No MBeanServer found — services may not stop cleanly.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("HA failover: error stopping services", e);
                System.err.println("ERROR stopping services: " + e.getMessage());
            }
        }, "ha-failover-shell");
        stopThread.setDaemon(false);
        stopThread.start();

        System.out.println("Monitor 'opennms:ha-status' on the partner to confirm it activates.");

        return null;
    }
}
