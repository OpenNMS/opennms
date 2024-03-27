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
package org.opennms.features.config.service.impl;

import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.CmJaxbConfigDao;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.BeanFieldCopyUtil;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * <p>Abstract AbstractCmJaxbConfigDao class.</p>
 *
 * @param <E> Configuration class
 * @version $Id: $
 */
public abstract class AbstractCmJaxbConfigDao<E> implements CmJaxbConfigDao {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCmJaxbConfigDao.class);

    @Autowired
    protected ConfigurationManagerService configurationManagerService;

    private Class<E> entityClass;
    private String description;
    private ConcurrentHashMap<String, E> lastKnownEntityMap = new ConcurrentHashMap<>();

    /**
     * <p>Constructor for AbstractJaxbConfigDao.</p>
     *
     * @param entityClass a {@link java.lang.Class} object.
     * @param description a {@link java.lang.String} object.
     * @see #getUpdateCallback()
     */
    protected AbstractCmJaxbConfigDao(final Class<E> entityClass, final String description) {
        this.entityClass = Objects.requireNonNull(entityClass);
        this.description = Objects.requireNonNull(description);
    }

    /**
     * Add default callback
     */
    @PostConstruct
    public void postConstruct() {
        Set<String> configIds = configurationManagerService.getConfigIds(this.getConfigName());
        configIds.forEach(configId -> this.addOnReloadedCallback(configId, this.getUpdateCallback()));
        var validateCallback = this.getValidationCallback();
        if (validateCallback != null) {
            this.addValidationCallback(validateCallback);
        }
    }

    public E loadConfig() {
        return this.loadConfig(this.getDefaultConfigId());
    }

    /**
     * loadConfig from database by CM. If it is already in cache, it will update the cache.
     *
     * @param configId
     * @return ConfigObject
     */
    public E loadConfig(final String configId) {
        long startTime = System.currentTimeMillis();

        LOG.debug("Loading {} configuration from {}", description, configId);
        Optional<E> configOptional = configurationManagerService.getJSONStrConfiguration(this.getConfigName(), configId)
                .map(s -> ConfigConvertUtil.jsonToObject(s, entityClass)); // no validation since we validated already at write time
        if (configOptional.isEmpty()) {
            throw new ConfigNotFoundException("NOT_FOUND: configName: " + this.getConfigName() + " configId: " + configId);
        }
        final E config = configOptional.get();
        long endTime = System.currentTimeMillis();
        LOG.info("Loaded {} in {} ms", getDescription(), (endTime - startTime));

        return lastKnownEntityMap.compute(configId, (k, v) -> {
            if (v != null) {
                BeanFieldCopyUtil.copyFields(config, v);
                return v;
            } else {
                return config;
            }
        });
    }

    public E getConfig(String configId) {
        // cannot use computeIfAbsent, it will cause IllegalStateException
        E config = lastKnownEntityMap.get(configId);
        if (config != null) {
            return config;
        }
        return this.loadConfig(configId);
    }

    /**
     * Update config with configId
     *
     * @param configId
     * @param config
     * @throws ValidationException
     */
    public void updateConfig(String configId, E config) throws ValidationException {
        this.updateConfig(configId, ConfigConvertUtil.objectToJson(config));
    }

    /**
     * It is expected to have json String input
     *
     * @param configId
     * @param jsonConfigString
     * @throws ValidationException
     */
    public void updateConfig(String configId, String jsonConfigString) throws ValidationException {
        this.updateConfig(configId, jsonConfigString, false);
    }

    public void updateConfig(String configId, String jsonConfigString, boolean isReplace) throws ValidationException {
        configurationManagerService.updateConfiguration(this.getConfigName(), configId, new JsonAsString(jsonConfigString), isReplace);
    }

    /**
     * it will update the default config
     *
     * @param configJsonStr
     * @throws ValidationException
     * @see #updateConfig(String, String)
     */
    public void updateConfig(String configJsonStr) throws ValidationException {
        this.updateConfig(this.getDefaultConfigId(), configJsonStr);
    }

    public void updateConfig(E config) throws ValidationException {
        this.updateConfig(this.getDefaultConfigId(), config);
    }

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param callback a callback that will be called when the entity maintained by this DAO is reloaded
     */
    public void addOnReloadedCallback(String configId, Consumer<ConfigUpdateInfo> callback) {
        Objects.requireNonNull(callback);
        configurationManagerService.registerEventHandler(EventType.UPDATE, new ConfigUpdateInfo(this.getConfigName(), configId), callback);
    }

    public void addValidationCallback(Consumer<ConfigUpdateInfo> callback) {
        Objects.requireNonNull(callback);
        configurationManagerService.registerEventHandler(EventType.VALIDATE, new ConfigUpdateInfo(this.getConfigName()), callback);
    }
}
