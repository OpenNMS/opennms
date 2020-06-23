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

package org.opennms.netmgt.config.siteStatusViews;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "view")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("site-status-views.xsd")
public class View implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_TABLE_NAME = "assets";
    private static final String DEFAULT_COLUMN_NAME = "building";
    private static final String DEFAULT_COLUMN_TYPE = "varchar";

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "table-name")
    private String m_tableName;

    @XmlAttribute(name = "column-name")
    private String m_columnName;

    @XmlAttribute(name = "column-type")
    private String m_columnType;

    @XmlAttribute(name = "column-value")
    private String m_columnValue;

    @XmlElementWrapper(name = "rows", required = true)
    @XmlElement(name = "row-def", required = true)
    private List<RowDef> m_rows = new ArrayList<>();

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getTableName() {
        return m_tableName != null ? m_tableName : DEFAULT_TABLE_NAME;
    }

    public void setTableName(final String tableName) {
        m_tableName = ConfigUtils.normalizeString(tableName);
    }

    public String getColumnName() {
        return m_columnName != null ? m_columnName : DEFAULT_COLUMN_NAME;
    }

    public void setColumnName(final String columnName) {
        m_columnName = ConfigUtils.normalizeString(columnName);
    }

    public String getColumnType() {
        return m_columnType != null ? m_columnType : DEFAULT_COLUMN_TYPE;
    }

    public void setColumnType(final String columnType) {
        m_columnType = ConfigUtils.normalizeString(columnType);
    }

    public Optional<String> getColumnValue() {
        return Optional.ofNullable(m_columnValue);
    }

    public void setColumnValue(final String columnValue) {
        m_columnValue = ConfigUtils.normalizeString(columnValue);
    }

    public List<RowDef> getRows() {
        return m_rows;
    }

    public void setRows(final List<RowDef> rows) {
        if (rows == m_rows) return;
        m_rows.clear();
        if (rows != null) m_rows.addAll(rows);
    }

    public void addRow(final RowDef row) {
        m_rows.add(row);
    }

    public void addRow(final String rowLabel, final String... categories) {
        m_rows.add(new RowDef(rowLabel, categories));
    }

    public boolean removeRow(final RowDef row) {
        return m_rows.remove(row);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_tableName, 
                            m_columnName, 
                            m_columnType, 
                            m_columnValue, 
                            m_rows);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof View) {
            final View that = (View)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_tableName, that.m_tableName)
                    && Objects.equals(this.m_columnName, that.m_columnName)
                    && Objects.equals(this.m_columnType, that.m_columnType)
                    && Objects.equals(this.m_columnValue, that.m_columnValue)
                    && Objects.equals(this.m_rows, that.m_rows);
        }
        return false;
    }

}
