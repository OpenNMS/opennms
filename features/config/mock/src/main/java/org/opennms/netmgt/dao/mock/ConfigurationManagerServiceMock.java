/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigSchema;
import org.opennms.features.config.dao.impl.util.XmlConverter;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * It is a minimal mock for CM use. If configFile is passed, it will read and return as configEntity.
 * Otherwise, I will return a new instance.
 */
@Component
public class ConfigurationManagerServiceMock implements ConfigurationManagerService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceMock.class);

    private String configFile;

    private Map<String, ConfigData<JSONObject>> configs = new HashMap<>();
    private Map<String, ConfigDefinition> definationsMap = new HashMap<>();

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public void registerSchema(String configName, String xsdName, String topLevelElement) throws IOException, JAXBException {

    }

    @Override
    public void registerConfigDefinition(String configName, ConfigDefinition configDefinition) {
        LOG.info("registerConfigDefinition:" + configName + " " + configDefinition);

        definationsMap.put(configName, configDefinition);
    }

    @Override
    public void upgradeSchema(String configName, String xsdName, String topLevelElement) throws IOException, JAXBException {
    }

    @Override
    public void changeConfigDefinition(String configName, ConfigDefinition configDefinition) {
        LOG.info("changeConfigDefinition:" + configName + " " + configDefinition);
        definationsMap.put(configName, configDefinition);
    }

    @Override
    public Map<String, ConfigSchema<?>> getAllConfigSchema() {
        return null;
    }

    @Override
    public Optional<ConfigSchema<?>> getRegisteredSchema(String configName) {
        LOG.info("getRegisteredSchema:" + configName);
        ConfigSchema schema = null;
        try {
            if ("discovery".equals(configName)) {
                XmlConverter converter = new XmlConverter("discovery-configuration.xsd", "discovery-configuration");
                schema = new ConfigSchema(configName, XmlConverter.class, converter);
            } else if ("wmi".equals(configName)) {
                XmlConverter converter = new XmlConverter("wmi-config.xsd", "wmi-config");
                schema = new ConfigSchema(configName, XmlConverter.class, converter);
            } else if ("provisiond".equals(configName)) {
                XmlConverter converter = new XmlConverter("provisiond-configuration.xsd", "provisiond-configuration");
                schema = new ConfigSchema(configName, XmlConverter.class, converter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(schema);
    }

    @Override
    public Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName) {
        LOG.info("getRegisteredConfigDefinition:" + configName);
        return Optional.ofNullable(definationsMap.get(configName));
    }

    @Override
    public void registerReloadConsumer(ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
    }

    @Override
    public void registerConfiguration(String configName, String configId, JsonAsString configObject) throws IOException {
        Optional<JSONObject> configJson = this.getJSONConfiguration(configName, configId);
        if (configJson.isPresent()) {
            throw new RuntimeException("Exist configName: " + configName + " configId: " + configId);
        }
        this.updateConfiguration(configName, configId, configObject);
    }

    @Override
    public void unregisterConfiguration(String configName, String configId) throws IOException {

    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString configObject) throws IOException {
        ConfigData<JSONObject> configData = configs.get(configName);
        if (configData == null) {
            configData = new ConfigData<>();
            configs.put(configName, configData);
        }
        configData.getConfigs().put(configId, new JSONObject(configObject.toString()));
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(String configName, String configId) throws IOException {
        ConfigData<JSONObject> configData = configs.get(configName);
        if (configData == null || configData.getConfigs().isEmpty())
            return Optional.empty();
        return Optional.ofNullable(configData.getConfigs().get(configId));
    }

    @Override
    public String getJSONStrConfiguration(String configName, String configId) throws IOException {
        LOG.info("getJSONStrConfiguration:" + configName);
        Optional<JSONObject> json = this.getJSONConfiguration(configName, configId);
        return json.isEmpty() ? null : json.get().toString();
    }

    @Override
    public Optional<String> getXmlConfiguration(String configName, String configId) throws IOException {
        LOG.info("getXmlConfiguration:" + configName);
        String jsonStr = this.getJSONStrConfiguration(configName, configId);

        if (jsonStr != null) {
            Optional<ConfigSchema<?>> schema = this.getRegisteredSchema(configName);
            LOG.info(jsonStr);
            return Optional.of(schema.get().getConverter().jsonToXml(jsonStr));
        }

        if (configFile == null) {
            // if configFile is null, assume config file in opennms-dao-mock resource etc directly
            configFile = "etc/" + configName + "-" + configId + ".xml";
        }

        Optional<String> configOptional = Optional.empty();
        try {
            InputStream in = ConfigurationManagerServiceMock.class.getClassLoader().getResourceAsStream(configFile);
            String xmlStr = IOUtils.toString(in, StandardCharsets.UTF_8);
            configOptional = Optional.of(xmlStr);
            LOG.info("xmlStr: {}", xmlStr);
        } catch (Exception e) {
            LOG.error("FAIL TO LOAD XML: {}", configFile, e);
        }

        return configOptional;
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) throws IOException {
        return Optional.empty();
    }

    @Override
    public Set<String> getConfigNames() throws IOException {
        return new HashSet<>();
    }

    @Override
    public void unregisterSchema(String configName) throws IOException {

    }

    @Override
    public Set<String> getConfigIds(String configName) throws IOException {
        return new HashSet<>();
    }
}
