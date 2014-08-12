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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.core.style.ToStringCreator;

@XmlRootElement(name = "hwEntityAttributeType")
@Entity
@Table(name="hwEntityAttributeType")
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HwEntityAttributeType implements Serializable {

    private static final long serialVersionUID = -136267386674546238L;

    private Integer m_id;

    private String m_attributeName;

    private String m_attributeType;

    public HwEntityAttributeType() {
    }

    public HwEntityAttributeType(String name, String type) {
        super();
        this.m_attributeName = name;
        this.m_attributeType = type;
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
    @XmlAttribute(name="id")
    @Transient
    public String getHwEntityAttributeTypeId() {
        return getId() == null ? null : getId().toString();
    }

    public void setOnmsHwEntityAttributeId(final String id) {
        setId(Integer.valueOf(id));
    }

    @Column(name="attribName", unique=true, nullable=false)
    public String getName() {
        return m_attributeName;
    }

    public void setName(String attributeName) {
        this.m_attributeName = attributeName;
    }

    @Column(name="attribType")
    public String getType() {
        return m_attributeType;
    }

    public void setType(String attributeType) {
        this.m_attributeType = attributeType;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
        .append("id", m_id)
        .append("name", m_attributeName)
        .append("type", m_attributeType)
        .toString();
    }

}
