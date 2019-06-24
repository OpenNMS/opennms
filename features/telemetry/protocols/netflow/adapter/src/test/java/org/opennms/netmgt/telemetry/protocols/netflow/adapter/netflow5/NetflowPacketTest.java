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
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.proto.NetflowPacket;

import com.google.common.base.Strings;

public class NetflowPacketTest {

    @Test
    public void canReadValidNetflow5() throws IOException, URISyntaxException {
        execute("/flows/netflow5.dat", flowPacket -> {
            assertThat(flowPacket.isValid(), is(true));

            // Verify Header
            assertThat(flowPacket.getVersion(), is((NetflowPacket.VERSION)));
            assertThat(flowPacket.getCount(), is(2));
            assertThat(flowPacket.getSysUptime(), is(3381L)); // Hex: 0x00000D35
            assertThat(flowPacket.getUnixSecs(), is(1430591888L)); // Hex: 0x55451990
            assertThat(flowPacket.getUnixNSecs(), is(280328000L)); // Hex: 0x10B57740
            assertThat(flowPacket.getFlowSequence(), is(0L));
            assertThat(flowPacket.getEngineId(), is(0));
            assertThat(flowPacket.getEngineType(), is(0));
            assertThat(flowPacket.getSamplingInterval(), is(0));
            assertThat(flowPacket.getSamplingAlgorithm(), is(0));

            // Verify Flow Record 1
            assertThat(flowPacket.getRecord(0).getSrcAddr(), is("10.0.2.2"));
            assertThat(flowPacket.getRecord(0).getDstAddr(), is("10.0.2.15"));
            assertThat(flowPacket.getRecord(0).getNextHop(), is("0.0.0.0"));
            assertThat(flowPacket.getRecord(0).getSrcPort(), is(54435));
            assertThat(flowPacket.getRecord(0).getDstPort(), is(22));
            assertThat(flowPacket.getRecord(0).getTcpFlags(), is(16));
            assertThat(flowPacket.getRecord(0).getInput(), is(0));
            assertThat(flowPacket.getRecord(0).getOutput(), is(0));
            assertThat(flowPacket.getRecord(0).getDPkts(), is(5L));
            assertThat(flowPacket.getRecord(0).getDOctets(), is(230L));
            assertThat(flowPacket.getRecord(0).getFirst(), is(1024L * 1024L * 1024L * 4L - 1)); // Hex: 0xFFFFFFFF
            assertThat(flowPacket.getRecord(0).getLast(), is(2577L)); // Hex: 0x00000A11
            assertThat(flowPacket.getRecord(0).getProt(), is(6));
            assertThat(flowPacket.getRecord(0).getToS(), is(0));
            assertThat(flowPacket.getRecord(0).getSrcAs(), is(0));
            assertThat(flowPacket.getRecord(0).getDstAs(), is(0));
            assertThat(flowPacket.getRecord(0).getSrcMask(), is(0));
            assertThat(flowPacket.getRecord(0).getDstMask(), is(0));
            assertThat(flowPacket.getRecord(0).isEgress(), is(false));

            // Verify Flow Record 1
            assertThat(flowPacket.getRecord(1).getSrcAddr(), is("10.0.2.15"));
            assertThat(flowPacket.getRecord(1).getDstAddr(), is("10.0.2.2"));
            assertThat(flowPacket.getRecord(1).getNextHop(), is("0.0.0.0"));
            assertThat(flowPacket.getRecord(1).getSrcPort(), is(22));
            assertThat(flowPacket.getRecord(1).getDstPort(), is(54435));
            assertThat(flowPacket.getRecord(1).getTcpFlags(), is(24));
            assertThat(flowPacket.getRecord(1).getInput(), is(0));
            assertThat(flowPacket.getRecord(1).getOutput(), is(0));
            assertThat(flowPacket.getRecord(1).getDPkts(), is(4L));
            assertThat(flowPacket.getRecord(1).getDOctets(), is(304L));
            assertThat(flowPacket.getRecord(1).getFirst(), is(1024L * 1024L * 1024L * 4L - 1)); // Hex: 0xFFFFFFFF
            assertThat(flowPacket.getRecord(1).getLast(), is(2577L)); // Hex: 0x00000A11
            assertThat(flowPacket.getRecord(1).getProt(), is(6));
            assertThat(flowPacket.getRecord(1).getToS(), is(0));
            assertThat(flowPacket.getRecord(1).getSrcAs(), is(0));
            assertThat(flowPacket.getRecord(1).getDstAs(), is(0));
            assertThat(flowPacket.getRecord(1).getSrcMask(), is(0));
            assertThat(flowPacket.getRecord(1).getDstMask(), is(0));
            assertThat(flowPacket.getRecord(1).isEgress(), is(true));
        });
    }

    @Test
    public void canReadInvalidNetflow5_01() throws URISyntaxException, IOException {
        execute("/flows/netflow5_test_invalid01.dat", flowPacket -> {
            assertThat(flowPacket.isValid(), is(false));
        });
    }

    @Test
    public void canReadInvalidNetflow5_02() throws URISyntaxException, IOException {
        execute("/flows/netflow5_test_invalid02.dat", flowPacket -> {
            assertThat(flowPacket.isValid(), is(false));
        });
    }

    @Test
    public void canReadMicrotikNetflow5() {
        execute("/flows/netflow5_test_microtik.dat", flowPacket -> {
            assertThat(flowPacket.isValid(), is(true));

            // Verify Header
            assertThat(flowPacket.getVersion(), is((NetflowPacket.VERSION)));
            assertThat(flowPacket.getCount(), is(30));
            assertThat(flowPacket.getRecords(), hasSize(30));
            assertThat(flowPacket.getSysUptime(), is(27361640L)); // Hex: 0x01A18168
            assertThat(flowPacket.getUnixSecs(), is(1469109117L)); // Hex: 0x5790D37D
            assertThat(flowPacket.getUnixNSecs(), is(514932000L)); // Hex: 0x1EB13D20
            assertThat(flowPacket.getFlowSequence(), is(8140050L));
            assertThat(flowPacket.getEngineId(), is(0));
            assertThat(flowPacket.getEngineType(), is(0));
            assertThat(flowPacket.getSamplingInterval(), is(0));
            assertThat(flowPacket.getSamplingAlgorithm(), is(0));

            // Verify Last Flow Record
            assertThat(flowPacket.getRecord(29).getSrcAddr(), is("10.0.8.1"));
            assertThat(flowPacket.getRecord(29).getDstAddr(), is("192.168.0.1"));
            assertThat(flowPacket.getRecord(29).getNextHop(), is("192.168.0.1"));
            assertThat(flowPacket.getRecord(29).getSrcPort(), is(80));
            assertThat(flowPacket.getRecord(29).getDstPort(), is(51826));
            assertThat(flowPacket.getRecord(29).getToS(), is(40));
            assertThat(flowPacket.getRecord(29).getInput(), is(13));
            assertThat(flowPacket.getRecord(29).getOutput(), is(46));
            assertThat(flowPacket.getRecord(29).getDPkts(), is(13L));
            assertThat(flowPacket.getRecord(29).getDOctets(), is(11442L));
            assertThat(flowPacket.getRecord(29).getFirst(), is(27346380L)); // Hex: 0x01A145CC
            assertThat(flowPacket.getRecord(29).getLast(), is(27346380L)); // Hex: 0x01A145CC
            assertThat(flowPacket.getRecord(29).getTcpFlags(), is(82));
            assertThat(flowPacket.getRecord(29).getProt(), is(6));
            assertThat(flowPacket.getRecord(29).getSrcAs(), is(0));
            assertThat(flowPacket.getRecord(29).getDstAs(), is(0));
            assertThat(flowPacket.getRecord(29).getSrcMask(), is(0));
            assertThat(flowPacket.getRecord(29).getDstMask(), is(0));
            assertThat(flowPacket.getRecord(29).isEgress(), is(false));
        });
    }

    @Test
    public void canReadJuniperMX80Netflow5() {
        execute("/flows/netflow5_test_juniper_mx80.dat", flowPacket -> {
            assertThat(flowPacket.isValid(), is(true));

            // Verify Flow Header
            assertThat(flowPacket.getVersion(), is((NetflowPacket.VERSION)));
            assertThat(flowPacket.getCount(), is(29));
            assertThat(flowPacket.getRecords(), hasSize(29));
            assertThat(flowPacket.getSysUptime(), is(190649064L)); // Hex: 0x0B5D12E8
            assertThat(flowPacket.getUnixSecs(), is(1469109172L)); // Hex: 0x5790D3B4
            assertThat(flowPacket.getUnixNSecs(), is(00000000L)); // Hex: 0x00000000
            assertThat(flowPacket.getFlowSequence(), is(528678L));
            assertThat(flowPacket.getEngineId(), is(0));
            assertThat(flowPacket.getEngineType(), is(0));
            assertThat(flowPacket.getSamplingInterval(), is(1000));
            assertThat(flowPacket.getSamplingAlgorithm(), is(0));

            // Verify Last Flow Record
            assertThat(flowPacket.getRecord(28).getSrcAddr(), is("66.249.92.75"));
            assertThat(flowPacket.getRecord(28).getDstAddr(), is("192.168.0.1"));
            assertThat(flowPacket.getRecord(28).getNextHop(), is("192.168.0.1"));
            assertThat(flowPacket.getRecord(28).getSrcPort(), is(37387));
            assertThat(flowPacket.getRecord(28).getDstPort(), is(80));
            assertThat(flowPacket.getRecord(28).getSrcAs(), is(15169));
            assertThat(flowPacket.getRecord(28).getDstAs(), is(64496));
            assertThat(flowPacket.getRecord(28).getToS(), is(0));
            assertThat(flowPacket.getRecord(28).getInput(), is(542));
            assertThat(flowPacket.getRecord(28).getOutput(), is(536));
            assertThat(flowPacket.getRecord(28).getDPkts(), is(2L));
            assertThat(flowPacket.getRecord(28).getDOctets(), is(104L));
            assertThat(flowPacket.getRecord(28).getFirst(), is(190631000L)); // Hex: 0x0B5CCC58
            assertThat(flowPacket.getRecord(28).getLast(), is(190631000L)); // Hex: 0x0B5CCC58
            assertThat(flowPacket.getRecord(28).getTcpFlags(), is(16));
            assertThat(flowPacket.getRecord(28).getProt(), is(6));
            assertThat(flowPacket.getRecord(28).getSrcAs(), is(15169));
            assertThat(flowPacket.getRecord(28).getDstAs(), is(64496));
            assertThat(flowPacket.getRecord(28).getSrcMask(), is(19));
            assertThat(flowPacket.getRecord(28).getDstMask(), is(24));
            assertThat(flowPacket.getRecord(28).isEgress(), is(false));
        });
    }

    // Verify that all fields can be handled if they were maxed out.
    // This ensures that all fields are converted correctly.
    // For example if a 2 byte unsigned field's value were FFFF, it must be converted to an integer instead of a short.
    // NOTE: This is purely theoretically and does not reflect a REAL WORLD netflow packet.
    @Test
    public void canHandleMaxValuesNetflow5() {
        // Generate minimal netflow packet with 1 netflow record but maximum values (theoretical values only)
        final String string = Strings.padStart("", 72 * 2, 'F');
        byte[] bytes = new byte[string.length() / 2];
        for (int i=0, a=0; i<string.length(); i+=2, a++) {
            byte firstNibble = Byte.parseByte(string.substring(i, i+1), 16);
            byte secondNibble = Byte.parseByte(string.substring(i+1, i+2), 16);
            byte theByte = (byte)((secondNibble) | (firstNibble << 4 )); // bit-operations only with numbers, not bytes.
            bytes[a] = theByte;
        }

        // Parse and Verify
        NetflowPacket netflowPacket = new NetflowPacket(ByteBuffer.wrap(bytes));
        netflowPacket.isValid();

        // Verify Header
        assertThat(netflowPacket.getVersion(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getCount(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getSysUptime(), is(1024L * 1024L * 1024L * 4l - 1)); // 2^32-1
        assertThat(netflowPacket.getUnixSecs(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getUnixNSecs(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getFlowSequence(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getEngineType(), is(255)); // 2^8-1
        assertThat(netflowPacket.getEngineId(), is(255)); // 2^8-1
        assertThat(netflowPacket.getSamplingInterval(), is(16384 - 1)); // 2^14-1
        assertThat(netflowPacket.getSamplingAlgorithm(), is(4 - 1)); // 2^2-1

        // Verify Body
        assertThat(netflowPacket.getRecord(0).getSrcAddr(), is("255.255.255.255")); // quadruple: (2^8-1, 2^8-1, 2^8-1, 2^8-1)
        assertThat(netflowPacket.getRecord(0).getDstAddr(), is("255.255.255.255")); // quadruple: (2^8-1, 2^8-1, 2^8-1, 2^8-1)
        assertThat(netflowPacket.getRecord(0).getNextHop(), is("255.255.255.255")); // quadruple: (2^8-1, 2^8-1, 2^8-1, 2^8-1)
        assertThat(netflowPacket.getRecord(0).getInput(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getRecord(0).getOutput(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getRecord(0).getDPkts(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getRecord(0).getDOctets(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getRecord(0).getFirst(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getRecord(0).getLast(), is(1024L * 1024L * 1024L * 4 - 1)); // 2^32-1
        assertThat(netflowPacket.getRecord(0).getSrcPort(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getRecord(0).getDstPort(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getRecord(0).getTcpFlags(), is(255)); // 2^8-1
        assertThat(netflowPacket.getRecord(0).getProt(), is(255)); // 2^8-1
        assertThat(netflowPacket.getRecord(0).getToS(), is(255)); // 2^8-1
        assertThat(netflowPacket.getRecord(0).getSrcAs(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getRecord(0).getDstAs(), is(65536 - 1)); // 2^16-1
        assertThat(netflowPacket.getRecord(0).getSrcMask(), is(255)); // 2^8-1
        assertThat(netflowPacket.getRecord(0).getDstMask(), is(255)); // 2^8-1
        assertThat(netflowPacket.getRecord(0).isEgress(), is(false));
    }

    @Test
    public void canReadJuniperPackets() {
        execute("/flows/jflow-packet.dat", packet -> {
            assertThat(packet.getSamplingInterval(), is(20));
            assertThat(packet.getSamplingAlgorithm(), is(0));
            assertThat(packet.getCount(), is(29));
        });
    }

    public void execute(String resource, Consumer<NetflowPacket> consumer) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(consumer);

        URL resourceURL = getClass().getResource(resource);
        Objects.requireNonNull(resourceURL);

        try {
            final byte[] contents = Files.readAllBytes(Paths.get(resourceURL.toURI()));
            final ByteBuffer data = ByteBuffer.wrap(contents);
            final NetflowPacket netflowPacket = new NetflowPacket(data);
            consumer.accept(netflowPacket);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
