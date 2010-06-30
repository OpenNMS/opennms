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
// Modifications:
//
// 2007 Apr 05: Organized imports. - dj@opennms.org
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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.CollectionOfElements;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>OnmsCategory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name = "category")
@Entity
@Table(name="categories")
public class OnmsCategory implements Serializable, Comparable<OnmsCategory> {

    private static final long serialVersionUID = 4694348093332239377L;

    /** identifier field */
    private Integer m_id;
    
    /** persistent field */
    private String m_name;
    
    /** persistent field */
    private String m_description;

    private Set<String> m_authorizedGroups = new HashSet<String>();

    //private Set<OnmsNode> m_memberNodes;

    /**
     * <p>Constructor for OnmsCategory.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param descr a {@link java.lang.String} object.
     */
    public OnmsCategory(String name, String descr) {
        m_name = name;
        m_description = descr;
    }

    /**
     * default constructor
     */
    public OnmsCategory() {
    }
    
    /**
     * <p>Constructor for OnmsCategory.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public OnmsCategory(String name) {
        this();
        setName(name);
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @XmlAttribute(name="id")
    @Column(name="categoryid")
    @SequenceGenerator(name="categorySequence", sequenceName="catNxtId")
    @GeneratedValue(generator="categorySequence")
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
    @XmlAttribute(name="name")
    @Column(name="categoryName", unique=true, nullable=false)
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
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="description")
    @Column(name="categoryDescription")
	public String getDescription() {
		return m_description;
	}
	/**
	 * <p>setDescription</p>
	 *
	 * @param description a {@link java.lang.String} object.
	 */
	public void setDescription(String description) {
		m_description = description;
	}
	
	/**
	 * <p>getAuthorizedGroups</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	@CollectionOfElements
	@JoinTable(name="category_group",
	           joinColumns=@JoinColumn(name="categoryId")
	    )
	@Column(name="groupId", nullable=false)
	public Set<String> getAuthorizedGroups() {
	    return m_authorizedGroups;
	}
	
	/**
	 * <p>setAuthorizedGroups</p>
	 *
	 * @param authorizedGroups a {@link java.util.Set} object.
	 */
	public void setAuthorizedGroups(Set<String> authorizedGroups) {
	    m_authorizedGroups = authorizedGroups;
	}
	
        /*
    @ManyToMany(mappedBy="categories")
    public Set<OnmsNode> getMemberNodes() {
        return m_memberNodes;
    }

    public void setMemberNodes(Set<OnmsNode> memberNodes) {
        m_memberNodes = memberNodes;
    }
    */

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("name", getName())
            .append("description", getDescription())
            .toString();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof OnmsCategory) {
            OnmsCategory t = (OnmsCategory)obj;
            return m_name.equals(t.m_name);
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_name.hashCode();
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsCategory} object.
     * @return a int.
     */
    public int compareTo(OnmsCategory o) {
        return m_name.compareToIgnoreCase(o.m_name);
    }

}
