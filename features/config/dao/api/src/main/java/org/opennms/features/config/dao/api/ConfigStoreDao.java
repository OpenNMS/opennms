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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opennms.features.distributed.kvstore.api.KeyValueStore;

public interface ConfigStoreDao<T> extends KeyValueStore<String> {

    /**
     * register service to config manager
     * @param configData
     * @return status
     */
    public boolean register(ConfigData<T> configData);

    /**
     * get config data by serviceName
     * @param serviceName
     * @return ConfigData
     */
    public Optional<ConfigData<T>> getConfigData(String serviceName);

    /**
     * add new config to a registered service name
     * @param serviceName
     * @param filename
     * @param config
     * @return status
     */
    public boolean addOrUpdateConfig(String serviceName, String filename, T config);

    /**
     * replace all configs for the registered service name
     * @param serviceName
     * @param configs
     * @return status
     */
    public boolean updateConfigs(String serviceName, Map<String, T> configs);

    /**
     * delete one config from registered service name
     * @param serviceName
     * @param filename
     * @return status
     */
    public boolean deleteConfig(String serviceName, String filename);

    /**
     * deregister a service from config manager
     * @param serviceName
     */
    public void deregister(String serviceName);

    /**
     * get all configs by registered service name
     * @param serviceName
     * @return configs
     */
    public Optional<Map<String, T>> getConfigs(String serviceName);
}
