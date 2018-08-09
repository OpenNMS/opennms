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

public abstract class AbstractFlowIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract String getSentinelReadyString();

    protected abstract void customizeTestEnvironment(TestEnvironmentBuilder builder);

    protected TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder();

            // Enable Flow-Listeners
            builder.withMinionEnvironment()
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50000.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-50000.cfg")
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50001.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-50001.cfg")
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50002.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-50002.cfg")
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50003.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-50003.cfg")
            ;

            customizeTestEnvironment(builder);

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

    @Test
    public void verifyFlowStack() throws Exception {
        // Determine endpoints
        final InetSocketAddress elasticRestAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.ELASTICSEARCH_6, 9200, "tcp");
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);
        final InetSocketAddress minionNetflow5ListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.NETFLOW5_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress minionNetflow9ListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.NETFLOW9_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress minionIpfixListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.IPFIX_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress minionSflowListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.SFLOW_LISTENER_UDP_PORT, "udp");
        final String elasticRestUrl = String.format("http://%s:%d", elasticRestAddress.getHostString(), elasticRestAddress.getPort());

        waitForSentinelStartup(sentinelSshAddress);

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
            sendNetflowPacket(minionNetflow5ListenerAddress, "/flows/netflow5.dat"); // 2 records
            sendNetflowPacket(minionNetflow9ListenerAddress, "/flows/netflow9.dat"); // 7 records
            sendNetflowPacket(minionIpfixListenerAddress, "/flows/ipfix.dat"); // 6 records
            sendNetflowPacket(minionSflowListenerAddress, "/flows/sflow.dat"); // 1 record

            // Ensure that the template has been created
            verify(() -> {
                final JestResult result = client.execute(new GetTemplate.Builder(TEMPLATE_NAME).build());
                return result.isSucceeded() && result.getJsonObject().get(TEMPLATE_NAME) != null;
            });

            // Verify directly at elastic that the flows have been created
            verify(() -> {
                final SearchResult response = client.execute(new Search.Builder("").addIndex("netflow-*").build());
                return response.isSucceeded() && response.getTotal() == 16L;
            });
        }
    }

    private void waitForSentinelStartup(InetSocketAddress sentinelSshAddress) throws Exception {
        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("log:display");
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final String sentinelReadyString = getSentinelReadyString();
                        final boolean routeStarted = shellOutput.contains(sentinelReadyString);

                        logger.info("log:display");
                        logger.info("{}", shellOutput);
                        return routeStarted;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });
    }
}