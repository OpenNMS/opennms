/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.httpdatacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
 *         &lt;element ref="{http://xmlns.opennms.org/xsd/config/http-datacollection}parameters" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="method" type="{http://xmlns.opennms.org/xsd/config/http-datacollection}allowed-methods" default="GET" /&gt;
 *       &lt;attribute name="http-version" type="{http://xmlns.opennms.org/xsd/config/http-datacollection}allowed-versions" default="1.1" /&gt;
 *       &lt;attribute name="user-agent" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="virtual-host" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="scheme" type="{http://www.w3.org/2001/XMLSchema}string" default="http" /&gt;
 *       &lt;attribute name="user-info" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="host" type="{http://www.w3.org/2001/XMLSchema}string" default="${ipaddr}" /&gt;
 *       &lt;attribute name="port" default="80"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int"&gt;
 *             &lt;minInclusive value="1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="path" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="query" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="fragment" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="matches" type="{http://www.w3.org/2001/XMLSchema}string" default="(.*)" /&gt;
 *       &lt;attribute name="response-range" type="{http://www.w3.org/2001/XMLSchema}string" default="100-399" /&gt;
 *       &lt;attribute name="canonical-equivalence" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="case-insensitive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="comments" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="dotall" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="literal" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="multiline" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="unicode-case" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="unix-lines" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parameters"
})
@XmlRootElement(name = "url")
public class Url implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    protected List<Parameter> parameters;
    @XmlAttribute(name = "method")
    protected String method;
    @XmlAttribute(name = "http-version")
    protected String httpVersion;
    @XmlAttribute(name = "user-agent")
    protected String userAgent;
    @XmlAttribute(name = "virtual-host")
    protected String virtualHost;
    @XmlAttribute(name = "scheme")
    protected String scheme;
    @XmlAttribute(name = "user-info")
    protected String userInfo;
    @XmlAttribute(name = "host")
    protected String host;
    @XmlAttribute(name = "port")
    protected Integer port;
    @XmlAttribute(name = "path", required = true)
    protected String path;
    @XmlAttribute(name = "query")
    protected String query;
    @XmlAttribute(name = "fragment")
    protected String fragment;
    @XmlAttribute(name = "matches")
    protected String matches;
    @XmlAttribute(name = "response-range")
    protected String responseRange;
    @XmlAttribute(name = "canonical-equivalence")
    protected Boolean canonicalEquivalence;
    @XmlAttribute(name = "case-insensitive")
    protected Boolean caseInsensitive;
    @XmlAttribute(name = "comments")
    protected Boolean comments;
    @XmlAttribute(name = "dotall")
    protected Boolean dotall;
    @XmlAttribute(name = "literal")
    protected Boolean literal;
    @XmlAttribute(name = "multiline")
    protected Boolean multiline;
    @XmlAttribute(name = "unicode-case")
    protected Boolean unicodeCase;
    @XmlAttribute(name = "unix-lines")
    protected Boolean unixLines;

    public List<Parameter> getParameters() {
        return parameters == null? Collections.emptyList() : parameters;
    }

    public void setParameters(List<Parameter> value) {
        this.parameters = value == null? new ArrayList<>() : value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethod() {
        if (method == null) {
            return "GET";
        } else {
            return method;
        }
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethod(String value) {
        this.method = value;
    }

    /**
     * Gets the value of the httpVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHttpVersion() {
        if (httpVersion == null) {
            return "1.1";
        } else {
            return httpVersion;
        }
    }

    /**
     * Sets the value of the httpVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHttpVersion(String value) {
        this.httpVersion = value;
    }

    /**
     * Gets the value of the userAgent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    /**
     * Sets the value of the userAgent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserAgent(String value) {
        this.userAgent = value;
    }

    /**
     * Gets the value of the virtualHost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Optional<String> getVirtualHost() {
        return Optional.ofNullable(virtualHost);
    }

    /**
     * Sets the value of the virtualHost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVirtualHost(String value) {
        this.virtualHost = value;
    }

    /**
     * Gets the value of the scheme property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScheme() {
        if (scheme == null) {
            return "http";
        } else {
            return scheme;
        }
    }

    /**
     * Sets the value of the scheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScheme(String value) {
        this.scheme = value;
    }

    /**
     * Gets the value of the userInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Optional<String> getUserInfo() {
        return Optional.ofNullable(userInfo);
    }

    /**
     * Sets the value of the userInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserInfo(String value) {
        this.userInfo = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        if (host == null) {
            return "${ipaddr}";
        } else {
            return host;
        }
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPort() {
        if (port == null) {
            return  80;
        } else {
            return port;
        }
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
        if (value == null) {
            throw new IllegalArgumentException("'path' is a required attribute!");
        }
        this.path = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Optional<String> getQuery() {
        return Optional.ofNullable(query);
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuery(String value) {
        this.query = value;
    }

    /**
     * Gets the value of the fragment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Optional<String> getFragment() {
        return Optional.ofNullable(fragment);
    }

    /**
     * Sets the value of the fragment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFragment(String value) {
        this.fragment = value;
    }

    /**
     * Gets the value of the matches property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMatches() {
        if (matches == null) {
            return "(.*)";
        } else {
            return matches;
        }
    }

    /**
     * Sets the value of the matches property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMatches(String value) {
        this.matches = value;
    }

    /**
     * Gets the value of the responseRange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResponseRange() {
        if (responseRange == null) {
            return "100-399";
        } else {
            return responseRange;
        }
    }

    /**
     * Sets the value of the responseRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResponseRange(String value) {
        this.responseRange = value;
    }

    /**
     * Gets the value of the canonicalEquivalence property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCanonicalEquivalence() {
        if (canonicalEquivalence == null) {
            return false;
        } else {
            return canonicalEquivalence;
        }
    }

    /**
     * Sets the value of the canonicalEquivalence property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCanonicalEquivalence(Boolean value) {
        this.canonicalEquivalence = value;
    }

    /**
     * Gets the value of the caseInsensitive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCaseInsensitive() {
        if (caseInsensitive == null) {
            return false;
        } else {
            return caseInsensitive;
        }
    }

    /**
     * Sets the value of the caseInsensitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCaseInsensitive(Boolean value) {
        this.caseInsensitive = value;
    }

    /**
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isComments() {
        if (comments == null) {
            return false;
        } else {
            return comments;
        }
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setComments(Boolean value) {
        this.comments = value;
    }

    /**
     * Gets the value of the dotall property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDotall() {
        if (dotall == null) {
            return false;
        } else {
            return dotall;
        }
    }

    /**
     * Sets the value of the dotall property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDotall(Boolean value) {
        this.dotall = value;
    }

    /**
     * Gets the value of the literal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLiteral() {
        if (literal == null) {
            return false;
        } else {
            return literal;
        }
    }

    /**
     * Sets the value of the literal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLiteral(Boolean value) {
        this.literal = value;
    }

    /**
     * Gets the value of the multiline property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMultiline() {
        if (multiline == null) {
            return false;
        } else {
            return multiline;
        }
    }

    /**
     * Sets the value of the multiline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMultiline(Boolean value) {
        this.multiline = value;
    }

    /**
     * Gets the value of the unicodeCase property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUnicodeCase() {
        if (unicodeCase == null) {
            return false;
        } else {
            return unicodeCase;
        }
    }

    /**
     * Sets the value of the unicodeCase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnicodeCase(Boolean value) {
        this.unicodeCase = value;
    }

    /**
     * Gets the value of the unixLines property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUnixLines() {
        if (unixLines == null) {
            return false;
        } else {
            return unixLines;
        }
    }

    /**
     * Sets the value of the unixLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnixLines(Boolean value) {
        this.unixLines = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Url)) {
            return false;
        }
        Url castOther = (Url) other;
        return Objects.equals(parameters, castOther.parameters) && Objects.equals(method, castOther.method)
                && Objects.equals(httpVersion, castOther.httpVersion) && Objects.equals(userAgent, castOther.userAgent)
                && Objects.equals(virtualHost, castOther.virtualHost) && Objects.equals(scheme, castOther.scheme)
                && Objects.equals(userInfo, castOther.userInfo) && Objects.equals(host, castOther.host)
                && Objects.equals(port, castOther.port) && Objects.equals(path, castOther.path)
                && Objects.equals(query, castOther.query) && Objects.equals(fragment, castOther.fragment)
                && Objects.equals(matches, castOther.matches) && Objects.equals(responseRange, castOther.responseRange)
                && Objects.equals(canonicalEquivalence, castOther.canonicalEquivalence)
                && Objects.equals(caseInsensitive, castOther.caseInsensitive)
                && Objects.equals(comments, castOther.comments) && Objects.equals(dotall, castOther.dotall)
                && Objects.equals(literal, castOther.literal) && Objects.equals(multiline, castOther.multiline)
                && Objects.equals(unicodeCase, castOther.unicodeCase) && Objects.equals(unixLines, castOther.unixLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, method, httpVersion, userAgent, virtualHost, scheme, userInfo, host, port, path,
                query, fragment, matches, responseRange, canonicalEquivalence, caseInsensitive, comments, dotall,
                literal, multiline, unicodeCase, unixLines);
    }

}
