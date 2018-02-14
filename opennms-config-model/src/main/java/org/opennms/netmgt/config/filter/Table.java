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

package org.opennms.netmgt.config.filter;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "table")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("database-schema.xsd")
public class Table implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_VISIBLE = "true";
    private static final String DEFAULT_KEY = "secondary";

    @XmlAttribute(name = "visible")
    private String m_visible;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "key")
    private String m_key;

    @XmlElement(name = "join")
    private List<Join> m_joins = new ArrayList<>();

    @XmlElement(name = "column", required = true)
    private List<Column> m_columns = new ArrayList<>();

    public Table() {
    }

    public String getVisible() {
        return m_visible != null ? m_visible : DEFAULT_VISIBLE;
    }

    public void setVisible(final String visible) {
        m_visible = ConfigUtils.normalizeString(visible);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getKey() {
        return m_key != null ? m_key : DEFAULT_KEY;
    }

    public void setKey(final String key) {
        m_key = ConfigUtils.normalizeString(key);
    }

    public List<Join> getJoins() {
        return m_joins;
    }

    public void setJoins(final List<Join> joins) {
        if (joins == m_joins) return;
        m_joins.clear();
        if (joins != null) m_joins.addAll(joins);
    }

    public void addJoin(final Join join) {
        m_joins.add(join);
    }

    public boolean removeJoin(final Join join) {
        return m_joins.remove(join);
    }

    public List<Column> getColumns() {
        return m_columns;
    }

    public void setColumns(final List<Column> columns) {
        if (columns == m_columns) return;
        m_columns.clear();
        if (columns != null) m_columns.addAll(columns);
    }

    public void addColumn(final Column column) {
        m_columns.add(column);
    }

    public boolean removeColumn(final Column column) {
        return m_columns.remove(column);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(
                                m_visible, 
                                m_name, 
                                m_key, 
                                m_joins, 
                                m_columns);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Table) {
            final Table that = (Table)obj;
            return Objects.equals(this.m_visible, that.m_visible)
                    && Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_key, that.m_key)
                    && Objects.equals(this.m_joins, that.m_joins)
                    && Objects.equals(this.m_columns, that.m_columns);
        }
        return false;
    }

}
