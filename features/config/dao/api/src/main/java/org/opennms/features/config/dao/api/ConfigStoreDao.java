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

package org.opennms.features.config.dao.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.opennms.features.config.exception.ValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * It handles storing config data in database by generic datatype
 * It also validates config before persist. (add & update)
 *
 * @param <T> data type store in database
 */
public interface ConfigStoreDao<T> {

    /**
     * register service to config manager
     *
     * @param configDefinition
     * @return status
     */
    void register(ConfigDefinition configDefinition);

    /**
     * get all config names managing by config manager
     *
     * @return list of config name
     * @throws JsonProcessingException
     */
    Set<String> getConfigNames();

    Map<String, ConfigDefinition> getAllConfigDefinitions();

    /**
     * get configs meta by configName
     *
     * @param configName
     * @return status
     */
    Optional<ConfigDefinition> getConfigDefinition(String configName);

    /**
     * update configs meta by configName
     *
     * @param configDefinition
     * @throws JsonProcessingException
     * @throws ClassNotFoundException
     * @throws ValidationException
     */
    void updateConfigDefinition(ConfigDefinition configDefinition) throws ValidationException;

    /**
     * get configs data by configName
     * It gives the raw ConfigData object.
     * If you only want to get the specific config, you should use getConfig(String, String)
     *
     * @param configName
     * @return config object
     * @see #getConfig(String, String)
     * @see ConfigData
     */
    Optional<ConfigData<T>> getConfigs(String configName);

    /**
     * add configs for the registered service name, throws exception if config already exist
     *
     * @param configName
     * @param configData
     * @throws ValidationException
     */
    void addConfigs(String configName, ConfigData<T> configData) throws ValidationException;

    /**
     * add new config to a registered service name
     *
     * @param configName
     * @param configId
     * @param configObject (JSONObject)
     * @throws ValidationException
     */
    void addConfig(String configName, String configId, JSONObject configObject) throws ValidationException;

    Optional<T> getConfig(String configName, String configId);

    /**
     * **replace** all configs for the registered service name
     *
     * @param configName
     * @param configData
     * @throws ValidationException
     */
    void updateConfigs(String configName, ConfigData<T> configData) throws ValidationException;

    /**
     * delete one config from registered service name
     *
     * @param configName
     * @param configId
     */
    void deleteConfig(String configName, String configId);

    /**
     * unregister a service from config manager, it will remove both schema and configs
     *
     * @param configName
     */
    void unregister(String configName);

    /**
     * get all configs by registered config name
     *
     * @param configName
     * @return configs
     */
    Map<String, T> get(String configName);
}

