package org.opennms.features.vaadin.surveillanceviews.model;

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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the row-def element of the surveillance view configuration xml
 */
public class RowDef implements Def {

    private java.lang.String m_label = "default";
    private java.lang.String m_reportCategory;
    private List<Category> m_categories = new LinkedList<Category>();

    @Override
    @XmlAttribute(name = "label", required = true)
    public String getLabel() {
        return m_label;
    }

    @Override
    public void setLabel(String label) {
        this.m_label = label;
    }

    @Override
    @XmlAttribute(name = "report-category", required = false)
    public String getReportCategory() {
        return m_reportCategory;
    }

    @Override
    public void setReportCategory(String reportCategory) {
        this.m_reportCategory = reportCategory;
    }

    @Override
    @XmlElement(name = "category")
    public List<Category> getCategories() {
        return m_categories;
    }

    @Override
    public Set<String> getCategoryNames() {
        Set<String> listOfNames = new HashSet<>();
        for(Category category:getCategories()) {
            listOfNames.add(category.getName());
        }
        return listOfNames;
    }

    @Override
    public boolean containsCategory(String name) {
        for (Category category : getCategories()) {
            if (name.equals(category.getName())) {
                return true;
            }
        }
        return false;
    }
}
