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

package org.opennms.netmgt.config.datacollction.nsclient;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.collection.api.AttributeType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;pattern value="([Cc](ounter|OUNTER)|[Gg](auge|AUGE))"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="maxval" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="minval" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "attrib")
public class Attrib {

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "type", required = true)
    protected AttributeType type;
    @XmlAttribute(name = "maxval")
    protected String maxval;
    @XmlAttribute(name = "minval")
    protected String minval;

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
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    /**
     * Gets the value of the maxval property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxval() {
        return maxval;
    }

    /**
     * Sets the value of the maxval property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxval(String value) {
        this.maxval = value;
    }

    /**
     * Gets the value of the minval property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinval() {
        return minval;
    }

    /**
     * Sets the value of the minval property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinval(String value) {
        this.minval = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Attrib)) {
            return false;
        }
        Attrib castOther = (Attrib) other;
        return Objects.equals(name, castOther.name) && Objects.equals(alias, castOther.alias)
                && Objects.equals(type, castOther.type) && Objects.equals(maxval, castOther.maxval)
                && Objects.equals(minval, castOther.minval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, alias, type, maxval, minval);
    }

}
