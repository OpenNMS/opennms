/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Filter;

import com.google.common.base.MoreObjects;

/**
 * <p>OnmsCategory class.</p>
 */
@XmlRootElement(name = "category")
@Entity
@Table(name="categories")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="categoryid in (select distinct cn.categoryId from category_node cn join category_node cn2 on cn.nodeid = cn2.nodeid join category_group cg on cn2.categoryId = cg.categoryId where cg.groupId in (:userGroups))")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsCategory implements Serializable, Comparable<OnmsCategory> {

    private static final long serialVersionUID = 4694348093332239377L;

    /** identifier field */
    private Integer m_id;
    
    /** persistent field */
    private String m_name;
    
    /** persistent field */
    private String m_description;

    private Set<String> m_authorizedGroups = new HashSet<>();

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
    @Column(name="categoryid", nullable=false)
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
	@ElementCollection
	@JoinTable(name="category_group", joinColumns=@JoinColumn(name="categoryId"))
	@Column(name="groupId", nullable=false, length=64)
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
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", getId())
            .add("name", getName())
            .add("description", getDescription())
            .add("authorizedGroups", getAuthorizedGroups())
            .toString();
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
    public int hashCode() {
        return m_name.hashCode();
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.netmgt.model.OnmsCategory} object.
     * @return a int.
     */
    @Override
    public int compareTo(OnmsCategory o) {
        return m_name.compareToIgnoreCase(o.m_name);
    }

}
