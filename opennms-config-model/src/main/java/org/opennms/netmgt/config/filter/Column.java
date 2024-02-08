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
package org.opennms.netmgt.config.filter;


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

@XmlRootElement(name = "column")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("database-schema.xsd")
public class Column implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_VISISBLE = "true";

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "sql-type")
    private String m_sqlType;

    @XmlAttribute(name = "java-type")
    private String m_javaType;

    @XmlAttribute(name = "visible")
    private String m_visible;

    @XmlElement(name = "alias")
    private List<Alias> m_aliases = new ArrayList<>();

    @XmlElement(name = "constraint")
    private List<Constraint> m_constraints = new ArrayList<>();

    public Column() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<String> getSqlType() {
        return Optional.ofNullable(m_sqlType);
    }

    public void setSqlType(final String sqlType) {
        m_sqlType = ConfigUtils.normalizeString(sqlType);
    }

    public Optional<String> getJavaType() {
        return Optional.ofNullable(m_javaType);
    }

    public void setJavaType(final String javaType) {
        m_javaType = ConfigUtils.normalizeString(javaType);
    }

    public String getVisible() {
        return m_visible != null ? m_visible : DEFAULT_VISISBLE;
    }

    public void setVisible(final String visible) {
        m_visible = ConfigUtils.normalizeString(visible);
    }

    public List<Alias> getAliases() {
        return m_aliases;
    }

    public void setAliases(final List<Alias> aliases) {
        if (aliases == m_aliases) return;
        m_aliases.clear();
        if (aliases != null) m_aliases.addAll(aliases);
    }

    public void addAlias(final Alias alias) {
        m_aliases.add(alias);
    }

    public boolean removeAlias(final Alias alias) {
        return m_aliases.remove(alias);
    }

    public List<Constraint> getConstraints() {
        return m_constraints;
    }

    public void setConstraints(final List<Constraint> constraints) {
        if (constraints == m_constraints) return;
        m_constraints.clear();
        if (constraints != null) m_constraints.addAll(constraints);
    }

    public void addConstraint(final Constraint constraint) {
        m_constraints.add(constraint);
    }

    public boolean removeConstraint(final Constraint constraint) {
        return m_constraints.remove(constraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_sqlType, 
                            m_javaType, 
                            m_visible, 
                            m_aliases, 
                            m_constraints);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Column) {
            final Column that = (Column)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_sqlType, that.m_sqlType)
                    && Objects.equals(this.m_javaType, that.m_javaType)
                    && Objects.equals(this.m_visible, that.m_visible)
                    && Objects.equals(this.m_aliases, that.m_aliases)
                    && Objects.equals(this.m_constraints, that.m_constraints);
        }
        return false;
    }

}
