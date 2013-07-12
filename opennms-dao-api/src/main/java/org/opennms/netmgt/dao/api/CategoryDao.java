/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.opennms.netmgt.model.OnmsCategory;

/**
 * <p>CategoryDao interface.</p>
 */
public interface CategoryDao extends OnmsDao<OnmsCategory, Integer> {
	
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    OnmsCategory findByName(String name);
    
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param useCached a boolean.
     * @return a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    OnmsCategory findByName(String name, boolean useCached);
    
    /**
     * <p>getAllCategoryNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getAllCategoryNames();
    
    /**
     * <p>getCriterionForCategorySetsUnion</p>
     *
     * @param categories an array of {@link java.lang.String} objects.
     * @return a {@link java.util.List} object.
     */
    List<Criterion> getCriterionForCategorySetsUnion(String[]... categories);
    
    /**
     * <p>getCategoriesWithAuthorizedGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsCategory> getCategoriesWithAuthorizedGroup(String groupName);
}
