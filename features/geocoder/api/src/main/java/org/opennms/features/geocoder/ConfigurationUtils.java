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
package org.opennms.features.geocoder;

import java.util.Map;

/**
 * Helper class to deal with String values.
 *
 * @author mvrueden
 */
public abstract class ConfigurationUtils {

    public static final String PROVIDE_A_VALUE_TEXT = "Please provide a value";
    public static final String URL_NOT_VALID_TEMPLATE = "The provided URL ''{0}'' is not valid: ''{1}''";

    private ConfigurationUtils() {}

    public static <T> T getValue(Map<String, Object> properties, String key, T defaultValue) {
        final Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    public static Boolean getBoolean(Map<String, Object> properties, String key, Boolean defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.valueOf(((String) value).toLowerCase());
        }
        return defaultValue;
    }

    public static Integer getInteger(Map<String, Object> properties, String key, Integer defaultValue) {
        Object value = properties.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer)value;
        }
        if (value instanceof String) {
            return Integer.valueOf(value.toString());
        }
        return defaultValue;
    }
}
