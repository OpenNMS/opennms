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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.telemetry.FlowPacket;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.telemetry.Sender;

import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class SinglePortFlowsIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinel()
            .withTelemetryProcessing()
            .withIpcStrategy(IpcStrategy.KAFKA)
            .build());

    @Test
    public void verifySinglePort() throws Exception {
        // Determine endpoints
        final InetSocketAddress elasticRestAddress = InetSocketAddress.createUnresolved(
                stack.elastic().getContainerIpAddress(), stack.elastic().getMappedPort(9200));
        final InetSocketAddress flowTelemetryAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.FLOWS);

        // Now verify Flow creation
        final FlowTester tester = new FlowTestBuilder()
                .withFlowPackets(Packets.getFlowPackets(), Sender.udp(flowTelemetryAddress))
                .verifyBeforeSendingFlows((flowTester) -> {
                    try {
                        final SearchResult response = flowTester.getJestClient().execute(
                                new Search.Builder("")
                                        .addIndex("netflow-*")
                                        .build());
                        assertEquals(Boolean.TRUE, response.isSucceeded());
                        assertEquals(0L, SearchResultUtils.getTotal(response));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build(elasticRestAddress);
        tester.verifyFlows();
    }

}
