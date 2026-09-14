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
package org.opennms.protocols.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.protocols.xml.collector.UrlFactory;

import com.sun.net.httpserver.HttpServer;

/**
 * Verifies that {@link HttpUrlConnection} surfaces HTTP failure statuses as
 * {@link IOException} rather than silently returning the error-response
 * body. This is the trigger that lets {@code TokenAuthCollectorAdaptor}
 * invalidate stale tokens on a 401 from the upstream backend.
 */
public class HttpUrlConnectionAuthFailureTest {

    private HttpServer server;
    private int port;

    @Before
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", ex -> {
            byte[] body = "{\"value\":42}".getBytes();
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext("/unauth", ex -> {
            byte[] body = "{\"error\":\"token rejected\"}".getBytes();
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(401, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext("/server-error", ex -> {
            ex.sendResponseHeaders(500, -1);
            ex.close();
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void success_returnsBody() throws Exception {
        final URL url = UrlFactory.getUrl("http://127.0.0.1:" + port + "/ok", null);
        final URLConnection c = url.openConnection();
        try (InputStream is = c.getInputStream()) {
            assertEquals("{\"value\":42}", IOUtils.toString(is, "UTF-8"));
        } finally {
            UrlFactory.disconnect(c);
        }
    }

    @Test
    public void unauthorized_throwsIOException_withAuthFailurePrefix() throws Exception {
        final URL url = UrlFactory.getUrl("http://127.0.0.1:" + port + "/unauth", null);
        final URLConnection c = url.openConnection();
        try {
            c.getInputStream();
            fail("expected IOException on HTTP 401");
        } catch (IOException expected) {
            final String chain = chainMessages(expected);
            assertTrue("expected 'auth failure:' prefix in cause chain, got: " + chain,
                    chain.contains("auth failure:"));
            assertTrue("message should reference the URL, got: " + chain,
                    chain.contains("/unauth"));
        } finally {
            UrlFactory.disconnect(c);
        }
    }

    @Test
    public void serverError_throwsIOException_withoutAuthFailurePrefix() throws Exception {
        final URL url = UrlFactory.getUrl("http://127.0.0.1:" + port + "/server-error", null);
        final URLConnection c = url.openConnection();
        try {
            c.getInputStream();
            fail("expected IOException on HTTP 500");
        } catch (IOException expected) {
            assertTrue("non-auth statuses must not get the auth prefix; got: " + chainMessages(expected),
                    !chainMessages(expected).contains("auth failure:"));
        } finally {
            UrlFactory.disconnect(c);
        }
    }

    private static String chainMessages(final Throwable t) {
        final StringBuilder sb = new StringBuilder();
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(cur.getMessage());
        }
        return sb.toString();
    }
}
