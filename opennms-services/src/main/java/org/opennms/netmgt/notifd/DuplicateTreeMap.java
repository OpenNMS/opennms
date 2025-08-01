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
package org.opennms.netmgt.notifd;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>DuplicateTreeMap class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DuplicateTreeMap<K, V> extends TreeMap<K, List<V>> {
    /**
     * 
     */
    private static final long serialVersionUID = 8020472612288161254L;

    /**
     * <p>putItem</p>
     *
     * @param key a K object.
     * @param value a V object.
     * @param <K> a K object.
     * @param <V> a V object.
     * @return a V object.
     */
    public V putItem(K key, V value) {
        List<V> l;
        if (super.containsKey(key)) {
            l = super.get(key);
        } else {
            l = new LinkedList<>();
            put(key, l);
        }
        
        if (l.contains(value)) {
            return value;
        } else {
            l.add(value);
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();

        for (List<V> list : values()) {
            for (V item : list) {
                buffer.append(item.toString() + System.getProperty("line.separator"));
            }
        }

        return buffer.toString();
    }
}

