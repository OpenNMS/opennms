//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.model;

import java.util.Collection;

/**
 * This class defines how the AggregateStatus object is to be
 * created and it's properties are to be populated.
 * 
 * @author david
 *
 */
public class AggregateStatusDefinition {
    
    private String m_aggrStatusLabel;
    private Collection<String> m_categories;
    private int m_id;
    private String m_name;
    
    public AggregateStatusDefinition() {
        
    }
    
    public AggregateStatusDefinition(String aggrStatus, Collection<String> categories) {
        if (aggrStatus == null || categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        
        m_aggrStatusLabel = aggrStatus;
        m_categories = categories;
    }

    public String getAggrStatusLabel() {
        return m_aggrStatusLabel;
    }

    public void setAggrStatusLabel(String aggrStatusLabel) {
        m_aggrStatusLabel = aggrStatusLabel;
    }

    public Collection<String> getCategories() {
        return m_categories;
    }

    public void setCategories(Collection<String> categories) {
        m_categories = categories;
    }
    
    @Override
    public String toString() {
        return m_aggrStatusLabel;
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
    
}
