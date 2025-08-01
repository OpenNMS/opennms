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
        m_className = clazz.getCanonicalName();
        BeanWrapper wrapper = new BeanWrapperImpl(clazz);

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
