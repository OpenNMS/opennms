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
package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.netmgt.provision.support.PluginWrapper;

/**
 * <p>PluginParameter class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_key", "m_value" })
public class PluginParameter implements Serializable, Comparable<PluginParameter> {

    private static final long serialVersionUID = -6314596729655404812L;

    @XmlAttribute(name="key")
    private String m_key = null;

    @XmlAttribute(name="value")
    private String m_value = null;

    @XmlTransient
    private PluginConfig m_parent = null;

    /**
     * <p>Constructor for PluginParameter.</p>
     */
    public PluginParameter() {
    }
    
    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public PluginParameter(String key, String value) {
        m_key = key;
        m_value = value;
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param e a {@link java.util.Map.Entry} object.
     */
    public PluginParameter(Entry<String, String> e) {
        m_key = e.getKey();
        m_value = e.getValue();
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public PluginParameter(PluginConfig pluginConfig, String key, String value) {
        this(key, value);
        m_parent = pluginConfig;
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param set a {@link java.util.Map.Entry} object.
     */
    public PluginParameter(PluginConfig pluginConfig, Entry<String, String> set) {
        this(set);
        m_parent = pluginConfig;
    }

    /**
     * <p>setPluginConfig</p>
     *
     * @param pc a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public void setPluginConfig(PluginConfig pc) {
        m_parent = pc;
    }

    /**
     * <p>getKey</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKey() {
        return m_key;
    }
    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValue() {
        return m_value;
    }
    /**
     * <p>setKey</p>
     *
     * @param key a {@link java.lang.String} object.
     */
    public void setKey(String key) {
        m_key = key;
    }
    /**
     * <p>setValue</p>
     *
     * @param value a {@link java.lang.String} object.
     */
    public void setValue(String value) {
        m_value = value;
    }

    /**
     * <p>getAvailableParameterKeys</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getAvailableParameterKeys() {
        Set<String> keys = new TreeSet<>();
        if (m_parent != null) {
            try {
                PluginWrapper pw = new PluginWrapper(m_parent.getPluginClass());
                keys = pw.getOptionalKeys();
                for (PluginParameter p : m_parent.getParameters()) {
                    if (getKey() == null) {
                        if (p.getKey() != null) {
                            keys.remove(p.getKey());
                        }
                    } else if (!getKey().equals(p.getKey())) {
                        keys.remove(p.getKey());
                    }
                }
            } catch (ClassNotFoundException e) {
                // we just let it return the empty set
            }
        }
        return keys;
    }

    @Override
    public int hashCode() {
        final int prime = 421;
        int result = 1;
        result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof PluginParameter)) return false;
        final PluginParameter other = (PluginParameter) obj;
        if (m_key == null) {
            if (other.m_key != null) return false;
        } else if (!m_key.equals(other.m_key)) {
            return false;
        }
        if (m_value == null) {
            if (other.m_value != null) return false;
        } else if (!m_value.equals(other.m_value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PluginParameter [key=" + m_key + ", value=" + m_value + "]";
    }

    @Override
    public int compareTo(final PluginParameter other) {
        return new CompareToBuilder()
            .append(m_key, other.m_key)
            .append(m_value, other.m_value)
            .toComparison();
    }
}
