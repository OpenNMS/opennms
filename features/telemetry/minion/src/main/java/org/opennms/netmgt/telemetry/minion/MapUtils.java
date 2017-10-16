/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.minion;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import java.util.Dictionary;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapUtils {

    public static Map<String, String> fromDict(Dictionary properties) {
        final Iterator<String> keysIter = Iterators.forEnumeration(properties.keys());
        return Maps.toMap(keysIter, key -> (String)properties.get(key));
    }

    public static Map<String, String> filterKeysByPrefix(Map<String, String> map, String prefix) {
        // Extract the keys from the map that are prefixed
        return map.keySet().stream()
                .filter(k -> k.startsWith(prefix))
                .collect(Collectors.toMap(k -> k.substring(prefix.length(), k.length()),
                        k -> map.get(k)));
    }

    public static String getRequiredString(String key, Map<String, String> parameters) {
        final String value = parameters.get(key);
        if (value == null) {
            throw new IllegalArgumentException(String.format("%s must be set.", key));
        }
        return value;
    }

    public static Optional<Integer> getOptionalInteger(String key, Map<String, String> parameters) {
        final String strValue = parameters.get(key);
        if (strValue == null) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(strValue));
    }
}
