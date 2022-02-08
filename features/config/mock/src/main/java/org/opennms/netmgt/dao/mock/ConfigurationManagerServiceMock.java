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

package org.opennms.netmgt.dao.mock;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.SchemaConversionException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * It is a minimal mock for CM use. If configFile is passed, it will read and return as configEntity.
 * Otherwise, it will return a new instance.
 */
@Component
public class ConfigurationManagerServiceMock implements ConfigurationManagerService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceMock.class);

    private String configFile;
    private Optional<String> configOptional;

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
    public Map<String, ConfigDefinition> getAllConfigDefinitions() {
        return null;
    }

    @Override
    public Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName) {
        ConfigDefinition def = null;
        if ("provisiond".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("provisiond", "provisiond-configuration.xsd",
                    "provisiond-configuration", ConfigurationManagerService.BASE_PATH);
        }

        return Optional.ofNullable(def);
    }

    @Override
    public void registerEventHandler(EventType type, ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
    }

    @Override
    public void registerConfiguration(String configName, String configId, JsonAsString configObject) {
    }

    @Override
    public void unregisterConfiguration(String configName, String configId) {

    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString configObject, boolean isReplace) {
        configOptional = Optional.of(configObject.toString());
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(String configName, String configId) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getJSONStrConfiguration(String configName, String configId) {
        String xmlStr = this.getXmlConfiguration(configName, configId).get();
        try {
            ConfigConverter converter = XsdHelper.getConverter(this.getRegisteredConfigDefinition(configName).get());
            return Optional.ofNullable(converter.xmlToJson(xmlStr));
        } catch (IOException e) {
            throw new SchemaConversionException("Fail to getConverter configName: " + configName, e);
        }

    }

    private Optional<String> getXmlConfiguration(String configName, String configId) {
        if (configOptional != null) {
            return configOptional;
        }
        if (configFile == null) {
            // if configFile is null, assume config file in opennms-dao-mock resource etc directly
            configFile = "etc/" + configName + "-" + configId + ".xml";
        }

        try {
            InputStream in = ConfigurationManagerServiceMock.class.getClassLoader().getResourceAsStream(configFile);
            String xmlStr = IOUtils.toString(in, StandardCharsets.UTF_8);
            configOptional = Optional.of(xmlStr);
            LOG.debug("xmlStr: {}", xmlStr);
        } catch (IOException e) {
            LOG.error("FAIL TO LOAD XML: {}", configFile, e);
        }

        return configOptional;
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) {
        return Optional.empty();
    }

    @Override
    public Set<String> getConfigNames() {
        return new HashSet<>();
    }

    @Override
    public void unregisterSchema(String configName) {

    }

    @Override
    public Set<String> getConfigIds(String configName) {
        return new HashSet<>();
    }
}
