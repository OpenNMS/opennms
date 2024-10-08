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
    private boolean m_useSystemProxy = false;

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
        if(m_useSystemProxy){
            m_httpClientWrapper.useSystemProxySettings();
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

    public void setUseSystemProxy(boolean useSystemProxy){
        m_useSystemProxy = useSystemProxy;
    }

}
