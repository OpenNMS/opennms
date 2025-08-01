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
