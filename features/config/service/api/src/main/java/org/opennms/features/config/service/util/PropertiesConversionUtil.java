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
