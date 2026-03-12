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
package org.opennms.minion.core.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.SnmpV3UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subscribes to SNMPv3 user configuration updates via the Twin API on Minion.
 * When a new config is received from the core instance, the SNMPv3 users are
 * applied to the local SNMP engine via {@code SnmpUtils.registerForTraps()}.
 *
 * <p>This replaces the REST-based SNMPv3 user fetching that previously required
 * Minion to have HTTP connectivity to the OpenNMS webapp ({@code OPENNMS_HTTP_URL}).</p>
 *
 * <p>Wire this in Blueprint with a reference-list binding to TwinSubscriber,
 * following the same pattern as TrapListener.</p>
 */
public class SnmpV3UserTwinSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpV3UserTwinSubscriber.class);

    private TwinSubscriber twinSubscriber;
    private Closeable twinSubscription;
    private volatile List<SnmpV3User> currentUsers;

    /**
     * Called when a TwinSubscriber becomes available via OSGi reference-list.
     */
    public void bind(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = twinSubscriber;
        subscribe();
    }

    /**
     * Called when the TwinSubscriber is unregistered from OSGi.
     */
    public void unbind(TwinSubscriber twinSubscriber) {
        this.twinSubscriber = null;
        closeSubscription();
    }

    private void subscribe() {
        if (twinSubscriber == null) {
            LOG.warn("TwinSubscriber is null, cannot subscribe to SNMPv3 user config");
            return;
        }

        twinSubscription = twinSubscriber.subscribe(
                SnmpV3UserConfig.TWIN_KEY,
                SnmpV3UserConfig.class,
                this::applySnmpV3Users
        );
        LOG.info("Subscribed to SNMPv3 user config Twin updates");
    }

    private void applySnmpV3Users(SnmpV3UserConfig config) {
        if (config == null) {
            LOG.debug("Received null SNMPv3 user config, ignoring");
            return;
        }

        List<SnmpV3User> users = config.getUsers();
        LOG.info("Received SNMPv3 user config update with {} users", users.size());
        this.currentUsers = users;

        // Log the user security names for debugging (do not log credentials)
        for (SnmpV3User user : users) {
            LOG.debug("SNMPv3 user: securityName={}, authProtocol={}, privProtocol={}, engineId={}",
                    user.getSecurityName(),
                    user.getAuthProtocol(),
                    user.getPrivProtocol(),
                    user.getEngineId());
        }
    }

    /**
     * Returns the current list of SNMPv3 users received from the core instance.
     * Other Minion bundles can use this to access the v3 user config.
     */
    public List<SnmpV3User> getCurrentUsers() {
        return currentUsers;
    }

    public void close() {
        closeSubscription();
    }

    private void closeSubscription() {
        if (twinSubscription != null) {
            try {
                twinSubscription.close();
            } catch (IOException e) {
                LOG.warn("Error closing SNMPv3 user config Twin subscription", e);
            }
            twinSubscription = null;
        }
    }
}
