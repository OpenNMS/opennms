/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.netmgt.provision.support.PluginWrapper;

/**
 * A PluginConfig represents a portion of a configuration that defines a reference
 * to a Java class "plugin" along with a set of parameters used to configure the
 * behavior of that plugin.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="plugin")
public class PluginConfig implements Serializable, Comparable<PluginConfig> {

    private static final long serialVersionUID = 4307231598310473690L;

    @XmlAttribute(name="name", required=true)
    @NotNull
    private String m_name;

    @XmlAttribute(name="class", required=true)
    @NotNull
    private String m_pluginClass;

    @XmlElement(name="parameter")
    private Set<PluginParameter> m_parameters = new LinkedHashSet<>();

    /**
     * Creates an empty plugin configuration.
     */
    public PluginConfig() {
    }
    
    /**
     * Creates a plugin configuration with the given name and class.
     *
     * @param name the human-readable name of the plugin
     * @param clazz the name of the plugin's java class
     */
    public PluginConfig(String name, String clazz) {
        setName(name);
        setPluginClass(clazz);
    }
    
    /**
     * <p>Constructor for PluginConfig.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     */
    public PluginConfig(PluginConfig pluginConfig) {
        setName(pluginConfig.getName());
        setPluginClass(pluginConfig.getPluginClass());
        setParameterMap(pluginConfig.getParameterMap());
    }

    /**
     * Get the name of the plugin.
     *
     * @return the human-readable name of the plugin
     */
    public String getName() {
        return m_name;
    }
    /**
     * Sets the name of the plugin.
     *
     * @param name the human-readable name to set
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * Get the name of the plugin's java class.
     *
     * @return the plugin's class name
     */
    public String getPluginClass() {
        return m_pluginClass;
    }
    /**
     * Set the name of the plugin's java class.
     *
     * @param clazz a {@link java.lang.String} object.
     */
    public void setPluginClass(String clazz) {
        m_pluginClass = clazz;
    }
    /**
     * Get a {@link List} of the plugin parameters.
     *
     * @return the parameters
     */
    public Set<PluginParameter> getParameters() {
        for (PluginParameter p : m_parameters) {
            p.setPluginConfig(this);
        }
        return m_parameters;
    }
    
    /**
     * <p>setParameters</p>
     *
     * @param list a {@link java.util.Set} object.
     */
    public void setParameters(Set<PluginParameter> list) {
        for (PluginParameter p : list) {
            p.setPluginConfig(this);
        }
        m_parameters = list;
    }
    
    /**
     * <p>getParameterMap</p>
     *
     * @return the parameters
     */
    public Map<String,String> getParameterMap() {
        Map<String,String> parms = new LinkedHashMap<String,String>();
        for (PluginParameter p : getParameters()) {
            parms.put(p.getKey(), p.getValue());
        }
        return Collections.unmodifiableMap(parms);
    }
    
    /**
     * <p>setParameterMap</p>
     *
     * @param parameters the parameters to set
     */
    public void setParameterMap(Map<String, String> parameters) {
        m_parameters.clear();
        for (Entry<String,String> set : parameters.entrySet()) {
            m_parameters.add(new PluginParameter(this, set));
        }
    }

    /**
     * <p>getParameter</p>
     *
     * @param key the parameter name
     * @return the parameter value
     */
    public String getParameter(String key) {
        for (PluginParameter p : getParameters()) {
            if (p.getKey().equals(key)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * <p>addParameter</p>
     *
     * @param key the parameter name
     * @param value the parameter value
     */
    public void addParameter(String key, String value) {
        m_parameters.add(new PluginParameter(this, key, value));
    }

    /**
     * <p>removeParameters</p>
     *
     * @param p a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginParameter} object.
     */
    public void deleteParameters(PluginParameter p) {
        m_parameters.remove(p);
    }

    /**
     * <p>getAvailableParameterKeys</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getAvailableParameterKeys() {
        Set<String> keys = new TreeSet<>();
        try {
            PluginWrapper pw = new PluginWrapper(m_pluginClass);
            keys = pw.getOptionalKeys();
            for (PluginParameter p : getParameters()) {
                keys.remove(p.getKey());
            }
        } catch (ClassNotFoundException e) {
            // we just let it return the empty set
        }
        return keys;
    }

    private String getParametersAsString() {
        final StringBuilder sb = new StringBuilder();
        for (final PluginParameter p : getParameters()) {
            sb.append(p.getKey()).append('=').append(p.getValue()).append('/');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 107;
        int result = 1;
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_pluginClass == null) ? 0 : m_pluginClass.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof PluginConfig)) return false;
        final PluginConfig other = (PluginConfig) obj;
        if (m_name == null) {
            if (other.m_name != null) return false;
        } else if (!m_name.equals(other.m_name)) {
            return false;
        }
        if (m_pluginClass == null) {
            if (other.m_pluginClass != null) return false;
        } else if (!m_pluginClass.equals(other.m_pluginClass)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) return false;
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PluginConfig [name=" + m_name + ", pluginClass="
                + m_pluginClass + ", parameters=" + getParametersAsString() + "]";
    }


    @Override
    public int compareTo(final PluginConfig other) {
        return new CompareToBuilder()
            .append(m_name, other.m_name)
            .append(m_pluginClass, other.m_pluginClass)
            .toComparison();
    }
}
