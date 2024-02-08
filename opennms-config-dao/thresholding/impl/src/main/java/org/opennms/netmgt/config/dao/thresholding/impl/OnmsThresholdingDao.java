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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.common.api.SaveableConfigContainer;
import org.opennms.netmgt.config.dao.common.impl.FileSystemSaveableConfigContainer;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.threshd.Group;
import org.opennms.netmgt.config.threshd.ThresholdingConfig;

import com.google.common.annotations.VisibleForTesting;

public class OnmsThresholdingDao extends AbstractThresholdingDao implements WriteableThresholdingDao {
    private final SaveableConfigContainer<ThresholdingConfig> saveableConfigContainer;
    private final ConfigReloadContainer<ThresholdingConfig> extContainer;
    private final ObjectMapper objectMapper = JacksonUtils.createDefaultObjectMapper();
    private volatile ThresholdingConfig filesystemConfig;

    @VisibleForTesting
    OnmsThresholdingDao(JsonStore jsonStore, File configFile) {
        super(jsonStore);
        Objects.requireNonNull(configFile);
        extContainer = new ConfigReloadContainer.Builder<>(ThresholdingConfig.class)
                .withFolder((accumulator, next) -> accumulator.getGroups().addAll(next.getGroups()))
                .build();
        saveableConfigContainer = new FileSystemSaveableConfigContainer<>(ThresholdingConfig.class, "thresholds",
                Collections.singleton(this::fileSystemConfigUpdated), configFile);

        reload();
    }

    public OnmsThresholdingDao(JsonStore jsonStore) throws IOException {
        this(jsonStore, ConfigFileConstants.getFile(ConfigFileConstants.THRESHOLDING_CONF_FILE_NAME));
    }

    @Override
    public void saveConfig() {
        saveableConfigContainer.saveConfig();
    }

    /**
     * @return the merged configuration consisting of the filesystem configuration and any configuration provided by
     * extensions
     */
    @Override
    public ThresholdingConfig getReadOnlyConfig() {
        return getMergedConfig();
    }

    /**
     * @return just the configuration from the filesystem since configuration provided by extensions is read only
     */
    @Override
    public ThresholdingConfig getWriteableConfig() {
        return saveableConfigContainer.getConfig();
    }

    private synchronized ThresholdingConfig getMergedConfig() {
        ThresholdingConfig externalConfig = extContainer.getObject();

        if (filesystemConfig == null && externalConfig == null) {
            return null;
        } else if (externalConfig == null) {
            return filesystemConfig;
        } else if (filesystemConfig == null) {
            return externalConfig;
        }

        // Create a merged config by combining the config from filesystem and the external config provided by extensions
        ThresholdingConfig mergedConfig = new ThresholdingConfig();

        List<Group> groups = new ArrayList<>();
        groups.addAll(filesystemConfig.getGroups());
        groups.addAll(externalConfig.getGroups());
        mergedConfig.setGroups(Collections.unmodifiableList(groups));

        return mergedConfig;
    }

    private synchronized void publishMergedConfig() {
        try {
            jsonStore.put(JSON_STORE_KEY, objectMapper.writeValueAsString(getMergedConfig()),
                    ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload() {
        saveableConfigContainer.reload();
    }

    @Override
    public void onConfigChanged() {
        publishMergedConfig();
    }

    private synchronized void fileSystemConfigUpdated(ThresholdingConfig updatedConfig) {
        filesystemConfig = updatedConfig;
        onConfigChanged();
    }
}
