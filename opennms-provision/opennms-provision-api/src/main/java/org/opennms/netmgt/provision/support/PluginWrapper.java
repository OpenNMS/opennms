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

/**
 * <p>PluginWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PluginWrapper {
    private Map<String,Set<String>> m_required = new TreeMap<String,Set<String>>();
    private Map<String,Set<String>> m_optional = new TreeMap<String,Set<String>>();
    
    private final String m_className;

    /**
     * <p>Constructor for PluginWrapper.</p>
     *
     * @param className a {@link java.lang.String} object.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public PluginWrapper(String className) throws ClassNotFoundException {
        this(Class.forName(className));
    }
    
    /**
     * <p>Constructor for PluginWrapper.</p>
     *
     * @param clazz a {@link java.lang.Class} object.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public PluginWrapper(Class<?> clazz) throws ClassNotFoundException {
        m_className = clazz.getName();
        BeanWrapper wrapper = new BeanWrapperImpl(Class.forName(m_className));

        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (pd.getName().equals("class")) {
                continue;
            }
            final Method m = pd.getReadMethod();
            if (m != null) {
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
    }

    /**
     * <p>getClassName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * <p>getRequired</p>
     *
     * @return a {@link java.util.Map} object.
     */
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

    /**
     * <p>getRequiredKeys</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getRequiredKeys() {
        return m_required.keySet();
    }
    /**
     * <p>getOptionalKeys</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getOptionalKeys() {
        return m_optional.keySet();
    }

    /**
     * <p>getRequiredItems</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,Set<String>> getRequiredItems() {
        return m_required;
    }
    /**
     * <p>getOptionalItems</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String,Set<String>> getOptionalItems() {
        return m_optional;
    }
}
