/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.telemetry.protocols.cache;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;

public class NodeInfoCacheImpl implements NodeInfoCache {
    private static final Logger LOG = LoggerFactory.getLogger(NodeInfoCacheImpl.class);

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
    private final NodeDao nodeDao;
    private final IpInterfaceDao ipInterfaceDao;
    private final InterfaceToNodeCache interfaceToNodeCache;
    private final Cache<NodeMetadataKey, Optional<NodeInfo>> nodeMetadataCache;
    private final Cache<InterfaceToNodeCache.Entry, Optional<NodeInfo>> nodeInfoCache;
    private final Timer nodeLoadTimer;

    private final SessionUtils sessionUtils;

    public NodeInfoCacheImpl(final CacheConfig nodeInfoCacheConfig, final boolean nodeMetadataEnabled, final MetricRegistry metricRegistry, final NodeDao nodeDao, final IpInterfaceDao ipInterfaceDao, final InterfaceToNodeCache interfaceToNodeCache, final SessionUtils sessionUtils) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);

        final CacheConfig nodeMetadataCacheConfig = new CacheConfigBuilder()
                .withName("nodeMetadataCache")
                .withExpireAfterRead(nodeInfoCacheConfig.getExpireAfterRead())
                .withExpireAfterWrite(nodeInfoCacheConfig.getExpireAfterWrite())
                .withMaximumSize(nodeInfoCacheConfig.getMaximumSize())
                .build();

        this.nodeInfoCache = new CacheBuilder()
                .withConfig(Objects.requireNonNull(nodeInfoCacheConfig))
                .withCacheLoader(new CacheLoader<InterfaceToNodeCache.Entry, Optional<NodeInfo>>() {
                    @Override
                    public Optional<NodeInfo> load(InterfaceToNodeCache.Entry entry) {
                        return getNodeInfo(entry);
                    }
                }).build();

        this.nodeMetadataCache = new CacheBuilder()
                .withConfig(nodeMetadataCacheConfig)
                .withCacheLoader(new CacheLoader<NodeMetadataKey, Optional<NodeInfo>>() {
                    @Override
                    public Optional<NodeInfo> load(NodeMetadataKey key) {
                        return getNodeInfoFromMetadataContext(key.contextKey, key.value);
                    }
                }).build();

        this.nodeLoadTimer = Objects.requireNonNull(metricRegistry).timer("nodeLoadTime");
    }

    public Optional<NodeInfo> getNodeInfoFromCache(final String location, final String ipAddress, final ContextKey contextKey, final String value) {
        return sessionUtils.withTransaction(() -> {
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
        });
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
}
