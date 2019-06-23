/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
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