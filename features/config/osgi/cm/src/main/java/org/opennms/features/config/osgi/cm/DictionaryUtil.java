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
package org.opennms.features.config.osgi.cm;

import static org.opennms.features.config.osgi.cm.CmPersistenceManager.OSGI_PROPERTIES;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

import org.json.JSONObject;
import org.opennms.features.config.service.api.JsonAsString;

public class DictionaryUtil {

    public static Dictionary createFromJson(JsonAsString json) {
        Objects.requireNonNull(json);
        Properties props = new Properties();
        new JSONObject(json.toString())
                .toMap()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != null) // remove null values => not allowed in dictionary
                .forEach(e -> props.put(e.getKey(), e.getValue()));
        return props;
    }

    public static JsonAsString writeToJson(final Dictionary dictionary) {
        Objects.requireNonNull(dictionary);
        JSONObject json = new JSONObject();
        Enumeration keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = dictionary.get(key);
            if(!OSGI_PROPERTIES.contains(key.toString())) {
                json.put(key.toString(), value);
            }
        }
        return new JsonAsString(json.toString());
    }
}
