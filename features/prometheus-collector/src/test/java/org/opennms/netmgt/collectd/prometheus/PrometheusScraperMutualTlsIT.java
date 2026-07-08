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
package org.opennms.netmgt.collectd.prometheus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.walkers.MetricCollectingWalker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.web.SslContextFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * Verifies that the Prometheus scraper can present a client certificate (mutual TLS)
 * and validate the server against a custom trust anchor, driven purely by service
 * parameters. The server side is a plain JDK HttpsServer with needClientAuth enabled,
 * using a certificate that is only valid for the hostname "localhost".
 */
public class PrometheusScraperMutualTlsIT {

    private static final String METRICS_BODY = "# HELP node_load1 1m load average.\n"
            + "# TYPE node_load1 gauge\n"
            + "node_load1 0.58\n"
            + "# HELP node_load5 5m load average.\n"
            + "# TYPE node_load5 gauge\n"
            + "node_load5 0.36\n";

    private HttpsServer server;
    private HttpsServer otherServer;
    private final AtomicInteger otherServerHits = new AtomicInteger();

    @Before
    public void setUp() throws Exception {
        final SSLContext serverContext = SslContextFactory.buildSslContext(
                tlsResource("server.p12"), "PKCS12", "server-store-pw", null,
                tlsResource("server-truststore.p12"), "PKCS12", "server-trust-pw");

        // A second host (by address) that redirects should never reach; no client
        // certificate is required here so only the redirect strategy protects it
        otherServer = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        otherServer.setHttpsConfigurator(new HttpsConfigurator(serverContext));
        otherServer.createContext("/metrics", exchange -> {
            otherServerHits.incrementAndGet();
            serveMetrics(exchange);
        });
        otherServer.start();

        // Bind to 127.0.0.1 explicitly so the hostname-verification tests cannot be
        // satisfied by a connection failure on hosts where localhost resolves to ::1
        server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(serverContext) {
            @Override
            public void configure(final HttpsParameters params) {
                final SSLParameters sslParameters = getSSLContext().getDefaultSSLParameters();
                sslParameters.setNeedClientAuth(true);
                params.setSSLParameters(sslParameters);
            }
        });
        server.createContext("/metrics", this::serveMetrics);
        server.createContext("/redirect-same-host", exchange -> {
            exchange.getResponseHeaders().add("Location", "/metrics");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/redirect-other-host", exchange -> {
            exchange.getResponseHeaders().add("Location",
                    String.format("https://127.0.0.1:%d/metrics", otherServer.getAddress().getPort()));
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.start();
    }

    private void serveMetrics(final HttpExchange exchange) throws IOException {
        final byte[] body = METRICS_BODY.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        if (otherServer != null) {
            otherServer.stop(0);
        }
    }

    @Test
    public void canScrapeWithClientCertificate() throws IOException {
        final List<Metric> metrics = scrape("localhost", "/metrics", mutualTlsParameters());
        assertThat(metrics, hasSize(2));
        assertEquals("node_load1", metrics.get(0).getName());
    }

    @Test
    public void failsWithoutClientCertificate() {
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.remove("key-store");
        parameters.remove("key-store-password");
        // Not pinned to an SSL exception type: with TLS 1.3 the server's
        // certificate_required alert races the connection reset, so the client
        // may see either an SSLException or a plain SocketException
        expectScrapeFailure("localhost", parameters, IOException.class);
    }

    @Test
    public void failsWithoutCustomTrustAnchor() {
        // The default JVM trust anchors do not include our self-signed server certificate
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.remove("trust-store");
        parameters.remove("trust-store-password");
        expectScrapeFailure("localhost", parameters, SSLHandshakeException.class);
    }

    @Test
    public void failsOnHostnameMismatch() {
        // The server certificate is only valid for "localhost", not for the IP address
        expectScrapeFailure("127.0.0.1", mutualTlsParameters(), SSLPeerUnverifiedException.class);
    }

    @Test
    public void canDisableHostnameVerification() throws IOException {
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.put("hostname-verification", "false");
        final List<Metric> metrics = scrape("127.0.0.1", "/metrics", parameters);
        assertThat(metrics, hasSize(2));
    }

    @Test
    public void followsRedirectsOnTheSameHost() throws IOException {
        final List<Metric> metrics = scrape("localhost", "/redirect-same-host", mutualTlsParameters());
        assertThat(metrics, hasSize(2));
    }

    @Test
    public void doesNotPresentClientCertificateToOtherHostsOnRedirect() {
        // Disable hostname verification so the redirect strategy is the only thing
        // standing between the client certificate and the other host
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.put("hostname-verification", "false");
        try {
            final List<Metric> metrics = scrape("localhost", "/redirect-other-host", parameters);
            // The unfollowed 302 has no metrics payload
            assertThat(metrics, hasSize(0));
        } catch (IOException e) {
            // Also acceptable: an empty 302 response entity surfaces as an IOException
        }
        assertEquals("The redirect target must never be contacted", 0, otherServerHits.get());
    }

    private Map<String, Object> mutualTlsParameters() {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("key-store", tlsResource("client.p12"));
        parameters.put("key-store-password", "client-store-pw");
        parameters.put("trust-store", tlsResource("client-truststore.p12"));
        parameters.put("trust-store-password", "client-trust-pw");
        parameters.put("retry", "0");
        return parameters;
    }

    private List<Metric> scrape(final String host, final String path, final Map<String, Object> parameters) throws IOException {
        final URI uri = URI.create(String.format("https://%s:%d%s", host, server.getAddress().getPort(), path));
        final MetricCollectingWalker walker = new MetricCollectingWalker();
        PrometheusScraper.scrape(uri, parameters, walker);
        return walker.getMetrics();
    }

    private void expectScrapeFailure(final String host, final Map<String, Object> parameters, final Class<? extends IOException> expectedType) {
        try {
            scrape(host, "/metrics", parameters);
            fail("Expected the scrape to fail with " + expectedType.getSimpleName());
        } catch (IOException e) {
            assertTrue("Expected " + expectedType.getSimpleName() + " but got " + e, expectedType.isInstance(e));
        }
    }

    private static String tlsResource(final String filename) {
        return Paths.get("src", "test", "resources", "tls", filename).toAbsolutePath().toString();
    }
}
