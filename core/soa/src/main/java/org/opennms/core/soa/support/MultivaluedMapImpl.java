/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.soa.support;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * MultivaluedMapImpl
 *
 * @author brozow
 */
public class MultivaluedMapImpl<K, V> extends LinkedHashMap<K, Set<V>>  implements MultivaluedMap<K, V> {
    private static final long serialVersionUID = -4898439337217431661L;

    public static class SynchronizedMultivaluedMap<Key, Value> implements MultivaluedMap<Key, Value> {
        MultivaluedMap<Key, Value> m_data;
        Object m_lock;


        public SynchronizedMultivaluedMap(MultivaluedMap<Key, Value> m) {
            m_data = m;
            m_lock = this;
        }

        @Override
        public void add(final Key key, final Value value) {
            synchronized (m_lock) {
                m_data.add(key, value);
            }
        }

        @Override
        public boolean remove(final Object key, final Object value) {
            synchronized (m_lock) {
                return m_data.remove(key, value);
            }
        }

        @Override
        public Set<Value> getCopy(final Key key) {
            synchronized (m_lock) {
                return m_data.getCopy(key);
            }
        }

        @Override
        public void clear() {
            synchronized (m_lock) {
                m_data.clear();
            }
        }

        @Override
        public boolean containsKey(final Object key) {
            synchronized (m_lock) {
                return m_data.containsKey(key);
            }
        }

        @Override
        public boolean containsValue(final Object value) {
            synchronized (m_lock) {
                return m_data.containsValue(value);
            }
        }

        @Override
        public Set<java.util.Map.Entry<Key, Set<Value>>> entrySet() {
            synchronized (m_lock) {
                return m_data.entrySet();
            }
        }

        @Override
        public Set<Value> get(final Object key) {
            synchronized (m_lock) {
                return m_data.get(key);
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (m_lock) {
                return m_data.isEmpty();
            }
        }

        @Override
        public Set<Key> keySet() {
            synchronized (m_lock) {
                return m_data.keySet();
            }
        }

        @Override
        public Set<Value> put(final Key key, final Set<Value> value) {
            synchronized (m_lock) {
                return m_data.put(key, value);
            }
        }

        @Override
        public void putAll(final Map<? extends Key, ? extends Set<Value>> t) {
            synchronized (m_lock) {
                m_data.putAll(t);
            }
        }

        @Override
        public Set<Value> remove(final Object key) {
            synchronized (m_lock) {
                return m_data.remove(key);
            }
        }

        @Override
        public int size() {
            synchronized (m_lock) {
                return m_data.size();
            }
        }

        @Override
        public Collection<Set<Value>> values() {
            synchronized (m_lock) {
                return m_data.values();
            }
        }

    }

    public MultivaluedMapImpl() {
        super();
    }

    @Override
    public void add(final K key, final V value) {
        if (!containsKey(key)) {
            final LinkedHashSet<V> valueList = new LinkedHashSet<>();
            valueList.add(value);
            put(key, valueList);
        } else {
            get(key).add(value);
        }
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        if (!containsKey(key)) return false;

        final Set<V> valueList = get(key);
        final boolean found = valueList.remove(value);

        if (valueList.isEmpty()) {
            remove(key);
        }

        return found;
    }

    @Override
    public Set<V> getCopy(final K key) {
        final Set<V> values = get(key);
        return values == null ? null : new LinkedHashSet<V>(values);
    }

    public static <Key, Value> MultivaluedMap<Key, Value> synchronizedMultivaluedMap(final MultivaluedMap<Key, Value> m) {
        return new SynchronizedMultivaluedMap<Key, Value>(m);
    }

    public static <Key, Value> MultivaluedMap<Key, Value> synchronizedMultivaluedMap() {
        return synchronizedMultivaluedMap(new MultivaluedMapImpl<Key, Value>());
    }
}
