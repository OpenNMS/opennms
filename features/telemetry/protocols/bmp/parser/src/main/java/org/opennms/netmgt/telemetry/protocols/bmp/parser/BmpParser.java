/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.TcpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.Capability;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Aggregator;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPath;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPathLimit;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AtomicAggregate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AttrSet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Attribute;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.ClusterList;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Community;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Connector;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.ExtendedCommunities;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.ExtendedV6Communities;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LargeCommunities;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LocalPref;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiExistDisc;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.NextHop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Origin;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.OriginatorId;
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
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateWithdraw;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.ExportRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsConfedLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsPathLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToClusterListLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToOriginatorId;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.LocalRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Metric;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiAdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiAdjRibOut;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiExportRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiLocalRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PrefixTreatAsWithdraw;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Rejected;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.UpdateTreatAsWithdraw;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;

import io.netty.buffer.ByteBuf;

public class BmpParser implements TcpParser {
    public static final Logger LOG = LoggerFactory.getLogger(BmpParser.class);

    public final static long HEARTBEAT_INTERVAL = 4 * 60 * 60 * 1000;

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final Meter recordsDispatched;

    private ScheduledFuture<?> heartbeatFuture;

    private HashSet<InetAddress> connections = Sets.newHashSet();

    public BmpParser(final String name,
                     final AsyncDispatcher<TelemetryMessage> dispatcher,
                     final MetricRegistry metricRegistry) {
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);

        this.recordsDispatched = metricRegistry.meter(MetricRegistry.name("parsers", name, "recordsDispatched"));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
        this.sendHeartbeat(HeartbeatMode.STARTED);
        this.heartbeatFuture = executorService.scheduleAtFixedRate(() -> this.sendHeartbeat(HeartbeatMode.PERIODIC),
                                                                   HEARTBEAT_INTERVAL,
                                                                   HEARTBEAT_INTERVAL,
                                                                   TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        this.heartbeatFuture.cancel(false);
        this.sendHeartbeat(HeartbeatMode.STOPPED);
    }

    @Override
    public Handler accept(final InetSocketAddress remoteAddress,
                          final InetSocketAddress localAddress) {
        return new Handler() {
            private InetAddress bgpId;

            @Override
            public Optional<CompletableFuture<?>> parse(ByteBuf buffer) throws Exception {
                buffer.markReaderIndex();

                final Header header;
                if (buffer.isReadable(Header.SIZE)) {
                    header = new Header(slice(buffer, Header.SIZE));
                } else {
                    buffer.resetReaderIndex();
                    return Optional.empty();
                }

                final Packet packet;
                if (buffer.isReadable(header.payloadLength())) {
                    packet = header.parsePayload(slice(buffer, header.payloadLength()));
                } else {
                    buffer.resetReaderIndex();
                    return Optional.empty();
                }

                LOG.trace("Got packet: {}", packet);

                packet.accept(new Packet.Visitor.Adapter() {
                    @Override
                    public void visit(InitiationPacket packet) {
                        packet.information.first(InformationElement.Type.BGP_ID)
                                .map(InetAddressUtils::addr)
                                .ifPresent(_bgpId -> bgpId = _bgpId);
                    }
                });

                final Transport.Message.Builder message = Transport.Message.newBuilder()
                                                                           .setVersion(header.version);

                if (bgpId != null) {
                    message.setBgpId(address(bgpId));
                }

                packet.accept(new Serializer(message));

                final CompletableFuture<AsyncDispatcher.DispatchStatus> dispatched = dispatcher.send(new TelemetryMessage(remoteAddress, ByteBuffer.wrap(message.build().toByteArray())));
                BmpParser.this.recordsDispatched.mark();

                return Optional.of(dispatched);
            }

            @Override
            public void active() {
                BmpParser.this.connections.add(remoteAddress.getAddress());
                BmpParser.this.sendHeartbeat(HeartbeatMode.CHANGE);
            }

            @Override
            public void inactive() {
                BmpParser.this.connections.remove(remoteAddress.getAddress());
                BmpParser.this.sendHeartbeat(HeartbeatMode.CHANGE);
            }
        };
    }

    private enum HeartbeatMode {
        STARTED,
        CHANGE,
        PERIODIC,
        STOPPED;

        public <R> R map(final Function<HeartbeatMode, R> mapper) {
            return mapper.apply(this);
        }
    }

    private void sendHeartbeat(final HeartbeatMode mode) {
        final Transport.Message.Builder message = Transport.Message.newBuilder();

        message.getHeartbeatBuilder()
               .setMode(mode.map(m -> { switch(m) {
                   case STARTED: return Transport.Heartbeat.Mode.STARTED;
                   case STOPPED: return Transport.Heartbeat.Mode.STOPPED;
                   case PERIODIC: return Transport.Heartbeat.Mode.PERIODIC;
                   case CHANGE: return Transport.Heartbeat.Mode.CHANGE;
                   default: throw new IllegalStateException();
               }}))
               .addAllRouters(Iterables.transform(this.connections, BmpParser::address));

        this.dispatcher.send(new TelemetryMessage(InetSocketAddress.createUnresolved("0.0.0.0", 0), ByteBuffer.wrap(message.build().toByteArray())));
        BmpParser.this.recordsDispatched.mark();
    }

    private static class Serializer implements Packet.Visitor {
        private final Transport.Message.Builder message;

        private Serializer(final Transport.Message.Builder message) {
            this.message = Objects.requireNonNull(message);
        }

        private static Transport.Peer peer(final PeerHeader peerHeader) {
            final Transport.Peer.Builder peer = Transport.Peer.newBuilder();

            peer.setType(peerHeader.type.map(v -> {
                switch (v) {
                    case GLOBAL_INSTANCE:
                        return Transport.Peer.Type.GLOBAL_INSTANCE;
                    case RD_INSTANCE:
                        return Transport.Peer.Type.RD_INSTANCE;
                    case LOCAL_INSTANCE:
                        return Transport.Peer.Type.LOCAL_INSTANCE;
                    default:
                        throw new IllegalStateException();
                }
            }));

            if (peerHeader.type == PeerHeader.Type.LOC_RIB_INSTANCE) {
                final Transport.Peer.LocRibFlags.Builder locRibFlags = peer.getLocRibFlagsBuilder();
                locRibFlags.setFiltered(peerHeader.locRibFlags.filtered);
                peer.setLocRibFlags(locRibFlags.build());
            } else {
                final Transport.Peer.PeerFlags.Builder peerFlags = peer.getPeerFlagsBuilder();
                peerFlags.setIpVersion(peerHeader.flags.addressVersion.map(v -> {
                    switch (v) {
                        case IP_V4:
                            return Transport.Peer.PeerFlags.IpVersion.IP_V4;
                        case IP_V6:
                            return Transport.Peer.PeerFlags.IpVersion.IP_V6;
                        default:
                            throw new IllegalStateException();
                    }
                }));
                peerFlags.setPolicy(peerHeader.flags.policy.map(v -> {
                    switch (v) {
                        case PRE_POLICY:
                            return Transport.Peer.PeerFlags.Policy.PRE_POLICY;
                        case POST_POLICY:
                            return Transport.Peer.PeerFlags.Policy.POST_POLICY;
                        default:
                            throw new IllegalStateException();
                    }
                }));
                peerFlags.setLegacyAsPath(peerHeader.flags.legacyASPath);
                peerFlags.setAdjIn(peerHeader.flags.adjIn);
                peer.setPeerFlags(peerFlags.build());
            }

            peer.setDistinguisher(peerHeader.distinguisher.longValue());
            peer.setAddress(address(peerHeader.address));
            peer.setAs((int) peerHeader.as);
            peer.setId(address(peerHeader.id));

            peer.getTimestampBuilder()
                .setSeconds(peerHeader.timestamp.getEpochSecond())
                .setNanos(peerHeader.timestamp.getNano());

            return peer.build();
        }

        @Override
        public void visit(final InitiationPacket packet) {
            final Transport.InitiationPacket.Builder message = this.message.getInitiationBuilder();
            message.setSysName(packet.information.first(InformationElement.Type.SYS_NAME)
                                                 .orElse(""));
            message.addAllSysDesc(packet.information.all(InformationElement.Type.SYS_DESCR)
                                                    .collect(Collectors.toList()));
            message.addAllMessage(packet.information.all(InformationElement.Type.STRING)
                                                    .collect(Collectors.toList()));
            packet.information.first(InformationElement.Type.BGP_ID)
                    .map(addr -> address(InetAddressUtils.addr(addr)))
                    .ifPresent(message::setBgpId);
        }

        @Override
        public void visit(final TerminationPacket packet) {
            final Transport.TerminationPacket.Builder message = this.message.getTerminationBuilder();

            for (final TerminationPacket.Element information : packet.information) {
                information.value.accept(new TerminationPacket.Information.Visitor() {
                    @Override
                    public void visit(TerminationPacket.StringInformation string) {
                        message.addMessage(string.string);
                    }

                    @Override
                    public void visit(TerminationPacket.ReasonInformation reason) {
                        message.setReason(reason.reason);
                    }

                    @Override
                    public void visit(TerminationPacket.UnknownInformation unknown) {
                        message.getUnknownBuilder();
                    }
                });
            }
        }

        @Override
        public void visit(final PeerUpPacket packet) {
            final Transport.PeerUpPacket.Builder message = this.message.getPeerUpBuilder();
            message.setPeer(peer(packet.peerHeader));

            message.setLocalAddress(address(packet.localAddress));
            message.setLocalPort(packet.localPort);
            message.setRemotePort(packet.remotePort);

            Transport.PeerUpPacket.CapabilityList.Builder sendCapabilitiesBuilder = Transport.PeerUpPacket.CapabilityList.newBuilder();
            for (final Capability capability : packet.sendOpenMessage.capabilities) {
                sendCapabilitiesBuilder.addCapability(Transport.PeerUpPacket.Capability.newBuilder()
                        .setCode(capability.getCode())
                        .setLength(capability.getLength())
                        .setValue(capability.getValue())
                        .build());
            }

            message.getSendMsgBuilder()
                   .setVersion(packet.sendOpenMessage.version)
                   .setAs(packet.sendOpenMessage.as)
                   .setHoldTime(packet.sendOpenMessage.holdTime)
                   .setId(address(packet.sendOpenMessage.id))
                   .setCapabilities(sendCapabilitiesBuilder.build());

            Transport.PeerUpPacket.CapabilityList.Builder recvCapabilitiesBuilder = Transport.PeerUpPacket.CapabilityList.newBuilder();
            for (final Capability capability : packet.recvOpenMessage.capabilities) {
                recvCapabilitiesBuilder.addCapability(Transport.PeerUpPacket.Capability.newBuilder()
                        .setCode(capability.getCode())
                        .setLength(capability.getLength())
                        .setValue(capability.getValue())
                        .build());
            }

            message.getRecvMsgBuilder()
                   .setVersion(packet.recvOpenMessage.version)
                   .setAs(packet.recvOpenMessage.as)
                   .setHoldTime(packet.recvOpenMessage.holdTime)
                   .setId(address(packet.recvOpenMessage.id))
                   .setCapabilities(recvCapabilitiesBuilder.build());

            message.setSysName(packet.information.first(InformationElement.Type.SYS_NAME)
                                                 .orElse(""));
            message.setTableName(packet.information.first(InformationElement.Type.VRF_TABLE_NAME)
                    .orElse(""));
            message.setSysDesc(packet.information.all(InformationElement.Type.SYS_DESCR)
                                                 .collect(Collectors.joining("\n")));
            message.setMessage(packet.information.all(InformationElement.Type.STRING)
                                                 .collect(Collectors.joining("\n")));
        }

        @Override
        public void visit(final PeerDownPacket packet) {
            final Transport.PeerDownPacket.Builder message = this.message.getPeerDownBuilder();
            message.setPeer(peer(packet.peerHeader));

            packet.reason.accept(new Reason.Visitor() {
                @Override
                public void visit(final LocalBgpNotification localBgpNotification) {
                    message.getLocalBgpNotificationBuilder()
                            .setCode(localBgpNotification.notification.code)
                            .setSubcode(localBgpNotification.notification.subcode);
                }

                @Override
                public void visit(final LocalNoNotification localNoNotification) {
                    message.setLocalNoNotification(localNoNotification.code);
                }

                @Override
                public void visit(final RemoteBgpNotification remoteBgpNotification) {
                    message.getRemoteBgpNotificationBuilder()
                            .setCode(remoteBgpNotification.notification.code)
                            .setSubcode(remoteBgpNotification.notification.subcode);
                }

                @Override
                public void visit(final RemoteNoNotification remoteNoNotification) {
                    message.setRemoteNoNotification(Empty.getDefaultInstance());
                }

                @Override
                public void visit(final Unknown unknown) {
                    message.setUnknown(Empty.getDefaultInstance());
                }
            });
        }

        @Override
        public void visit(final StatisticsReportPacket packet) {
            final Transport.StatisticsReportPacket.Builder message = this.message.getStatisticsReportBuilder();
            message.setPeer(peer(packet.peerHeader));

            for (final StatisticsReportPacket.Element statistic : packet.statistics) {
                statistic.value.accept(new Metric.Visitor() {
                    @Override
                    public void visit(final DuplicatePrefix duplicatePrefix) {
                        message.getDuplicatePrefixBuilder().setCount((int) duplicatePrefix.counter);
                    }

                    @Override
                    public void visit(final DuplicateWithdraw duplicateWithdraw) {
                        message.getDuplicateWithdrawBuilder().setCount((int) duplicateWithdraw.counter);
                    }

                    @Override
                    public void visit(final AdjRibIn adjRibIn) {
                        message.getAdjRibInBuilder().setValue(adjRibIn.gauge.longValue());
                    }

                    @Override
                    public void visit(final AdjRibOut adjRibOut) {
                        message.getAdjRibOutBuilder().setValue(adjRibOut.gauge.longValue());
                    }

                    @Override
                    public void visit(final ExportRib exportRib) {
                        message.getExportRibBuilder().setValue(exportRib.gauge.longValue());
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToAsConfedLoop invalidUpdateDueToAsConfedLoop) {
                        message.getInvalidUpdateDueToAsConfedLoopBuilder().setCount((int) invalidUpdateDueToAsConfedLoop.counter);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToAsPathLoop invalidUpdateDueToAsPathLoop) {
                        message.getInvalidUpdateDueToAsPathLoopBuilder().setCount((int) invalidUpdateDueToAsPathLoop.counter);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToClusterListLoop invalidUpdateDueToClusterListLoop) {
                        message.getInvalidUpdateDueToClusterListLoopBuilder().setCount((int) invalidUpdateDueToClusterListLoop.counter);
                    }

                    @Override
                    public void visit(final InvalidUpdateDueToOriginatorId invalidUpdateDueToOriginatorId) {
                        message.getInvalidUpdateDueToOriginatorIdBuilder().setCount((int) invalidUpdateDueToOriginatorId.counter);
                    }

                    @Override
                    public void visit(final PerAfiAdjRibIn perAfiAdjRibIn) {
                        final String key = String.format("%d:%d", perAfiAdjRibIn.afi, perAfiAdjRibIn.safi);
                        message.putPerAfiAdjRibIn(key, Transport.StatisticsReportPacket.Gauge.newBuilder()
                                                                                             .setValue(perAfiAdjRibIn.gauge.longValue())
                                                                                             .build());
                    }

                    @Override
                    public void visit(final PerAfiLocalRib perAfiLocalRib) {
                        final String key = String.format("%d:%d", perAfiLocalRib.afi, perAfiLocalRib.safi);
                        message.putPerAfiLocalRib(key, Transport.StatisticsReportPacket.Gauge.newBuilder()
                                                                                             .setValue(perAfiLocalRib.gauge.longValue())
                                                                                             .build());
                    }

                    @Override
                    public void visit(final PrefixTreatAsWithdraw prefixTreatAsWithdraw) {
                        message.getPrefixTreatAsWithdrawBuilder().setCount((int) prefixTreatAsWithdraw.counter);
                    }

                    @Override
                    public void visit(final UpdateTreatAsWithdraw updateTreatAsWithdraw) {
                        message.getUpdateTreatAsWithdrawBuilder().setCount((int) updateTreatAsWithdraw.counter);
                    }

                    @Override
                    public void visit(final LocalRib localRib) {
                        message.getLocalRibBuilder().setValue(localRib.gauge.longValue());
                    }

                    @Override
                    public void visit(final DuplicateUpdate duplicateUpdate) {
                        message.getDuplicateUpdateBuilder().setCount((int) duplicateUpdate.counter);
                    }

                    @Override
                    public void visit(final Rejected rejected) {
                        message.getRejectedBuilder().setCount((int) rejected.counter);
                    }

                    @Override
                    public void visit(final PerAfiAdjRibOut perAfiAdjRibOut) {
                        final String key = String.format("%d:%d", perAfiAdjRibOut.afi, perAfiAdjRibOut.safi);
                        message.putPerAfiAdjRibOut(key, Transport.StatisticsReportPacket.Gauge.newBuilder()
                                                                                              .setValue(perAfiAdjRibOut.gauge.longValue())
                                                                                              .build());
                    }

                    @Override
                    public void visit(final PerAfiExportRib perAfiExportRib) {
                        final String key = String.format("%d:%d", perAfiExportRib.afi, perAfiExportRib.safi);
                        message.putPerAfiExportRib(key, Transport.StatisticsReportPacket.Gauge.newBuilder()
                                                                                              .setValue(perAfiExportRib.gauge.longValue())
                                                                                              .build());
                    }

                    @Override
                    public void visit(final org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Unknown unknown) {
                    }
                });
            }
        }

        @Override
        public void visit(final RouteMonitoringPacket packet) {
            final Transport.RouteMonitoringPacket.Builder message = this.message.getRouteMonitoringBuilder();
            message.setPeer(peer(packet.peerHeader));

            for (final UpdatePacket.Prefix prefix : packet.updateMessage.withdrawRoutes) {
                message.addWithdrawsBuilder()
                       .setPrefix(address(prefix.prefix))
                       .setLength(prefix.length);
            }

            for (final UpdatePacket.Prefix prefix : packet.updateMessage.reachableRoutes) {
                message.addReachablesBuilder()
                       .setPrefix(address(prefix.prefix))
                       .setLength(prefix.length);
            }

            for (final UpdatePacket.PathAttribute attribute : packet.updateMessage.pathAttributes) {
                message.addAttributes(pathAttribute(attribute));
            }
        }

        @Override
        public void visit(final RouteMirroringPacket packet) {
            // Don't send out mirrored BGP packets.
        }
    }

    private static Transport.RouteMonitoringPacket.PathAttribute.Builder pathAttribute(UpdatePacket.PathAttribute attribute) {
        final Transport.RouteMonitoringPacket.PathAttribute.Builder attributesBuilder = Transport.RouteMonitoringPacket.PathAttribute.newBuilder();
        attributesBuilder.setOptional(attribute.optional)
                .setTransitive(attribute.transitive)
                .setPartial(attribute.partial)
                .setExtended(attribute.extended);

        attribute.attribute.accept(new Attribute.Visitor() {
            @Override
            public void visit(final Aggregator aggregator) {
                attributesBuilder.getAggregatorBuilder()
                        .setAs(aggregator.as)
                        .setAddress(address(aggregator.address));
            }

            @Override
            public void visit(final AsPath asPath) {
                final Transport.RouteMonitoringPacket.PathAttribute.AsPath.Builder asPathBuilder = attributesBuilder.getAsPathBuilder();
                for (final AsPath.Segment segment : asPath.segments) {
                    final Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Builder segmentBuilder = asPathBuilder.addSegmentsBuilder();
                    segmentBuilder.setType(segment.type.map(t -> {
                        switch (t) {
                            case AS_SET:
                                return Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SET;
                            case AS_SEQUENCE:
                                return Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SEQUENCE;
                            case UNKNOWN:
                                return Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.UNRECOGNIZED;
                            default:
                                throw new IllegalStateException();
                        }
                    }));
                    for (final long as : segment.path) {
                        segmentBuilder.addPaths((int) as);
                    }
                }
            }

            @Override
            public void visit(final AtomicAggregate atomicAggregate) {
                attributesBuilder.getAtomicAggregateBuilder();
            }

            @Override
            public void visit(final LocalPref localPref) {
                attributesBuilder.getLocalPrefBuilder()
                        .setPreference((int) localPref.preference);
            }

            @Override
            public void visit(final MultiExistDisc multiExistDisc) {
                attributesBuilder.getMultiExitDiscBuilder()
                        .setDiscriminator((int) multiExistDisc.discriminator);
            }

            @Override
            public void visit(final NextHop nextHop) {
                attributesBuilder.getNextHopBuilder()
                        .setAddress(address(nextHop.address));
            }

            @Override
            public void visit(final Origin origin) {
                attributesBuilder.setOrigin(origin.value.map(v -> {
                    switch (v) {
                        case IGP:
                            return Transport.RouteMonitoringPacket.PathAttribute.Origin.IGP;
                        case EGP:
                            return Transport.RouteMonitoringPacket.PathAttribute.Origin.EGP;
                        case INCOMPLETE:
                            return Transport.RouteMonitoringPacket.PathAttribute.Origin.INCOMPLETE;
                        case UNKNOWN:
                            return Transport.RouteMonitoringPacket.PathAttribute.Origin.UNRECOGNIZED;
                        default:
                            throw new IllegalStateException();
                    }
                }));
            }

            @Override
            public void visit(Community community) {
                attributesBuilder.setCommunity((int) community.community);
            }

            @Override
            public void visit(OriginatorId originatorId) {
                attributesBuilder.setOriginatorId((int) originatorId.originatorId);
            }

            @Override
            public void visit(ClusterList clusterList) {
                Transport.RouteMonitoringPacket.PathAttribute.ClusterList.Builder clusterListBuilder = Transport.RouteMonitoringPacket.PathAttribute.ClusterList.newBuilder();
                for (InetAddress clusterId : clusterList.clusterIds) {
                    clusterListBuilder.addClusterId(address(clusterId));
                }
                attributesBuilder.setClusterList(clusterListBuilder);
            }

            @Override
            public void visit(ExtendedCommunities extendedCommunities) {
                Transport.RouteMonitoringPacket.PathAttribute.ExtendedCommunities.Builder extendedCommunitiesBuilder = Transport.RouteMonitoringPacket.PathAttribute.ExtendedCommunities.newBuilder();
                for (ExtendedCommunities.ExtendedCommunity extendedCommunity : extendedCommunities.extendedCommunities) {
                    extendedCommunitiesBuilder.addExtendedCommunitiesBuilder()
                            .setHighType(extendedCommunity.highType)
                            .setLowType(extendedCommunity.lowType)
                            .setAuthoritative(extendedCommunity.authoritative)
                            .setTransitive(extendedCommunity.transitive)
                            .setType(extendedCommunity.value.type)
                            .setValue(extendedCommunity.value.value);
                }
                attributesBuilder.setExtendedCommunities(extendedCommunitiesBuilder);
            }

            @Override
            public void visit(ExtendedV6Communities extendedV6Communities) {
                Transport.RouteMonitoringPacket.PathAttribute.ExtendedV6Communities.Builder extendedV6CommunitiesBuilder = Transport.RouteMonitoringPacket.PathAttribute.ExtendedV6Communities.newBuilder();
                for (ExtendedV6Communities.ExtendedV6Community extendedCommunity : extendedV6Communities.extendedCommunities) {
                    extendedV6CommunitiesBuilder.addExtendedCommunitiesBuilder()
                                                .setHighType(extendedCommunity.highType)
                                                .setLowType(extendedCommunity.lowType)
                                                .setAuthoritative(extendedCommunity.authoritative)
                                                .setTransitive(extendedCommunity.transitive)
                                                .setType(extendedCommunity.value.type)
                                                .setValue(extendedCommunity.value.value);
                }
                attributesBuilder.setExtendedV6Communities(extendedV6CommunitiesBuilder);
            }

            @Override
            public void visit(Connector connector) {
                attributesBuilder.setConnector(connector.connector);
            }

            @Override
            public void visit(AsPathLimit asPathLimit) {
                attributesBuilder.setAsPathLimit(Transport.RouteMonitoringPacket.PathAttribute.AsPathLimit.newBuilder()
                        .setUpperBound(asPathLimit.upperBound)
                        .setAs((int) asPathLimit.as)
                        .build());
            }

            @Override
            public void visit(LargeCommunities largeCommunities) {
                Transport.RouteMonitoringPacket.PathAttribute.LargeCommunities.Builder largeCommunitiesBuilder = Transport.RouteMonitoringPacket.PathAttribute.LargeCommunities.newBuilder();
                for (LargeCommunities.LargeCommunity largeCommunity : largeCommunities.largeCommunities) {
                    largeCommunitiesBuilder.addLargeCommunitiesBuilder()
                            .setGlobalAdministrator((int)largeCommunity.globalAdministrator)
                            .setLocalDataPart1((int)largeCommunity.localDataPart1)
                            .setLocalDataPart2((int)largeCommunity.localDataPart2);
                }
                attributesBuilder.setLargeCommunities(largeCommunitiesBuilder);
            }

            @Override
            public void visit(AttrSet attrSet) {
                Transport.RouteMonitoringPacket.PathAttribute.AttrSet.Builder attrSetBuilder = Transport.RouteMonitoringPacket.PathAttribute.AttrSet.newBuilder()
                        .setOriginAs((int)attrSet.originAs);
                for (UpdatePacket.PathAttribute attribute : attrSet.pathAttributes) {
                    attrSetBuilder.addPathAttributes(pathAttribute(attribute));
                }
                attributesBuilder.setAttrSet(attrSetBuilder);
            }

            @Override
            public void visit(org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Unknown unknown) {
            }
        });
        return attributesBuilder;
    }

    public static Transport.IpAddress address(final InetAddress address) {
        final Transport.IpAddress.Builder builder = Transport.IpAddress.newBuilder();
        if (address instanceof Inet4Address) {
            builder.setV4(ByteString.copyFrom(address.getAddress()));
        } else if (address instanceof Inet6Address) {
            builder.setV6(ByteString.copyFrom(address.getAddress()));
        } else {
            throw new IllegalStateException();
        }

        return builder.build();
    }
}
