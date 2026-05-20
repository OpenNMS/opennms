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

import com.google.common.base.Strings;
import org.opennms.core.config.api.TextEncryptor;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.dao.jaxb.callback.ConfigurationReloadEventCallback;
import org.opennms.netmgt.dao.jaxb.callback.SnmpConfigConfigurationValidationCallback;
import org.opennms.netmgt.events.api.EventForwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class DefaultSnmpConfigDao extends AbstractCmJaxbConfigDao<SnmpConfig> implements SnmpConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSnmpConfigDao.class);
    public static final String CONFIG_NAME = "snmp-config";
    protected static final String ENCRYPTION_ENABLED = "org.opennms.snmp.encryption.enabled";
    private final Boolean encryptionEnabled = Boolean.getBoolean(ENCRYPTION_ENABLED);

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private TextEncryptor textEncryptor;

    public DefaultSnmpConfigDao() {
        super(SnmpConfig.class, "SNMP Config");
    }

    @Override
    public String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    public SnmpConfig getConfig() {
        return this.getConfig(this.getDefaultConfigId());
    }

    @Override
    public Consumer<ConfigUpdateInfo> getUpdateCallback() {
        return new ConfigurationReloadEventCallback(eventForwarder, this);
    }

    @Override
    public Consumer<ConfigUpdateInfo> getValidationCallback() {
        return new SnmpConfigConfigurationValidationCallback();
    }

    /**
     * Override to allow use of isReplace flag.
     */
    @Override
    public void updateConfig(SnmpConfig config, boolean isReplace) {
        this.updateConfig(this.getDefaultConfigId(), ConfigConvertUtil.objectToJson(config), isReplace);
    }

    @Override
    public void postConstruct() {
        super.postConstruct();

        if (encryptionEnabled) {
            LOG.warn("Encryption/decryption of snmp-config.xml is deprecated and is no longer available. Please use the Secure Credentials Vault to encrypt any secrets.");
        }

        if (configurationManagerService.getConfigNames().contains(CONFIG_NAME)) {
            decryptConfig();
        }
    }

    void decryptConfig() {
        final SnmpConfig snmpConfig = getConfig();

        if (!snmpConfig.getEncrypted()) {
            return;
        }

        try {
            decryptConfig(snmpConfig);
            snmpConfig.getDefinitions().forEach(this::decryptConfig);
            if (snmpConfig.getSnmpProfiles() != null) {
                snmpConfig.getSnmpProfiles().getSnmpProfiles().forEach(this::decryptConfig);
            }
        } catch (Exception e) {
            LOG.error("Failed to decrypt snmp-config; leaving on-disk config untouched. Resolve the underlying error and restart to retry the migration.", e);
            return;
        }

        updateConfig(snmpConfig);
    }

    private void decryptConfig(final Configuration config) {
        if (!config.getEncrypted()) {
            return;
        }

        try {
            if (!Strings.isNullOrEmpty(config.getAuthPassphrase())) {
                String authPassPhrase = textEncryptor.decrypt(CONFIG_NAME, config.getAuthPassphrase());
                config.setAuthPassphrase(authPassPhrase);
            }
            if (!Strings.isNullOrEmpty(config.getPrivacyPassphrase())) {
                String privPassPhrase = textEncryptor.decrypt(CONFIG_NAME, config.getPrivacyPassphrase());
                config.setPrivacyPassphrase(privPassPhrase);
            }
            if (!Strings.isNullOrEmpty(config.getReadCommunity())) {
                String readCommunity = textEncryptor.decrypt(CONFIG_NAME, config.getReadCommunity());
                config.setReadCommunity(readCommunity);
            }
            if (!Strings.isNullOrEmpty(config.getWriteCommunity())) {
                String writeCommunity = textEncryptor.decrypt(CONFIG_NAME, config.getWriteCommunity());
                config.setWriteCommunity(writeCommunity);
            }
            config.setEncrypted(false);
        } catch (Exception e) {
            LOG.error("Exception while trying to decrypt snmp config", e);
        }
    }
}
