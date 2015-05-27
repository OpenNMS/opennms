/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.surveillanceviews.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the view element of the surveillance view configuration xml.
 *
 * @author Christian Pape
 */
@XmlRootElement
public class View {
    /**
     * the row definitions for this view
     */
    private List<RowDef> m_rows = new LinkedList<RowDef>();
    /**
     * the column definitions for this view
     */
    private List<ColumnDef> m_columns = new LinkedList<ColumnDef>();
    /**
     * the name for this view
     */
    private java.lang.String m_name = "untitled";
    /**
     * the refresh interval in seconds
     */
    private java.lang.Integer m_refreshSeconds = 300;

    /**
     * Returns the list of row defs for this view.
     *
     * @return the list of row defs
     */
    @XmlElement(name = "row-def")
    @XmlElementWrapper(name = "rows")
    public List<RowDef> getRows() {
        return m_rows;
    }

    /**
     * Returns the list of column defs for this view.
     *
     * @return the list of column defs
     */
    @XmlElement(name = "column-def")
    @XmlElementWrapper(name = "columns")
    public List<ColumnDef> getColumns() {
        return m_columns;
    }

    /**
     * Returns the name of this view
     *
     * @return the view's name
     */
    @XmlAttribute(name = "name", required = true)
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name for this view.
     *
     * @param name the name to be used
     */
    public void setName(String name) {
        this.m_name = name;
    }

    /**
     * Returns the refresh interval in seconds
     *
     * @return the refresh interval
     */
    @XmlAttribute(name = "refresh-seconds", required = false)
    public int getRefreshSeconds() {
        return m_refreshSeconds;
    }

    /**
     * Sets the refresh interval in seconds
     *
     * @param refreshSeconds the refresh interval to be used
     */
    public void setRefreshSeconds(int refreshSeconds) {
        this.m_refreshSeconds = refreshSeconds;
    }

    /**
     * Returns the row def with the given name.
     *
     * @param label the name to search for
     * @return the row def if found, null otherwise
     */
    public RowDef getRowDef(String label) {
        for (RowDef rowDef : getRows()) {
            if (label.equals(rowDef.getLabel())) {
                return rowDef;
            }
        }
        return null;
    }

    /**
     * Returns the column def with the given name.
     *
     * @param label the name to search for
     * @return the column def if found, null otherwise
     */
    public ColumnDef getColumnDef(String label) {
        for (ColumnDef columnDef : getColumns()) {
            if (label.equals(columnDef.getLabel())) {
                return columnDef;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        View view = (View) o;

        if (m_columns != null ? !m_columns.equals(view.m_columns) : view.m_columns != null) {
            return false;
        }
        if (m_name != null ? !m_name.equals(view.m_name) : view.m_name != null) {
            return false;
        }
        if (m_refreshSeconds != null ? !m_refreshSeconds.equals(view.m_refreshSeconds) : view.m_refreshSeconds != null) {
            return false;
        }
        if (m_rows != null ? !m_rows.equals(view.m_rows) : view.m_rows != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = m_rows != null ? m_rows.hashCode() : 0;
        result = 31 * result + (m_columns != null ? m_columns.hashCode() : 0);
        result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
        result = 31 * result + (m_refreshSeconds != null ? m_refreshSeconds.hashCode() : 0);
        return result;
    }
}
