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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.SnmpV3UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes SNMPv3 user configuration to Minion instances via the Twin API.
 * This replaces the REST-based {@code getSnmpV3Users()} endpoint that previously
 * required Minion to have HTTP connectivity to the OpenNMS webapp.
 *
 * <p>Extracts unique SNMPv3 users from all definitions in snmp-config.xml
 * and publishes them as a {@link SnmpV3UserConfig} via {@link TwinPublisher}.</p>
 *
 * <p>Wire this bean in the webapp's Spring context with {@code init-method="init"}
 * and {@code destroy-method="close"}.</p>
 */
public class SnmpV3UserTwinPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpV3UserTwinPublisher.class);

    private final TwinPublisher twinPublisher;
    private TwinPublisher.Session<SnmpV3UserConfig> twinSession;

    public SnmpV3UserTwinPublisher(TwinPublisher twinPublisher) {
        this.twinPublisher = twinPublisher;
    }

    public void init() {
        try {
            twinSession = twinPublisher.register(SnmpV3UserConfig.TWIN_KEY, SnmpV3UserConfig.class);
            publishCurrentConfig();
            LOG.info("SNMPv3 user config Twin publisher initialized with {} users",
                    buildConfigFromSnmpPeerFactory().getUsers().size());
        } catch (IOException e) {
            LOG.error("Failed to register SNMPv3 user config Twin publisher", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Republishes the current SNMPv3 user configuration. Call this after
     * snmp-config.xml has been modified (e.g., on a reloadDaemonConfig event).
     */
    public void publishCurrentConfig() {
        if (twinSession == null) {
            LOG.warn("Twin session not initialized, cannot publish SNMPv3 user config");
            return;
        }
        try {
            SnmpV3UserConfig config = buildConfigFromSnmpPeerFactory();
            twinSession.publish(config);
            LOG.debug("Published SNMPv3 user config with {} users", config.getUsers().size());
        } catch (IOException e) {
            LOG.error("Failed to publish SNMPv3 user config via Twin", e);
        }
    }

    public void close() {
        if (twinSession != null) {
            try {
                twinSession.close();
            } catch (IOException e) {
                LOG.warn("Error closing SNMPv3 user config Twin session", e);
            }
        }
    }

    /**
     * Extracts unique SNMPv3 users from all definitions in the SNMP config.
     * A definition is considered v3 if it has a non-empty securityName.
     */
    private SnmpV3UserConfig buildConfigFromSnmpPeerFactory() {
        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        Set<SnmpV3User> uniqueUsers = new LinkedHashSet<>();

        if (snmpConfig != null && snmpConfig.getDefinitions() != null) {
            for (Definition def : snmpConfig.getDefinitions()) {
                String securityName = def.getSecurityName();
                if (securityName != null && !securityName.isEmpty()) {
                    SnmpV3User user = new SnmpV3User();
                    user.setSecurityName(securityName);
                    user.setAuthProtocol(def.getAuthProtocol());
                    user.setAuthPassPhrase(def.getAuthPassphrase());
                    user.setPrivProtocol(def.getPrivacyProtocol());
                    user.setPrivPassPhrase(def.getPrivacyPassphrase());
                    user.setEngineId(def.getEngineId());
                    user.setSecurityLevel(def.getSecurityLevel());
                    uniqueUsers.add(user);
                }
            }
        }

        return new SnmpV3UserConfig(new ArrayList<>(uniqueUsers));
    }
}
