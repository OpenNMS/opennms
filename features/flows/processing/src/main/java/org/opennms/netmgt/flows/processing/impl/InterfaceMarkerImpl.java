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

package org.opennms.netmgt.flows.processing.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.opennms.netmgt.flows.api.Flow.Direction;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class InterfaceMarkerImpl {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceMarkerImpl.class);

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("initialize-marker-cache")
            .build();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

    private final SessionUtils sessionUtils;

    private final NodeDao nodeDao;

    private final SnmpInterfaceDao snmpInterfaceDao;

    private final CountDownLatch markerCacheSyncDone = new CountDownLatch(1);

    /**
     * Cache for marking nodes and interfaces as having flows.
     *
     * This maps a node ID to a set if snmpInterface IDs.
     */
    private final Map<Direction, Cache<Integer, Set<Integer>>> markerCache = Maps.newEnumMap(Direction.class);

    public InterfaceMarkerImpl(final SessionUtils sessionUtils,
                               final NodeDao nodeDao,
                               final SnmpInterfaceDao snmpInterfaceDao) {
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);

        this.markerCache.put(Direction.INGRESS, CacheBuilder.newBuilder()
                                                            .expireAfterWrite(1, TimeUnit.HOURS)
                                                            .build());

        this.markerCache.put(Direction.EGRESS, CacheBuilder.newBuilder()
                                                           .expireAfterWrite(1, TimeUnit.HOURS)
                                                           .build());

        this.executorService.execute(this::initializeMarkerCache);
    }

    private void initializeMarkerCache() {
        this.sessionUtils.withTransaction(() -> {
            for (final OnmsNode node : this.nodeDao.findAllHavingIngressFlows()) {
                this.markerCache.get(Direction.INGRESS).put(node.getId(),
                                                            this.snmpInterfaceDao.findAllHavingIngressFlows(node.getId()).stream()
                                                                                 .map(OnmsSnmpInterface::getIfIndex)
                                                                                 .collect(Collectors.toCollection(Sets::newConcurrentHashSet)));
            }

            for (final OnmsNode node : this.nodeDao.findAllHavingEgressFlows()) {
                this.markerCache.get(Direction.EGRESS).put(node.getId(),
                                                           this.snmpInterfaceDao.findAllHavingEgressFlows(node.getId()).stream()
                                                                                .map(OnmsSnmpInterface::getIfIndex)
                                                                                .collect(Collectors.toCollection(Sets::newConcurrentHashSet)));
            }
            this.markerCacheSyncDone.countDown();
            return null;
        });
    }

    public void mark(final List<EnrichedFlow> flows) {
        // Wait for the cache to be synced
        try {
            this.markerCacheSyncDone.await();
        } catch (InterruptedException e) {
            LOG.warn("Marker Cache sync wait was interrupted", e);
        }

        final Map<Direction, List<Integer>> nodesToUpdate = Maps.newEnumMap(Direction.class);
        final Map<Direction, Map<Integer, List<Integer>>> interfacesToUpdate = Maps.newEnumMap(Direction.class);

        nodesToUpdate.put(Direction.INGRESS, Lists.newArrayListWithExpectedSize(flows.size()));
        nodesToUpdate.put(Direction.EGRESS, Lists.newArrayListWithExpectedSize(flows.size()));
        interfacesToUpdate.put(Direction.INGRESS, Maps.newHashMap());
        interfacesToUpdate.put(Direction.EGRESS, Maps.newHashMap());

        for (final EnrichedFlow flow : flows) {
            if (flow.getExporterNodeInfo() == null) continue;
            if (flow.getExporterNodeInfo().getNodeId() == null) continue;

            final Integer nodeId = flow.getExporterNodeInfo().getNodeId();

            if (flow.getInputSnmp() != null &&
                flow.getInputSnmp() != 0 &&
                (flow.getDirection() == Direction.INGRESS || flow.getDirection() == Direction.UNKNOWN)) {

                Set<Integer> ingressMarkerCache = this.markerCache.get(Direction.INGRESS).getIfPresent(nodeId);
                if (ingressMarkerCache == null) {
                    this.markerCache.get(Direction.INGRESS).put(nodeId, ingressMarkerCache = Sets.newConcurrentHashSet());
                    nodesToUpdate.get(Direction.INGRESS).add(nodeId);
                }

                if (!ingressMarkerCache.contains(flow.getInputSnmp())) {
                    ingressMarkerCache.add(flow.getInputSnmp());
                    interfacesToUpdate.get(Direction.INGRESS).computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getInputSnmp());
                }
            }

            if (flow.getOutputSnmp() != null &&
                flow.getOutputSnmp() != 0 &&
                (flow.getDirection() == Direction.EGRESS || flow.getDirection() == Direction.UNKNOWN)) {

                Set<Integer> egressMarkerCache = this.markerCache.get(Direction.EGRESS).getIfPresent(nodeId);
                if (egressMarkerCache == null) {
                    this.markerCache.get(Direction.EGRESS).put(nodeId, egressMarkerCache = Sets.newConcurrentHashSet());
                    nodesToUpdate.get(Direction.EGRESS).add(nodeId);
                }

                if (!egressMarkerCache.contains(flow.getOutputSnmp())) {
                    egressMarkerCache.add(flow.getOutputSnmp());
                    interfacesToUpdate.get(Direction.EGRESS).computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getOutputSnmp());
                }
            }
        }

        if (!nodesToUpdate.get(Direction.INGRESS).isEmpty() ||
            !interfacesToUpdate.get(Direction.INGRESS).isEmpty() ||
            !nodesToUpdate.get(Direction.EGRESS).isEmpty() ||
            !interfacesToUpdate.get(Direction.EGRESS).isEmpty()) {
            this.sessionUtils.withTransaction(() -> {
                if (!nodesToUpdate.get(Direction.INGRESS).isEmpty() || !nodesToUpdate.get(Direction.EGRESS).isEmpty()) {
                    this.nodeDao.markHavingFlows(nodesToUpdate.get(Direction.INGRESS), nodesToUpdate.get(Direction.EGRESS));
                }

                for (final Map.Entry<Integer, List<Integer>> e : interfacesToUpdate.get(Direction.INGRESS).entrySet()) {
                    this.snmpInterfaceDao.markHavingIngressFlows(e.getKey(), e.getValue());
                }

                for (final Map.Entry<Integer, List<Integer>> e : interfacesToUpdate.get(Direction.EGRESS).entrySet()) {
                    this.snmpInterfaceDao.markHavingEgressFlows(e.getKey(), e.getValue());
                }
                return null;
            });
        }
    }

    public void stop() throws FlowException {
        this.markerCacheSyncDone.countDown();
        this.executorService.shutdownNow();
    }
}
