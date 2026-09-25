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
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.opennms.core.web.HttpClientWrapper;

/**
 * Outbound HTTP through the shared {@link HttpClientWrapper}: system proxy
 * settings are honoured and the socket timeout also bounds stalls in the middle
 * of a download, which the JDK client's request timeout does not.
 */
final class ApacheHttpFetcher implements HttpFetcher {
    static final int CONNECT_TIMEOUT_MS = 10_000;
    static final int READ_TIMEOUT_MS = 30_000;

    private final HttpClientWrapper wrapper;
    private final CloseableHttpClient client;
    private final RequestConfig requestConfig;
    private final String userAgent;

    ApacheHttpFetcher(final String userAgent) {
        this.userAgent = userAgent;
        wrapper = HttpClientWrapper.create()
                .useSystemProxySettings()
                .setConnectionTimeout(CONNECT_TIMEOUT_MS)
                .setSocketTimeout(READ_TIMEOUT_MS)
                .setUserAgent(userAgent);
        client = wrapper.getClient();
        // Redirects are followed by the callers so that every hop's host is validated.
        requestConfig = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .setConnectTimeout(CONNECT_TIMEOUT_MS)
                .setConnectionRequestTimeout(CONNECT_TIMEOUT_MS)
                .setSocketTimeout(READ_TIMEOUT_MS)
                .build();
    }

    @Override
    public Response get(final URI uri, final Map<String, String> headers) throws IOException {
        final HttpGet get = new HttpGet(uri);
        // HttpClientWrapper.execute() re-wraps the request and loses this per-request config.
        get.setConfig(requestConfig);
        get.setHeader("User-Agent", userAgent);
        if (headers != null) {
            headers.forEach(get::setHeader);
        }
        final CloseableHttpResponse response = client.execute(get);
        try {
            final Map<String, String> responseHeaders = new HashMap<>();
            for (final Header header : response.getAllHeaders()) {
                responseHeaders.putIfAbsent(header.getName(), header.getValue());
            }
            final HttpEntity entity = response.getEntity();
            final InputStream body = entity == null ? null : entity.getContent();
            return new Response(response.getStatusLine().getStatusCode(), responseHeaders, body, response);
        } catch (final IOException | RuntimeException e) {
            response.close();
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        wrapper.close();
    }
}
