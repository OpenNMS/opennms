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
package org.opennms.features.config.service.api;

import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.exception.ValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Responsible for managing Schemas and Configurations.
 * A Schema is a set of rules that constrain the data of a Configuration.
 * A Configuration is the data that defines the runtime behaviour of a service together with the code of that service.
 *
 * @apiNote Due to the classloading behaviour. Do not use any non-primitive API parameters via OSGi interface. It will subject to <b>FAIL</b>!!!
 */
public interface ConfigurationManagerService {
    String BASE_PATH = "/rest/cm";

    /**
     * Registers a ConfigDefinition under a unique configName. If the schema id is present it will throw an IllegalArgumentException.
     */
    void registerConfigDefinition(String configName, ConfigDefinition configDefinition);

    /**
     * Changes a ConfigDefinition. If the configName is not present it will throw an  IllegalArgumentException.
     */
    void changeConfigDefinition(String configName, ConfigDefinition configDefinition);

    Map<String, ConfigDefinition> getAllConfigDefinitions();

    Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName);

    @Deprecated
    default void registerEventHandler(ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
        registerEventHandler(EventType.UPDATE, info, consumer);
    }
    void registerEventHandler(EventType type, ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer);

    /**
     * register a new configuration by config object.
     * It will make sure the configId is not duplicated !!!
     *
     * @param configName
     * @param configId
     * @param configObject (config object / JSONObject)
     */
    void registerConfiguration(String configName, String configId, JsonAsString configObject);
    default void registerConfiguration(ConfigUpdateInfo configIdentifier, JsonAsString configObject) {
        this.registerConfiguration(configIdentifier.getConfigName(), configIdentifier.getConfigId(), configObject);
    }
    /**
     * remove configure from service
     *
     * @param configId
     */
    void unregisterConfiguration(String configName, String configId);

    /**
     * update config to a registered service name. It can be partial data and copy into existing config.
     * The flow of update
     * 1. reading the config by configName & configId
     * 2. if replace is true, the whole new config will be replaced directly
     *    if replace is false, the new config will copy to existing database config by its property keys to config in database
     * 3. validate
     * 4. update db
     *
     * @param configName
     * @param configId
     * @param configObject
     * @param isReplace
     * @throws ValidationException
     */
    void updateConfiguration(String configName, String configId,
                             JsonAsString configObject, boolean isReplace);

    /**
     * get config as json by configName, configId
     *
     * @param configName
     * @param configId
     * @return JSONObject
     */
    Optional<JSONObject> getJSONConfiguration(String configName, String configId);

    /**
     * Use for osgi API
     *
     * @return config in json string
     * @see #getJSONStrConfiguration(String, String)
     */
    Optional<String> getJSONStrConfiguration(String configName, String configId);
    default Optional<String> getJSONStrConfiguration(ConfigUpdateInfo configIdentifier){
        return getJSONStrConfiguration(configIdentifier.getConfigName(), configIdentifier.getConfigId());
    }
    /**
     * get whole ConfigData by configName
     *
     * @param configName
     * @return ConfigData
     */
    Optional<ConfigData<JSONObject>> getConfigData(String configName);

    /**
     * get a list of registered configName
     *
     * @return configName set
     */
    Set<String> getConfigNames();

    /**
     * it will remove both config and schema
     *
     * @param configName
     */
    void unregisterSchema(String configName);

    /**
     * return configIds by configName
     *
     * @param configName
     */
    Set<String> getConfigIds(String configName);
}
