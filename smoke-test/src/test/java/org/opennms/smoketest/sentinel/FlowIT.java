/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.sentinel;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.opennms.smoketest.flow.FlowStackIT.TEMPLATE_NAME;
import static org.opennms.smoketest.flow.FlowStackIT.sendNetflowPacket;
import static org.opennms.smoketest.flow.FlowStackIT.verify;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.flow.FlowStackIT;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.template.GetTemplate;

// Verifies that flows can be processed by a sentinel and are persisted to Elastic
public class FlowIT {

    private static final Logger LOG = LoggerFactory.getLogger(FlowIT.class);

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder()
                    .opennms()
                    .minion()
                    .es6()
                    .sentinel();
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);

            // Enable Netflow 5 Adapter
            builder.withSentinelEnvironment()
                    .addFile(getClass().getResource("/sentinel/features.xml"), "deploy/features.xml");

            // Enable Netflow 5 Listener
            builder.withMinionEnvironment()
                    .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50000.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-50000.cfg");
            return builder.build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void verifyFlowStack() throws Exception {
        // Determine endpoints
        final InetSocketAddress elasticRestAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.ELASTICSEARCH_6, 9200, "tcp");
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);
        final InetSocketAddress minionNetflowListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.NETFLOW5_LISTENER_UDP_PORT, "udp");
        final String elasticRestUrl = String.format("http://%s:%d", elasticRestAddress.getHostString(), elasticRestAddress.getPort());

        waitForNetflow5Route(sentinelSshAddress);

        // Build the Elastic Rest Client
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticRestUrl).multiThreaded(true).build());

        try (final JestClient client = factory.getObject()) {
            // Verify that at this point no flows are persisted
            verify(() -> {
                final SearchResult response = client.execute(new Search.Builder("").addIndex("netflow-*").build());
                return response.isSucceeded() && response.getTotal() == 0L;
            });

            // Send flow packet to minion
            sendNetflowPacket(minionNetflowListenerAddress, "/flows/netflow5.dat");

            // Ensure that the template has been created
            verify(() -> {
                final JestResult result = client.execute(new GetTemplate.Builder(TEMPLATE_NAME).build());
                return result.isSucceeded() && result.getJsonObject().get(TEMPLATE_NAME) != null;
            });

            // Verify that the flow has been created
            verify(() -> {
                final SearchResult response = client.execute(new Search.Builder("").addIndex("netflow-*").build());
                return response.isSucceeded() && response.getTotal() == 2L;
            });
        }
    }

    // Waits up to n minutes for the Telemetry-Netflow-5 Sink server to be present
    private void waitForNetflow5Route(InetSocketAddress sentinelSshAddress) throws Exception {
        try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
            final PrintStream pipe = sshClient.openShell();

            // Ensure we are actually started the sink and are ready to listen for messages
            await().atMost(5, MINUTES)
                    .pollInterval(15, SECONDS)
                    .until(() -> {
                            pipe.println("log:display");
                            final String shellOutput = sshClient.getStdout();
                            final boolean routeStarted = shellOutput.contains("Route: Sink.Server.Telemetry-Netflow-5 started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-Netflow-5");

                            LOG.info("log:display");
                            LOG.info("{}", shellOutput);
                            return routeStarted;
                        }
                    );
            LOG.info("logout");
            pipe.println("logout");
            await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
        }
    }
}
