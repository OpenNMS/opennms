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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.RecordProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.ListValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.NullValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.StringValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UndeclaredValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserBase {
    private static final Logger LOG = LoggerFactory.getLogger(ParserBase.class);

    private final Protocol protocol;

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    public ParserBase(final Protocol protocol,
                      final String name,
                      final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.protocol = Objects.requireNonNull(protocol);
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }

    public String getName() {
        return this.name;
    }

    protected CompletableFuture<?> transmit(final RecordProvider packet, final InetSocketAddress remoteAddress) throws Exception {
        LOG.trace("Got packet: {}", packet);

        // Return a future which completes when message is parsed and all records are transmitted
        return CompletableFuture.allOf(packet.getRecords().map(record -> {
            final ByteBuffer buffer = serialize(this.protocol, record);

            // Build the message to dispatch
            final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer);

            // Dispatch and retain a reference to the packet
            // in the case that we are sharing the underlying byte array
            return dispatcher.send(msg);
        }).toArray(CompletableFuture[]::new));
    }

    public static ByteBuffer serialize(final Protocol protocol, final Iterable<Value<?>> record) {
        // Build BSON document from flow
        final BasicOutputBuffer output = new BasicOutputBuffer();
        try (final BsonBinaryWriter writer = new BsonBinaryWriter(output)) {
            writer.writeStartDocument();
            writer.writeInt32("@version", protocol.version);

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
