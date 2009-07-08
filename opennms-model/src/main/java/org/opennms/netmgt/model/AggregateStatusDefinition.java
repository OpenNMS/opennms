/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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


package org.opennms.netmgt.model;

import java.util.Set;

/**
 * This class defines how the AggregateStatus object is to be
 * created and it's properties are to be populated.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class AggregateStatusDefinition {
    
    private int m_id;
    private String m_name;
    private String m_reportCategory;
    private Set<OnmsCategory> m_categories;
    
    public AggregateStatusDefinition() {
        
    }
    
    public AggregateStatusDefinition(String aggrStatus, Set<OnmsCategory> categories) {
        if (aggrStatus == null || categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        
        m_name = aggrStatus;
        m_categories = categories;
    }

    public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        m_id = id;
    }

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
    

	public Set<OnmsCategory> getCategories() {
        return m_categories;
    }

    public void setCategories(Set<OnmsCategory> categories) {
        m_categories = categories;
    }
    
    public String toString() {
        return getName();
    }

    public String getReportCategory() {
        return m_reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        m_reportCategory = reportCategory;
    }

}
