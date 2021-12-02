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

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * It is a minimal mock for CM use. If configFile is passed, it will read and return as configEntity.
 * Otherwise, I will return a new instance.
 */
@Component
public class ConfigurationManagerServiceMock implements ConfigurationManagerService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceMock.class);

    private String configFile;

    // It is Map<configName, Map<configId, json>>
    private Map<String, Map<String, String>> configStore = new HashMap<>();

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public void registerConfigDefinition(String configName, ConfigDefinition configDefinition) {

    }

    @Override
    public void changeConfigDefinition(String configName, ConfigDefinition configDefinition) {

    }

    @Override
    public Map<String, ConfigDefinition> getAllConfigDefinition() {
        return null;
    }

    @Override
    public Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName) {
        ConfigDefinition def = null;
        if ("provisiond".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("provisiond", "provisiond-configuration.xsd",
                    "provisiond-configuration", ConfigurationManagerService.BASE_PATH);
        } else if ("enlinkd".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("enlinkd", "enlinkd-configuration.xsd",
                    "enlinkd-configuration", ConfigurationManagerService.BASE_PATH);
        } else if ("vmware".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("vmware", "vmware-config.xsd",
                    "vmware-config", ConfigurationManagerService.BASE_PATH);
        } else if ("discovery".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("discovery", "discovery-configuration.xsd",
                    "discovery-configuration", ConfigurationManagerService.BASE_PATH);
        } else if ("jmx".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("jmx", "jmx-config.xsd",
                    "jmx-config", ConfigurationManagerService.BASE_PATH);
        } else if ("xmp".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("xmp", "xmp-config.xsd",
                    "xmp-config", ConfigurationManagerService.BASE_PATH);
        }
        return Optional.ofNullable(def);
    }

    @Override
    public void registerReloadConsumer(ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
    }

    @Override
    public void registerConfiguration(String configName, String configId, JsonAsString configObject) throws IOException {
    }

    @Override
    public void unregisterConfiguration(String configName, String configId) throws IOException {

    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString configObject) throws IOException {
        this.putConfig(configName, configId, configObject.toString());
    }

    private String getConfig(String configName, String configId) {
        Map<String, String> configs = configStore.get(configName);
        if (configs == null) {
            return null;
        }
        return configs.get(configId);
    }

    private void putConfig(String configName, String configId, String json) {
        Map<String, String> configs = configStore.get(configName);
        if (configs == null) {
            configs = new HashMap<>();
            configStore.put(configName, configs);
        }
        configs.put(configId, json);
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(String configName, String configId) throws IOException {
        Optional<String> jsonStr = this.getJSONStrConfiguration(configName, configId);
        if(jsonStr.isEmpty())
            return Optional.empty();
        return Optional.of(new JSONObject(jsonStr.get()));
    }

    @Override
    public Optional<String> getJSONStrConfiguration(String configName, String configId) throws IOException {
        String jsonStr = this.getConfig(configName, configId);
        if(jsonStr != null){
            return Optional.of(jsonStr);
        }
        String xmlStr = this.getXmlConfiguration(configName, configId);
        if(xmlStr == null){
            LOG.error("Cannot found config !!! configName: {}, configId: {}", configName, configId);
            return Optional.empty();
        }
        ConfigConverter converter = XsdHelper.getConverter(this.getRegisteredConfigDefinition(configName).get());
        jsonStr = converter.xmlToJson(xmlStr);
        if(jsonStr != null){
            this.putConfig(configName, configId, jsonStr);
        }
        return Optional.ofNullable(jsonStr);
    }

    /**
     * Main function in this mock, it read the xml and return to getJsonConfiguration for conversion
     * @param configName
     * @param configId
     * @return
     * @throws IOException
     */
    private String getXmlConfiguration(String configName, String configId) throws IOException {
        if (configFile == null) {
            // if configFile is null, assume config file in opennms-dao-mock resource etc directly
            configFile = "etc/" + configName + "-" + configId + ".xml";
        }

        try {
            InputStream in;
            if(configFile.startsWith("/")){
                in = new FileInputStream(configFile);
            } else {
                in = ConfigurationManagerServiceMock.class.getClassLoader().getResourceAsStream(configFile);
            }
            String xmlStr = IOUtils.toString(in, StandardCharsets.UTF_8);
            LOG.debug("xmlStr: {}", xmlStr);
            return xmlStr;
        } catch (Exception e) {
            LOG.error("FAIL TO LOAD XML: {}", configFile, e);
        }

        return null;
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