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

package org.opennms.netmgt.telemetry.listeners.sflow;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.bson.BsonBinaryWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.flows.SampleDatagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;

public class PacketHandler extends SimpleChannelInboundHandler<DefaultAddressedEnvelope<SampleDatagram, InetSocketAddress>> {
    private static final Logger LOG = LoggerFactory.getLogger(PacketHandler.class);

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    public PacketHandler(final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final DefaultAddressedEnvelope<SampleDatagram, InetSocketAddress> packet) throws Exception {
        LOG.trace("Got packet: {}", packet);

        final BasicOutputBuffer output = new BasicOutputBuffer();
        try (final BsonBinaryWriter bsonWriter = new BsonBinaryWriter(output)) {
            bsonWriter.writeStartDocument();

            bsonWriter.writeName("time");
            bsonWriter.writeInt64(System.currentTimeMillis());

            bsonWriter.writeName("data");
            packet.content().version.datagram.writeBson(bsonWriter);

            bsonWriter.writeEndDocument();
        }

        // Build the message to be sent
        final TelemetryMessage msg = new TelemetryMessage(packet.sender(), output.getByteBuffers().get(0).asNIO());
        final CompletableFuture<TelemetryMessage> future = dispatcher.send(msg);

        // Pass exception if dispatching fails
        future.handle((result, ex) -> {
            if (ex != null) {
                ctx.fireExceptionCaught(ex);
            }
            return result;
        });
    }
}
