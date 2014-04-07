/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Category, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Category, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Category, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsCategoryCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "categories")
public class OnmsCategoryCollection implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name="category")
    private List<OnmsCategory> m_categories = new ArrayList<OnmsCategory>();
    private Integer m_totalCount;

    public OnmsCategoryCollection() {}
    public OnmsCategoryCollection(final Collection<? extends OnmsCategory> categories) {
        m_categories.addAll(categories);
    }

    public List<OnmsCategory> getCategories() {
        return m_categories;
    }
    public void setCategories(final List<OnmsCategory> categories) {
        if (categories == m_categories) return;
        m_categories.clear();
        m_categories.addAll(categories);
    }

    public void add(final OnmsCategory category) {
        m_categories.add(category);
    }
    public void addAll(final Collection<OnmsCategory> categories) {
        m_categories.addAll(categories);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
        if (m_categories.size() == 0) {
            return null;
        } else {
            return m_categories.size();
        }
    }
    public void setCount(final Integer count) {
        // dummy to make JAXB happy
    }
    public int size() {
        return m_categories.size();
    }
    
    @XmlAttribute(name="totalCount")
    public Integer getTotalCount() {
        return m_totalCount == null? getCount() : m_totalCount;
    }
    public void setTotalCount(final Integer totalCount) {
        m_totalCount = totalCount;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_categories == null) ? 0 : m_categories.hashCode());
        result = prime * result + ((m_totalCount == null) ? 0 : m_totalCount.hashCode());
        return result;
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OnmsCategoryCollection)) {
            return false;
        }
        final OnmsCategoryCollection other = (OnmsCategoryCollection) obj;
        if (m_categories == null) {
            if (other.m_categories != null) {
                return false;
            }
        } else if (!m_categories.equals(other.m_categories)) {
            return false;
        }
        if (getTotalCount() == null) {
            if (other.getTotalCount() != null) {
                return false;
            }
        } else if (!getTotalCount().equals(other.getTotalCount())) {
            return false;
        }
        return true;
    }
}
