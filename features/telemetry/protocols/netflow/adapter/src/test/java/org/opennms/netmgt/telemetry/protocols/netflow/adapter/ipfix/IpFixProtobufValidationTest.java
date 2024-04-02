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
package org.opennms.netmgt.telemetry.protocols.netflow.adapter.ipfix;

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
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ParserBase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.IpFixMessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.TransportValueVisitor;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


/**
 * This test validates netflow protobuf values against json output.
 */
public class IpFixProtobufValidationTest {


    @BeforeClass
    public static void beforeClass() {
        System.setProperty("karaf.etc", "src/test/resources");
    }

    @Test
    public void canValidateIpFixFlowsWithJsonOutput() {
        // Generate flows from existing packet payloads
        List<Flow> flows = getFlowsForPayloadsInSession(false, "/flows/ipfix_test_1.dat",
                "/flows/ipfix_test_2.dat");
        assertThat(flows, hasSize(8));
        List<Flow> jsonData = Utils.getJsonFlowFromResources(Instant.now(),
                                                             "/flows/ipfix_test_1.json",
                                                             "/flows/ipfix_test_2.json");
        //assertThat(jsonData, hasSize(8));
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals(flows.get(i).getFlowSeqNum(), jsonData.get(i).getFlowSeqNum());
            Assert.assertEquals(flows.get(i).getFlowRecords(), jsonData.get(i).getFlowRecords());
            Assert.assertEquals(flows.get(i).getTimestamp(), jsonData.get(i).getTimestamp());
            Assert.assertEquals(flows.get(i).getBytes(), jsonData.get(i).getBytes());
            Assert.assertEquals(flows.get(i).getDirection(), jsonData.get(i).getDirection());
            Assert.assertEquals(flows.get(i).getFirstSwitched(), jsonData.get(i).getFirstSwitched());
            Assert.assertEquals(flows.get(i).getLastSwitched(), jsonData.get(i).getLastSwitched());
            Assert.assertEquals(flows.get(i).getDeltaSwitched(), jsonData.get(i).getDeltaSwitched());
            Assert.assertEquals(flows.get(i).getDstAddr(), jsonData.get(i).getDstAddr());
            Assert.assertEquals(flows.get(i).getDstAs(), jsonData.get(i).getDstAs());
            Assert.assertEquals(flows.get(i).getDstPort(), jsonData.get(i).getDstPort());
            Assert.assertEquals(flows.get(i).getDstMaskLen(), jsonData.get(i).getDstMaskLen());
            Assert.assertEquals(flows.get(i).getDstAddrHostname(), jsonData.get(i).getDstAddrHostname());
            Assert.assertEquals(flows.get(i).getSrcAddr(), jsonData.get(i).getSrcAddr());
            Assert.assertEquals(flows.get(i).getSrcAs(), jsonData.get(i).getSrcAs());
            Assert.assertEquals(flows.get(i).getSrcPort(), jsonData.get(i).getSrcPort());
            Assert.assertEquals(flows.get(i).getSrcAddrHostname(), jsonData.get(i).getSrcAddrHostname());
            Assert.assertEquals(flows.get(i).getSrcMaskLen(), jsonData.get(i).getSrcMaskLen());
            Assert.assertEquals(flows.get(i).getNextHop(), jsonData.get(i).getNextHop());
            Assert.assertEquals(flows.get(i).getInputSnmp(), jsonData.get(i).getInputSnmp());
            Assert.assertEquals(flows.get(i).getOutputSnmp(), jsonData.get(i).getOutputSnmp());
            Assert.assertEquals(flows.get(i).getNetflowVersion(), jsonData.get(i).getNetflowVersion());
            Assert.assertEquals(flows.get(i).getTcpFlags(), jsonData.get(i).getTcpFlags());
            Assert.assertEquals(flows.get(i).getProtocol(), jsonData.get(i).getProtocol());
            Assert.assertEquals(flows.get(i).getTos(), jsonData.get(i).getTos());
            Assert.assertEquals(flows.get(i).getEngineId(), jsonData.get(i).getEngineId());
            Assert.assertEquals(flows.get(i).getEngineType(), jsonData.get(i).getEngineType());
            Assert.assertEquals(flows.get(i).getPackets(), jsonData.get(i).getPackets());
            Assert.assertEquals(flows.get(i).getSamplingAlgorithm(), jsonData.get(i).getSamplingAlgorithm());
            Assert.assertEquals(flows.get(i).getSamplingInterval(), jsonData.get(i).getSamplingInterval());
            Assert.assertEquals(flows.get(i).getIpProtocolVersion(), jsonData.get(i).getIpProtocolVersion());
            Assert.assertEquals(flows.get(i).getVlan(), jsonData.get(i).getVlan());
            Assert.assertEquals(flows.get(i).getNodeIdentifier(), jsonData.get(i).getNodeIdentifier());

            Assert.assertEquals(0, flows.get(i).getRawMessage().size());
        }
    }

    @Test
    public void canValidateIpFixFlowsIncludingRawMessageWithJsonOutput() {
        // Generate flows from existing packet payloads
        List<Flow> flows = getFlowsForPayloadsInSession(true, "/flows/ipfix_test_1.dat",
                "/flows/ipfix_test_2.dat");
        assertThat(flows, hasSize(8));
        List<Flow> jsonData = Utils.getJsonFlowFromResources(Instant.now(),
                "/flows/ipfix_test_1.json",
                "/flows/ipfix_test_2.json");
        assertThat(jsonData, hasSize(8));
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals(flows.get(i).getFlowSeqNum(), jsonData.get(i).getFlowSeqNum());
            Assert.assertEquals(flows.get(i).getFlowRecords(), jsonData.get(i).getFlowRecords());
            Assert.assertEquals(flows.get(i).getTimestamp(), jsonData.get(i).getTimestamp());
            Assert.assertEquals(flows.get(i).getBytes(), jsonData.get(i).getBytes());
            Assert.assertEquals(flows.get(i).getDirection(), jsonData.get(i).getDirection());
            Assert.assertEquals(flows.get(i).getFirstSwitched(), jsonData.get(i).getFirstSwitched());
            Assert.assertEquals(flows.get(i).getLastSwitched(), jsonData.get(i).getLastSwitched());
            Assert.assertEquals(flows.get(i).getDeltaSwitched(), jsonData.get(i).getDeltaSwitched());
            Assert.assertEquals(flows.get(i).getDstAddr(), jsonData.get(i).getDstAddr());
            Assert.assertEquals(flows.get(i).getDstAs(), jsonData.get(i).getDstAs());
            Assert.assertEquals(flows.get(i).getDstPort(), jsonData.get(i).getDstPort());
            Assert.assertEquals(flows.get(i).getDstMaskLen(), jsonData.get(i).getDstMaskLen());
            Assert.assertEquals(flows.get(i).getDstAddrHostname(), jsonData.get(i).getDstAddrHostname());
            Assert.assertEquals(flows.get(i).getSrcAddr(), jsonData.get(i).getSrcAddr());
            Assert.assertEquals(flows.get(i).getSrcAs(), jsonData.get(i).getSrcAs());
            Assert.assertEquals(flows.get(i).getSrcPort(), jsonData.get(i).getSrcPort());
            Assert.assertEquals(flows.get(i).getSrcAddrHostname(), jsonData.get(i).getSrcAddrHostname());
            Assert.assertEquals(flows.get(i).getSrcMaskLen(), jsonData.get(i).getSrcMaskLen());
            Assert.assertEquals(flows.get(i).getNextHop(), jsonData.get(i).getNextHop());
            Assert.assertEquals(flows.get(i).getInputSnmp(), jsonData.get(i).getInputSnmp());
            Assert.assertEquals(flows.get(i).getOutputSnmp(), jsonData.get(i).getOutputSnmp());
            Assert.assertEquals(flows.get(i).getNetflowVersion(), jsonData.get(i).getNetflowVersion());
            Assert.assertEquals(flows.get(i).getTcpFlags(), jsonData.get(i).getTcpFlags());
            Assert.assertEquals(flows.get(i).getProtocol(), jsonData.get(i).getProtocol());
            Assert.assertEquals(flows.get(i).getTos(), jsonData.get(i).getTos());
            Assert.assertEquals(flows.get(i).getEngineId(), jsonData.get(i).getEngineId());
            Assert.assertEquals(flows.get(i).getEngineType(), jsonData.get(i).getEngineType());
            Assert.assertEquals(flows.get(i).getPackets(), jsonData.get(i).getPackets());
            Assert.assertEquals(flows.get(i).getSamplingAlgorithm(), jsonData.get(i).getSamplingAlgorithm());
            Assert.assertEquals(flows.get(i).getSamplingInterval(), jsonData.get(i).getSamplingInterval());
            Assert.assertEquals(flows.get(i).getIpProtocolVersion(), jsonData.get(i).getIpProtocolVersion());
            Assert.assertEquals(flows.get(i).getVlan(), jsonData.get(i).getVlan());
            Assert.assertEquals(flows.get(i).getNodeIdentifier(), jsonData.get(i).getNodeIdentifier());

            Assert.assertEquals(Long.valueOf(jsonData.get(i).getDstPort()), flows.get(i).getRawMessage().get("destinationTransportPort"));
            Assert.assertEquals(Long.valueOf(jsonData.get(i).getSrcPort()), flows.get(i).getRawMessage().get("sourceTransportPort"));
            Assert.assertEquals(jsonData.get(i).getDstAddr(), flows.get(i).getRawMessage().get("destinationIPv4Address"));
            Assert.assertEquals(jsonData.get(i).getSrcAddr(), flows.get(i).getRawMessage().get("sourceIPv4Address"));
            Assert.assertEquals(Long.valueOf(jsonData.get(i).getBytes()), flows.get(i).getRawMessage().get("octetDeltaCount"));
        }
    }

    private List<Flow> getFlowsForPayloadsInSession(final boolean includeRawMessage, String... resources) {
        final List<byte[]> payloads = new ArrayList<>(resources.length);
        for (String resource : resources) {
            URL resourceURL = getClass().getResource(resource);
            try {
                payloads.add(Files.readAllBytes(Paths.get(resourceURL.toURI())));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return getFlowsForPayloadsInSession(payloads, includeRawMessage);
    }

    private List<Flow> getFlowsForPayloadsInSession(List<byte[]> payloads, final boolean includeRawMessage) {
        final List<Flow> flows = new ArrayList<>();
        final Session session = new TcpSession(InetAddress.getLoopbackAddress(), () -> new SequenceNumberTracker(32));
        for (byte[] payload : payloads) {
            final ByteBuf buffer = Unpooled.wrappedBuffer(payload);
            final Header header;

            try {
                header = new Header(slice(buffer, Header.SIZE));
                final Packet packet = new Packet(session, header, slice(buffer, header.payloadLength()));
                packet.getRecords().forEach(rec -> {
                    final FlowMessage.Builder builder = new IpFixMessageBuilder().buildMessage(rec, (address) -> Optional.empty());
                    if (includeRawMessage) {
                        for (final Value<?> value : rec) {
                            final TransportValueVisitor transportValueVisitor = new TransportValueVisitor();
                            value.visit(transportValueVisitor);
                            builder.addRawMessage(transportValueVisitor.build());
                        }
                    }
                    FlowMessage flowMessage = builder.build();
                    flows.add(new NetflowMessage(flowMessage, Instant.now()));
                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }

        return flows;
    }
}
