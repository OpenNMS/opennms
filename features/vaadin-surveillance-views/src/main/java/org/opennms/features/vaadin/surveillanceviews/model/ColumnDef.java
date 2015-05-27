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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the column-def element of the surveillance view configuration xml.
 *
 * @author Christian Pape
 */
public class ColumnDef implements Def {
    /**
     * field storing the label
     */
    private java.lang.String m_label = "default";
    /**
     * field for the report-category attribute - this field is
     * deprecated and should not be used anymore!
     */
    private java.lang.String m_reportCategory;
    /**
     * the list of categories associated with this column def
     */
    private List<Category> m_categories = new LinkedList<Category>();

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlAttribute(name = "label", required = true)
    public String getLabel() {
        return m_label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLabel(String label) {
        this.m_label = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlAttribute(name = "report-category", required = false)
    public String getReportCategory() {
        return m_reportCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReportCategory(String reportCategory) {
        this.m_reportCategory = reportCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @XmlElement(name = "category")
    public List<Category> getCategories() {
        return m_categories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getCategoryNames() {
        Set<String> listOfNames = new HashSet<>();
        for (Category category : getCategories()) {
            listOfNames.add(category.getName());
        }
        return listOfNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsCategory(String name) {
        for (Category category : getCategories()) {
            if (name.equals(category.getName())) {
                return true;
            }
        }
        return false;
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

        ColumnDef columnDef = (ColumnDef) o;

        if (m_categories != null ? !m_categories.equals(columnDef.m_categories) : columnDef.m_categories != null) {
            return false;
        }
        if (m_label != null ? !m_label.equals(columnDef.m_label) : columnDef.m_label != null) {
            return false;
        }
        if (m_reportCategory != null ? !m_reportCategory.equals(columnDef.m_reportCategory) : columnDef.m_reportCategory != null) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = m_label != null ? m_label.hashCode() : 0;
        result = 31 * result + (m_reportCategory != null ? m_reportCategory.hashCode() : 0);
        result = 31 * result + (m_categories != null ? m_categories.hashCode() : 0);
        return result;
    }
}
