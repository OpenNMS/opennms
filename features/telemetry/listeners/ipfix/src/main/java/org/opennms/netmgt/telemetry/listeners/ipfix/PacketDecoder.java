/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.ipfix;

import java.nio.ByteBuffer;
import java.util.List;

import org.opennms.netmgt.telemetry.listeners.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.listeners.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.listeners.ipfix.session.Session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketDecoder extends ByteToMessageDecoder {

    private final Session session;

    public PacketDecoder(final Session session) {
        this.session = session;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        final Object decoded = this.decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    private Object decode(final ChannelHandlerContext ctx, final ByteBuf in) throws InvalidPacketException {
        if (in.readableBytes() < Header.SIZE) {
            return null;
        }

        final ByteBuffer headerBuffer = in.slice(0, Header.SIZE).nioBuffer();
        final Header header = new Header(headerBuffer);

        if (in.readableBytes() < header.length) {
            return null;
        }

        final ByteBuffer payloadBuffer = in.slice(Header.SIZE, header.length - Header.SIZE).nioBuffer();
        final Packet packet = new Packet(this.session, header, payloadBuffer);

        in.skipBytes(header.length);

        return packet;
    }
}
