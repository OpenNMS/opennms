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

@XmlRootElement(name = "view")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("viewsdisplay.xsd")
public class View implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "view-name", required = true)
    private String m_viewName;

    @XmlElement(name = "section", required = true)
    private List<Section> m_sections = new ArrayList<>();

    public View() {
    }

    public String getViewName() {
        return m_viewName;
    }

    public void setViewName(final String viewName) {
        m_viewName = ConfigUtils.assertNotEmpty(viewName, "view-name");
    }

    public List<Section> getSections() {
        return m_sections;
    }

    public void setSections(final List<Section> sections) {
        if (sections == m_sections) return;
        m_sections.clear();
        if (sections != null) m_sections.addAll(sections);
    }

    public void addSection(final Section section) {
        m_sections.add(section);
    }

    public boolean removeSection(final Section section) {
        return m_sections.remove(section);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_viewName, m_sections);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof View) {
            final View that = (View)obj;
            return Objects.equals(this.m_viewName, that.m_viewName)
                    && Objects.equals(this.m_sections, that.m_sections);
        }
        return false;
    }

}
