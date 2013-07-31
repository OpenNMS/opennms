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

package org.opennms.protocols.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.opennms.core.utils.EmptyKeyRelaxedTrustSSLContext;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.xml.config.Content;
import org.opennms.protocols.xml.config.Header;
import org.opennms.protocols.xml.config.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class for managing HTTP URL Connection using Apache HTTP Client
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HttpUrlConnection extends URLConnection {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUrlConnection.class);

    /** The URL. */
    private URL m_url;

    /** The Request. */
    private Request m_request;

    /** The HTTP Client. */
    private DefaultHttpClient m_client;

    /**
     * Instantiates a new SFTP URL connection.
     *
     * @param url the URL
     * @param request 
     */
    protected HttpUrlConnection(URL url, Request request) {
        super(url);
        m_url = url;
        m_request = request;
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        if (m_client != null) {
            return;
        }
        final HttpParams httpParams = new BasicHttpParams();
        if (m_request != null) {
            int timeout = m_request.getParameterAsInt("timeout");
            if (timeout > 0) {
                HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
                HttpConnectionParams.setSoTimeout(httpParams, timeout);
            }
        }
        m_client = new DefaultHttpClient(httpParams);
        m_client.addRequestInterceptor(new RequestAcceptEncoding());
        m_client.addResponseInterceptor(new ResponseContentEncoding());
        if (m_request != null) {
            int retries = m_request.getParameterAsInt("retries");
            if (retries > 0) {
                m_client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler() {
                    @Override
                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                        if (executionCount <= getRetryCount() && (exception instanceof SocketTimeoutException || exception instanceof ConnectTimeoutException)) {
                            return true;
                        }
                        return super.retryRequest(exception, executionCount, context);
                    }
                });
            }
            String disableSslVerification = m_request.getParameter("disable-ssl-verification");
            if (Boolean.getBoolean(disableSslVerification)) {
                final SchemeRegistry registry = m_client.getConnectionManager().getSchemeRegistry();
                final Scheme https = registry.getScheme("https");
                try {
                    SSLSocketFactory factory = new SSLSocketFactory(SSLContext.getInstance(EmptyKeyRelaxedTrustSSLContext.ALGORITHM), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    final Scheme lenient = new Scheme(https.getName(), https.getDefaultPort(), factory);
                    registry.register(lenient);
                } catch (NoSuchAlgorithmException e) {
                    LOG.warn(e.getMessage());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            if (m_client == null) {
                connect();
            }
            // Build URL
            int port = m_url.getPort() > 0 ? m_url.getPort() : m_url.getDefaultPort();
            URIBuilder ub = new URIBuilder();
            ub.setPort(port);
            ub.setScheme(m_url.getProtocol());
            ub.setHost(m_url.getHost());
            ub.setPath(m_url.getPath());
            ub.setQuery(m_url.getQuery());
            // Build Request
            HttpRequestBase request = null;
            if (m_request != null && m_request.getMethod().equalsIgnoreCase("post")) {
                final Content cnt = m_request.getContent();
                HttpPost post = new HttpPost(ub.build());
                ContentType contentType = ContentType.create(cnt.getType());
                LOG.info("Processing POST request for %s", contentType);
                if (contentType.getMimeType().equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
                    FormFields fields = JaxbUtils.unmarshal(FormFields.class, cnt.getData());
                    post.setEntity(fields.getEntity());
                } else {
                    StringEntity entity = new StringEntity(cnt.getData(), contentType);
                    post.setEntity(entity);
                }
                request = post;
            } else {
                request = new HttpGet(ub.build());
            }
            if (m_request != null) {
                // Add Custom Headers
                for (Header header : m_request.getHeaders()) {
                    request.addHeader(header.getName(), header.getValue());
                }
            }
            // Add User Authentication
            String[] userInfo = m_url.getUserInfo() == null ? null :  m_url.getUserInfo().split(":");
            if (userInfo != null) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userInfo[0], userInfo[1]);
                request.addHeader(BasicScheme.authenticate(credentials, "UTF-8", false));
            }
            // Get Response
            HttpResponse response = m_client.execute(request);
            return response.getEntity().getContent();
        } catch (Exception e) {
            throw new IOException("Can't retrieve " + m_url.getPath() + " from " + m_url.getHost() + " because " + e.getMessage(), e);
        }
    }

    /**
     * Disconnect
     */
    public void disconnect() {
        if (m_client != null) {
            m_client.getConnectionManager().shutdown();
        }
    }

    /**
     * Log.
     *
     * @return the thread category
     */

}
