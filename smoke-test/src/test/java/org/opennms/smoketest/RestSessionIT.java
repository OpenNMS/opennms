/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to verify that a session will not be created by rest requests
 *
 * @author cpape
 */
public class RestSessionIT extends OpenNMSSeleniumTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(RestSessionIT.class);

    private Header[] queryUri(final String uri, final String header) throws IOException {
        final HttpGet httpGet = new HttpGet(getBaseUrl() + uri);
        final HttpHost targetHost = new HttpHost(httpGet.getURI().getHost(), httpGet.getURI().getPort(), httpGet.getURI().getScheme());
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()), new UsernamePasswordCredentials("admin", "admin"));
        final AuthCache authCache = new BasicAuthCache();
        final BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        final CloseableHttpResponse closeableHttpResponse = HttpClients.createDefault().execute(targetHost, httpGet, context);
        closeableHttpResponse.close();
        return closeableHttpResponse.getHeaders(header);
    }

    /**
     * Verifies that session will not be created by calls to the ReST V1 Api.
     * <p>
     * See NMS-8093.
     */
    @Test
    public void checkRestV1Api() throws ClientProtocolException, IOException, InterruptedException {
        final String uri = "/opennms/rest/nodes";
        LOG.info("Checking for existing Set-Cookie header of response from V1 ReST Api '{}'", uri);
        final Header[] headers = queryUri(uri, "Set-Cookie");
        for (final Header header : headers){
            LOG.error("Set-Cookie header found with value '{}'", header.getValue());
        }
        Assert.assertEquals(0, headers.length);
    }

    /**
     * Verifies that session will not be created by calls to the ReST V2 Api.
     * <p>
     * See NMS-8093.
     */
    @Test
    public void checkRestV2Api() throws ClientProtocolException, IOException, InterruptedException {
        final String uri = "/opennms/api/v2/nodes";
        LOG.info("Checking for existing Set-Cookie header of response from V2 ReST Api '{}'", uri);
        final Header[] headers = queryUri(uri, "Set-Cookie");
        for (final Header header : headers){
            LOG.error("Set-Cookie header found with value '{}'", header.getValue());
        }
        Assert.assertEquals(0, headers.length);
    }
}
