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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.collectd.HttpCollector.HttpCollectorException;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.Uri;
import org.opennms.netmgt.config.httpdatacollection.Url;

import com.sun.net.httpserver.HttpServer;

/**
 * Verifies that an HTTP 401/403 from a data endpoint surfaces as an
 * {@link HttpCollectorException} whose message carries the
 * {@code "auth failure:"} prefix the controller-side
 * {@code TokenAuthCollectorAdaptor} looks for. Without that rethrow,
 * the collector would only set FAILED on the CollectionSet and the
 * auth-only invalidation gate would never fire.
 */
public class HttpCollectorAuthFailureTest {

    private HttpServer server;
    private int port;
    private HttpCollector collector;
    private CollectionAgent agent;

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/unauth", ex -> {
            byte[] body = "denied".getBytes();
            ex.sendResponseHeaders(401, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext("/forbidden", ex -> {
            byte[] body = "nope".getBytes();
            ex.sendResponseHeaders(403, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
        port = server.getAddress().getPort();

        collector = new HttpCollector();
        agent = mock(CollectionAgent.class);
        when(agent.getNodeId()).thenReturn(42);
        when(agent.getAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void httpStatus401SurfacesAsAuthFailureException() {
        final Map<String, Object> params = paramsForSingleUri("/unauth");
        try {
            collector.collect(agent, params);
            fail("expected HttpCollectorException on HTTP 401");
        } catch (HttpCollectorException expected) {
            assertTrue("expected 'auth failure:' marker in message, got: " + expected.getMessage(),
                    expected.getMessage() != null
                            && expected.getMessage().contains("auth failure:"));
        }
    }

    @Test
    public void httpStatus403SurfacesAsAuthFailureException() {
        final Map<String, Object> params = paramsForSingleUri("/forbidden");
        try {
            collector.collect(agent, params);
            fail("expected HttpCollectorException on HTTP 403");
        } catch (HttpCollectorException expected) {
            assertTrue("expected 'auth failure:' marker in message, got: " + expected.getMessage(),
                    expected.getMessage() != null
                            && expected.getMessage().contains("auth failure:"));
        }
    }

    private Map<String, Object> paramsForSingleUri(final String path) {
        final Url url = new Url();
        url.setScheme("http");
        url.setHost("127.0.0.1");
        url.setPort(port);
        url.setPath(path);
        url.setMethod("GET");
        url.setHttpVersion("1.1");
        url.setMatches(".*");

        final Uri uriDef = new Uri();
        uriDef.setName("probe");
        uriDef.setUrl(url);

        final HttpCollection collection = new HttpCollection();
        collection.setName("auth-failure-test");
        collection.setUris(Collections.singletonList(uriDef));

        final Map<String, Object> params = new HashMap<>();
        // HTTP_COLLECTION_KEY is private to HttpCollector but its
        // value ("httpCollection") is part of the runtime contract
        // between getRuntimeAttributes() and collect().
        params.put("httpCollection", collection);
        return params;
    }
}
