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
