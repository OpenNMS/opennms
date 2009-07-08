/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.acl.model;

/**
 * Contains the logic to perform pagination
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
public class Pager {

    /**
     * Constructor
     * 
     * @param page requested
     * @param max number of pages
     * @param itemsNumberOnPage
     */
    public Pager(Integer page, Integer max, Integer itemsNumberOnPage) {
        this.page = page;
        this.max = max;
        this.itemsNumberOnPage = itemsNumberOnPage;
    }

    /**
     * @return The max count of pages
     */
    public Integer getMax() {
        return max;
    }

    /**
     * @return Current number page displayed
     */
    public Integer getPage() {
        return page;
    }

    /**
     * @return The number of items to show on the page
     */
    public Integer getItemsNumberOnPage() {
        return itemsNumberOnPage;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("page:").append(page).append(" ");
        sb.append("items on page:").append(itemsNumberOnPage).append(" ");
        sb.append("max items:").append(max).append(" ");
        return sb.toString();
    }

    private Integer page, max, itemsNumberOnPage;
}
