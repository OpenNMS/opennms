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

package org.opennms.features.config.service.api;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigSchema;

/**
 * Responsible for managing Schemas and Configurations.
 * A Schema is a set of rules that constrain the data of a Configuration.
 * A Configuration is the data that defines the runtime behaviour of a service together with the code of that service.
 *
 * @apiNote Due to the classloading behaviour. Do not use any non-primitive API parameters via OSGi interface. It will subject to <b>FAIL</b>!!!
 */
public interface ConfigurationManagerService {

    /** Registers a new schema. The schema name must not have been used before. */
    @Deprecated // use registerConfigDefinition() instead.
    void registerSchema(String configName, String xsdName, String topLevelElement) throws IOException, JAXBException;

    /** Registers a ConfigDefinition under a unique configName. If the schema id is present it will throw an IllegalArgumentException. */
    void registerConfigDefinition(String configName, ConfigDefinition configDefinition);

    /** Upgrades an existing schema to a new version. Existing da is validated against the new schema. */
    @Deprecated // use changeConfigDefinition() instead.
    void upgradeSchema(String configName, String xsdName, String topLevelElement) throws IOException, JAXBException;

    /** Changes a ConfigDefinition. If the configName is not present it will throw an  IllegalArgumentException. */
    void changeConfigDefinition(String configName, ConfigDefinition configDefinition);

    Map<String, ConfigSchema<?>> getAllConfigSchema();

    /**
     * Get the registered Schema
     *
     * @param configName
     * @return ConfigSchema
     * @throws IOException
     */
    @Deprecated // replace with getRegisteredConfigDefinition
    Optional<ConfigSchema<?>> getRegisteredSchema(String configName);

    Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName);

    void registerReloadConsumer(ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer);

    /**
     * register a new configuration by config object.
     * It will make sure the configId is not duplicated !!!
     *
     * @param configName
     * @param configId
     * @param configObject (config object / JSONObject)
     * @throws IOException
     */
    void registerConfiguration(String configName, String configId, JsonAsString configObject) throws IOException;

    /**
     * remove configure from service
     *
     * @param configId
     * @throws IOException
     */
    void unregisterConfiguration(String configName, String configId) throws IOException;

    void updateConfiguration(String configName, String configId,
                             JsonAsString configObject) throws IOException, IllegalArgumentException;

    /**
     * get config as json by configName, configId
     *
     * @param configName
     * @param configId
     * @return JSONObject
     * @throws IOException
     */
    Optional<JSONObject> getJSONConfiguration(String configName, String configId) throws IOException;

    /**
     * Use for osgi API
     *
     * @return config in json string
     * @see #getJSONStrConfiguration(String, String)
     */
    String getJSONStrConfiguration(String configName, String configId) throws IOException;

    /**
     * get config as xml by configName, configId
     *
     * @param configName
     * @param configId
     * @return xml string
     * @throws IOException
     */
    Optional<String> getXmlConfiguration(String configName, String configId) throws IOException;

    /**
     * get whole ConfigData by configName
     *
     * @param configName
     * @return ConfigData
     * @throws IOException
     */
    Optional<ConfigData<JSONObject>> getConfigData(String configName) throws IOException;

    /**
     * get a list of registered configName
     *
     * @return configName set
     * @throws IOException
     */
    Set<String> getConfigNames() throws IOException;

    /**
     * it will remove both config and schema
     *
     * @param configName
     * @throws IOException
     */
    void unregisterSchema(String configName) throws IOException;

    /**
     * return configIds by configName
     *
     * @param configName
     */
    Set<String> getConfigIds(String configName) throws IOException;

// TODO: next phase for xml conversion work
//    /** add: inserts a child into a parent element */
//    void addConfiguration(final String configId, final String path, final String content);
//
//    /** Inserts or updates an configuration element at the given path. */
//    void putConfiguration(final String configId, final String path, final String content);
//
//    void putConfiguration(final String configId, JSONObject object);
//
//    /** Removes all configuration objects which can be found with the given xpath expression. */
//
//    void removeConfiguration(final String configId, final String path);
}
