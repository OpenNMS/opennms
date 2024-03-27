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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/nsclient-datacollection}nsclient-collection" maxOccurs="unbounded"/&gt;
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
    "nsclientCollection"
})
@XmlRootElement(name = "nsclient-datacollection-config")
public class NsclientDatacollectionConfig {

    @XmlElement(name = "nsclient-collection", required = true)
    protected List<NsclientCollection> nsclientCollection;
    @XmlAttribute(name = "rrdRepository", required = true)
    protected String rrdRepository;

    /**
     * Gets the value of the nsclientCollection property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nsclientCollection property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNsclientCollection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NsclientCollection }
     * 
     * 
     */
    public List<NsclientCollection> getNsclientCollection() {
        if (nsclientCollection == null) {
            nsclientCollection = new ArrayList<>();
        }
        return this.nsclientCollection;
    }

    /**
     * Gets the value of the rrdRepository property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRrdRepository() {
        return rrdRepository;
    }

    /**
     * Sets the value of the rrdRepository property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRrdRepository(String value) {
        this.rrdRepository = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NsclientDatacollectionConfig)) {
            return false;
        }
        NsclientDatacollectionConfig castOther = (NsclientDatacollectionConfig) other;
        return Objects.equals(nsclientCollection, castOther.nsclientCollection)
                && Objects.equals(rrdRepository, castOther.rrdRepository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nsclientCollection, rrdRepository);
    }

}
