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

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 * @hibernate.class table="categories"
 *     
*/
public class OnmsCategory implements Serializable {

    private static final long serialVersionUID = 4694348093332239377L;

    /** identifier field */
    private Integer m_id;
    
    /** persistent field */
    private String m_name;
    
    /** persistent field */
    private String m_description;

    public OnmsCategory(String name, String descr) {
        m_name = name;
        m_description = descr;
    }

    /** default constructor */
    public OnmsCategory() {
    }
    
    public OnmsCategory(String name) {
        this();
        setName(name);
    }

    /** 
     * @hibernate.id generator-class="native" column="categoryId"
     * @hibernate.generator-param name="sequence" value="custCatNxtId"
     */
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    /** 
     * @hibernate.property column="categoryName" unique="true" not-null="true"
     *         
     */
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }

    /** 
     * @hibernate.property column="categoryDescription" unique="false" not-null="false"
     *         
     */
	public String getDescription() {
		return m_description;
	}
	public void setDescription(String description) {
		m_description = description;
	}
	
	//Still trying to figure this out....
//	/**
//	 * @hibernate.set name="cn" lazy="true" cascade="save-update" table="category_node"
//	 * @hibernate.key column="categoryid"
//	 * @hibernate.many-to-many class="org.opennms.model.OnmsNode" column="nodeid"
//	 * @return a <code>Set</code> of OnmsNodes matching the category
//	 */
//	public Set getNodes(OnmsCustomCategory category) {
//		return m_nodes;
//	}

    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("name", getName())
            .append("description", getDescription())
            .toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof OnmsCategory) {
            OnmsCategory t = (OnmsCategory)obj;
            return m_id.equals(t.m_id);
        }
        return false;
    }

    public int hashCode() {
        return m_id.intValue();
    }

}
