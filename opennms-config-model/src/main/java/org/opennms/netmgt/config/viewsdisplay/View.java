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
