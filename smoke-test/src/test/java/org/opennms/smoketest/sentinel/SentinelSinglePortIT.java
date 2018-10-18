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

import static org.opennms.smoketest.flow.FlowStackIT.TEMPLATE_NAME;
import static org.opennms.smoketest.flow.FlowStackIT.sendNetflowPacket;
import static org.opennms.smoketest.flow.FlowStackIT.verify;

import java.net.InetSocketAddress;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.flow.FlowStackIT;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.template.GetTemplate;

// TODO MVR slightly rework this
// TODO MVR maybe reconsolidate with eisting tests
public class SentinelSinglePortIT {
    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    protected TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder()
                    .opennms()
                    .es6()
                    .minion()
                    .sentinel();

            builder.withMinionEnvironment()
                .addFile("/sentinel/org.opennms.features.telemetry.listeners-udp-50003-single-port.cfg", "etc/org.opennms.features.telemetry.listeners-udp-single-port.cfg");

            builder.withSentinelEnvironment()
                .addFile("/sentinel/features-jms", "deploy/features-jms"); // We re-use the features-jms.xml file here, as it should work as well

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
    public void verifySinglePort() throws Exception {
        // Determine endpoints
        final InetSocketAddress elasticRestAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.ELASTICSEARCH_6, 9200, "tcp");
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);
        final InetSocketAddress minionSinglePortAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.SFLOW_LISTENER_UDP_PORT, "udp");
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
            sendNetflowPacket(minionSinglePortAddress, "/flows/netflow5.dat"); // 2 records
            sendNetflowPacket(minionSinglePortAddress, "/flows/netflow9.dat"); // 7 records
            sendNetflowPacket(minionSinglePortAddress, "/flows/ipfix.dat"); // 6 records
            sendNetflowPacket(minionSinglePortAddress, "/flows/sflow.dat"); // 1 record

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
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> shellOutput.contains("Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-Netflow-5"));
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> shellOutput.contains("Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-Netflow-9"));
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> shellOutput.contains("Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-IPFIX"));
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> shellOutput.contains("Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-SFlow"));

    }
}

