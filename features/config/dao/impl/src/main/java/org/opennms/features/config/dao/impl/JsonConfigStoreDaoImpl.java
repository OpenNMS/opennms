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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.UnmarshalException;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.dao.impl.util.JSONObjectDeserializer;
import org.opennms.features.config.dao.impl.util.JSONObjectSerialIzer;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
    public void register(ConfigSchema<?> configSchema) throws IOException {
        this.putSchema(configSchema);
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
    public Optional<ConfigSchema<?>> getConfigSchema(String configName) throws IOException {
        Optional<String> jsonStr = jsonStore.get(configName, CONTEXT_SCHEMA);
        if (jsonStr.isEmpty()) {
            return Optional.empty();
        }
        JSONObject json = new JSONObject(jsonStr.get());
        String className = (String) json.get("converterClass");
        Class<?> converterClass = null;
        try {
            converterClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid schema for configName: " + configName);
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ConfigSchema.class, converterClass);
        ConfigSchema<?> schema = mapper.readValue(jsonStr.get(), javaType);
        return Optional.ofNullable(schema);
    }

    @Override
    public void updateConfigSchema(ConfigSchema<?> configSchema) throws IOException {
        Optional<ConfigSchema<?>> schema = this.getConfigSchema(configSchema.getName());
        if (schema.isEmpty()) {
            throw new IllegalArgumentException("ConfigName not found: " + configSchema.getName());
        }
        this.putSchema(configSchema);
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
        JSONObject json = this.validateConfigWithConvert(configName, configObject);
        configs.put(configId, json);
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
        this.validateConfigWithConvert(configName, existingJson);
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

    private void putSchema(ConfigSchema<?> configSchema) throws IOException {
        long timestamp = jsonStore.put(configSchema.getName(), mapper.writeValueAsString(configSchema), CONTEXT_SCHEMA);
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
     * convert (it will skip convert it JSONObject passed) and validate
     *
     * @param configName
     * @param configObject (config object / JSONObject)
     * @return
     * @throws IOException
     */
    private JSONObject validateConfigWithConvert(final String configName, final JSONObject configObject)
            throws IOException {
        Optional<ConfigSchema<?>> schema = this.getConfigSchema(configName);
        try {
                this.validateConfig(schema, configObject);
                return configObject;
        } catch (RuntimeException e) {
            // make it error easier to understand
            if (e.getCause() instanceof UnmarshalException) {
                throw new RuntimeException("Input format error ! " + e.getMessage());
            }
            throw e;
        }
    }

    private void validateConfigData(final String configName, final ConfigData<JSONObject> configData)
            throws IOException {
        Optional<ConfigSchema<?>> schema = this.getConfigSchema(configName);
        if (schema.isEmpty()) {
            LOG.error("Schema not found!");
            throw new RuntimeException("Schema not found!");
        }
        configData.getConfigs().forEach((key, config) -> {
            this.validateConfig(schema, config);
        });
    }

    private void validateConfig(final Optional<ConfigSchema<?>> schema, final JSONObject configObject) {
        try {
            if (schema.isEmpty()) {
                LOG.error("Schema not found!");
                throw new RuntimeException("Schema not found!");
            }
            ConfigConverter converter = schema.get().getConverter();
            // TODO: Patrick: make this work for all SCHEMA_TYPEs
            if (!converter.validate(converter.jsonToXml(configObject.toString()), ConfigConverter.SCHEMA_TYPE.XML)) {
                LOG.error("Config validation error!: {} ", schema.get().getName());
                throw new RuntimeException("Fail to validate xml! May be schema issue.");
            }
        } catch (Exception e) {
            LOG.error("Config validation fail!: {}", configObject, e);
            throw new RuntimeException(e);
        }
    }
}
