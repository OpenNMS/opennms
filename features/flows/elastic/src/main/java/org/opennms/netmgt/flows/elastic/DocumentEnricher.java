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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheBuilder;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;

public class DocumentEnricher {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentEnricher.class);

    private final NodeDao nodeDao;

    private final InterfaceToNodeCache interfaceToNodeCache;

    private final SessionUtils sessionUtils;

    private final ClassificationEngine classificationEngine;

    // Caches NodeDocument data
    private final Cache<NodeInfoKey, Optional<NodeDocument>> nodeInfoCache;

    private final Timer nodeLoadTimer;

    public DocumentEnricher(MetricRegistry metricRegistry, NodeDao nodeDao, InterfaceToNodeCache interfaceToNodeCache,
                            SessionUtils sessionUtils, ClassificationEngine classificationEngine,
                            CacheConfig cacheConfig) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);

        this.nodeInfoCache = new CacheBuilder()
                .withConfig(cacheConfig)
                .withCacheLoader(new CacheLoader<NodeInfoKey, Optional<NodeDocument>>() {
                    @Override
                    public Optional<NodeDocument> load(NodeInfoKey key) {
                        return getNodeInfo(key.location, key.ipAddress, key.contextKey, key.value);
                    }
                }).build();
        this.nodeLoadTimer = metricRegistry.timer("nodeLoadTime");
    }

    public List<FlowDocument> enrich(final Collection<Flow> flows, final FlowSource source) {
        if (flows.isEmpty()) {
            LOG.info("Nothing to enrich.");
            return Collections.emptyList();
        }

        return sessionUtils.withTransaction(() -> flows.stream().map(flow -> {
            final FlowDocument document = FlowDocument.from(flow);
            // Metadata from message
            document.setHost(source.getSourceAddress());
            document.setLocation(source.getLocation());

            // Node data
            getNodeInfoFromCache(source.getLocation(), source.getSourceAddress(), source.getContextKey(), flow.getNodeIdentifier()).ifPresent(document::setNodeExporter);
            if (document.getDstAddr() != null) {
                getNodeInfoFromCache(source.getLocation(), document.getDstAddr(), null, null).ifPresent(document::setNodeDst);
            }
            if (document.getSrcAddr() != null) {
                getNodeInfoFromCache(source.getLocation(), document.getSrcAddr(), null, null).ifPresent(document::setNodeSrc);
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

            final ClassificationRequest classificationRequest = createClassificationRequest(document);

            // Check whether classification is possible
            if (classificationRequest.isClassifiable()) {
                // Apply Application mapping
                document.setApplication(classificationEngine.classify(classificationRequest));
            }

            // Conversation tagging
            document.setConvoKey(ConversationKeyUtils.getConvoKeyAsJsonString(document));

            return document;
        }).collect(Collectors.toList()));
    }

    private static boolean isPrivateAddress(String ipAddress) {
        final InetAddress inetAddress = InetAddressUtils.addr(ipAddress);
        return inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isSiteLocalAddress();
    }

    private Optional<NodeDocument> getNodeInfoFromCache(final String location, final String ipAddress, final ContextKey contextKey, final String value) {
        final NodeInfoKey key = new NodeInfoKey(location, ipAddress, contextKey, value);
        try {
            return nodeInfoCache.get(key);
        } catch (ExecutionException e) {
            LOG.error("Error while retrieving NodeDocument from NodeInfoCache: {}.", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Optional<NodeDocument> getNodeInfo(final String location, final String ipAddress, final ContextKey contextKey, final String value) {
        return getNodeInfo(location, InetAddressUtils.addr(ipAddress), contextKey, value);
    }

    private Optional<NodeDocument> getNodeInfo(final String location, final InetAddress ipAddress, final ContextKey contextKey, final String value) {
        OnmsNode onmsNode = null;

        if (contextKey != null && !Strings.isNullOrEmpty(value)) {
            final List<OnmsNode> nodes = nodeDao.findNodeWithMetaData(contextKey.getContext(), contextKey.getKey(), value);

            if (!nodes.isEmpty()) {
                onmsNode = nodes.get(0);
            }
        }

        if (onmsNode == null) {
            final Optional<Integer> nodeId = interfaceToNodeCache.getFirstNodeId(location, ipAddress);
            if (nodeId.isPresent()) {
                try (Timer.Context ctx = nodeLoadTimer.time()) {
                    onmsNode = nodeDao.get(nodeId.get());
                }
            } else {
                LOG.warn("Node with id: {} at location: {} with IP address: {} is in the interface to node cache, but wasn't found in the database.", nodeId, location, ipAddress);
            }
        }

        if (onmsNode != null) {
            final NodeDocument nodeDocument = new NodeDocument();
            nodeDocument.setForeignSource(onmsNode.getForeignSource());
            nodeDocument.setForeignId(onmsNode.getForeignId());
            nodeDocument.setNodeId(onmsNode.getId());
            nodeDocument.setCategories(onmsNode.getCategories().stream().map(OnmsCategory::getName).collect(Collectors.toList()));

            return Optional.of(nodeDocument);
        }

        return Optional.empty();
    }

    // Key class, which is used to cache NodeDocument objects
    private static class NodeInfoKey {

        public final String location;

        public final String ipAddress;

        public final ContextKey contextKey;

        public final String value;

        private NodeInfoKey(final String location, final String ipAddress, final ContextKey contextKey, final String value) {
            this.location = location;
            this.ipAddress = ipAddress;
            this.contextKey = contextKey;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NodeInfoKey that = (NodeInfoKey) o;
            return Objects.equals(location, that.location) &&
                    Objects.equals(ipAddress, that.ipAddress) &&
                    Objects.equals(contextKey, that.contextKey) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, ipAddress, contextKey, value);
        }
    }

    protected static ClassificationRequest createClassificationRequest(FlowDocument document) {
        final ClassificationRequest request = new ClassificationRequest();
        request.setProtocol(document.getProtocol() == null ? null : Protocols.getProtocol(document.getProtocol()));
        request.setLocation(document.getLocation());
        request.setExporterAddress(document.getHost());

        request.setDstAddress(document.getDstAddr());
        request.setDstPort(document.getDstPort());
        request.setSrcAddress(document.getSrcAddr());
        request.setSrcPort(document.getSrcPort());

        return request;
    }
}
