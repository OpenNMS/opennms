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

/**
 * The Class OnmsHwEntityAttribute.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "hwEntityAttribute")
@Entity
@Table(name="hwEntityAttribute")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsHwEntityAttribute implements Serializable, Comparable<OnmsHwEntityAttribute> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 468469978315437731L;

    /** The id. */
    private Integer m_id;

    /** The attribute type. */
    private HwEntityAttributeType m_attributeType = new HwEntityAttributeType();

    /** The attribute value. */
    private String m_attributeValue;

    /** The hardware entity. */
    private OnmsHwEntity m_hwEntity;

    /**
     * The Constructor.
     */
    public OnmsHwEntityAttribute() {
    }

    /**
     * The Constructor.
     *
     * @param type the type
     * @param value the value
     */
    public OnmsHwEntityAttribute(HwEntityAttributeType type, String value) {
        super();
        this.m_attributeType = type;
        this.m_attributeValue = value;
    }

    /**
     * Gets the id.
     *
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
     * Sets the id.
     *
     * @param id the id
     */
    public void setId(Integer id) {
        m_id = id;
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
     * Gets the type.
     *
     * @return the type
     */
    @ManyToOne(optional=false)
    @JoinColumn(name="hwAttribTypeId")
    @XmlTransient
    public HwEntityAttributeType getType() {
        return m_attributeType;
    }

    /**
     * Sets the type.
     *
     * @param attributeType the type
     */
    public void setType(HwEntityAttributeType attributeType) {
        this.m_attributeType = attributeType;
    }

    /**
     * Gets the type name.
     *
     * @return the type name
     */
    @Transient
    @XmlAttribute(name="name")
    public String getTypeName() {
        return m_attributeType.getName();
    }

    /**
     * Sets the type name.
     *
     * @param typeName the type name
     */
    public void setTypeName(String typeName) {
        m_attributeType.setName(typeName);
    }

    /**
     * Gets the type OID.
     *
     * @return the type OID
     */
    @Transient
    @XmlAttribute(name="oid")
    public String getTypeOid() {
        return m_attributeType.getOid();
    }

    /**
     * Sets the type OID.
     *
     * @param typeOid the type OID
     */
    public void setTypeOid(String typeOid) {
        m_attributeType.setOid(typeOid);
    }

    /**
     * Gets the type class.
     *
     * @return the type class
     */
    @Transient
    @XmlAttribute(name="class")
    public String getTypeClass() {
        return m_attributeType.getAttributeClass();
    }

    /**
     * Sets the type class.
     *
     * @param typeClass the type class
     */
    public void setTypeClass(String typeClass) {
        m_attributeType.setAttributeClass(typeClass);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Column(name="attribValue")
    @XmlAttribute(name="value")
    public String getValue() {
        return m_attributeValue;
    }

    /**
     * Sets the value.
     *
     * @param attributeValue the value
     */
    public void setValue(String attributeValue) {
        this.m_attributeValue = attributeValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this.getClass().getSimpleName(), ToStringStyle.SHORT_PREFIX_STYLE)
        .append("entPhysicalIndex", m_hwEntity == null ? null : m_hwEntity.getEntPhysicalIndex())
        .append("type", m_attributeType)
        .append("value", m_attributeValue)
        .toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof OnmsHwEntityAttribute) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(OnmsHwEntityAttribute o) {
        return getTypeName().compareTo(o.getTypeName());
    }

}
