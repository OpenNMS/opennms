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
 * <p>CategoryNodeONMSDTO class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
public class CategoryNodeONMSDTO {

    /**
     * <p>Getter for the field <code>categoryId</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getCategoryId() {
        return categoryId;
    }

    /**
     * <p>Setter for the field <code>categoryId</code>.</p>
     *
     * @param categoryId a {@link java.lang.Integer} object.
     */
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * <p>Getter for the field <code>categoryName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * <p>Setter for the field <code>categoryName</code>.</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * <p>Getter for the field <code>categoryDescription</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategoryDescription() {
        return categoryDescription;
    }

    /**
     * <p>Setter for the field <code>categoryDescription</code>.</p>
     *
     * @param categoryDescription a {@link java.lang.String} object.
     */
    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof CategoryNodeONMSDTO))
            return false;
        CategoryNodeONMSDTO category = (CategoryNodeONMSDTO) o;
        return (categoryName.equalsIgnoreCase(category.getCategoryName()) && categoryDescription.equalsIgnoreCase(category.getCategoryDescription()) && categoryId == category.categoryId);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 18;
            result = 31 * result + categoryName.hashCode();
            result = 31 * result + categoryDescription.hashCode();
            result = 31 * result + categoryId.hashCode();
            hashCode = result;
        }
        return result;
    }

    private Integer categoryId;
    private String categoryName, categoryDescription;
    private volatile int hashCode;
}
