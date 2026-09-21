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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Test;
import org.opennms.netmgt.config.httpdatacollection.Header;
import org.opennms.netmgt.config.httpdatacollection.Url;

/**
 * Verifies that headers configured on a {@link Url} make it onto the
 * outgoing {@link HttpRequestBase}. The actual HTTP send-and-receive
 * is exercised by the broader collector ITs; this test pins the
 * narrower invariant ("headers I configure show up in the request"),
 * which is the regression risk if {@link HttpCollector#buildHttpMethod}
 * is ever refactored.
 */
public class HttpCollectorHeaderTest {

    @Test
    public void appliesConfiguredHeadersToRequest() {
        final Url url = new Url();
        url.setPath("/whatever");
        url.setHeaders(Arrays.asList(
                new Header("Authorization", "Bearer some-token-value"),
                new Header("X-Custom", "abc")));

        final HttpRequestBase method = new HttpGet("http://localhost/whatever");

        HttpCollector.applyConfiguredHeaders(method, url);

        final org.apache.http.Header auth = method.getFirstHeader("Authorization");
        assertNotNull("Authorization header should have been set", auth);
        assertEquals("Bearer some-token-value", auth.getValue());

        final org.apache.http.Header custom = method.getFirstHeader("X-Custom");
        assertNotNull("X-Custom header should have been set", custom);
        assertEquals("abc", custom.getValue());
    }

    @Test
    public void noHeadersConfiguredLeavesRequestUntouched() {
        final Url url = new Url();
        url.setPath("/whatever");
        // Don't call setHeaders -- exercises the empty-list branch via
        // Url.getHeaders()'s null-tolerant default.

        final HttpRequestBase method = new HttpGet("http://localhost/whatever");
        HttpCollector.applyConfiguredHeaders(method, url);

        assertNull("no Authorization header should have been set",
                method.getFirstHeader("Authorization"));
    }

    @Test
    public void virtualHostStillSetWhenHeadersConfigured() {
        // Verifies the virtual-host header (existing behavior) is
        // not regressed by the new headers-loop.
        final Url url = new Url();
        url.setPath("/whatever");
        url.setVirtualHost("vhost.example.com");
        url.setHeaders(Collections.singletonList(
                new Header("Authorization", "Bearer x-y-z-1234567890")));

        final HttpRequestBase method = new HttpGet("http://localhost/whatever");
        HttpCollector.applyConfiguredHeaders(method, url);

        final org.apache.http.Header host = method.getFirstHeader("Host");
        assertNotNull("Host header should have been set from virtualHost", host);
        assertEquals("vhost.example.com", host.getValue());
        assertNotNull("Authorization header should also have been set",
                method.getFirstHeader("Authorization"));
    }

    @Test
    public void emptyHeaderNameIsSkipped() {
        final Url url = new Url();
        url.setPath("/whatever");
        // The XSD already requires name; exercise the defensive guard.
        // Build the Header via the no-arg constructor + reflection-style
        // direct field access (we can't via setName because that
        // rejects empty). Instead just construct a Header that sets
        // value but leaves name null.
        final Header h = new Header();
        h.setValue("only-value");
        url.setHeaders(Collections.singletonList(h));

        final HttpRequestBase method = new HttpGet("http://localhost/whatever");
        HttpCollector.applyConfiguredHeaders(method, url);

        // Apache HttpClient lets you query "" but the named-header
        // accessor returns null for an unset name. The point is that
        // applyConfiguredHeaders did not throw and did not set a
        // null-named header on the request.
        assertNull(method.getFirstHeader(""));
    }
}
