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
package org.opennms.netmgt.dao.mock;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.impl.util.XsdHelper;
import org.opennms.features.config.exception.ConfigNotFoundException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.EventType;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * It is a minimal mock for CM use. If configFile is passed, it will read and return as configEntity.
 * Otherwise, it will return a new instance.
 */
@Component
public class ConfigurationManagerServiceMock implements ConfigurationManagerService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceMock.class);
    public static final String JSON_EXTENSION = ".json";
    public static final String XML_EXTENSION = ".xml";

    // It is Map<configName, Map<configId, json>>
    private final Map<String, Map<String, String>> configStore = new HashMap<>();

    // It store override path for configs Map<configName, filePath>
    private Map<String, String> configFileMap;

    public void setConfigFileMap(Map<String, String> configFileMap) {
        this.configFileMap = configFileMap;
    }

    @Override
    public void registerConfigDefinition(String configName, ConfigDefinition configDefinition) {
        // mock not support liquibase
    }

    @Override
    public void changeConfigDefinition(String configName, ConfigDefinition configDefinition) {
        // mock not support liquibase
    }

    @Override
    public Map<String, ConfigDefinition> getAllConfigDefinitions() {
        // mock not support
        return Collections.emptyMap();
    }

    @Override
    public Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName) {
        ConfigDefinition def = null;
        if ("provisiond".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("provisiond", "provisiond-configuration.xsd",
                    "provisiond-configuration", ConfigurationManagerService.BASE_PATH);
        }

        if ("snmp-config".equals(configName)) {
            def = XsdHelper.buildConfigDefinition("snmp-config", "snmp-config.xsd",
                    "snmp-config", ConfigurationManagerService.BASE_PATH);
        }

        return Optional.ofNullable(def);
    }

    @Override
    public void registerEventHandler(EventType type, ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
        // mock not support yet
    }

    @Override
    public void registerConfiguration(String configName, String configId, JsonAsString configObject) {
        this.putConfig(configName, configId, configObject.toString());
    }

    @Override
    public void unregisterConfiguration(String configName, String configId) {
        // mock not support liquibase
    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString configObject, boolean isReplace) {
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
        Map<String, String> configs = configStore.computeIfAbsent(configName, value -> new HashMap<>());
        configs.put(configId, json);
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(String configName, String configId) {
        Optional<String> jsonStr = this.getJSONStrConfiguration(configName, configId);
        if (jsonStr.isEmpty())
            return Optional.empty();
        return Optional.of(new JSONObject(jsonStr.get()));
    }

    /**
     * It will return config in following order. After first read, it will cache it in memory. updateConfig will overwrite existing config in memory
     * <ul>
     * <li>memory</li>
     * <li>json file</li>
     * <li>xml file</li>
     * </ul>
     *
     * @param configName
     * @param configId
     * @return
     */
    @Override
    public Optional<String> getJSONStrConfiguration(String configName, String configId) {
        String jsonStr = this.getConfig(configName, configId);
        if (jsonStr != null) {
            return Optional.of(jsonStr);
        }
        // try get json from file
        List<String> paths = new ArrayList<>();
        if (configFileMap != null && configFileMap.containsKey(configName))
            paths.add(configFileMap.get(configName));
        paths.add("etc/" + configName + "-" + configId + JSON_EXTENSION);
        paths.add("mock/" + configName + "-" + configId + JSON_EXTENSION);
        String tmpConfigFile = this.findConfigFile(paths);
        if (tmpConfigFile != null && tmpConfigFile.endsWith(JSON_EXTENSION)) {
            try {
                InputStream in = ConfigurationManagerServiceMock.class.getClassLoader().getResourceAsStream(tmpConfigFile);
                if (in != null) {
                    jsonStr = IOUtils.toString(in, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                LOG.error("FAIL to read file: {} message: {}", tmpConfigFile, e.getMessage());
            }
        }

        // fall back to old xml file
        if (jsonStr == null) {
            String xmlStr = this.getXmlConfiguration(configName, configId);
            if (xmlStr == null) {
                LOG.error("Cannot found config !!! configName: {}, configId: {}. Returning empty config.", configName, configId);
                // return empty json if nothing found.
                return Optional.of("{}");
            }
            Optional<ConfigDefinition> def = this.getRegisteredConfigDefinition(configName);
            if (def.isEmpty()) {
                LOG.error("Cannot find ConfigDefinition for {}. ", configName);
                throw new ConfigNotFoundException("ConfigDefinition not found!");
            }
            try {
                ConfigConverter converter = XsdHelper.getConverter(def.get());
                jsonStr = converter.xmlToJson(xmlStr);
            } catch (IOException e) {
                LOG.error("FAIL to convert xml: {}, message: {}", xmlStr, e.getMessage());
            }
        }
        if (jsonStr != null) {
            this.putConfig(configName, configId, jsonStr);
        }
        return Optional.ofNullable(jsonStr);
    }

    /**
     * Main function in this mock, it read the xml and return to getJsonConfiguration for conversion
     * <p>
     * It will look up xml in following path
     * <ul>
     * <li>configFile in bean property</li>
     * <li>resources/etc/configName-configId.xml</li>
     * <li>resources/default/configName-configuration.xml</li>
     * <li>resources/default/configName-config.xml</li>
     * </ul>
     *
     * @param configName
     * @param configId
     * @return
     */
    private String getXmlConfiguration(String configName, String configId) {
        String tmpConfigFile;
        String configFile = (configFileMap != null) ? configFileMap.get(configName) : null;
        if (configFile == null) {
            List<String> paths = new ArrayList<>();
            // if configFile is null, assume config file in opennms-dao-mock resource etc directly
            paths.add("etc/" + configName + "-" + configId + XML_EXTENSION);
            paths.add("defaults/" + configName + "-configuration.xml");
            paths.add("defaults/" + configName + "-config.xml");
            tmpConfigFile = this.findConfigFile(paths);
        } else {
            tmpConfigFile = configFile;
        }

        if (tmpConfigFile == null) {
            return null;
        }

        try {
            InputStream in;
            if (tmpConfigFile.startsWith("/")) {
                in = new FileInputStream(tmpConfigFile);
            } else {
                in = ConfigurationManagerServiceMock.class.getClassLoader().getResourceAsStream(tmpConfigFile);
            }
            String xmlStr = IOUtils.toString(in, StandardCharsets.UTF_8);
            LOG.debug("xmlStr: {}", xmlStr);
            return xmlStr;
        } catch (Exception e) {
            LOG.error("FAIL TO LOAD XML: {}", configFile, e);
        }

        return null;
    }

    private String findConfigFile(List<String> paths) {
        if (paths == null) {
            return null;
        }
        AtomicReference<String> fileFound = new AtomicReference<>();
        paths.forEach(path -> {
            if (fileFound.get() != null)
                return;
            if (path == null)
                return;
            File tmpFile = new File(path);
            if (tmpFile.isFile() && tmpFile.canRead()) {
                fileFound.set(tmpFile.getAbsolutePath());
            } else {
                URL url = ConfigurationManagerServiceMock.class.getClassLoader().getResource(path);
                if (url != null) {
                    fileFound.set(path);
                }
            }
        });
        LOG.info("Config file find. path: {}", fileFound.get());
        return fileFound.get();
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
        // mock not support liquibase
    }

    @Override
    public Set<String> getConfigIds(String configName) {
        Map<String, String> configs = this.configStore.get(configName);
        if (configs != null) {
            return configs.keySet();
        }
        return new HashSet<>();
    }
}
