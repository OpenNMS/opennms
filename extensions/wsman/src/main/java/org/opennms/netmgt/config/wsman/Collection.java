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
