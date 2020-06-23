/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wsman}range" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="specific" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/wsman}ip-match" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="retry" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="username" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="password" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="port" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="max-elements" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="ssl" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="strict-ssl" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="path" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
public class Definition implements WsmanAgentConfig {

    protected List<Range> range;
    protected List<String> specific;
    @XmlElement(name = "ip-match")
    protected List<String> ipMatch;
    @XmlAttribute(name = "retry")
    protected Integer retry;
    @XmlAttribute(name = "timeout")
    protected Integer timeout;
    @XmlAttribute(name = "username")
    protected String username;
    @XmlAttribute(name = "password")
    protected String password;
    @XmlAttribute(name = "port")
    protected Integer port;
    @XmlAttribute(name = "max-elements")
    protected Integer maxElements;
    @XmlAttribute(name = "ssl")
    protected Boolean ssl;
    @XmlAttribute(name = "strict-ssl")
    protected Boolean strictSsl;
    @XmlAttribute(name = "path")
    protected String path;
    @XmlAttribute(name = "product-vendor")
    protected String productVendor;
    @XmlAttribute(name = "product-version")
    protected String productVersion;
    @XmlAttribute(name = "gss-auth")
    protected Boolean gssAuth;

    public Definition() { }

    public Definition(WsmanAgentConfig config) {
        setUsername(config.getUsername());
        setPassword(config.getPassword());
        setPort(config.getPort());
        setRetry(config.getRetry());
        setTimeout(config.getTimeout());
        setMaxElements(config.getMaxElements());
        setSsl(config.isSsl());
        setStrictSsl(config.isStrictSsl());
        setPath(config.getPath());
        setProductVendor(config.getProductVendor());
        setProductVersion(config.getProductVersion());
        setGssAuth(config.isGssAuth());
    }

    /**
     * IP address range to which this definition applies.Gets the value of the range property.
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
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
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
     * Gets the value of the maxElements property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxElements() {
        return maxElements;
    }

    /**
     * Sets the value of the maxElements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxElements(Integer value) {
        this.maxElements = value;
    }

    /**
     * Gets the value of the ssl property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSsl() {
        return ssl;
    }

    /**
     * Sets the value of the ssl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSsl(Boolean value) {
        this.ssl = value;
    }

    /**
     * Gets the value of the strictSsl property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStrictSsl() {
        return strictSsl;
    }

    /**
     * Sets the value of the strictSsl property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStrictSsl(Boolean value) {
        this.strictSsl = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

    public String getProductVendor() {
        return productVendor;
    }

    public void setProductVendor(String productVendor) {
        this.productVendor = productVendor;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    @Override
    public Boolean isGssAuth() {
        return gssAuth;
    }

    public void setGssAuth(Boolean value) {
        this.gssAuth = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(range, specific, ipMatch, timeout, retry, username, password, port, maxElements,
                ssl, strictSsl, path, productVendor, gssAuth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Definition other = (Definition) obj;
        return Objects.equals(this.range, other.range) &&
                Objects.equals(this.specific, other.specific) &&
                Objects.equals(this.ipMatch, other.ipMatch) &&
                Objects.equals(this.timeout, other.timeout) &&
                Objects.equals(this.retry, other.retry) &&
                Objects.equals(this.username, other.username) &&
                Objects.equals(this.password, other.password) &&
                Objects.equals(this.port, other.port) &&
                Objects.equals(this.maxElements, other.maxElements) &&
                Objects.equals(this.ssl, other.ssl) &&
                Objects.equals(this.strictSsl, other.strictSsl) &&
                Objects.equals(this.path, other.path) &&
                Objects.equals(this.productVendor, other.productVendor) &&
                Objects.equals(this.productVersion, other.productVersion) &&
                Objects.equals(this.gssAuth, other.gssAuth);
    }
}
