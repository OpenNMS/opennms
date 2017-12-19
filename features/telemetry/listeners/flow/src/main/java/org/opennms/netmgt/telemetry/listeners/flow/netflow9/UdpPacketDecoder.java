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

package org.opennms.netmgt.telemetry.listeners.flow.netflow9;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;
import org.opennms.netmgt.telemetry.listeners.flow.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.listeners.flow.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.listeners.flow.session.UdpSession;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

public class UdpPacketDecoder extends MessageToMessageDecoder<DatagramPacket> {
    private final UdpSession session;

    public UdpPacketDecoder(final UdpSession session) {
        this.session = Objects.requireNonNull(session);
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final DatagramPacket msg, final List<Object> out) throws Exception {
        final TemplateManager templateManager = this.session.getTemplateManager(msg.sender(), msg.recipient());

        final ByteBuf buf = msg.content();

        final ByteBuffer headerBuffer = buf.readSlice(Header.SIZE).nioBuffer();
        final Header header = new Header(headerBuffer);

        final ByteBuffer payloadBuffer = buf.nioBuffer();
        final Packet packet = new Packet(templateManager, header, payloadBuffer);

        out.add(new DefaultAddressedEnvelope<>(packet, msg.recipient(), msg.sender()));
    }
}
