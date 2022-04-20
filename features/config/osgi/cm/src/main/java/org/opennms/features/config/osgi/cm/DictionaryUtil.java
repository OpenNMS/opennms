/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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
