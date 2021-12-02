/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2021 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.opennms.features.config.service.api.JsonAsString;

public class PropertiesConversionUtil {
    // We expect to have a flat json with only simple data types as children.
    // Returns an immutable map.
    public static Map<String, String> jsonToMap(String jsonString) {
        JSONObject  json = new JSONObject(jsonString);
        Map<String, String> map = new HashMap<>();
        for(String key : json.keySet()) {
            String value = Optional.of(json.get(key))
                    .map(o -> JSONObject.NULL.equals(o) ? null : o) // map back to Java null
                    .map(Object::toString)
                    .orElse(null);
            map.put(key, value);
        }
        return map;
    }

    /** We return a JSonString object, otherwise Osgi wil fail, see details at JsonAsString */
    public static JsonAsString propertiesToJsonString(Map<String,?> map) {
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
