/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "hwEntity")
@Entity
@Table(name="hwEntity")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsHwEntity implements Serializable, Comparable<OnmsHwEntity> {
    private static final Logger LOG = LoggerFactory.getLogger(OnmsHwEntity.class);

    private static final long serialVersionUID = -543872118396806431L;

    private Integer m_id;

    private Integer m_entPhysicalIndex;
    private Integer m_entPhysicalParentRelPos; // FIXME This can be removed
    private Integer m_entPhysicalContainedIn;
    private String m_entPhysicalName;
    private String m_entPhysicalDescr;
    private String m_entPhysicalAlias;
    private String m_entPhysicalVendorType;
    private String m_entPhysicalClass;
    private String m_entPhysicalMfgName;
    private String m_entPhysicalModelName;
    private String m_entPhysicalHardwareRev;
    private String m_entPhysicalFirmwareRev;
    private String m_entPhysicalSoftwareRev;
    private String m_entPhysicalSerialNum;

    private String m_entPhysicalAssetID;
    private Boolean m_entPhysicalIsFRU;
    private Date m_entPhysicalMfgDate;
    private String m_entPhysicalUris;

    private OnmsNode m_node;

    private List<OnmsHwEntityAttribute> m_hwAttributes = new ArrayList<OnmsHwEntityAttribute>();

    private OnmsHwEntity m_parent;

    private List<OnmsHwEntity> m_children = new ArrayList<OnmsHwEntity>();

    public OnmsHwEntity() {
    }

    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    @XmlID
    @XmlAttribute(name="entityId")
    @Transient
    public String getOnmsHwEntityId() {
        return getId() == null ? null : getId().toString();
    }

    public void setOnmsHwEntityId(final String id) {
        setId(Integer.valueOf(id));
    }

    @Column(nullable=false)
    @XmlAttribute
    public Integer getEntPhysicalIndex() {
        return m_entPhysicalIndex;
    }

    public void setEntPhysicalIndex(Integer entPhysicalIndex) {
        this.m_entPhysicalIndex = entPhysicalIndex;
    }

    @Transient
    @XmlAttribute(name="parentId")
    public Integer getEntPhysicalContainedIn() {
        return m_entPhysicalContainedIn;
    }
    
    public void setEntPhysicalContainedIn(Integer entPhysicalContainedIn) {
        this.m_entPhysicalContainedIn = entPhysicalContainedIn;
    }

    @Column
    @XmlElement
    public String getEntPhysicalDescr() {
        return m_entPhysicalDescr;
    }

    public void setEntPhysicalDescr(String entPhysicalDescr) {
        this.m_entPhysicalDescr = entPhysicalDescr;
    }

    @Column
    @XmlElement
    public String getEntPhysicalVendorType() {
        return m_entPhysicalVendorType;
    }

    public void setEntPhysicalVendorType(String entPhysicalVendorType) {
        this.m_entPhysicalVendorType = entPhysicalVendorType;
    }

    @Column
    @XmlElement
    public String getEntPhysicalClass() {
        return m_entPhysicalClass;
    }

    public void setEntPhysicalClass(String entPhysicalClass) {
        this.m_entPhysicalClass = entPhysicalClass;
    }

    @Column
    @XmlElement
    public Integer getEntPhysicalParentRelPos() {
        return m_entPhysicalParentRelPos;
    }

    public void setEntPhysicalParentRelPos(Integer entPhysicalParentRelPos) {
        this.m_entPhysicalParentRelPos = entPhysicalParentRelPos;
    }

    @Column
    @XmlElement
    public String getEntPhysicalName() {
        return m_entPhysicalName;
    }

    public void setEntPhysicalName(String entPhysicalName) {
        this.m_entPhysicalName = entPhysicalName;
    }

    @Column
    @XmlElement
    public String getEntPhysicalHardwareRev() {
        return m_entPhysicalHardwareRev;
    }

    public void setEntPhysicalHardwareRev(String entPhysicalHardwareRev) {
        this.m_entPhysicalHardwareRev = entPhysicalHardwareRev;
    }

    @Column
    @XmlElement
    public String getEntPhysicalFirmwareRev() {
        return m_entPhysicalFirmwareRev;
    }

    public void setEntPhysicalFirmwareRev(String entPhysicalFirmwareRev) {
        this.m_entPhysicalFirmwareRev = entPhysicalFirmwareRev;
    }

    @Column
    @XmlElement
    public String getEntPhysicalSoftwareRev() {
        return m_entPhysicalSoftwareRev;
    }

    public void setEntPhysicalSoftwareRev(String entPhysicalSoftwareRev) {
        this.m_entPhysicalSoftwareRev = entPhysicalSoftwareRev;
    }

    @Column
    @XmlElement
    public String getEntPhysicalSerialNum() {
        return m_entPhysicalSerialNum;
    }

    public void setEntPhysicalSerialNum(String entPhysicalSerialNum) {
        this.m_entPhysicalSerialNum = entPhysicalSerialNum;
    }

    @Column
    @XmlElement
    public String getEntPhysicalMfgName() {
        return m_entPhysicalMfgName;
    }

    public void setEntPhysicalMfgName(String entPhysicalMfgName) {
        this.m_entPhysicalMfgName = entPhysicalMfgName;
    }

    @Column
    @XmlElement
    public String getEntPhysicalModelName() {
        return m_entPhysicalModelName;
    }

    public void setEntPhysicalModelName(String entPhysicalModelName) {
        this.m_entPhysicalModelName = entPhysicalModelName;
    }

    @Column
    @XmlElement
    public String getEntPhysicalAlias() {
        return m_entPhysicalAlias;
    }

    public void setEntPhysicalAlias(String entPhysicalAlias) {
        this.m_entPhysicalAlias = entPhysicalAlias;
    }

    @Column
    @XmlElement
    public String getEntPhysicalAssetID() {
        return m_entPhysicalAssetID;
    }

    public void setEntPhysicalAssetID(String entPhysicalAssetID) {
        this.m_entPhysicalAssetID = entPhysicalAssetID;
    }

    @Column
    @XmlElement
    public Boolean getEntPhysicalIsFRU() {
        return m_entPhysicalIsFRU;
    }

    public void setEntPhysicalIsFRU(Boolean entPhysicalIsFRU) {
        this.m_entPhysicalIsFRU = entPhysicalIsFRU;
    }

    @Column
    @XmlElement
    public Date getEntPhysicalMfgDate() {
        return m_entPhysicalMfgDate;
    }

    public void setEntPhysicalMfgDate(Date entPhysicalMfgDate) {
        this.m_entPhysicalMfgDate = entPhysicalMfgDate;
    }

    @Column
    @XmlElement
    public String getEntPhysicalUris() {
        return m_entPhysicalUris;
    }

    public void setEntPhysicalUris(String entPhysicalUris) {
        this.m_entPhysicalUris = entPhysicalUris;
    }

    @XmlTransient
    @ManyToOne(cascade={CascadeType.ALL}, optional=true)
    @JoinColumn(name="parentId")
    public OnmsHwEntity getParent() {
        return m_parent;
    }

    public void setParent(OnmsHwEntity parent) {
        this.m_parent = parent;
    }

    @Transient
    @XmlAttribute(name="parentId")
    public Integer getParentId() {
        return m_parent == null ? null : m_parent.getEntPhysicalIndex();
    }

    @XmlElement(name="hwEntity")
    @XmlElementWrapper(name="children")
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY, cascade={CascadeType.ALL})
    public List<OnmsHwEntity> getChildren() {
        return m_children;
    }

    public void setChildren(List<OnmsHwEntity> children) {
        this.m_children = children;
    }

    public void addChildEntity(OnmsHwEntity child) {
        child.setParent(this);
        getChildren().add(child);        
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlAttribute(name="nodeId")
    @XmlJavaTypeAdapter(NodeIdAdapter.class)
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
        setNodeRecursively(this, node);
    }

    private void setNodeRecursively(OnmsHwEntity entity, OnmsNode node) {
        for (OnmsHwEntity e : entity.getChildren()) {
            if (e.getNode() == null) {
                LOG.trace("Setting nodeId {} on entity {} contained in {}", node.getId(), e.getEntPhysicalIndex(), e.getEntPhysicalContainedIn());
                e.setNode(node);
            }
            if (e.hasChildren()) {
                setNodeRecursively(e, node);
            }
        }
    }

    @OneToMany(mappedBy="hwEntity", fetch=FetchType.LAZY, cascade={CascadeType.ALL}, orphanRemoval=true)
    @XmlElement(name="hwEntityAttribute")
    @XmlElementWrapper(name="vendorAttributes")
    public List<OnmsHwEntityAttribute> getHwEntityAttributes() {
        return m_hwAttributes;
    }

    public void setHwEntityAttributes(List<OnmsHwEntityAttribute> hwAttributes) {
        m_hwAttributes = hwAttributes;
    }

    public void addAttribute(HwEntityAttributeType type, String value) {
        OnmsHwEntityAttribute attr = new OnmsHwEntityAttribute(type, value);
        attr.setHwEntity(this);
        m_hwAttributes.add(attr);
    }

    public String getAttributeValue(String typeName) {
        for (OnmsHwEntityAttribute attr : m_hwAttributes) {
            if (attr.getTypeName().equals(typeName)) {
                return attr.getValue();
            }
        }
        return null;
    }

    public String getAttributeClass(String typeName) {
        for (OnmsHwEntityAttribute attr : m_hwAttributes) {
            if (attr.getTypeName().equals(typeName)) {
                return attr.getType().getAttributeClass();
            }
        }
        return null;
    }

    @Transient
    public boolean isRoot() {
        return m_parent == null || m_entPhysicalIndex == 0;
    }

    @Transient
    public boolean hasChildren() {
        return !m_hwAttributes.isEmpty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this.getClass().getSimpleName(), ToStringStyle.SHORT_PREFIX_STYLE)
        .append("parent", getParentId())
        .append("nodeId", m_node == null ? null : m_node.getId())
        .append("entPhysicalIndex", m_entPhysicalIndex)
        .append("entPhysicalName", m_entPhysicalName)
        .append("entPhysicalDescr", m_entPhysicalDescr)
        .append("entPhysicalAlias", m_entPhysicalAlias)
        .append("entPhysicalVendorType", m_entPhysicalVendorType)
        .append("entPhysicalClass", m_entPhysicalClass)
        .append("entPhysicalMfgName", m_entPhysicalMfgName)
        .append("entPhysicalModelName", m_entPhysicalModelName)
        .append("entPhysicalHardwareRev", m_entPhysicalHardwareRev)
        .append("entPhysicalFirmwareRev", m_entPhysicalFirmwareRev)
        .append("entPhysicalSoftwareRev", m_entPhysicalSoftwareRev)
        .append("entPhysicalSerialNum", m_entPhysicalSerialNum)
        .append("vendorAttributes", m_hwAttributes.toString())
        .append("children", m_children.toString())
        .toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof OnmsHwEntity) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int compareTo(OnmsHwEntity o) {
        return getEntPhysicalName().compareTo(o.getEntPhysicalName());
    }

}
