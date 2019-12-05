/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Aggregator;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPath;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AtomicAggregate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Attribute;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LocalPref;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiExistDisc;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.NextHop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Origin;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class UpdatePacket implements Packet {
    public final Header header;

    public final List<Prefix> withdrawRoutes;
    public final List<PathAttribute> pathAttributes;
    public final List<Prefix> reachableRoutes;

    public UpdatePacket(final Header header, final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        this.withdrawRoutes = repeatRemaining(slice(buffer, uint16(buffer)), prefixBuffer -> new Prefix(prefixBuffer, flags));
        this.pathAttributes = repeatRemaining(slice(buffer, uint16(buffer)), pathAttributeBuffer -> new PathAttribute(pathAttributeBuffer, flags));
        this.reachableRoutes = repeatRemaining(buffer, prefixBuffer -> new Prefix(prefixBuffer, flags));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static class Prefix {
        public final int length;         // uint8
        public final InetAddress prefix; // byte[length padded to 8 bits]

        public Prefix(final ByteBuf buffer, final PeerFlags flags) {
            this.length = uint8(buffer);

            // Create a buffer for the address with the size required to hold the full address (depending on the
            // version) but fill ony the first n bytes. Remaining bytes will be initialized all zero.
            final byte[] prefix = new byte[flags.addressVersion.map(v -> {switch (v) {
                case IP_V4: return 4;
                case IP_V6: return 16;
                default: throw new IllegalStateException();
            }})];
            buffer.readBytes(prefix, 0, (this.length + (8-1)) / 8); // Read n bits padded to the next full byte
            this.prefix = InetAddressUtils.getInetAddress(prefix);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("length", this.length)
                    .add("prefix", this.prefix)
                    .toString();
        }
    }

    public static class PathAttribute {
        public final boolean optional;   // uint8 x0000000
        public final boolean transitive; // ..... 0x000000
        public final boolean partial;    // ..... 00x00000
        public final boolean extended;   // ..... 000x0000

        public final Type type;  // uint8
        public final int length; // uint8 / uint16 (extended)

        public final Attribute attribute;

        public PathAttribute(final ByteBuf buffer, final PeerFlags peerFlags) throws InvalidPacketException {
            final int flags = uint8(buffer);
            this.optional   = ((flags >> 7 & 0x01) == 1);
            this.transitive = ((flags >> 6 & 0x01) == 1);
            this.partial    = ((flags >> 5 & 0x01) == 1);
            this.extended   = ((flags >> 4 & 0x01) == 1);

            this.type = Type.from(uint8(buffer));

            if (this.extended) {
                this.length = uint16(buffer);
            } else {
                this.length = uint8(buffer);
            }

            this.attribute = this.type.parse(slice(buffer, this.length), peerFlags);
        }

        public enum Type {
            ORIGIN {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new Origin(buffer, flags);
                }
            },
            AS_PATH {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new AsPath(buffer, flags);
                }
            },
            NEXT_HOP {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new NextHop(buffer, flags);
                }
            },
            MULTI_EXIT_DISC {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new MultiExistDisc(buffer, flags);
                }
            },
            LOCAL_PREF {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new LocalPref(buffer, flags);
                }
            },
            ATOMIC_AGGREGATE {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new AtomicAggregate(buffer, flags);
                }
            },
            AGGREGATOR {
                @Override
                public Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new Aggregator(buffer, flags);
                }
            };

            public abstract Attribute parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException;

            private static Type from(final int type) {
                switch (type) {
                    case 1: return ORIGIN;
                    case 2: return AS_PATH;
                    case 3: return NEXT_HOP;
                    case 4: return MULTI_EXIT_DISC;
                    case 5: return LOCAL_PREF;
                    case 6: return ATOMIC_AGGREGATE;
                    case 7: return AGGREGATOR;
                    default:
                        throw new IllegalArgumentException("Unknown path attribute type: " + type);
                }
            }
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("optional", this.optional)
                    .add("transitive", this.transitive)
                    .add("partial", this.partial)
                    .add("extended", this.extended)
                    .add("type", this.type)
                    .add("length", this.length)
                    .add("attribute", this.attribute)
                    .toString();
        }
    }

    public static UpdatePacket parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
        final Header header = new Header(buffer);
        if (header.type != Header.Type.UPDATE) {
            throw new InvalidPacketException(buffer, "Expected Update Message, got: {}", header.type);
        }

        return new UpdatePacket(header, slice(buffer, header.length - Header.SIZE), flags);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("withdrawRoutes", this.withdrawRoutes)
                .add("pathAttributes", this.pathAttributes)
                .add("reachableRoutes", this.reachableRoutes)
                .toString();
    }
}
