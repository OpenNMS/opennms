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

package org.opennms.netmgt.telemetry.listeners.flow.ipfix;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.listeners.flow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.listeners.flow.session.Session;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.handler.codec.ByteToMessageDecoder;

public class TcpPacketDecoder extends ByteToMessageDecoder {
    private final InetSocketAddress senderAddress;
    private final InetSocketAddress recipientAddress;
    private final Session session;

    public TcpPacketDecoder(final InetSocketAddress senderAddress,
                            final InetSocketAddress recipientAddress,
                            final Session session) {
        this.senderAddress = Objects.requireNonNull(senderAddress);
        this.recipientAddress = Objects.requireNonNull(recipientAddress);
        this.session = Objects.requireNonNull(session);
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        final Object decoded = this.decode(ctx, in);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    private Object decode(final ChannelHandlerContext ctx, final ByteBuf in) throws InvalidPacketException {
        in.markReaderIndex();

        if (in.readableBytes() < Header.SIZE) {
            in.resetReaderIndex();
            return null;
        }

        final ByteBuffer headerBuffer = in.readSlice(Header.SIZE).nioBuffer();
        final Header header = new Header(headerBuffer);

        if (in.readableBytes() < header.length - Header.SIZE) {
            in.resetReaderIndex();
            return null;
        }

        final ByteBuffer payloadBuffer = in.readSlice(header.length - Header.SIZE).nioBuffer();
        final Packet packet = new Packet(this.session, this.senderAddress, header, payloadBuffer);

        return new DefaultAddressedEnvelope<>(packet, this.recipientAddress, this.senderAddress);
    }
}
