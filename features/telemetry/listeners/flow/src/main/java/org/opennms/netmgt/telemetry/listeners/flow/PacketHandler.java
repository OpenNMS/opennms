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
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.flow.dto.Flows;
import org.opennms.netmgt.telemetry.listeners.flow.ie.RecordProvider;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Semantics;
import org.opennms.netmgt.telemetry.listeners.flow.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.DateTimeValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.FloatValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.ListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.SignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.listeners.flow.session.EnterpriseField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;

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
        LOG.info("Got packet: {}", packet);

        packet.content().getRecords().forEach(record -> {
            final Flows.Flow.Builder flow = Flows.Flow.newBuilder()
                    .setVersion(protocol.magic)
                    .setObservationDomainId(record.observationDomainId)
                    .setScopeFieldCount(record.scopeFieldCount);

            final FlowBuilderVisitor visitor = new FlowBuilderVisitor(flow);
            for (final Value value : record.values) {
                value.visit(visitor);
            }

            final ByteBuffer buffer = ByteBuffer.wrap(flow.build().toByteArray());

            // Build the message to dispatch via the Sink API
            final TelemetryMessage msg = new TelemetryMessage(packet.sender(), buffer);

            // Dispatch and retain a reference to the packet
            // in the case that we are sharing the underlying byte array
            final CompletableFuture<TelemetryMessage> future = dispatcher.send(msg);

            // TODO: Handle future result and drop connection if dispatching fails
            future.join();
        });
    }

    protected static class FlowBuilderVisitor implements Value.Visitor {
        private final Flows.Flow.Builder flow;
        private final Iterable<String> prefix;

        public FlowBuilderVisitor(final Flows.Flow.Builder flow) {
            this.flow = flow;
            this.prefix = Collections.emptyList();
        }

        private FlowBuilderVisitor(final Flows.Flow.Builder flow,
                                   final Iterable<String> prefix) {
            this.flow = flow;
            this.prefix = prefix;
        }

        private Iterable<String> buildName(final String name) {
            return Iterables.concat(this.prefix, Collections.singleton(name));
        }

        private Iterable<String> buildName(final String... names) {
            return Iterables.concat(this.prefix, Arrays.asList(names));
        }

        private Flows.Entry.Builder buildEntry(final String name, final Optional<Semantics> semantics) {
            final Flows.Entry.Builder builder = this.flow.addEntriesBuilder();

            builder.addAllKey(this.buildName(name));

            if (semantics.isPresent()) {
                builder.setSemantics(Flows.Entry.Semantics.valueOf(semantics.get().ordinal()));
            } else {
                builder.clearSemantics();
            }

            return builder;
        }

        @Override
        public void accept(final BooleanValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setBool(value.getValue());
        }

        @Override
        public void accept(final DateTimeValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setTimestamp(Flows.Entry.Timestamp.newBuilder()
                            .setSeconds(value.getValue().getEpochSecond())
                            .setNanos(value.getValue().getNano()));
        }

        @Override
        public void accept(final FloatValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setFloat(value.getValue());
        }

        @Override
        public void accept(final IPv4AddressValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setIpv4Address(ByteString.copyFrom(value.getValue().getAddress()));
        }

        @Override
        public void accept(final IPv6AddressValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setIpv6Address(ByteString.copyFrom(value.getValue().getAddress()));
        }

        @Override
        public void accept(final MacAddressValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setMacAddress(ByteString.copyFrom(value.getValue()));
        }

        @Override
        public void accept(final OctetArrayValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setBytes(ByteString.copyFrom(value.getValue()));
        }

        @Override
        public void accept(final SignedValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setSigned(value.getValue());
        }

        @Override
        public void accept(final StringValue value) {
            this.buildEntry(value.getName(), value.getSemantics())
                    .setString(value.getValue());
        }

        @Override
        public void accept(final ListValue value) {
            for (int i = 0; i < value.getValue().size(); i++) {
                final FlowBuilderVisitor visitor = new FlowBuilderVisitor(this.flow, this.buildName(value.getName(), Integer.toString(i)));
                for (int j = 0; j < value.getValue().get(i).size(); j++) {
                    value.getValue().get(i).get(j).visit(visitor);
                }
            }
        }

        @Override
        public void accept(final UnsignedValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setUnsigned(value.getValue().longValue());
        }

        @Override
        public void accept(final EnterpriseField.EnterpriseValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setBytes(ByteString.copyFrom(value.getValue()));
        }
    }
}
