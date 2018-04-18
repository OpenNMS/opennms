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

package org.opennms.netmgt.flows.elastic;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheLoader;

public class DocumentEnricher {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentEnricher.class);

    private final NodeDao nodeDao;

    private final InterfaceToNodeCache interfaceToNodeCache;

    private final TransactionOperations transactionOperations;

    private final ClassificationEngine classificationEngine;

    // Caches NodeDocument data
    private final Cache<NodeInfoKey, Optional<NodeDocument>> nodeInfoCache;

    private final Timer nodeLoadTimer;

    public DocumentEnricher(MetricRegistry metricRegistry, NodeDao nodeDao, InterfaceToNodeCache interfaceToNodeCache,
                            TransactionOperations transactionOperations, ClassificationEngine classificationEngine,
                            CacheConfig cacheConfig) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);

        this.nodeInfoCache = new CacheBuilder()
                .withConfig(cacheConfig)
                .withCacheLoader(new CacheLoader<NodeInfoKey, Optional<NodeDocument>>() {
                    @Override
                    public Optional<NodeDocument> load(NodeInfoKey key) {
                        return getNodeInfo(key.location, key.ipAddress);
                    }
                }).build();
        this.nodeLoadTimer = metricRegistry.timer("nodeLoadTime");
    }

    public void enrich(final List<FlowDocument> documents, final FlowSource source) {
        if (documents.isEmpty()) {
            LOG.info("Nothing to enrich.");
            return;
        }

        transactionOperations.execute(callback -> {
            documents.forEach(document -> {
                // Metadata from message
                document.setHost(source.getSourceAddress());
                document.setLocation(source.getLocation());

                // Node data
                getNodeInfoFromCache(source.getLocation(), source.getSourceAddress()).ifPresent(document::setNodeExporter);
                if (document.getDstAddr() != null) {
                    getNodeInfoFromCache(source.getLocation(), document.getDstAddr()).ifPresent(document::setNodeDst);
                }
                if (document.getSrcAddr() != null) {
                    getNodeInfoFromCache(source.getLocation(), document.getSrcAddr()).ifPresent(document::setNodeSrc);
                }

                // Locality
                if (document.getSrcAddr() != null) {
                    document.setSrcLocality(isPrivateAddress(document.getSrcAddr()) ? Locality.PRIVATE : Locality.PUBLIC);
                }
                if (document.getDstAddr() != null) {
                    document.setDstLocality(isPrivateAddress(document.getDstAddr()) ? Locality.PRIVATE : Locality.PUBLIC);
                }

                if (Locality.PUBLIC.equals(document.getDstLocality()) || Locality.PUBLIC.equals(document.getSrcLocality())) {
                    document.setFlowLocality(Locality.PUBLIC);
                } else if (Locality.PRIVATE.equals(document.getDstLocality()) || Locality.PRIVATE.equals(document.getSrcLocality())) {
                    document.setFlowLocality(Locality.PRIVATE);
                }

                // Conversation tagging
                document.setConvoKey(ConversationKeyUtils.getConvoKeyAsJsonString(document));

                // Apply Application mapping
                document.setApplication(classificationEngine.classify(createClassificationRequest(document)));
            });
            return null;
        });
    }

    private static boolean isPrivateAddress(String ipAddress) {
        final InetAddress inetAddress = InetAddressUtils.addr(ipAddress);
        return inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
    }

    private Optional<NodeDocument> getNodeInfoFromCache(String location, String ipAddress) {
        final NodeInfoKey key = new NodeInfoKey(location, ipAddress);
        try {
            return nodeInfoCache.get(key);
        } catch (ExecutionException e) {
            LOG.error("Error while retrieving NodeDocument from NodeInfoCache: {}.", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Optional<NodeDocument> getNodeInfo(String location, String ipAddress) {
        return getNodeInfo(location, InetAddressUtils.addr(ipAddress));
    }

    private Optional<NodeDocument> getNodeInfo(String location, InetAddress ipAddress) {
        final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(location, ipAddress);
        if (nodeId.isPresent()) {
            try (Timer.Context ctx = nodeLoadTimer.time()) {
                final OnmsNode onmsNode = nodeDao.get(nodeId.get());
                if (onmsNode != null) {
                    final NodeDocument nodeInfo = new NodeDocument();
                    nodeInfo.setForeignSource(onmsNode.getForeignSource());
                    nodeInfo.setForeignId(onmsNode.getForeignId());
                    nodeInfo.setNodeId(nodeId.get());
                    nodeInfo.setCategories(onmsNode.getCategories().stream().map(OnmsCategory::getName).collect(Collectors.toList()));

                    return Optional.of(nodeInfo);
                } else {
                    LOG.warn("Node with id: {} at location: {} with IP address: {} is in the interface to node cache, but wasn't found in the database.");
                }
            }
        }
        return Optional.empty();
    }

    // Key class, which is used to cache NodeDocument objects
    private static class NodeInfoKey {

        public final String location;

        public final String ipAddress;

        private NodeInfoKey(String location, String ipAddress) {
            this.location = location;
            this.ipAddress = ipAddress;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NodeInfoKey that = (NodeInfoKey) o;
            return Objects.equals(location, that.location) &&
                   Objects.equals(ipAddress, that.ipAddress);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, ipAddress);
        }
    }

    protected static ClassificationRequest createClassificationRequest(FlowDocument document) {
        final ClassificationRequest request = new ClassificationRequest();
        request.setProtocol(Protocols.getProtocol(document.getProtocol()));
        request.setLocation(document.getLocation());
        request.setExporterAddress(document.getHost());
        if (document.getDirection() == Direction.INGRESS) {
            request.setDstAddress(document.getDstAddr());
            request.setDstPort(document.getDstPort());
            request.setSrcAddress(document.getSrcAddr());
            request.setSrcPort(document.getSrcPort());
        } else {
            request.setSrcAddress(document.getDstAddr());
            request.setSrcPort(document.getDstPort());
            request.setDstAddress(document.getSrcAddr());
            request.setDstPort(document.getSrcPort());
        }
        return request;
    }
}
