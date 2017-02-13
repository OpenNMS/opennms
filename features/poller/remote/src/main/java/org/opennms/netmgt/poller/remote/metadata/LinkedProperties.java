/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.metadata;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.keyvalue.DefaultMapEntry;

public class LinkedProperties extends Properties {
    private static final long serialVersionUID = 1L;

    protected LinkedProperties m_linkedDefaults;
    protected Set<Object> m_linkedKeys = new LinkedHashSet<>();

    public LinkedProperties() { super(); }

    public LinkedProperties(final Properties defaultProps) {
        super(defaultProps); // super.defaults = defaultProps;
        if (defaultProps instanceof LinkedProperties) {
            m_linkedDefaults = (LinkedProperties)defaultProps;
        }
    }

    @Override
    public synchronized Enumeration<?> propertyNames() {
        return keys();
    }

    @Override
    public Enumeration<Object> keys() {
        final Set<Object> allKeys = new LinkedHashSet<>();
        if (m_linkedDefaults != null) {
            allKeys.addAll(m_linkedDefaults.m_linkedKeys);
        }
        allKeys.addAll(m_linkedKeys);
        return Collections.enumeration(allKeys);
    }

    @SuppressWarnings("unchecked")
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        final Set<Map.Entry<Object,Object>> ret = new LinkedHashSet<>();
        for (final Object key : m_linkedKeys) {
            ret.add(new DefaultMapEntry(key, get(key)));
        }
        return ret;
    }

    @Override
    public synchronized Object put(final Object key, final Object value) {
        m_linkedKeys.add(key);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(final Object key) {
        m_linkedKeys.remove(key);
        return super.remove(key);
    }

    @Override
    public synchronized void putAll(final Map<?, ?> values) {
        for (final Object key : values.keySet()) {
            m_linkedKeys.add(key);
        }
        super.putAll(values);
    }

    @Override
    public synchronized void clear() {
        super.clear();
        m_linkedKeys.clear();
    }
}
