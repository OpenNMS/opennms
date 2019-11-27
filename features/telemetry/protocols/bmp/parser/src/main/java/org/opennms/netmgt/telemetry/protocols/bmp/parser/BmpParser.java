/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.slice;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.bson.BsonBinaryWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.TcpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Aggregator;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPath;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AtomicAggregate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Attribute;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LocalPref;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiExistDisc;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.NextHop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Origin;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.InformationElement;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.InitiationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerDownPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerUpPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMirroringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMonitoringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.StatisticsReportPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.TerminationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.LocalBgpNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.LocalNoNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.Reason;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.RemoteBgpNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.RemoteNoNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.Unknown;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.AdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.AdjRibOut;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicatePrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateUpdate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateWithdraws;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.ExportRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsConfedLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsPathLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToClusterListLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToOriginatorId;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.LocRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Metric;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiAdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiLocRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PrefixTreatAsWithdraw;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Rejected;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.UpdateTreatAsWithdraw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.primitives.UnsignedLong;

public class BmpParser implements TcpParser {
    public static final Logger LOG = LoggerFactory.getLogger(BmpParser.class);

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final Meter recordsDispatched;

    public BmpParser(final String name,
                     final AsyncDispatcher<TelemetryMessage> dispatcher,
                     final MetricRegistry metricRegistry) {
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);

        this.recordsDispatched = metricRegistry.meter(MetricRegistry.name("parsers",  name, "recordsDispatched"));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {}

    @Override
    public void stop() {}

    @Override
    public Handler accept(final InetSocketAddress remoteAddress,
                          final InetSocketAddress localAddress) {
        return buffer -> {
            final Header header = new Header(slice(buffer, Header.SIZE));
            final Packet packet = header.parsePayload(slice(buffer, header.length - Header.SIZE));

            LOG.trace("Got packet: {}", packet);

            final BasicOutputBuffer output = new BasicOutputBuffer();
            try (final BsonBinaryWriter writer = new BsonBinaryWriter(output)) {
                writer.writeStartDocument();

                writer.writeInt32("@version", header.version);
                writer.writeString("@type", header.type.name());

                packet.accept(new Serializer(writer));

                writer.writeEndDocument();
            }

            this.recordsDispatched.mark();

            final TelemetryMessage message = new TelemetryMessage(remoteAddress, output.getByteBuffers().get(0).asNIO());
            return dispatcher.send(message);
        };
    }

    private static class Serializer implements Packet.Visitor {
        private final BsonBinaryWriter writer;

        private Serializer(final BsonBinaryWriter writer) {
            this.writer = Objects.requireNonNull(writer);
        }

        @Override
        public void visit(final InitiationPacket packet) {
            packet.information.first(InformationElement.Type.SYS_NAME)
                    .ifPresent(v -> this.writer.writeString("sys_name", v));

            final String sysDescr = packet.information.all(InformationElement.Type.SYS_DESCR)
                    .collect(Collectors.joining("\n"));
            if (!sysDescr.isEmpty()) {
                this.writer.writeString("sys_desc", sysDescr);
            }

            final String message = packet.information.all(InformationElement.Type.STRING)
                    .collect(Collectors.joining("\n"));
            if (!message.isEmpty()) {
                this.writer.writeString("message", message);
            }
        }

        @Override
        public void visit(final PeerUpPacket packet) {
            this.writePeerHeader(packet.peerHeader);

            this.writer.writeString("local_address", packet.localAddress.getHostAddress());
            this.writer.writeInt64("local_port", packet.localPort);
            this.writer.writeInt64("remote_port", packet.remotePort);

            this.writer.writeStartDocument("send_open_msg");
            this.writer.writeInt64("version", packet.sendOpenMessage.version);
            this.writer.writeInt64("as", packet.sendOpenMessage.as);
            this.writer.writeInt64("holdTime", packet.sendOpenMessage.holdTime);
            this.writer.writeInt64("id", packet.sendOpenMessage.id);
            this.writer.writeEndDocument();

            this.writer.writeStartDocument("recv_open_msg");
            this.writer.writeInt64("version", packet.recvOpenMessage.version);
            this.writer.writeInt64("as", packet.recvOpenMessage.as);
            this.writer.writeInt64("holdTime", packet.recvOpenMessage.holdTime);
            this.writer.writeInt64("id", packet.recvOpenMessage.id);
            this.writer.writeEndDocument();

            packet.information.first(InformationElement.Type.SYS_NAME)
                    .ifPresent(v -> this.writer.writeString("sys_name", v));

            final String sysDescr = packet.information.all(InformationElement.Type.SYS_DESCR)
                    .collect(Collectors.joining("\n"));
            if (!sysDescr.isEmpty()) {
                this.writer.writeString("sys_desc", sysDescr);
            }

            final String message = packet.information.all(InformationElement.Type.STRING)
                    .collect(Collectors.joining("\n"));
            if (!message.isEmpty()) {
                this.writer.writeString("message", message);
            }
        }

        @Override
        public void visit(final PeerDownPacket packet) {
            this.writePeerHeader(packet.peerHeader);

            this.writer.writeString("type", packet.type.name());

            packet.reason.accept(new Reason.Visitor() {
                @Override
                public void visit(final LocalBgpNotification localNotification) {
                    Serializer.this.writer.writeString("error", localNotification.notification.error.name());
                }

                @Override
                public void visit(final LocalNoNotification localNoNotification) {
                    Serializer.this.writer.writeInt64("code", localNoNotification.code);
                }

                @Override
                public void visit(final RemoteBgpNotification remoteNotification) {
                    Serializer.this.writer.writeString("error", remoteNotification.notification.error.name());
                }

                @Override
                public void visit(final RemoteNoNotification remoteNoNotification) {
                    // No data
                }

                @Override
                public void visit(final Unknown unknown) {
                    // No data
                }
            });
        }

        @Override
        public void visit(final RouteMonitoringPacket packet) {
            this.writePeerHeader(packet.peerHeader);

            this.writer.writeStartArray("withdraw");
            for (final UpdatePacket.Prefix prefix : packet.updateMessage.withdrawRoutes) {
                this.writer.writeStartDocument();
                this.writer.writeInt64("length", prefix.length);
                this.writer.writeString("prefix", prefix.prefix.getHostAddress());
                this.writer.writeEndDocument();
            }
            this.writer.writeEndArray();

            this.writer.writeStartArray("reachable");
            for (final UpdatePacket.Prefix prefix : packet.updateMessage.reachableRoutes) {
                this.writer.writeStartDocument();
                this.writer.writeInt64("length", prefix.length);
                this.writer.writeString("prefix", prefix.prefix.getHostAddress());
                this.writer.writeEndDocument();
            }
            this.writer.writeEndArray();

            this.writer.writeStartArray("path_attributes");
            for (final UpdatePacket.PathAttribute attribute : packet.updateMessage.pathAttributes) {
                this.writer.writeStartDocument();
                this.writer.writeBoolean("optional", attribute.optional);
                this.writer.writeBoolean("transitive", attribute.transitive);
                this.writer.writeBoolean("partial", attribute.partial);
                this.writer.writeBoolean("extended", attribute.extended);

                this.writer.writeString("type", attribute.type.name());

                attribute.attribute.accept(new Attribute.Visitor() {
                    @Override
                    public void visit(final Aggregator aggregator) {
                        Serializer.this.writer.writeInt64("as", aggregator.as);
                        Serializer.this.writer.writeString("address", aggregator.address.getHostAddress());
                    }

                    @Override
                    public void visit(final AsPath asPath) {
                        Serializer.this.writer.writeStartArray("segments");
                        for (final AsPath.Segment segment : asPath.segments) {
                            Serializer.this.writer.writeStartDocument();
                            Serializer.this.writer.writeString("type", segment.type.name());
                            Serializer.this.writer.writeStartArray("path");
                            for (final long as : segment.path) {
                                Serializer.this.writer.writeInt64(as);
                            }
                            Serializer.this.writer.writeEndArray();
                            Serializer.this.writer.writeEndDocument();
                        }
                        Serializer.this.writer.writeEndArray();
                    }

                    @Override
                    public void visit(final AtomicAggregate atomicAggregate) {
                        // No data
                    }

                    @Override
                    public void visit(final LocalPref localPref) {
                        Serializer.this.writer.writeInt64("preference", localPref.preference);
                    }

                    @Override
                    public void visit(final MultiExistDisc multiExistDisc) {
                        Serializer.this.writer.writeInt64("discriminator", multiExistDisc.discriminator);
                    }

                    @Override
                    public void visit(final NextHop nextHop) {
                        Serializer.this.writer.writeString("address", nextHop.address.getHostAddress());
                    }

                    @Override
                    public void visit(final Origin origin) {
                        Serializer.this.writer.writeString("value", origin.value.name());
                    }
                });

                this.writer.writeEndDocument();
            }
            this.writer.writeEndArray();
        }

        @Override
        public void visit(final StatisticsReportPacket packet) {
            this.writePeerHeader(packet.peerHeader);

            for (final StatisticsReportPacket.Element statistic : packet.statistics) {
                statistic.value.accept(new Metric.Visitor() {
                    @Override
                    public void visit(final DuplicatePrefix duplicatePrefix) {
                        this.writeCounter("duplicate_prefix", duplicatePrefix.counter);
                    }

                    @Override
                    public void visit(final DuplicateWithdraws duplicateWithdraws) {
                        this.writeCounter("duplicate_withdraws", duplicateWithdraws.counter);
                    }

                    @Override
                    public void visit(final AdjRibIn adjRibIn) {
                        this.writeGauge("adj_rib_in", adjRibIn.gauge);
                    }

                    @Override
                    public void visit(final AdjRibOut adjRibOut) {
                        this.writeGauge("adj_rib_out", adjRibOut.gauge);
                    }

                    @Override
                    public void visit(final ExportRib exportRib) {
                        this.writeGauge("export_rib", exportRib.gauge);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToAsConfedLoop invalidUpdateDueToAsConfedLoop) {
                        this.writeCounter("invalid_update_due_to_as_confed_loop", invalidUpdateDueToAsConfedLoop.counter);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToAsPathLoop invalidUpdateDueToAsPathLoop) {
                        this.writeCounter("invalid_update_due_to_as_path_loop", invalidUpdateDueToAsPathLoop.counter);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToClusterListLoop invalidUpdateDueToClusterListLoop) {
                        this.writeCounter("invalid_update_due_to_cluster_list_loop", invalidUpdateDueToClusterListLoop.counter);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToOriginatorId invalidUpdateDueToOriginatorId) {
                        this.writeCounter("invalid_update_due_to_originator_id", invalidUpdateDueToOriginatorId.counter);
                    }

                    @Override
                    public void visit(final PerAfiAdjRibIn perAfiAdjRibIn) {
                        final String name = new StringJoiner(":")
                                .add("per_afi_adj_rib_in")
                                .add(Integer.toString(perAfiAdjRibIn.afi))
                                .add(Integer.toString(perAfiAdjRibIn.safi))
                                .toString();
                        this.writeGauge(name, perAfiAdjRibIn.gauge);
                    }

                    @Override
                    public void visit(final PerAfiLocRib perAfiLocRib) {
                        final String name = new StringJoiner(":")
                            .add("per_afi_loc_rib")
                                .add(Integer.toString(perAfiLocRib.afi))
                                .add(Integer.toString(perAfiLocRib.safi))
                                .toString();
                        this.writeGauge(name, perAfiLocRib.gauge);
                    }

                    @Override
                    public void visit(final PrefixTreatAsWithdraw prefixTreatAsWithdraw) {
                        this.writeCounter("prefix_treat_as_withdraw", prefixTreatAsWithdraw.counter);
                    }

                    @Override
                    public void visit(final UpdateTreatAsWithdraw updateTreatAsWithdraw) {
                        this.writeCounter("update_treat_as_withdraw", updateTreatAsWithdraw.counter);
                    }

                    @Override
                    public void visit(final LocRib locRib) {
                        this.writeGauge("loc_rib", locRib.gauge);
                    }

                    @Override
                    public void visit(final DuplicateUpdate duplicateUpdate) {
                        this.writeCounter("duplicate_update", duplicateUpdate.counter);
                    }

                    @Override
                    public void visit(final Rejected rejected) {
                        this.writeCounter("rejected", rejected.counter);
                    }

                    private void writeCounter(final String name, final long counter) {
                        Serializer.this.writer.writeStartDocument(name);
                        Serializer.this.writer.writeInt64("counter", counter);
                        Serializer.this.writer.writeEndDocument();
                    }

                    private void writeGauge(final String name, final UnsignedLong gauge) {
                        Serializer.this.writer.writeStartDocument(name);
                        Serializer.this.writer.writeInt64("gauge", gauge.longValue());
                        Serializer.this.writer.writeEndDocument();
                    }
                });
            }
        }

        @Override
        public void visit(final TerminationPacket packet) {
            this.writer.writeStartArray("information");
            for (final TerminationPacket.Element information : packet.information) {
                this.writer.writeString(information.value);
            }
            this.writer.writeEndArray();
        }

        @Override
        public void visit(final RouteMirroringPacket packet) {
            // Don't send out mirrored BGP packets.
        }

        private void writePeerHeader(final PeerHeader peerHeader) {
            this.writer.writeStartDocument("peer");

            this.writer.writeString("type", peerHeader.type.name());

            this.writer.writeInt32("ip_version", peerHeader.flags.addressVersion.map(v -> {switch(v) {
                case IP_V4: return 4;
                case IP_V6: return 6;
                default: throw new IllegalStateException();
            }}));
            this.writer.writeBoolean("post_policy", peerHeader.flags.postPolicy);
            this.writer.writeBoolean("legacy_as_path", peerHeader.flags.legacyASPath);

            this.writer.writeInt64("distinguisher", peerHeader.distinguisher.longValue());
            this.writer.writeString("address", peerHeader.address.getHostAddress());
            this.writer.writeInt64("as", peerHeader.as);
            this.writer.writeInt64("id", peerHeader.id);

            this.writer.writeStartDocument("timestamp");
            this.writer.writeInt64("epoch", peerHeader.timestamp.getEpochSecond());
            if (peerHeader.timestamp.getNano() != 0) {
                this.writer.writeInt64("nanos", peerHeader.timestamp.getNano());
            }
            this.writer.writeEndDocument();

            this.writer.writeEndDocument();
        }
    }
}
