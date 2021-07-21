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

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ConfigStoreDao<CONFIG_DATATYPE> {

    /**
     * register service to config manager
     *
     * @param configSchema
     * @return status
     */
    void register(ConfigSchema<?> configSchema) throws IOException;

    /**
     * get all services managing by config manager
     *
     * @return list of configdata
     * @throws IOException
     */

    Optional<Set<String>> getServiceIds();

    Optional<Set<String>> getConfigIds();

    /**
     * get configs meta by configName
     *
     * @param configName
     * @return status
     */
    Optional<ConfigSchema<?>> getConfigSchema(String configName) throws IOException;

    /**
     * update configs meta by configName
     * @param configSchema
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void updateConfigSchema(ConfigSchema<?> configSchema) throws IOException;

    Optional<List<ConfigData>> getServices() throws IOException;
    /**
     * get configs data by configName and configId
     *
     * @param configName
     * @return config object
     * @throws IOException
     */
    Optional<ConfigData<CONFIG_DATATYPE>> getConfigData(String configName) throws IOException;

    /**
     * add configs for the registered service name, return false is config already exist
     * @param configName
     * @param configData
     * @throws IOException
     */
    void addConfigs(String configName, ConfigData<CONFIG_DATATYPE> configData) throws IOException;

    /**
     * add new config to a registered service name
     * @param configName
     * @param configId
     * @param config
     * @throws IOException
     */
    void addConfig(String configName, String configId, JSONObject config) throws IOException;

    Optional<CONFIG_DATATYPE> getConfig(String configName, String configId) throws IOException;

    /**
     * update config to a registered service name
     * @param configName
     * @param configId
     * @param config
     * @throws IOException
     */
    void updateConfig(String configName, String configId, JSONObject config) throws IOException;

    /**
     * **replace** all configs for the registered service name
     * @param configName
     * @param configData
     * @throws IOException
     */
    void updateConfigs(String configName, ConfigData<CONFIG_DATATYPE> configData) throws IOException;

    /**
     * delete one config from registered service name
     * @param configName
     * @param configId
     * @throws IOException
     */
    boolean deleteConfig(String serviceName, String filename) throws IOException;

    /**
     * deregister a service from config manager
     *
     * @param configName
     */
    void unregister(String configName) throws IOException;

    /**
     * get all configs by registered service name
     *
     * @param configName
     * @return configs
     */
    Optional<Map<String, CONFIG_DATATYPE>> getConfigs(String configName) throws IOException;
}
