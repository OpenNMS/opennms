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
package org.opennms.netmgt.dao.jaxb;

import org.apache.commons.lang3.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.opennms.netmgt.dao.api.TrapdConfigDao;
import org.opennms.netmgt.dao.jaxb.callback.ConfigurationReloadEventCallback;
import org.opennms.netmgt.events.api.EventForwarder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class DefaultTrapdConfigDao extends AbstractCmJaxbConfigDao<TrapdConfiguration> implements TrapdConfigDao {
    public static final String CONFIG_NAME = "trapd-config";
    private static final String PASSPHRASE_PLACEHOLDER = "********";

    @Autowired
    private EventForwarder eventForwarder;

    public DefaultTrapdConfigDao() {
        super(TrapdConfiguration.class, "Trapd Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public TrapdConfiguration getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    public TrapdConfiguration getMaskedConfig() {
        return this.maskPassphrases(this.getConfig(this.getDefaultConfigId()));
    }

    @Override
    public void updateConfig(final TrapdConfiguration config) {
        this.updateConfig(this.getDefaultConfigId(), ConfigConvertUtil.objectToJson(config), true);
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback(){
        return new ConfigurationReloadEventCallback(eventForwarder, this);
    }

    @Override
    public Consumer getValidationCallback() {
        return super.getValidationCallback();
    }

    @Override
    public void updateConfigWithoutUsers(TrapdConfiguration config) {
        this.updateConfig(mergeTrapdConfiguration(config));
    }
    
    /**
     * Merges the given payload into the current TrapdConfiguration.
     * Only non-null fields in the payload will overwrite the current config.
     * If payload is null, returns the current config as-is.
     *
     * @param payload the TrapdConfiguration with new values
     * @return the merged TrapdConfiguration
     */
    private TrapdConfiguration mergeTrapdConfiguration(final TrapdConfiguration payload) {
        TrapdConfiguration config = this.getConfig();
        if (config == null) {
            config = new TrapdConfiguration();
        }
        if (payload == null) {
            return config;
        }

        // Merge String fields (null-safe, value-safe)
        if (payload.getSnmpTrapAddress() != null && !java.util.Objects.equals(payload.getSnmpTrapAddress(), config.getSnmpTrapAddress())) {
            config.setSnmpTrapAddress(payload.getSnmpTrapAddress());
        }

        // Merge int fields (always set, unless you want to skip 0)
        if (payload.getSnmpTrapPort() != config.getSnmpTrapPort()) {
            config.setSnmpTrapPort(payload.getSnmpTrapPort());
        }
        if (payload.getThreads() != config.getThreads()) {
            config.setThreads(payload.getThreads());
        }
        if (payload.getQueueSize() != config.getQueueSize()) {
            config.setQueueSize(payload.getQueueSize());
        }
        if (payload.getBatchSize() != config.getBatchSize()) {
            config.setBatchSize(payload.getBatchSize());
        }
        if (payload.getBatchInterval() != config.getBatchInterval()) {
            config.setBatchInterval(payload.getBatchInterval());
        }

        // Merge boolean fields (always set)
        config.setNewSuspectOnTrap(payload.getNewSuspectOnTrap());
        config.setIncludeRawMessage(payload.isIncludeRawMessage());

        // Merge useAddressFromVarbind (nullable Boolean)
        try {
            java.lang.reflect.Field field = payload.getClass().getDeclaredField("useAddressFromVarbind");
            field.setAccessible(true);
            Boolean useAddressFromVarbind = (Boolean) field.get(payload);
            if (useAddressFromVarbind != null) {
                config.setUseAddressFromVarbind(useAddressFromVarbind);
            }
        } catch (Exception e) {
            // ignore, do not set if not accessible
        }

        // Merge snmpv3User list if present and not empty
        java.util.List<Snmpv3User> snmpv3UserList = payload.getSnmpv3UserCollection();
        if (snmpv3UserList != null && !snmpv3UserList.isEmpty()) {
            config.setSnmpv3User(snmpv3UserList);
        }

        return config;
    }

    /**
     * Returns a deep copy of the given {@link TrapdConfiguration} with
     * {@code authPassphrase} and {@code privacyPassphrase} replaced by
     * {@link #PASSPHRASE_PLACEHOLDER} so that real credentials are never
     * exposed over the REST API.
     *
     * @param config the TrapdConfiguration to sanitize
     * @return a sanitized deep copy with passphrases masked, or null if input is null
     */
    private TrapdConfiguration maskPassphrases(final TrapdConfiguration config) {
        if (config == null) {
            return null;
        }
        TrapdConfiguration sanitized;
        try {
            sanitized = JaxbUtils.unmarshal(
                    TrapdConfiguration.class, JaxbUtils.marshal(config));
        } catch (Exception e) {
            // Optionally log the error
            throw new RuntimeException("Failed to deep copy TrapdConfiguration for masking", e);
        }
        if (sanitized.getSnmpv3UserCollection() != null) {
            for (final Snmpv3User user : sanitized.getSnmpv3UserCollection()) {
                if (!StringUtils.isBlank(user.getAuthPassphrase())) {
                    user.setAuthPassphrase(PASSPHRASE_PLACEHOLDER);
                }
                if (!StringUtils.isBlank(user.getPrivacyPassphrase())) {
                    user.setPrivacyPassphrase(PASSPHRASE_PLACEHOLDER);
                }
            }
        }
        return sanitized;
    }
}
