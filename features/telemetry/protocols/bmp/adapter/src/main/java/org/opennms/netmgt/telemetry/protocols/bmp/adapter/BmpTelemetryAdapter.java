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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.address;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.timestamp;

import java.net.InetAddress;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class BmpTelemetryAdapter extends AbstractCollectionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpTelemetryAdapter.class);

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    public BmpTelemetryAdapter(final AdapterDefinition adapterConfig,
                               final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);
    }

    @Override
    public Stream<CollectionSetWithAgent> handleCollectionMessage(final TelemetryMessageLogEntry messageLogEntry,
                                                                  final TelemetryMessageLog messageLog) {
        LOG.trace("Parsing packet: {}", messageLogEntry);
        final Transport.Message message;
        try {
            message = Transport.Message.parseFrom(messageLogEntry.getByteArray());
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Invalid message", e);
            return Stream.empty();
        }

        // This adapter only cares about statistic packets
        if (!message.hasStatisticsReport()) {
            return Stream.empty();
        }

        final Transport.StatisticsReportPacket stats = message.getStatisticsReport();

        // Find the node for the router who has exported the stats and build a collection agent for it
        final InetAddress exporterAddress = InetAddressUtils.getInetAddress(messageLog.getSourceAddress());
        final Optional<Integer> exporterNodeId = this.interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), exporterAddress);
        if (!exporterNodeId.isPresent()) {
            LOG.warn("Unable to find node and interface for agent address: {}", exporterAddress);
            return Stream.empty();
        }
        final CollectionAgent agent = this.collectionAgentFactory.createCollectionAgent(Integer.toString(exporterNodeId.get()), exporterAddress);

        // Extract peer details
        final String peerAddress = InetAddressUtils.str(address(stats.getPeer().getAddress()));

        // Build resource for the peer
        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
        final DeferredGenericTypeResource peerResource = new DeferredGenericTypeResource(nodeResource, "bmp", peerAddress);

        // Build the collection set for the peer
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withTimestamp(Date.from(timestamp(stats.getPeer().getTimestamp())));
        builder.withStringAttribute(peerResource, "bmp", "address", peerAddress);
        builder.withStringAttribute(peerResource, "bmp", "as", Long.toString(stats.getPeer().getAs()));
        builder.withStringAttribute(peerResource, "bmp", "id", BmpAdapterTools.addressAsStr(stats.getPeer().getId()));

        final Function<String, Consumer<Transport.StatisticsReportPacket.Counter>> addCounter = (name) -> (counter) -> {
            final String identifier = String.format("bmp_%s_%s", peerAddress, name);
            builder.withIdentifiedNumericAttribute(peerResource, "bmp", name, counter.getCount(), AttributeType.COUNTER, identifier);
        };

        final Function<String, Consumer<Transport.StatisticsReportPacket.Gauge>> addGauge = (name) -> (gauge) -> {
            final String identifier = String.format("bmp_%s_%s", peerAddress, name);
            builder.withIdentifiedNumericAttribute(peerResource, "bmp", name, gauge.getValue(), AttributeType.COUNTER, identifier);
        };

        Optional.ofNullable(stats.getRejected()).ifPresent(addCounter.apply("rejected"));
        Optional.ofNullable(stats.getDuplicatePrefix()).ifPresent(addCounter.apply("duplicate_prefix"));
        Optional.ofNullable(stats.getDuplicateWithdraw()).ifPresent(addCounter.apply("duplicate_withdraw"));
        Optional.ofNullable(stats.getInvalidUpdateDueToAsConfedLoop()).ifPresent(addCounter.apply("inv_as_confed_loop"));
        Optional.ofNullable(stats.getInvalidUpdateDueToAsPathLoop()).ifPresent(addCounter.apply("inv_as_path_loop"));
        Optional.ofNullable(stats.getInvalidUpdateDueToClusterListLoop()).ifPresent(addCounter.apply("inv_cl_loop"));
        Optional.ofNullable(stats.getInvalidUpdateDueToOriginatorId()).ifPresent(addCounter.apply("inv_originator_id"));
        Optional.ofNullable(stats.getAdjRibIn()).ifPresent(addGauge.apply("adj_rib_in"));
        Optional.ofNullable(stats.getAdjRibOut()).ifPresent(addGauge.apply("adj_rib_out"));

        // TODO fooker: Add per AFI counters (perAfiAdjRibIn and perAfiLocalRib)
        // See https://issues.opennms.org/browse/NMS-12553

        Optional.ofNullable(stats.getUpdateTreatAsWithdraw()).ifPresent(addCounter.apply("update_withdraw"));
        Optional.ofNullable(stats.getPrefixTreatAsWithdraw()).ifPresent(addCounter.apply("prefix_withdraw"));
        Optional.ofNullable(stats.getDuplicateUpdate()).ifPresent(addCounter.apply("duplicate_update"));
        Optional.ofNullable(stats.getLocalRib()).ifPresent(addGauge.apply("local_rib"));
        Optional.ofNullable(stats.getExportRib()).ifPresent(addGauge.apply("export_rib"));

        // TODO fooker: Add per AFI counters (perAfiAdjRibOut and perAfiExportRib)
        // See https://issues.opennms.org/browse/NMS-12553

        return Stream.of(new CollectionSetWithAgent(agent, builder.build()));
    }

    public void setCollectionAgentFactory(final CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(final InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }
}
