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

package org.opennms.protocols.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.opennms.core.web.HttpClientWrapper;
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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(HttpUrlConnection.class);

    /** The URL. */
    private URL m_url;

    /** The Request. */
    private Request m_request;

    /** The HTTP Client. */
    private HttpClientWrapper m_clientWrapper;

    /**
     * Instantiates a new SFTP URL connection.
     *
     * @param url the URL
     * @param request 
     */
    protected HttpUrlConnection(final URL url, final Request request) {
        super(url);
        m_url = url;
        m_request = request;
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        if (m_clientWrapper != null) {
            return;
        }
        m_clientWrapper = HttpClientWrapper.create();
        if (m_request != null) {
            int timeout = m_request.getParameterAsInt("timeout");
            if (timeout > 0) {
                m_clientWrapper.setConnectionTimeout(timeout)
                    .setSocketTimeout(timeout);
            }

            int retries = m_request.getParameterAsInt("retries");
            if (retries == 0) {
                retries = m_request.getParameterAsInt("retry");
            }
            if (retries > 0) {
                m_clientWrapper.setRetries(retries);
            }

            String disableSslVerification = m_request.getParameter("disable-ssl-verification");
            if (Boolean.parseBoolean(disableSslVerification)) {
                try {
                    m_clientWrapper.useRelaxedSSL("https");
                } catch (final GeneralSecurityException e) {
                    LOG.warn("Failed to set up relaxed SSL.", e);
                }
            }
        }

        m_clientWrapper.addRequestInterceptor(new RequestAcceptEncoding())
            .addResponseInterceptor(new ResponseContentEncoding());

        // Add User Authentication
        String[] userInfo = m_url.getUserInfo() == null ? null :  m_url.getUserInfo().split(":");
        if (userInfo != null && userInfo.length == 2) {
            // If the URL contains a username/password, it might need to be decoded
            String uname = URLDecoder.decode(userInfo[0], StandardCharsets.UTF_8.name());
            String pwd = URLDecoder.decode(userInfo[1], StandardCharsets.UTF_8.name());
            m_clientWrapper.addBasicCredentials(uname, pwd);
        }

    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        try {
            if (m_clientWrapper == null) {
                connect();
            }

            // Build URL
            int port = m_url.getPort() > 0 ? m_url.getPort() : m_url.getDefaultPort();
            URIBuilder ub = new URIBuilder();
            ub.setPort(port);
            ub.setScheme(m_url.getProtocol());
            ub.setHost(m_url.getHost());
            ub.setPath(m_url.getPath());
            if (m_url.getQuery() != null && !m_url.getQuery().trim().isEmpty()) {
                final List<NameValuePair> params = URLEncodedUtils.parse(m_url.getQuery(), StandardCharsets.UTF_8);
                if (!params.isEmpty()) {
                    ub.addParameters(params);
                }
            }

            // Build Request
            HttpRequestBase request = null;
            if (m_request != null && m_request.getMethod().equalsIgnoreCase("post")) {
                final Content cnt = m_request.getContent();
                HttpPost post = new HttpPost(ub.build());
                ContentType contentType = ContentType.create(cnt.getType());
                LOG.info("Processing POST request for {}", contentType);
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
                for (final Header header : m_request.getHeaders()) {
                    request.addHeader(header.getName(), header.getValue());
                }
            }

            // Get Response
            CloseableHttpResponse response = m_clientWrapper.execute(request);
            return response.getEntity().getContent();
        } catch (Exception e) {
            throw new IOException("Can't retrieve " + m_url.getPath() + " from " + m_url.getHost() + " because " + e.getMessage(), e);
        }
    }

    /**
     * Disconnect
     */
    public void disconnect() {
        IOUtils.closeQuietly(m_clientWrapper);
        m_clientWrapper = null;
    }
}
