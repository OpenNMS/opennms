//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("page:").append(page).append(" ");
        sb.append("items on page:").append(itemsNumberOnPage).append(" ");
        sb.append("max items:").append(max).append(" ");
        return sb.toString();
    }

    private Integer page, max, itemsNumberOnPage;
}
