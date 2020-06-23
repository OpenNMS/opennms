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

    private final List<FlowTester.Delivery> deliveries = new ArrayList<>();
    private final List<Consumer<FlowTester>> runBefore = new ArrayList<>();
    private final List<Consumer<FlowTester>> runAfter = new ArrayList<>();
    private InetSocketAddress opennmsWebAddress;

    public FlowTestBuilder withFlowPacket(final FlowPacket packet, final Sender sender) {
        this.deliveries.add(new FlowTester.Delivery(packet, sender));
        return this;
    }

    public FlowTestBuilder withFlowPackets(List<FlowTester.Delivery> deliveries) {
        this.deliveries.addAll(deliveries);
        return this;
    }

    public FlowTestBuilder withFlowPackets(List<FlowPacket> packets, final Sender sender) {
        packets.stream()
               .map(packet -> new FlowTester.Delivery(packet, sender))
               .forEach(this.deliveries::add);
        return this;
    }

    public FlowTestBuilder verifyOpennmsRestEndpoint(InetSocketAddress opennmsWebAddress) {
        this.opennmsWebAddress = Objects.requireNonNull(opennmsWebAddress);
        return this;
    }

    public FlowTestBuilder withNetflow5Packet(final Sender sender) {
        return withFlowPacket(Packets.Netflow5, sender);
    }

    public FlowTestBuilder withNetflow9Packet(final Sender sender) {
        return withFlowPacket(Packets.Netflow9, sender);
    }

    public FlowTestBuilder withIpfixPacket(final Sender sender) {
        return withFlowPacket(Packets.Ipfix, sender);
    }

    public FlowTestBuilder withSFlowPacket(final Sender sender) {
        return withFlowPacket(Packets.SFlow, sender);
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
        final FlowTester flowTester = new FlowTester(elasticAddress, opennmsWebAddress, deliveries);
        flowTester.setRunAfter(runAfter);
        flowTester.setRunBefore(runBefore);
        return flowTester;
    }

}
