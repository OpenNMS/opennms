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

package org.opennms.features.config.service;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/** Responsible for managing Schemas and Configurations.
 * A Schema is a set of rules that constrain the data of a Configuration.
 * A Configuration is the data that defines the runtime behaviour of a service together with the code of that service.
 */
public interface ConfigurationManager {

    void registerSchema(String schemaId, String pathToXsd, String topLevelElement) throws IOException;

    boolean isSchemaRegistered(String schemaId);

    Optional<ServiceSchema> getRegisteredSchema(String schemaId);

    void registerConfiguration(final String configId, String schemaId);

    boolean isConfigurationRegistered(final String configId);

    void unregisterConfiguration(final String configId);

    Optional<Configuration> getConfigurationMetaData(final String configId);

    Optional<JSONObject> getConfiguration(final String configId);

    Set<String> getConfigurationIds();

    ServiceSchema getSchemaForConfiguration(final String configId);

    /** add: inserts a child into a parent element */
    void addConfiguration(final String configId, final String path, final String content);

    /** Inserts or updates an configuration element at the given path. */
    void putConfiguration(final String configId, final String path, final String content);

    void putConfiguration(final String configId, JSONObject object);

    /** Removes all configuration objects which can be found with the given xpath expression. */

    void removeConfiguration(final String configId, final String path);
}
