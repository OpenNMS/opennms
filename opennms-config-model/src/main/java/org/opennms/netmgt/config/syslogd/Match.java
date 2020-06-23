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

package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * String against which to match the message body; interpreted
 *  as a substring or a regular expression according to the
 *  value of the "type" attribute
 */
@XmlRootElement(name = "match")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class Match implements Serializable {
    private static final List<String> TYPE_OPTIONS = Arrays.asList("substr", "regex", "regexp");

    private static final long serialVersionUID = 2L;

    /**
     * Whether to interpret this match string as a simple
     *  substring or as a regular expression
     */
    @XmlAttribute(name = "type", required = true)
    private String m_type;

    /**
     * The match expression
     */
    @XmlAttribute(name = "expression", required = true)
    private String m_expression;

    /**
     * Whether to do the default mappings of matching-groups to
     *  event parameters (group 1 -> group1, etc.) if this is a
     *  regex match.
     */
    @XmlAttribute(name = "default-parameter-mapping")
    private Boolean m_defaultParameterMapping;

    public Match() {
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertOnlyContains(ConfigUtils.assertNotEmpty(type, "type"), TYPE_OPTIONS, "type");
    }

    public String getExpression() {
        return m_expression;
    }

    public void setExpression(final String expression) {
        m_expression = ConfigUtils.assertNotEmpty(expression, "expression");
    }

    public Boolean getDefaultParameterMapping() {
        return m_defaultParameterMapping != null ? m_defaultParameterMapping : Boolean.TRUE;
    }

    public void setDefaultParameterMapping(final Boolean defaultParameterMapping) {
        m_defaultParameterMapping = defaultParameterMapping;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_type, m_expression, m_defaultParameterMapping);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Match) {
            final Match that = (Match)obj;
            return Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_expression, that.m_expression)
                    && Objects.equals(this.m_defaultParameterMapping, that.m_defaultParameterMapping);
        }
        return false;
    }

}
