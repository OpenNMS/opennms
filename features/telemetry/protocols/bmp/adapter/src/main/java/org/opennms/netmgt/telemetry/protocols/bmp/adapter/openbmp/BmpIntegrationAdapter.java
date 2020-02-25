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

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;

public class BmpIntegrationAdapter extends AbstractAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpIntegrationAdapter.class);
    private final static AtomicLong SEQUENCE = new AtomicLong();
    private final KafkaProducer<String, String> producer;

    public BmpIntegrationAdapter(final AdapterDefinition adapterConfig,
                                 final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
        this.producer = buildProducer(adapterConfig);
    }

    private static KafkaProducer<String, String> buildProducer(final AdapterDefinition adapterConfig) {
        final Map<String, Object> kafkaConfig = Maps.newHashMap();
        for (final Map.Entry<String, String> entry : adapterConfig.getParameterMap().entrySet()) {
            StringUtils.truncatePrefix(entry.getKey(), "kafka.").ifPresent(key -> {
                kafkaConfig.put(key, entry.getValue());
            });
        }

        // TODO fooker: Apply defaults (steal from https://github.com/SNAS/openbmp/blob/1a615a3c75a0143cc87ec70458471f0af67d3929/Server/src/kafka/MsgBusImpl_kafka.cpp#L162)

        return new KafkaProducer<>(kafkaConfig, new StringSerializer(), new StringSerializer());
    }

    private Optional<Message> handleInitiationMessage(final Transport.Message message,
                                                      final Transport.InitiationPacket initiation,
                                                      final Context context) {
        final Router router = new Router();
        router.action = Router.Action.INIT;
        router.sequence = SEQUENCE.getAndIncrement();
        router.name = initiation.getSysName();
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = initiation.getSysDesc();
        router.termCode = null;
        router.termReason = null;
        router.initData = initiation.getMessage();
        router.termData = null;
        router.timestamp = context.timestamp;
        router.bgpId = null; // TODO: Where is this at?

        return Optional.of(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)));
    }

    private Optional<Message> handleTerminationMessage(final Transport.Message message,
                                                       final Transport.TerminationPacket termination,
                                                       final Context context) {
        final Router router = new Router();
        router.action = Router.Action.TERM;
        router.sequence = SEQUENCE.getAndIncrement();
        router.name = null;
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = null;
        router.termCode = null; // TODO fooker: Extract from document
        router.termReason = null; // TODO fooker: Extract from document
        router.initData = null;
        router.termData = null;
        router.timestamp = context.timestamp;
        router.bgpId = null;

        return Optional.of(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)));
    }

    private Optional<Message> handlePeerUpNotification(final Transport.Message message,
                                                       final Transport.PeerUpPacket peerUp,
                                                       final Context context) {
        final Transport.Peer bgpPeer = peerUp.getPeer();
        final Peer peer = new Peer();
        peer.action = Peer.Action.UP;
        peer.sequence = SEQUENCE.getAndIncrement();
        peer.name = peerUp.getSysName();
        peer.hash = Record.hash(bgpPeer.getAddress(),
                                bgpPeer.getDistinguisher(),
                                context.routerHashId);
        peer.routerHash = context.routerHashId;
        peer.remoteBgpId = InetAddressUtils.str(address(peerUp.getRecvMsg().getId())); // FIXME; This is weird
        peer.routerIp = context.sourceAddress;
        peer.timestamp = context.timestamp;
        peer.remoteAsn = (long) peerUp.getRecvMsg().getAs();
        peer.remoteIp = address(bgpPeer.getAddress());
        peer.peerRd = Long.toString(bgpPeer.getDistinguisher());
        peer.remotePort = peerUp.getRemotePort();
        peer.localAsn = (long) peerUp.getSendMsg().getAs(); // FIXME: long vs int?
        peer.localIp = address(peerUp.getLocalAddress());
        peer.localPort = peerUp.getLocalPort();
        peer.localBgpId = InetAddressUtils.str(address(peerUp.getSendMsg().getId())); // FIXME; still weird
        peer.infoData = peerUp.getMessage();
        peer.advertisedCapabilities = ""; // TODO: Not parsed right now
        peer.receivedCapabilities = ""; // TODO: Not parsed right now
        peer.remoteHolddown = (long) peerUp.getRecvMsg().getHoldTime(); // FIXME: long vs int?
        peer.advertisedHolddown = (long) peerUp.getRecvMsg().getHoldTime();
        peer.bmpReason = null;
        peer.bgpErrorCode = null;
        peer.bgpErrorSubcode = null;
        peer.errorText = null;
        peer.l3vpn = false; // TODO: Extract from document
        peer.prePolicy = false; // TODO?
        peer.ipv4 = Transport.IpAddress.AddressCase.V4.equals(bgpPeer.getAddress().getAddressCase());
        peer.locRib = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.locRibFiltered = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.tableName = ""; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)

        return Optional.of(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)));
    }

    private Optional<Message> handlePeerDownNotification(final Transport.Message message,
                                                         final Transport.PeerDownPacket peerDown,
                                                         final Context context) {
        final Transport.Peer bgpPeer = peerDown.getPeer();
        final Peer peer = new Peer();
        peer.action = Peer.Action.DOWN;
        peer.sequence = SEQUENCE.getAndIncrement();
        peer.name = null; // FIXME: Can populate?
        peer.hash = Record.hash(bgpPeer.getAddress(),
                bgpPeer.getDistinguisher(),
                context.routerHashId);
        peer.routerHash = context.routerHashId;
        peer.remoteBgpId = null; // FIXME: Can populate?
        peer.routerIp = context.sourceAddress;
        peer.timestamp = context.timestamp;
        peer.remoteAsn = 0L; // FIXME: Can populate?
        peer.remoteIp = address(bgpPeer.getAddress());
        peer.peerRd = Long.toString(bgpPeer.getDistinguisher());
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
        peer.bmpReason = null; // TODO: Extract from document
        peer.bgpErrorCode = null; // TODO: Extract from document
        peer.bgpErrorSubcode = null; // TODO: Extract from document
        peer.errorText = null; // TODO: Extract from document
        peer.l3vpn = false; // TODO: Extract from document
        peer.prePolicy = false; // TODO?
        peer.ipv4 = Transport.IpAddress.AddressCase.V4.equals(bgpPeer.getAddress().getAddressCase());
        peer.locRib = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.locRibFiltered = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.tableName = ""; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)

        return Optional.of(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)));
    }

    private Optional<Message> handleStatisticReport(final Transport.Message message,
                                                    final Transport.StatisticsReportPacket statisticsReport,
                                                    final Context context) {
        final Transport.Peer peer = statisticsReport.getPeer();
        final Stat stat = new Stat();
        stat.action = Stat.Action.ADD;
        stat.sequence = SEQUENCE.getAndIncrement();
        stat.routerHash = Record.hash(context.sourceAddress.getHostAddress(), Integer.toString(context.sourcePort), context.collectorHashId);
        stat.routerIp = context.sourceAddress;
        stat.peerHash = Record.hash(peer.getAddress(), peer.getDistinguisher(), stat.routerHash);
        stat.peerIp = address(peer.getAddress());
        stat.peerAsn = (long)peer.getAs();
        stat.timestamp = context.timestamp;
        stat.prefixesRejected = statisticsReport.getRejected().getCount();
        stat.knownDupPrefixes = statisticsReport.getDuplicatePrefix().getCount();
        stat.knownDupWithdraws = statisticsReport.getDuplicateWithdraw().getCount();
        stat.invalidClusterList = statisticsReport.getInvalidUpdateDueToClusterListLoop().getCount();
        stat.invalidAsPath = statisticsReport.getInvalidUpdateDueToAsPathLoop().getCount();
        stat.invalidOriginatorId = statisticsReport.getInvalidUpdateDueToOriginatorId().getCount();
        stat.invalidAsConfed = statisticsReport.getInvalidUpdateDueToAsConfedLoop().getCount();
        stat.prefixesPrePolicy = statisticsReport.getAdjRibIn().getValue();
        stat.prefixesPostPolicy = statisticsReport.getLocRib().getValue();
        return Optional.of(new Message(context.collectorHashId, Type.BMP_STAT, ImmutableList.of(stat)));
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
        final Context context = new Context(collectorHashId,
                                            routerHashId,
                                            Instant.ofEpochMilli(messageLogEntry.getTimestamp()),
                                            InetAddressUtils.addr(messageLog.getSourceAddress()),
                                            messageLog.getSourcePort());

        Optional<Message> messageToSend = Optional.empty();
        switch(message.getPacketCase()) {
            case INITIATION:
                messageToSend = this.handleInitiationMessage(message, message.getInitiation(), context);
                break;
            case TERMINATION:
                messageToSend =  this.handleTerminationMessage(message, message.getTermination(), context);
                break;
            case PEERUP:
                messageToSend = this.handlePeerUpNotification(message, message.getPeerUp(), context);
                break;
            case PEERDOWN:
                messageToSend = this.handlePeerDownNotification(message, message.getPeerDown(), context);
                break;
            case STATISTICSREPORT:
                messageToSend = this.handleStatisticReport(message, message.getStatisticsReport(), context);
                break;
            case ROUTEMONITORING:
            case PACKET_NOT_SET:
                break;
        }
        messageToSend.ifPresent(this::send);
    }

    @Override
    public void destroy() {
        this.producer.close();
        super.destroy();
    }

    private void send(final Message message) {
        final StringBuffer buffer = new StringBuffer();
        message.serialize(buffer);

        final String topic = message.getType().getTopic();
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, message.getCollectorHashId(), buffer.toString());

        this.producer.send(record, (meta, err) -> {
            if (err != null) {
                LOG.warn("Failed to send OpenBMP message", err);
            } else {
                LOG.trace("Send OpenBMP message: {} = {}@{}", meta.topic(), meta.offset(), meta.partition());
            }
        });
    }

    private static class Context {
        public final String collectorHashId;
        public final String routerHashId;

        public final Instant timestamp;

        public final InetAddress sourceAddress;
        public final int sourcePort;

        private Context(final String collectorHashId,
                        final String routerHashId,
                        final Instant timestamp,
                        final InetAddress sourceAddress,
                        final int sourcePort) {
            this.collectorHashId = Objects.requireNonNull(collectorHashId);
            this.routerHashId = Objects.requireNonNull(routerHashId);
            this.timestamp = Objects.requireNonNull(timestamp);
            this.sourceAddress = Objects.requireNonNull(sourceAddress);
            this.sourcePort = sourcePort;
        }
    }
}
