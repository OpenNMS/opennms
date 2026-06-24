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
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

public class OnmsThresholdingDao extends AbstractThresholdingDao implements WriteableThresholdingDao {
    public static final String CONFIG_NAME = "thresholding-config";

    private final ConfigReloadContainer<ThresholdingConfig> extContainer;
    private final ObjectMapper objectMapper = JacksonUtils.createDefaultObjectMapper();
    private volatile ThresholdingConfig dbConfig;
    private volatile ConfigurationManagerService cms;
    private volatile boolean initialized;

    public OnmsThresholdingDao(JsonStore jsonStore) {
        super(jsonStore);
        extContainer = new ConfigReloadContainer.Builder<>(ThresholdingConfig.class)
                .withFolder((accumulator, next) -> accumulator.getGroups().addAll(next.getGroups()))
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
                    initialized = true;
                    reload();
                }
            }
        }
    }

    @Override
    public void saveConfig() {
        ThresholdingConfig config = dbConfig;
        if (config == null) {
            throw new IllegalStateException("No thresholding configuration loaded; cannot save");
        }
        getCms().updateConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID,
                new JsonAsString(ConfigConvertUtil.objectToJson(config)), true);
    }

    /**
     * @return the merged configuration consisting of the database configuration and any configuration provided by
     * extensions
     */
    @Override
    public ThresholdingConfig getReadOnlyConfig() {
        ensureInitialized();
        return getMergedConfig();
    }

    /**
     * @return just the configuration from the database since configuration provided by extensions is read only
     */
    @Override
    public ThresholdingConfig getWriteableConfig() {
        ensureInitialized();
        return dbConfig;
    }

    @Override
    public synchronized void reload() {
        dbConfig = loadFromDb();
        publishMergedConfig();
    }

    @Override
    public void onConfigChanged() {
        reload();
    }

    private ThresholdingConfig loadFromDb() {
        return getCms().getJSONStrConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID)
                .map(json -> ConfigConvertUtil.jsonToObject(json, ThresholdingConfig.class))
                .orElse(null);
    }

    private synchronized ThresholdingConfig getMergedConfig() {
        ThresholdingConfig externalConfig = extContainer.getObject();

        if (dbConfig == null && externalConfig == null) {
            return null;
        } else if (externalConfig == null) {
            return dbConfig;
        } else if (dbConfig == null) {
            return externalConfig;
        }

        // Create a merged config by combining the config from the database and the external config provided by extensions
        ThresholdingConfig mergedConfig = new ThresholdingConfig();

        List<Group> groups = new ArrayList<>();
        groups.addAll(dbConfig.getGroups());
        groups.addAll(externalConfig.getGroups());
        mergedConfig.setGroups(Collections.unmodifiableList(groups));

        return mergedConfig;
    }

    private synchronized void publishMergedConfig() {
        ThresholdingConfig merged = getMergedConfig();
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
