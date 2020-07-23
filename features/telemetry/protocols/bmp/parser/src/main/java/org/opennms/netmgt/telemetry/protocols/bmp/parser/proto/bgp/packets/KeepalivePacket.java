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

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class KeepalivePacket implements Packet {
    public final Header header;

    public KeepalivePacket(final Header header, final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) {
        this.header = Objects.requireNonNull(header);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static Optional<KeepalivePacket> parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
        final Header header = new Header(buffer);
        if (header.type != Header.Type.KEEPALIVE) {
            BmpParser.RATE_LIMITED_LOG.debug("Expected Keepalive Message, got: {}", header.type);
            skip(buffer, header.length - Header.SIZE);
            return Optional.empty();
        }

        return Optional.of(new KeepalivePacket(header, slice(buffer, header.length - Header.SIZE), flags, peerInfo));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .toString();
    }
}
