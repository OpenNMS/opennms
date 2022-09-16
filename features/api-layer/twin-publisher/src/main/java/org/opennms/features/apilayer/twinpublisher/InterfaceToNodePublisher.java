/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.twinpublisher;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.reflect.TypeToken;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.features.apilayer.api.InterfaceToNodeInfo;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache.Entry;
import org.opennms.netmgt.dao.api.InterfaceToNodeCacheUpdateCallback;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.InterfaceToNodeCacheDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * InterfaceToNodeCache publisher class. It will trigger by InterfaceToNodeCacheDaoImpl reload or update
 */
public class InterfaceToNodePublisher implements InterfaceToNodeCacheUpdateCallback {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceToNodePublisher.class);

    private IpInterfaceDao ipInterfaceDao;

    private NodeDao nodeDao;

    private InterfaceToNodeCache cache;

    private TwinPublisher publisher;

    private MetricRegistry metricRegistry;

    private final Map<String, TwinPublisher.Session<InterfaceToNodeInfo>> locationEntriesSessionMap;
    private final Timer nodeLoadTimer;

    public InterfaceToNodePublisher(InterfaceToNodeCache cache, TwinPublisher publisher, MetricRegistry metricRegistry,
                                    NodeDao nodeDao, IpInterfaceDao ipInterfaceDao) {
        this.cache = Objects.requireNonNull(cache);
        this.publisher = Objects.requireNonNull(publisher);
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);

        this.locationEntriesSessionMap = new HashMap<>();
        this.nodeLoadTimer = metricRegistry.timer("nodeLoadTime");
        cache.setUpdateCallback(this);
    }

    private InterfaceToNodeInfo buildCache(String location, List<Entry> entries) {
        try (Timer.Context ctx = this.nodeLoadTimer.time()) {
            Map<Integer, Entry> entriesMap = new HashMap<>(entries.size());
            for (var entry : entries) {
                if (location == null || entry.location.equals(location)) {
                    entriesMap.put(entry.nodeId, entry);
                }
            }
            InterfaceToNodeInfo info = new InterfaceToNodeInfo();
            info.setEntryMap(entriesMap);
            info.setNetworkMetaToEntry(buildInterfaceMeta());
            info.setNodeMetaToEntry(buildNodeMeta());
            return info;
        }
    }

    private void publishUpdateForLocation(String location, InterfaceToNodeInfo info) {
        try {
            LOG.debug("Try to public InterfaceToNodePublisher location: {}", location);
            var session = locationEntriesSessionMap.get(location);
            if (session == null) {
                session = this.publisher.register(InterfaceToNodeInfo.FULL_KEY, new TypeToken<>() {
                }, location);
                locationEntriesSessionMap.put(location, session);
            }
            session.publish(info);
            LOG.debug("Done public InterfaceToNodePublisher location: {}, size: {}", location, info.getEntryMap().size());
        } catch (IOException e) {
            LOG.error("Fail to publish data location: {} size: {} error: {}", location, info.getEntryMap().size(), e.getMessage());
        }
    }

    @Override
    public void fullUpdate(List<Entry> entries) {
        // TODO: Freddy split entries by location
        InterfaceToNodeInfo info = this.buildCache(null, entries);
        this.publishUpdateForLocation(null, info);
    }

    private Map<InterfaceToNodeInfo.NodeMetadataKey, Integer> buildInterfaceMeta() {
        Map<InterfaceToNodeInfo.NodeMetadataKey, Integer> map = new HashMap<>();
        ipInterfaceDao.findAll().stream().forEach(i -> {
            for (var meta : i.getMetaData()) {
                map.put(new InterfaceToNodeInfo.NodeMetadataKey(new ContextKey(meta.getContext(), meta.getKey()), meta.getValue()), i.getNodeId());
            }
        });
        return map;
    }

    private Map<InterfaceToNodeInfo.NodeMetadataKey, Integer> buildNodeMeta() {
        Map<InterfaceToNodeInfo.NodeMetadataKey, Integer> map = new HashMap<>();
        nodeDao.findAll().stream().forEach(i -> {
            for (var meta : i.getMetaData()) {
                map.put(new InterfaceToNodeInfo.NodeMetadataKey(new ContextKey(meta.getContext(), meta.getKey()), meta.getValue()), i.getId());
            }
        });
        return map;
    }
}
