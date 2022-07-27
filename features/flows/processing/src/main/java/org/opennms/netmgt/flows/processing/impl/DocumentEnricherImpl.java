/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.processing.impl;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.flows.processing.enrichment.NodeInfo;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;

public class DocumentEnricherImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentEnricherImpl.class);

    private static final String NODE_METADATA_CACHE = "flows.node.metadata";

    private final NodeDao nodeDao;

    private final IpInterfaceDao ipInterfaceDao;

    private final InterfaceToNodeCache interfaceToNodeCache;

    private final SessionUtils sessionUtils;

    private final ClassificationEngine classificationEngine;

    // Caches NodeDocument data for a given node Id.
    private final Cache<InterfaceToNodeCache.Entry, Optional<NodeInfo>> nodeInfoCache;

    // Caches NodeDocument data for a given node metadata.
    private final Cache<NodeMetadataKey, Optional<NodeInfo>> nodeMetadataCache;

    private final Timer nodeLoadTimer;

    private final long clockSkewCorrectionThreshold;

    private final DocumentMangler mangler;

    public DocumentEnricherImpl(final MetricRegistry metricRegistry,
                                final NodeDao nodeDao,
                                final IpInterfaceDao ipInterfaceDao,
                                final InterfaceToNodeCache interfaceToNodeCache,
                                final SessionUtils sessionUtils,
                                final ClassificationEngine classificationEngine,
                                final CacheConfig cacheConfig,
                                final long clockSkewCorrectionThreshold,
                                final DocumentMangler mangler) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);

        this.nodeInfoCache = new CacheBuilder()
                .withConfig(cacheConfig)
                .withCacheLoader(new CacheLoader<InterfaceToNodeCache.Entry, Optional<NodeInfo>>() {
                    @Override
                    public Optional<NodeInfo> load(InterfaceToNodeCache.Entry entry) {
                        return getNodeInfo(entry);
                    }
                }).build();

       final CacheConfig nodeMetadataCacheConfig = buildMetadataCacheConfig(cacheConfig);
       this.nodeMetadataCache = new CacheBuilder()
               .withConfig(nodeMetadataCacheConfig)
               .withCacheLoader(new CacheLoader<NodeMetadataKey, Optional<NodeInfo>>() {
                   @Override
                   public Optional<NodeInfo> load(NodeMetadataKey key) {
                       return getNodeInfoFromMetadataContext(key.contextKey, key.value);
                   }
               }).build();

        this.nodeLoadTimer = metricRegistry.timer("nodeLoadTime");

        this.clockSkewCorrectionThreshold = clockSkewCorrectionThreshold;

        this.mangler = Objects.requireNonNull(mangler);
    }

    public List<EnrichedFlow> enrich(final Collection<Flow> flows, final FlowSource source) {
        if (flows.isEmpty()) {
            LOG.info("Nothing to enrich.");
            return Collections.emptyList();
        }

        return sessionUtils.withTransaction(() -> flows.stream().flatMap(flow -> {
            final EnrichedFlow document = this.mangler.mangle(EnrichedFlow.from(flow));
            if (document == null) {
                return Stream.empty();
            }

            // Metadata from message
            document.setHost(source.getSourceAddress());
            document.setLocation(source.getLocation());

            // Node data
            getNodeInfoFromCache(source.getLocation(), source.getSourceAddress(), source.getContextKey(), flow.getNodeIdentifier()).ifPresent(document::setExporterNodeInfo);
            if (flow.getDstAddr() != null) {
                getNodeInfoFromCache(source.getLocation(), flow.getDstAddr(), null, null).ifPresent(document::setSrcNodeInfo);
            }
            if (flow.getSrcAddr() != null) {
                getNodeInfoFromCache(source.getLocation(), flow.getSrcAddr(), null, null).ifPresent(document::setDstNodeInfo);
            }

            // Locality
            if (flow.getSrcAddr() != null) {
                document.setSrcLocality(isPrivateAddress(flow.getSrcAddr()) ? EnrichedFlow.Locality.PRIVATE : EnrichedFlow.Locality.PUBLIC);
            }
            if (flow.getDstAddr() != null) {
                document.setDstLocality(isPrivateAddress(flow.getDstAddr()) ? EnrichedFlow.Locality.PRIVATE : EnrichedFlow.Locality.PUBLIC);
            }

            if (EnrichedFlow.Locality.PUBLIC.equals(document.getDstLocality()) || EnrichedFlow.Locality.PUBLIC.equals(document.getSrcLocality())) {
                document.setFlowLocality(EnrichedFlow.Locality.PUBLIC);
            } else if (EnrichedFlow.Locality.PRIVATE.equals(document.getDstLocality()) || EnrichedFlow.Locality.PRIVATE.equals(document.getSrcLocality())) {
                document.setFlowLocality(EnrichedFlow.Locality.PRIVATE);
            }

            final ClassificationRequest classificationRequest = createClassificationRequest(document);

            // Check whether classification is possible
            if (classificationRequest.isClassifiable()) {
                // Apply Application mapping
                document.setApplication(classificationEngine.classify(classificationRequest));
            }

            // Fix skewed clock
            // If received time and export time differ to much, correct all timestamps by the difference
            if (this.clockSkewCorrectionThreshold > 0) {
                final long skew = flow.getTimestamp() - flow.getReceivedAt();
                if (Math.abs(skew) >= this.clockSkewCorrectionThreshold) {
                    // The applied correction the the negative skew
                    document.setClockCorrection(-skew);

                    // Fix the skew on all timestamps of the flow
                    document.setTimestamp(flow.getTimestamp() - skew);
                    document.setFirstSwitched(flow.getFirstSwitched() - skew);
                    document.setDeltaSwitched(flow.getDeltaSwitched() - skew);
                    document.setLastSwitched(flow.getLastSwitched() - skew);
                }
            }

            return Stream.of(document);
        }).collect(Collectors.toList()));
    }

    private static boolean isPrivateAddress(String ipAddress) {
        final InetAddress inetAddress = InetAddressUtils.addr(ipAddress);
        return inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
    }

    private Optional<NodeInfo> getNodeInfoFromCache(final String location, final String ipAddress, final ContextKey contextKey, final String value) {
        Optional<NodeInfo> nodeDocument = Optional.empty();
        if (contextKey != null && !Strings.isNullOrEmpty(value)) {
            final NodeMetadataKey metadataKey = new NodeMetadataKey(contextKey, value);
            try {
                nodeDocument = this.nodeMetadataCache.get(metadataKey);
            } catch (ExecutionException e) {
                LOG.error("Error while retrieving NodeDocument from NodeMetadataCache: {}.", e.getMessage(), e);
                throw new RuntimeException(e);
            }
            if(nodeDocument.isPresent()) {
                return nodeDocument;
            }
        }

        final var entry = this.interfaceToNodeCache.getFirst(location, InetAddressUtils.addr(ipAddress));
        if(entry.isPresent()) {
            try {
                return this.nodeInfoCache.get(entry.get());
            } catch (ExecutionException e) {
                LOG.error("Error while retrieving NodeDocument from NodeInfoCache: {}.", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return nodeDocument;
    }


    // Key class, which is used to cache NodeInfo for a given node metadata.
    private static class NodeMetadataKey {

        public final ContextKey contextKey;

        public final String value;

        private NodeMetadataKey(final ContextKey contextKey, final String value) {
            this.contextKey = contextKey;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NodeMetadataKey that = (NodeMetadataKey) o;
            return Objects.equals(contextKey, that.contextKey) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contextKey, value);
        }
    }

    private Optional<NodeInfo> getNodeInfoFromMetadataContext(ContextKey contextKey, String value) {
        // First, try to find interface
        final List<OnmsIpInterface> ifaces;
        try (Timer.Context ctx = this.nodeLoadTimer.time()) {
            ifaces = this.ipInterfaceDao.findInterfacesWithMetadata(contextKey.getContext(), contextKey.getKey(), value);
        }
        if (!ifaces.isEmpty()) {
            final var iface = ifaces.get(0);
            return mapOnmsNodeToNodeDocument(iface.getNode(), iface.getId());
        }

        // Alternatively, try to find node and chose primary interface
        final List<OnmsNode> nodes;
        try (Timer.Context ctx = this.nodeLoadTimer.time()) {
            nodes = this.nodeDao.findNodeWithMetaData(contextKey.getContext(), contextKey.getKey(), value);
        }
        if(!nodes.isEmpty()) {
            final var node = nodes.get(0);
            return mapOnmsNodeToNodeDocument(node, node.getPrimaryInterface().getId());
        }

        return Optional.empty();
    }

    private Optional<NodeInfo> getNodeInfo(final InterfaceToNodeCache.Entry entry) {
        final OnmsNode onmsNode;
        try (Timer.Context ctx = this.nodeLoadTimer.time()) {
            onmsNode = this.nodeDao.get(entry.nodeId);
        }

        return mapOnmsNodeToNodeDocument(onmsNode, entry.interfaceId);
    }

    private Optional<NodeInfo> mapOnmsNodeToNodeDocument(final OnmsNode onmsNode, final int interfaceId) {
        if(onmsNode != null) {
            final NodeInfo nodeDocument = new NodeInfo();
            nodeDocument.setForeignSource(onmsNode.getForeignSource());
            nodeDocument.setForeignId(onmsNode.getForeignId());
            nodeDocument.setNodeId(onmsNode.getId());
            nodeDocument.setInterfaceId(interfaceId);
            nodeDocument.setCategories(onmsNode.getCategories().stream().map(OnmsCategory::getName).collect(Collectors.toList()));

            return Optional.of(nodeDocument);
        }
        return Optional.empty();
    }

    public static ClassificationRequest createClassificationRequest(EnrichedFlow document) {
        final ClassificationRequest request = new ClassificationRequest();
        request.setProtocol(Protocols.getProtocol(document.getProtocol()));
        request.setLocation(document.getLocation());
        request.setExporterAddress(document.getHost());
        request.setDstAddress(document.getDstAddr());
        request.setDstPort(document.getDstPort());
        request.setSrcAddress(document.getSrcAddr());
        request.setSrcPort(document.getSrcPort());

        return request;
    }

    private CacheConfig buildMetadataCacheConfig(CacheConfig cacheConfig) {
        // Use existing config for the nodes with a new name for node metadata cache.
        final CacheConfig metadataCacheConfig = new CacheConfigBuilder()
                .withName(NODE_METADATA_CACHE)
                .withMaximumSize(cacheConfig.getMaximumSize())
                .withExpireAfterWrite(cacheConfig.getExpireAfterWrite())
                .build();
        cacheConfig.setRecordStats(true);
        cacheConfig.setMetricRegistry(cacheConfig.getMetricRegistry());
        return metadataCacheConfig;
    }
}
