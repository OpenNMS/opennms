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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.opennms.netmgt.snmp.SnmpObjId;

@XmlRootElement(name = "hwEntityAttributeType")
@Entity
@Table(name="hwEntityAttributeType")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HwEntityAttributeType implements Serializable, Comparable<HwEntityAttributeType> {

    private static final long serialVersionUID = -136267386674546238L;

    private Integer m_id;

    private String m_attributeOid;
    
    private String m_attributeName;

    private String m_attributeClass;

    public HwEntityAttributeType() {
    }

    public HwEntityAttributeType(String attributeOid, String attributeName, String attributeClass) {
        super();
        this.m_attributeOid = attributeOid;
        this.m_attributeName = attributeName;
        this.m_attributeClass = attributeClass;
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

    @Column(name="attribName", unique=true, nullable=false)
    public String getName() {
        return m_attributeName;
    }

    public void setName(String attributeName) {
        this.m_attributeName = attributeName;
    }

    @Column(name="attribOid", unique=true, nullable=false)
    public String getOid() {
        return m_attributeOid;
    }

    public void setOid(String attributeOid) {
        this.m_attributeOid = attributeOid;
    }

    @Transient
    public SnmpObjId getSnmpObjId() {
        return SnmpObjId.get(m_attributeOid);
    }

    @Column(name="attribClass")
    public String getAttributeClass() {
        return m_attributeClass;
    }

    public void setAttributeClass(String attributeClass) {
        this.m_attributeClass = attributeClass;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this.getClass().getSimpleName(), ToStringStyle.SHORT_PREFIX_STYLE)
        .append("oid", m_attributeOid)
        .append("name", m_attributeName)
        .append("class", m_attributeClass)
        .toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof HwEntityAttributeType) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int compareTo(HwEntityAttributeType o) {
        return getName().compareTo(o.getName());
    }

}
