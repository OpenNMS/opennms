/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
