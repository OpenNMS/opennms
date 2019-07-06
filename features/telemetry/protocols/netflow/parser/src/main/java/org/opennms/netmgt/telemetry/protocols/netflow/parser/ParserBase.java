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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.bson.BsonBinary;
import org.bson.BsonBinaryWriter;
import org.bson.BsonWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.common.utils.DnsUtils;
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ParserBase {
    private static final Logger LOG = LoggerFactory.getLogger(ParserBase.class);

    private final Protocol protocol;

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final EventForwarder eventForwarder;

    private final Identity identity;

    private long maxClockSkew = 0;

    private final LoadingCache<InetAddress, Optional<Instant>> eventCache;

    public ParserBase(final Protocol protocol,
                      final String name,
                      final AsyncDispatcher<TelemetryMessage> dispatcher,
                      final EventForwarder eventForwarder,
                      final Identity identity) {
        this.protocol = Objects.requireNonNull(protocol);
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.identity = Objects.requireNonNull(identity);

        this.eventCache =  CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<InetAddress, Optional<Instant>>() {
            @Override
            public Optional<Instant> load(InetAddress key) throws Exception {
                return Optional.empty();
            }
        });
    }

    public String getName() {
        return this.name;
    }

    public void setMaxClockSkew(long maxClockSkew) {
        this.maxClockSkew = maxClockSkew;
    }

    public long getMaxClockSkew() {
        return this.maxClockSkew;
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

    protected void detectClockSkew(final long packetTimestamp, final InetAddress remoteAddress) {
        if (getMaxClockSkew() > 0) {
            long delta = Math.abs(packetTimestamp - System.currentTimeMillis());
            if (delta > getMaxClockSkew() * 1000L) {
                final Optional<Instant> instant = eventCache.getUnchecked(remoteAddress);

                if (!instant.isPresent() || Duration.between(instant.get(), Instant.now()).toHours() > 0) {
                    eventCache.put(remoteAddress, Optional.of(Instant.now()));

                    eventForwarder.sendNow(new EventBuilder()
                            .setUei("uei.opennms.org/internal/telemetry/clockSkewDetected")
                            .setTime(new Date())
                            .setSource(getName())
                            .setInterface(remoteAddress)
                            .setDistPoller(identity.getId())
                            .addParam("monitoringSystemId", identity.getId())
                            .addParam("monitoringSystemLocation", identity.getLocation())
                            .setParam("delta", (int) delta)
                            .setParam("maxClockSkew", (int) getMaxClockSkew())
                            .getEvent());
                }

            }
        }
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
            this.writer.writeStartDocument(value.getName());
            this.writer.writeString("address", value.getValue().getHostAddress());
            DnsUtils.reverseLookup(value.getValue()).ifPresent((hostname) -> this.writer.writeString("hostname", hostname));
            this.writer.writeEndDocument();
        }

        @Override
        public void accept(final IPv6AddressValue value) {
            this.writer.writeStartDocument(value.getName());
            this.writer.writeString("address", value.getValue().getHostAddress());
            DnsUtils.reverseLookup(value.getValue()).ifPresent((hostname) -> this.writer.writeString("hostname", hostname));
            this.writer.writeEndDocument();
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
