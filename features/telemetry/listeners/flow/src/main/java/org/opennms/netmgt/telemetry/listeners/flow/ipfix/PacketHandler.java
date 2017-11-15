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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.listeners.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.flow.dto.Flows;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.BasicListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.BooleanValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.DateTimeMicrosecondsValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.DateTimeMillisecondsValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.DateTimeNanosecondsValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.DateTimeSecondsValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Float32Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Float64Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.IPv4AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.IPv6AddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.MacAddressValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.OctetArrayValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Signed16Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Signed32Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Signed64Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Signed8Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.StringValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.SubTemplateListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.SubTemplateMultiListValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Unsigned16Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Unsigned32Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Unsigned64Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.ie.values.Unsigned8Value;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.DataRecord;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.FieldValue;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Set;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.SetHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class PacketHandler extends MessageToMessageDecoder<Packet> {
    private static final Logger LOG = LoggerFactory.getLogger(PacketHandler.class);

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    public PacketHandler(final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.dispatcher = dispatcher;
    }

//        final TelemetryMessage msg = new TelemetryMessage(ctx, buffer);

//                                // Build the message to dispatch via the Sink API
//                                final TelemetryMessage msg = new TelemetryMessage(packet.sender(), buffer);
//                                // Dispatch and retain a reference to the packet
//                                // in the case that we are sharing the underlying byte array
//                                final CompletableFuture<TelemetryMessage> future = dispatcher.send(msg);
//                                packet.retain();
//                                future.whenComplete((res,ex) -> packet.release());

    @Override
    protected void decode(final ChannelHandlerContext ctx, final Packet packet, final List<Object> out) throws Exception {
        LOG.info("Got packet: {}", packet);

        for (final Set<?> set : packet.sets) {
            if (set.header.getType() != SetHeader.Type.DATA_SET) {
                continue;
            }

            final Set<DataRecord> dataRecordSet = (Set<DataRecord>) set;

            for (final DataRecord record : dataRecordSet.records) {
                final Flows.Flow.Builder flow = Flows.Flow.newBuilder()
                        .setSourceId(packet.header.observationDomainId)
                        .setScopeFieldCount(record.template.scopeFieldsCount);

                final FlowBuilderVisitor visitor = new FlowBuilderVisitor(flow);
                for (final FieldValue field : record.fields) {
                    field.value.visit(visitor);
                }

                out.add(flow);
            }
        }
    }

    private static class FlowBuilderVisitor implements Value.Visitor {
        private final Flows.Flow.Builder flow;
        private final Iterable<String> prefix;

        private FlowBuilderVisitor(final Flows.Flow.Builder flow) {
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

        @Override
        public void accept(final BasicListValue value) {
            for (int i = 0; i < value.getValue().size(); i++) {
                final FlowBuilderVisitor visitor = new FlowBuilderVisitor(this.flow, this.buildName(value.getName(), Integer.toString(i)));
                value.getValue().get(i).visit(visitor);
            }
        }

        @Override
        public void accept(final BooleanValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setBool(value.getValue());
        }

        @Override
        public void accept(final DateTimeMicrosecondsValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setTimestamp(Flows.Entry.Timestamp.newBuilder()
                            .setSeconds(value.getValue().getEpochSecond())
                            .setNanos(value.getValue().getNano()));
        }

        @Override
        public void accept(final DateTimeMillisecondsValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setTimestamp(Flows.Entry.Timestamp.newBuilder()
                            .setSeconds(value.getValue().getEpochSecond())
                            .setNanos(value.getValue().getNano()));
        }

        @Override
        public void accept(final DateTimeNanosecondsValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setTimestamp(Flows.Entry.Timestamp.newBuilder()
                            .setSeconds(value.getValue().getEpochSecond())
                            .setNanos(value.getValue().getNano()));
        }

        @Override
        public void accept(final DateTimeSecondsValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setTimestamp(Flows.Entry.Timestamp.newBuilder()
                            .setSeconds(value.getValue().getEpochSecond())
                            .setNanos(value.getValue().getNano()));
        }

        @Override
        public void accept(final Float32Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setFloat(value.getValue());
        }

        @Override
        public void accept(final Float64Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setFloat(value.getValue());
        }

        @Override
        public void accept(final IPv4AddressValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setIpv4Address(ByteString.copyFrom(value.getValue().getAddress()));
        }

        @Override
        public void accept(final IPv6AddressValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setIpv6Address(ByteString.copyFrom(value.getValue().getAddress()));
        }

        @Override
        public void accept(final MacAddressValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setMacAddress(ByteString.copyFrom(value.getValue()));
        }

        @Override
        public void accept(final OctetArrayValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setBytes(ByteString.copyFrom(value.getValue()));
        }

        @Override
        public void accept(final Signed8Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setSigned(value.getValue());
        }

        @Override
        public void accept(final Signed16Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setSigned(value.getValue());
        }

        @Override
        public void accept(final Signed32Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setSigned(value.getValue());
        }

        @Override
        public void accept(final Signed64Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setSigned(value.getValue());
        }

        @Override
        public void accept(final StringValue value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setString(value.getValue());
        }

        @Override
        public void accept(final SubTemplateListValue value) {
            for (int i = 0; i < value.getValue().size(); i++) {
                final FlowBuilderVisitor visitor = new FlowBuilderVisitor(this.flow, this.buildName(value.getName(), Integer.toString(i)));
                for (int j = 0; j < value.getValue().get(i).size(); j++) {
                    value.getValue().get(i).get(j).visit(visitor);
                }
            }
        }

        @Override
        public void accept(final SubTemplateMultiListValue value) {
            for (int i = 0; i < value.getValue().size(); i++) {
                final FlowBuilderVisitor visitor = new FlowBuilderVisitor(this.flow, this.buildName(value.getName(), Integer.toString(i)));
                for (int j = 0; j < value.getValue().get(i).size(); j++) {
                    value.getValue().get(i).get(j).visit(visitor);
                }
            }
        }

        @Override
        public void accept(final Unsigned8Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setUnsigned(value.getValue().longValue());
        }

        @Override
        public void accept(final Unsigned16Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setUnsigned(value.getValue().longValue());
        }

        @Override
        public void accept(final Unsigned32Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setUnsigned(value.getValue().longValue());
        }

        @Override
        public void accept(final Unsigned64Value value) {
            flow.addEntriesBuilder()
                    .addAllKey(this.buildName(value.getName()))
                    .setUnsigned(value.getValue().longValue());
        }
    }
}
