/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.generic;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods to build a proper map.
 * Cannot use {@link com.google.common.collect.ImmutableMap} as it does not allow duplicate keys, which we actually require.
 */
class MapBuilder<K,V> {

    private final Map<K,V> map;

    MapBuilder() {
        map = new HashMap<>();
    }

    public MapBuilder<K,V> withProperties(Map<K, V> properties){
        this.map.putAll(properties);
        return this;
    }

    public MapBuilder<K,V> withProperty(K key, V value){
        this.map.put(key, value);
        return this;
    }

    public Map<K,V> build(){
        return map;
    }
}
