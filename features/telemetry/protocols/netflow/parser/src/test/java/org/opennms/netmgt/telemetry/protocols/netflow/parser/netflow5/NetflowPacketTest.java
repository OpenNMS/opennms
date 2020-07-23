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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Record;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NetflowPacketTest {

    @Test
    public void canReadValidNetflow5() throws InvalidPacketException {
        execute("/flows/netflow5.dat", flowPacket -> {

            // Verify Header
            assertThat(flowPacket.header.versionNumber, is(0x0005));
            assertThat(flowPacket.header.count, is(2));
            assertThat(flowPacket.header.sysUptime, is(3381L)); // Hex: 0x00000D35
            assertThat(flowPacket.header.unixSecs, is(1430591888L)); // Hex: 0x55451990
            assertThat(flowPacket.header.unixNSecs, is(280328000L)); // Hex: 0x10B57740
            assertThat(flowPacket.header.flowSequence, is(0L));
            assertThat(flowPacket.header.engineId, is(0));
            assertThat(flowPacket.header.engineType, is(0));
            assertThat(flowPacket.header.samplingInterval, is(0));
            assertThat(flowPacket.header.samplingAlgorithm, is(0));

            assertThat(flowPacket.records, hasSize(2));

            assertThat(flowPacket.records.get(0).srcAddr.getHostAddress(), is("10.0.2.2"));
            assertThat(flowPacket.records.get(0).dstAddr.getHostAddress(), is("10.0.2.15"));
            assertThat(flowPacket.records.get(0).nextHop.getHostAddress(), is("0.0.0.0"));
            assertThat(flowPacket.records.get(0).srcPort, is(54435));
            assertThat(flowPacket.records.get(0).dstPort, is(22));
            assertThat(flowPacket.records.get(0).tcpFlags, is(16));
            assertThat(flowPacket.records.get(0).input, is(0));
            assertThat(flowPacket.records.get(0).output, is(0));
            assertThat(flowPacket.records.get(0).dPkts, is(5L));
            assertThat(flowPacket.records.get(0).dOctets, is(230L));
            assertThat(flowPacket.records.get(0).first, is(1024L * 1024L * 1024L * 4L - 1)); // Hex: 0xFFFFFFFF
            assertThat(flowPacket.records.get(0).last, is(2577L)); // Hex: 0x00000A11
            assertThat(flowPacket.records.get(0).proto, is(6));
            assertThat(flowPacket.records.get(0).tos, is(0));
            assertThat(flowPacket.records.get(0).srcAs, is(0));
            assertThat(flowPacket.records.get(0).dstAs, is(0));
            assertThat(flowPacket.records.get(0).srcMask, is(0));
            assertThat(flowPacket.records.get(0).dstMask, is(0));
            assertThat(flowPacket.records.get(0).egress, is(false));

            assertThat(flowPacket.records.get(1).srcAddr.getHostAddress(), is("10.0.2.15"));
            assertThat(flowPacket.records.get(1).dstAddr.getHostAddress(), is("10.0.2.2"));
            assertThat(flowPacket.records.get(1).nextHop.getHostAddress(), is("0.0.0.0"));
            assertThat(flowPacket.records.get(1).srcPort, is(22));
            assertThat(flowPacket.records.get(1).dstPort, is(54435));
            assertThat(flowPacket.records.get(1).tcpFlags, is(24));
            assertThat(flowPacket.records.get(1).input, is(0));
            assertThat(flowPacket.records.get(1).output, is(0));
            assertThat(flowPacket.records.get(1).dPkts, is(4L));
            assertThat(flowPacket.records.get(1).dOctets, is(304L));
            assertThat(flowPacket.records.get(1).first, is(1024L * 1024L * 1024L * 4L - 1)); // Hex: 0xFFFFFFFF
            assertThat(flowPacket.records.get(1).last, is(2577L)); // Hex: 0x00000A11
            assertThat(flowPacket.records.get(1).proto, is(6));
            assertThat(flowPacket.records.get(1).tos, is(0));
            assertThat(flowPacket.records.get(1).srcAs, is(0));
            assertThat(flowPacket.records.get(1).dstAs, is(0));
            assertThat(flowPacket.records.get(1).srcMask, is(0));
            assertThat(flowPacket.records.get(1).dstMask, is(0));
            assertThat(flowPacket.records.get(1).egress, is(true));
        });
    }

    @Test(expected = InvalidPacketException.class)
    public void canReadInvalidNetflow5_01() throws InvalidPacketException {
        execute("/flows/netflow5_test_invalid01.dat", flowPacket -> {
            throw new IllegalStateException();
        });
    }

    @Test(expected = InvalidPacketException.class)
    public void canReadInvalidNetflow5_02() throws InvalidPacketException {
        execute("/flows/netflow5_test_invalid02.dat", flowPacket -> {
            throw new IllegalStateException();
        });
    }

    @Test
    public void canReadMicrotikNetflow5() throws InvalidPacketException {
        execute("/flows/netflow5_test_microtik.dat", flowPacket -> {

            // Verify Header
            assertThat(flowPacket.header.versionNumber, is(0x0005));
            assertThat(flowPacket.header.count, is(30));
            assertThat(flowPacket.header.sysUptime, is(27361640L)); // Hex: 0x01A18168
            assertThat(flowPacket.header.unixSecs, is(1469109117L)); // Hex: 0x5790D37D
            assertThat(flowPacket.header.unixNSecs, is(514932000L)); // Hex: 0x1EB13D20
            assertThat(flowPacket.header.flowSequence, is(8140050L));
            assertThat(flowPacket.header.engineId, is(0));
            assertThat(flowPacket.header.engineType, is(0));
            assertThat(flowPacket.header.samplingInterval, is(0));
            assertThat(flowPacket.header.samplingAlgorithm, is(0));

            // Verify Last Flow Record
            assertThat(flowPacket.records, hasSize(30));
            assertThat(flowPacket.records.get(29).srcAddr.getHostAddress(), is("10.0.8.1"));
            assertThat(flowPacket.records.get(29).dstAddr.getHostAddress(), is("192.168.0.1"));
            assertThat(flowPacket.records.get(29).nextHop.getHostAddress(), is("192.168.0.1"));
            assertThat(flowPacket.records.get(29).srcPort, is(80));
            assertThat(flowPacket.records.get(29).dstPort, is(51826));
            assertThat(flowPacket.records.get(29).tos, is(40));
            assertThat(flowPacket.records.get(29).input, is(13));
            assertThat(flowPacket.records.get(29).output, is(46));
            assertThat(flowPacket.records.get(29).dPkts, is(13L));
            assertThat(flowPacket.records.get(29).dOctets, is(11442L));
            assertThat(flowPacket.records.get(29).first, is(27346380L)); // Hex: 0x01A145CC
            assertThat(flowPacket.records.get(29).last, is(27346380L)); // Hex: 0x01A145CC
            assertThat(flowPacket.records.get(29).tcpFlags, is(82));
            assertThat(flowPacket.records.get(29).proto, is(6));
            assertThat(flowPacket.records.get(29).srcAs, is(0));
            assertThat(flowPacket.records.get(29).dstAs, is(0));
            assertThat(flowPacket.records.get(29).srcMask, is(0));
            assertThat(flowPacket.records.get(29).dstMask, is(0));
            assertThat(flowPacket.records.get(29).egress, is(false));
        });
    }

    @Test
    public void canReadJuniperMX80Netflow5() throws InvalidPacketException {
        execute("/flows/netflow5_test_juniper_mx80.dat", flowPacket -> {

            // Verify Flow Header
            assertThat(flowPacket.header.versionNumber, is(0x0005));
            assertThat(flowPacket.header.count, is(29));
            assertThat(flowPacket.header.sysUptime, is(190649064L)); // Hex: 0x0B5D12E8
            assertThat(flowPacket.header.unixSecs, is(1469109172L)); // Hex: 0x5790D3B4
            assertThat(flowPacket.header.unixNSecs, is(00000000L)); // Hex: 0x00000000
            assertThat(flowPacket.header.flowSequence, is(528678L));
            assertThat(flowPacket.header.engineId, is(0));
            assertThat(flowPacket.header.engineType, is(0));
            assertThat(flowPacket.header.samplingInterval, is(1000));
            assertThat(flowPacket.header.samplingAlgorithm, is(0));

            // Verify Last Flow Record
            assertThat(flowPacket.records, hasSize(29));
            assertThat(flowPacket.records.get(28).srcAddr.getHostAddress(), is("66.249.92.75"));
            assertThat(flowPacket.records.get(28).dstAddr.getHostAddress(), is("192.168.0.1"));
            assertThat(flowPacket.records.get(28).nextHop.getHostAddress(), is("192.168.0.1"));
            assertThat(flowPacket.records.get(28).srcPort, is(37387));
            assertThat(flowPacket.records.get(28).dstPort, is(80));
            assertThat(flowPacket.records.get(28).srcAs, is(15169));
            assertThat(flowPacket.records.get(28).dstAs, is(64496));
            assertThat(flowPacket.records.get(28).tos, is(0));
            assertThat(flowPacket.records.get(28).input, is(542));
            assertThat(flowPacket.records.get(28).output, is(536));
            assertThat(flowPacket.records.get(28).dPkts, is(2L));
            assertThat(flowPacket.records.get(28).dOctets, is(104L));
            assertThat(flowPacket.records.get(28).first, is(190631000L)); // Hex: 0x0B5CCC58
            assertThat(flowPacket.records.get(28).last, is(190631000L)); // Hex: 0x0B5CCC58
            assertThat(flowPacket.records.get(28).tcpFlags, is(16));
            assertThat(flowPacket.records.get(28).proto, is(6));
            assertThat(flowPacket.records.get(28).srcAs, is(15169));
            assertThat(flowPacket.records.get(28).dstAs, is(64496));
            assertThat(flowPacket.records.get(28).srcMask, is(19));
            assertThat(flowPacket.records.get(28).dstMask, is(24));
            assertThat(flowPacket.records.get(28).egress, is(false));
        });
    }

    // Verify that all fields can be handled if they were maxed out.
    // This ensures that all fields are converted correctly.
    // For example if a 2 byte unsigned field's value were FFFF, it must be converted to an integer instead of a short.
    // NOTE: This is purely theoretically and does not reflect a REAL WORLD netflow packet.
    @Test
    public void canHandleMaxValuesNetflow5() throws InvalidPacketException {
        // Generate minimal netflow packet with 1 netflow record but maximum values (theoretical values only)
        byte[] bytes = new byte[Header.SIZE + Record.SIZE];
        Arrays.fill(bytes, (byte) 0xFF);
        bytes[0] = 0x00;
        bytes[1] = 0x05;
        bytes[2] = 0x00;
        bytes[3] = 0x01;

        // Parse and Verify
        final ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

        final Header header = new Header(buffer);
        final Packet packet = new Packet(header, buffer);

        // Verify Header
        assertThat(packet.header.sysUptime, is(1024L * 1024L * 1024L * 4L - 1)); // 2^32-1
        assertThat(packet.header.unixSecs, is(1024L * 1024L * 1024L * 4L - 1)); // 2^32-1
        assertThat(packet.header.unixNSecs, is(1024L * 1024L * 1024L * 4L - 1)); // 2^32-1
        assertThat(packet.header.flowSequence, is(1024L * 1024L * 1024L * 4L - 1)); // 2^32-1
        assertThat(packet.header.engineType, is(255)); // 2^8-1
        assertThat(packet.header.engineId, is(255)); // 2^8-1
        assertThat(packet.header.samplingAlgorithm, is(4 - 1)); // 2^2-1
        assertThat(packet.header.samplingInterval, is(16384 - 1)); // 2^14-1

        // Verify Body
        assertThat(packet.records, hasSize(1));
        assertThat(packet.records.get(0).srcAddr.getHostAddress(), is("255.255.255.255")); // quadruple: (2^8-1, 2^8-1, 2^8-1, 2^8-1)
        assertThat(packet.records.get(0).dstAddr.getHostAddress(), is("255.255.255.255")); // quadruple: (2^8-1, 2^8-1, 2^8-1, 2^8-1)
        assertThat(packet.records.get(0).nextHop.getHostAddress(), is("255.255.255.255")); // quadruple: (2^8-1, 2^8-1, 2^8-1, 2^8-1)
        assertThat(packet.records.get(0).input, is(65536 - 1)); // 2^16-1
        assertThat(packet.records.get(0).output, is(65536 - 1)); // 2^16-1
        assertThat(packet.records.get(0).dPkts, is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(packet.records.get(0).dOctets, is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(packet.records.get(0).first, is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(packet.records.get(0).last, is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(packet.records.get(0).srcPort, is(65536 - 1)); // 2^16-1
        assertThat(packet.records.get(0).dstPort, is(65536 - 1)); // 2^16-1
        assertThat(packet.records.get(0).tcpFlags, is(255)); // 2^8-1
        assertThat(packet.records.get(0).proto, is(255)); // 2^8-1
        assertThat(packet.records.get(0).tos, is(255)); // 2^8-1
        assertThat(packet.records.get(0).srcAs, is(65536 - 1)); // 2^16-1
        assertThat(packet.records.get(0).dstAs, is(65536 - 1)); // 2^16-1
        assertThat(packet.records.get(0).srcMask, is(255)); // 2^8-1
        assertThat(packet.records.get(0).dstMask, is(255)); // 2^8-1
        assertThat(packet.records.get(0).egress, is(false));
    }

    @Test
    public void canReadJuniperPackets() throws InvalidPacketException {
        execute("/flows/jflow-packet.dat", packet -> {
            assertThat(packet.header.samplingInterval, is(20));
            assertThat(packet.header.samplingAlgorithm, is(0));
            assertThat(packet.records.size(), is(29));
        });
    }

    public void execute(final String resource, final Consumer<Packet> consumer) throws InvalidPacketException {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(consumer);

        final URL resourceURL = Objects.requireNonNull(getClass().getResource(resource));

        try {
            final byte[] contents = Files.readAllBytes(Paths.get(resourceURL.toURI()));
            final ByteBuf buffer = Unpooled.wrappedBuffer(contents);

            final Header header = new Header(slice(buffer, Header.SIZE));
            final Packet packet = new Packet(header, buffer);

            consumer.accept(packet);

        } catch (final URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
