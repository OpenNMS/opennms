/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.model;

/**
 * Contains the logic to perform pagination
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class Pager {

    /**
     * Constructor
     *
     * @param page requested
     * @param max number of pages
     * @param itemsNumberOnPage a {@link java.lang.Integer} object.
     */
    public Pager(Integer page, Integer max, Integer itemsNumberOnPage) {
        this.page = page;
        this.max = max;
        this.itemsNumberOnPage = itemsNumberOnPage;
    }

    /**
     * <p>Getter for the field <code>max</code>.</p>
     *
     * @return The max count of pages
     */
    public Integer getMax() {
        return max;
    }

    /**
     * <p>Getter for the field <code>page</code>.</p>
     *
     * @return Current number page displayed
     */
    public Integer getPage() {
        return page;
    }

    /**
     * <p>Getter for the field <code>itemsNumberOnPage</code>.</p>
     *
     * @return The number of items to show on the page
     */
    public Integer getItemsNumberOnPage() {
        return itemsNumberOnPage;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("page:").append(page).append(" ");
        sb.append("items on page:").append(itemsNumberOnPage).append(" ");
        sb.append("max items:").append(max).append(" ");
        return sb.toString();
    }

    private Integer page, max, itemsNumberOnPage;
}
