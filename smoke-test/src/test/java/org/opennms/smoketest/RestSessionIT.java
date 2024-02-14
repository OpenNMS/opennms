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
public class RestSessionIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(RestSessionIT.class);

    private Header[] queryUri(final String uri, final String header) throws IOException {
        final HttpGet httpGet = new HttpGet(getBaseUrlExternal() + uri);
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
