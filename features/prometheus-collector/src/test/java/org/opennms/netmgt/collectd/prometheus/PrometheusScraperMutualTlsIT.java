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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.walkers.MetricCollectingWalker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.web.SslContextFactory;

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
        server.createContext("/metrics", exchange -> {
            final byte[] body = METRICS_BODY.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
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
    public void canScrapeWithClientCertificate() throws IOException {
        final List<Metric> metrics = scrape("localhost", mutualTlsParameters());
        assertThat(metrics, hasSize(2));
        assertEquals("node_load1", metrics.get(0).getName());
    }

    @Test
    public void failsWithoutClientCertificate() {
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.remove("key-store");
        parameters.remove("key-store-password");
        expectScrapeFailure("localhost", parameters);
    }

    @Test
    public void failsWithoutCustomTrustAnchor() {
        // The default JVM trust anchors do not include our self-signed server certificate
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.remove("trust-store");
        parameters.remove("trust-store-password");
        expectScrapeFailure("localhost", parameters);
    }

    @Test
    public void failsOnHostnameMismatch() {
        // The server certificate is only valid for "localhost", not for the IP address
        expectScrapeFailure("127.0.0.1", mutualTlsParameters());
    }

    @Test
    public void canDisableHostnameVerification() throws IOException {
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.put("hostname-verification", "false");
        final List<Metric> metrics = scrape("127.0.0.1", parameters);
        assertThat(metrics, hasSize(2));
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

    private List<Metric> scrape(final String host, final Map<String, Object> parameters) throws IOException {
        final URI uri = URI.create(String.format("https://%s:%d/metrics", host, server.getAddress().getPort()));
        final MetricCollectingWalker walker = new MetricCollectingWalker();
        PrometheusScraper.scrape(uri, parameters, walker);
        return walker.getMetrics();
    }

    private void expectScrapeFailure(final String host, final Map<String, Object> parameters) {
        try {
            scrape(host, parameters);
            fail("Expected the scrape to fail with an IOException");
        } catch (IOException e) {
            // expected
        }
    }

    private static String tlsResource(final String filename) {
        return Paths.get("src", "test", "resources", "tls", filename).toAbsolutePath().toString();
    }
}
