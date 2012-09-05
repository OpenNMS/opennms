/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
            l = new LinkedList<V>();
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
        StringBuffer buffer = new StringBuffer();

        for (List<V> list : values()) {
            for (V item : list) {
                buffer.append(item.toString() + System.getProperty("line.separator"));
            }
        }

        return buffer.toString();
    }
}

