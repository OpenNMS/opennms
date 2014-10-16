/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * The Class OnmsHwEntity.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "hwEntity")
@Entity
@Table(name="hwEntity")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsHwEntity implements Serializable, Comparable<OnmsHwEntity> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -543872118396806431L;

    /** The id. */
    private Integer m_id;

    /** The entity physical index. */
    private Integer m_entPhysicalIndex;

    /** The entity physical parent relative position. */
    private Integer m_entPhysicalParentRelPos;

    /** The entity physical contained in. */
    private Integer m_entPhysicalContainedIn;

    /** The entity physical name. */
    private String m_entPhysicalName;

    /** The entity physical description. */
    private String m_entPhysicalDescr;

    /** The entity physical alias. */
    private String m_entPhysicalAlias;

    /** The entity physical vendor type. */
    private String m_entPhysicalVendorType;

    /** The entity physical class. */
    private String m_entPhysicalClass;

    /** The entity physical manufacturer name. */
    private String m_entPhysicalMfgName;

    /** The entity physical model name. */
    private String m_entPhysicalModelName;

    /** The entity physical hardware revision. */
    private String m_entPhysicalHardwareRev;

    /** The entity physical firmware revision. */
    private String m_entPhysicalFirmwareRev;

    /** The entity physical software revision. */
    private String m_entPhysicalSoftwareRev;

    /** The entity physical serial number. */
    private String m_entPhysicalSerialNum;

    /** The entity physical asset id. */
    private String m_entPhysicalAssetID;

    /** The entity physical is FRU. */
    private Boolean m_entPhysicalIsFRU;

    /** The entity physical manufactured date. */
    private Date m_entPhysicalMfgDate; // FIXME This is not being used

    /** The entity physical URIs. */
    private String m_entPhysicalUris;

    /** The OpenNMS node. */
    private OnmsNode m_node;

    /** The custom hardware attributes. */
    private SortedSet<OnmsHwEntityAttribute> m_hwAttributes = new TreeSet<OnmsHwEntityAttribute>();

    /** The entity's parent. */
    private OnmsHwEntity m_parent;

    /** The entity's children. */
    private SortedSet<OnmsHwEntity> m_children = new TreeSet<OnmsHwEntity>();

    /**
     * The Constructor.
     */
    public OnmsHwEntity() {
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
     * Gets the entity id.
     *
     * @return the entity id
     */
    @XmlID
    @XmlAttribute(name="entityId")
    @Transient
    public String getOnmsHwEntityId() {
        return getId() == null ? null : getId().toString();
    }

    /**
     * Sets the entity id.
     *
     * @param id the entity id
     */
    public void setOnmsHwEntityId(final String id) {
        setId(Integer.valueOf(id));
    }

    /**
     * Gets the entity physical index.
     *
     * @return the entity physical index
     */
    @Column(nullable=false)
    @XmlAttribute
    public Integer getEntPhysicalIndex() {
        return m_entPhysicalIndex;
    }

    /**
     * Sets the entity physical index.
     *
     * @param entPhysicalIndex the entity physical index
     */
    public void setEntPhysicalIndex(Integer entPhysicalIndex) {
        this.m_entPhysicalIndex = entPhysicalIndex;
    }

    /**
     * Gets the entity physical contained in.
     * <p>This is used only by the ENTITY-MIB parser, it is not required to persist it on the database.</p>
     * 
     * @return the entity physical contained in
     */
    @Transient
    @XmlTransient
    public Integer getEntPhysicalContainedIn() {
        return m_entPhysicalContainedIn;
    }

    /**
     * Sets the entity physical contained in.
     *
     * @param entPhysicalContainedIn the entity physical contained in
     */
    public void setEntPhysicalContainedIn(Integer entPhysicalContainedIn) {
        this.m_entPhysicalContainedIn = entPhysicalContainedIn;
    }

    /**
     * Gets the entity physical description.
     *
     * @return the entity physical description
     */
    @Column
    @XmlElement
    public String getEntPhysicalDescr() {
        return m_entPhysicalDescr;
    }

    /**
     * Sets the entity physical description.
     *
     * @param entPhysicalDescr the entity physical description
     */
    public void setEntPhysicalDescr(String entPhysicalDescr) {
        this.m_entPhysicalDescr = entPhysicalDescr;
    }

    /**
     * Gets the entity physical vendor type.
     *
     * @return the entity physical vendor type
     */
    @Column
    @XmlElement
    public String getEntPhysicalVendorType() {
        return m_entPhysicalVendorType;
    }

    /**
     * Sets the entity physical vendor type.
     *
     * @param entPhysicalVendorType the entity physical vendor type
     */
    public void setEntPhysicalVendorType(String entPhysicalVendorType) {
        this.m_entPhysicalVendorType = entPhysicalVendorType;
    }

    /**
     * Gets the entity physical class.
     *
     * @return the entity physical class
     */
    @Column
    @XmlElement
    public String getEntPhysicalClass() {
        return m_entPhysicalClass;
    }

    /**
     * Sets the entity physical class.
     *
     * @param entPhysicalClass the entity physical class
     */
    public void setEntPhysicalClass(String entPhysicalClass) {
        this.m_entPhysicalClass = entPhysicalClass;
    }

    /**
     * Gets the entity physical parent relative position.
     *
     * @return the entity physical parent relative position
     */
    @Column
    @XmlTransient
    public Integer getEntPhysicalParentRelPos() {
        return m_entPhysicalParentRelPos;
    }

    /**
     * Sets the entity physical parent relative position.
     *
     * @param entPhysicalParentRelPos the entity physical parent relative position
     */
    public void setEntPhysicalParentRelPos(Integer entPhysicalParentRelPos) {
        this.m_entPhysicalParentRelPos = entPhysicalParentRelPos;
    }

    /**
     * Gets the entity physical name.
     *
     * @return the entity physical name
     */
    @Column
    @XmlElement
    public String getEntPhysicalName() {
        return m_entPhysicalName;
    }

    /**
     * Sets the entity physical name.
     *
     * @param entPhysicalName the entity physical name
     */
    public void setEntPhysicalName(String entPhysicalName) {
        this.m_entPhysicalName = entPhysicalName;
    }

    /**
     * Gets the entity physical hardware revision.
     *
     * @return the entity physical hardware revision
     */
    @Column
    @XmlElement
    public String getEntPhysicalHardwareRev() {
        return m_entPhysicalHardwareRev;
    }

    /**
     * Sets the entity physical hardware revision.
     *
     * @param entPhysicalHardwareRev the entity physical hardware revision
     */
    public void setEntPhysicalHardwareRev(String entPhysicalHardwareRev) {
        this.m_entPhysicalHardwareRev = entPhysicalHardwareRev;
    }

    /**
     * Gets the entity physical firmware revision.
     *
     * @return the entity physical firmware revision
     */
    @Column
    @XmlElement
    public String getEntPhysicalFirmwareRev() {
        return m_entPhysicalFirmwareRev;
    }

    /**
     * Sets the entity physical firmware revision.
     *
     * @param entPhysicalFirmwareRev the entity physical firmware revision
     */
    public void setEntPhysicalFirmwareRev(String entPhysicalFirmwareRev) {
        this.m_entPhysicalFirmwareRev = entPhysicalFirmwareRev;
    }

    /**
     * Gets the entity physical software revision.
     *
     * @return the entity physical software revision
     */
    @Column
    @XmlElement
    public String getEntPhysicalSoftwareRev() {
        return m_entPhysicalSoftwareRev;
    }

    /**
     * Sets the entity physical software revision.
     *
     * @param entPhysicalSoftwareRev the entity physical software revision
     */
    public void setEntPhysicalSoftwareRev(String entPhysicalSoftwareRev) {
        this.m_entPhysicalSoftwareRev = entPhysicalSoftwareRev;
    }

    /**
     * Gets the entity physical serial number.
     *
     * @return the entity physical serial number
     */
    @Column
    @XmlElement
    public String getEntPhysicalSerialNum() {
        return m_entPhysicalSerialNum;
    }

    /**
     * Sets the entity physical serial number.
     *
     * @param entPhysicalSerialNum the entity physical serial number
     */
    public void setEntPhysicalSerialNum(String entPhysicalSerialNum) {
        this.m_entPhysicalSerialNum = entPhysicalSerialNum;
    }

    /**
     * Gets the entity physical manufacturer name.
     *
     * @return the entity physical manufacturer name
     */
    @Column
    @XmlElement
    public String getEntPhysicalMfgName() {
        return m_entPhysicalMfgName;
    }

    /**
     * Sets the entity physical manufacturer name.
     *
     * @param entPhysicalMfgName the entity physical manufacturer name
     */
    public void setEntPhysicalMfgName(String entPhysicalMfgName) {
        this.m_entPhysicalMfgName = entPhysicalMfgName;
    }

    /**
     * Gets the entity physical model name.
     *
     * @return the entity physical model name
     */
    @Column
    @XmlElement
    public String getEntPhysicalModelName() {
        return m_entPhysicalModelName;
    }

    /**
     * Sets the entity physical model name.
     *
     * @param entPhysicalModelName the entity physical model name
     */
    public void setEntPhysicalModelName(String entPhysicalModelName) {
        this.m_entPhysicalModelName = entPhysicalModelName;
    }

    /**
     * Gets the entity physical alias.
     *
     * @return the entity physical alias
     */
    @Column
    @XmlElement
    public String getEntPhysicalAlias() {
        return m_entPhysicalAlias;
    }

    /**
     * Sets the entity physical alias.
     *
     * @param entPhysicalAlias the entity physical alias
     */
    public void setEntPhysicalAlias(String entPhysicalAlias) {
        this.m_entPhysicalAlias = entPhysicalAlias;
    }

    /**
     * Gets the entity physical asset id.
     *
     * @return the entity physical asset id
     */
    @Column
    @XmlElement
    public String getEntPhysicalAssetID() {
        return m_entPhysicalAssetID;
    }

    /**
     * Sets the entity physical asset id.
     *
     * @param entPhysicalAssetID the entity physical asset id
     */
    public void setEntPhysicalAssetID(String entPhysicalAssetID) {
        this.m_entPhysicalAssetID = entPhysicalAssetID;
    }

    /**
     * Gets the entity physical is FRU.
     *
     * @return the entity physical is FRU
     */
    @Column
    @XmlElement
    public Boolean getEntPhysicalIsFRU() {
        return m_entPhysicalIsFRU;
    }

    /**
     * Sets the entity physical is FRU.
     *
     * @param entPhysicalIsFRU the entity physical is FRU
     */
    public void setEntPhysicalIsFRU(Boolean entPhysicalIsFRU) {
        this.m_entPhysicalIsFRU = entPhysicalIsFRU;
    }

    /**
     * Gets the entity physical manufactured date.
     *
     * @return the entity physical manufactured date
     */
    @Column
    @XmlElement
    public Date getEntPhysicalMfgDate() {
        return m_entPhysicalMfgDate;
    }

    /**
     * Sets the entity physical manufactured date.
     *
     * @param entPhysicalMfgDate the entity physical manufactured date
     */
    public void setEntPhysicalMfgDate(Date entPhysicalMfgDate) {
        this.m_entPhysicalMfgDate = entPhysicalMfgDate;
    }

    /**
     * Gets the entity physical URIs.
     *
     * @return the entity physical URIs
     */
    @Column
    @XmlElement
    public String getEntPhysicalUris() {
        return m_entPhysicalUris;
    }

    /**
     * Sets the entity physical URIs.
     *
     * @param entPhysicalUris the entity physical URIs
     */
    public void setEntPhysicalUris(String entPhysicalUris) {
        this.m_entPhysicalUris = entPhysicalUris;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    @XmlTransient
    @ManyToOne(cascade={CascadeType.ALL}, optional=true)
    @JoinColumn(name="parentId")
    public OnmsHwEntity getParent() {
        return m_parent;
    }

    /**
     * Sets the parent.
     *
     * @param parent the parent
     */
    public void setParent(OnmsHwEntity parent) {
        this.m_parent = parent;
    }

    /**
     * Gets the parent id.
     *
     * @return the parent id
     */
    @Transient
    @XmlAttribute(name="parentPhysicalIndex")
    public Integer getParentIndex() {
        return m_parent == null ? null : m_parent.getEntPhysicalIndex();
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    @XmlElement(name="hwEntity")
    @XmlElementWrapper(name="children")
    @Sort(type = SortType.NATURAL, comparator = OnmsHwEntity.class)
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
    public SortedSet<OnmsHwEntity> getChildren() {
        return m_children;
    }

    /**
     * Sets the children.
     *
     * @param children the children
     */
    public void setChildren(SortedSet<OnmsHwEntity> children) {
        if (children != null) this.m_children = children;
    }

    /**
     * Adds the child entity.
     *
     * @param child the child
     */
    public void addChildEntity(OnmsHwEntity child) {
        child.setParent(this);
        getChildren().add(child);        
    }

    /**
     * Gets the child by index.
     *
     * @param entPhysicalIndex the entity physical index
     * @return the child by index
     */
    public OnmsHwEntity getChildByIndex(Integer entPhysicalIndex) {
        for (OnmsHwEntity child : m_children) {
            if (child.getEntPhysicalIndex() == entPhysicalIndex) {
                return child;
            }
        }
        return null;
    }

    /**
     * Removes the child by index.
     *
     * @param entPhysicalIndex the entity physical index
     */
    public void removeChild(OnmsHwEntity child) {
        if (m_children != null) m_children.remove(child);
    }

    /**
     * Gets the node.
     *
     * @return the node
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlAttribute(name="nodeId")
    @XmlJavaTypeAdapter(NodeIdAdapter.class)
    public OnmsNode getNode() {
        return m_node;
    }

    /**
     * Sets the node.
     *
     * @param node the node
     */
    public void setNode(OnmsNode node) {
        if (node == null) return;
        m_node = node;
        if (m_children == null) return;
        for (OnmsHwEntity child : m_children) {
            child.setNode(node);
        }
    }

    /**
     * Gets the hardware entity attributes.
     *
     * @return the hardware entity attributes
     */
    @OneToMany(mappedBy="hwEntity", fetch=FetchType.LAZY, cascade={CascadeType.ALL}, orphanRemoval=true)
    @Sort(type = SortType.NATURAL)
    @XmlElement(name="hwEntityAttribute")
    @XmlElementWrapper(name="vendorAttributes")
    public SortedSet<OnmsHwEntityAttribute> getHwEntityAttributes() {
        return m_hwAttributes;
    }

    /**
     * Sets the hardware entity attributes.
     *
     * @param hwAttributes the hardware entity attributes
     */
    public void setHwEntityAttributes(SortedSet<OnmsHwEntityAttribute> hwAttributes) {
        if (hwAttributes != null) m_hwAttributes = hwAttributes;
    }

    /**
     * Adds the attribute.
     *
     * @param type the type
     * @param value the value
     */
    public void addAttribute(HwEntityAttributeType type, String value) {
        OnmsHwEntityAttribute attr = new OnmsHwEntityAttribute(type, value);
        attr.setHwEntity(this);
        m_hwAttributes.add(attr);
    }

    /**
     * Gets the attribute.
     *
     * @param typeName the type name
     * @return the attribute
     */
    public OnmsHwEntityAttribute getAttribute(String typeName) {
        for (OnmsHwEntityAttribute attr : m_hwAttributes) {
            if (attr.getTypeName().equals(typeName)) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Gets the attribute value.
     *
     * @param typeName the type name
     * @return the attribute value
     */
    public String getAttributeValue(String typeName) {
        final OnmsHwEntityAttribute attr = getAttribute(typeName);
        return attr == null ? null : attr.getValue();
    }

    /**
     * Gets the attribute class.
     *
     * @param typeName the type name
     * @return the attribute class
     */
    public String getAttributeClass(String typeName) {
        final OnmsHwEntityAttribute attr = getAttribute(typeName);
        return attr == null ? null : attr.getType().getAttributeClass();
    }

    /**
     * Checks if is root.
     *
     * @return true, if checks if is root
     */
    @Transient
    public boolean isRoot() {
        return m_parent == null || m_entPhysicalIndex == 0;
    }

    /**
     * Checks for children.
     *
     * @return true, if checks for children
     */
    @Transient
    public boolean hasChildren() {
        return !m_hwAttributes.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this.getClass().getSimpleName(), ToStringStyle.SHORT_PREFIX_STYLE);
        if (m_node != null)
            b.append("nodeId", m_node.getId());
        if (getParentIndex() != null)
            b.append("parentPhysicalIndex", getParentIndex());
        if (m_entPhysicalIndex != null)
            b.append("entPhysicalIndex", m_entPhysicalIndex);
        if (m_entPhysicalName != null)
            b.append("entPhysicalName", m_entPhysicalName);
        if (m_entPhysicalDescr != null)
            b.append("entPhysicalDescr", m_entPhysicalDescr);
        if (m_entPhysicalAlias != null)
            b.append("entPhysicalAlias", m_entPhysicalAlias);
        if (m_entPhysicalVendorType != null)
            b.append("entPhysicalVendorType", m_entPhysicalVendorType);
        if (m_entPhysicalClass != null)
            b.append("entPhysicalClass", m_entPhysicalClass);
        if (m_entPhysicalMfgName != null)
            b.append("entPhysicalMfgName", m_entPhysicalMfgName);
        if (m_entPhysicalModelName != null)
            b.append("entPhysicalModelName", m_entPhysicalModelName);
        if (m_entPhysicalHardwareRev != null)
            b.append("entPhysicalHardwareRev", m_entPhysicalHardwareRev);
        if (m_entPhysicalFirmwareRev != null)
            b.append("entPhysicalFirmwareRev", m_entPhysicalFirmwareRev);
        if (m_entPhysicalSoftwareRev != null)
            b.append("entPhysicalSoftwareRev", m_entPhysicalSoftwareRev);
        if (m_entPhysicalSerialNum != null)
            b.append("entPhysicalSerialNum", m_entPhysicalSerialNum);
        if (!m_hwAttributes.isEmpty())
            b.append("vendorAttributes", m_hwAttributes.toString());
        if (!m_children.isEmpty())
            b.append("children", m_children.toString());
        return b.toString();
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
        if (obj instanceof OnmsHwEntity) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(OnmsHwEntity o) {
        if (o == null) return -1;
        return toString().compareTo(o.toString());
    }

    /**
     * Fix relationships.
     * 
     * When a node is created from a XML, the internal relationships may not be correct.
     * Prior storing an hardware object into the database, this method most be called to
     * ensure that the DB relationships are correct.
     */
    public void fixRelationships() {
        for (OnmsHwEntityAttribute attrib : m_hwAttributes) {
            attrib.setHwEntity(this);
        }
        if (m_children == null) return;
        for (OnmsHwEntity child : m_children) {
            child.setParent(this);
            child.fixRelationships();
        }
    }
}
