/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
