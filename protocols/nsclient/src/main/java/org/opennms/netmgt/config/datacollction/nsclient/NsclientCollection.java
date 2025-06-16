/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
