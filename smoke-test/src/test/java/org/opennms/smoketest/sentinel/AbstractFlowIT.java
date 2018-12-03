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

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.telemetry.Ports;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final InetSocketAddress minionNetflow5ListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, Ports.NETFLOW5_PORT, "udp");
        final InetSocketAddress minionNetflow9ListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, Ports.NETFLOW9_PORT, "udp");
        final InetSocketAddress minionIpfixListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, Ports.IPFIX_PORT, "udp");
        final InetSocketAddress minionSflowListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, Ports.SFLOW_PORT, "udp");

        waitForSentinelStartup(sentinelSshAddress);

        final FlowTester flowTester = new FlowTestBuilder()
                .withNetflow5Packet(minionNetflow5ListenerAddress)
                .withNetflow9Packet(minionNetflow9ListenerAddress)
                .withIpfixPacket(minionIpfixListenerAddress)
                .withSFlowPacket(minionSflowListenerAddress)
                .build(elasticRestAddress);

        flowTester.verifyFlows();
    }

    private void waitForSentinelStartup(InetSocketAddress sentinelSshAddress) {
        new KarafShell(sentinelSshAddress).verifyLog(shellOutput -> {
            final String sentinelReadyString = getSentinelReadyString();
            final boolean routeStarted = shellOutput.contains(sentinelReadyString);
            return routeStarted;
        });
    }
}