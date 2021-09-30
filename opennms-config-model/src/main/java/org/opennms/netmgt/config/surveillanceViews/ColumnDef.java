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

package org.opennms.netmgt.config.surveillanceViews;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "column-def")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("surveillance-views.xsd")
public class ColumnDef implements Def, Serializable {
    private static final long serialVersionUID = 3L;

    @XmlAttribute(name = "label", required = true)
    private String m_label;

    @XmlAttribute(name = "report-category")
    private String m_reportCategory;

    /**
     * This element is used to specify OpenNMS specific categories. Note:
     * currently, these categories are defined in a separate configuration file
     * and are
     *  related directly to monitored services. I have separated out this element
     * so that it can be referenced by other entities (nodes, interfaces, etc.)
     *  however, they will be ignored until the domain model is changed and the
     * service layer is adapted for this behavior.
     *  
     */
    @XmlElement(name = "category", required = true)
    private List<Category> m_categories = new ArrayList<>();

    public ColumnDef() {
    }

    public ColumnDef(final String label, final String... categories) {
        setLabel(label);
        for (final String category : categories) {
            addCategory(category);
        }
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(final String label) {
        m_label = ConfigUtils.assertNotEmpty(label, "label");
    }

    public Optional<String> getReportCategory() {
        return Optional.ofNullable(m_reportCategory);
    }

    public void setReportCategory(final String reportCategory) {
        m_reportCategory = ConfigUtils.normalizeString(reportCategory);
    }

    public List<Category> getCategories() {
        return m_categories;
    }

    public void setCategories(final List<Category> categories) {
        ConfigUtils.assertMinimumSize(categories, 1, "category");
        if (categories == m_categories) return;
        m_categories.clear();
        if (categories != null) m_categories.addAll(categories);
    }

    public void addCategory(final Category vCategory) {
        m_categories.add(vCategory);
    }

    public void addCategory(final String category) {
        m_categories.add(new Category(category));
    }

    public boolean removeCategory(final Category category) {
        return m_categories.remove(category);
    }

    @Override
    public Set<String> getCategoryNames() {
        return getCategories().stream().map(cat -> {
            return cat.getName();
        }).collect(Collectors.toSet());
    }

    @Override
    public boolean containsCategory(final String name) {
        return getCategories().stream().anyMatch(cat -> {
            return name.equals(cat.getName());
        });
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_label, 
                            m_reportCategory, 
                            m_categories);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof ColumnDef) {
            final ColumnDef that = (ColumnDef)obj;
            return Objects.equals(this.m_label, that.m_label)
                    && Objects.equals(this.m_reportCategory, that.m_reportCategory)
                    && Objects.equals(this.m_categories, that.m_categories);
        }
        return false;
    }

}
