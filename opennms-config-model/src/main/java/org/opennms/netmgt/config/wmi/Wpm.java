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

package org.opennms.netmgt.config.wmi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;


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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "attrib"
})
@XmlRootElement(name = "wpm")
public class Wpm {

    protected List<Attrib> attrib;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "wmiClass", required = true)
    protected String wmiClass;
    @XmlAttribute(name = "keyvalue", required = true)
    protected String keyvalue;
    @XmlAttribute(name = "recheckInterval", required = true)
    protected int recheckInterval;
    @XmlAttribute(name = "ifType", required = true)
    protected String ifType;
    @XmlAttribute(name = "resourceType", required = true)
    protected String resourceType;
    @XmlAttribute(name = "wmiNamespace")
    protected String wmiNamespace;

    /**
     * 
     * 							An WMI Object
     * 						Gets the value of the attrib property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attrib property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttrib().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Attrib }
     * 
     * 
     */
    public List<Attrib> getAttrib() {
        if (attrib == null) {
            attrib = new ArrayList<Attrib>();
        }
        return this.attrib;
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
     * Gets the value of the wmiClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWmiClass() {
        return wmiClass;
    }

    /**
     * Sets the value of the wmiClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWmiClass(String value) {
        this.wmiClass = value;
    }

    /**
     * Gets the value of the keyvalue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyvalue() {
        return keyvalue;
    }

    /**
     * Sets the value of the keyvalue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyvalue(String value) {
        this.keyvalue = value;
    }

    /**
     * Gets the value of the recheckInterval property.
     * 
     */
    public int getRecheckInterval() {
        return recheckInterval;
    }

    /**
     * Sets the value of the recheckInterval property.
     * 
     */
    public void setRecheckInterval(int value) {
        this.recheckInterval = value;
    }

    /**
     * Gets the value of the ifType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIfType() {
        return ifType;
    }

    /**
     * Sets the value of the ifType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIfType(String value) {
        this.ifType = value;
    }

    /**
     * Gets the value of the resourceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Sets the value of the resourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResourceType(String value) {
        this.resourceType = value;
    }

    /**
     * Gets the value of the wmiNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWmiNamespace() {
        if (wmiNamespace == null) {
            return "root/cimv2";
        } else {
            return wmiNamespace;
        }
    }

    /**
     * Sets the value of the wmiNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWmiNamespace(String value) {
        this.wmiNamespace = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Wpm)) {
            return false;
        }
        Wpm castOther = (Wpm) other;
        return Objects.equals(attrib, castOther.attrib) && Objects.equals(name, castOther.name)
                && Objects.equals(wmiClass, castOther.wmiClass) && Objects.equals(keyvalue, castOther.keyvalue)
                && Objects.equals(recheckInterval, castOther.recheckInterval)
                && Objects.equals(ifType, castOther.ifType) && Objects.equals(resourceType, castOther.resourceType)
                && Objects.equals(wmiNamespace, castOther.wmiNamespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attrib, name, wmiClass, keyvalue, recheckInterval, ifType, resourceType, wmiNamespace);
    }

}
