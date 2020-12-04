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

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerAccessor;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.LocalBgpNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.LocalNoNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.Reason;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.RemoteBgpNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.RemoteNoNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.Unknown;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class PeerDownPacket implements Packet {
    public final Header header;
    public final PeerHeader peerHeader;

    public final Type type;     // uint8
    public final Reason reason; // variable

    public PeerDownPacket(final Header header, final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);
        this.peerHeader = new PeerHeader(buffer);

        this.type = Type.from(uint8(buffer));
        this.reason = this.type.parse(buffer, this.peerHeader.flags, peerAccessor.getPeerInfo(peerHeader));
    }

    public enum Type {
        LOCAL_BGP_NOTIFICATION {
            @Override
            public Reason parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                return new LocalBgpNotification(buffer, flags, peerInfo);
            }
        },
        LOCAL_NO_NOTIFICATION {
            @Override
            public Reason parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                return new LocalNoNotification(buffer, flags, peerInfo);
            }
        },
        REMOTE_BGP_NOTIFICATION {
            @Override
            public Reason parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                return new RemoteBgpNotification(buffer, flags, peerInfo);
            }
        },
        REMOTE_NO_NOTIFICATION {
            @Override
            public Reason parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                return new RemoteNoNotification(buffer, flags, peerInfo);
            }
        },
        UNKNOWN {
            @Override
            public Reason parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                return new Unknown(buffer, flags, peerInfo);
            }
        };

        public abstract Reason parse(final ByteBuf buffer, final PeerFlags flags, final Optional<PeerInfo> peerInfo) throws InvalidPacketException;

        private static Type from(final int type) {
            switch (type) {
                case 1: return LOCAL_BGP_NOTIFICATION;
                case 2: return LOCAL_NO_NOTIFICATION;
                case 3: return REMOTE_BGP_NOTIFICATION;
                case 4: return REMOTE_NO_NOTIFICATION;
                default:
                    BmpParser.RATE_LIMITED_LOG.debug("Unknown Peer Down Type: {}", type);
                    return UNKNOWN;
            }
        }
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R map(final Mapper<R> mapper) {
        return mapper.map(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("peerHeader", this.peerHeader)
                .add("type", this.type)
                .add("reason", this.reason)
                .toString();
    }
}
