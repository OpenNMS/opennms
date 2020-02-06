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

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getBool;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt32;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
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
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class BmpIntegrationAdapter extends AbstractAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpIntegrationAdapter.class);
    private final static AtomicInteger SEQUENCE = new AtomicInteger();
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

    private Optional<Message> handleInitiationMessage(final BsonDocument document,
                                                      final Context context) {
        final Router router = new Router();
        router.action = Router.Action.INIT;
        router.sequence = SEQUENCE.getAndIncrement();
        router.name = getString(document, "sys_name").orElse("");
        router.hash = context.routerHashId;
        router.ipAddress = context.sourceAddress;
        router.description = getString(document, "sys_desc").orElse(null);
        router.termCode = null;
        router.termReason = null;
        router.initData = getString(document, "message").orElse(null);
        router.termData = null;
        router.timestamp = context.timestamp;
        router.bgpId = getString(document, "bgp_id").map(InetAddressUtils::addr).orElse(null);

        return Optional.of(new Message(context.collectorHashId, Type.ROUTER, ImmutableList.of(router)));
    }

    private Optional<Message> handleTerminationMessage(final BsonDocument document,
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

    private Optional<Message> handlePeerUpNotification(final BsonDocument document,
                                                       final Context context) {
        final Peer peer = new Peer();
        peer.action = Peer.Action.UP;
        peer.sequence = SEQUENCE.getAndIncrement();
        peer.name = getString(document, "sys_name").orElse(null);
        peer.hash = Record.hash(getString(document, "peer", "address").get(),
                                getString(document, "peer", "distinguisher").get(),
                                context.routerHashId);
        peer.routerHash = context.routerHashId;
        peer.remoteBgpId = getString(document, "recv_open_msg", "id").get();
        peer.routerIp = context.sourceAddress;
        peer.timestamp = context.timestamp;
        peer.remoteAsn = getInt64(document, "recv_open_msg", "as").get();
        peer.remoteIp = getString(document, "peer", "address").map(InetAddressUtils::addr).get();
        peer.peerRd = getString(document, "peer", "distinguisher").get();
        peer.remotePort = getInt32(document, "remote_port").get();
        peer.localAsn = getInt64(document, "send_open_msg", "as").get();
        peer.localIp = getString(document, "local_address").map(InetAddressUtils::addr).get();
        peer.localPort = getInt32(document, "local_port").get();
        peer.localBgpId = getString(document, "send_open_msg", "id").get();
        peer.infoData = getString(document, "message").orElse(null);
        peer.advertisedCapabilities = ""; // TODO: Not parsed right now
        peer.receivedCapabilities = ""; // TODO: Not parsed right now
        peer.remoteHolddown = getInt64(document, "recv_open_msg", "hold_time").orElse(null);
        peer.advertisedHolddown = getInt64(document, "send_open_msg", "hold_time").orElse(null);
        peer.bmpReason = null;
        peer.bgpErrorCode = null;
        peer.bgpErrorSubcode = null;
        peer.errorText = null;
        peer.l3vpn = false; // TODO: Extract from document
        peer.prePolicy = !getBool(document, "peer", "post_policy").get();
        peer.ipv4 = getInt64(document, "peer", "ip_version").get() == 4;
        peer.locRib = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.locRibFiltered = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.tableName = ""; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)

        return Optional.of(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)));
    }

    private Optional<Message> handlePeerDownNotification(final BsonDocument document,
                                                         final Context context) {
        final Peer peer = new Peer();
        peer.action = Peer.Action.DOWN;
        peer.sequence = SEQUENCE.getAndIncrement();
        peer.name = getString(document, "sys_name").orElse(null);
        peer.hash = Record.hash(getString(document, "peer", "address").get(),
                                getString(document, "peer", "distinguisher").get(),
                                context.routerHashId);
        peer.routerHash = context.routerHashId;
        peer.remoteBgpId = getString(document, "recv_open_msg", "id").get();
        peer.routerIp = context.sourceAddress;
        peer.timestamp = context.timestamp;
        peer.remoteAsn = getInt64(document, "recv_open_msg", "as").get();
        peer.remoteIp = getString(document, "peer", "address").map(InetAddressUtils::addr).get();
        peer.peerRd = getString(document, "peer", "distinguisher").get();
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
        peer.prePolicy = !getBool(document, "peer", "post_policy").get();
        peer.ipv4 = getInt64(document, "peer", "ip_version").get() == 4;
        peer.locRib = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.locRibFiltered = false; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)
        peer.tableName = ""; // TODO: Not implemented (see RFC draft-ietf-grow-bmp-loc-rib)

        return Optional.of(new Message(context.collectorHashId, Type.PEER, ImmutableList.of(peer)));
    }

    private Optional<Message> handleStatisticReport(final BsonDocument document,
                                                    final Context context) {
        final Stat stat = new Stat();
        stat.action = Stat.Action.ADD;
        stat.sequence = SEQUENCE.getAndIncrement();
        stat.routerHash = Record.hash(context.sourceAddress.getHostAddress(), Integer.toString(context.sourcePort), context.collectorHashId);
        stat.routerIp = context.sourceAddress;
        stat.peerHash = Record.hash(getString(document, "peer", "address").get(),
                                    getString(document, "peer", "distinguisher").get(),
                                    stat.routerHash);
        stat.peerIp = getString(document, "peer", "address").map(InetAddressUtils::addr).get();
        stat.peerAsn = getInt64(document, "recv_open_msg", "as").get();
        stat.timestamp = context.timestamp;
        stat.prefixesRejected = getInt64(document, "rejected", "counter").map(Long::intValue).orElse(0);
        stat.knownDupPrefixes = getInt64(document, "duplicate_prefix", "counter").map(Long::intValue).orElse(0);
        stat.knownDupWithdraws = getInt64(document, "duplicate_withdraw", "counter").map(Long::intValue).orElse(0);
        stat.invalidClusterList = getInt64(document, "invalid_update_due_to_cluster_list_loop", "counter").map(Long::intValue).orElse(0);
        stat.invalidAsPath = getInt64(document, "invalid_update_due_to_as_path_loop", "counter").map(Long::intValue).orElse(0);
        stat.invalidOriginatorId = getInt64(document, "invalid_update_due_to_originator_id", "counter").map(Long::intValue).orElse(0);
        stat.invalidAsConfed = getInt64(document, "invalid_update_due_to_as_confed_loop", "counter").map(Long::intValue).orElse(0);
        stat.prefixesPrePolicy = getInt64(document, "adj_rib_in", "gauge").orElse(0L);
        stat.prefixesPostPolicy = getInt64(document, "loc_rib", "gauge").orElse(0L);

        return Optional.of(new Message(context.collectorHashId, Type.BMP_STAT, ImmutableList.of(stat)));

    }

    @Override
    public void handleMessage(final TelemetryMessageLogEntry message,
                              final TelemetryMessageLog messageLog) {
        LOG.trace("Parsing packet: {}", message);
        final BsonDocument document = new RawBsonDocument(message.getByteArray());


        final String collectorHashId = Record.hash(messageLog.getSystemId());
        final String routerHashId = Record.hash(messageLog.getSourceAddress(), Integer.toString(messageLog.getSourcePort()), collectorHashId);
        final Context context = new Context(collectorHashId,
                                            routerHashId,
                                            Instant.ofEpochMilli(message.getTimestamp()),
                                            InetAddressUtils.addr(messageLog.getSourceAddress()),
                                            messageLog.getSourcePort());

        getString(document, "@type")
                .map(Header.Type::valueOf)
                .orElseThrow(IllegalStateException::new)
                .<Optional<Message>>map(type -> {
                    switch (type) {
                        case ROUTE_MONITORING: {
                            return Optional.empty();
                        }
                        case STATISTICS_REPORT: {
                            return this.handleStatisticReport(document, context);
                        }
                        case PEER_DOWN_NOTIFICATION: {
                            return this.handlePeerUpNotification(document, context);
                        }
                        case PEER_UP_NOTIFICATION: {
                            return this.handlePeerDownNotification(document, context);
                        }
                        case INITIATION_MESSAGE: {
                            return this.handleInitiationMessage(document, context);
                        }
                        case TERMINATION_MESSAGE: {
                            return this.handleTerminationMessage(document, context);
                        }
                        case ROUTE_MIRRORING_MESSAGE: {
                            return Optional.empty();
                        }
                        default:
                            throw new IllegalStateException();
                    }
                }).ifPresent(this::send);
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
