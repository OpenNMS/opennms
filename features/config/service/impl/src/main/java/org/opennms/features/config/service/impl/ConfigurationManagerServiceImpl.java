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

package org.opennms.features.config.service.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.exception.ConfigAlreadyExistsException;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.exception.SchemaAlreadyExistsException;
import org.opennms.features.config.exception.SchemaNotFoundException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.OpenAPIConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

@Component
public class ConfigurationManagerServiceImpl implements ConfigurationManagerService {
    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceImpl.class);
    private final ConfigStoreDao<JSONObject> configStoreDao;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager();

    public ConfigurationManagerServiceImpl(final ConfigStoreDao<JSONObject> configStoreDao) {
        this.configStoreDao = configStoreDao;
    }

    @Override
    public void registerConfigDefinition(String configName, ConfigDefinition configDefinition) {
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configDefinition);

        if (this.getRegisteredConfigDefinition(configName).isPresent()) {
            throw new SchemaAlreadyExistsException(String.format("Schema with configName=%s is already registered.", configName), null);
        }
        configStoreDao.register(configDefinition);
    }

    @Override
    public void changeConfigDefinition(String configName, ConfigDefinition configDefinition) {
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configDefinition);
        final Optional<ConfigDefinition> existingDefinition = this.getRegisteredConfigDefinition(configName);
        if (existingDefinition.isEmpty()) {
            throw new SchemaNotFoundException(String.format("Schema with configName=%s is not present. Use registerSchema instead.", configName));
        }
        //allowMultiple must be preserved. It is set only once on creation
        configDefinition.setAllowMultiple(existingDefinition.get().getAllowMultiple());
        configStoreDao.updateConfigDefinition(configDefinition);
    }

    @Override
    public Map<String, ConfigDefinition> getAllConfigDefinitions() {
        return configStoreDao.getAllConfigDefinitions();
    }

    @Override
    public Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName) {
        Objects.requireNonNull(configName);
        return configStoreDao.getConfigDefinition(configName);
    }

    @Override
    public void registerEventHandler(EventType type, ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
        eventHandlerManager.registerEventHandler(type, info, consumer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerConfiguration(final String configName, final String inConfigId, JsonAsString configObject) {
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configObject);
        Optional<ConfigDefinition> configDefinition = this.getRegisteredConfigDefinition(configName);
        if (configDefinition.isEmpty()) {
            throw new SchemaNotFoundException(String.format("Unknown service with configName: %s.", configName));
        }
        String configId = inConfigId;
        if (configId == null) {
            configId = DEFAULT_CONFIG_ID;
        }
        final Set<String> configIds = this.getConfigIds(configName);
        if (configIds.contains(configId)) {
            throw new ConfigAlreadyExistsException(String.format(
                    "Configuration with service=%s, id=%s is already registered, update instead.", configName, configId));
        }
        if (!configDefinition.get().getAllowMultiple()) {
            if (!DEFAULT_CONFIG_ID.equals(configId)) {
                throw new ConfigRuntimeException(String.format(
                        "The '%s' service allows only one configuration with id='%s'.", configName, DEFAULT_CONFIG_ID, configId));
            }
            if (!configIds.isEmpty()) {
                throw new ConfigAlreadyExistsException(String.format(
                        "The service '%s' found existing configuration(s) with id(s) other than '%s'.", configName, DEFAULT_CONFIG_ID));
            }
        }

        this.eventHandlerManager.callEventHandlers(EventType.VALIDATE, new ConfigUpdateInfo(configName, configId, new JSONObject(configObject.toString())));
        configStoreDao.addConfig(configName, configId, new JSONObject(configObject.toString()));
        LOG.info("ConfigurationManager.registeredConfiguration(configName={}, configId={}, config={});", configName, configId, configObject);

        this.eventHandlerManager.callEventHandlers(EventType.CREATE, new ConfigUpdateInfo(configName, configId));
    }

    @Override
    public void unregisterConfiguration(final String configName, final String configId) {
        this.configStoreDao.deleteConfig(configName, configId);
        this.eventHandlerManager.callEventHandlers(EventType.DELETE, new ConfigUpdateInfo(configName, configId));
    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString config, boolean isReplace) {
        Optional<ConfigData<JSONObject>> configData = configStoreDao.getConfigs(configName);
        if (configData.isEmpty()) {
            throw new ConfigNotFoundException("ConfigData not found configName: " + configName);
        }
        Map<String, JSONObject> configs = configData.get().getConfigs();
        if (!configs.containsKey(configId)) {
            throw new ConfigNotFoundException("Config not found configName: " + configName + ", configId: " + configId);
        }

        JSONObject configObject = new JSONObject(config.toString());
        JSONObject configToUpdate;
        if (isReplace) {
            configToUpdate = configObject;
            configs.put(configId, configToUpdate);
        } else {
            configToUpdate = configs.get(configId);

            // copy all first level keys' value into existing config
            configObject.keySet().forEach(key -> configToUpdate.put(key, configObject.get(key)));
        }

        this.eventHandlerManager.callEventHandlers(EventType.VALIDATE, new ConfigUpdateInfo(configName, configId, configToUpdate));
        configStoreDao.updateConfigs(configName, configData.get());
        this.eventHandlerManager.callEventHandlers(EventType.UPDATE, new ConfigUpdateInfo(configName, configId));
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(final String configName, final String configId) {
        Optional<JSONObject> configObj = configStoreDao.getConfig(configName, configId);
        if (configObj.isEmpty()) {
            return configObj;
        }
        // try to fill default value or empty signature
        Optional<ConfigDefinition> def = configStoreDao.getConfigDefinition(configName);
        if (def.isPresent()) {
            String schemaName = (String) def.get().getMetaValue(ConfigDefinition.TOP_LEVEL_ELEMENT_NAME_TAG);
            if (schemaName == null) { // assume if top element name is null, the top schema name is configName
                schemaName = configName;
            }
            OpenAPIConfigHelper.fillWithDefaultValue(def.get().getSchema(), schemaName, configObj.get());
        }
        return configObj;
    }

    @Override
    public Optional<String> getJSONStrConfiguration(String configName, String configId) {
        Optional<JSONObject> config = this.getJSONConfiguration(configName, configId);
        if (config.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(config.get().toString());
    }

    @Override
    public Set<String> getConfigNames() {
        return configStoreDao.getConfigNames();
    }

    @Override
    public void unregisterSchema(String configName) {
        configStoreDao.unregister(configName);
    }

    @Override
    public Set<String> getConfigIds(String configName) {
        Optional<ConfigData<JSONObject>> configData = configStoreDao.getConfigs(configName);
        if (configData.isEmpty()) {
            return new HashSet<>();
        }
        return configData.get().getConfigs().keySet();
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) {
        return configStoreDao.getConfigs(configName);
    }
}
