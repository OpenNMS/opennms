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

import java.nio.ByteBuffer;
import java.util.List;

import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.session.TemplateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketDecoder extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(PacketDecoder.class);

    private final TemplateManager templateManager;

    public PacketDecoder(final TemplateManager templateManager) {
        this.templateManager = templateManager;
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
        final Packet packet = new Packet(sender, this.templateManager, header, payloadBuffer);

        return packet;
    }
}
