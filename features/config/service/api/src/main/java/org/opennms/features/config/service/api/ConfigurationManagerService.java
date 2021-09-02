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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigSchema;

/**
 * Responsible for managing Schemas and Configurations.
 * A Schema is a set of rules that constrain the data of a Configuration.
 * A Configuration is the data that defines the runtime behaviour of a service together with the code of that service.
 */
public interface ConfigurationManagerService {

    /**
     * Register a service to use config manager
     *
     * @param configName
     * @param majorVersion
     * @param minorVersion
     * @param patchVersion
     * @param entityClass (Must have ValidateUsing annotation)
     * @param <ENTITY>
     * @throws IOException
     * @throws JAXBException
     */
    <ENTITY> void registerSchema(String configName, int majorVersion, int minorVersion,
                                 int patchVersion, Class<ENTITY> entityClass) throws IOException, JAXBException;

    <ENTITY> void registerSchema(String configName, Version version, Class<ENTITY> entityClass) throws IOException, JAXBException;

    void registerSchema(String configName, int majorVersion, int minorVersion,
                        int patchVersion, ConfigConverter converter) throws IOException;

    /**
     * Get the registered Schema
     *
     * @param configName
     * @return ConfigSchema
     * @throws IOException
     */
    Optional<ConfigSchema<?>> getRegisteredSchema(String configName) throws IOException;

    /**
     * register a new configuration by JSONObject.
     * It will make sure the configId is not duplicated !!!
     *
     * @param configName
     * @param configId
     * @param configObject
     * @throws IOException
     */
    void registerConfiguration(String configName, String configId, Object configObject)
            throws IOException;

    /**
     * remove configure from service
     *
     * @param configId
     * @throws IOException
     */
    void unregisterConfiguration(String configName, String configId) throws IOException;

    void updateConfiguration(String configName, String configId,
                                         Object configObject)
            throws IOException;

    /**
     * get config as configObject by configName, configId and Config class
     *
     * @param configName
     * @param configId
     * @param entityClass
     * @param <ENTITY>
     * @return configObject
     * @throws IOException
     */
    <ENTITY> Optional<ENTITY> getConfiguration(String configName, String configId, Class<ENTITY> entityClass)
            throws IOException;

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


    final class Version {
        int majorVersion;
        int minorVersion;
        int patchVersion;
        public Version(int majorVersion, int minorVersion, int patchVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.patchVersion = patchVersion;
        }

        public int getMajorVersion() {
            return majorVersion;
        }

        public void setMajorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
        }

        public int getMinorVersion() {
            return minorVersion;
        }

        public void setMinorVersion(int minorVersion) {
            this.minorVersion = minorVersion;
        }

        public int getPatchVersion() {
            return patchVersion;
        }

        public void setPatchVersion(int patchVersion) {
            this.patchVersion = patchVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Version version = (Version) o;
            return majorVersion == version.majorVersion && minorVersion == version.minorVersion && patchVersion == version.patchVersion;
        }

        @Override
        public int hashCode() {
            return Objects.hash(majorVersion, minorVersion, patchVersion);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Version.class.getSimpleName() + "[", "]")
                    .add("majorVersion=" + majorVersion)
                    .add("minorVersion=" + minorVersion)
                    .add("patchVersion=" + patchVersion)
                    .toString();
        }
    }
    //    Optional<ConfigData<JSONObject>> getConfigurationMetaData(final String configName);
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
