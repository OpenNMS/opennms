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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}http-collection" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rrdRepository" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "m_httpCollection"
})
@XmlRootElement(name = "http-datacollection-config")
@ValidateUsing("http-datacollection-config.xsd")
public class HttpDatacollectionConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "http-collection")
    protected List<HttpCollection> m_httpCollection = new ArrayList<>();
    @XmlAttribute(name = "rrdRepository", required = true)
    protected String m_rrdRepository;

    /**
     * Gets the value of the httpCollection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the httpCollection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHttpCollection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HttpCollection }
     * 
     * 
     */
    public List<HttpCollection> getHttpCollection() {
        return m_httpCollection;
    }

    public String getRrdRepository() {
        return m_rrdRepository;
    }

    void setRrdRepository(final String value) {
        m_rrdRepository = ConfigUtils.assertNotEmpty(value, "value");
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HttpDatacollectionConfig)) {
            return false;
        }
        final HttpDatacollectionConfig that = (HttpDatacollectionConfig) other;
        return Objects.equals(this.m_httpCollection, that.m_httpCollection)
                && Objects.equals(this.m_rrdRepository, that.m_rrdRepository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_httpCollection, m_rrdRepository);
    }

}
