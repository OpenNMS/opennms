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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement(name = "entAlias")
@Entity
@Table(name="hwEntityAlias")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsHwEntityAlias implements Serializable, Comparable<OnmsHwEntityAlias> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2863137645849222221L;

    /** The id. */
    private Integer m_id;
    
    /** The entity Alias index. */
    private Integer m_index;

    /** The entity physical index. */
    private String m_oid;

    /** The hardware entity. */
    private OnmsHwEntity m_hwEntity;

    private Integer m_hwEntityId;
    
    /**
     * The Constructor.
     */
    public OnmsHwEntityAlias() {
    }

    /**
     * The Constructor.
     *
     * @param index the alias index
     * @param oid the alias oid 
     */
    public OnmsHwEntityAlias(Integer index, String oid) {
        super();
        this.m_index = index;
        this.m_oid = oid;
    }

    /**
     * @return the id
     */
    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId", allocationSize = 1)
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * @param id the m_id to set
     */
    public void setId(Integer id) {
        this.m_id = id;
    }

    /**
     * Gets the hardware entity.
     *
     * @return the hardware entity
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="hwEntityId")
    @XmlTransient
    public OnmsHwEntity getHwEntity() {
        return m_hwEntity;
    }

    @Transient
    public Integer getHwEntityId() {
        return m_hwEntityId;
    }

    public void setHwEntityId(Integer hwEntityId) {
        this.m_hwEntityId = hwEntityId;
    }

    /**
     * Sets the hardware entity.
     *
     * @param hwEntity the hardware entity
     */
    public void setHwEntity(OnmsHwEntity hwEntity) {
        m_hwEntity = hwEntity;
    }

    /**
     * @return the m_entAliasId
     */
    public Integer getIndex() {
        return m_index;
    }

    /**
     * @param index the index to set
     */
    @XmlAttribute(name="index")
    public void setIndex(Integer index) {
        this.m_index = index;
    }

    /**
     * @return the m_entAliasOid
     */
    @XmlAttribute(name="oid")
    public String getOid() {
        return m_oid;
    }

    /**
     * @param oid the oid to set
     */
    public void setOid(String oid) {
        this.m_oid = oid;
    }

    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(OnmsHwEntityAlias.class.getSimpleName(), ToStringStyle.SHORT_PREFIX_STYLE);
        if (m_hwEntity != null) {
            b.append("entity", m_hwEntity.getEntPhysicalIndex());
        }
        if (m_index != null) {
            b.append("idx", m_index);
        }
        if (m_oid != null) {
            b.append("oid", m_oid);
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof OnmsHwEntityAlias) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int compareTo(OnmsHwEntityAlias o) {
        if (o == null) return -1;
        return toString().compareTo(o.toString());
    }
    
}