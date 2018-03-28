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
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.template.GetTemplate;

public class FlowStackIT {

    private static Logger LOG = LoggerFactory.getLogger(FlowStackIT.class);

    private static int NETFLOW5_LISTENER_UDP_PORT = 50000;
    private static int NETFLOW9_LISTENER_UDP_PORT = 50001;
    private static int IPFIX_LISTENER_UDP_PORT = 50002;
    private static int SFLOW_LISTENER_UDP_PORT = 50003;

    private static final String TEMPLATE_NAME = "netflow";

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private RestClient restClient;

    private final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().opennms().es5();
            // Enable flow adapter
            builder.withOpenNMSEnvironment().addFile(getClass().getResource("/flows/telemetryd-configuration.xml"), "etc/telemetryd-configuration.xml");

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
        final InetSocketAddress opennmsNetflow5AdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, NETFLOW5_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress opennmsNetflow9AdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, NETFLOW9_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress opennmsIpfixAdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, IPFIX_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress opennmsSflowAdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, SFLOW_LISTENER_UDP_PORT, "udp");
        final String elasticRestUrl = String.format("http://%s:%d", elasticRestAddress.getHostString(), elasticRestAddress.getPort());

        // Proxy the REST service
        restClient = new RestClient(opennmsWebAddress);

        // Configure OpenNMS
        setupOnmsContainer(opennmsSshAddress);

        // No flows should be present
        assertEquals(Long.valueOf(0L), restClient.getFlowCount(0L, System.currentTimeMillis()));

        // Build the Elastic Rest Client
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticRestUrl).multiThreaded(true).build());
        try (final JestClient client = factory.getObject()) {
            // Send packets
            sendNetflowPacket(opennmsNetflow5AdapterAddress, "/flows/netflow5.dat");
            sendNetflowPacket(opennmsNetflow9AdapterAddress, "/flows/netflow9.dat");
            sendNetflowPacket(opennmsIpfixAdapterAddress, "/flows/ipfix.dat");
            sendNetflowPacket(opennmsSflowAdapterAddress, "/flows/sflow.dat");

            // Ensure that the template has been created
            verify(client, (jestClient) -> {
                JestResult result = jestClient.execute(new GetTemplate.Builder(TEMPLATE_NAME).build());
                return result.isSucceeded() && result.getJsonObject().get(TEMPLATE_NAME) != null;
            });

            // Verify directly at elastic that the flows have been created
            verify(client, jestClient -> {
                SearchResult response = jestClient.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", response.isSucceeded() ? "Success" : "Failure", response.getTotal());
                return response.isSucceeded() && response.getTotal() == 16;
            });

            // Verify the flow count via the REST API
            with().pollInterval(15, SECONDS).await().atMost(1, MINUTES)
                    .until(() -> restClient.getFlowCount(0L, System.currentTimeMillis()), equalTo(16L));
        }
    }

    private byte[] getNetflowPacketContent(final String filename) throws IOException {
        try (final InputStream is = getClass().getResourceAsStream(filename)) {
            return ByteStreams.toByteArray(is);
        }
    }

    // Sends a netflow Packet to the given destination address
    private void sendNetflowPacket(final InetSocketAddress destinationAddress, final String filename) throws IOException {
        final byte[] bytes = getNetflowPacketContent(filename);
        try (DatagramSocket serverSocket = new DatagramSocket(0)) { // opens any free port
            final DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, destinationAddress.getAddress(), destinationAddress.getPort());
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

    private interface Block {

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

}
