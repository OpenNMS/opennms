/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ncs.northbounder;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.opennms.netmgt.alarmd.api.NorthbounderException;

/**
 * Configuration for HTTP NBI implementation.
 * FIXME: This needs lots of work.
 * FIXME: Make configuration mimic configuration of other HttpClient configurations: PSM, HttpCollector
 * so that users can reuse their configuration knowledge and not have to configured HTTP based client
 * configurations differently in every section of the software.
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
@XmlRootElement(name="ncs-northbounder-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class NCSNorthbounderConfig implements Serializable, Comparable<NCSNorthbounderConfig>{

    private static final long serialVersionUID = 1L;

    public static enum HttpMethod {
        POST {
                @Override
        	HttpEntityEnclosingRequestBase getRequestMethod(URI uri) {
        		return new HttpPost(uri);
        	}
        },
        PUT {
                @Override
        	HttpEntityEnclosingRequestBase getRequestMethod(URI uri) {
        		return new HttpPut(uri);
        	}
        };
        
        abstract HttpEntityEnclosingRequestBase getRequestMethod(URI uri);
    }
    
    @XmlAttribute(name="enabled", required=true)
    private boolean m_enabled = true;
    
    @XmlAttribute(name="nagles-delay", required=true)
    private long m_naglesDelay = 100;

    @XmlAttribute(name="method", required=false)
    private HttpMethod m_method = HttpMethod.POST;
    
    @XmlAttribute(name="http-version", required=false)
    private String m_httpVersion = "1.1";
    
    @XmlAttribute(name="user-agent", required=false)
    private String m_userAgent = "OpenNMS Http Northbound Interface";
    
    @XmlAttribute(name="virtual-host", required=false)
    private String m_virtualHost;
    
    @XmlAttribute(name="scheme", required=false)
    private String m_scheme = "http";
    
    @XmlAttribute(name="user-info", required=false)
    private String m_userInfo;
    
    @XmlAttribute(name="host", required=true)
    private String m_host;

    @XmlAttribute(name="port", required=false)
    private Integer m_port = Integer.valueOf(80);
    
    @XmlAttribute(name="path", required=false)
    private String m_path = "/";
    
    @XmlAttribute(name="query", required=false)
    private String m_query;
    
    @XmlAttribute(name="fragment", required=false)
    private String m_fragment;

    @XmlElement(name="uei")
    private List<String> m_acceptableUeis;

    
    @Override
    public int compareTo(NCSNorthbounderConfig o) {
        int c = 0;
        try {
            c = getURI().compareTo(o.getURI());
        } catch (NorthbounderException e) {
        }
        return c;
    }
    
    @Override
    public boolean equals(Object o) {
        boolean eq = false;
        if (o instanceof NCSNorthbounderConfig) {
            NCSNorthbounderConfig other = (NCSNorthbounderConfig) o;
            try {
                eq = getURI().equals(other.getURI());
            } catch (NorthbounderException e) {
                eq = false;
            }
        }
        return eq;
    }
    
    public boolean isEnabled() {
		return m_enabled;
	}

	public void setEnabled(boolean enabled) {
		m_enabled = enabled;
	}

	public HttpMethod getMethod() {
        return m_method;
    }

    public void setMethod(HttpMethod method) {
        m_method = method;
    }

    public String getHttpVersion() {
        return m_httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        m_httpVersion = httpVersion;
    }

    public String getUserAgent() {
        return m_userAgent;
    }

    public void setUserAgent(String userAgent) {
        m_userAgent = userAgent;
    }

    public String getVirtualHost() {
        return m_virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        m_virtualHost = virtualHost;
    }

    public String getScheme() {
        return m_scheme;
    }

    public void setScheme(String scheme) {
        m_scheme = scheme;
    }

    public String getUserInfo() {
        return m_userInfo;
    }

    public void setUserInfo(String userInfo) {
        m_userInfo = userInfo;
    }

    public String getHost() {
        return m_host;
    }

    public void setHost(String host) {
        m_host = host;
    }

    public Integer getPort() {
        return m_port;
    }

    public void setPort(Integer port) {
        m_port = port;
    }

    public String getPath() {
        return m_path;
    }

    public void setPath(String path) {
        m_path = path;
    }

    public String getQuery() {
        return m_query;
    }

    public void setQuery(String query) {
        m_query = query;
    }

    public String getFragment() {
        return m_fragment;
    }

    public void setFragment(String fragment) {
        m_fragment = fragment;
    }

    public long getNaglesDelay() {
		return m_naglesDelay;
	}

	public void setNaglesDelay(long naglesDelay) {
		m_naglesDelay = naglesDelay;
	}

	public List<String> getAcceptableUeis() {
        return m_acceptableUeis;
    }

    public void setAcceptableUeis(List<String> acceptableUeis) {
        m_acceptableUeis = acceptableUeis;
    }

    public URI getURI() {
        try {
            return new URI(getScheme(), getUserInfo(), getHost(), 
                    getPort(), getPath(), getQuery(), getFragment());
        } catch (URISyntaxException e) {
            throw new NorthbounderException(e);
        }
    }

}
