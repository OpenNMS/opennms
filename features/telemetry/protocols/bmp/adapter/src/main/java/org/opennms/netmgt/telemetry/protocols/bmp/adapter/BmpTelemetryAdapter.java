/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.address;
import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.timestamp;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractCollectionAdapter;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;

public class BmpTelemetryAdapter extends AbstractCollectionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpTelemetryAdapter.class);

    private CollectionAgentFactory collectionAgentFactory;

    private InterfaceToNodeCache interfaceToNodeCache;

    private String metaDataNodeLookup;
    private ContextKey contextKey;
    private NodeDao nodeDao;
    private TransactionTemplate transactionTemplate;

    private static class ExporterInfo {
        public final int nodeId;
        public final InetAddress nodeAddress;

        public ExporterInfo(int nodeId, InetAddress nodeAddress) {
            this.nodeId = nodeId;
            this.nodeAddress = nodeAddress;
        }
    }

    public BmpTelemetryAdapter(final AdapterDefinition adapterConfig,
                               final MetricRegistry metricRegistry,
                               final NodeDao nodeDao,
                               final TransactionTemplate transactionTemplate) {
        super(adapterConfig, metricRegistry);
        this.nodeDao = nodeDao;
        this.transactionTemplate = transactionTemplate;
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
        InetAddress exporterAddress = InetAddressUtils.getInetAddress(messageLog.getSourceAddress());
        Optional<Integer> exporterNodeId = this.interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), exporterAddress);

        if (!exporterNodeId.isPresent()) {
            if (message.hasBgpId()) {
                final InetAddress bgpId = address(message.getBgpId());
                LOG.info("Unable to find node for exporter address: {} at location: {}. Trying to lookup by bgpId: {}",
                        exporterAddress, messageLog.getLocation(), bgpId);

                final ExporterInfo exporterInfo = transactionTemplate.execute(new TransactionCallback<ExporterInfo>() {
                    @Override
                    public ExporterInfo doInTransaction(final TransactionStatus transactionStatus) {
                        final List<OnmsNode> nodes = nodeDao.findNodeWithMetaData(contextKey.getContext(), contextKey.getKey(), InetAddressUtils.toIpAddrString(bgpId));

                        if (!nodes.isEmpty()) {
                            final OnmsNode firstNode = nodes.get(0);
                            if (nodes.size() > 1) {
                                LOG.warn("More that one node match bgpId: {}. Using the first: {}", bgpId, firstNode.getId());
                            }

                            if (firstNode.containsInterface(bgpId)) {
                                return new ExporterInfo(firstNode.getId(), bgpId);
                            } else {
                                return new ExporterInfo(firstNode.getId(), firstNode.getPrimaryInterface().getIpAddress());
                            }
                        } else {
                            LOG.warn("Unable to find node for bgpId: {}. Message will be ignored.", bgpId);
                            return null;
                        }
                    }
                });

                if (exporterAddress == null) {
                    return Stream.empty();
                }

                exporterNodeId = Optional.of(exporterInfo.nodeId);
                exporterAddress = exporterInfo.nodeAddress;

            } else {
                LOG.info("Unable to find node for exporter address: {} at location: {}. Message has no bgpId. Message will be ignored.",
                        exporterAddress, messageLog.getLocation());
                return Stream.empty();
            }
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
            builder.withIdentifiedNumericAttribute(peerResource, "bmp", name, gauge.getValue(), AttributeType.GAUGE, identifier);
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

        Optional.ofNullable(stats.getPerAfiAdjRibInMap()).ifPresent(m -> m.entrySet().stream()
                .forEach(
                    e -> {
                        addGauge.apply("adj_r_in_" + e.getKey().replace(":","_"));
                    }
                )
        );

        Optional.ofNullable(stats.getUpdateTreatAsWithdraw()).ifPresent(addCounter.apply("update_withdraw"));
        Optional.ofNullable(stats.getPrefixTreatAsWithdraw()).ifPresent(addCounter.apply("prefix_withdraw"));
        Optional.ofNullable(stats.getDuplicateUpdate()).ifPresent(addCounter.apply("duplicate_update"));
        Optional.ofNullable(stats.getLocalRib()).ifPresent(addGauge.apply("local_rib"));
        Optional.ofNullable(stats.getExportRib()).ifPresent(addGauge.apply("export_rib"));

        Optional.ofNullable(stats.getPerAfiExportRibMap()).ifPresent(m -> m.entrySet().stream()
                .forEach(
                    e -> {
                        addGauge.apply("exp_r_" + e.getKey().replace(":","_"));
                    }
                )
        );

        return Stream.of(new CollectionSetWithAgent(agent, builder.build()));
    }

    public void setCollectionAgentFactory(final CollectionAgentFactory collectionAgentFactory) {
        this.collectionAgentFactory = collectionAgentFactory;
    }

    public void setInterfaceToNodeCache(final InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public String getMetaDataNodeLookup() {
        return metaDataNodeLookup;
    }

    public void setMetaDataNodeLookup(String metaDataNodeLookup) {
        this.metaDataNodeLookup = metaDataNodeLookup;

        if (!Strings.isNullOrEmpty(this.metaDataNodeLookup)) {
            this.contextKey = new ContextKey(metaDataNodeLookup);
        } else {
            this.contextKey = null;
        }
    }
}
