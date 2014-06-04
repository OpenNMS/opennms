/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

import java.util.Collection;
import java.util.LinkedList;
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
public class OnmsCategoryCollection extends LinkedList<OnmsCategory> {

    private static final long serialVersionUID = 4731486422555152257L;

    /**
     * <p>Constructor for OnmsCategoryCollection.</p>
     */
    public OnmsCategoryCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsCategoryCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsCategoryCollection(Collection<? extends OnmsCategory> c) {
        super(c);
    }

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name = "category")
    public List<OnmsCategory> getCategories() {
        return this;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<OnmsCategory> categories) {
        if (categories == this) return;
        clear();
        addAll(categories);
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public Integer getCount() {
        return this.size();
    }

}
