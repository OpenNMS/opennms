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
