/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision.persist.foreignsource;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.provision.support.PluginWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "m_key", "m_value" })
public class PluginParameter implements Serializable, Comparable<PluginParameter> {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="key")
    private String m_key = null;

    @XmlAttribute(name="value")
    private String m_value = null;

    @XmlTransient
    private PluginConfig m_parent = null;

    public PluginParameter() {
    }
    
    public PluginParameter(String key, String value) {
        m_key = key;
        m_value = value;
    }

    public PluginParameter(Entry<String, String> e) {
        m_key = e.getKey();
        m_value = e.getValue();
    }

    public PluginParameter(PluginConfig pluginConfig, String key, String value) {
        this(key, value);
        m_parent = pluginConfig;
    }

    public PluginParameter(PluginConfig pluginConfig, Entry<String, String> set) {
        this(set);
        m_parent = pluginConfig;
    }

    public void setPluginConfig(PluginConfig pc) {
        m_parent = pc;
    }

    public String getKey() {
        return m_key;
    }
    public String getValue() {
        return m_value;
    }
    public void setKey(String key) {
        m_key = key;
    }
    public void setValue(String value) {
        m_value = value;
    }

    public Set<String> getAvailableParameterKeys() {
        Set<String> keys = new TreeSet<String>();
        if (m_parent != null) {
            try {
                PluginWrapper pw = new PluginWrapper(m_parent.getPluginClass());
                keys = pw.getOptionalKeys();
                for (PluginParameter p : m_parent.getParameters()) {
                    if (p.getKey() != getKey()) {
                        keys.remove(p.getKey());
                    }
                }
            } catch (ClassNotFoundException e) {
                // we just let it return the empty set
            }
        }
        return keys;
    }

    public int compareTo(PluginParameter obj) {
        return new CompareToBuilder()
            .append(getKey(), obj.getKey())
            .toComparison();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("key", getKey())
            .append("value", getValue())
            .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PluginParameter) {
            PluginParameter other = (PluginParameter) obj;
            return new EqualsBuilder()
                .append(getKey(), other.getKey())
                .append(getValue(), other.getValue())
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(701, 1873)
            .append(getKey())
            .append(getValue())
            .toHashCode();
      }

}
