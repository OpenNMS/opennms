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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.web.SslContextFactory;
import org.opennms.protocols.xml.config.Request;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * Verifies that the HTTP URL connection used by the XML and JSON collectors can
 * present a client certificate (mutual TLS) and validate the server against a
 * custom trust anchor, driven by request parameters from the collection config.
 */
public class HttpUrlConnectionMutualTlsIT {

    private static final String XML_BODY = "<data><count>42</count></data>";

    private HttpsServer server;

    @Before
    public void setUp() throws Exception {
        final SSLContext serverContext = SslContextFactory.buildSslContext(
                tlsResource("server.p12"), "PKCS12", "server-store-pw", null,
                tlsResource("server-truststore.p12"), "PKCS12", "server-trust-pw");

        server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(serverContext) {
            @Override
            public void configure(final HttpsParameters params) {
                final SSLParameters sslParameters = getSSLContext().getDefaultSSLParameters();
                sslParameters.setNeedClientAuth(true);
                params.setSSLParameters(sslParameters);
            }
        });
        server.createContext("/data.xml", exchange -> {
            final byte[] body = XML_BODY.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void canConnectWithClientCertificate() throws Exception {
        final HttpUrlConnection connection = new HttpUrlConnection(buildUrl(), mutualTlsRequest());
        try {
            try (InputStream is = connection.getInputStream()) {
                assertEquals(XML_BODY, IOUtils.toString(is, StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    @Test
    public void failsWithoutClientCertificate() throws Exception {
        final Request request = new Request();
        request.addParameter("trust-store", tlsResource("client-truststore.p12"));
        request.addParameter("trust-store-password", "client-trust-pw");

        final HttpUrlConnection connection = new HttpUrlConnection(buildUrl(), request);
        try {
            connection.getInputStream();
            fail("Expected the connection to fail with an IOException");
        } catch (IOException e) {
            // expected
        } finally {
            connection.disconnect();
        }
    }

    private URL buildUrl() throws Exception {
        return new URL(String.format("https://localhost:%d/data.xml", server.getAddress().getPort()));
    }

    private Request mutualTlsRequest() {
        final Request request = new Request();
        request.addParameter("key-store", tlsResource("client.p12"));
        request.addParameter("key-store-password", "client-store-pw");
        request.addParameter("trust-store", tlsResource("client-truststore.p12"));
        request.addParameter("trust-store-password", "client-trust-pw");
        return request;
    }

    private static String tlsResource(final String filename) {
        return Paths.get("src", "test", "resources", "tls", filename).toAbsolutePath().toString();
    }
}
