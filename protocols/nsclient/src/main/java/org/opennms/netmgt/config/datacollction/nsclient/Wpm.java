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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/nsclient-datacollection}attrib" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="keyvalue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="recheckInterval" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
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
    @XmlAttribute(name = "keyvalue", required = true)
    protected String keyvalue;
    @XmlAttribute(name = "recheckInterval", required = true)
    protected int recheckInterval;

    /**
     * 
     * 							An NSClient Object
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
            attrib = new ArrayList<>();
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

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Wpm)) {
            return false;
        }
        Wpm castOther = (Wpm) other;
        return Objects.equals(attrib, castOther.attrib) && Objects.equals(name, castOther.name)
                && Objects.equals(keyvalue, castOther.keyvalue)
                && Objects.equals(recheckInterval, castOther.recheckInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attrib, name, keyvalue, recheckInterval);
    }

}
