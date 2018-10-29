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
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Ports;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

public class FlowStackIT {

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().opennms().es5();
            // Enable flow adapter
            builder.withOpenNMSEnvironment().addFile(getClass().getResource("/flows/telemetryd-configuration.xml"), "etc/telemetryd-configuration.xml");
            builder.withOpenNMSEnvironment().addFile(getClass().getResource("/flows/org.opennms.features.flows.persistence.elastic.cfg"), "etc/org.opennms.features.flows.persistence.elastic.cfg");
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
        final InetSocketAddress opennmsNetflow5AdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, Ports.NETFLOW5_PORT, "udp");
        final InetSocketAddress opennmsNetflow9AdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, Ports.NETFLOW9_PORT, "udp");
        final InetSocketAddress opennmsIpfixAdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, Ports.IPFIX_PORT, "udp");
        final InetSocketAddress opennmsSflowAdapterAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, Ports.SFLOW_PORT, "udp");
        final InetSocketAddress elasticRestAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.ELASTICSEARCH_5, 9200, "tcp");
        final InetSocketAddress opennmsWebAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8980);

        final FlowTester flowTester = new FlowTestBuilder()
                .withFlowPacket(Packets.Netflow5, opennmsNetflow5AdapterAddress)
                .withFlowPacket(Packets.Netflow9, opennmsNetflow9AdapterAddress)
                .withFlowPacket(Packets.Ipfix, opennmsIpfixAdapterAddress)
                .withFlowPacket(Packets.SFlow, opennmsSflowAdapterAddress)
                .verifyOpennmsRestEndpoint(opennmsWebAddress)
                .build(elasticRestAddress);

        flowTester.verifyFlows();

    }
}
