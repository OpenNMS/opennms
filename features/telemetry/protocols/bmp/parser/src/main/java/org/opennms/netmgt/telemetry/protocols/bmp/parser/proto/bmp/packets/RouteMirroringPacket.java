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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;

import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.TLV;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.BgpMessage;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.Information;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.mirroring.Mirroring;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class RouteMirroringPacket implements Packet {

    public final Header header;
    public final PeerHeader peerHeader;
    public final TLV.List<Element, Element.Type, Mirroring> elements;

    public RouteMirroringPacket(final Header header, final ByteBuf buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);
        this.peerHeader = new PeerHeader(buffer);

        this.elements = TLV.List.wrap(repeatRemaining(buffer, elementBuffer -> new Element(elementBuffer, this.peerHeader.flags)));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R map(final Mapper<R> mapper) {
        return mapper.map(this);
    }

    public static class Element extends TLV<Element.Type, Mirroring, PeerFlags> {

        public Element(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
            super(buffer, Element.Type::from, flags);
        }

        public enum Type implements TLV.Type<Mirroring, PeerFlags> {
            BGP_MESSAGE{
                @Override
                public Mirroring parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new BgpMessage(buffer, flags);
                }
            },
            INFORMATION{
                @Override
                public Mirroring parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
                    return new Information(buffer, flags);
                }
            },
            UNKNOWN{
                @Override
                public Mirroring parse(final ByteBuf buffer, final PeerFlags parameter) throws InvalidPacketException {
                    return null;
                }
            };

            private static Type from(final int type) {
                switch (type) {
                    case 0: return BGP_MESSAGE;
                    case 1: return INFORMATION;
                    default:
                        BmpParser.LOG.warn("Unknown Route Mirroring Packet Type: {}", type);
                        return UNKNOWN;
                }
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("peerHeader", this.peerHeader)
                .add("elements", this.elements)
                .toString();
    }
}
