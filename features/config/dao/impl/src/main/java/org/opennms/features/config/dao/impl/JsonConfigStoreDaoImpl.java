/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.dao.impl.util.JSONObjectDeserializer;
import org.opennms.features.config.dao.impl.util.JSONObjectSerialIzer;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Component
public class JsonConfigStoreDaoImpl implements ConfigStoreDao<JSONObject> {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigStoreDao.class);
    public static final String CONTEXT_CONFIG = "CM_CONFIG";
    public static final String CONTEXT_SCHEMA = "CM_SCHEMA";
    private final ObjectMapper mapper;

    private final JsonStore jsonStore;

    public JsonConfigStoreDaoImpl(JsonStore jsonStore) {
        this.jsonStore = jsonStore;
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(JSONObject.class, new JSONObjectDeserializer());
        module.addSerializer(JSONObject.class, new JSONObjectSerialIzer());
        mapper.registerModule(module);
    }

    @Override
    public void register(ConfigDefinition configDefinition) throws IOException {
        this.putConfigDefinition(configDefinition);
    }

    @Override
    public Optional<Set<String>> getConfigNames() {
        return this.getIds(CONTEXT_SCHEMA);
    }

    private Optional<Set<String>> getIds(String context) {
        Map<String, String> allMap = jsonStore.enumerateContext(context);
        if (allMap == null || allMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(allMap.keySet());
    }

    @Override
    public Map<String, ConfigDefinition> getAllConfigDefinition() {
        Map<String, ConfigDefinition> output = new HashMap<>();
        jsonStore.enumerateContext(CONTEXT_SCHEMA).forEach((key, value) -> {
            try {
                output.put(key, this.deserializeConfigDefinition(value));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        return output;
    }

    private ConfigDefinition deserializeConfigDefinition(String jsonStr) throws JsonProcessingException {
        return mapper.readValue(jsonStr, ConfigDefinition.class);
    }

    @Override
    public Optional<ConfigDefinition> getConfigDefinition(String configName) throws JsonProcessingException {
        Optional<String> jsonStr = jsonStore.get(configName, CONTEXT_SCHEMA);
        if (jsonStr.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.deserializeConfigDefinition(jsonStr.get()));
    }

    @Override
    public void updateConfigDefinition(ConfigDefinition configDefinition) throws IOException {
        Optional<ConfigDefinition> oldDef = this.getConfigDefinition(configDefinition.getConfigName());
        if (oldDef.isEmpty()) {
            throw new IllegalArgumentException("ConfigName not found: " + configDefinition.getConfigName());
        }
        // check config are still valid for the new schema
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(configDefinition.getConfigName());
        if(configData.isPresent()) {
            this.validateConfigData(Optional.of(configDefinition), configData.get());
        }
        this.putConfigDefinition(configDefinition);
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(final String configName) throws IOException {
        Optional<String> configDataJsonStr = jsonStore.get(configName, CONTEXT_CONFIG);
        if (configDataJsonStr.isEmpty()) {
            return Optional.empty();
        }
        JavaType type = mapper.getTypeFactory().constructParametricType(ConfigData.class, JSONObject.class);
        return Optional.of(mapper.readValue(configDataJsonStr.get(), type));
    }

    @Override
    public void addConfigs(String configName, ConfigData<JSONObject> configData) throws IOException {
        Optional<ConfigData<JSONObject>> exist = this.getConfigData(configName);
        if (exist.isPresent()) {
            throw new IllegalArgumentException("Duplicate config found for service: " + configName);
        }
        this.validateConfigData(configName, configData);
        this.putConfig(configName, configData);
    }

    @Override
    public void addConfig(String configName, String configId, JSONObject configObject) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(configName);
        if (configData.isEmpty()) {
            configData = Optional.of(new ConfigData<>());
        }
        Map<String, JSONObject> configs = configData.get().getConfigs();
        if (configs.containsKey(configId)) {
            throw new IllegalArgumentException("Duplicate config found for configId: " + configId);
        }
        ValidationReport report = this.validateConfig(configName, configObject);
        if (report.hasErrors()) {
            LOG.warn("Reject invalid config. configName: {} configId: {}\n {} \n Errors: {}", configName, configId, configObject, report.getMessages());
            throw new IllegalArgumentException(mapper.writeValueAsString(report));
        }
        configs.put(configId, configObject);
        this.putConfig(configName, configData.get());
    }

    @Override
    public Optional<JSONObject> getConfig(String configName, String configId) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(configName);
        if (configData.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(configData.get().getConfigs().get(configId));
    }

    @Override
    public void updateConfig(String configName, String configId, JSONObject config) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(configName);
        if (configData.isEmpty()) {
            throw new IllegalArgumentException("Config not found for config " + configName + ", configId " + configId);
        }
        Map<String, JSONObject> configs = configData.get().getConfigs();
        if (!configs.containsKey(configId)) {
            throw new IllegalArgumentException("Config not found for config " + configName + ", configId " + configId);
        }
        JSONObject existingJson = configs.get(configId);

        // copy all first level keys' value into existing config
        config.keySet().forEach((key) -> {
            existingJson.put(key, config.get(key));
        });
        ValidationReport report = this.validateConfig(configName, existingJson);
        if (report.hasErrors()) {
            throw new IllegalArgumentException(mapper.writeValueAsString(report));
        }
        this.putConfig(configName, configData.get());
    }

    @Override
    public void updateConfigs(String configName, ConfigData<JSONObject> configData) throws IOException {
        this.validateConfigData(configName, configData);
        this.putConfig(configName, configData);
    }

    @Override
    public void deleteConfig(String configName, String configId) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(configName);
        if (configData.isEmpty()) {
            throw new IllegalArgumentException("Config not found for config " + configName + ", configId " + configId);
        }
        if (configData.get().getConfigs().size() <= 1 ) {
            throw new IllegalArgumentException("Deletion of the last config is not allowed. " + configName + ", configId " + configId);
        }
        if (configData.get().getConfigs().remove(configId) == null) {
            throw new IllegalArgumentException("Config not found for config " + configName + ", configId " + configId);
        }
        this.putConfig(configName, configData.get());
    }

    @Override
    public void unregister(String configName) {
        jsonStore.delete(configName, CONTEXT_SCHEMA);
        jsonStore.delete(configName, CONTEXT_CONFIG);
    }

    @Override
    public Optional<Map<String, JSONObject>> getConfigs(String configName) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(configName);
        if (configData.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(configData.get().getConfigs());
    }

    private void putConfigDefinition(ConfigDefinition configDefinition) throws IOException {
        long timestamp = jsonStore.put(configDefinition.getConfigName(), mapper.writeValueAsString(configDefinition),
                CONTEXT_SCHEMA);
        if (timestamp < 0) {
            throw new RuntimeException("Fail to put data in JsonStore!");
        }
    }

    private void putConfig(String configName, ConfigData<JSONObject> configData) throws IOException {
        long timestamp = jsonStore.put(configName, mapper.writeValueAsString(configData), CONTEXT_CONFIG);
        if (timestamp < 0) {
            throw new RuntimeException("Fail to put data in JsonStore!");
        }
    }

    /**
     * Validate the whole config set
     * @param configName
     * @param configData
     * @throws IOException
     */
    private void validateConfigData(final String configName, final ConfigData<JSONObject> configData) throws IOException {
        this.validateConfigData(this.getConfigDefinition(configName), configData);
    }

    private void validateConfigData(final Optional<ConfigDefinition> configDefinition, final ConfigData<JSONObject> configData) throws IOException {
        if (configDefinition.isEmpty()) {
            LOG.error("ConfigDefinition not found!");
            throw new RuntimeException("ConfigDefinition not found!");
        }
        ValidationReport finalReport = ValidationReport.empty();
        configData.getConfigs().forEach((key, config) -> {
            ValidationReport report = this.validateConfig(configDefinition, config);
            if (report.hasErrors()) {
                finalReport.merge(report);
            }
        });
        if (finalReport.hasErrors()) {
            throw new IllegalArgumentException(mapper.writeValueAsString(finalReport));
        }
    }

    /**
     * Validate a single config
     * @param configName
     * @param configObject
     * @return
     * @throws JsonProcessingException
     */
    private ValidationReport validateConfig(final String configName, final JSONObject configObject) throws JsonProcessingException {
        Optional<ConfigDefinition> configDefinition = this.getConfigDefinition(configName);
        if (configDefinition.isEmpty()) {
            LOG.error("ConfigDefinition not found!");
            throw new RuntimeException("ConfigDefinition not found!");
        }
        ValidationReport report = configDefinition.get().validate(configObject.toString());
        return report;
    }

    private ValidationReport validateConfig(final Optional<ConfigDefinition> configDefinition, final JSONObject configObject) {
        if (configDefinition.isEmpty()) {
            LOG.error("ConfigDefinition not found!");
            throw new RuntimeException("ConfigDefinition not found!");
        }
        ValidationReport report = configDefinition.get().validate(configObject.toString());
        return report;
    }
}