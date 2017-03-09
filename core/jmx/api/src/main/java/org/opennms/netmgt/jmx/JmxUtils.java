/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.config.collectd.jmx.Mbean;

public final class JmxUtils {

    /**
     * Converts the map, so that it only contains String values. All non String values will be removed (null values included).
     * <p/>
     * The returned map is not modifiable.
     * <p/>
     * If the input map is null, null is also returned.
     *
     * @param map The map to be converted. May be null.
     * @return An unmodifiable map containing only String values from the input map, or null if input map was null.
     */
    public static Map<String, String> convertToUnmodifiableStringMap(final Map<String, Object> map) {
        if (map != null) {
            Map<String, String> convertedProperties = new HashMap<>();
            for (Map.Entry<String, Object> eachEntry : map.entrySet()) {
                if (eachEntry.getValue() != null) {
                    convertedProperties.put(eachEntry.getKey(), eachEntry.getValue().toString());
                }
            }
            return Collections.unmodifiableMap(convertedProperties);
        }
        return null;
    }

    /**
     * Converts the map, so that it only contains String values. All non String values will be removed (null values included).
     * <p/>
     * The returned map is modifiable.
     * <p/>
     * If the input map is null, null is also returned.
     *
     * @param map The map to be converted. May be null.
     * @return An unmodifiable map containing only String values from the input map, or null if input map was null.
     */
    public static Map<String, String> convertToStringMap(final Map<String, Object> map) {
        if (map != null) {
            Map<String, String> convertedProperties = new HashMap<>();
            for (Map.Entry<String, Object> eachEntry : map.entrySet()) {
                if (eachEntry.getValue() != null) {
                    convertedProperties.put(eachEntry.getKey(), eachEntry.getValue().toString());
                }
            }
            return convertedProperties;
        }
        return null;
    }

    public static String getCollectionDirectory(final Map<String, String> map, final String friendlyName, final String serviceName) {
        Objects.requireNonNull(map, "Map must be initialized!");

        if (friendlyName != null && !friendlyName.isEmpty()) {
            return friendlyName;
        }
        if (serviceName != null && !serviceName.isEmpty()) {
            return serviceName.toLowerCase();
        }
        final String port = map.get(ParameterName.PORT.toString());
        return port;
    }

    public static String getGroupName(final Map<String, String> map, final Mbean mbean) {
        final boolean useMbeanForRrds = Boolean.valueOf(map.get(ParameterName.USE_MBEAN_NAME_FOR_RRDS.toString()));
        final String groupName = useMbeanForRrds ? mbean.getName() : mbean.getObjectname();
        return groupName;
    }

    private JmxUtils() {

    }
}
