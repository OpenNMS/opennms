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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.elastic.index.IndexSelector;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkException;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkRequest;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkWrapper;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

public class ElasticFlowRepository implements FlowRepository {

    public static final String OTHER_APPLICATION_NAME = "Other";
    public static final String UNKNOWN_APPLICATION_NAME = "Unknown";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private static final String TYPE = "netflow";

    private final JestClient client;

    private final IndexStrategy indexStrategy;

    private final DocumentEnricher documentEnricher;

    private final SearchQueryProvider searchQueryProvider = new SearchQueryProvider();

    private final ClassificationEngine classificationEngine;

    private final int bulkRetryCount;

    /**
     * Flows/second throughput
     */
    private final Meter flowsPersistedMeter;

    /**
     * Time taken to convert the flows in a log
     */
    private final Timer logConversionTimer;

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

    private final IndexSelector indexSelector;

    private final TransactionOperations transactionOperations;

    private final NodeDao nodeDao;
    private final SnmpInterfaceDao snmpInterfaceDao;

    /**
     * Cache for marking nodes and interfaces as having flows.
     *
     * This maps a node ID to a set if snmpInterface IDs.
     */
    private final ConcurrentMap<Integer, Set<Integer>> markerCache = Maps.newConcurrentMap();

    public ElasticFlowRepository(MetricRegistry metricRegistry, JestClient jestClient, IndexStrategy indexStrategy,
                                 DocumentEnricher documentEnricher, ClassificationEngine classificationEngine,
                                 TransactionOperations transactionOperations, NodeDao nodeDao, SnmpInterfaceDao snmpInterfaceDao,
                                 int bulkRetryCount, long maxFlowDurationMs) {
        this.client = Objects.requireNonNull(jestClient);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.documentEnricher = Objects.requireNonNull(documentEnricher);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.bulkRetryCount = bulkRetryCount;
        this.indexSelector = new IndexSelector(TYPE, indexStrategy, maxFlowDurationMs);

        flowsPersistedMeter = metricRegistry.meter("flowsPersisted");
        logConversionTimer = metricRegistry.timer("logConversion");
        logEnrichementTimer = metricRegistry.timer("logEnrichment");
        logPersistingTimer = metricRegistry.timer("logPersisting");
        logMarkingTimer = metricRegistry.timer("logMarking");
        flowsPerLog = metricRegistry.histogram("flowsPerLog");

        // Pre-populate marker cache with values from DB
        this.transactionOperations.execute(cb -> {
            for (final OnmsNode node : this.nodeDao.findAllHavingFlows()) {
                this.markerCache.put(node.getId(),
                        this.snmpInterfaceDao.findAllHavingFlows(node.getId()).stream()
                                .map(OnmsSnmpInterface::getIfIndex)
                                .collect(Collectors.toCollection(Sets::newConcurrentHashSet)));
            }
            return null;
        });
    }

    @Override
    public void persist(final Collection<Flow> flows, final FlowSource source) throws FlowException {
        LOG.debug("Converting {} flows from {} to flow documents.", flows.size(), source);
        final List<FlowDocument> documents;
        try (final Timer.Context ctx = logConversionTimer.time()) {
            documents = flows.stream()
                    .map(FlowDocument::from)
                    .collect(Collectors.toList());
        }
        enrichAndPersistFlows(documents, source);
    }

    public void enrichAndPersistFlows(final List<FlowDocument> flowDocuments, FlowSource source) throws FlowException {
        // Track the number of flows per call
        flowsPerLog.update(flowDocuments.size());

        if (flowDocuments.isEmpty()) {
            LOG.info("Received empty flows. Nothing to do.");
            return;
        }

        LOG.debug("Enriching {} flow documents.", flowDocuments.size());
        try (final Timer.Context ctx = logEnrichementTimer.time()) {
            documentEnricher.enrich(flowDocuments, source);
        }

        LOG.debug("Persisting {} flow documents.", flowDocuments.size());
        try (final Timer.Context ctx = logPersistingTimer.time()) {
            final BulkRequest<FlowDocument> bulkRequest = new BulkRequest<>(client, flowDocuments, (documents) -> {
                final Bulk.Builder bulkBuilder = new Bulk.Builder();
                for (FlowDocument flowDocument : documents) {
                   final String index = indexStrategy.getIndex(TYPE, Instant.ofEpochMilli(flowDocument.getTimestamp()));
                   final Index.Builder indexBuilder = new Index.Builder(flowDocument)
                        .index(index)
                        .type(TYPE);
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
            final List<Integer> nodesToUpdate = Lists.newArrayListWithExpectedSize(flowDocuments.size());
            final Map<Integer, List<Integer>> interfacesToUpdate = Maps.newHashMap();

            for (final FlowDocument flow : flowDocuments) {
                if (flow.getNodeExporter() == null) continue;
                if (flow.getNodeExporter().getNodeId() == null) continue;

                final Integer nodeId = flow.getNodeExporter().getNodeId();

                Set<Integer> ifaceMarkerCache = this.markerCache.get(nodeId);
                if (ifaceMarkerCache == null) {
                    this.markerCache.put(nodeId, ifaceMarkerCache = Sets.newConcurrentHashSet());
                    nodesToUpdate.add(nodeId);
                }

                if (flow.getInputSnmp() != null &&
                    flow.getInputSnmp() != 0 &&
                    !ifaceMarkerCache.contains(flow.getInputSnmp())) {
                    ifaceMarkerCache.add(flow.getInputSnmp());
                    interfacesToUpdate.computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getInputSnmp());
                }
                if (flow.getOutputSnmp() != null &&
                    flow.getOutputSnmp() != 0 &&
                    !ifaceMarkerCache.contains(flow.getOutputSnmp())) {
                    ifaceMarkerCache.add(flow.getOutputSnmp());
                    interfacesToUpdate.computeIfAbsent(nodeId, k -> Lists.newArrayList()).add(flow.getOutputSnmp());
                }
            }

            if (!nodesToUpdate.isEmpty() || !interfacesToUpdate.isEmpty()) {
                this.transactionOperations.execute(cb -> {
                    if (!nodesToUpdate.isEmpty()) {
                        this.nodeDao.markHavingFlows(nodesToUpdate);
                    }
                    for (final Map.Entry<Integer, List<Integer>> e : interfacesToUpdate.entrySet()) {
                        this.snmpInterfaceDao.markHavingFlows(e.getKey(), e.getValue());
                    }
                    return null;
                });
            }
        }
    }

    @Override
    public CompletableFuture<Long> getFlowCount(List<Filter> filters) {
        final String query = searchQueryProvider.getFlowCountQuery(filters);
        return searchAsync(query, extractTimeRangeFilter(filters)).thenApply(SearchResult::getTotal);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplications(int N, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFromTopN(N, "netflow.application", UNKNOWN_APPLICATION_NAME, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationsSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFromTopN(N, step, "netflow.application", UNKNOWN_APPLICATION_NAME, includeOther, filters).thenApply((res) -> mapTable(res, s -> s));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversations(int N, List<Filter> filters) {
        return getTotalBytesFromTopN(N, "netflow.convo_key", null, false, filters).thenApply((res) -> res.stream()
                .map(summary -> {
                    final ConversationKey convo = ConversationKeyUtils.fromJsonString(summary.getEntity());
                    final Conversation conversation = new Conversation(convo, classify(convo));
                    final TrafficSummary<Conversation> out = new TrafficSummary<>(conversation);
                    out.setBytesIn(summary.getBytesIn());
                    out.setBytesOut(summary.getBytesOut());
                    return out;
                })
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationsSeries(int N, long step, List<Filter> filters) {
        return getSeriesFromTopN(N, step, "netflow.convo_key", null, false, filters).thenApply((res) -> mapTable(res, (key) -> {
            final ConversationKey convo = ConversationKeyUtils.fromJsonString(key);
            final String application = classify(convo);
            return new Conversation(convo, application);
        }));
    }

    private CompletableFuture<List<String>> getTopN(int N, String groupByTerm, String keyForMissingTerm, List<Filter> filters) {
        if (N < 1) {
            // Avoid a query and return an empty list
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        // Increase the multiplier for increased accuracy
        // See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html#_size
        final int multiplier = 2;
        final String query = searchQueryProvider.getTopNQuery(multiplier*N, groupByTerm, keyForMissingTerm, filters);
        return searchAsync(query, extractTimeRangeFilter(filters))
                .thenApply(res -> {
                    final TermsAggregation groupedBy = res.getAggregations().getTermsAggregation("grouped_by");
                    if (groupedBy == null) {
                        // No results
                        return Collections.emptyList();
                    }
                    return groupedBy.getBuckets().stream()
                            .map(TermsAggregation.Entry::getKey)
                            .limit(N)
                            .collect(Collectors.toList());
                });
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFromTopN(List<String> topN, long step, String groupByTerm,
                                                                                          String keyForMissingTerm,
                                                                                          boolean includeOther, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        final ImmutableTable.Builder<Directional<String>, Long, Double> builder = ImmutableTable.builder();
        CompletableFuture<Void> seriesFuture;
        if (topN.size() < 1) {
            // If there are no entries, skip the query
            seriesFuture = CompletableFuture.completedFuture(null);
        } else {
            final String seriesFromTopNQuery = searchQueryProvider.getSeriesFromTopNQuery(topN, step, timeRangeFilter.getStart(),
                    timeRangeFilter.getEnd(), groupByTerm, filters);
            seriesFuture = searchAsync(seriesFromTopNQuery, timeRangeFilter)
                    .thenApply(res -> {
                        toTable(builder, res);
                        return null;
                    });
        }

        final boolean missingTermIncludedInTopN = keyForMissingTerm != null && topN.contains(keyForMissingTerm);
        if (missingTermIncludedInTopN) {
            // We also need to query for items with a missing term, this will require a separate query
            final String seriesFromMissingQuery = searchQueryProvider.getSeriesFromMissingQuery(step,
                    timeRangeFilter.getStart(), timeRangeFilter.getEnd(), groupByTerm, keyForMissingTerm, filters);
            seriesFuture = seriesFuture
                    .thenCombine(searchAsync(seriesFromMissingQuery, extractTimeRangeFilter(filters)), (ignored,res) -> {
                toTable(builder, res);
                return null;
            });
        }

        if (includeOther) {
            // We also want to gather series for terms not part of the Top N
            final String seriesFromOthersQuery = searchQueryProvider.getSeriesFromOthersQuery(topN, step,
                    timeRangeFilter.getStart(), timeRangeFilter.getEnd(), groupByTerm, missingTermIncludedInTopN, filters);
            seriesFuture = seriesFuture.thenCombine(searchAsync(seriesFromOthersQuery, timeRangeFilter), (ignored,res) -> {
                final MetricAggregation aggs = res.getAggregations();
                final TermsAggregation directionAgg = aggs.getTermsAggregation("direction");
                for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
                    final boolean isIngress = isIngress(directionBucket);
                    final ProportionalSumAggregation sumAgg = directionBucket.getAggregation("bytes", ProportionalSumAggregation.class);
                    for (ProportionalSumAggregation.DateHistogram dateHistogram : sumAgg.getBuckets()) {
                        builder.put(new Directional<>(OTHER_APPLICATION_NAME, isIngress), dateHistogram.getTime(), dateHistogram.getValue());
                    }
                }
                return null;
            });
        }

        // Sort the table to ensure that the rows as in the same order as the Top N
        return seriesFuture.thenApply(ignored -> TableUtils.sortTableByRowKeys(builder.build(), topN));
    }

    private static void toTable(ImmutableTable.Builder<Directional<String>, Long, Double> builder, SearchResult res) {
        final MetricAggregation aggs = res.getAggregations();
        final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
        for (TermsAggregation.Entry bucket : groupedBy.getBuckets()) {
            final TermsAggregation directionAgg = bucket.getTermsAggregation("direction");
            for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
                final boolean isIngress = isIngress(directionBucket);
                final ProportionalSumAggregation sumAgg = directionBucket.getAggregation("bytes", ProportionalSumAggregation.class);
                for (ProportionalSumAggregation.DateHistogram dateHistogram : sumAgg.getBuckets()) {
                    builder.put(new Directional<>(bucket.getKey(), isIngress), dateHistogram.getTime(), dateHistogram.getValue());
                }
            }
        }
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFromTopN(int N, long step, String groupByTerm,
                                                                                          String keyForMissingTerm, boolean includeOther,
                                                                                          List<Filter> filters) {
        return getTopN(N, groupByTerm, keyForMissingTerm, filters)
                .thenCompose((topN) -> getSeriesFromTopN(topN, step, groupByTerm, keyForMissingTerm, includeOther, filters));
    }

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesFromTopN(List<String> topN, String groupByTerm,
                                                                                  String keyForMissingTerm,
                                                                                  boolean includeOther, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        final long start = timeRangeFilter.getStart();
        // Remove 1 from the end to make sure we have a single bucket
        final long end = Math.max(timeRangeFilter.getStart(), timeRangeFilter.getEnd() - 1);
        // A single step
        final long step = timeRangeFilter.getEnd() - timeRangeFilter.getStart();

        CompletableFuture<Map<String, TrafficSummary<String>>> summariesFuture;
        if (topN.size() < 1) {
            // If there are no entries, skip the query
            summariesFuture = CompletableFuture.completedFuture(new LinkedHashMap<>());
        } else {
            final String bytesFromTopNQuery = searchQueryProvider.getSeriesFromTopNQuery(topN, step, start, end, groupByTerm, filters);
            summariesFuture = searchAsync(bytesFromTopNQuery, timeRangeFilter).thenApply(ElasticFlowRepository::toTrafficSummaries);
        }

        final boolean missingTermIncludedInTopN = keyForMissingTerm != null && topN.contains(keyForMissingTerm);
        if (missingTermIncludedInTopN) {
            // We also need to query for items with a missing term, this will require a separate query
            final String bytesFromMissingQuery = searchQueryProvider.getSeriesFromMissingQuery(step, start, end,
                    groupByTerm, keyForMissingTerm, filters);
            summariesFuture = summariesFuture.thenCombine(searchAsync(bytesFromMissingQuery, timeRangeFilter), (summaries,results) -> {
                summaries.putAll(toTrafficSummaries(results));
                return summaries;
            });
        }

        if (includeOther) {
            // We also want to tally up traffic from other elements not part of the Top N
            final String bytesFromOthersQuery = searchQueryProvider.getSeriesFromOthersQuery(topN, step, start, end,
                    groupByTerm, missingTermIncludedInTopN, filters);
            summariesFuture = summariesFuture.thenCombine(searchAsync(bytesFromOthersQuery, timeRangeFilter), (summaries,results) -> {
                final MetricAggregation aggs = results.getAggregations();
                final TrafficSummary<String> trafficSummary = new TrafficSummary<>(OTHER_APPLICATION_NAME);
                final TermsAggregation directionAgg = aggs.getTermsAggregation("direction");
                for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
                    final boolean isIngress = isIngress(directionBucket);
                    final ProportionalSumAggregation sumAgg = directionBucket.getAggregation("bytes", ProportionalSumAggregation.class);
                    final List<ProportionalSumAggregation.DateHistogram> sumBuckets = sumAgg.getBuckets();
                    // There should only be a single bucket here
                    if (sumBuckets.size() != 1) {
                        throw new IllegalStateException("Expected 1 bucket, but got: " + sumBuckets);
                    }
                    final Double sum = sumBuckets.iterator().next().getValue();
                    if (!isIngress) {
                        trafficSummary.setBytesOut(sum.longValue());
                    } else {
                        trafficSummary.setBytesIn(sum.longValue());
                    }
                }
                summaries.put(OTHER_APPLICATION_NAME, trafficSummary);
                return summaries;
            });
        }

        return summariesFuture.thenApply(summaries -> {
            // Now build a list in the same order as the given top N list
            final List<TrafficSummary<String>> topNRes = new ArrayList<>(topN.size());
            for (String topNEntry : topN) {
                final TrafficSummary<String> summary = summaries.remove(topNEntry);
                if (summary != null) {
                    topNRes.add(summary);
                }
            }
            // Append any remaining elements
            topNRes.addAll(summaries.values());
            return topNRes;
        });
    }

    private static Map<String, TrafficSummary<String>> toTrafficSummaries(SearchResult res) {
        // Build the traffic summaries from the search results
        final Map<String, TrafficSummary<String>> summaries = new LinkedHashMap<>();
        final MetricAggregation aggs = res.getAggregations();
        final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
        for (TermsAggregation.Entry bucket : groupedBy.getBuckets()) {
            final TrafficSummary<String> trafficSummary = new TrafficSummary<>(bucket.getKey());
            final TermsAggregation directionAgg = bucket.getTermsAggregation("direction");
            for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
                final boolean isIngress = isIngress(directionBucket);
                final ProportionalSumAggregation sumAgg = directionBucket.getAggregation("bytes", ProportionalSumAggregation.class);
                final List<ProportionalSumAggregation.DateHistogram> sumBuckets = sumAgg.getBuckets();
                // There should only be a single bucket here
                if (sumBuckets.size() != 1) {
                    throw new IllegalStateException("Expected 1 bucket, but got: " + sumBuckets);
                }
                final Double sum = sumBuckets.iterator().next().getValue();
                if (!isIngress) {
                    trafficSummary.setBytesOut(sum.longValue());
                } else {
                    trafficSummary.setBytesIn(sum.longValue());
                }
            }
            summaries.put(bucket.getKey(), trafficSummary);
        }
        return summaries;
    }

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesFromTopN(int N, String groupByTerm, String keyForMissingTerm, boolean includeOther, List<Filter> filters) {
        return getTopN(N, groupByTerm, keyForMissingTerm, filters)
                .thenCompose((topN) -> getTotalBytesFromTopN(topN, groupByTerm, keyForMissingTerm, includeOther, filters));
    }

    /**
     * Perform a best-effort classification of the application based on
     * the context of the conversation key.
     *
     * This currently has the following limitations:
     *  * The application will be classified using the current configuration which may not match
     *    that at the time that the flow were originally classified
     *  * The classification does not take the exporter into account, and as a result can be classified
     *    differently then the related flows
     *
     * @param convo the conversation key to classify
     * @return the classified application name
     */
    private String classify(ConversationKey convo) {
        final ClassificationRequest request = new ClassificationRequest();
        request.setSrcAddress(convo.getSrcIp());
        request.setSrcPort(convo.getSrcPort());
        request.setDstAddress(convo.getSrcIp());
        request.setDstPort(convo.getDstPort());

        Protocol protocol = Protocols.getProtocol(convo.getProtocol());
        if (protocol == null) {
            protocol = new Protocol(convo.getProtocol(), null, null);
        }
        request.setProtocol(protocol);
        request.setLocation(convo.getLocation());

        String application = classificationEngine.classify(request);
        if (application != null) {
            return application;
        }

        // Flip the source and destination addresses and try again
        request.setSrcAddress(convo.getDstIp());
        request.setSrcPort(convo.getDstPort());
        request.setDstAddress(convo.getSrcIp());
        request.setDstPort(convo.getSrcPort());
        return classificationEngine.classify(request);
    }

    private CompletableFuture<SearchResult> searchAsync(String query, TimeRangeFilter timeRangeFilter) {
        Search.Builder builder = new Search.Builder(query)
                .addType(TYPE);
        if(timeRangeFilter != null) {
            final List<String> indices = indexSelector.getIndexNames(timeRangeFilter);
            builder.addIndices(indices);
            builder.setParameter("ignore_unavailable", "true"); // ignore unknown index

            LOG.debug("Executing asynchronous query on {}: {}", indices, query);
        } else {
            LOG.debug("Executing asynchronous query on all indices: {}", query);
        }
        return executeAsync(builder.build());
    }

    private <T extends JestResult> CompletableFuture<T> executeAsync(Action<T> action) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        client.executeAsync(action, new JestResultHandler<T>() {
            @Override
            public void completed(T result) {
                if (!result.isSucceeded()) {
                    future.completeExceptionally(new Exception(result.getErrorMessage()));
                } else {
                    future.complete(result);
                }
            }
            @Override
            public void failed(Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    /**
     * Rebuilds the table, mapping the row keys using the given function and fills
     * in missing cells with NaN values.
     */
    private static <T> Table<Directional<T>, Long, Double> mapTable(Table<Directional<String>, Long, Double> source, Function<String, T> fn) {
        final ImmutableTable.Builder<Directional<T>, Long, Double> target = ImmutableTable.builder();
        final Set<Long> columnKeys = source.columnKeySet();
        for (Directional<String> sourceRowKey : source.rowKeySet()) {
            final Directional<T> targetRowKey = new Directional<>(fn.apply(sourceRowKey.getValue()), sourceRowKey.isIngress());
            for (Long columnKey : columnKeys) {
                Double value = source.get(sourceRowKey, columnKey);
                if (value == null) {
                    value = Double.NaN;
                }
                target.put(targetRowKey, columnKey, value);
            }
        }
        return target.build();
    }

    private static TimeRangeFilter getRequiredTimeRangeFilter(Collection<Filter> filters) {
        final TimeRangeFilter filter = extractTimeRangeFilter(filters);
        if (filter == null) {
            throw new IllegalArgumentException("Time range is required.");
        }
        return filter;
    }

    private static TimeRangeFilter extractTimeRangeFilter(Collection<Filter> filters){
        return filters.stream()
                .filter(f -> f instanceof  TimeRangeFilter)
                .map(f -> (TimeRangeFilter)f)
                .findFirst().orElse(null);
    }

    private static boolean isIngress(TermsAggregation.Entry entry) {
        final String directionAsString = entry.getKeyAsString();
        if (Direction.INGRESS.name().equalsIgnoreCase(directionAsString)) {
            return true;
        } else if (Direction.EGRESS.name().equalsIgnoreCase(directionAsString)) {
            return false;
        } else {
            throw new IllegalArgumentException("Unknown direction value: " + directionAsString);
        }
    }
}
