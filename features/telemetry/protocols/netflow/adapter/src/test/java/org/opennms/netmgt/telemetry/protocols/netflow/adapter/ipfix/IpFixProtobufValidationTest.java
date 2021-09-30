/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.Utils;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.common.NetflowConverter;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


/**
 * This test validates netflow protobuf values against json output.
 */
public class IpFixProtobufValidationTest {

    private Converter ipFixConverter;

    @Test
    public void canValidateIpFixFlowsWithJsonOutput() {
        // Generate flows from existing packet payloads
        ipFixConverter = new NetflowConverter();
        List<Flow> flows = getFlowsForPayloadsInSession("/flows/ipfix_test_1.dat",
                "/flows/ipfix_test_2.dat");
        assertThat(flows, hasSize(8));
        Utils.JsonConverter jsonConverter = new Utils.JsonConverter();
        List<String> jsonStrings = jsonConverter.getJsonStringFromResources("/flows/ipfix_test_1.json",
                "/flows/ipfix_test_2.json");
        List<Flow> jsonData = jsonConverter.convert(jsonStrings, Instant.now());
        assertThat(jsonData, hasSize(8));
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals(flows.get(i).getFlowSeqNum(), jsonData.get(i).getFlowSeqNum());
            Assert.assertEquals(flows.get(i).getFlowRecords(), jsonData.get(i).getFlowRecords());
            Assert.assertEquals(flows.get(i).getTimestamp(), jsonData.get(i).getTimestamp());
            Assert.assertEquals(flows.get(i).getBytes(), jsonData.get(i).getBytes());
            Flow.Direction direction = jsonData.get(i).getDirection() != null ? jsonData.get(i).getDirection() : Flow.Direction.INGRESS;
            Assert.assertEquals(flows.get(i).getDirection(), direction);
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
                final Packet packet = new Packet(session, header, slice(buffer, header.payloadLength()));
                packet.getRecords().forEach(rec -> {

                    final FlowMessage flowMessage;
                    try {
                        flowMessage = Utils.buildAndSerialize(Protocol.IPFIX, rec).build();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    flows.addAll(ipFixConverter.convert(flowMessage, Instant.now()));
                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }

        return flows;
    }
}
