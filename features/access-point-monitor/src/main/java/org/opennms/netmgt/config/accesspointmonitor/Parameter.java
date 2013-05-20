/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.accesspointmonitor;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * <p>
 * Parameter class.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public class Parameter implements Serializable, Comparable<Parameter>, Cloneable {
    private static final long serialVersionUID = -9083835214208208854L;

    @XmlAttribute(name = "key")
    private String m_key;

    @XmlAttribute(name = "value")
    private String m_value;

    public Parameter() {

    }

    public Parameter(Parameter copy) {
        if (copy.m_key != null) {
            m_key = new String(copy.m_key);
        }
        if (copy.m_value != null) {
            m_value = new String(copy.m_value);
        }
    }

    public Parameter(String key, String value) {
        m_key = key;
        m_value = value;
    }

    @XmlTransient
    public String getKey() {
        return m_key;
    }

    public void setKey(String key) {
        m_key = key;
    }

    @XmlTransient
    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }

    @Override
    public int compareTo(Parameter obj) {
        return new CompareToBuilder()
            .append(getKey(), obj.getKey())
            .append(getValue(), obj.getValue())
            .toComparison();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_key == null) ? 0 : m_key.hashCode());
        result = prime * result + ((m_value == null) ? 0 : m_value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter other = (Parameter) obj;
            return new EqualsBuilder()
                .append(getKey(), other.getKey())
                .append(getValue(), other.getValue())
                .isEquals();
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Parameter cloned = new Parameter();
        cloned.m_key = m_key;
        cloned.m_value = m_value;
        return cloned;
    }
}
