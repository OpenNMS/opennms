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
package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow9;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.Utils;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetflowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


/**
 * This test validates netflow protobuf values against json output.
 */
public class Netflow9ProtobufValidationTest {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("karaf.etc", "src/test/resources");
    }

    @Test
    public void canValidateNetflow9FlowsWithJsonOutput() {
        // Generate flows from existing packet payloads
        List<Flow> flows = getFlowsForPayloadsInSession("/flows/netflow9.dat",
                "/flows/netflow9_template.dat",
                "/flows/netflow9_records.dat");

        assertThat(flows, hasSize(12));
        List<Flow> jsonFlows = Utils.getJsonFlowFromResources(Instant.now(),
                                                              "/flows/netflow9.json",
                                                              "/flows/netflow9_1.json");
        assertThat(jsonFlows, hasSize(12));
        for (int i = 0; i < 12; i++) {
            Assert.assertEquals(flows.get(i).getFlowSeqNum(), jsonFlows.get(i).getFlowSeqNum());
            Assert.assertEquals(flows.get(i).getFlowRecords(), jsonFlows.get(i).getFlowRecords());
            Assert.assertEquals(flows.get(i).getTimestamp(), jsonFlows.get(i).getTimestamp());
            Assert.assertEquals(flows.get(i).getBytes(), jsonFlows.get(i).getBytes());
            Assert.assertEquals(flows.get(i).getDirection(), jsonFlows.get(i).getDirection());
            Assert.assertEquals(flows.get(i).getFirstSwitched(), jsonFlows.get(i).getFirstSwitched());
            Assert.assertEquals(flows.get(i).getLastSwitched(), jsonFlows.get(i).getLastSwitched());
            Assert.assertEquals(flows.get(i).getDeltaSwitched(), jsonFlows.get(i).getDeltaSwitched());
            Assert.assertEquals(flows.get(i).getDstAddr(), jsonFlows.get(i).getDstAddr());
            Assert.assertEquals(flows.get(i).getDstAs(), jsonFlows.get(i).getDstAs());
            Assert.assertEquals(flows.get(i).getDstPort(), jsonFlows.get(i).getDstPort());
            Assert.assertEquals(flows.get(i).getDstMaskLen(), jsonFlows.get(i).getDstMaskLen());
            Assert.assertEquals(flows.get(i).getDstAddrHostname(), jsonFlows.get(i).getDstAddrHostname());
            Assert.assertEquals(flows.get(i).getSrcAddr(), jsonFlows.get(i).getSrcAddr());
            Assert.assertEquals(flows.get(i).getSrcAs(), jsonFlows.get(i).getSrcAs());
            Assert.assertEquals(flows.get(i).getSrcPort(), jsonFlows.get(i).getSrcPort());
            Assert.assertEquals(flows.get(i).getSrcAddrHostname(), jsonFlows.get(i).getSrcAddrHostname());
            Assert.assertEquals(flows.get(i).getSrcMaskLen(), jsonFlows.get(i).getSrcMaskLen());
            Assert.assertEquals(flows.get(i).getNextHop(), jsonFlows.get(i).getNextHop());
            Assert.assertEquals(flows.get(i).getInputSnmp(), jsonFlows.get(i).getInputSnmp());
            Assert.assertEquals(flows.get(i).getOutputSnmp(), jsonFlows.get(i).getOutputSnmp());
            Assert.assertEquals(flows.get(i).getNetflowVersion(), jsonFlows.get(i).getNetflowVersion());
            Assert.assertEquals(flows.get(i).getTcpFlags(), jsonFlows.get(i).getTcpFlags());
            Assert.assertEquals(flows.get(i).getProtocol(), jsonFlows.get(i).getProtocol());
            Assert.assertEquals(flows.get(i).getTos(), jsonFlows.get(i).getTos());
            Assert.assertEquals(flows.get(i).getEngineId(), jsonFlows.get(i).getEngineId());
            Assert.assertEquals(flows.get(i).getEngineType(), jsonFlows.get(i).getEngineType());
            Assert.assertEquals(flows.get(i).getPackets(), jsonFlows.get(i).getPackets());
            Assert.assertEquals(flows.get(i).getSamplingAlgorithm(), jsonFlows.get(i).getSamplingAlgorithm());
            Assert.assertEquals(flows.get(i).getSamplingInterval(), jsonFlows.get(i).getSamplingInterval());
            Assert.assertEquals(flows.get(i).getIpProtocolVersion(), jsonFlows.get(i).getIpProtocolVersion());
            Assert.assertEquals(flows.get(i).getVlan(), jsonFlows.get(i).getVlan());
            Assert.assertEquals(flows.get(i).getNodeIdentifier(), jsonFlows.get(i).getNodeIdentifier());

        }
    }


    private List<Flow> getFlowsForPayloadsInSession(String... resources) {
        final List<byte[]> payloads = new ArrayList<>(resources.length);
        for (String resource : resources) {
            URL resourceURL = getClass().getResource(resource);
            try {
                payloads.add(Files.readAllBytes(Paths.get(resourceURL.toURI())));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return getFlowsForPayloadsInSession(payloads);
    }

    private List<Flow> getFlowsForPayloadsInSession(List<byte[]> payloads) {
        final List<Flow> flows = new ArrayList<>();
        final Session session = new TcpSession(InetAddress.getLoopbackAddress(), () -> new SequenceNumberTracker(32));

        for (byte[] payload : payloads) {
            final ByteBuf buffer = Unpooled.wrappedBuffer(payload);
            final Header header;
            try {
                header = new Header(slice(buffer, Header.SIZE));
                final Packet packet = new Packet(session, header, buffer);
                packet.getRecords().forEach(rec -> {
                    final FlowMessage flowMessage = new Netflow9MessageBuilder().buildMessage(rec, (address) -> Optional.empty()).build();
                    flows.add(new NetflowMessage(flowMessage, Instant.now()));
                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }

        return flows;
    }
}
