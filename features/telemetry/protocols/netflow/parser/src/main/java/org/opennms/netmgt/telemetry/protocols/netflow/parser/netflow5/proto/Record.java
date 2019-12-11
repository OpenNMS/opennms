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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;

public class Record {

    public static final int SIZE = 48;

    public final Packet packet; // Enclosing packet

    public final Inet4Address srcAddr;
    public final Inet4Address dstAddr;
    public final Inet4Address nextHop;

    // SNMP index of input / output interface
    public final int input;
    public final int output;

    // Packets in the flow
    public final long dPkts;

    // Total number of Layer 3 bytes in the packets of the flow
    public final long dOctets;

    // SysUptime at start of flow
    public final long first;

    // SysUptime at the time the last packet of the flow was received
    public final long last;

    // TCP/UDP source / destination port number or equivalent
    public final int srcPort;
    public final int dstPort;

    // Cumulative OR of TCP flags
    public final int tcpFlags;

    // IP protocol type (e.g. TCP = 6; UDP = 17)
    public final int proto;

    // Autonomous system number of the source / destination, either origin or peer
    public final int srcAs;
    public final int dstAs;

    // IP type of service (ToS)
    public final int tos;

    // Source / destination address prefix mask bits
    public final int srcMask;
    public final int dstMask;

    // 2nd bit of padding is set to 0x08 when this is an egress flow
    public final boolean egress;

    public Record(final Packet packet, final ByteBuf buffer) throws InvalidPacketException {
        this.packet = Objects.requireNonNull(packet);

        this.srcAddr = parseAddress(buffer);
        this.dstAddr = parseAddress(buffer);
        this.nextHop = parseAddress(buffer);

        this.input = uint16(buffer);
        this.output = uint16(buffer);

        this.dPkts = uint32(buffer);
        this.dOctets = uint32(buffer);

        this.first = uint32(buffer);
        this.last = uint32(buffer);

        this.srcPort = uint16(buffer);
        this.dstPort = uint16(buffer);

        final int padding1 = uint8(buffer);

        this.tcpFlags = uint8(buffer);

        this.proto = uint8(buffer);

        this.tos = uint8(buffer);

        this.srcAs = uint16(buffer);
        this.dstAs = uint16(buffer);

        this.srcMask = uint8(buffer);
        this.dstMask = uint8(buffer);

        final int padding2 = uint16(buffer);

        // 2nd bit of padding is set to 0x08 when this is an egress flow
        this.egress = padding2 == 0x0008;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("srcAddr", this.srcAddr)
                .add("dstAddr", this.dstAddr)
                .add("nextHop", this.nextHop)
                .add("input", this.input)
                .add("output", this.output)
                .add("dPkts", this.dPkts)
                .add("dOctets", this.dOctets)
                .add("first", this.first)
                .add("last", this.last)
                .add("srcPort", this.srcPort)
                .add("dstPort", this.dstPort)
                .add("tcpFlags", this.tcpFlags)
                .add("proto", this.proto)
                .add("srcAs", this.srcAs)
                .add("dstAs", this.dstAs)
                .add("tos", this.tos)
                .add("srcMask", this.srcMask)
                .add("dstMask", this.dstMask)
                .add("egress", this.egress)
                .toString();
    }

    private static Inet4Address parseAddress(final ByteBuf buffer) throws InvalidPacketException {
        try {
            return (Inet4Address) Inet4Address.getByAddress(bytes(buffer, 4));
        } catch (final UnknownHostException e) {
            throw new InvalidPacketException(buffer, "Error parsing IPv4 value", e);
        }
    }

    public Iterable<Value<?>> asValues() {
        return ImmutableList.<Value<?>>builder()
                .add(new IPv4AddressValue("srcAddr", this.srcAddr))
                .add(new IPv4AddressValue("dstAddr", this.dstAddr))
                .add(new IPv4AddressValue("nextHop", this.nextHop))
                .add(new UnsignedValue("input", this.input))
                .add(new UnsignedValue("output", this.output))
                .add(new UnsignedValue("dPkts", this.dPkts))
                .add(new UnsignedValue("dOctets", this.dOctets))
                .add(new UnsignedValue("first", this.first))
                .add(new UnsignedValue("last", this.last))
                .add(new UnsignedValue("srcPort", this.srcPort))
                .add(new UnsignedValue("dstPort", this.dstPort))
                .add(new UnsignedValue("tcpFlags", this.tcpFlags))
                .add(new UnsignedValue("proto", this.proto))
                .add(new UnsignedValue("srcAs", this.srcAs))
                .add(new UnsignedValue("dstAs", this.dstAs))
                .add(new UnsignedValue("tos", this.tos))
                .add(new UnsignedValue("srcMask", this.srcMask))
                .add(new UnsignedValue("dstMask", this.dstMask))
                .add(new BooleanValue("egress", this.egress))
                .build();
    }
}
