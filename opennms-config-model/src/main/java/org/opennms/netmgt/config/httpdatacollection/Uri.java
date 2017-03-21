/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.httpdatacollection;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}url"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}attributes" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "m_url",
        "m_attributes"
})
@XmlRootElement(name = "uri")
@ValidateUsing("http-datacollection-config.xsd")
public class Uri implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "url", required = true)
    protected Url m_url;
    @XmlElementWrapper(name="attributes")
    @XmlElement(name="attrib")
    protected List<Attrib> m_attributes;
    @XmlAttribute(name = "name", required = true)
    protected String m_name;

    public Url getUrl() {
        return m_url;
    }

    public void setUrl(final Url value) {
        m_url = ConfigUtils.assertNotNull(value, "url");
    }

    public List<Attrib> getAttributes() {
        return m_attributes == null? Collections.emptyList() : m_attributes;
    }

    public void setAttributes(final List<Attrib> value) {
        m_attributes = value;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String value) {
        m_name = ConfigUtils.assertNotEmpty(value, "name");
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Uri)) {
            return false;
        }
        final Uri that = (Uri) other;
        return Objects.equals(this.m_url, that.m_url)
                && Objects.equals(this.m_attributes, that.m_attributes)
                && Objects.equals(this.m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_url, m_attributes, m_name);
    }

}
