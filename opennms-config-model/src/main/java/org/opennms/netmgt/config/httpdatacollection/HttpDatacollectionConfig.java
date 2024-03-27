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
