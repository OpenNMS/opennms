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

package org.opennms.netmgt.model;

import java.util.Set;

/**
 * This class defines how the AggregateStatus object is to be
 * created and it's properties are to be populated.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AggregateStatusDefinition {
    
    private int m_id;
    private String m_name;
    private String m_reportCategory;
    private Set<OnmsCategory> m_categories;
    
    /**
     * <p>Constructor for AggregateStatusDefinition.</p>
     */
    public AggregateStatusDefinition() {
        
    }
    
    /**
     * <p>Constructor for AggregateStatusDefinition.</p>
     *
     * @param aggrStatus a {@link java.lang.String} object.
     * @param categories a {@link java.util.Set} object.
     */
    public AggregateStatusDefinition(String aggrStatus, Set<OnmsCategory> categories) {
        if (aggrStatus == null || categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        
        m_name = aggrStatus;
        m_categories = categories;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		m_name = name;
	}
    

	/**
	 * <p>getCategories</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<OnmsCategory> getCategories() {
        return m_categories;
    }

    /**
     * <p>setCategories</p>
     *
     * @param categories a {@link java.util.Set} object.
     */
    public void setCategories(Set<OnmsCategory> categories) {
        m_categories = categories;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * <p>getReportCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportCategory() {
        return m_reportCategory;
    }

    /**
     * <p>setReportCategory</p>
     *
     * @param reportCategory a {@link java.lang.String} object.
     */
    public void setReportCategory(String reportCategory) {
        m_reportCategory = reportCategory;
    }

}
