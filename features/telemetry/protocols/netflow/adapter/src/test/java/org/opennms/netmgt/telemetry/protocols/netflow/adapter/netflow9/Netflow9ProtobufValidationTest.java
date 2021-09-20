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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow9;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.protocols.netflow.adapter.Utils.buildAndSerialize;

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
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


/**
 * This test validates netflow protobuf values against json output.
 */
public class Netflow9ProtobufValidationTest {

    private Converter nf9Converter;


    @Test
    public void canValidateNetflow9FlowsWithJsonOutput() {
        // Generate flows from existing packet payloads
        nf9Converter = new NetflowConverter();
        List<Flow> flows = getFlowsForPayloadsInSession("/flows/netflow9.dat",
                "/flows/netflow9_template.dat",
                "/flows/netflow9_records.dat");

        assertThat(flows, hasSize(12));
        Utils.JsonConverter jsonConverter = new Utils.JsonConverter();
        List<String> jsonStrings = jsonConverter.getJsonStringFromResources("/flows/netflow9.json",
                "/flows/netflow9_1.json");
        List<Flow> jsonFlows = jsonConverter.convert(jsonStrings, Instant.now());
        assertThat(jsonFlows, hasSize(12));
        for (int i = 0; i < 12; i++) {
            Assert.assertEquals(flows.get(i).getFlowSeqNum(), jsonFlows.get(i).getFlowSeqNum());
            Assert.assertEquals(flows.get(i).getFlowRecords(), jsonFlows.get(i).getFlowRecords());
            Assert.assertEquals(flows.get(i).getTimestamp(), jsonFlows.get(i).getTimestamp());
            Assert.assertEquals(flows.get(i).getBytes(), jsonFlows.get(i).getBytes());
            Flow.Direction direction = jsonFlows.get(i).getDirection() != null ? jsonFlows.get(i).getDirection() : Flow.Direction.INGRESS;
            Assert.assertEquals(flows.get(i).getDirection(), direction);
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

                    final FlowMessage flowMessage;
                    try {
                        flowMessage = buildAndSerialize(Protocol.NETFLOW9, rec).build();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    flows.addAll(nf9Converter.convert(flowMessage, Instant.now()));
                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }

        return flows;
    }
}
