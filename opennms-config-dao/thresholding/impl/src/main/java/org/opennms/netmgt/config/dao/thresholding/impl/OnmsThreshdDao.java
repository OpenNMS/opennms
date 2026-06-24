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
package org.opennms.netmgt.config.dao.thresholding.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

public class OnmsThreshdDao extends AbstractThreshdDao implements WriteableThreshdDao {
    public static final String CONFIG_NAME = "threshd-config";

    private final ConfigReloadContainer<ThreshdConfiguration> extContainer;
    private final ObjectMapper objectMapper = JacksonUtils.createDefaultObjectMapper();
    private volatile ThreshdConfiguration dbConfig;
    private volatile ConfigurationManagerService cms;
    private volatile boolean initialized;

    public OnmsThreshdDao(JsonStore jsonStore) {
        super(jsonStore);
        extContainer = new ConfigReloadContainer.Builder<>(ThreshdConfiguration.class)
                .withFolder((accumulator, next) -> accumulator.getPackages().addAll(next.getPackages()))
                .build();
        // No I/O in the constructor — CMS is looked up lazily on first access (matches SnmpPeerFactory).
    }

    private ConfigurationManagerService getCms() {
        if (cms == null) {
            synchronized (this) {
                if (cms == null) {
                    cms = BeanUtils.getBean("daoContext", "configurationManagerService", ConfigurationManagerService.class);
                    cms.registerEventHandler(EventType.UPDATE,
                            new ConfigUpdateInfo(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID),
                            info -> onConfigChanged());
                }
            }
        }
        return cms;
    }

    private void ensureInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    // Set the flag first so any reentrant calls during reload() (e.g. from IPMap construction
                    // via super.reload() → getReadOnlyConfig()) don't recurse back into reload().
                    initialized = true;
                    reload();
                }
            }
        }
    }

    /**
     * @return the merged configuration consisting of the database configuration and any configuration provided by
     * extensions
     */
    @Override
    public ThreshdConfiguration getReadOnlyConfig() {
        ensureInitialized();
        return getMergedConfig();
    }

    /**
     * @return just the configuration from the database since configuration provided by extensions is read only
     */
    @Override
    public ThreshdConfiguration getWriteableConfig() {
        ensureInitialized();
        return dbConfig;
    }

    @Override
    public synchronized void reload() {
        dbConfig = loadFromDb();
        super.reload();
        publishMergedConfig();
    }

    @Override
    public void saveConfig() {
        ThreshdConfiguration config = dbConfig;
        if (config == null) {
            throw new IllegalStateException("No threshd configuration loaded; cannot save");
        }
        getCms().updateConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID,
                new JsonAsString(ConfigConvertUtil.objectToJson(config)), true);
    }

    @Override
    public void onConfigChanged() {
        reload();
    }

    private ThreshdConfiguration loadFromDb() {
        return getCms().getJSONStrConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID)
                .map(json -> ConfigConvertUtil.jsonToObject(json, ThreshdConfiguration.class))
                .orElse(null);
    }

    private synchronized ThreshdConfiguration getMergedConfig() {
        ThreshdConfiguration externalConfig = extContainer.getObject();

        if (dbConfig == null && externalConfig == null) {
            return null;
        } else if (externalConfig == null) {
            return dbConfig;
        } else if (dbConfig == null) {
            return externalConfig;
        }

        // Create a merged config by combining the config from the database and the external config provided by extensions
        ThreshdConfiguration mergedConfig = new ThreshdConfiguration();

        List<Package> mergedPackages = new ArrayList<>();
        mergedPackages.addAll(dbConfig.getPackages());
        mergedPackages.addAll(externalConfig.getPackages());
        mergedConfig.setPackages(Collections.unmodifiableList(mergedPackages));

        return mergedConfig;
    }

    private synchronized void publishMergedConfig() {
        ThreshdConfiguration merged = getMergedConfig();
        if (merged == null) {
            return;
        }
        try {
            jsonStore.put(JSON_STORE_KEY, objectMapper.writeValueAsString(merged),
                    ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
