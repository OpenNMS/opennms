/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ParserBase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Netflow5ConverterTest {

    private Netflow5Converter nf5Converter = new Netflow5Converter();

    @Test
    public void canParseNetflow5Flows() {
        // Generate flows from existing packet payloads
        List<Flow> flows = getFlowsForPayloadsInSession("/flows/netflow5.dat");
        MatcherAssert.assertThat(flows, hasSize(2));

        final Flow flow = flows.get(0);
        assertThat(flow.getNetflowVersion(), is((Flow.NetflowVersion.V5)));
        assertThat(flow.getFlowRecords(), is(2));
        assertThat(flow.getFlowSeqNum(), is(0L));
        assertThat(flow.getEngineId(), is(0));
        assertThat(flow.getEngineType(), is(0));
        assertThat(flow.getSamplingInterval(), is(0.0));
        assertThat(flow.getSamplingAlgorithm(), is(Flow.SamplingAlgorithm.Unassigned));
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
        MatcherAssert.assertThat(flow.getFirstSwitched(), equalTo(1434886852194L));
        MatcherAssert.assertThat(flow.getLastSwitched(), equalTo(1430591887476L));
        MatcherAssert.assertThat(flow.getPackets(), equalTo(5L));
        MatcherAssert.assertThat(flow.getDirection(), equalTo(Flow.Direction.INGRESS));
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
                    final ByteBuffer bf = ParserBase.serialize(Protocol.NETFLOW9, rec);
                    final BsonDocument doc = new RawBsonDocument(bf.array());
                    flows.addAll(nf5Converter.convert(doc));
                });
            } catch (InvalidPacketException e) {
                throw new RuntimeException(e);
            }
        }
        return flows;
    }
}
