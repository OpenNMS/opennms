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

package org.opennms.features.config.dao.api;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

/**
 * It handles storing config data in database by generic datatype
 * It also validation config before persist. (add & update)
 *
 * @param <CONFIG_DATATYPE> data type store in database
 */
public interface ConfigStoreDao<CONFIG_DATATYPE> {

    /**
     * register service to config manager
     *
     * @param configSchema schema object
     * @return status
     */
    void register(ConfigSchema<?> configSchema) throws IOException;

    /**
     * get all config names managing by config manager
     *
     * @return list of config name
     * @throws IOException
     */
    Optional<Set<String>> getConfigNames();

    /**
     * get configs meta by configName
     *
     * @param configName
     * @return status
     */
    Optional<ConfigSchema<?>> getConfigSchema(String configName) throws IOException;

    /**
     * update configs meta by configName
     *
     * @param configSchema
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void updateConfigSchema(ConfigSchema<?> configSchema) throws IOException;

    /**
     * get configs data by configName
     * It gives the raw ConfigData object.
     * If you only want to get the specific config, you should use getConfig(String, String)
     *
     * @param configName
     * @return config object
     * @throws IOException
     * @see #getConfig(String, String)
     * @see ConfigData
     */
    Optional<ConfigData<CONFIG_DATATYPE>> getConfigData(String configName) throws IOException;

    /**
     * add configs for the registered service name, return false is config already exist
     *
     * @param configName
     * @param configData
     * @throws IOException
     */
    void addConfigs(String configName, ConfigData<CONFIG_DATATYPE> configData) throws IOException;

    /**
     * add new config to a registered service name
     *
     * @param configName
     * @param configId
     * @param configObject (entityBean/String(json)/JSONObject)
     * @throws IOException
     */
    void addConfig(String configName, String configId, JSONObject configObject) throws IOException;

    Optional<CONFIG_DATATYPE> getConfig(String configName, String configId) throws IOException;

    /**
     * update config to a registered service name, if the configObject is String / JSONObject, it can be partial data and
     * copy into existing config.
     *
     * @param configName
     * @param configId
     * @param configObject (entityBean/String(json)/JSONObject)
     * @throws IOException
     */
    void updateConfig(String configName, String configId, JSONObject config) throws IOException;
    /**
     * **replace** all configs for the registered service name
     *
     * @param configName
     * @param configData
     * @throws IOException
     */
    void updateConfigs(String configName, ConfigData<CONFIG_DATATYPE> configData) throws IOException;

    /**
     * delete one config from registered service name
     *
     * @param configName
     * @param configId
     * @throws IOException
     */
    void deleteConfig(String configName, String configId) throws IOException;

    /**
     * unregister a service from config manager, it will remove both schema and configs
     *
     * @param configName
     */
    void unregister(String configName) throws IOException;

    /**
     * get all configs by registered config name
     *
     * @param configName
     * @return configs
     */
    Optional<Map<String, CONFIG_DATATYPE>> getConfigs(String configName) throws IOException;
}
