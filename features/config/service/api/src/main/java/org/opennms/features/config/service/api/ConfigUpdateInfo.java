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

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.util.Objects;

public class ConfigUpdateInfo {

    public static String WILDCARD_ID = "*";

    final private String configName;
    final private String configId;
    final private JSONObject configJson;

    /**
     * ConfigId is nullable, when it is null. It will reload all configIds.
     *
     * @param configName
     * @param configId
     * @param configJson (nullable)
     */
    public ConfigUpdateInfo(String configName, String configId, JSONObject configJson) {
        this.configName = Objects.requireNonNull(configName);
        this.configId = Objects.requireNonNull(configId);
        this.configJson = configJson;
    }

    public ConfigUpdateInfo(String configName) {
        this(configName, DEFAULT_CONFIG_ID, null);
    }

    public ConfigUpdateInfo(String configName, String configId) {
        this(configName, configId, null);
    }

    public ConfigUpdateInfo(String configName, JSONObject configJson) {
        this(configName, DEFAULT_CONFIG_ID, configJson);
    }

    public String getConfigName() {
        return configName;
    }

    public String getConfigId() {
        return configId;
    }

    public JSONObject getConfigJson() {
        return configJson;
    }

    @Override
    public int hashCode() {
        return Objects.hash(configName, configId);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ConfigUpdateInfo)) {
            return false;
        }
        return ((ConfigUpdateInfo) obj).configName.equals(this.configName)
                && ((ConfigUpdateInfo) obj).configId.equals(this.configId);
    }
}
