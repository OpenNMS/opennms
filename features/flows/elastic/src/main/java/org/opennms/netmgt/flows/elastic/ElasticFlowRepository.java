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

package org.opennms.netmgt.flows.elastic;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.swrve.ratelimitedlogger.RateLimitedLog;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.Identity;
import org.opennms.features.jest.client.bulk.BulkException;
import org.opennms.features.jest.client.bulk.BulkRequest;
import org.opennms.features.jest.client.bulk.BulkWrapper;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.EnrichedFlowForwarder;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ElasticFlowRepository implements FlowRepository {

    public static final String TRACER_FLOW_MODULE = "ElasticFlow";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("initialize-marker-cache")
            .build();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

    private final CountDownLatch markerCacheSyncDone = new CountDownLatch(1);

    private static final String INDEX_NAME = "netflow";

    private final JestClient client;

    private final IndexStrategy indexStrategy;

    private final DocumentEnricher documentEnricher;

    /**
     * Flows/second throughput
     */
    private final Meter flowsPersistedMeter;

    /**
     * Time taken to enrich the flows in a log
     */
    private final Timer logEnrichementTimer;

    /**
     * Time taken to persist the flows in a log
     */
    private final Timer logPersistingTimer;

    /**
     * Time taken to mark the flows in a log
     */
    private final Timer logMarkingTimer;

    /**
     * Number of flows in a log
     */
    private final Histogram flowsPerLog;

    private final Counter emptyFlows;

    private final SessionUtils sessionUtils;

    private final NodeDao nodeDao;
    private final SnmpInterfaceDao snmpInterfaceDao;

    // An OpenNMS or Sentinel Identity.
    private final Identity identity;
    private final TracerRegistry tracerRegistry;

    private final IndexSettings indexSettings;

    private final EnrichedFlowForwarder enrichedFlowForwarder;

    private boolean enableFlowForwarding = false;

    private int bulkSize = 1000;
    private int bulkRetryCount = 5;
    private int bulkFlushMs = 500;

    /**
     * Can be used to skip persisting the flows into ES>
     */
    private boolean skipElasticsearchPersistence = false;

    /**
     * Cache for marking nodes and interfaces as having flows.
     *
     * This maps a node ID to a set if snmpInterface IDs.
     */
    private final Map<Direction, Cache<Integer, Set<Integer>>> markerCache = Maps.newEnumMap(Direction.class);
    
    private class FlowBulk {
        private List<FlowDocument> documents = Lists.newArrayListWithCapacity(ElasticFlowRepository.this.bulkSize);
        private ReentrantLock lock = new ReentrantLock();
        private long lastPersist = 0;

        public FlowBulk() {
        }
    }

    /**
     * Collect flow documents ready for persistence.
     */
    private final Map<Thread, FlowBulk> flowBulks = Maps.newConcurrentMap();
    private java.util.Timer flushTimer;

    public ElasticFlowRepository(MetricRegistry metricRegistry, JestClient jestClient, IndexStrategy indexStrategy,
                                 DocumentEnricher documentEnricher,
                                 SessionUtils sessionUtils, NodeDao nodeDao, SnmpInterfaceDao snmpInterfaceDao,
                                 Identity identity, TracerRegistry tracerRegistry, EnrichedFlowForwarder enrichedFlowForwarder,
                                 IndexSettings indexSettings) {
        this.client = Objects.requireNonNull(jestClient);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.documentEnricher = Objects.requireNonNull(documentEnricher);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.identity = identity;
        this.tracerRegistry = tracerRegistry;
        this.enrichedFlowForwarder = enrichedFlowForwarder;
        this.indexSettings = Objects.requireNonNull(indexSettings);

        this.emptyFlows = metricRegistry.counter("emptyFlows");
        flowsPersistedMeter = metricRegistry.meter("flowsPersisted");
        logEnrichementTimer = metricRegistry.timer("logEnrichment");
        logPersistingTimer = metricRegistry.timer("logPersisting");
        logMarkingTimer = metricRegistry.timer("logMarking");
        flowsPerLog = metricRegistry.histogram("flowsPerLog");

        this.markerCache.put(Direction.INGRESS, CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());

        this.markerCache.put(Direction.EGRESS, CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build());

        this.startTimer();

        executorService.execute(this::initializeMarkerCache);
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
            markerCacheSyncDone.countDown();
            return null;
        });
    }

    private void waitForMarkerCacheSync() {
        try {
            markerCacheSyncDone.await();
        } catch (InterruptedException e) {
            LOG.warn("Marker Cache sync wait was interrupted", e);
        }
    }

    public ElasticFlowRepository(final MetricRegistry metricRegistry, final JestClient jestClient, final IndexStrategy indexStrategy,
                                 final DocumentEnricher documentEnricher, final SessionUtils sessionUtils, final NodeDao nodeDao,
                                 final SnmpInterfaceDao snmpInterfaceDao, final Identity identity, final TracerRegistry tracerRegistry,
                                 final EnrichedFlowForwarder enrichedFlowForwarder, final IndexSettings indexSettings, final int bulkSize,
                                 final int bulkFlushMs) {
        this(metricRegistry, jestClient, indexStrategy, documentEnricher, sessionUtils, nodeDao, snmpInterfaceDao, identity, tracerRegistry, enrichedFlowForwarder, indexSettings);
        this.bulkSize = bulkSize;
        this.bulkFlushMs = bulkFlushMs;
    }

    private void startTimer() {
        if (flushTimer != null) {
            return;
        }

        if (bulkFlushMs > 0) {
            int delay = Math.max(1, bulkFlushMs / 2);
            flushTimer = new java.util.Timer("ElasticFlowRepositoryFlush");
            flushTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    final long currentTimeMillis = System.currentTimeMillis();
                    for(final Map.Entry<Thread, ElasticFlowRepository.FlowBulk> entry : flowBulks.entrySet()) {
                        final ElasticFlowRepository.FlowBulk flowBulk = entry.getValue();
                        if (currentTimeMillis - flowBulk.lastPersist > bulkFlushMs) {
                            if (flowBulk.lock.tryLock()) {
                                try {
                                    if (flowBulk.documents.size() > 0) {
                                        try {
                                            persistBulk(flowBulk.documents);
                                            flowBulk.lastPersist = currentTimeMillis;
                                        } catch (Throwable t) {
                                            LOG.error("An error occurred while flushing one or more bulks in ElasticFlowRepository.", t);
                                        }
                                    }
                                } finally {
                                    flowBulk.lock.unlock();
                                }
                            }
                        }
                    }
                }
            }, delay, delay);
        } else {
            flushTimer = null;
        }
    }

    private void stopTimer() {
        if (flushTimer != null) {
            flushTimer.cancel();
            flushTimer = null;
        }
    }

    @Override
    public void persist(final Collection<Flow> flows, final FlowSource source) throws FlowException {
        // Track the number of flows per call
        flowsPerLog.update(flows.size());
        if (flows.isEmpty()) {
            this.emptyFlows.inc();
            LOG.info("Received empty flows from {} @ {}. Nothing to do.", source.getSourceAddress(), source.getLocation());
            return;
        }
        waitForMarkerCacheSync();
        LOG.debug("Enriching {} flow documents.", flows.size());
        final List<FlowDocument> flowDocuments;
        try (final Timer.Context ctx = logEnrichementTimer.time()) {
            flowDocuments = documentEnricher.enrich(flows, source);
        } catch (Exception e) {
            throw new FlowException("Failed to enrich one or more flows.", e);
        }

        if(enableFlowForwarding) {
            LOG.debug("Forwarding {} flow documents.", flowDocuments.size());
            flowDocuments.stream().map(FlowDocument::buildEnrichedFlow).forEach(enrichedFlowForwarder::forward);
        }

        if (skipElasticsearchPersistence) {
            RATE_LIMITED_LOGGER.info("Flow persistence disabled. Dropping {} flow documents.", flowDocuments.size());
        } else {
            final FlowBulk flowBulk = this.flowBulks.computeIfAbsent(Thread.currentThread(), (thread) -> new FlowBulk());
            flowBulk.lock.lock();
            try {
                flowBulk.documents.addAll(flowDocuments);
                if (flowBulk.documents.size() >= this.bulkSize) {
                    this.persistBulk(flowBulk.documents);
                    flowBulk.lastPersist = System.currentTimeMillis();
                }
            } finally {
                flowBulk.lock.unlock();
            }
        }

        // Mark nodes and interfaces as having associated flows
        try (final Timer.Context ctx = logMarkingTimer.time()) {
            final Map<Direction, List<Integer>> nodesToUpdate = Maps.newEnumMap(Direction.class);
            final Map<Direction, Map<Integer, List<Integer>>> interfacesToUpdate = Maps.newEnumMap(Direction.class);

            nodesToUpdate.put(Direction.INGRESS, Lists.newArrayListWithExpectedSize(flowDocuments.size()));
            nodesToUpdate.put(Direction.EGRESS, Lists.newArrayListWithExpectedSize(flowDocuments.size()));
            interfacesToUpdate.put(Direction.INGRESS, Maps.newHashMap());
            interfacesToUpdate.put(Direction.EGRESS, Maps.newHashMap());

            for (final FlowDocument flow : flowDocuments) {
                if (flow.getNodeExporter() == null) continue;
                if (flow.getNodeExporter().getNodeId() == null) continue;

                final Integer nodeId = flow.getNodeExporter().getNodeId();

                Set<Integer> ifaceMarkerCache = this.markerCache.get(flow.getDirection()).getIfPresent(nodeId);

                if (ifaceMarkerCache == null) {
                    this.markerCache.get(flow.getDirection()).put(nodeId, ifaceMarkerCache = Sets.newConcurrentHashSet());
                    nodesToUpdate.get(flow.getDirection()).add(nodeId);
                }

                if (flow.getInputSnmp() != null &&
                    flow.getInputSnmp() != 0 &&
                    flow.getDirection() == Direction.INGRESS &&
                    !ifaceMarkerCache.contains(flow.getInputSnmp())) {
                    ifaceMarkerCache.add(flow.getInputSnmp());
                    interfacesToUpdate.get(flow.getDirection()).computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getInputSnmp());
                }
                if (flow.getOutputSnmp() != null &&
                    flow.getOutputSnmp() != 0 &&
                    flow.getDirection() == Direction.EGRESS &&
                    !ifaceMarkerCache.contains(flow.getOutputSnmp())) {
                    ifaceMarkerCache.add(flow.getOutputSnmp());
                    interfacesToUpdate.get(flow.getDirection()).computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getOutputSnmp());
                }
            }

            if (!nodesToUpdate.get(Direction.INGRESS).isEmpty() ||
                !interfacesToUpdate.get(Direction.INGRESS).isEmpty() ||
                !nodesToUpdate.get(Direction.EGRESS).isEmpty() ||
                !interfacesToUpdate.get(Direction.EGRESS).isEmpty()) {
                sessionUtils.withTransaction(() -> {
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
    }

    private void persistBulk(final List<FlowDocument> bulk) throws FlowException {
        LOG.debug("Persisting {} flow documents.", bulk.size());
        final Tracer tracer = getTracer();
        try (final Timer.Context ctx = logPersistingTimer.time();
             Scope scope = tracer.buildSpan(TRACER_FLOW_MODULE).startActive(true)) {
            // Add location and source address tags to span.
            scope.span().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            final BulkRequest<FlowDocument> bulkRequest = new BulkRequest<>(client, bulk, (documents) -> {
                final Bulk.Builder bulkBuilder = new Bulk.Builder();
                for (FlowDocument flowDocument : documents) {
                    final String index = indexStrategy.getIndex(indexSettings, INDEX_NAME, Instant.ofEpochMilli(flowDocument.getTimestamp()));
                    final Index.Builder indexBuilder = new Index.Builder(flowDocument)
                            .index(index);
                    bulkBuilder.addAction(indexBuilder.build());
                }
                return new BulkWrapper(bulkBuilder);
            }, bulkRetryCount);
            try {
                // the bulk request considers retries
                bulkRequest.execute();
            } catch (BulkException ex) {
                if (ex.getBulkResult() != null) {
                    throw new PersistenceException(ex.getMessage(), ex.getBulkResult().getFailedDocuments());
                } else {
                    throw new PersistenceException(ex.getMessage(), Collections.emptyList());
                }
            } catch (IOException ex) {
                LOG.error("An error occurred while executing the given request: {}", ex.getMessage(), ex);
                throw new FlowException(ex.getMessage(), ex);
            }
            flowsPersistedMeter.mark(bulk.size());

            bulk.clear();
        }
    }

    public Identity getIdentity() {
        return identity;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void start() {
        if (tracerRegistry != null && identity != null) {
            tracerRegistry.init(identity.getId());
        }

        startTimer();
    }

    public void stop() throws FlowException {
        stopTimer();
        markerCacheSyncDone.countDown();
        executorService.shutdownNow();
        for(final FlowBulk flowBulk : flowBulks.values()) {
            persistBulk(flowBulk.documents);
        }
    }

    private Tracer getTracer() {
        if (tracerRegistry != null) {
            return tracerRegistry.getTracer();
        }
        return GlobalTracer.get();
    }

    public boolean isEnableFlowForwarding() {
        return enableFlowForwarding;
    }

    public void setEnableFlowForwarding(boolean enableFlowForwarding) {
        this.enableFlowForwarding = enableFlowForwarding;
    }

    public int getBulkSize() {
        return this.bulkSize;
    }

    public void setBulkSize(final int bulkSize) {
        this.bulkSize = bulkSize;
    }

    public int getBulkRetryCount() {
        return bulkRetryCount;
    }

    public void setBulkRetryCount(int bulkRetryCount) {
        this.bulkRetryCount = bulkRetryCount;
    }

    public int getBulkFlushMs() {
        return bulkFlushMs;
    }

    public void setBulkFlushMs(final int bulkFlushMs) {
        this.bulkFlushMs = bulkFlushMs;

        stopTimer();
        startTimer();
    }
    public boolean isSkipElasticsearchPersistence() {
        return skipElasticsearchPersistence;
    }

    public void setSkipElasticsearchPersistence(boolean skipElasticsearchPersistence) {
        this.skipElasticsearchPersistence = skipElasticsearchPersistence;
    }
}
