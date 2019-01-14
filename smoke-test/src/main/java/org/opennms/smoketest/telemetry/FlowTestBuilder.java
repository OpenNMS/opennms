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

package org.opennms.smoketest.telemetry;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FlowTestBuilder {

    private final List<FlowPacket> packets = new ArrayList<>();
    private final List<Consumer<FlowTester>> runBefore = new ArrayList<>();
    private final List<Consumer<FlowTester>> runAfter = new ArrayList<>();
    private InetSocketAddress opennmsWebAddress;

    public FlowTestBuilder withFlowPacket(FlowPacket packet, InetSocketAddress sendToAddress) {
        final FlowPacket packetWithDestination = new FlowPacket(packet.getResource(), packet.getFlowCount(), sendToAddress);
        this.packets.add(packetWithDestination);
        return this;
    }

    public FlowTestBuilder withFlowPackets(List<FlowPacket> packets) {
        this.packets.clear();
        this.packets.addAll(packets);
        return this;
    }

    public FlowTestBuilder verifyOpennmsRestEndpoint(InetSocketAddress opennmsWebAddress) {
        this.opennmsWebAddress = Objects.requireNonNull(opennmsWebAddress);
        return this;
    }

    public FlowTestBuilder withNetflow5Packet(InetSocketAddress sendToAddress) {
        return withFlowPacket(Packets.Netflow5, sendToAddress);
    }

    public FlowTestBuilder withNetflow9Packet(InetSocketAddress sendToAddress) {
        return withFlowPacket(Packets.Netflow9, sendToAddress);
    }

    public FlowTestBuilder withIpfixPacket(InetSocketAddress sendToAddress) {
        return withFlowPacket(Packets.Ipfix, sendToAddress);
    }

    public FlowTestBuilder withSFlowPacket(InetSocketAddress sendToAddress) {
        return withFlowPacket(Packets.SFlow, sendToAddress);
    }

    public FlowTestBuilder verifyBeforeSendingFlows(Consumer<FlowTester> before) {
        this.runBefore.add(before);
        return this;
    }

    public FlowTestBuilder verifyAfterSendingFlows(Consumer<FlowTester> after) {
        this.runAfter.add(after);
        return this;
    }

    public FlowTester build(InetSocketAddress elasticAddress) {
        final FlowTester flowTester = new FlowTester(elasticAddress, opennmsWebAddress, packets);
        flowTester.setRunAfter(runAfter);
        flowTester.setRunBefore(runBefore);
        return flowTester;
    }

}
