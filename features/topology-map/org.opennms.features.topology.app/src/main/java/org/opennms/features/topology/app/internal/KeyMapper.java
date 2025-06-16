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
package org.opennms.features.topology.app.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class KeyMapper implements Serializable {

    private static final long serialVersionUID = 1997604505886247127L;

    private int lastKey = 0;

    private final Map<Object, String> objectKeyMap = new HashMap<>();

    private final Map<String, Object> keyObjectMap = new HashMap<>();

    private String m_prefix;
    
    public KeyMapper(String prefix) {
        m_prefix = prefix;
    }
    
    /**
     * Gets key for an object.
     * 
     * @param o
     *            the object.
     */
    public String key(Object o) {

        if (o == null) {
            return "null";
        }

        // If the object is already mapped, use existing key
        String key = objectKeyMap.get(o);
        if (key != null) {
            return key;
        }

        // If the object is not yet mapped, map it
        key = getNextKey();
        objectKeyMap.put(o, key);
        keyObjectMap.put(key, o);

        return key;
    }

    private String getNextKey() {
        return m_prefix + String.valueOf(++lastKey);
    }

    /**
     * Retrieves object with the key.
     * 
     * @param key
     *            the name with the desired value.
     * @return the object with the key.
     */
    public Object get(String key) {

        return keyObjectMap.get(key);
    }

    /**
     * Removes object from the mapper.
     * 
     * @param removeobj
     *            the object to be removed.
     */
    public void remove(Object removeobj) {
        final String key = objectKeyMap.get(removeobj);

        if (key != null) {
            objectKeyMap.remove(removeobj);
            keyObjectMap.remove(key);
        }
    }

    /**
     * Removes all objects from the mapper.
     */
    public void removeAll() {
        objectKeyMap.clear();
        keyObjectMap.clear();
    }
}

