/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.config.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.exception.*;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JsonConfigStoreDaoImpl implements ConfigStoreDao<JSONObject> {
    private static final Logger LOG = LoggerFactory.getLogger(JsonConfigStoreDaoImpl.class);
    public static final String CONTEXT_CONFIG = "CM_CONFIG";
    public static final String CONTEXT_SCHEMA = "CM_SCHEMA";
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JsonOrgModule());

    private final JsonStore jsonStore;

    public JsonConfigStoreDaoImpl(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    @Override
    public void register(ConfigDefinition configDefinition) {
        this.putConfigDefinition(configDefinition);
    }

    @Override
    public Set<String> getConfigNames() {
        return this.getIds(CONTEXT_SCHEMA);
    }

    private Set<String> getIds(String context) {
        Map<String, String> allMap = jsonStore.enumerateContext(context);
        if (allMap == null) {
            return Collections.emptySet();
        }
        return allMap.keySet();
    }

    @Override
    public Map<String, ConfigDefinition> getAllConfigDefinitions() {
        Map<String, ConfigDefinition> output = new HashMap<>();
        jsonStore.enumerateContext(CONTEXT_SCHEMA).forEach((key, value) ->
                output.put(key, this.deserializeConfigDefinition(value))
        );
        return output;
    }

    private ConfigDefinition deserializeConfigDefinition(String jsonStr) {
        try {
            return mapper.readValue(jsonStr, ConfigDefinition.class);
        } catch (JsonProcessingException e) {
            throw new SchemaConversionException(jsonStr, e);
        }
    }

    @Override
    public Optional<ConfigDefinition> getConfigDefinition(String configName) {
        var jsonStr = jsonStore.get(configName, CONTEXT_SCHEMA);
        if (jsonStr.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.deserializeConfigDefinition(jsonStr.get()));
    }

    @Override
    public void updateConfigDefinition(ConfigDefinition configDefinition) throws ValidationException {
        Optional<ConfigDefinition> oldDef = this.getConfigDefinition(configDefinition.getConfigName());
        if (oldDef.isEmpty()) {
            throw new SchemaNotFoundException("ConfigName not found: " + configDefinition.getConfigName());
        }
        // check config are still valid for the new schema
        Optional<ConfigData<JSONObject>> configData = this.getConfigs(configDefinition.getConfigName());
        configData.ifPresent(jsonObjectConfigData -> this.validateConfigData(configDefinition, jsonObjectConfigData));
        this.putConfigDefinition(configDefinition);
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigs(final String configName) {
        Optional<String> configDataJsonStr = jsonStore.get(configName, CONTEXT_CONFIG);
        if (configDataJsonStr.isEmpty()) {
            return Optional.empty();
        }
        JavaType type = mapper.getTypeFactory().constructParametricType(ConfigData.class, JSONObject.class);
        try {
            return Optional.of(mapper.readValue(configDataJsonStr.get(), type));
        } catch (JsonProcessingException e) {
            throw new ConfigConversionException(configDataJsonStr.get(), e);
        }
    }

    @Override
    public void addConfigs(String configName, ConfigData<JSONObject> configData) throws ValidationException {
        Optional<ConfigData<JSONObject>> exist = this.getConfigs(configName);
        if (exist.isPresent()) {
            throw new ConfigAlreadyExistsException("Duplicate config found for service: " + configName);
        }
        this.validateConfigData(configName, configData);
        this.putConfig(configName, configData);
    }

    @Override
    public void addConfig(String configName, String configId, JSONObject configObject) throws ValidationException {
        var configData = this.getConfigs(configName).orElse(new ConfigData<>());
        Map<String, JSONObject> configs = configData.getConfigs();
        if (configs.containsKey(configId)) {
            throw new ConfigAlreadyExistsException("Duplicate config found for configId: " + configId);
        }
        this.validateConfig(configName, configObject);
        configs.put(configId, configObject);
        this.putConfig(configName, configData);
    }

    @Override
    public Optional<JSONObject> getConfig(String configName, String configId) {
        return getConfigs(configName).flatMap(configData -> Optional.ofNullable(configData.getConfigs().get(configId)));
    }

    @Override
    public void updateConfigs(String configName, ConfigData<JSONObject> configData) throws ValidationException {
        this.validateConfigData(configName, configData);
        this.putConfig(configName, configData);
    }

    @Override
    public void deleteConfig(String configName, String configId) {
        ConfigDefinition configDefinition = this
                .getConfigDefinition(configName)
                .orElseThrow(() -> new ConfigNotFoundException("ConfigDefinition not found for config=" + configName));
        ConfigData<JSONObject> configData = this
                .getConfigs(configName)
                .orElseThrow(() -> new ConfigNotFoundException("Config not found for config=" + configName + ", configId=" + configId));
        if (configData.getConfigs().size() <= 1 && !configDefinition.getAllowMultiple()) {
            throw new ConfigRuntimeException("Deletion of the last config is not allowed. " + configName + ", configId " + configId, null);
        }
        if (configData.getConfigs().remove(configId) == null) {
            throw new ConfigNotFoundException("Config not found for config " + configName + ", configId " + configId);
        }
        this.putConfig(configName, configData);
    }

    @Override
    public void unregister(String configName) {
        jsonStore.delete(configName, CONTEXT_SCHEMA);
        jsonStore.delete(configName, CONTEXT_CONFIG);
    }

    @Override
    public Map<String, JSONObject> get(String configName) {
        Optional<ConfigData<JSONObject>> configData = this.getConfigs(configName);
        if (configData.isEmpty()) {
            return Collections.emptyMap();
        }
        return configData.get().getConfigs();
    }

    private void putConfigDefinition(ConfigDefinition configDefinition) {
        try {
            long timestamp = jsonStore.put(configDefinition.getConfigName(), mapper.writeValueAsString(configDefinition),
                    CONTEXT_SCHEMA);
            if (timestamp < 0) {
                throw new ConfigIOException("Fail to put ConfigDefinition in JsonStore! configName: " + configDefinition.getConfigName(), null);
            }
        } catch (JsonProcessingException e) {
            throw new SchemaConversionException("Fail to convert Definition to String! configName: " + configDefinition.getConfigName(), e);
        }
    }

    private void putConfig(String configName, ConfigData<JSONObject> configData) {
        try {
            long timestamp = jsonStore.put(configName, mapper.writeValueAsString(configData), CONTEXT_CONFIG);
            if (timestamp < 0) {
                throw new ConfigIOException("Fail to put data in JsonStore! configName: " + configName, null);
            }
        } catch (JsonProcessingException e) {
            throw new ConfigConversionException("Fail to convert ConfigData to String! configName: " + configName, e);
        }
    }

    /**
     * Validate the whole config set
     *
     * @param configName
     * @param configData
     * @throws ValidationException
     */
    private void validateConfigData(final String configName, final ConfigData<JSONObject> configData) throws ValidationException {
        this.getConfigDefinition(configName).ifPresentOrElse(
                def -> this.validateConfigData(def, configData),
                () -> {
                    LOG.error("ConfigDefinition not found! configName: " + configName);
                    throw new ConfigNotFoundException("ConfigDefinition not found! configName: " + configName);
                });
    }

    private void validateConfigData(final ConfigDefinition configDefinition, final ConfigData<JSONObject> configData) throws ValidationException {
        final Map<String, JSONObject> configs = configData.getConfigs();
        if (!configDefinition.getAllowMultiple() && configs.size() > 1) {
            throw new ConfigRuntimeException(String.format("Cannot set multiple configurations for '%s'", configDefinition.getConfigName()));
        }
        configs.forEach((key, config) -> this.validateConfig(configDefinition, config));
    }

    /**
     * Validate a single config
     *
     * @param configName
     * @param configObject
     */
    private void validateConfig(final String configName, final JSONObject configObject) {
        this.getConfigDefinition(configName).ifPresentOrElse(
                def -> this.validateConfig(def, configObject),
                () -> {
                    LOG.error("ConfigDefinition not found! configName: " + configName);
                    throw new ConfigNotFoundException("ConfigDefinition not found! configName: " + configName);
                });
    }

    /**
     * Validate config against schema
     * @param configDefinition
     * @param configObject
     */
    private void validateConfig(final ConfigDefinition configDefinition, final JSONObject configObject) {
        configDefinition.validate(configObject.toString());
    }
}
