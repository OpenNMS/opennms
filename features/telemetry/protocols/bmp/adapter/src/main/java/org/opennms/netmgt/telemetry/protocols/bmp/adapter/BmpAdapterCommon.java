/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.address;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.addressAsStr;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.asAttr;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.getPathAttributeOfType;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.getPathAttributesOfType;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.isV4;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.timestamp;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.uint32;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.BmpMessageHandler;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.Context;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.AddressFamilyIdentifier;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.SubsequentAddressFamilyIdentifier;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BmpAdapterCommon {

    private static final Logger LOG = LoggerFactory.getLogger(BmpAdapterCommon.class);

    public enum Error {
        // Message Header Error
        CONNECTION_NOT_SYNCHRONIZED("Connection not synchronized"),
        BAD_MESSAGE_LENGTH("Bad message header length"),
        BAD_MESSAGE_TYPE("Bad message header type"),

        // Open Message Error
        UNSUPPORTED_VERSION_NUMBER("Unsupported BGP version"),
        BAD_PEER_AS("Incorrect peer AS"),
        BAD_BGP_IDENTIFIER("Bad BGP ID"),
        UNSUPPORTED_OPTIONAL_PARAMETER("Unsupported optinal parameter"),
        AUTHENTICATION_FAILURE("Authentication failure"),
        UNACCEPTABLE_HOLD_TIME("Unacceptable hold time"),

        // Update Message Error
        MALFORMED_ATTRIBUTE_LIST("Malformed attribute list"),
        UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE("Unrecognized well known attribute"),
        MISSING_WELL_KNOWN_ATTRIBUTE("Missing well known attribute"),
        ATTRIBUTE_FLAGS_ERROR("Update attribute flags error"),
        ATTRIBUTE_LENGTH_ERROR("Update attribute length error"),
        INVALID_ORIGIN_ATTRIBUTE("Invalid origin"),
        ROUTING_LOOP("Routing loop"),
        INVALID_NEXT_HOP_ATTRIBUTE("Invalid next hop address/attribute"),
        OPTIONAL_ATTRIBUTE_ERROR("Update optional attribute error"),
        INVALID_NETWORK_FIELD("Invalid network field"),
        MALFORMED_AS_PATH("Malformed AS_PATH"),

        // Hold Timer Expired
        HOLD_TIMER_EXPIRED("Hold timer expired"),

        // FSM Error
        FSM_ERROR("FSM error"),

        // Cease
        MAXIMUM_NUMBER_OF_PREFIXES_REACHED("Maximum number of prefixes reached"),
        ADMINISTRATIVE_SHUTDOWN("Administrative shutdown"),
        PEER_DECONFIGURED("Peer de-configured"),
        ADMINISTRATIVE_RESET("Administrative reset"),
        CONNECTION_RESET("Connection rejected"),
        OTHER_CONFIGURATION_CHANGE("Other configuration change"),
        CONNECTION_COLLISION_RESOLUTION("Connection collision resolution"),
        OUT_OF_RESOURCES("Maximum number of prefixes reached"),

        UNKNOWN("Unknown notification type"),
        ;

        private String errorText;

        Error(final String errorText) {
            this.errorText = errorText;
        }

        public String getErrorText() {
            return errorText;
        }

        public static Error from(final int code, final int subcode) {
            switch ((code << 8) + subcode) {
                case (1 << 8) + 1: return CONNECTION_NOT_SYNCHRONIZED;
                case (1 << 8) + 2: return BAD_MESSAGE_LENGTH;
                case (1 << 8) + 3: return BAD_MESSAGE_TYPE;

                case (2 << 8) + 1: return UNSUPPORTED_VERSION_NUMBER;
                case (2 << 8) + 2: return BAD_PEER_AS;
                case (2 << 8) + 3: return BAD_BGP_IDENTIFIER;
                case (2 << 8) + 4: return UNSUPPORTED_OPTIONAL_PARAMETER;
                case (2 << 8) + 5: return AUTHENTICATION_FAILURE;
                case (2 << 8) + 6: return UNACCEPTABLE_HOLD_TIME;

                case (3 << 8) + 1: return MALFORMED_ATTRIBUTE_LIST;
                case (3 << 8) + 2: return UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE;
                case (3 << 8) + 3: return MISSING_WELL_KNOWN_ATTRIBUTE;
                case (3 << 8) + 4: return ATTRIBUTE_FLAGS_ERROR;
                case (3 << 8) + 5: return ATTRIBUTE_LENGTH_ERROR;
                case (3 << 8) + 6: return INVALID_ORIGIN_ATTRIBUTE;
                case (3 << 8) + 7: return ROUTING_LOOP;
                case (3 << 8) + 8: return INVALID_NEXT_HOP_ATTRIBUTE;
                case (3 << 8) + 9: return OPTIONAL_ATTRIBUTE_ERROR;
                case (3 << 8) + 10: return INVALID_NETWORK_FIELD;
                case (3 << 8) + 11: return MALFORMED_AS_PATH;

                case (4 << 8) + 1: return HOLD_TIMER_EXPIRED;

                case (5 << 8) + 1: return FSM_ERROR;

                case (6 << 8) + 1: return MAXIMUM_NUMBER_OF_PREFIXES_REACHED;
                case (6 << 8) + 2: return ADMINISTRATIVE_SHUTDOWN;
                case (6 << 8) + 3: return PEER_DECONFIGURED;
                case (6 << 8) + 4: return ADMINISTRATIVE_RESET;
                case (6 << 8) + 5: return CONNECTION_RESET;
                case (6 << 8) + 6: return OTHER_CONFIGURATION_CHANGE;
                case (6 << 8) + 7: return CONNECTION_COLLISION_RESOLUTION;
                case (6 << 8) + 8: return OUT_OF_RESOURCES;

                default:
                    //LOG.warn("Unknown Notification Packet Code: {}/{}", code, subcode);
                    return UNKNOWN;
            }
        }
    }

    public static void handleBmpMessage(TelemetryMessageLogEntry messageLogEntry, TelemetryMessageLog messageLog,
                                        BmpMessageHandler messageHandler, final AtomicLong sequence) {
        LOG.trace("Parsing packet: {}", messageLogEntry);
        final Transport.Message message;
        try {
            message = Transport.Message.parseFrom(messageLogEntry.getByteArray());
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Invalid message", e);
            return;
        }

        final String collectorHashId = Record.hash(messageLog.getSystemId());
        final String routerHashId = Record.hash(messageLog.getSourceAddress());
        final Context context = new Context(messageLog.getSystemId(),
                collectorHashId,
                routerHashId,
                Instant.ofEpochMilli(messageLogEntry.getTimestamp()),
                InetAddressUtils.addr(messageLog.getSourceAddress()),
                messageLog.getSourcePort(), messageLog.getLocation());

        switch(message.getPacketCase()) {
            case HEARTBEAT:
                handleHeartbeatMessage(messageHandler, message, message.getHeartbeat(), context, sequence);
                break;
            case INITIATION:
                handleInitiationMessage(messageHandler, message, message.getInitiation(), context, sequence);
                break;
            case TERMINATION:
                handleTerminationMessage(messageHandler, message, message.getTermination(), context, sequence);
                break;
            case PEER_UP:
                handlePeerUpNotification(messageHandler, message, message.getPeerUp(), context, sequence);
                break;
            case PEER_DOWN:
                handlePeerDownNotification(messageHandler, message, message.getPeerDown(), context, sequence);
                break;
            case STATISTICS_REPORT:
                handleStatisticReport(messageHandler, message, message.getStatisticsReport(), context, sequence);
                break;
            case ROUTE_MONITORING:
                handleRouteMonitoringMessage(messageHandler, message, message.getRouteMonitoring(), context, sequence);
                break;
            case PACKET_NOT_SET:
                break;
        }

    }

    public static void handleHeartbeatMessage(final BmpMessageHandler messageHandler,
                                        final Transport.Message message,
                                        final Transport.Heartbeat heartbeat,
                                        final Context context,
                                        final AtomicLong sequence) {
        final Collector collector = new Collector();

        switch (heartbeat.getMode()) {
            case STARTED:
                collector.action = Collector.Action.STARTED;
                break;
            case STOPPED:
                collector.action = Collector.Action.STOPPED;
                break;
            case PERIODIC:
                collector.action = Collector.Action.HEARTBEAT;
                break;
            case CHANGE:
                collector.action = Collector.Action.CHANGE;
                break;
        }

        collector.sequence = sequence.getAndIncrement();
        collector.adminId = context.adminId;
        collector.hash = context.collectorHashId;
        collector.routers = Lists.transform(heartbeat.getRoutersList(), BmpAdapterTools::address);
        collector.timestamp = context.timestamp;

        messageHandler.handle(new Message(context.collectorHashId, Type.COLLECTOR, ImmutableList.of(collector)), context);
    }

    public static void handleInitiationMessage(final BmpMessageHandler messageHandler,
                                         final Transport.Message message,
                                         final Transport.InitiationPacket initiation,
                                         final Context context,
                                         final AtomicLong sequence) {
        final Router router = new Router();
        router.action = Router.Action.INIT;
        router.sequence = sequence.getAndIncrement();
        router.name = initiation.getSysName() != null
                ? initiation.getSysName()
                : initiation.getHostname();
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = Joiner.on('\n').join(initiation.getSysDescList());
        router.termCode = null;
        router.termReason = null;
        router.initData = Joiner.on('\n').join(initiation.getMessageList());
        router.termData = null;
        router.timestamp = context.timestamp;
        router.bgpId = initiation.hasBgpId() ? BmpAdapterTools.address(initiation.getBgpId()) : null;

        messageHandler.handle(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)), context);
    }

    public static void handleTerminationMessage(final BmpMessageHandler messageHandler, final Transport.Message message,
                                          final Transport.TerminationPacket termination,
                                          final Context context,
                                          final AtomicLong sequence) {
        final Router router = new Router();
        router.action = Router.Action.TERM;
        router.sequence = sequence.getAndIncrement();
        router.name = null;
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = null;
        router.termCode = termination.getReason();

        switch (router.termCode) {
            case 0:
                router.termReason = "Session administratively closed. The session might be re-initiated";
                break;
            case 1:
                router.termReason = "Unspecified reason";
                break;
            case 2:
                router.termReason = "Out of resources. The router has exhausted resources available for the BMP session";
                break;
            case 3:
                router.termReason = "Redundant connection. The router has determined that this connection is redundant with another one";
                break;
            case 4:
                router.termReason = "Session permanently administratively closed, will not be re-initiated";
                break;
            default:
                router.termReason = "Unknown reason";
        }

        router.initData = null;
        router.termData = Joiner.on('\n').join(termination.getMessageList());
        router.timestamp = context.timestamp;
        router.bgpId = null;

        messageHandler.handle(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)), context);
    }

    public static void handlePeerUpNotification(final BmpMessageHandler messageHandler, final Transport.Message message,
                                          final Transport.PeerUpPacket peerUp,
                                          final Context context,
                                          final AtomicLong sequence) {
        final Transport.Peer bgpPeer = peerUp.getPeer();

        final Peer peer = new Peer();
        peer.action = Peer.Action.UP;
        peer.sequence = sequence.getAndIncrement();
        peer.name = !Strings.isNullOrEmpty(bgpPeer.getHostname())
                ? bgpPeer.getHostname()
                : addressAsStr(bgpPeer.getAddress());
        peer.hash = Record.hash(bgpPeer.getAddress(),
                bgpPeer.getDistinguisher(),
                context.routerHashId);
        peer.routerHash = context.routerHashId;
        peer.remoteBgpId = address(peerUp.getRecvMsg().getId());
        peer.routerIp = context.sourceAddress;
        peer.timestamp = timestamp(bgpPeer.getTimestamp());
        peer.remoteAsn = uint32(peerUp.getRecvMsg().getAs());
        peer.remoteIp = address(bgpPeer.getAddress());
        peer.peerRd = asAttr((int) bgpPeer.getDistinguisher());
        peer.remotePort = peerUp.getRemotePort();
        peer.localAsn = uint32(peerUp.getSendMsg().getAs());
        peer.localIp = address(peerUp.getLocalAddress());
        peer.localPort = peerUp.getLocalPort();
        peer.localBgpId = address(peerUp.getSendMsg().getId());
        peer.infoData = peerUp.getMessage();
        peer.advertisedCapabilities = peerUp.getSendMsg().getCapabilities().getCapabilityList().stream().map(c -> generateCapabilityMessage(c.getCode(), c.getValue())).collect(Collectors.joining(","));
        peer.receivedCapabilities = peerUp.getRecvMsg().getCapabilities().getCapabilityList().stream().map(c -> generateCapabilityMessage(c.getCode(), c.getValue())).collect(Collectors.joining(","));
        peer.remoteHolddown = uint32(peerUp.getRecvMsg().getHoldTime());
        peer.advertisedHolddown = uint32(peerUp.getSendMsg().getHoldTime());
        peer.bmpReason = null;
        peer.bgpErrorCode = null;
        peer.bgpErrorSubcode = null;
        peer.errorText = null;
        peer.l3vpn = bgpPeer.getType() == Transport.Peer.Type.RD_INSTANCE;
        peer.prePolicy = bgpPeer.hasPeerFlags() && bgpPeer.getPeerFlags().getPolicy() == Transport.Peer.PeerFlags.Policy.PRE_POLICY;
        peer.ipv4 = isV4(bgpPeer.getAddress());
        peer.locRib = bgpPeer.getType() == Transport.Peer.Type.LOC_RIB_INSTANCE;
        peer.locRibFiltered = bgpPeer.hasLocRibFlags() && bgpPeer.getLocRibFlags().getFiltered();
        peer.tableName = peerUp.getTableName();

        messageHandler.handle(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)), context);
    }

    public static void handlePeerDownNotification(final BmpMessageHandler messageHandler,
                                            final Transport.Message message,
                                            final Transport.PeerDownPacket peerDown,
                                            final Context context,
                                            final AtomicLong sequence) {
        final Transport.Peer bgpPeer = peerDown.getPeer();

        final Peer peer = new Peer();
        peer.action = Peer.Action.DOWN;
        peer.sequence = sequence.getAndIncrement();
        peer.name = !Strings.isNullOrEmpty(bgpPeer.getHostname())
                ? bgpPeer.getHostname()
                : addressAsStr(bgpPeer.getAddress());
        peer.hash = Record.hash(bgpPeer.getAddress(),
                bgpPeer.getDistinguisher(),
                context.routerHashId);
        peer.routerHash = context.routerHashId;
        peer.remoteBgpId = address(bgpPeer.getId());
        peer.routerIp = context.sourceAddress;
        peer.timestamp = timestamp(bgpPeer.getTimestamp());
        peer.remoteAsn = uint32(bgpPeer.getAs());
        peer.remoteIp = address(bgpPeer.getAddress());
        peer.peerRd = asAttr((int) bgpPeer.getDistinguisher());
        peer.remotePort = null;
        peer.localAsn = null;
        peer.localIp = null;
        peer.localPort = null;
        peer.localBgpId = null;
        peer.infoData = null;
        peer.advertisedCapabilities = null;
        peer.receivedCapabilities = null;
        peer.remoteHolddown = null;
        peer.advertisedHolddown = null;
        peer.bmpReason = peerDown.getReasonCase().getNumber() - 1;

        switch (peerDown.getReasonCase().getNumber()) {
            case 2:
                peer.bgpErrorCode = peerDown.getLocalBgpNotification().getCode();
                peer.bgpErrorSubcode = peerDown.getLocalBgpNotification().getSubcode();
                break;
            case 4:
                peer.bgpErrorCode = peerDown.getRemoteBgpNotification().getCode();
                peer.bgpErrorSubcode = peerDown.getRemoteBgpNotification().getSubcode();
                break;
            default:
                peer.bgpErrorCode = null;
                peer.bgpErrorSubcode = null;
        }

        if (peer.bgpErrorCode != null && peer.bgpErrorSubcode != null) {
            peer.errorText = Error.from(peer.bgpErrorCode, peer.bgpErrorSubcode).getErrorText();
        }

        peer.l3vpn = bgpPeer.getType() == Transport.Peer.Type.RD_INSTANCE;
        peer.prePolicy = bgpPeer.hasPeerFlags() && bgpPeer.getPeerFlags().getPolicy() == Transport.Peer.PeerFlags.Policy.PRE_POLICY;
        peer.ipv4 = isV4(bgpPeer.getAddress());
        peer.locRib = bgpPeer.getType() == Transport.Peer.Type.LOC_RIB_INSTANCE;
        peer.locRibFiltered = bgpPeer.hasLocRibFlags() && bgpPeer.getLocRibFlags().getFiltered();
        peer.tableName = "";

        messageHandler.handle(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)), context);
    }

    public static void handleStatisticReport(final BmpMessageHandler messageHandler,
                                       final Transport.Message message,
                                       final Transport.StatisticsReportPacket statisticsReport,
                                       final Context context,
                                       final AtomicLong sequence) {
        final Transport.Peer peer = statisticsReport.getPeer();

        final Stat stat = new Stat();
        stat.action = Stat.Action.ADD;
        stat.sequence = sequence.getAndIncrement();
        stat.routerHash = Record.hash(context.sourceAddress.getHostAddress(), context.collectorHashId);
        stat.routerIp = context.sourceAddress;
        stat.peerHash = Record.hash(peer.getAddress(), peer.getDistinguisher(), stat.routerHash);
        stat.peerIp = address(peer.getAddress());
        stat.peerAsn = uint32(peer.getAs());
        stat.timestamp = timestamp(peer.getTimestamp());
        stat.prefixesRejected = statisticsReport.getRejected().getCount();
        stat.knownDupPrefixes = statisticsReport.getDuplicatePrefix().getCount();
        stat.knownDupWithdraws = statisticsReport.getDuplicateWithdraw().getCount();
        stat.invalidClusterList = statisticsReport.getInvalidUpdateDueToClusterListLoop().getCount();
        stat.invalidAsPath = statisticsReport.getInvalidUpdateDueToAsPathLoop().getCount();
        stat.invalidOriginatorId = statisticsReport.getInvalidUpdateDueToOriginatorId().getCount();
        stat.invalidAsConfed = statisticsReport.getInvalidUpdateDueToAsConfedLoop().getCount();
        stat.prefixesPrePolicy = statisticsReport.getAdjRibIn().getValue();
        stat.prefixesPostPolicy = statisticsReport.getLocalRib().getValue();

        messageHandler.handle(new Message(context.collectorHashId, Type.BMP_STAT, ImmutableList.of(stat)), context);
    }

    private static BaseAttribute toBaseAttributeRecord(final Transport.RouteMonitoringPacket routeMonitoring,
                                                final Context context,
                                                final AtomicLong sequence) {
        final Transport.Peer peer = routeMonitoring.getPeer();

        final BaseAttribute baseAttr = new BaseAttribute();
        baseAttr.action = BaseAttribute.Action.ADD; // Action is always ADD - attributes are never withdrawn
        baseAttr.sequence = sequence.getAndIncrement();
        baseAttr.routerHash = context.getRouterHash();
        baseAttr.routerIp = context.sourceAddress;
        baseAttr.peerHash = Record.hash(peer.getAddress(), peer.getDistinguisher(), baseAttr.routerHash);
        baseAttr.peerIp = address(peer.getAddress());
        baseAttr.peerAsn = uint32(peer.getAs());
        baseAttr.timestamp = context.timestamp;

        // Derive the origin of the prefix from the path attributes - default to an empty string if not set
        baseAttr.origin = getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.ORIGIN)
                .map(attr -> attr.getOrigin().name().toLowerCase()).orElse("");

        // Build the AS path from the path attributes - default to an empty string if not set
        // See UpdateMsg::parseAttr_AsPath in the OpenBMP collector for the corresponding logic
        baseAttr.asPathCount = 0;

        baseAttr.asPath = getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.AS_PATH)
                .map(asPathAttr -> {
                    final StringBuilder asPath = new StringBuilder();

                    asPathAttr.getAsPath().getSegmentsList().forEach(segment -> {
                        if (Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SET.equals(segment.getType())) {
                            asPath.append("{");
                        }
                        segment.getPathsList().forEach(segmentPath -> {
                            asPath.append(segmentPath);
                            asPath.append(" ");
                            baseAttr.asPathCount++;
                            baseAttr.originAs = uint32(segmentPath);
                        });
                        if (Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SET.equals(segment.getType())) {
                            asPath.append("}");
                        }
                    });

                    return asPath.toString();
                })
                .orElse("");

        // Derive the next hop from the path attributes
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.NEXT_HOP)
                .map(attr -> attr.getNextHop().getAddress())
                .ifPresent(nextHop -> {
                    baseAttr.nextHop = address(nextHop);
                    baseAttr.nextHopIpv4 = isV4(nextHop);
                });

        // Derive the Multi Exit Discriminator (MED) from the path attributes (lower values are preferred)
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.MULTI_EXIT_DISC)
                .map(attr -> attr.getMultiExitDisc().getDiscriminator())
                .ifPresent(med -> {
                    baseAttr.med = uint32(med);
                });

        // Derive the local preference from the path attributes
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.LOCAL_PREF)
                .map(attr -> attr.getLocalPref().getPreference())
                .ifPresent(localPref -> {
                    baseAttr.localPref = uint32(localPref);
                });

        // Derive the aggregator from the path attributes
        // See UpdateMsg::parseAttr_Aggegator in the OpenBMP collector for the corresponding logic
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.AGGREGATOR)
                .map(Transport.RouteMonitoringPacket.PathAttribute::getAggregator)
                .ifPresent(agg -> {
                    baseAttr.aggregator = String.format("%d %s", agg.getAs(), BmpAdapterTools.addressAsStr(agg.getAddress()));
                });

        // Derive the community list from the path attributes
        baseAttr.communityList = getPathAttributesOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.COMMUNITY)
                .map(Transport.RouteMonitoringPacket.PathAttribute::getCommunity)
                .map(BmpAdapterTools::asAttr)
                .collect(Collectors.joining(" "));

        // Derive the extended community list from the path attributes
        baseAttr.extCommunityList = Stream.concat(
                getPathAttributesOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.EXTENDED_COMMUNITIES)
                        .map(Transport.RouteMonitoringPacket.PathAttribute::getExtendedCommunities)
                        .flatMap(extendedCommunities -> extendedCommunities.getExtendedCommunitiesList().stream()
                                .map(extendedCommunity -> String.format("%s=%s", extendedCommunity.getType(), extendedCommunity.getValue()))
                        ),
                getPathAttributesOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.EXTENDED_V6_COMMUNITIES)
                        .map(Transport.RouteMonitoringPacket.PathAttribute::getExtendedV6Communities)
                        .flatMap(extendedV6Communities -> extendedV6Communities.getExtendedCommunitiesList().stream()
                                .map(extendedCommunity -> String.format("%s=%s", extendedCommunity.getType(), extendedCommunity.getValue()))
                        )
        ).collect(Collectors.joining(" "));

        // Derive the cluster list from the path attributes
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.CLUSTER_LIST)
                .map(Transport.RouteMonitoringPacket.PathAttribute::getClusterList)
                .ifPresent(clusterList -> {
                    baseAttr.clusterList = clusterList.getClusterIdList().stream()
                            .map(BmpAdapterTools::addressAsStr)
                            .collect(Collectors.joining(" "));
                });

        // Derive the large community list from the path attributes
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.LARGE_COMMUNITIES)
                .map(Transport.RouteMonitoringPacket.PathAttribute::getLargeCommunities)
                .ifPresent(largeCommunities -> {
                    baseAttr.largeCommunityList = largeCommunities.getLargeCommunitiesList().stream()
                            .map(largeCommunity -> String.format("%d:%d:%d",
                                    uint32(largeCommunity.getGlobalAdministrator()),
                                    uint32(largeCommunity.getLocalDataPart1()),
                                    uint32(largeCommunity.getLocalDataPart2())))
                            .collect(Collectors.joining(" "));
                });

        // Derive the originator id from the path attributes
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.ORIGINATOR_ID)
                .map(Transport.RouteMonitoringPacket.PathAttribute::getOriginatorId)
                .ifPresent(originatorId -> {
                    baseAttr.originatorId = Long.toString(uint32(originatorId));
                });

        // Set the atomic flag is the atomic aggregate path attribute is present
        baseAttr.atomicAgg = getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.ATOMIC_AGGREGATE)
                .isPresent();

        // Compute hash - fields [ as path, next hop, aggregator, origin, med, local pref, community list, ext community list, peer hash ]
        baseAttr.hash = Record.hash(baseAttr.asPath,
                Record.nullSafeStr(baseAttr.nextHop),
                baseAttr.aggregator,
                baseAttr.origin,
                Record.nullSafeStr(baseAttr.med),
                Record.nullSafeStr(baseAttr.localPref),
                baseAttr.communityList,
                baseAttr.extCommunityList,
                baseAttr.peerHash);

        return baseAttr;
    }

    private static UnicastPrefix toUnicastPrefixRecord(final Transport.RouteMonitoringPacket routeMonitoring,
                                                final Transport.RouteMonitoringPacket.Route route,
                                                final BaseAttribute baseAttr,
                                                final Context context,
                                                final AtomicLong sequence) {
        final Transport.Peer peer = routeMonitoring.getPeer();

        final UnicastPrefix unicastPrefix = new UnicastPrefix();
        unicastPrefix.sequence = sequence.incrementAndGet();
        unicastPrefix.routerHash = context.getRouterHash();
        unicastPrefix.routerIp = context.sourceAddress;
        unicastPrefix.peerHash = Record.hash(peer.getAddress(), peer.getDistinguisher(), unicastPrefix.routerHash);
        unicastPrefix.peerIp = address(peer.getAddress());
        unicastPrefix.peerAsn = uint32(peer.getAs());
        unicastPrefix.timestamp = context.timestamp;
        unicastPrefix.prefix = address(route.getPrefix());
        unicastPrefix.length = route.getLength();
        unicastPrefix.ipv4 = isV4(route.getPrefix());

        unicastPrefix.pathId = route.getPathId();
        unicastPrefix.labels = route.getLabels();
        unicastPrefix.prePolicy = peer.hasPeerFlags() && peer.getPeerFlags().getPolicy() == Transport.Peer.PeerFlags.Policy.PRE_POLICY;
        unicastPrefix.adjIn = peer.hasPeerFlags() && peer.getPeerFlags().getAdjIn();

        // Augment with base attributes if present
        if (baseAttr != null) {
            unicastPrefix.baseAttrHash = baseAttr.hash;
            unicastPrefix.origin = baseAttr.origin;
            unicastPrefix.asPath = baseAttr.asPath;
            unicastPrefix.asPathCount = baseAttr.asPathCount;
            unicastPrefix.originAs = baseAttr.originAs;
            unicastPrefix.nextHop = baseAttr.nextHop;
            unicastPrefix.med = baseAttr.med;
            unicastPrefix.localPref = baseAttr.localPref;
            unicastPrefix.aggregator = baseAttr.aggregator;
            unicastPrefix.communityList = baseAttr.communityList;
            unicastPrefix.extCommunityList = baseAttr.extCommunityList;
            unicastPrefix.clusterList = baseAttr.clusterList;
            unicastPrefix.atomicAgg = baseAttr.atomicAgg;
            unicastPrefix.nextHopIpv4 = baseAttr.nextHopIpv4;
            unicastPrefix.originatorId = baseAttr.originatorId;
            unicastPrefix.largeCommunityList = baseAttr.largeCommunityList;
        }

        //  Hash of fields [ prefix, prefix length, peer hash, path_id, 1 if has label(s) ]
        unicastPrefix.hash = Record.hash(InetAddressUtils.str(unicastPrefix.prefix),
                Integer.toString(unicastPrefix.length),
                unicastPrefix.peerHash,
                Long.toString(unicastPrefix.pathId),
                Strings.isNullOrEmpty(unicastPrefix.labels) ? "0" : "1");

        return unicastPrefix;
    }

    static void handleRouteMonitoringMessage(final BmpMessageHandler messageHandler, final Transport.Message message,
                                              final Transport.RouteMonitoringPacket routeMonitoring,
                                              final Context context, final AtomicLong sequence) {
        final List<Record> unicastPrefixRecords = new ArrayList<>(routeMonitoring.getWithdrawsCount() + routeMonitoring.getReachablesCount());

        // Handle withdraws
        for (org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport.RouteMonitoringPacket.Route route : routeMonitoring.getWithdrawsList()) {
            final UnicastPrefix unicastPrefix = toUnicastPrefixRecord(routeMonitoring, route, null, context, sequence);
            unicastPrefix.action = UnicastPrefix.Action.DELETE;

            unicastPrefixRecords.add(unicastPrefix);
        }

        final BaseAttribute baseAttr ;
        if (routeMonitoring.getReachablesCount() > 0) {
            // Generate base attribute record - the same attributes apply to all reachables in the packet
            baseAttr = toBaseAttributeRecord(routeMonitoring, context, sequence);

            // Handle reachables
            for (final org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport.RouteMonitoringPacket.Route route : routeMonitoring.getReachablesList()) {
                final UnicastPrefix unicastPrefix = toUnicastPrefixRecord(routeMonitoring, route, baseAttr, context, sequence);
                unicastPrefix.action = UnicastPrefix.Action.ADD;
                unicastPrefixRecords.add(unicastPrefix);
            }

            final List<Transport.RouteMonitoringPacket.PathAttribute.MultiprotocolReachableNrli> mpReachNlriList = routeMonitoring.getAttributesList().stream()
                    .filter(a -> a.hasMpReachNrli())
                    .map(a -> a.getMpReachNrli())
                    .collect(Collectors.toList());

            for(final Transport.RouteMonitoringPacket.PathAttribute.MultiprotocolReachableNrli multiprotocolReachableNrli : mpReachNlriList) {
                for (final org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport.RouteMonitoringPacket.Route route : multiprotocolReachableNrli.getAdvertisedList()) {
                    final UnicastPrefix unicastPrefix = toUnicastPrefixRecord(routeMonitoring, route, baseAttr, context, sequence);
                    unicastPrefix.action = UnicastPrefix.Action.ADD;
                    unicastPrefixRecords.add(unicastPrefix);
                }
                for (final org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport.RouteMonitoringPacket.Route route : multiprotocolReachableNrli.getVpnAdvertisedList()) {
                    final UnicastPrefix unicastPrefix = toUnicastPrefixRecord(routeMonitoring, route, baseAttr, context, sequence);
                    unicastPrefix.action = UnicastPrefix.Action.ADD;
                    unicastPrefixRecords.add(unicastPrefix);
                }
            }
        } else {
            baseAttr = null;
        }

        // Forward the messages to the handler
        if (baseAttr != null) {
            messageHandler.handle(new Message(context.collectorHashId, Type.BASE_ATTRIBUTE, ImmutableList.of(baseAttr)), context);
        }

        messageHandler.handle(new Message(context.collectorHashId, Type.UNICAST_PREFIX, unicastPrefixRecords), context);
    }

    private static String generateCapabilityMessage(final int code, final ByteString byteString) {
        final ByteBuf value = Unpooled.wrappedBuffer(byteString.toByteArray());
        if (code == 72 || (code >= 10 && code <= 63) || (code >= 74 && code <= 127)) {
            return String.format("Unassigned (%d)", code);
        }

        if (code >= 128 && code <= 255) {
            return String.format("Reserved for Private Use", code);
        }
        switch (code) {
            case 0:
                return "Reserved";
            case 1: {
                int afi = uint16(value);
                skip(value, 1);
                int safi = uint8(value);

                return String.format("Multiprotocol Extensions for BGP-4 (1): afi=%d safi=%d: %s %s",
                        code,
                        afi,
                        safi,
                        AddressFamilyIdentifier.from(afi).getDescription(),
                        SubsequentAddressFamilyIdentifier.from(safi).getDescription());
            }
            case 2:
                return "Route Refresh Capability for BGP-4 (2)";
            case 3:
                return "Outbound Route Filtering Capability (3)";
            case 4:
                return "Multiple routes to a destination capability (deprecated) (4)";
            case 5:
                return "Extended Next Hop Encoding (5)";
            case 6:
                return "BGP Extended Message (6)";
            case 7:
                return "BGPsec Capability (7)";
            case 8:
                return "BGP Role (TEMPORARY - registered 2018-03-29, extension registered 2019-03-18, expires 2020-03-29) (8)";
            case 9:
                return "Multiple Labels Capability (9)";
            case 64:
                return "Graceful Restart Capability (64)";
            case 65:
                return "Support for 4-octet AS number capability (65)";
            case 66:
                return "Deprecated (2003-03-06) (66)";
            case 67:
                return "Support for Dynamic Capability (capability specific) (67)";
            case 68:
                return "Multisession BGP Capability (68)";
            case 69: {
                final List<String> strings = repeatRemaining(value, b -> {
                    int afi = uint16(b);
                    int safi = uint8(b);
                    int sendReceive = uint8(b);

                    return String.format("afi=%d safi=%d send/receive=%d: %s %s %s",
                            afi,
                            safi,
                            sendReceive,
                            AddressFamilyIdentifier.from(afi),
                            SubsequentAddressFamilyIdentifier.from(safi),
                            parseSendReceive(sendReceive));
                });

                return String.format("ADD-PATH Capability (69): %s", String.join(", ", strings));
            }
            case 70:
                return "Enhanced Route Refresh Capability (70)";
            case 71:
                return "Long-Lived Graceful Restart (71)";
            case 73:
                return "FQDN Capability (73)";
        }
        return String.format("Unknown capability (%d)", code);
    }

    private static String parseSendReceive(final int sendReceive) {
        switch (sendReceive) {
            case 1:
                return "Receive";
            case 2:
                return "Send";
            case 3:
                return "Send/Receive";
            default:
                return "unknown";
        }
    }

}
