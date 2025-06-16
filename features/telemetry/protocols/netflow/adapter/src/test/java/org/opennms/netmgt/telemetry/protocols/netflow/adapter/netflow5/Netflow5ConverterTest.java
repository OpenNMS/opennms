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
package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.SamplingAlgorithm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetflowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow5MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Netflow5ConverterTest {

    @Test
    public void canParseNetflow5Flows() {
        // Generate flows from existing packet payloads
        List<Flow> flows = getFlowsForPayloadsInSession("/flows/netflow5.dat");
        MatcherAssert.assertThat(flows, hasSize(2));

        final Flow flow = flows.get(0);
        assertThat(flow.getNetflowVersion(), is((NetflowVersion.V5)));
        assertThat(flow.getFlowRecords(), is(2));
        assertThat(flow.getFlowSeqNum(), is(0L));
        assertThat(flow.getEngineId(), is(0));
        assertThat(flow.getEngineType(), is(0));
        assertThat(flow.getSamplingInterval(), is(0.0));
        assertThat(flow.getSamplingAlgorithm(), is(SamplingAlgorithm.Unassigned));
        MatcherAssert.assertThat(flow.getSrcAddr(), equalTo("10.0.2.2"));
        MatcherAssert.assertThat(flow.getSrcAddrHostname(), equalTo(Optional.empty()));
        MatcherAssert.assertThat(flow.getSrcPort(), equalTo(54435));
        MatcherAssert.assertThat(flow.getSrcMaskLen(), equalTo(0));
        MatcherAssert.assertThat(flow.getDstAddr(), equalTo("10.0.2.15"));
        MatcherAssert.assertThat(flow.getDstAddrHostname(), equalTo(Optional.empty()));
        MatcherAssert.assertThat(flow.getDstPort(), equalTo(22));
        MatcherAssert.assertThat(flow.getDstMaskLen(), equalTo(0));
        MatcherAssert.assertThat(flow.getTcpFlags(), equalTo(16));
        MatcherAssert.assertThat(flow.getProtocol(), equalTo(6)); // TCP
        MatcherAssert.assertThat(flow.getBytes(), equalTo(230L));
        MatcherAssert.assertThat(flow.getInputSnmp(), equalTo(0));
        MatcherAssert.assertThat(flow.getOutputSnmp(), equalTo(0));
        MatcherAssert.assertThat(flow.getFirstSwitched(), equalTo(Instant.ofEpochMilli(1430608661859L)));
        MatcherAssert.assertThat(flow.getLastSwitched(), equalTo(Instant.ofEpochMilli(1434870077556L)));
        MatcherAssert.assertThat(flow.getDeltaSwitched(), equalTo(Instant.ofEpochMilli(1430608661859L)));
        MatcherAssert.assertThat(flow.getPackets(), equalTo(5L));
        MatcherAssert.assertThat(flow.getDirection(), equalTo(Direction.INGRESS));
        MatcherAssert.assertThat(flow.getNextHop(), equalTo("0.0.0.0"));
        MatcherAssert.assertThat(flow.getNextHopHostname(), equalTo(Optional.empty()));
        MatcherAssert.assertThat(flow.getVlan(), nullValue());
    }

    private List<Flow> getFlowsForPayloadsInSession(String... resources) {
        final List<byte[]> payloads = new ArrayList<>(resources.length);
        for (String resource : resources) {
            URL resourceURL = getClass().getResource(resource);
            try {
                payloads.add(Files.readAllBytes(Paths.get(resourceURL.toURI())));
            } catch (IOException|URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return getFlowsForPayloadsInSession(payloads);
    }

    private List<Flow> getFlowsForPayloadsInSession(List<byte[]> payloads) {
        final List<Flow> flows = new ArrayList<>();
        for (byte[] payload : payloads) {
            final ByteBuf buffer = Unpooled.wrappedBuffer(payload);
            final Header header;
            try {
                header = new Header(slice(buffer, Header.SIZE));
                final Packet packet = new Packet(header, buffer);
                packet.getRecords().forEach(rec -> {
                    final FlowMessage flowMessage = new Netflow5MessageBuilder().buildMessage(rec, (address) -> Optional.empty()).build();
                    flows.add(new NetflowMessage(flowMessage, Instant.now()));
                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }
        return flows;
    }
}
