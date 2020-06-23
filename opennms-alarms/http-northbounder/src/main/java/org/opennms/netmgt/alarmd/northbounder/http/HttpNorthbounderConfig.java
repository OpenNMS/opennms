/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.http;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.alarmd.api.NorthbounderException;

/**
 * Configuration for HTTP NBI implementation.
 * FIXME: This needs lots of work.
 * FIXME: Make configuration mimic configuration of other HttpClient configurations: PSM, HttpCollector
 * so that users can reuse their configuration knowledge and not have to configured HTTP based client
 * configurations differently in every section of the software.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name="http-northbounder-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpNorthbounderConfig implements Serializable, Comparable<HttpNorthbounderConfig>{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Enumeration HttpMethod.
     */
    public static enum HttpMethod {
        /** The get. */
        GET, 
        /** The post. */
        POST
    }

    /** The method. */
    @XmlAttribute(name="method", required=false)
    private HttpMethod m_method = HttpMethod.GET;

    /** The HTTP version. */
    @XmlAttribute(name="http-version", required=false)
    private String m_httpVersion = "1.1";

    /** The user agent. */
    @XmlAttribute(name="user-agent", required=false)
    private String m_userAgent = "OpenNMS Http Northbound Interface";

    /** The virtual host. */
    @XmlAttribute(name="virtual-host", required=false)
    private String m_virtualHost;

    /** The scheme. */
    @XmlAttribute(name="scheme", required=false)
    private String m_scheme = "http";

    /** The user info. */
    @XmlAttribute(name="user-info", required=false)
    private String m_userInfo;

    /** The host. */
    @XmlAttribute(name="host", required=true)
    private String m_host;

    /** The port. */
    @XmlAttribute(name="port", required=false)
    private Integer m_port = Integer.valueOf(80);

    /** The path. */
    @XmlAttribute(name="path", required=false)
    private String m_path = "/";

    /** The query. */
    @XmlAttribute(name="query", required=false)
    private String m_query;

    /** The fragment. */
    @XmlAttribute(name="fragment", required=false)
    private String m_fragment;

    /** The acceptable UEIs. */
    private List<String> m_acceptableUeis;

    /**
     * Instantiates a new HTTP northbounder configuration.
     *
     * @param host the host
     */
    public HttpNorthbounderConfig(String host) {
        m_host = host;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(HttpNorthbounderConfig o) {
        int c = 0;
        try {
            c = getURI().compareTo(o.getURI());
        } catch (NorthbounderException e) {
        }
        return c;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        boolean eq = false;
        if (o instanceof HttpNorthbounderConfig) {
            HttpNorthbounderConfig other = (HttpNorthbounderConfig) o;
            try {
                eq = getURI().equals(other.getURI());
            } catch (NorthbounderException e) {
                eq = false;
            }
        }
        return eq;
    }

    /**
     * Gets the method.
     *
     * @return the method
     */
    public HttpMethod getMethod() {
        return m_method;
    }

    /**
     * Sets the method.
     *
     * @param method the new method
     */
    public void setMethod(HttpMethod method) {
        m_method = method;
    }

    /**
     * Gets the HTTP version.
     *
     * @return the HTTP version
     */
    public String getHttpVersion() {
        return m_httpVersion;
    }

    /**
     * Sets the HTTP version.
     *
     * @param httpVersion the new HTTP version
     */
    public void setHttpVersion(String httpVersion) {
        m_httpVersion = httpVersion;
    }

    /**
     * Gets the user agent.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return m_userAgent;
    }

    /**
     * Sets the user agent.
     *
     * @param userAgent the new user agent
     */
    public void setUserAgent(String userAgent) {
        m_userAgent = userAgent;
    }

    /**
     * Gets the virtual host.
     *
     * @return the virtual host
     */
    public String getVirtualHost() {
        return m_virtualHost;
    }

    /**
     * Sets the virtual host.
     *
     * @param virtualHost the new virtual host
     */
    public void setVirtualHost(String virtualHost) {
        m_virtualHost = virtualHost;
    }

    /**
     * Gets the scheme.
     *
     * @return the scheme
     */
    public String getScheme() {
        return m_scheme;
    }

    /**
     * Sets the scheme.
     *
     * @param scheme the new scheme
     */
    public void setScheme(String scheme) {
        m_scheme = scheme;
    }

    /**
     * Gets the user info.
     *
     * @return the user info
     */
    public String getUserInfo() {
        return m_userInfo;
    }

    /**
     * Sets the user info.
     *
     * @param userInfo the new user info
     */
    public void setUserInfo(String userInfo) {
        m_userInfo = userInfo;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return m_host;
    }

    /**
     * Sets the host.
     *
     * @param host the new host
     */
    public void setHost(String host) {
        m_host = host;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public Integer getPort() {
        return m_port;
    }

    /**
     * Sets the port.
     *
     * @param port the new port
     */
    public void setPort(Integer port) {
        m_port = port;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return m_path;
    }

    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(String path) {
        m_path = path;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
        return m_query;
    }

    /**
     * Sets the query.
     *
     * @param query the new query
     */
    public void setQuery(String query) {
        m_query = query;
    }

    /**
     * Gets the fragment.
     *
     * @return the fragment
     */
    public String getFragment() {
        return m_fragment;
    }

    /**
     * Sets the fragment.
     *
     * @param fragment the new fragment
     */
    public void setFragment(String fragment) {
        m_fragment = fragment;
    }

    /**
     * Gets the acceptable UEIs.
     *
     * @return the acceptable UEIs
     */
    public List<String> getAcceptableUeis() {
        return m_acceptableUeis;
    }

    /**
     * Sets the acceptable UEIs.
     *
     * @param acceptableUeis the new acceptable UEIs
     */
    public void setAcceptableUeis(List<String> acceptableUeis) {
        m_acceptableUeis = acceptableUeis;
    }

    /**
     * Gets the URI.
     *
     * @return the URI
     */
    public URI getURI() {
        try {
            return new URI(getScheme(), getUserInfo(), getHost(), getPort(), getPath(), getQuery(), getFragment());
        } catch (URISyntaxException e) {
            throw new NorthbounderException(e);
        }
    }

}
