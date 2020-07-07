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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.EnrichedFlowForwarder;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.elastic.agg.AggregatedFlowQueryService;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.swrve.ratelimitedlogger.RateLimitedLog;

import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;

public class ElasticFlowRepository implements FlowRepository {

    public static final String TRACER_FLOW_MODULE = "ElasticFlow";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

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

    private final SessionUtils sessionUtils;

    private final NodeDao nodeDao;
    private final SnmpInterfaceDao snmpInterfaceDao;

    // An OpenNMS or Sentinel Identity.
    private final Identity identity;
    private final TracerRegistry tracerRegistry;

    private final IndexSettings indexSettings;

    private final SmartQueryService smartQueryService;

    private final EnrichedFlowForwarder enrichedFlowForwarder;

    private boolean enableFlowForwarding = false;

    private int bulkRetryCount = 1;

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

    public ElasticFlowRepository(MetricRegistry metricRegistry, JestClient jestClient, IndexStrategy indexStrategy,
                                 DocumentEnricher documentEnricher,
                                 SessionUtils sessionUtils, NodeDao nodeDao, SnmpInterfaceDao snmpInterfaceDao,
                                 Identity identity, TracerRegistry tracerRegistry, EnrichedFlowForwarder enrichedFlowForwarder,
                                 IndexSettings indexSettings,
                                 SmartQueryService smartQueryService) {
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
        this.smartQueryService = Objects.requireNonNull(smartQueryService);

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
            return null;
        });
    }

    @Override
    public void persist(final Collection<Flow> flows, final FlowSource source) throws FlowException {
        // Track the number of flows per call
        flowsPerLog.update(flows.size());
        if (flows.isEmpty()) {
            LOG.info("Received empty flows from {} @ {}. Nothing to do.", source.getSourceAddress(), source.getLocation());
            return;
        }

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
            RATE_LIMITED_LOGGER.error("Flow persistence disabled. Dropping {} flow documents.", flowDocuments.size());
            return;
        }

        LOG.debug("Persisting {} flow documents.", flowDocuments.size());
        final Tracer tracer = getTracer();
        try (final Timer.Context ctx = logPersistingTimer.time();
             Scope scope = tracer.buildSpan(TRACER_FLOW_MODULE).startActive(true)) {
            // Add location and source address tags to span.
            scope.span().setTag(TracerConstants.TAG_LOCATION, source.getLocation());
            scope.span().setTag(TracerConstants.TAG_SOURCE_ADDRESS, source.getSourceAddress());
            scope.span().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            final BulkRequest<FlowDocument> bulkRequest = new BulkRequest<>(client, flowDocuments, (documents) -> {
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
                throw new PersistenceException(ex.getMessage(), ex.getBulkResult().getFailedDocuments());
            } catch (IOException ex) {
                LOG.error("An error occurred while executing the given request: {}", ex.getMessage(), ex);
                throw new FlowException(ex.getMessage(), ex);
            }
            flowsPersistedMeter.mark(flowDocuments.size());
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
                    !ifaceMarkerCache.contains(flow.getInputSnmp())) {
                    ifaceMarkerCache.add(flow.getInputSnmp());
                    interfacesToUpdate.get(flow.getDirection()).computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getInputSnmp());
                }
                if (flow.getOutputSnmp() != null &&
                    flow.getOutputSnmp() != 0 &&
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

    @Override
    public CompletableFuture<Long> getFlowCount(List<Filter> filters) {
        return smartQueryService.getFlowCount(filters);
    }

    @Override
    public CompletableFuture<List<String>> getApplications(String matchingPrefix, long limit, List<Filter> filters) {
        return smartQueryService.getApplications(matchingPrefix, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(int N, boolean includeOther,
                                                                                       List<Filter> filters) {
        return smartQueryService.getTopNApplicationSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(Set<String> applications,
                                                                                   boolean includeOther,
                                                                                   List<Filter> filters) {
        return smartQueryService.getApplicationSummaries(applications, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(Set<String> applications,
                                                                                            long step,
                                                                                            boolean includeOther,
                                                                                            List<Filter> filters) {
        return smartQueryService.getApplicationSeries(applications, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(int N, long step,
                                                                                                boolean includeOther,
                                                                                                List<Filter> filters) {
        return smartQueryService.getTopNApplicationSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getConversations(String locationPattern, String protocolPattern,
                                                            String lowerIPPattern, String upperIPPattern,
                                                            String applicationPattern, long limit,
                                                            List<Filter> filters) {
        return smartQueryService.getConversations(locationPattern, protocolPattern,
                lowerIPPattern, upperIPPattern,
                applicationPattern, limit,
                filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(int N,
                                                                                              boolean includeOther,
                                                                                              List<Filter> filters) {
        return smartQueryService.getTopNConversationSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(Set<String> conversations,
                                                                                          boolean includeOther,
                                                                                          List<Filter> filters) {
        return smartQueryService.getConversationSummaries(conversations, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(Set<String> conversations, long step, boolean includeOther, List<Filter> filters) {
        return smartQueryService.getConversationSeries(conversations, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(int N,
                                                                                                          long step,
                                                                                                          boolean includeOther,
                                                                                                          List<Filter> filters) {
        return smartQueryService.getTopNConversationSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getHosts(String regex, long limit, List<Filter> filters) {
        return smartQueryService.getHosts(regex, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(int N, boolean includeOther,
                                                                              List<Filter> filters) {
        return smartQueryService.getTopNHostSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(Set<String> hosts,
                                                                            boolean includeOther,
                                                                            List<Filter> filters) {
        return smartQueryService.getHostSummaries(hosts, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(Set<String> hosts, long step,
                                                                                     boolean includeOther,
                                                                                     List<Filter> filters) {
        return smartQueryService.getHostSeries(hosts, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(int N, long step,
                                                                                         boolean includeOther,
                                                                                         List<Filter> filters) {
        return smartQueryService.getTopNHostSeries(N, step, includeOther, filters);
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

    public int getBulkRetryCount() {
        return bulkRetryCount;
    }

    public void setBulkRetryCount(int bulkRetryCount) {
        this.bulkRetryCount = bulkRetryCount;
    }

    public boolean isSkipElasticsearchPersistence() {
        return skipElasticsearchPersistence;
    }

    public void setSkipElasticsearchPersistence(boolean skipElasticsearchPersistence) {
        this.skipElasticsearchPersistence = skipElasticsearchPersistence;
    }
}
