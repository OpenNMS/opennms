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

import org.opennms.netmgt.ha.HaConfiguration;
import org.opennms.netmgt.ha.HaStartupCoordinator;

/**
 * Karaf shell command: {@code opennms:ha-config}
 *
 * <p>With no flags, displays the HA configuration for this instance. With one
 * or more modification flags (e.g. {@code --sync-enabled}, {@code --heartbeat-interval}),
 * mutates the matching fields, writes the result to disk, and applies it
 * immediately to the running coordinator.
 *
 * <p>Equivalent to {@code GET /rest/ha/config} and {@code PUT /rest/ha/config}.
 */
@Command(scope = "opennms", name = "ha-config",
        description = "Display or modify the HA configuration for this instance.")
@Service
public class HaConfigCommand implements Action {

    private static final String FMT = "  %-30s %s%n";

    @Option(name = "--sync-enabled",
            description = "Set sync-enabled to true or false.")
    private String syncEnabled;

    @Option(name = "--sync-interval",
            description = "Set the config sync interval in seconds.")
    private Integer syncInterval;

    @Option(name = "--heartbeat-interval",
            description = "Set the heartbeat interval in seconds (minimum 5).")
    private Integer heartbeatInterval;

    @Option(name = "--failover-threshold",
            description = "Set the failover threshold in seconds (minimum 20).")
    private Integer failoverThreshold;

    @Option(name = "--partner-rest-url",
            description = "Set the partner REST URL (e.g. http://partner:8980/opennms).")
    private String partnerRestUrl;

    @Override
    public Object execute() throws Exception {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            System.out.println("HA is not enabled on this instance.");
            return null;
        }

        if (syncEnabled != null && !syncEnabled.equalsIgnoreCase("true") && !syncEnabled.equalsIgnoreCase("false")) {
            System.out.println("ERROR: --sync-enabled must be either 'true' or 'false'");
            return null;
        }

        boolean modified = syncEnabled != null
                || syncInterval != null
                || heartbeatInterval != null
                || failoverThreshold != null
                || partnerRestUrl != null;

        if (modified) {
            HaConfiguration newCfg = copyOf(coord.getConfig());
            if (syncEnabled != null)        newCfg.setSyncEnabled(Boolean.parseBoolean(syncEnabled));
            if (syncInterval != null)       newCfg.setSyncIntervalSeconds(syncInterval);
            if (heartbeatInterval != null)  newCfg.setHeartbeatIntervalSeconds(heartbeatInterval);
            if (failoverThreshold != null)  newCfg.setFailoverThresholdSeconds(failoverThreshold);
            if (partnerRestUrl != null)     newCfg.setPartnerRestUrl(partnerRestUrl);

            try {
                coord.writeConfig(newCfg); // writes to disk and applies in one step
                System.out.println("HA configuration written and applied.");
                System.out.println();
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: " + e.getMessage());
                return null;
            } catch (Exception e) {
                System.err.println("ERROR: Failed to write configuration: " + e.getMessage());
                return null;
            }
        }

        displayConfig(coord);
        return null;
    }

    private static void displayConfig(HaStartupCoordinator coord) {
        HaConfiguration cfg = coord.getConfig();

        System.out.println("HA Configuration");
        System.out.println("================");
        System.out.printf(FMT, "Enabled:",            cfg.isEnabled());
        System.out.printf(FMT, "Instance ID:",         nvl(cfg.getInstanceId()));
        System.out.printf(FMT, "Role:",                nvl(cfg.getRole()));
        System.out.printf(FMT, "Mode:",                nvl(cfg.getMode()));
        System.out.printf(FMT, "Partner Instance ID:", nvl(cfg.getPartnerInstanceId()));
        System.out.printf(FMT, "Heartbeat Interval:",  cfg.getHeartbeatIntervalSeconds() + "s");
        System.out.printf(FMT, "Failover Threshold:",  cfg.getFailoverThresholdSeconds() + "s");
        System.out.printf(FMT, "Sync Enabled:",        cfg.isSyncEnabled());
        System.out.printf(FMT, "Sync Interval:",       cfg.getSyncIntervalSeconds() + "s");
        System.out.printf(FMT, "Partner REST URL:",    nvl(cfg.getPartnerRestUrl()));
        System.out.printf(FMT, "Sync Username:",       nvl(cfg.getSyncUsername()));
        System.out.printf(FMT, "Sync Password:",       cfg.getSyncPassword() != null ? "***" : "--");

        System.out.println();
        System.out.printf(FMT, "Current State:", coord.getCurrentState());
    }

    private static HaConfiguration copyOf(HaConfiguration src) {
        HaConfiguration c = new HaConfiguration();
        c.setEnabled(src.isEnabled());
        c.setInstanceId(src.getInstanceId());
        c.setRole(src.getRole());
        c.setMode(src.getMode());
        c.setPartnerInstanceId(src.getPartnerInstanceId());
        c.setHeartbeatIntervalSeconds(src.getHeartbeatIntervalSeconds());
        c.setFailoverThresholdSeconds(src.getFailoverThresholdSeconds());
        c.setSyncEnabled(src.isSyncEnabled());
        c.setSyncIntervalSeconds(src.getSyncIntervalSeconds());
        c.setPartnerRestUrl(src.getPartnerRestUrl());
        c.setSyncUsername(src.getSyncUsername());
        c.setSyncPassword(src.getSyncPassword());
        c.setSyncExcludes(src.getSyncExcludes());
        return c;
    }

    private static String nvl(Object o) {
        return o != null ? o.toString() : "--";
    }
}
