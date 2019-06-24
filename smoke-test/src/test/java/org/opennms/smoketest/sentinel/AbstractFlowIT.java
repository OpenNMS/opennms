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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.NetworkProtocol;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.stacks.TimeSeriesStrategy;
import org.opennms.smoketest.telemetry.FlowTestBuilder;
import org.opennms.smoketest.telemetry.FlowTester;
import org.opennms.smoketest.utils.KarafShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFlowIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @Rule
    public final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinel()
            .withIpcStrategy(getIpcStrategy())
            .withTimeSeriesStrategy(TimeSeriesStrategy.NEWTS)
            .withTelemetryProcessing()
            .build());

    protected abstract IpcStrategy getIpcStrategy();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract String getSentinelReadyString();

    @Test
    public void verifyFlowStack() throws Exception {
        // Determine endpoints
        final InetSocketAddress elasticRestAddress = InetSocketAddress.createUnresolved(stack.elastic().getContainerIpAddress(), stack.elastic().getMappedPort(9200));
        final InetSocketAddress sentinelSshAddress = stack.sentinel().getSshAddress();
        final InetSocketAddress minionFlowAddress = stack.minion().getNetworkProtocolAddress(NetworkProtocol.FLOWS);

        waitForSentinelStartup(sentinelSshAddress);

        final FlowTester flowTester = new FlowTestBuilder()
                .withNetflow5Packet(minionFlowAddress)
                .withNetflow9Packet(minionFlowAddress)
                .withIpfixPacket(minionFlowAddress)
                .withSFlowPacket(minionFlowAddress)
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