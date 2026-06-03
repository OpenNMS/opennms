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

import org.opennms.netmgt.ha.HaConfiguration;
import org.opennms.netmgt.ha.HaStartupCoordinator;

/**
 * Karaf shell command: {@code opennms:ha-config}
 *
 * <p>Displays the HA configuration for this instance, equivalent to
 * {@code GET /rest/ha/config}.
 */
@Command(scope = "opennms", name = "ha-config", description = "Display the HA configuration for this instance.")
@Service
public class HaConfigCommand implements Action {

    private static final String FMT = "  %-30s %s%n";

    @Override
    public Object execute() throws Exception {
        HaStartupCoordinator coord = HaStartupCoordinator.getInstance();
        if (coord == null) {
            System.out.println("HA is not enabled on this instance.");
            return null;
        }

        HaConfiguration cfg = coord.getConfig();

        System.out.println("HA Configuration");
        System.out.println("================");
        System.out.printf(FMT, "Enabled:",            cfg.isEnabled());
        System.out.printf(FMT, "Instance ID:",         nvl(cfg.getInstanceId()));
        System.out.printf(FMT, "Role:",                nvl(cfg.getRole()));
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

        return null;
    }

    private static String nvl(Object o) {
        return o != null ? o.toString() : "--";
    }
}
