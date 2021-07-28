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

import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigMeta;
import org.opennms.features.config.dao.api.XmlConfigConverter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Responsible for managing Schemas and Configurations.
 * A Schema is a set of rules that constrain the data of a Configuration.
 * A Configuration is the data that defines the runtime behaviour of a service together with the code of that service.
 */
public interface ConfigurationManagerService<CONFIG_STORE_TYPE> {

    /**
     * Register a service to use config manager
     *
     * @param serviceName
     * @param majorVersion
     * @param minorVersion
     * @param patchVersion
     * @param entityClass (Must have ValidateUsing annotation)
     * @param <ENTITY>
     * @throws IOException
     * @throws ClassNotFoundException
     */
    <ENTITY> void registerSchema(final String serviceName, final int majorVersion, final int minorVersion,
                                 final int patchVersion, Class<ENTITY> entityClass) throws IOException, ClassNotFoundException;

    void registerSchema(final String serviceName, final int majorVersion, final int minorVersion,
                        final int patchVersion, final XmlConfigConverter converter) throws IOException, ClassNotFoundException;

    /**
     * Get the registered Schema
     *
     * @param serviceName
     * @return ConfigMeta
     * @throws IOException
     */
    Optional<ConfigMeta<?>> getRegisteredSchema(String serviceName) throws IOException, ClassNotFoundException;

    /**
     * register a new configuration by xml
     *
     * @param serviceName
     * @param configId
     * @param xmlPath
     * @throws IOException
     */
    <ENTITY> void registerConfiguration(final String serviceName, final String configId, final String xmlPath)
            throws IOException, ClassNotFoundException;

    /**
     * register a new configuration by JSONObject
     *
     * @param serviceName
     * @param configId
     * @param object
     * @throws IOException
     */
    <ENTITY> void registerConfiguration(final String serviceName, final String configId, final CONFIG_STORE_TYPE object)
            throws IOException;

    /**
     * remove configure from service
     *
     * @param configId
     * @throws IOException
     */
    void unregisterConfiguration(final String serviceName, final String configId) throws IOException;

    boolean updateConfiguration(final String serviceName, final String configId,
                                                    final CONFIG_STORE_TYPE object) throws IOException;

    Optional<CONFIG_STORE_TYPE> getConfiguration(final String serviceName, final String configId) throws IOException;

    Set<String> getServiceIds() throws IOException;

    /**
     * it will remove both config and schema
     * @param serviceName
     * @throws IOException
     */
    void unregisterSchema(final String serviceName) throws IOException;

    Optional<ConfigData<CONFIG_STORE_TYPE>> getConfigData(String serviceName) throws IOException;
    //    Optional<ConfigData<JSONObject>> getConfigurationMetaData(final String serviceName);
    //    ConfigData getSchemaForConfiguration(final String configId);
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
