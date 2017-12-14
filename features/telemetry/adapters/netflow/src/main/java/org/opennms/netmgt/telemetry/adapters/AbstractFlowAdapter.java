/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.flows.api.NodeInfo;
import org.opennms.netmgt.flows.api.PersistenceException;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessage;
import org.opennms.netmgt.telemetry.adapters.api.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class AbstractFlowAdapter<T> implements Adapter {

    // Key class, which is used to cache NodeInfo objects
    private static class NodeInfoKey {

        public final String location;

        public final String ipAddress;

        public NodeInfoKey(String location, String ipAddress) {
            this.location = location;
            this.ipAddress = ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NodeInfoKey that = (NodeInfoKey) o;
            boolean equals = Objects.equals(location, that.location)
                    && Objects.equals(ipAddress, that.ipAddress);
            return equals;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, ipAddress);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractFlowAdapter.class);

    private MetricRegistry metricRegistry;

    private InterfaceToNodeCache interfaceToNodeCache;

    private NodeDao nodeDao;

    private TransactionOperations transactionOperations;

    private FlowRepository flowRepository;


    /**
     * Flows/second throughput
     */
    private Meter flowsPersistedMeter;

    /**
     * Time taken to parse a log
     */
    private Timer logParsingTimer;

    /**
     * Time taken to convert and enrich the flows in a log
     */
    private Timer logEnrichementTimer;

    /**
     * Time taken to perist the flows in alog
     */
    private Timer logPersistingTimer;

    /**
     * Number of packets per log.
     */
    private Histogram packetsPerLogHistogram;

    /**
     * Number of flows per packet.
     */
    private Histogram flowsPerPacketHistogram;

    // Caches NodeInfo data
    private LoadingCache<NodeInfoKey, Optional<NodeInfo>> nodeInfoCache;

    protected AbstractFlowAdapter() {
    }

    protected abstract T parse(final TelemetryMessage message);
    protected abstract List<NetflowDocument> convert(final T packet);

    public void init() {
        flowsPersistedMeter = metricRegistry.meter("flowsPersisted");
        logParsingTimer = metricRegistry.timer("logParsing");
        logEnrichementTimer = metricRegistry.timer("logEnrichment");
        logPersistingTimer = metricRegistry.timer("logPersisting");
        packetsPerLogHistogram = metricRegistry.histogram("packetsPerLog");
        flowsPerPacketHistogram = metricRegistry.histogram("flowsPerPacket");

        // TODO MVR make this configurable, when it is actually an osgi-module
        nodeInfoCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(300, TimeUnit.SECONDS) // 5 Minutes
                .build(new CacheLoader<NodeInfoKey, Optional<NodeInfo>>() {
                    @Override
                    public Optional<NodeInfo> load(NodeInfoKey key) throws Exception {
                        return getNodeInfo(key.location, key.ipAddress);
                    }
                });

        // Verify initialized
        Objects.requireNonNull(metricRegistry);
        Objects.requireNonNull(interfaceToNodeCache);
        Objects.requireNonNull(nodeDao);
        Objects.requireNonNull(transactionOperations);
        Objects.requireNonNull(flowRepository);
    }

    @Override
    public void setProtocol(Protocol protocol) {
        // we do not need the protocol
    }

    @Override
    public void handleMessageLog(TelemetryMessageLog messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessageList().size());

        final List<T> flowPackets = new LinkedList<>();
        try (Timer.Context ctx = logParsingTimer.time()) {
            for (TelemetryMessage eachMessage : messageLog.getMessageList()) {
                LOG.trace("Parsing packet: {}", eachMessage);
                final T flowPacket = this.parse(eachMessage);
                if (flowPacket != null) {
                    flowPackets.add(flowPacket);
                }
            }
        }
        packetsPerLogHistogram.update(flowPackets.size());

        final List<NetflowDocument> flowDocuments;
        try (Timer.Context ctx = logEnrichementTimer.time()) {
            LOG.debug("Converting {} packets to flows.", flowPackets.size());
            flowDocuments = flowPackets.stream()
                    .map(pkts -> {
                        final List<NetflowDocument> docs = this.convert(pkts);
                        // Track the number of flows per packet
                        flowsPerPacketHistogram.update(docs.size());
                        return docs;
                    })
                    .flatMap(docs -> docs.stream())
                    .collect(Collectors.toList());
            LOG.debug("Enriching {} flows.", flowDocuments.size());
            enrich(flowDocuments, messageLog);
        }

        try (Timer.Context ctx = logPersistingTimer.time()) {
            LOG.debug("Persisting {}.", flowDocuments.size());
            persist(flowDocuments);
            flowsPersistedMeter.mark(flowDocuments.size());
        } catch (Exception e) {
            LOG.error("An error occurred while handling incoming flow packets: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        LOG.debug("Completed processing {} telemetry messages into {} flows.",
                messageLog.getMessageList().size(), flowDocuments.size());
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void setInterfaceToNodeCache(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = interfaceToNodeCache;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.transactionOperations = transactionOperations;
    }

    public void setFlowRepository(FlowRepository flowRepository) {
        this.flowRepository = flowRepository;
    }

    private void enrich(final List<NetflowDocument> documents, final TelemetryMessageLog messageLog) {
        enrich(documents, messageLog.getLocation(), messageLog.getSourceAddress());
    }

    public void enrich(final List<NetflowDocument> documents, final String location, final String sourceAddress) {
        if (documents.isEmpty()) {
            LOG.debug("Nothing to enrich.");
            return;
        }

        transactionOperations.execute(callback -> {
            documents.stream().forEach(document -> {
                // Metadata from message
                document.setExporterAddress(sourceAddress);
                document.setLocation(location);

                // Node data
                getNodeInfoFromCache(location, sourceAddress).ifPresent(node -> document.setExporterNodeInfo(node));
                getNodeInfoFromCache(location, document.getIpv4DestAddress()).ifPresent(node -> document.setDestNodeInfo(node));
                getNodeInfoFromCache(location, document.getIpv4SourceAddress()).ifPresent(node -> document.setSourceNodeInfo(node));
            });
            return null;
        });
    }

    private Optional<NodeInfo> getNodeInfoFromCache(String location, String ipAddress) {
        final NodeInfoKey key = new NodeInfoKey(location, ipAddress);
        try {
            final Optional<NodeInfo> value = nodeInfoCache.get(key);
            return value;
        } catch (ExecutionException e) {
            LOG.error("Error while retrieving NodeInfo from NodeInfoCache: {}.", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Optional<NodeInfo> getNodeInfo(String location, String ipAddress) {
        return getNodeInfo(location, InetAddressUtils.addr(ipAddress));
    }

    private Optional<NodeInfo> getNodeInfo(String location, InetAddress ipAddress) {
        final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(location, ipAddress);
        if (nodeId.isPresent()) {
            final OnmsNode onmsNode = nodeDao.get(nodeId.get());

            final NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setForeignSource(onmsNode.getForeignSource());
            nodeInfo.setForeignId(onmsNode.getForeignId());
            nodeInfo.setCategories(onmsNode.getCategories().stream().map(c -> c.getName()).collect(Collectors.toList()));

            return Optional.of(nodeInfo);
        }
        return Optional.empty();
    }

    private void persist(List<NetflowDocument> documents) throws Exception {
        if (documents.isEmpty()) {
            LOG.debug("Nothing to persist");
            return;
        }

        try {
            flowRepository.save(documents);
        } catch (PersistenceException ex) {
            LOG.error("Not all flows have been persisted: {}", ex.getMessage());
            ex.getFailedItems().forEach(failedItem -> {
                LOG.error("Flow {} could not be persisted. Reason: {}", failedItem.getItem(), failedItem.getCause().getMessage(), failedItem.getCause());
            });
        }
    }
}
