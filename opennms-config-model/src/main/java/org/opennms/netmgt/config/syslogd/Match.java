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
