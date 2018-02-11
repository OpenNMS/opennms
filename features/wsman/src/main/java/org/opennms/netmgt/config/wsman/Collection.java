/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.wsman;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wsman-datacollection}rrd"/&gt;
 *         &lt;element name="include-all-system-definitions"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="include-system-definition" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
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
    "rrd",
    "includeAllSystemDefinitions",
    "includeSystemDefinition"
})
@XmlRootElement(name = "collection")
public class Collection {

    @XmlElement(required = true)
    protected Rrd rrd;
    @XmlElement(name = "include-all-system-definitions", required = true)
    protected Collection.IncludeAllSystemDefinitions includeAllSystemDefinitions;
    @XmlElement(name = "include-system-definition", required = true)
    protected List<String> includeSystemDefinition;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the rrd property.
     * 
     * @return
     *     possible object is
     *     {@link Rrd }
     *     
     */
    public Rrd getRrd() {
        return rrd;
    }

    /**
     * Sets the value of the rrd property.
     * 
     * @param value
     *     allowed object is
     *     {@link Rrd }
     *     
     */
    public void setRrd(Rrd value) {
        this.rrd = value;
    }

    /**
     * Gets the value of the includeAllSystemDefinitions property.
     * 
     * @return
     *     possible object is
     *     {@link Collection.IncludeAllSystemDefinitions }
     *     
     */
    public Collection.IncludeAllSystemDefinitions getIncludeAllSystemDefinitions() {
        return includeAllSystemDefinitions;
    }

    /**
     * Sets the value of the includeAllSystemDefinitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Collection.IncludeAllSystemDefinitions }
     *     
     */
    public void setIncludeAllSystemDefinitions(Collection.IncludeAllSystemDefinitions value) {
        this.includeAllSystemDefinitions = value;
    }

    /**
     * Gets the value of the includeSystemDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the includeSystemDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIncludeSystemDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getIncludeSystemDefinition() {
        if (includeSystemDefinition == null) {
            includeSystemDefinition = new ArrayList<>();
        }
        return this.includeSystemDefinition;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class IncludeAllSystemDefinitions {

        @Override
        public int hashCode() {
            return Objects.hash();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return true;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(rrd, includeAllSystemDefinitions, includeSystemDefinition, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Collection other = (Collection) obj;
        return Objects.equals(this.rrd, other.rrd) &&
                Objects.equals(this.includeAllSystemDefinitions, other.includeAllSystemDefinitions) &&
                Objects.equals(this.includeSystemDefinition, other.includeSystemDefinition) &&
                Objects.equals(this.name, other.name);
    }
}
