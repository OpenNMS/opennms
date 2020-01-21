/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;

public class BmpTelemetryAdapter extends AbstractCollectionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpTelemetryAdapter.class);

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    public BmpTelemetryAdapter(final AdapterDefinition adapterConfig,
                               final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(final TelemetryMessageLogEntry message,
                                                                  final TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        LOG.trace("Parsing packet: {}", message);
        final BsonDocument document = new RawBsonDocument(message.getByteArray());

        // This adapter only cares about statistic packets
        if (!getString(document, "@type")
                .map(type -> Header.Type.STATISTICS_REPORT.name().equals(type))
                .orElse(false)) {
            return Stream.empty();
        }

        // Find the node for the router who has exported the stats and build a collection agent for it
        final InetAddress exporterAddress = InetAddressUtils.getInetAddress(messageLog.getSourceAddress());
        final Optional<Integer> exporterNodeId = this.interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), exporterAddress);
        if (!exporterNodeId.isPresent()) {
            LOG.warn("Unable to find node and interface for agent address: {}", exporterAddress);
            return Stream.empty();
        }
        final CollectionAgent agent = this.collectionAgentFactory.createCollectionAgent(Integer.toString(exporterNodeId.get()), exporterAddress);

        // Extract peer details
        final String peerAddress = getString(document, "peer", "address").get();
        final Instant timestamp = Instant.ofEpochSecond(getInt64(document, "peer", "timestamp", "epoch").get(),
                                                        getInt64(document, "peer", "timestamp", "nanos").orElse(0L));
        final String as = Long.toString(getInt64(document, "peer", "as").get());
        final String id = Long.toString(getInt64(document, "peer", "id").get());

        // Build resource for the peer
        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
        final DeferredGenericTypeResource peerResource = new DeferredGenericTypeResource(nodeResource, "bmp", peerAddress);

        // Build the collection set for the peer
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withTimestamp(Date.from(timestamp));
        builder.withStringAttribute(peerResource, "bmp", "address", peerAddress);
        builder.withStringAttribute(peerResource, "bmp", "as", as);
        builder.withStringAttribute(peerResource, "bmp", "id", id);

        final BsonDocument stats = document.getDocument("stats");
        for (final String key : stats.keySet()) {
            final BsonDocument metric = stats.getDocument(key);

            final String identifier = String.format("bmp_%s_%s", peerAddress, key);

            getInt64(metric, "counter").ifPresent(counter -> {
                Optional.ofNullable(METRIC_ATTRIBUTE_MAP.get(key)).ifPresent(name -> {
                    builder.withIdentifiedNumericAttribute(peerResource, "bmp", name, counter, AttributeType.COUNTER, identifier);
                });
            });
            getInt64(metric, "gauge").ifPresent(gauge -> {
                Optional.ofNullable(METRIC_ATTRIBUTE_MAP.get(key)).ifPresent(name -> {
                    builder.withIdentifiedNumericAttribute(peerResource, "bmp", name, gauge, AttributeType.GAUGE, identifier);
                });
            });
        }

        return Stream.of(new CollectionSetWithAgent(agent, builder.build()));
    }

    public void setCollectionAgentFactory(final CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(final InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    private static final Map<String, String> METRIC_ATTRIBUTE_MAP = ImmutableMap.<String, String>builder()
        .put(BmpParser.METRIC_DUPLICATE_PREFIX, "duplicate_prefix")
        .put(BmpParser.METRIC_DUPLICATE_WITHDRAW, "duplicate_withdraw")
        .put(BmpParser.METRIC_ADJ_RIB_IN, "adj_rib_in")
        .put(BmpParser.METRIC_ADJ_RIB_OUT, "adj_rib_out")
        .put(BmpParser.METRIC_EXPORT_RIB, "export_rib")
        .put(BmpParser.METRIC_INVALID_UPDATE_DUE_TO_AS_CONFED_LOOP, "inv_as_confed_loop")
        .put(BmpParser.METRIC_INVALID_UPDATE_DUE_TO_AS_PATH_LOOP, "inv_as_path_loop")
        .put(BmpParser.METRIC_INVALID_UPDATE_DUE_TO_CLUSTER_LIST_LOOP, "inv_cl_loop")
        .put(BmpParser.METRIC_INVALID_UPDATE_DUE_TO_ORIGINATOR_ID, "inv_originator_id")
        .put(BmpParser.METRIC_PREFIX_TREAT_AS_WITHDRAW, "prefix_withdraw")
        .put(BmpParser.METRIC_UPDATE_TREAT_AS_WITHDRAW, "update_withdraw")
        .put(BmpParser.METRIC_LOC_RIB, "loc_rib")
        .put(BmpParser.METRIC_DUPLICATE_UPDATE, "duplicate_update")
        .put(BmpParser.METRIC_REJECTED, "rejected")
        .build();
}
