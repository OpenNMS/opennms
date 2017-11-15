/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.flow;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.telemetry.adapters.netflow.Netflow5Converter;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.NetflowPacket;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.searchbox.client.AbstractJestClient;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.template.GetTemplate;

public class FlowStackIT {

    private static Logger LOG = LoggerFactory.getLogger(FlowStackIT.class);

    // TODO MVR duplicated from ClientFactory, should be merged or removed
    private static final Gson gson =  new GsonBuilder()
            .setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final String REST_URL = "rest/flows";

    private static int NETFLOW_LISTENER_UDP_PORT = 50000;

    private static final String TEMPLATE_NAME = "netflow";

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment
                    .builder()
                    .opennms()
                    .es5();
            // Enable flow adapter
            builder.withOpenNMSEnvironment()
                    .addFile(getClass().getResource("/flows/telemetryd-configuration.xml"), "etc/telemetryd-configuration.xml");

            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            return builder.build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    // Verifies that when OpenNMS and ElasticSearch is running and configured, that sending a flow packet
    // will actually be persisted in elastic
    @Test
    public void verifyFlowStack() throws Exception {
        // Determine endpoints
        final InetSocketAddress elasticRestAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.ELASTICSEARCH_5, 9200, "tcp");
        final InetSocketAddress opennmsWebAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8980);
        final InetSocketAddress opennmsSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8101);
        final InetSocketAddress opennmsNetflowAdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, NETFLOW_LISTENER_UDP_PORT, "udp");
        final String elasticRestUrl = String.format("http://%s:%d", elasticRestAddress.getHostString(), elasticRestAddress.getPort());

        // Configure OpenNMS
        setupOnmsContainer(opennmsSshAddress);

        // Build the Elastic Rest Client
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticRestUrl)
                .multiThreaded(true)
                .build());
        final JestClient client = factory.getObject();
        try {
            // Read netflow 5 packet
           sendNetflowPacket(opennmsNetflowAdapterAddress);

            // Ensure that the template has been created
            verify(client, (jestClient) -> {
                JestResult result = jestClient.execute(new GetTemplate.Builder(TEMPLATE_NAME).build());
                if (result.isSucceeded() && result.getJsonObject().get(TEMPLATE_NAME) != null) {
                    return true;
                }
                return false;
            });
            // Verify directly at elastic that the flows have been created
            verify(client, jestClient -> {
                SearchResult response = jestClient.execute(new Search.Builder("").addIndex("flow-*").build());
                if (response.isSucceeded() && response.getTotal() == 2) {
                    return true;
                }
                return false;
            });
        } finally {
            // JestClient 2.x does not support Autoclosable, so we close it manually.
            // Can be removed when updated to a later version
            if (client != null) {
                client.shutdownClient();
            }
        }

        // Verify via OpenNMS ReST API
        final String flowRestUrl = "http://" + opennmsWebAddress.getHostString().toString() + ":" + opennmsWebAddress.getPort() + "/opennms/" + REST_URL;
        final NetflowPacket netflowPacket = new NetflowPacket(ByteBuffer.wrap(getNetflowPacketContent()));
        final List<NetflowDocument> documents = new Netflow5Converter().convert(netflowPacket);
        documents.stream().forEach(d -> {
            d.setLocation("Default");
            d.setExporterAddress("127.0.0.1");
        });
        try (HttpClientWrapper restClient = createClientWrapper()) {
            // Persist flow
            HttpPut httpPut = new HttpPut(flowRestUrl);
            httpPut.addHeader("content-type", "application/json");
            httpPut.setEntity(new StringEntity(gson.toJson(documents)));
            CloseableHttpResponse response = restClient.execute(httpPut);
            assertEquals(202, response.getStatusLine().getStatusCode());

            // Wait 5 seconds, because it takes a while before elastic returns the data
            Thread.sleep(5000);

            // Query flows
            final HttpGet httpGet = new HttpGet(flowRestUrl);
            httpGet.addHeader("accept", "application/json");
            response = restClient.execute(httpGet);
            assertEquals(200, response.getStatusLine().getStatusCode());

            // Read response
            final Type listType = new TypeToken<ArrayList<NetflowDocument>>() {
            }.getType();
            List<NetflowDocument> netflowDocuments = gson.fromJson(new InputStreamReader(response.getEntity().getContent()), listType);
            assertEquals(4, netflowDocuments.size());

            // Proxy query
            final HttpPost httpPost = new HttpPost(flowRestUrl + "/proxy");
            httpPost.addHeader("content-type", "application/json");
            httpPost.addHeader("accept", "application/json");
            httpPost.setEntity(new StringEntity("{}"));
            response = restClient.execute(httpPost);
            assertEquals(200, response.getStatusLine().getStatusCode());

            // Verify response by checking that hits.hits exists
            final String json = EntityUtils.toString(response.getEntity());
            EntityUtils.consume(response.getEntity());
            final JsonObject jsonRoot = gson.fromJson(json, JsonObject.class);
            final JsonArray jsonArray = jsonRoot.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
            assertEquals(4, jsonArray.size());
        }
    }

    private byte[] getNetflowPacketContent() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/flows/netflow5.dat")) {
            final byte[] bytes = new byte[is.available()];
            ByteStreams.readFully(is, bytes);
            return bytes;
        }
    }

    // Sends a netflow Packet to the given destination address
    private void sendNetflowPacket(InetSocketAddress destinationAddress) throws IOException {
        byte[] bytes = getNetflowPacketContent();
        // now send to netflow 5 adapter
        try (DatagramSocket serverSocket = new DatagramSocket(0)) { // opens any free port
            final DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length,
                    destinationAddress.getAddress(), destinationAddress.getPort());
            serverSocket.send(sendPacket);
        }
    }

    // Configures the elastic bundle and also installs the flows feature to expose a NetflowRepository.
    // This is required, otherwise persisting does not work, as no FlowRepository implementation exists.
    private void setupOnmsContainer(InetSocketAddress opennmsSshAddress) throws Exception {
        try (final SshClient sshClient = new SshClient(opennmsSshAddress, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            // Update feature configuration to point url to elastic container
            pipe.println("config:edit org.opennms.features.flows.persistence.elastic");
            pipe.println("config:property-set elasticUrl http://elasticsearch:9200");
            pipe.println("config:update");

            pipe.println("feature:list");
            pipe.println("log:set INFO");
            pipe.println("logout");

            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    private static interface Block {

        boolean test(JestClient client) throws Exception;
    }

    // Helper method to execute the defined block n-times or fail if not successful
    private static void verify(JestClient jestClient, Block verifyCallback) {
        Objects.requireNonNull(jestClient);
        Objects.requireNonNull(verifyCallback);

        // Verify
        with().pollInterval(15, SECONDS).await().atMost(1, MINUTES).until(() -> {
            try {
                LOG.info("Querying elastic search");
                return verifyCallback.test(jestClient);
            } catch (Exception e) {
                LOG.error("Error while querying to elastic search", e);
            }
            return false;
        });
    }

    private static HttpClientWrapper createClientWrapper() {
        HttpClientWrapper wrapper = HttpClientWrapper.create();
        wrapper.addBasicCredentials(OpenNMSSeleniumTestCase.BASIC_AUTH_USERNAME, OpenNMSSeleniumTestCase.BASIC_AUTH_PASSWORD);
        return wrapper;
    }

}
