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
