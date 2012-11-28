/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.web.event.filter;

import org.opennms.web.filter.SubstringFilter;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author jeffg
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeCategoryLikeFilter extends SubstringFilter {
    /** Constant <code>TYPE="nodecategorylike"</code> */
    public static final String TYPE = "nodecategorylike";

    /**
     * <p>Constructor for NodeCategoryFilter.</p>
     *
     * @param substring a {@link java.lang.String} object.
     */
    public NodeCategoryLikeFilter(String substring) {
        super(TYPE, "NODECATEGORY", "node.label", substring);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " NODEID IN (SELECT DISTINCT NODEID FROM CATEGORY_NODE WHERE CATEGORYID IN (SELECT CATEGORYID FROM CATEGORIES WHERE CATEGORYNAME ILIKE %s)) ";
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription() {
        return ("node category containing \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<WebEventRepository.NodeCategoryLikeFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getSubstring</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSubstring() {
        return getValue();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }
    
}
