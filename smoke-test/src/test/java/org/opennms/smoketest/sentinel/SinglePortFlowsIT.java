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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.telemetry.FlowPacket;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Ports;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class SinglePortFlowsIT {

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
                .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50003-single-port.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-single-port.cfg");

            builder.withSentinelEnvironment()
                .addFile(getClass().getResource("/sentinel/features-jms.xml"), "deploy/features.xml"); // We re-use the features-jms.xml file here, as it should work as well

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
        final InetSocketAddress minionSinglePortAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, Ports.SFLOW_PORT, "udp");

        waitForSentinelStartup(sentinelSshAddress);

        // For each existing FlowPacket, create a definition to point to "minionSinglePortAddress"
        final List<FlowPacket> collect = Packets.getFlowPackets().stream()
                .map(p -> new FlowPacket(p.getResource(), p.getFlowCount(), minionSinglePortAddress))
                .collect(Collectors.toList());

        // Now verify Flow creation
        final FlowTester tester = new FlowTestBuilder()
                .withFlowPackets(collect)
                .verifyBeforeSendingFlows((flowTester) -> {
                    try {
                        final SearchResult response = flowTester.getJestClient().execute(new Search.Builder("").addIndex("netflow-*").build());
                        Assert.assertEquals(Boolean.TRUE, response.isSucceeded());
                        Assert.assertEquals(0L, response.getTotal().longValue());
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }
                })
                .build(elasticRestAddress);
        tester.verifyFlows();
    }

    // Wait for sentinel to start all queues
    private void waitForSentinelStartup(InetSocketAddress sentinelSshAddress) {
        final String QUEUE_FORMAT = "Route: Sink.Server.Telemetry-%s started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-%s";
        final List<String> queues = Lists.newArrayList("Netflow-5", "Netflow-9", "IPFIX", "SFlow");
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> {
            for (String eachQueue : queues) {
                final String logEntry = String.format(QUEUE_FORMAT, eachQueue, eachQueue);
                if (!shellOutput.contains(logEntry)) {
                    return false;
                }
            }
            return true;
        });
    }
}

