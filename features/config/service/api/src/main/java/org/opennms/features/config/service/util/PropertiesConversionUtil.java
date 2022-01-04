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

package org.opennms.features.config.service.util;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.opennms.features.config.service.api.JsonAsString;

public class PropertiesConversionUtil {
    // We expect to have a flat json with only simple data types as children.
    // Returns an immutable map.
    public static Map<String, Object> jsonToMap(String jsonString) {
        JSONObject json = new JSONObject(Objects.requireNonNull(jsonString));
        Map<String, Object> map = new ConcurrentHashMap<>();
        for(String key : json.keySet()) {
            Object value = json.get(key);
            if(value != null && !JSONObject.NULL.equals(value)) {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * We expect to have a flat json with only simple data types as children.
     * Will remove null values since Properties don't support them.
     */
    public static Properties jsonToProperties(String json) {
        Objects.requireNonNull(json);
        Properties props = new Properties();
        props.putAll(jsonToMap(json));
        return props;
    }

    /** We return a JSonString object, otherwise Osgi wil fail, see details at JsonAsString */
    public static JsonAsString mapToJsonString(Map<String,?> map) {
        JSONObject json = new JSONObject();
        for(Map.Entry<?,?> entry : map.entrySet()) {
            Object value = entry.getValue();
            if(value == null) {
                value = JSONObject.NULL; // otherwise, the entry is removed altogether
            }
            json.put(entry.getKey().toString(), value);
        }
        return new JsonAsString(json.toString());
    }

}
