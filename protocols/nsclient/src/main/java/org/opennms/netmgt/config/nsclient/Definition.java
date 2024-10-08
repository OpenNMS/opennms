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
package org.opennms.netmgt.config.nsclient;

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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/nsclient}range" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="specific" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/nsclient}ip-match" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="port" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="retry" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="password" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "range",
    "specific",
    "ipMatch"
})
@XmlRootElement(name = "definition")
public class Definition {

    protected List<Range> range;
    protected List<String> specific;
    @XmlElement(name = "ip-match")
    protected List<String> ipMatch;
    @XmlAttribute(name = "port")
    protected Integer port;
    @XmlAttribute(name = "retry")
    protected Integer retry;
    @XmlAttribute(name = "timeout")
    protected Integer timeout;
    @XmlAttribute(name = "password")
    protected String password;

    /**
     * IP address range to which this definition
     *             applies.Gets the value of the range property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the range property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRange().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Range }
     * 
     * 
     */
    public List<Range> getRange() {
        if (range == null) {
            range = new ArrayList<>();
        }
        return this.range;
    }

    /**
     * Gets the value of the specific property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the specific property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpecific().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSpecific() {
        if (specific == null) {
            specific = new ArrayList<>();
        }
        return this.specific;
    }

    /**
     * Match Octets (as in IPLIKE) Gets the value of the ipMatch property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ipMatch property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIpMatch().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getIpMatch() {
        if (ipMatch == null) {
            ipMatch = new ArrayList<>();
        }
        return this.ipMatch;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPort(Integer value) {
        this.port = value;
    }

    /**
     * Gets the value of the retry property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRetry() {
        return retry;
    }

    /**
     * Sets the value of the retry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRetry(Integer value) {
        this.retry = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the timeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTimeout(Integer value) {
        this.timeout = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Definition)) {
            return false;
        }
        Definition castOther = (Definition) other;
        return Objects.equals(range, castOther.range) && Objects.equals(specific, castOther.specific)
                && Objects.equals(ipMatch, castOther.ipMatch) && Objects.equals(port, castOther.port)
                && Objects.equals(retry, castOther.retry) && Objects.equals(timeout, castOther.timeout)
                && Objects.equals(password, castOther.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, specific, ipMatch, port, retry, timeout, password);
    }

}
