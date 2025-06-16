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
package org.opennms.netmgt.config.categories;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;

@XmlRootElement(name = "catinfo")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("categories.xsd")
public class Catinfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "header", required = true)
    private Header m_header;

    @XmlElement(name = "categorygroup", required = true)
    private List<CategoryGroup> m_categoryGroups = new ArrayList<>();

    public Catinfo() {
    }

    public Catinfo(final String rev, final String created, final String mstation) {
        m_header = new Header(rev, created, mstation);
    }

    public Header getHeader() {
        return m_header;
    }

    public void setHeader(final Header header) {
        m_header = header;
    }

    public List<CategoryGroup> getCategoryGroups() {
        return m_categoryGroups;
    }

    public void setCategoryGroups(final List<CategoryGroup> groups) {
        if (groups == m_categoryGroups) return;
        m_categoryGroups.clear();
        if (groups != null) m_categoryGroups.addAll(groups);
    }

    public void addCategoryGroup(final CategoryGroup group) throws IndexOutOfBoundsException {
        m_categoryGroups.add(group);
    }

    public boolean removeCategoryGroup(final CategoryGroup vCategorygroup) {
        return m_categoryGroups.remove(vCategorygroup);
    }

    public boolean removeCategoryGroup(final String groupname) {
        return m_categoryGroups.removeIf(cg -> {
            return cg.getName().equals(groupname);
        });
    }

    public void clearCategoryGroups() {
        m_categoryGroups.clear();
    }

    public void replaceCategoryInGroup(final String groupname, final Category cat) {
        m_categoryGroups.forEach(cg -> {
            if (cg.getName().equals(groupname)) {
                cg.getCategories().replaceAll(c -> {
                    if (c.getLabel().equals(cat.getLabel())) {
                        return cat;
                    }
                    return c;
                });
            }
        });        
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_header, 
                            m_categoryGroups);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Catinfo) {
            final Catinfo temp = (Catinfo)obj;
            return Objects.equals(temp.m_header, m_header)
                    && Objects.equals(temp.m_categoryGroups, m_categoryGroups);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Catinfo [header=" + m_header + ", categoryGroup="
                + m_categoryGroups + "]";
    }

}
