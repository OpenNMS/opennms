/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.web.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.detector.web.request.WebRequest;
import org.opennms.netmgt.provision.detector.web.response.WebResponse;
import org.opennms.netmgt.provision.support.Client;

/**
 * <p>WebClient class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class WebClient implements Client<WebRequest, WebResponse> {

    private DefaultHttpClient m_httpClient;

    private HttpGet m_httpMethod;

    private String schema;

    private String path;

    public WebClient() {
        m_httpClient = new DefaultHttpClient();
    }

    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        URIBuilder ub = new URIBuilder();
        ub.setScheme(schema);
        ub.setHost(InetAddressUtils.str(address));
        ub.setPort(port);
        ub.setPath(path);
        m_httpMethod = new HttpGet(ub.build());
        setTimeout(timeout);
    }

    @Override
    public void close() {
        m_httpClient.getConnectionManager().shutdown();
    }

    @Override
    public WebResponse receiveBanner() throws IOException, Exception {
        return null;
    }

    @Override
    public WebResponse sendRequest(WebRequest request) throws IOException, Exception {
        for (Entry<String,String> entry : request.getHeaders().entrySet()) {
            m_httpMethod.addHeader(entry.getKey(), entry.getValue());
        }
        try {
            HttpResponse response = m_httpClient.execute(m_httpMethod);
            return new WebResponse(request, response);
        } catch (Exception e) {
            log().info(e.getMessage(), e);
            return new WebResponse(request, null);
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSchema(String schema) {
        this.schema = schema;        
    }

    public void setTimeout(int timeout) {
        if (timeout > 0) {
            m_httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
            m_httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
        }
    }

    public void setUserAgent(String userAgent) {
        m_httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
    }

    public void setVirtualHost(String virtualHost, int virtualPort) {
        if (virtualHost == null || virtualPort == 0)
            return;
        m_httpClient.getParams().setParameter(ClientPNames.VIRTUAL_HOST, new HttpHost(virtualHost, virtualPort));
    }

    public void setUseHttpV1(boolean useHttpV1) {
        if (useHttpV1) {
            m_httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
        }
    }

    public void setAuth(String userName, String password) {
        log().debug("enabling user authentication using credentials for " + userName);
        m_httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
    }

    public void setAuthPreemtive(boolean authPreemtive) {
        /**
         * Add an HttpRequestInterceptor that will perform preemptive authentication
         * @see http://hc.apache.org/httpcomponents-client-4.0.1/tutorial/html/authentication.html
         */
        HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws IOException {
                AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                CredentialsProvider credsProvider = (CredentialsProvider)context.getAttribute(ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                // If not authentication scheme has been initialized yet
                if (authState.getAuthScheme() == null) {
                    AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                    // Obtain credentials matching the target host
                    Credentials creds = credsProvider.getCredentials(authScope);
                    // If found, generate BasicScheme preemptively
                    if (creds != null) {
                        authState.update(new BasicScheme(), creds);
                    }
                }
            }

        };
        m_httpClient.addRequestInterceptor(preemptiveAuth, 0);
    }

    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
