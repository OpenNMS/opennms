/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.translator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * An element representing a value to be used in a
 *  translation. 
 */
@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("translator-configuration.xsd")
public class Value implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "result", required = true)
    private String m_result;

    @XmlAttribute(name = "matches")
    private String m_matches;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    @XmlAttribute(name = "name")
    private String m_name;

    /**
     * An element representing a value to be used in a
     *  translation. 
     */
    @XmlElement(name = "value")
    private List<Value> m_values = new ArrayList<>();

    public void addValue(final Value value) {
        m_values.add(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Value) {
            final Value that = (Value)obj;
            return Objects.equals(this.m_result, that.m_result)
                && Objects.equals(this.m_matches, that.m_matches)
                && Objects.equals(this.m_type, that.m_type)
                && Objects.equals(this.m_name, that.m_name)
                && Objects.equals(this.m_values, that.m_values);
        }
        return false;
    }

    public Optional<String> getMatches() {
        return Optional.ofNullable(m_matches);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(m_name);
    }

    public String getResult() {
        return m_result;
    }

    public String getType() {
        return m_type;
    }

    public List<Value> getValues() {
        return m_values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_result, 
            m_matches, 
            m_type, 
            m_name, 
            m_values);
    }

    public boolean removeValue(final Value value) {
        return m_values.remove(value);
    }

    public void setMatches(final String matches) {
        m_matches = ConfigUtils.normalizeString(matches);
    }

    public void setName(final String name) {
        m_name = ConfigUtils.normalizeString(name);
    }

    public void setResult(final String result) {
        m_result = ConfigUtils.assertNotEmpty(result, "result");
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public void setValue(final List<Value> values) {
        if (values == m_values) return;
        m_values.clear();
        if (values != null) m_values.addAll(values);
    }

}
