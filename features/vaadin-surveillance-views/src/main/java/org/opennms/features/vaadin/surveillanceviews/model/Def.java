/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.surveillanceviews.model;

import java.util.List;
import java.util.Set;

/**
 * Helper inteface to handle similar column-def/row-def stuff.
 *
 * @author Christian Pape
 */
public interface Def {
    /**
     * Returns the label of this column/row def.
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns the report-category attribute of this column/row def.
     *
     * @return the report-category attribute
     */
    String getReportCategory();

    /**
     * Returns the list of categories used by this column/row def.
     *
     * @return the list of categories
     */
    List<Category> getCategories();

    /**
     * Returns a set of category names used by this column/row def.
     *
     * @return the set of category names
     */
    Set<String> getCategoryNames();

    /**
     * Sets the label of this column/row def
     *
     * @param label the label to be used
     */
    void setLabel(String label);

    /**
     * Sets the report-category attribute of this column/row def
     *
     * @param reportCategory the report-category attribute to be used
     */
    void setReportCategory(String reportCategory);

    /**
     * Checks whether this column/row def uses the given category.
     *
     * @param name the nam eof the category to be checked
     * @return true, if used by this column/row def, false otherwise
     */
    boolean containsCategory(String name);
}
