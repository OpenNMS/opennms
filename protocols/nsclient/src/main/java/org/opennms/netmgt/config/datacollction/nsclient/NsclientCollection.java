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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/nsclient-datacollection}rrd"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/nsclient-datacollection}wpms"/&gt;
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
    "wpms"
})
@XmlRootElement(name = "nsclient-collection")
public class NsclientCollection {

    @XmlElement(required = true)
    protected Rrd rrd;
    @XmlElement(required = true)
    protected Wpms wpms;
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
     * Gets the value of the wpms property.
     * 
     * @return
     *     possible object is
     *     {@link Wpms }
     *     
     */
    public Wpms getWpms() {
        return wpms;
    }

    /**
     * Sets the value of the wpms property.
     * 
     * @param value
     *     allowed object is
     *     {@link Wpms }
     *     
     */
    public void setWpms(Wpms value) {
        this.wpms = value;
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

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NsclientCollection)) {
            return false;
        }
        NsclientCollection castOther = (NsclientCollection) other;
        return Objects.equals(rrd, castOther.rrd) && Objects.equals(wpms, castOther.wpms)
                && Objects.equals(name, castOther.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rrd, wpms, name);
    }

}
