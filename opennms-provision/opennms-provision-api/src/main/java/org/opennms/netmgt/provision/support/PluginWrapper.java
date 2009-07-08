/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

 package org.opennms.netmgt.provision.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class PluginWrapper {
    private Map<String,Set<String>> m_required = new TreeMap<String,Set<String>>();
    private Map<String,Set<String>> m_optional = new TreeMap<String,Set<String>>();
    
    private final String m_className;

    public PluginWrapper(String className) throws ClassNotFoundException {
        this(Class.forName(className));
    }
    
    public PluginWrapper(Class<?> clazz) throws ClassNotFoundException {
        m_className = clazz.getName();
        BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(m_className));

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (pd.getName().equals("class")) {
                continue;
            }
            Method m = pd.getReadMethod();
            if (m.isAnnotationPresent(Require.class)) {
                Set<String> values = new TreeSet<String>();
                Require a = m.getAnnotation(Require.class);
                for (String key: a.value()) {
                    values.add(key);
                }
                m_required.put(pd.getName(), values);
            } else {
                m_optional.put(pd.getName(), new HashSet<String>());
            }
        }
    }

    public String getClassName() {
        return m_className;
    }

    public Map<String,Boolean> getRequired() {
        Map<String,Boolean> ret = new HashMap<String,Boolean>();
        for (String key : m_required.keySet()) {
            ret.put(key, true);
        }
        for (String key : m_optional.keySet()) {
            ret.put(key, false);
        }
        return ret;
    }

    public Set<String> getRequiredKeys() {
        return m_required.keySet();
    }
    public Set<String> getOptionalKeys() {
        return m_optional.keySet();
    }

    public Map<String,Set<String>> getRequiredItems() {
        return m_required;
    }
    public Map<String,Set<String>> getOptionalItems() {
        return m_optional;
    }
}
