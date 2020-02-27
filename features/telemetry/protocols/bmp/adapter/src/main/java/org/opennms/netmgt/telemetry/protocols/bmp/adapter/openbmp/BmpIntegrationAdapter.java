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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.address;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.asAttr;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.getPathAttributeOfType;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.getPathAttributesOfType;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.isV4;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.uint32;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.timestamp;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;

public class BmpIntegrationAdapter extends AbstractAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpIntegrationAdapter.class);

    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong baseAttrSequence = new AtomicLong();
    private final AtomicLong unicastPrefixSequence = new AtomicLong();

    private final BmpMessageHandler handler;

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
                    LOG.warn("Unknown Notification Packet Code: {}/{}", code, subcode);
                    return UNKNOWN;
            }
        }
    }

    public BmpIntegrationAdapter(final AdapterDefinition adapterConfig,
                                 final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
        this.handler = new BmpKafkaProducer(adapterConfig);
    }

    public BmpIntegrationAdapter(final AdapterDefinition adapterConfig,
                                 final MetricRegistry metricRegistry,
                                 final BmpMessageHandler handler) {
        super(adapterConfig, metricRegistry);
        this.handler = Objects.requireNonNull(handler);
    }

    private void handleHeartbeatMessage(final Transport.Message message,
                                        final Transport.Heartbeat heartbeat,
                                        final Context context) {
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

        this.handler.handle(new Message(context.collectorHashId, Type.COLLECTOR, ImmutableList.of(collector)));
    }

    private void handleInitiationMessage(final Transport.Message message,
                                         final Transport.InitiationPacket initiation,
                                         final Context context) {
        final Router router = new Router();
        router.action = Router.Action.INIT;
        router.sequence = sequence.getAndIncrement();
        router.name = initiation.getSysName();
//        router.name = InetAddressUtils.str(address(??.getAddress())); // TODO: resolve Ip via DNS?
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = initiation.getSysDesc();
        router.termCode = null;
        router.termReason = null;
        router.initData = initiation.getMessage();
        router.termData = null;
        router.timestamp = context.timestamp;
        router.bgpId = initiation.hasBgpId() ? BmpAdapterTools.address(initiation.getBgpId()) : null;

        this.handler.handle(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)));
    }

    private void handleTerminationMessage(final Transport.Message message,
                                          final Transport.TerminationPacket termination,
                                          final Context context) {
        final Router router = new Router();
        router.action = Router.Action.TERM;
        router.sequence = sequence.getAndIncrement();
        router.name = null;
//        router.name = InetAddressUtils.str(address(??.getAddress())); // TODO: resolve Ip via DNS?
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = null;
        router.termCode = termination.getReason();

        switch (router.termCode) {
            case 0:
                router.termReason = "Session administratively closed.  The session might be re-initiated";
                break;
            case 1:
                router.termReason = "Unspecified reason";
                break;
            case 2:
                router.termReason = "Out of resources.  The router has exhausted resources available for the BMP session";
                break;
            case 3:
                router.termReason = "Redundant connection.  The router has determined that this connection is redundant with another one";
                break;
            case 4:
                router.termReason = "Session permanently administratively closed, will not be re-initiated";
                break;
            default:
                router.termReason = "Unknown reason";
        }

        router.initData = null;
        router.termData = termination.getMessage();
        router.timestamp = context.timestamp;
        router.bgpId = null;

        this.handler.handle(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)));
    }

    private void handlePeerUpNotification(final Transport.Message message,
                                          final Transport.PeerUpPacket peerUp,
                                          final Context context) {
        final Transport.Peer bgpPeer = peerUp.getPeer();

        final Peer peer = new Peer();
        peer.action = Peer.Action.UP;
        peer.sequence = sequence.getAndIncrement();
        peer.name = InetAddressUtils.str(address(bgpPeer.getAddress())); // TODO: resolve Ip via DNS?
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
        peer.advertisedCapabilities = ""; // TODO: Not parsed right now
        peer.receivedCapabilities = ""; // TODO: Not parsed right now
        peer.remoteHolddown = uint32(peerUp.getRecvMsg().getHoldTime());
        peer.advertisedHolddown = uint32(peerUp.getSendMsg().getHoldTime());
        peer.bmpReason = null;
        peer.bgpErrorCode = null;
        peer.bgpErrorSubcode = null;
        peer.errorText = null;
        peer.l3vpn = bgpPeer.getType() == Transport.Peer.Type.RD_INSTANCE;
        peer.prePolicy = bgpPeer.getFlags().getPolicy() == Transport.Peer.Flags.Policy.PRE_POLICY;
        peer.ipv4 = isV4(bgpPeer.getAddress());
        peer.locRib = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.locRibFiltered = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.tableName = ""; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)

        this.handler.handle(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)));
    }

    private void handlePeerDownNotification(final Transport.Message message,
                                            final Transport.PeerDownPacket peerDown,
                                            final Context context) {
        final Transport.Peer bgpPeer = peerDown.getPeer();

        final Peer peer = new Peer();
        peer.action = Peer.Action.DOWN;
        peer.sequence = sequence.getAndIncrement();
        peer.name = InetAddressUtils.str(address(bgpPeer.getAddress())); // TODO: resolve Ip via DNS?
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
        peer.prePolicy = bgpPeer.getFlags().getPolicy() == Transport.Peer.Flags.Policy.PRE_POLICY;
        peer.ipv4 = isV4(bgpPeer.getAddress());
        peer.locRib = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.locRibFiltered = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.tableName = ""; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)

        this.handler.handle(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)));
    }

    private void handleStatisticReport(final Transport.Message message,
                                       final Transport.StatisticsReportPacket statisticsReport,
                                       final Context context) {
        final Transport.Peer peer = statisticsReport.getPeer();

        final Stat stat = new Stat();
        stat.action = Stat.Action.ADD;
        stat.sequence = sequence.getAndIncrement();
        stat.routerHash = Record.hash(context.sourceAddress.getHostAddress(), Integer.toString(context.sourcePort), context.collectorHashId);
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
        stat.prefixesPostPolicy = statisticsReport.getLocRib().getValue();

        this.handler.handle(new Message(context.collectorHashId, Type.BMP_STAT, ImmutableList.of(stat)));
    }

    private BaseAttribute toBaseAttributeRecord(Transport.RouteMonitoringPacket routeMonitoring, Context context) {
        final Transport.Peer peer = routeMonitoring.getPeer();
        final BaseAttribute baseAttr = new BaseAttribute();
        // Action is always ADD - attributes are never withdrawn
        baseAttr.action = BaseAttribute.Action.ADD;
        // This increments for each attribute record by peer and restarts on collector restart or number wrap.
        baseAttr.sequence = baseAttrSequence.getAndIncrement();
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
        AtomicLong lastAsInPath = new AtomicLong(0);
        StringBuilder asPath = new StringBuilder();
        routeMonitoring.getAttributesList().stream()
                .filter(Transport.RouteMonitoringPacket.PathAttribute::hasAsPath)
                .findFirst()
                .ifPresent(asPathAttr -> {
                    asPathAttr.getAsPath().getSegmentsList().forEach(segment -> {
                        if (Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SET.equals(segment.getType())) {
                            asPath.append("{");
                        }
                        segment.getPathsList().forEach(segmentPath -> {
                            asPath.append(segmentPath);
                            asPath.append(" ");
                            baseAttr.asPathCount++;
                            lastAsInPath.set(uint32(segmentPath));
                        });
                        if (Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SET.equals(segment.getType())) {
                            asPath.append("}");
                        }
                    });
                });
        baseAttr.asPath = asPath.toString();
        // Originating ASN (right most in the path)
        baseAttr.originAs = lastAsInPath.get();
        // Derive the next hop from the path attributes
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.NEXT_HOP)
                .map(attr -> attr.getNextHop().getAddress())
                .ifPresent(nextHop -> {
                    baseAttr.nextHop = address(nextHop);
                    baseAttr.nextHopIpv4 = isV4(nextHop);
                });
        // Derive the Multi Exit Discriminator (MED) from the path attributes (lower values are preferred)
        // FIXME: MED is optional, should it be serialized as an empty string if not set?
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
        baseAttr.communityList = getPathAttributesOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.COMMUNITY)
                .map(Transport.RouteMonitoringPacket.PathAttribute::getCommunity)
                .map(BmpAdapterTools::asAttr)
                .collect(Collectors.joining(" "));

        // FIXME: Missing ATTR_TYPE_EXT_COMMUNITY
        // FIXME: Missing ATTR_TYPE_CLUSTER_LIST
        // FIXME: Missing ATTR_TYPE_LARGE_COMMUNITY
        // FIXME: Missing ATTR_TYPE_ORIGINATOR_ID

        // Set the atomic flag is the atomic aggregate path attribute is present
        // FIXME: Should this have a value? The OpenBMP code only sets it if it is 1
        getPathAttributeOfType(routeMonitoring, Transport.RouteMonitoringPacket.PathAttribute.ValueCase.ATOMIC_AGGREGATE)
                .ifPresent(isAtomic -> baseAttr.atomicAgg = true);

        // Compute hash - fields [ as path, next hop, aggregator, origin, med, local pref, community list, ext community list, peer hash ]
        baseAttr.hash = Record.hash(baseAttr.asPath, Record.nullSafeStr(baseAttr.nextHop),
                baseAttr.aggregator, baseAttr.origin, Record.nullSafeStr(baseAttr.med),
                Record.nullSafeStr(baseAttr.localPref), baseAttr.communityList,
                baseAttr.extCommunityList, baseAttr.peerHash);

        return baseAttr;
    }

    private UnicastPrefix toUnicastPrefixRecord(Transport.RouteMonitoringPacket routeMonitoring, Transport.RouteMonitoringPacket.Route route, BaseAttribute baseAttr, Context context) {
        final Transport.Peer peer = routeMonitoring.getPeer();
        final UnicastPrefix unicastPrefix = new UnicastPrefix();
        unicastPrefix.sequence = unicastPrefixSequence.incrementAndGet();
        unicastPrefix.routerHash = context.getRouterHash();
        unicastPrefix.routerIp = context.sourceAddress;
        unicastPrefix.peerHash = Record.hash(peer.getAddress(), peer.getDistinguisher(), unicastPrefix.routerHash);
        unicastPrefix.peerIp = address(peer.getAddress());
        unicastPrefix.peerAsn = uint32(peer.getAs());
        unicastPrefix.timestamp = context.timestamp;
        unicastPrefix.prefix = address(route.getPrefix());
        unicastPrefix.length = route.getLength();
        unicastPrefix.ipv4 = isV4(route.getPrefix());
        // FIXME: Where to derive path id from?
        unicastPrefix.pathId = 0;
        // FIXME: Where to derive labels from?
        unicastPrefix.labels = null;
        unicastPrefix.prePolicy = Transport.Peer.Flags.Policy.PRE_POLICY.equals(peer.getFlags().getPolicy());
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
        // FIXME: isAdjIn?

        //  Hash of fields [ prefix, prefix length, peer hash, path_id, 1 if has label(s) ]
        unicastPrefix.hash = Record.hash(InetAddressUtils.str(unicastPrefix.prefix),
                Integer.toString(unicastPrefix.length),
                unicastPrefix.peerHash,
                Integer.toString(unicastPrefix.pathId),
                Strings.isNullOrEmpty(unicastPrefix.labels) ? "0" : "1");
        return unicastPrefix;
    }

    private void handleRouteMonitoringMessage(Transport.Message message, Transport.RouteMonitoringPacket routeMonitoring, Context context) {
        final List<Record> unicastPrefixRecords = new ArrayList<>(routeMonitoring.getWithdrawsCount() + routeMonitoring.getReachablesCount());

        // Handle withdraws
        for (org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport.RouteMonitoringPacket.Route route : routeMonitoring.getWithdrawsList()) {
            final UnicastPrefix unicastPrefix = toUnicastPrefixRecord(routeMonitoring, route, null, context);
            unicastPrefix.action = UnicastPrefix.Action.DELETE;

            unicastPrefixRecords.add(unicastPrefix);
        }

        final BaseAttribute baseAttr;
        if (routeMonitoring.getReachablesCount() > 0) {
            // Generate base attribute record - the same attributes apply to all reachables in the packet
            baseAttr = toBaseAttributeRecord(routeMonitoring, context);

            // Handle reachables
            for (org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport.RouteMonitoringPacket.Route route : routeMonitoring.getReachablesList()) {
                final UnicastPrefix unicastPrefix = toUnicastPrefixRecord(routeMonitoring, route, baseAttr, context);
                unicastPrefix.action = UnicastPrefix.Action.ADD;
                unicastPrefixRecords.add(unicastPrefix);
            }
        } else {
            baseAttr = null;
        }

        // Forward the messages to the handler
        if (baseAttr != null) {
            handler.handle(new Message(context.collectorHashId, Type.BASE_ATTRIBUTE, ImmutableList.of(baseAttr)));
        }
        handler.handle(new Message(context.collectorHashId, Type.UNICAST_PREFIX, unicastPrefixRecords));
    }

    @Override
    public void handleMessage(final TelemetryMessageLogEntry messageLogEntry,
                              final TelemetryMessageLog messageLog) {
        LOG.trace("Parsing packet: {}", messageLogEntry);
        final Transport.Message message;
        try {
            message = Transport.Message.parseFrom(messageLogEntry.getByteArray());
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Invalid message", e);
            return;
        }

        final String collectorHashId = Record.hash(messageLog.getSystemId());
        final String routerHashId = Record.hash(messageLog.getSourceAddress(), Integer.toString(messageLog.getSourcePort()), collectorHashId);
        final Context context = new Context(messageLog.getSystemId(),
                                            collectorHashId,
                                            routerHashId,
                                            Instant.ofEpochMilli(messageLogEntry.getTimestamp()),
                                            InetAddressUtils.addr(messageLog.getSourceAddress()),
                                            messageLog.getSourcePort());

        switch(message.getPacketCase()) {
            case HEARTBEAT:
                handleHeartbeatMessage(message, message.getHeartbeat(), context);
                break;
            case INITIATION:
                handleInitiationMessage(message, message.getInitiation(), context);
                break;
            case TERMINATION:
                handleTerminationMessage(message, message.getTermination(), context);
                break;
            case PEER_UP:
                handlePeerUpNotification(message, message.getPeerUp(), context);
                break;
            case PEER_DOWN:
                handlePeerDownNotification(message, message.getPeerDown(), context);
                break;
            case STATISTICS_REPORT:
                handleStatisticReport(message, message.getStatisticsReport(), context);
                break;
            case ROUTE_MONITORING:
                handleRouteMonitoringMessage(message, message.getRouteMonitoring(), context);
                break;
            case PACKET_NOT_SET:
                break;
        }
    }

    @Override
    public void destroy() {
        this.handler.close();
        super.destroy();
    }

    private static class Context {
        public final String adminId;

        public final String collectorHashId;
        public final String routerHashId;

        public final Instant timestamp;

        public final InetAddress sourceAddress;
        public final int sourcePort;

        private Context(final String adminId,
                        final String collectorHashId,
                        final String routerHashId,
                        final Instant timestamp,
                        final InetAddress sourceAddress,
                        final int sourcePort) {
            this.adminId = Objects.requireNonNull(adminId);
            this.collectorHashId = Objects.requireNonNull(collectorHashId);
            this.routerHashId = Objects.requireNonNull(routerHashId);
            this.timestamp = Objects.requireNonNull(timestamp);
            this.sourceAddress = Objects.requireNonNull(sourceAddress);
            this.sourcePort = sourcePort;
        }

        public String getRouterHash() {
            return Record.hash(sourceAddress.getHostAddress(), Integer.toString(sourcePort), collectorHashId);
        }
    }
}
