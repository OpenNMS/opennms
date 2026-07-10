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
package org.opennms.core.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

public class HttpClientWrapperTest {

    @Test
    public void duplicatePreservesSslContextsAndHostnameVerification() throws Exception {
        final HttpClientWrapper wrapper = HttpClientWrapper.create()
                .setSSLContext("https", SSLContext.getDefault(), true);
        final HttpClientWrapper duplicate = wrapper.duplicate();
        // The wrapper offers no getters; its toString() includes the SSL configuration
        assertTrue("SSL context should be copied to the duplicate: " + duplicate,
                duplicate.toString().contains("sslContext={https="));
        assertTrue("Hostname verification should be copied to the duplicate: " + duplicate,
                duplicate.toString().contains("sslContextsWithHostnameVerification=[https]"));
    }

    @Test
    public void sameHostRedirectStrategyRefusesOtherHosts() throws Exception {
        assertFalse(isRedirected("https://scrape-target.example/metrics", "https://attacker.example/metrics"));
        assertTrue(isRedirected("https://scrape-target.example/metrics", "https://scrape-target.example/other"));
        assertTrue(isRedirected("https://scrape-target.example/metrics", "/relative/path"));
        assertFalse(isRedirected("https://scrape-target.example/metrics", "http://[unparseable"));
    }

    @Test
    public void sameHostRedirectStrategyRefusesOtherSchemesAndPorts() throws Exception {
        // A scheme downgrade would move credentials onto plain HTTP
        assertFalse(isRedirected("https://scrape-target.example/metrics", "http://scrape-target.example/metrics"));
        // A different port may be a different service on the same host
        assertFalse(isRedirected("https://scrape-target.example/metrics", "https://scrape-target.example:8443/metrics"));
        assertFalse(isRedirected("https://scrape-target.example:9100/metrics", "https://scrape-target.example/metrics"));
        // The default port for the scheme is the same service whether implicit or explicit
        assertTrue(isRedirected("https://scrape-target.example/metrics", "https://scrape-target.example:443/other"));
        assertTrue(isRedirected("https://scrape-target.example:9100/metrics", "https://scrape-target.example:9100/other"));
        // A scheme-relative location inherits the request scheme
        assertTrue(isRedirected("https://scrape-target.example/metrics", "//scrape-target.example/other"));
    }

    private static boolean isRedirected(final String requestUri, final String location) throws Exception {
        final HttpClientContext context = HttpClientContext.create();
        context.setTargetHost(HttpHost.create(requestUri.substring(0, requestUri.indexOf('/', 8))));

        final StatusLine statusLine = new BasicStatusLine(HttpVersion.HTTP_1_1, 302, "Found");
        final Header locationHeader = new BasicHeader("location", location);
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getFirstHeader("location")).thenReturn(locationHeader);

        return new HttpClientWrapper.SameHostRedirectStrategy()
                .isRedirected(new HttpGet(requestUri), response, context);
    }
}
