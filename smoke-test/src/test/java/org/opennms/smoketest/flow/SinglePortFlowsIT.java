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

package org.opennms.smoketest.flow;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.FlowPacket;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verifies that sending flow packets to a single port is dispatching the flows in the according queues.
 * See issue HZN-1270 for more details.
 */
public class SinglePortFlowsIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withTelemetryProcessing()
            .build());

    // Verifies that when OpenNMS and ElasticSearch is running and configured, that sending a flow packet
    // will actually be persisted in elastic
    @Test
    public void verifyFlowStack() throws Exception {
        final InetSocketAddress flowTelemetryAddress = stack.opennms().getNetworkProtocolAddress(NetworkProtocol.FLOWS);
        final InetSocketAddress opennmsWebAddress = stack.opennms().getWebAddress();
        final InetSocketAddress elasticRestAddress = InetSocketAddress.createUnresolved(
                stack.elastic().getContainerIpAddress(), stack.elastic().getMappedPort(9200));

        final FlowTester tester = new FlowTestBuilder()
                .withFlowPackets(Packets.getFlowPackets(), Sender.udp(flowTelemetryAddress))
                .verifyOpennmsRestEndpoint(opennmsWebAddress)
                .build(elasticRestAddress);
        tester.verifyFlows();
    }
}
