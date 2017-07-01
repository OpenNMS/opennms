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

package org.opennms.netmgt.provision.detector.web.client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.provision.detector.web.request.WebRequest;
import org.opennms.netmgt.provision.detector.web.response.WebResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>WebClient class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @author <A HREF="mailto:cliles@capario.com">Chris Liles</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class WebClient implements Client<WebRequest, WebResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(WebClient.class);

    private HttpClientWrapper m_httpClientWrapper = null;
    private HttpGet m_httpMethod;

    private HttpVersion m_version = HttpVersion.HTTP_1_1;
    private String m_schema;
    private String m_path;
    private String m_queryString;
    private String m_userAgent;
    private String m_virtualHost;
    private String m_userName;
    private String m_password;
    private boolean m_overrideSSL = false;
    private boolean m_authPreemptive = false;

    public WebClient(boolean override) {
        m_overrideSSL = override;
    }

    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        final URIBuilder ub = new URIBuilder();
        ub.setScheme(m_schema);
        ub.setHost(InetAddressUtils.str(address));
        ub.setPort(port);
        ub.setPath(m_path);
        if (m_queryString != null && m_queryString.trim().length() > 0) {
            final List<NameValuePair> params = URLEncodedUtils.parse(m_queryString, StandardCharsets.UTF_8);
            if (!params.isEmpty()) {
                ub.setParameters(params);
            }
        }

        m_httpMethod = new HttpGet(ub.build());
        m_httpMethod.setProtocolVersion(m_version);

        m_httpClientWrapper = HttpClientWrapper.create();
        if (m_overrideSSL) {
            try {
                m_httpClientWrapper.trustSelfSigned("https");
            } catch (final Exception e) {
                LOG.warn("Failed to create relaxed SSL client.", e);
            }
        }
        if (m_userAgent != null && !m_userAgent.trim().isEmpty()) {
            m_httpClientWrapper.setUserAgent(m_userAgent);
        }
        if (timeout > 0) {
            m_httpClientWrapper.setConnectionTimeout(timeout);
            m_httpClientWrapper.setSocketTimeout(timeout);
        }
        if (m_virtualHost != null && !m_virtualHost.trim().isEmpty()) {
            m_httpClientWrapper.setVirtualHost(m_virtualHost);
        }
        if (m_userName != null && !m_userName.trim().isEmpty()) {
            m_httpClientWrapper.addBasicCredentials(m_userName, m_password);
        }
        if (m_authPreemptive) {
            m_httpClientWrapper.usePreemptiveAuth();
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(m_httpClientWrapper);
        m_httpClientWrapper = null;
    }

    @Override
    public WebResponse receiveBanner() throws IOException, Exception {
        return null;
    }

    @Override
    public WebResponse sendRequest(final WebRequest request) throws IOException, Exception {
        for (final Entry<String,String> entry : request.getHeaders().entrySet()) {
            m_httpMethod.addHeader(entry.getKey(), entry.getValue());
        }
        CloseableHttpResponse response = null;
        try {
            response = m_httpClientWrapper.execute(m_httpMethod);
            return new WebResponse(request, response);
        } catch (final Exception e) {
            LOG.info(e.getMessage(), e);
            return new WebResponse(request, null);
        }
    }

    public void setPath(final String path) {
        m_path = path;
    }

    public void setQueryString(final String queryString) {
        m_queryString = queryString;
    }

    public void setSchema(final String schema) {
        m_schema = schema;        
    }

    public void setUserAgent(final String userAgent) {
        m_userAgent = userAgent;
    }

    public void setVirtualHost(final String virtualHost) {
        m_virtualHost = virtualHost;
    }

    public void setUseHttpV1(boolean useHttpV1) {
        if (useHttpV1) {
            m_version = HttpVersion.HTTP_1_0;
        }
    }

    public void setAuth(final String userName, final String password) {
        LOG.debug("enabling user authentication using credentials for {}", userName);
        m_userName = userName;
        m_password = password;
    }

    public void setAuthPreemtive(final boolean authPreemtive) {
        m_authPreemptive = authPreemtive;
    }

}
