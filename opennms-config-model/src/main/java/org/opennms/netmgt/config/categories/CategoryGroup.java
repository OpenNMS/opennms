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
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * A category group containing categories. The only parts of
 *  the category group that seem to be used are the common element and the
 *  list of categories.
 */
@XmlRootElement(name = "categorygroup")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("categories.xsd")
public class CategoryGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The name of the category group. This is seemingly
     *  unused.
     */
    @XmlElement(name = "name", required = true)
    private String m_name;

    /**
     * A comment describing the category group. This is
     *  seemingly unused.
     */
    @XmlElement(name = "comment")
    private String m_comment;

    /**
     * Common attributes that apply to all categories in
     *  the group.
     */
    @XmlElement(name = "common", required = true)
    private Common m_common;

    /**
     * The categories belonging to this category
     *  group.
     */
    @XmlElementWrapper(name = "categories")
    @XmlElement(name = "category")
    private List<Category> m_categories = new ArrayList<>();

    public CategoryGroup() {
    }

    public CategoryGroup(final String name) {
        setName(name);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(m_comment);
    }

    public void setComment(final String comment) {
        m_comment = comment;
    }

    public Common getCommon() {
        return m_common;
    }

    public void setCommon(final Common common) {
        if (common == null) {
            m_common = new Common();
        } else {
            m_common = common;
        }
    }

    public void setCommonRule(final String rule) {
        if (m_common == null) {
            m_common = new Common();
        }
        m_common.setRule(rule);
    }

    public List<Category> getCategories() {
        return m_categories;
    }

    public void setCategories(final List<Category> categories) {
        if (categories == m_categories) return;
        m_categories.clear();
        if (categories != null) m_categories.addAll(categories);
    }

    public void addCategory(final Category cat) {
        m_categories.add(cat);
    }

    public boolean removeCategory(final Category cat) {
        return m_categories.remove(cat);
    }

    public void removeCategory(final String label) {
        m_categories.removeIf(cat -> {
            return cat.getLabel().equals(label);
        });
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_name, 
                            m_comment, 
                            m_common, 
                            m_categories);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof CategoryGroup) {
            final CategoryGroup temp = (CategoryGroup)obj;
            return Objects.equals(temp.m_name, m_name)
                    && Objects.equals(temp.m_comment, m_comment)
                    && Objects.equals(temp.m_common, m_common)
                    && Objects.equals(temp.m_categories, m_categories);
        }
        return false;
    }

    @Override
    public String toString() {
        return "CategoryGroup [name=" + m_name + ", comment=" + m_comment
                + ", common=" + m_common + ", categories=" + m_categories
                + "]";
    }

}
