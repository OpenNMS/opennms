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
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.opennms.core.utils.ThreadCategory;

/**
 * The class for managing HTTP URL Connection using Apache HTTP Client
 * 
 * TODO Pending features:
 * 
 * 1) Support for HTTPS
 * 2) Support for POST with different content types
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HttpUrlConnection extends URLConnection {

    /** The URL. */
    private URL m_url;

    /** The HTTP Client. */
    private DefaultHttpClient m_client;

    /**
     * Instantiates a new SFTP URL connection.
     *
     * @param url the URL
     */
    protected HttpUrlConnection(URL url) {
        super(url);
        m_url = url;
    }

    /* (non-Javadoc)
     * @see java.net.URLConnection#connect()
     */
    @Override
    public void connect() throws IOException {
        if (m_client != null) {
            return;
        }
        m_client = new DefaultHttpClient();
        m_client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context)
                    throws HttpException, IOException {
                if (!request.containsHeader("Accept-Encoding")) {
                    request.addHeader("Accept-Encoding", "gzip");
                }
            }
        });
        m_client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context)
                    throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Header ceheader = entity.getContentEncoding();
                    if (ceheader != null) {
                        HeaderElement[] codecs = ceheader.getElements();
                        for (int i = 0; i < codecs.length; i++) {
                            if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                return;
                            }
                        }
                    }
                }
            }
        });
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
            int port = m_url.getPort() > 0 ? m_url.getPort() : m_url.getDefaultPort();
            String[] userInfo = m_url.getUserInfo() == null ? null :  m_url.getUserInfo().split(":");

            HttpGet request = new HttpGet(URIUtils.createURI(m_url.getProtocol(), m_url.getHost(), port, m_url.getPath(), m_url.getQuery(), null));
            if (userInfo != null) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userInfo[0], userInfo[1]);
                request.addHeader(BasicScheme.authenticate(credentials, "UTF-8", false));
            }
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
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
