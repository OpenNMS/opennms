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
        "m_parameters"
})
@XmlRootElement(name = "url")
@ValidateUsing("http-datacollection-config.xsd")
public class Url implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    protected List<Parameter> m_parameters;
    @XmlAttribute(name = "method")
    protected String m_method;
    @XmlAttribute(name = "http-version")
    protected String m_httpVersion;
    @XmlAttribute(name = "user-agent")
    protected String m_userAgent;
    @XmlAttribute(name = "virtual-host")
    protected String m_virtualHost;
    @XmlAttribute(name = "scheme")
    protected String m_scheme;
    @XmlAttribute(name = "user-info")
    protected String m_userInfo;
    @XmlAttribute(name = "host")
    protected String m_host;
    @XmlAttribute(name = "port")
    protected Integer m_port;
    @XmlAttribute(name = "path", required = true)
    protected String m_path;
    @XmlAttribute(name = "query")
    protected String m_query;
    @XmlAttribute(name = "fragment")
    protected String m_fragment;
    @XmlAttribute(name = "matches")
    protected String m_matches;
    @XmlAttribute(name = "response-range")
    protected String m_responseRange;
    @XmlAttribute(name = "canonical-equivalence")
    protected Boolean m_canonicalEquivalence;
    @XmlAttribute(name = "case-insensitive")
    protected Boolean m_caseInsensitive;
    @XmlAttribute(name = "comments")
    protected Boolean m_comments;
    @XmlAttribute(name = "dotall")
    protected Boolean m_dotall;
    @XmlAttribute(name = "literal")
    protected Boolean m_literal;
    @XmlAttribute(name = "multiline")
    protected Boolean m_multiline;
    @XmlAttribute(name = "unicode-case")
    protected Boolean m_unicodeCase;
    @XmlAttribute(name = "unix-lines")
    protected Boolean m_unixLines;

    public List<Parameter> getParameters() {
        return m_parameters == null? Collections.emptyList() : m_parameters;
    }

    public void setParameters(List<Parameter> value) {
        m_parameters = value;
    }

    public String getMethod() {
        return m_method == null? "GET" : m_method;
    }

    public void setMethod(final String value) {
        m_method = ConfigUtils.normalizeString(value);
    }

    public String getHttpVersion() {
        return m_httpVersion == null? "1.1" : m_httpVersion;
    }

    public void setHttpVersion(final String value) {
        m_httpVersion = ConfigUtils.normalizeString(value);
    }

    public Optional<String> getUserAgent() {
        return Optional.ofNullable(m_userAgent);
    }

    public void setUserAgent(final String value) {
        m_userAgent = ConfigUtils.normalizeString(value);
    }

    public Optional<String> getVirtualHost() {
        return Optional.ofNullable(m_virtualHost);
    }

    public void setVirtualHost(final String value) {
        m_virtualHost = ConfigUtils.normalizeString(value);
    }

    public String getScheme() {
        return m_scheme == null? "http" : m_scheme;
    }

    public void setScheme(final String value) {
        m_scheme = ConfigUtils.normalizeString(value);
    }

    public Optional<String> getUserInfo() {
        return Optional.ofNullable(m_userInfo);
    }

    public void setUserInfo(final String value) {
        m_userInfo = ConfigUtils.normalizeString(value);
    }

    public String getHost() {
        return m_host == null? "${ipaddr}" : m_host;
    }

    public void setHost(final String value) {
        m_host = ConfigUtils.normalizeString(value);
    }

    public int getPort() {
        return m_port == null? 80 : m_port;
    }

    public void setPort(final Integer value) {
        m_port = value;
    }

    public String getPath() {
        return m_path;
    }

    public void setPath(final String value) {
        m_path = ConfigUtils.assertNotEmpty(value, "path");
    }

    public Optional<String> getQuery() {
        return Optional.ofNullable(m_query);
    }

    public void setQuery(final String value) {
        m_query = ConfigUtils.normalizeString(value);
    }

    public Optional<String> getFragment() {
        return Optional.ofNullable(m_fragment);
    }

    public void setFragment(final String value) {
        m_fragment = ConfigUtils.normalizeString(value);
    }

    public String getMatches() {
        return m_matches == null? "(.*)" : m_matches;
    }

    public void setMatches(final String value) {
        m_matches = ConfigUtils.normalizeString(value);
    }

    public String getResponseRange() {
        return m_responseRange == null? "100-399" : m_responseRange;
    }

    public void setResponseRange(final String value) {
        m_responseRange = ConfigUtils.normalizeString(value);
    }

    public boolean isCanonicalEquivalence() {
        return m_canonicalEquivalence == null? false : m_canonicalEquivalence;
    }

    public void setCanonicalEquivalence(final Boolean value) {
        m_canonicalEquivalence = value;
    }

    public boolean isCaseInsensitive() {
        return m_caseInsensitive == null? false : m_caseInsensitive;
    }

    public void setCaseInsensitive(final Boolean value) {
        m_caseInsensitive = value;
    }

    public boolean isComments() {
        return m_comments == null? false : m_comments;
    }

    public void setComments(final Boolean value) {
        m_comments = value;
    }

    public boolean isDotall() {
        return m_dotall == null? false : m_dotall;
    }

    public void setDotall(final Boolean value) {
        m_dotall = value;
    }

    public boolean isLiteral() {
        return m_literal == null? false : m_literal;
    }

    public void setLiteral(final Boolean value) {
        m_literal = value;
    }

    public boolean isMultiline() {
        return m_multiline == null? false : m_multiline;
    }

    public void setMultiline(final Boolean value) {
        m_multiline = value;
    }

    public boolean isUnicodeCase() {
        return m_unicodeCase == null? false : m_unicodeCase;
    }

    public void setUnicodeCase(final Boolean value) {
        m_unicodeCase = value;
    }

    public boolean isUnixLines() {
        return m_unixLines == null? false : m_unixLines;
    }

    public void setUnixLines(final Boolean value) {
        m_unixLines = value;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Url)) {
            return false;
        }
        final Url that = (Url) other;
        return Objects.equals(this.m_parameters, that.m_parameters) && Objects.equals(this.m_method, that.m_method)
                && Objects.equals(this.m_httpVersion, that.m_httpVersion) && Objects.equals(this.m_userAgent, that.m_userAgent)
                && Objects.equals(this.m_virtualHost, that.m_virtualHost) && Objects.equals(this.m_scheme, that.m_scheme)
                && Objects.equals(this.m_userInfo, that.m_userInfo) && Objects.equals(this.m_host, that.m_host)
                && Objects.equals(this.m_port, that.m_port) && Objects.equals(this.m_path, that.m_path)
                && Objects.equals(this.m_query, that.m_query) && Objects.equals(this.m_fragment, that.m_fragment)
                && Objects.equals(this.m_matches, that.m_matches) && Objects.equals(this.m_responseRange, that.m_responseRange)
                && Objects.equals(this.m_canonicalEquivalence, that.m_canonicalEquivalence)
                && Objects.equals(this.m_caseInsensitive, that.m_caseInsensitive)
                && Objects.equals(this.m_comments, that.m_comments) && Objects.equals(this.m_dotall, that.m_dotall)
                && Objects.equals(this.m_literal, that.m_literal) && Objects.equals(this.m_multiline, that.m_multiline)
                && Objects.equals(this.m_unicodeCase, that.m_unicodeCase) && Objects.equals(this.m_unixLines, that.m_unixLines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_parameters, m_method, m_httpVersion, m_userAgent, m_virtualHost, m_scheme, m_userInfo, m_host, m_port, m_path,
                            m_query, m_fragment, m_matches, m_responseRange, m_canonicalEquivalence, m_caseInsensitive, m_comments, m_dotall,
                            m_literal, m_multiline, m_unicodeCase, m_unixLines);
    }

}
