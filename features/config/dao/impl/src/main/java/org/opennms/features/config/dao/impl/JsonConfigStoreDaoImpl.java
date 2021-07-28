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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.dao.impl.util.JSONObjectDeserializer;
import org.opennms.features.config.dao.impl.util.JSONObjectSerialIzer;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class JsonConfigStoreDaoImpl implements ConfigStoreDao<JSONObject> {
    //private static final Logger LOG = LoggerFactory.getLogger(ConfigStoreDao.class);
    public static final String CONTEXT_CONFIG = "CM_CONFIG";
    public static final String CONTEXT_META = "CM_META";
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
    public boolean register(ConfigSchema<?> configSchema) throws IOException {
        long timestamp = jsonStore.put(configSchema.getName(), mapper.writeValueAsString(configSchema), CONTEXT_META);
        return timestamp > 0;
    }

    @Override
    public Optional<Set<String>> getServiceIds() {
        return this.getIds(CONTEXT_META);
    }

    @Override
    public Optional<Set<String>> getConfigIds() {
        return this.getIds(CONTEXT_CONFIG);
    }

    private Optional<Set<String>> getIds(String context) {
        Map<String, String> allMap = jsonStore.enumerateContext(context);
        if (allMap == null || allMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(allMap.keySet());
    }

    @Override
    public Optional<ConfigSchema<?>> getConfigSchema(String serviceName) throws IOException, ClassNotFoundException {
        Optional<String> jsonStr = jsonStore.get(serviceName, CONTEXT_META);
        if (jsonStr.isEmpty()) {
            return Optional.empty();
        }
        JSONObject json = new JSONObject(jsonStr.get());
        String className = (String) json.get("converterClass");
        Class<?> converterClass = Class.forName(className);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(ConfigSchema.class, converterClass);
        System.out.println(javaType);
        ConfigSchema<?> schema = (ConfigSchema) mapper.readValue(jsonStr.get(), javaType);
        return Optional.ofNullable(schema);
    }

    @Override
    public boolean updateConfigSchema(ConfigSchema<?> configSchema) throws IOException, ClassNotFoundException {
        Optional<ConfigSchema<?>> schema = this.getConfigSchema(configSchema.getName());
        if (schema.isEmpty()) {
            return false;
        }
        return this.putSchema(configSchema);
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(final String serviceName) throws IOException {
        Optional<String> configDataJsonStr = jsonStore.get(serviceName, CONTEXT_CONFIG);

        if (configDataJsonStr.isEmpty()) {
            return Optional.empty();
        }
        JavaType type = mapper.getTypeFactory().constructParametricType(ConfigData.class, JSONObject.class);
        return Optional.of(mapper.readValue(configDataJsonStr.get(), type));
    }

    @Override
    public boolean addConfigs(String serviceName, ConfigData<JSONObject> configData) throws IOException {
        Optional<ConfigData<JSONObject>> exist = this.getConfigData(serviceName);
        if (exist.isPresent()) {
            return false;
        }
        return this.putConfig(serviceName, configData);
    }

    @Override
    public boolean addConfig(String serviceName, String configId, JSONObject config) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(serviceName);
        if (configData.isEmpty()) {
            configData = Optional.of(new ConfigData<JSONObject>());
        }
        Map<String, JSONObject> configs = configData.get().getConfigs();
        if (configs.containsKey(configId)) {
            return false;
        }
        configs.put(configId, config);
        return this.putConfig(serviceName, configData.get());
    }

    @Override
    public Optional<JSONObject> getConfig(String serviceName, String configId) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(serviceName);
        if (configData.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(configData.get().getConfigs().get(configId));
    }

    @Override
    public boolean updateConfig(String serviceName, String configId, JSONObject config) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(serviceName);
        if (configData.isEmpty()) {
            return false;
        }
        Map<String, JSONObject> configs = configData.get().getConfigs();
        if (!configs.containsKey(configId)) {
            return false;
        }
        configs.put(configId, config);
        return this.putConfig(serviceName, configData.get());
    }

    @Override
    public boolean updateConfigs(String serviceName, ConfigData<JSONObject> configData) throws IOException {
        return this.putConfig(serviceName, configData);
    }

    @Override
    public boolean deleteConfig(String serviceName, String configId) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(serviceName);
        if (configData.isEmpty()) {
            return false;
        }
        if (configData.get().getConfigs().remove(configId) == null) {
            return false;
        }
        return this.putConfig(serviceName, configData.get());
    }

    @Override
    public void unregister(String serviceName) {
        jsonStore.delete(serviceName, CONTEXT_META);
        jsonStore.delete(serviceName, CONTEXT_CONFIG);
    }

    @Override
    public Optional<Map<String, JSONObject>> getConfigs(String serviceName) throws IOException {
        Optional<ConfigData<JSONObject>> configData = this.getConfigData(serviceName);
        if (configData.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(configData.get().getConfigs());
    }

    private boolean putSchema(ConfigSchema<?> configSchema) throws IOException {
        long timestamp = jsonStore.put(configSchema.getName(), mapper.writeValueAsString(configSchema), CONTEXT_META);
        return timestamp > 0;
    }

    private boolean putConfig(String serviceName, ConfigData<JSONObject> configData) throws IOException {
        long timestamp = jsonStore.put(serviceName, mapper.writeValueAsString(configData), CONTEXT_CONFIG);
        return timestamp > 0;
    }
}
