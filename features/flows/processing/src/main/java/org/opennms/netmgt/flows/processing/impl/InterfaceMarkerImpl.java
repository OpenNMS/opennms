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
package org.opennms.netmgt.flows.processing.impl;

import static org.opennms.integration.api.v1.flows.Flow.Direction;

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

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.integration.api.v1.flows.FlowException;
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
