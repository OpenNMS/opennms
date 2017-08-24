/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.viewsdisplay;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "section")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("viewsdisplay.xsd")
public class Section implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "section-name", required = true)
    private String m_sectionName;

    @XmlElement(name = "category", required = true)
    private List<String> m_categories = new ArrayList<>();

    public Section() {
    }

    public String getSectionName() {
        return m_sectionName;
    }

    public void setSectionName(final String sectionName) {
        m_sectionName = ConfigUtils.assertNotEmpty(sectionName, "section-name");
    }

    public List<String> getCategories() {
        return m_categories;
    }

    public void setCategories(final List<String> categories) {
        if (categories == m_categories) return;
        m_categories.clear();
        if (categories != null) m_categories.addAll(categories);
    }

    public void addCategory(final String category) {
        m_categories.add(category);
    }

    public boolean removeCategory(final String category) {
        return m_categories.remove(category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_sectionName, m_categories);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Section) {
            final Section that = (Section)obj;
            return Objects.equals(this.m_sectionName, that.m_sectionName)
                    && Objects.equals(this.m_categories, that.m_categories);
        }
        return false;
    }

}
