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
    boolean register(ConfigSchema<?> configSchema) throws IOException;

    /**
     * get all services managing by config manager
     *
     * @return list of configdata
     * @throws IOException
     */

    Optional<Set<String>> getServiceIds();

    Optional<Set<String>> getConfigIds();

    /**
     * get configs meta by serviceName
     *
     * @param serviceName
     * @return status
     */
    Optional<ConfigSchema<?>> getConfigSchema(String serviceName) throws IOException, ClassNotFoundException;

    /**
     * update configs meta by serviceName
     *
     * @param configSchema
     * @return update status
     */
    boolean updateConfigSchema(ConfigSchema<?> configSchema) throws IOException, ClassNotFoundException;

    /**
     * get configs data by serviceName and configId
     *
     * @param serviceName
     * @return config object
     * @throws IOException
     */
    Optional<ConfigData<CONFIG_DATATYPE>> getConfigData(final String serviceName) throws IOException;

    /**
     * add configs for the registered service name, return false is config already exist
     *
     * @param serviceName
     * @param configData
     * @return status
     */
    boolean addConfigs(String serviceName, ConfigData<CONFIG_DATATYPE> configData) throws IOException;

    /**
     * add new config to a registered service name
     *
     * @param serviceName
     * @param configId
     * @param config
     * @return status
     */
    boolean addConfig(String serviceName, String configId, JSONObject config) throws IOException;

    Optional<CONFIG_DATATYPE> getConfig(String serviceName, String configId) throws IOException;

    /**
     * update config to a registered service name
     *
     * @param serviceName
     * @param configId
     * @param config
     * @return status
     */
    boolean updateConfig(String serviceName, String configId, JSONObject config) throws IOException;

    /**
     * **replace** all configs for the registered service name
     *
     * @param serviceName
     * @param configData
     * @return status
     */
    boolean updateConfigs(String serviceName, ConfigData<CONFIG_DATATYPE> configData) throws IOException;

    /**
     * delete one config from registered service name
     *
     * @param serviceName
     * @param configId
     * @return status
     */
    boolean deleteConfig(String serviceName, String configId) throws IOException;

    /**
     * deregister a service from config manager
     *
     * @param serviceName
     */
    void unregister(String serviceName) throws IOException;

    /**
     * get all configs by registered service name
     *
     * @param serviceName
     * @return configs
     */
    Optional<Map<String, CONFIG_DATATYPE>> getConfigs(String serviceName) throws IOException;
}