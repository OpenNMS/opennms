/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wmi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wmi-datacollection}attrib" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="wmiClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="keyvalue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="recheckInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ifType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="resourceType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="wmiNamespace" type="{http://www.w3.org/2001/XMLSchema}string" default="root/cimv2" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "", propOrder = {
        "m_attribs"
})
@XmlRootElement(name = "wpm")
@ValidateUsing("wmi-datacollection.xsd")
public class Wpm implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlElement(name = "attrib")
    protected List<Attrib> m_attribs = new ArrayList<>();

    @XmlAttribute(name = "name", required = true)
    protected String m_name;

    @XmlAttribute(name = "wmiClass", required = true)
    protected String m_wmiClass;

    @XmlAttribute(name = "keyvalue", required = true)
    protected String m_keyvalue;

    @XmlAttribute(name = "recheckInterval", required = true)
    protected Integer m_recheckInterval;

    @XmlAttribute(name = "ifType", required = true)
    protected String m_ifType;

    @XmlAttribute(name = "resourceType", required = true)
    protected String m_resourceType;

    @XmlAttribute(name = "wmiNamespace")
    protected String m_wmiNamespace;

    public List<Attrib> getAttribs() {
        return m_attribs;
    }

    public void setAttribs(final List<Attrib> attribs) {
        if (attribs == m_attribs) return;
        m_attribs.clear();
        if (attribs != null) m_attribs.addAll(attribs);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getWmiClass() {
        return m_wmiClass;
    }

    public void setWmiClass(final String wmiClass) {
        m_wmiClass = ConfigUtils.assertNotEmpty(wmiClass, "wmiClass");
    }

    public String getKeyvalue() {
        return m_keyvalue;
    }

    public void setKeyvalue(final String keyvalue) {
        m_keyvalue = ConfigUtils.assertNotEmpty(keyvalue, "keyvalue");
    }

    public Integer getRecheckInterval() {
        return m_recheckInterval;
    }

    public void setRecheckInterval(final Integer recheckInterval) {
        m_recheckInterval = ConfigUtils.assertNotNull(recheckInterval, "recheckInterval");
    }

    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(final String ifType) {
        m_ifType = ConfigUtils.assertNotEmpty(ifType, "ifType");
    }

    public String getResourceType() {
        return m_resourceType;
    }

    public void setResourceType(final String resourceType) {
        m_resourceType = ConfigUtils.assertNotEmpty(resourceType, "resourceType");
    }

    public String getWmiNamespace() {
        return m_wmiNamespace == null? "root/cimv2" : m_wmiNamespace;
    }

    public void setWmiNamespace(final String wmiNamespace) {
        m_wmiNamespace = ConfigUtils.normalizeString(wmiNamespace);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Wpm)) {
            return false;
        }
        final Wpm that = (Wpm) obj;
        return Objects.equals(this.m_attribs, that.m_attribs) &&
                Objects.equals(this.m_name, that.m_name) &&
                Objects.equals(this.m_wmiClass, that.m_wmiClass) &&
                Objects.equals(this.m_keyvalue, that.m_keyvalue) &&
                Objects.equals(this.m_recheckInterval, that.m_recheckInterval) &&
                Objects.equals(this.m_ifType, that.m_ifType) &&
                Objects.equals(this.m_resourceType, that.m_resourceType) &&
                Objects.equals(this.m_wmiNamespace, that.m_wmiNamespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_attribs,
                            m_name,
                            m_wmiClass,
                            m_keyvalue,
                            m_recheckInterval,
                            m_ifType,
                            m_resourceType,
                            m_wmiNamespace);
    }

}
