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

package org.opennms.netmgt.telemetry.listeners.flow;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.flow.ie.RecordProvider;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.NullValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultAddressedEnvelope;
import io.netty.channel.SimpleChannelInboundHandler;

public class PacketHandler extends SimpleChannelInboundHandler<DefaultAddressedEnvelope<RecordProvider, InetSocketAddress>> {
    private static final Logger LOG = LoggerFactory.getLogger(PacketHandler.class);

    private final Protocol protocol;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    public PacketHandler(final Protocol protocol,
                         final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.protocol = Objects.requireNonNull(protocol);
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final DefaultAddressedEnvelope<RecordProvider, InetSocketAddress> packet) throws Exception {
        LOG.trace("Got packet: {}", packet);

        packet.content().getRecords().forEach(record -> {
            final ByteBuffer buffer = serialize(this.protocol, record);

            // Build the message to dispatch
            final TelemetryMessage msg = new TelemetryMessage(packet.sender(), buffer);

            // Dispatch and retain a reference to the packet
            // in the case that we are sharing the underlying byte array
            final CompletableFuture<TelemetryMessage> future = dispatcher.send(msg);

            // Pass exception if dispatching fails
            future.handle((result, ex) -> {
                if (ex != null) {
                    ctx.fireExceptionCaught(ex);
                }
                return result;
            });
        });
    }

    public static ByteBuffer serialize(final Protocol protocol, final Iterable<Value<?>> record) {
        // Build BSON document from flow
        final BasicOutputBuffer output = new BasicOutputBuffer();
        try (final BsonBinaryWriter writer = new BsonBinaryWriter(output)) {
            writer.writeStartDocument();
            writer.writeInt32("@version", protocol.magic);

            final FlowBuilderVisitor visitor = new FlowBuilderVisitor(writer);
            for (final Value<?> value : record) {
                value.visit(visitor);
            }

            writer.writeEndDocument();
        }

        return output.getByteBuffers().get(0).asNIO();
    }

    private static class FlowBuilderVisitor implements Value.Visitor {
        // TODO: Really use ordinal for enums?

        private final BsonWriter writer;

        public FlowBuilderVisitor(final BsonWriter writer) {
            this.writer = writer;
        }

        @Override
        public void accept(final NullValue value) {
            this.writer.writeNull(value.getName());
        }

        @Override
        public void accept(final BooleanValue value) {
            this.writer.writeBoolean(value.getName(), value.getValue());
        }

        @Override
        public void accept(final DateTimeValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeInt64("epoch", value.getValue().getEpochSecond());
            if (value.getValue().getNano() != 0) {
                this.writer.writeInt64("nanos", value.getValue().getNano());
            }
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final FloatValue value) {
            this.writer.writeDouble(value.getName(), value.getValue());
        }

        @Override
        public void accept(final IPv4AddressValue value) {
            // TODO: Transport as binary?
            this.writer.writeString(value.getName(), value.getValue().getHostAddress());
        }

        @Override
        public void accept(final IPv6AddressValue value) {
            // TODO: Transport as binary?
            this.writer.writeString(value.getName(), value.getValue().getHostAddress());
        }

        @Override
        public void accept(final MacAddressValue value) {
            this.writer.writeStartDocument(value.getName());
            value.getSemantics().ifPresent(semantics -> {
                this.writer.writeInt32("s", semantics.ordinal());
            });
            this.writer.writeBinaryData("v", new BsonBinary(value.getValue()));
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final OctetArrayValue value) {
            this.writer.writeBinaryData(value.getName(), new BsonBinary(value.getValue()));
        }

        @Override
        public void accept(final SignedValue value) {
            this.writer.writeInt64(value.getName(), value.getValue());
        }

        @Override
        public void accept(final StringValue value) {
            this.writer.writeString(value.getName(), value.getValue());
        }

        @Override
        public void accept(final ListValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeInt32("semantic", value.getSemantic().ordinal());
            this.writer.writeStartArray("values");
            for (int i = 0; i < value.getValue().size(); i++) {
                this.writer.writeStartDocument();
                for (int j = 0; j < value.getValue().get(i).size(); j++) {
                    value.getValue().get(i).get(j).visit(this);
                }
                this.writer.writeEndDocument();
            }
            this.writer.writeEndArray();
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final UnsignedValue value) {
            // TODO: Mark this as unsigned?
            this.writer.writeInt64(value.getName(), value.getValue().longValue());
        }

        @Override
        public void accept(final UndeclaredValue value) {
            this.writer.writeBinaryData(value.getName(), new BsonBinary(value.getValue()));
        }
    }
}
