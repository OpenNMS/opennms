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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.collection.test.CollectionSetUtils;
import org.opennms.core.web.SslContextFactory;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.PlatformTransactionManager;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/**
 * Verifies that the HttpCollector can present a client certificate (mutual TLS)
 * and validate the server against a custom trust anchor via service parameters,
 * and that the pre-existing relaxed SSL behavior is preserved when no TLS
 * parameters are set.
 */
public class HttpCollectorMutualTlsIT {

    private static final String RESPONSE_BODY = "There are 42 documents";

    private HttpsServer mutualTlsServer;
    private HttpsServer plainTlsServer;

    @Before
    public void setUp() throws Exception {
        final SSLContext serverContext = SslContextFactory.buildSslContext(
                tlsResource("server.p12"), "PKCS12", "server-store-pw", null,
                tlsResource("server-truststore.p12"), "PKCS12", "server-trust-pw");

        // Requires a client certificate
        mutualTlsServer = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        mutualTlsServer.setHttpsConfigurator(new HttpsConfigurator(serverContext) {
            @Override
            public void configure(final HttpsParameters params) {
                final SSLParameters sslParameters = getSSLContext().getDefaultSSLParameters();
                sslParameters.setNeedClientAuth(true);
                params.setSSLParameters(sslParameters);
            }
        });
        addDocumentHandler(mutualTlsServer);
        mutualTlsServer.start();

        // Self-signed certificate, no client certificate required
        plainTlsServer = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        plainTlsServer.setHttpsConfigurator(new HttpsConfigurator(serverContext));
        addDocumentHandler(plainTlsServer);
        plainTlsServer.start();
    }

    private static void addDocumentHandler(final HttpsServer server) {
        server.createContext("/documents", exchange -> {
            final byte[] body = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }

    @After
    public void tearDown() {
        if (mutualTlsServer != null) {
            mutualTlsServer.stop(0);
        }
        if (plainTlsServer != null) {
            plainTlsServer.stop(0);
        }
    }

    @Test
    public void canCollectWithClientCertificate() throws Exception {
        final CollectionSet collectionSet = collect(mutualTlsServer, mutualTlsParameters());
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        final List<String> collectionSetKeys = CollectionSetUtils.flatten(collectionSet);
        assertEquals(1, collectionSetKeys.size());
        assertTrue("Expected a documentCount attribute in " + collectionSetKeys.get(0),
                collectionSetKeys.get(0).contains("documentCount[null,42"));
    }

    @Test
    public void failsWithoutClientCertificate() throws Exception {
        final Map<String, Object> parameters = mutualTlsParameters();
        parameters.remove("key-store");
        parameters.remove("key-store-password");
        // The HttpCollector reports per-URI failures via the collection status
        final CollectionSet collectionSet = collect(mutualTlsServer, parameters);
        assertEquals(CollectionStatus.FAILED, collectionSet.getStatus());
    }

    @Test
    public void relaxedSslIsStillTheDefaultWithoutTlsParameters() throws Exception {
        // No TLS parameters: the self-signed server certificate must still be
        // accepted, as it was before mutual TLS support was added
        final CollectionSet collectionSet = collect(plainTlsServer, new HashMap<>());
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());
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

    private CollectionSet collect(final HttpsServer server, final Map<String, Object> parameters) throws Exception {
        initConfigFactory(server.getAddress().getPort());

        final OnmsNode node = mock(OnmsNode.class);
        final OnmsIpInterface iface = mock(OnmsIpInterface.class);
        when(iface.getNode()).thenReturn(node);
        when(iface.getIpAddress()).thenReturn(InetAddress.getByName("127.0.0.1"));

        final IpInterfaceDao ifaceDao = mock(IpInterfaceDao.class);
        when(ifaceDao.load(1)).thenReturn(iface);
        final PlatformTransactionManager transMgr = mock(PlatformTransactionManager.class);
        final CollectionAgent agent = DefaultCollectionAgent.create(1, ifaceDao, transMgr);

        final HttpCollector collector = new HttpCollector();
        parameters.put("http-collection", "mtls-collection");
        parameters.put("httpCollection", HttpCollectionConfigFactory.getInstance().getHttpCollection("mtls-collection"));

        return collector.collect(agent, parameters);
    }

    private static void initConfigFactory(final int port) throws Exception {
        final InputStream config = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<http-datacollection-config\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    rrdRepository=\"target/rrd/\" >\n" +
                "  <http-collection name=\"mtls-collection\">\n" +
                "    <rrd step=\"300\">\n" +
                "      <rra>RRA:AVERAGE:0.5:1:8928</rra>\n" +
                "    </rrd>\n" +
                "    <uris>\n" +
                "      <uri name=\"document-count\">\n" +
                "        <url scheme=\"https\" host=\"localhost\" port=\"" + port + "\"\n" +
                "             path=\"/documents\"\n" +
                "             matches=\".*?([0-9]+).*\" response-range=\"100-399\" />\n" +
                "        <attributes>\n" +
                "          <attrib alias=\"documentCount\" match-group=\"1\" type=\"counter32\"/>\n" +
                "        </attributes>\n" +
                "      </uri>\n" +
                "    </uris>\n" +
                "  </http-collection>\n" +
                "</http-datacollection-config>").getBytes(StandardCharsets.UTF_8));

        new HttpCollectionConfigFactory(config) {
            {{
                initialized = true;
                setInstance(this);
            }}
        };
    }

    private static String tlsResource(final String filename) {
        return Paths.get("src", "test", "resources", "tls", filename).toAbsolutePath().toString();
    }
}
