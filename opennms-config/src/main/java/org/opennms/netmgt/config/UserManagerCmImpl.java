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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

/**
 * CM-backed UserManager: stores users in the kvstore_jsonb table (via ConfigurationManagerService)
 * instead of the users.xml file on disk. This survives Kubernetes pod restarts.
 *
 * The Liquibase migration in changelog-cm/36.0.0 imports the initial users.xml into CM
 * at startup. Afterwards all reads and writes go through this class.
 *
 * Thread safety: inherited read/write locks from UserManager guard m_users. The CM
 * UPDATE event callback only sets a flag (no lock acquired), and the actual reload
 * happens lazily in doUpdate() before the next operation acquires any lock.
 */
public class UserManagerCmImpl extends UserManager {
    private static final Logger LOG = LoggerFactory.getLogger(UserManagerCmImpl.class);

    public static final String CONFIG_NAME = "users-config";

    // required=false so that test contexts that load applicationContext-commonConfigs.xml
    // without a CM service (e.g. topology or linkd ITs) can still load the Spring context.
    // In production, CM is always present and init() does the real work.
    @Autowired(required = false)
    private ConfigurationManagerService configurationManagerService;

    private volatile long m_lastModified = System.currentTimeMillis();
    // Set to true by the CM update callback; cleared and acted on by doUpdate().
    private volatile boolean m_needsReload = false;

    public UserManagerCmImpl(final GroupManager groupManager) {
        super(groupManager);
    }

    @PostConstruct
    public void init() throws IOException {
        if (configurationManagerService == null) {
            LOG.warn("No ConfigurationManagerService available; UserManagerCmImpl is inactive (test context without CM?)");
            return;
        }
        if (configurationManagerService.getConfigNames().contains(CONFIG_NAME)) {
            reload();
        } else {
            LOG.warn("CM schema '{}' not yet registered; users will reload on first access after CM is populated", CONFIG_NAME);
            m_needsReload = true;
        }
        configurationManagerService.registerEventHandler(
                EventType.UPDATE,
                new ConfigUpdateInfo(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID),
                this::onConfigUpdated);
    }

    private void onConfigUpdated(final ConfigUpdateInfo info) {
        // Called by CM when any client updates the users-config. Only bump the timestamp
        // and flag a reload — do NOT try to acquire any lock here (could deadlock if a
        // write operation is in progress and holds the write lock when the CM fires the
        // synchronous callback).
        m_lastModified = System.currentTimeMillis();
        m_needsReload = true;
    }

    @Override
    public synchronized void reload() throws IOException, FileNotFoundException {
        if (configurationManagerService == null) {
            throw new IOException("ConfigurationManagerService is not available");
        }
        Optional<String> jsonOpt = configurationManagerService.getJSONStrConfiguration(
                CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        if (jsonOpt.isEmpty()) {
            throw new FileNotFoundException(
                    "No users configuration found in CM for config: " + CONFIG_NAME);
        }
        String xmlStr = configurationManagerService.getConverter(CONFIG_NAME)
                .orElseThrow(() -> new IOException("No CM schema registered for " + CONFIG_NAME))
                .jsonToXml(jsonOpt.get());
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8))) {
            parseXML(bais);
        }
        m_lastModified = System.currentTimeMillis();
    }

    /**
     * Persist the marshalled Userinfo XML to CM by converting it to the XSD-based JSON
     * format (same format produced by the Liquibase import).
     */
    @Override
    protected void saveXML(final String writerString) throws IOException {
        if (configurationManagerService == null) {
            throw new IOException("ConfigurationManagerService is not available; cannot save users to CM");
        }
        try {
            String json = configurationManagerService.getConverter(CONFIG_NAME)
                    .orElseThrow(() -> new IOException("No CM schema registered for " + CONFIG_NAME))
                    .xmlToJson(writerString);
            configurationManagerService.updateConfiguration(
                    CONFIG_NAME,
                    ConfigDefinition.DEFAULT_CONFIG_ID,
                    new JsonAsString(json),
                    true);
            m_lastModified = System.currentTimeMillis();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to save users configuration to CM", e);
        }
    }

    /**
     * Called before every read/write operation. Reloads from CM if the update callback
     * signalled that the config changed externally.
     */
    @Override
    protected void doUpdate() throws IOException, FileNotFoundException {
        if (m_needsReload) {
            m_needsReload = false;
            reload();
        }
    }

    @Override
    public boolean isUpdateNeeded() {
        return m_needsReload;
    }

    /**
     * Returns the epoch-millis timestamp of the last CM load or save. Used by
     * SpringSecurityUserDaoImpl to detect when its cached user map is stale.
     */
    @Override
    public long getLastModified() {
        return m_lastModified;
    }

    @Override
    public long getFileSize() {
        return 0L;
    }
}
