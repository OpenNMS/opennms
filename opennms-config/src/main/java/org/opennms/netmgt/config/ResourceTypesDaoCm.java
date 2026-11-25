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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.json.JSONObject;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource types stored in the configuration manager, schema {@value #CONFIG_NAME}.
 *
 * <p>The schema allows multiple configurations: every file of the former {@code etc/resource-types.d/} became a
 * configuration named after the file, e.g. {@code postgresql-resource}. The configurations are merged sorted by id,
 * which is the order the files used to be read in. When a resource type name is defined more than once, the
 * later configuration wins, as the later file did.</p>
 *
 * <p>The bean lives in the common context, but the configuration manager lives in the DAO context, so the DAO
 * context binds it with {@link #bind(ConfigurationManagerService, JsonStore)}. Until then only the resource types of
 * the data collection configuration and of extensions are known.</p>
 *
 * <p>Every reload publishes the stored resource types to the {@link JsonStore}, where Sentinel picks them up.</p>
 *
 * <p>The write methods store a configuration in the configuration manager, which validates it and calls
 * {@link #reload()} on the calling thread before it returns. They are not synchronized: the configuration manager
 * calls the handlers with a lock of its own held. Callers that need read-modify-write consistency serialize their
 * writes themselves.</p>
 */
public class ResourceTypesDaoCm extends AbstractResourceTypesDao {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypesDaoCm.class);

    public static final String CONFIG_NAME = "resource-types";

    private final AtomicLong lastUpdate = new AtomicLong(0);
    private volatile List<ResourceType> resourceTypes = Collections.emptyList();
    private volatile ConfigurationManagerService cms;
    private volatile JsonStore jsonStore;

    /**
     * Connects the DAO to the configuration manager, registers for changes of any configuration of the schema and
     * loads them. Must not be called while holding a lock of this DAO: the configuration manager calls the handlers
     * with a lock of its own held.
     */
    public void bind(final ConfigurationManagerService cms, final JsonStore jsonStore) {
        Objects.requireNonNull(cms);
        if (this.cms == cms && this.jsonStore == jsonStore) {
            return;
        }
        this.cms = cms;
        this.jsonStore = jsonStore;

        final ConfigUpdateInfo allConfigs = new ConfigUpdateInfo(CONFIG_NAME, ConfigUpdateInfo.WILDCARD_ID);
        cms.registerEventHandler(EventType.VALIDATE, allConfigs, getValidationCallback());
        final Consumer<ConfigUpdateInfo> reload = info -> reload();
        cms.registerEventHandler(EventType.CREATE, allConfigs, reload);
        cms.registerEventHandler(EventType.UPDATE, allConfigs, reload);
        cms.registerEventHandler(EventType.DELETE, allConfigs, reload);
        reload();
    }

    /**
     * Rejects configurations that pass the schema but can't be read into {@link ResourceTypes}.
     */
    static Consumer<ConfigUpdateInfo> getValidationCallback() {
        return configUpdateInfo -> {
            final JSONObject json = configUpdateInfo.getConfigJson();
            if (json == null) {
                throw new ValidationException(String.format("%s config %s is empty.",
                        configUpdateInfo.getConfigName(), configUpdateInfo.getConfigId()));
            }
            try {
                ConfigConvertUtil.jsonToObject(json.toString(), ResourceTypes.class);
            } catch (final ConfigConversionException e) {
                throw new ValidationException(ConfigConvertUtil.rootCauseMessage(e));
            }
        };
    }

    /**
     * Reads all configurations of the schema, merges them and publishes the result to the {@link JsonStore}.
     * A configuration that can't be read is logged and ignored, so it doesn't hide the others.
     */
    public synchronized void reload() {
        final ConfigurationManagerService configurationManagerService = this.cms;
        if (configurationManagerService == null) {
            LOG.debug("Not bound to the configuration manager yet, nothing to reload.");
            return;
        }
        final Map<String, JSONObject> configs = new TreeMap<>(configurationManagerService.getConfigData(CONFIG_NAME)
                .map(ConfigData::getConfigs)
                .orElse(Collections.emptyMap()));

        final Map<String, ResourceType> byName = new LinkedHashMap<>();
        final Map<String, String> origin = new TreeMap<>();
        for (final Map.Entry<String, JSONObject> config : configs.entrySet()) {
            final ResourceTypes types;
            try {
                types = ConfigConvertUtil.jsonToObject(config.getValue().toString(), ResourceTypes.class);
            } catch (final ConfigConversionException e) {
                // e.g. imported by the upgrade, which can't run the validation callback
                LOG.error("Configuration {} of {} can not be read and is ignored: {}", config.getKey(), CONFIG_NAME, ConfigConvertUtil.rootCauseMessage(e));
                continue;
            }
            for (final ResourceType type : types.getResourceTypes()) {
                final String previous = origin.put(type.getName(), config.getKey());
                if (previous != null) {
                    LOG.warn("Resource type '{}' is defined by configuration '{}' and '{}' of {}, '{}' is used.",
                            type.getName(), previous, config.getKey(), CONFIG_NAME, config.getKey());
                    // keep the position of the later definition, like a later file did
                    byName.remove(type.getName());
                }
                byName.put(type.getName(), type);
            }
        }

        resourceTypes = Collections.unmodifiableList(new ArrayList<>(byName.values()));
        // strictly increasing, so DefaultResourceDao notices two changes within the same millisecond
        lastUpdate.updateAndGet(previous -> Math.max(previous + 1, System.currentTimeMillis()));
        LOG.info("Loaded {} resource types from {} configurations of {}.", resourceTypes.size(), configs.size(), CONFIG_NAME);
        publish(resourceTypes);
    }

    /**
     * @return all configurations of the schema by id, sorted by id. A configuration that can't be read is left out.
     */
    public SortedMap<String, ResourceTypes> getConfigurations() {
        final Map<String, JSONObject> configs = requireCms().getConfigData(CONFIG_NAME)
                .map(ConfigData::getConfigs)
                .orElse(Collections.emptyMap());
        final SortedMap<String, ResourceTypes> result = new TreeMap<>();
        for (final Map.Entry<String, JSONObject> config : configs.entrySet()) {
            try {
                result.put(config.getKey(), ConfigConvertUtil.jsonToObject(config.getValue().toString(), ResourceTypes.class));
            } catch (final ConfigConversionException e) {
                LOG.error("Configuration {} of {} can not be read and is ignored: {}", config.getKey(), CONFIG_NAME, ConfigConvertUtil.rootCauseMessage(e));
            }
        }
        return result;
    }

    /**
     * @return the configuration with the given id, empty if there is none or it can't be read
     */
    public Optional<ResourceTypes> getConfiguration(final String configId) {
        Objects.requireNonNull(configId);
        return Optional.ofNullable(getConfigurations().get(configId));
    }

    /**
     * Stores a new configuration.
     *
     * @throws org.opennms.features.config.exception.ConfigAlreadyExistsException if the id is taken
     * @throws ValidationException if the configuration is invalid
     */
    public void createConfiguration(final String configId, final ResourceTypes resourceTypes) {
        Objects.requireNonNull(configId);
        requireCms().registerConfiguration(CONFIG_NAME, configId, toJson(resourceTypes));
    }

    /**
     * Replaces an existing configuration as a whole, so removing the last resource type is stored as well.
     *
     * @throws org.opennms.features.config.exception.ConfigNotFoundException if there is no such configuration
     * @throws ValidationException if the configuration is invalid
     */
    public void updateConfiguration(final String configId, final ResourceTypes resourceTypes) {
        Objects.requireNonNull(configId);
        requireCms().updateConfiguration(CONFIG_NAME, configId, toJson(resourceTypes), true);
    }

    /**
     * Removes a configuration.
     *
     * @throws org.opennms.features.config.exception.ConfigNotFoundException if there is no such configuration
     */
    public void deleteConfiguration(final String configId) {
        Objects.requireNonNull(configId);
        requireCms().unregisterConfiguration(CONFIG_NAME, configId);
    }

    private static JsonAsString toJson(final ResourceTypes resourceTypes) {
        Objects.requireNonNull(resourceTypes);
        return new JsonAsString(ConfigConvertUtil.objectToJson(resourceTypes));
    }

    private ConfigurationManagerService requireCms() {
        final ConfigurationManagerService configurationManagerService = this.cms;
        if (configurationManagerService == null) {
            throw new IllegalStateException("Not bound to the configuration manager yet.");
        }
        return configurationManagerService;
    }

    private void publish(final List<ResourceType> types) {
        final JsonStore store = this.jsonStore;
        if (store == null) {
            return;
        }
        final ResourceTypes document = new ResourceTypes();
        document.setResourceTypes(new ArrayList<>(types));
        try {
            // Jackson 1, as all configurations in the JSON store, see PollingJsonStoreReloadableConfigContainer
            store.put(JSON_STORE_KEY, JacksonUtils.createDefaultObjectMapper().writeValueAsString(document),
                    ConfigDaoConstants.JSON_KEY_STORE_CONTEXT);
        } catch (final Exception e) {
            // the configuration is stored and in use here, only Sentinel misses the change
            LOG.error("Failed to publish the resource types to the JSON store, Sentinel doesn't see the change: {}", e.getMessage(), e);
        }
    }

    @Override
    protected List<ResourceType> getStoredResourceTypes() {
        return resourceTypes;
    }

    @Override
    protected long getStoredLastUpdate() {
        return lastUpdate.get();
    }
}
