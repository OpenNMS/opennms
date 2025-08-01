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
package org.opennms.netmgt.config.surveillanceViews;


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
@ValidateUsing("surveillance-views.xsd")
public class View implements Serializable {
    private static final long serialVersionUID = 3L;

    private static final int DEFAULT_REFRESH_SECONDS = 300;

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "refresh-seconds")
    private Integer m_refreshSeconds;

    @XmlElementWrapper(name = "rows", required = true)
    @XmlElement(name = "row-def", required = true)
    private List<RowDef> m_rows = new ArrayList<>();

    @XmlElementWrapper(name = "columns", required = true)
    @XmlElement(name = "column-def", required = true)
    private List<ColumnDef> m_columns = new ArrayList<>();

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Integer getRefreshSeconds() {
        return m_refreshSeconds != null ? m_refreshSeconds : DEFAULT_REFRESH_SECONDS;
    }

    public void setRefreshSeconds(final Integer refreshSeconds) {
        m_refreshSeconds = refreshSeconds;
    }

    public List<RowDef> getRows() {
        return m_rows;
    }

    public void setRows(final List<RowDef> rows) {
        ConfigUtils.assertMinimumSize(rows, 1, "row-def");
        if (rows == m_rows) return;
        m_rows.clear();
        if (rows != null) m_rows.addAll(rows);
    }

    public void addRow(final RowDef row) {
        m_rows.add(row);
    }

    public void addRow(final String label, final String... categories) {
        m_rows.add(new RowDef(label, categories));
    }

    public boolean removeRow(final RowDef row) {
        return m_rows.remove(row);
    }

    public Optional<RowDef> getRowDef(final String label) {
        return m_rows.stream().filter(row -> {
            return label.equals(row.getLabel());
        }).findFirst();
    }

    public List<ColumnDef> getColumns() {
        return m_columns;
    }

    public void setColumns(final List<ColumnDef> columns) {
        ConfigUtils.assertMinimumSize(columns, 1, "column-def");
        if (columns == m_columns) return;
        m_columns.clear();
        if (columns != null) m_columns.addAll(columns);
    }

    public void addColumn(final ColumnDef column) {
        m_columns.add(column);
    }

    public void addColumn(final String label, final String... categories) {
        m_columns.add(new ColumnDef(label, categories));
    }

    public boolean removeColumn(final ColumnDef column) {
        return m_columns.remove(column);
    }

    public Optional<ColumnDef> getColumnDef(final String label) {
        return m_columns.stream().filter(col -> {
            return label.equals(col.getLabel());
        }).findFirst();
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_refreshSeconds, 
                            m_rows, 
                            m_columns);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof View) {
            final View that = (View)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_refreshSeconds, that.m_refreshSeconds)
                    && Objects.equals(this.m_rows, that.m_rows)
                    && Objects.equals(this.m_columns, that.m_columns);
        }
        return false;
    }
}
