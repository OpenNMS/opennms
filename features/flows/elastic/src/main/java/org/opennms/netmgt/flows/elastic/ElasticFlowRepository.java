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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.plugins.elasticsearch.rest.BulkResultWrapper;
import org.opennms.plugins.elasticsearch.rest.FailedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.DateHistogramAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.SumAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

public class ElasticFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private static final String TYPE = "netflow";

    private final JestClient client;

    private final IndexStrategy indexStrategy;

    private final DocumentEnricher documentEnricher;

    private final SearchQueryProvider searchQueryProvider = new SearchQueryProvider();

    /**
     * Flows/second throughput
     */
    private final Meter flowsPersistedMeter;

    /**
     * Time taken to convert and enrich the flows in a log
     */
    private final Timer logEnrichementTimer;

    /**
     * Time taken to persist the flows in a log
     */
    private final Timer logPersistingTimer;


    /**
     * Number of flows in a log
     */
    private final Histogram flowsPerLog;


    public ElasticFlowRepository(MetricRegistry metricRegistry, JestClient jestClient, IndexStrategy indexStrategy, DocumentEnricher documentEnricher) {
        this.client = Objects.requireNonNull(jestClient);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.documentEnricher = Objects.requireNonNull(documentEnricher);

        flowsPersistedMeter = metricRegistry.meter("flowsPersisted");
        logEnrichementTimer = metricRegistry.timer("logEnrichment");
        logPersistingTimer = metricRegistry.timer("logPersisting");
        flowsPerLog = metricRegistry.histogram("flowsPerLog");
    }

    @Override
    public <P> void persist(final Collection<? extends P> packets, final FlowSource source, final Converter<P> converter) throws FlowException {
        LOG.debug("Converting {} flow packets from {} to flow documents.", packets.size(), source);
        final Stream<FlowDocument> documents = packets.stream()
                .map(converter::convert)
                .flatMap(Collection::stream)
                .map(FlowDocument::from);
        enrichAndPersistFlows(documents, source);
    }

    public void enrichAndPersistFlows(final Stream<FlowDocument> documents, FlowSource source) throws FlowException {
        // TODO: Use streams here?
        List<FlowDocument> flowDocuments = documents.collect(Collectors.toList());

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
            final String index = indexStrategy.getIndex(new Date());

            final Bulk.Builder bulkBuilder = new Bulk.Builder();
            for (FlowDocument flowDocument : flowDocuments) {
                final Index.Builder indexBuilder = new Index.Builder(flowDocument)
                        .index(index)
                        .type(TYPE);
                bulkBuilder.addAction(indexBuilder.build());
            }
            final Bulk bulk = bulkBuilder.build();
            final BulkResultWrapper result = new BulkResultWrapper(executeRequest(bulk));
            if (!result.isSucceeded()) {
                final List<FailedItem<FlowDocument>> failedFlows = result.getFailedItems(flowDocuments);
                throw new PersistenceException(result.getErrorMessage(), failedFlows);
            }

            flowsPersistedMeter.mark(flowDocuments.size());
        }
    }

    @Override
    public CompletableFuture<Long> getFlowCount(long start, long end) {
        final String query = searchQueryProvider.getFlowCountQuery(start, end);
        return searchAsync(query).thenApply(SearchResult::getTotal);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplications(int N, long start, long end) {
        return getTotalBytesFromTopN(N, start, end, "netflow.application");
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationsSeries(int N, long start, long end, long step) {
        return getSeriesFromTopN(N, start, end, step, "netflow.application").thenApply((res) -> mapTable(res, s -> s));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<ConversationKey>>> getTopNConversations(int N, long start, long end) {
        return getTotalBytesFromTopN(N, start, end, "netflow.convo_key").thenApply((res) -> res.stream()
                .map(summary -> {
                    // Map the strings to the corresponding conversation keys
                    final TrafficSummary<ConversationKey> out = new TrafficSummary<>(ConversationKeyUtils.fromJsonString(summary.getEntity()));
                    out.setBytesIn(summary.getBytesIn());
                    out.setBytesOut(summary.getBytesOut());
                    return out;
                })
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Table<Directional<ConversationKey>, Long, Double>> getTopNConversationsSeries(int N, long start, long end, long step) {
        return getSeriesFromTopN(N, start, end, step, "netflow.convo_key").thenApply((res) -> mapTable(res, ConversationKeyUtils::fromJsonString));
    }

    private CompletableFuture<List<String>> getTopN(int N, long start, long end, String groupByTerm) {
        // Increase the multiplier for increased accuracy
        // See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-terms-aggregation.html#_size
        final int multiplier = 2;
        final String query = searchQueryProvider.getTopNQuery(multiplier*N, start, end, groupByTerm);
        return searchAsync(query).thenApply(res -> res.getAggregations().getTermsAggregation("grouped_by").getBuckets().stream()
                .map(TermsAggregation.Entry::getKey)
                .limit(N)
                .collect(Collectors.toList()));
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFromTopN(List<String> topN, long start, long end, long step, String groupByTerm) {
        final String query = searchQueryProvider.getSeriesFromTopNQuery(topN, start, end, step, groupByTerm);
        return searchAsync(query).thenApply(res -> {
            // Build a table using the search results
            final ImmutableTable.Builder<Directional<String>, Long, Double> results = ImmutableTable.builder();
            final MetricAggregation aggs = res.getAggregations();
            final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
            for (TermsAggregation.Entry groupedByBucket : groupedBy.getBuckets()) {
                final DateHistogramAggregation bytesAggs = groupedByBucket.getDateHistogramAggregation("bytes_over_time");
                for (DateHistogramAggregation.DateHistogram dateHistogram : bytesAggs.getBuckets()) {
                    final Long time = dateHistogram.getTime();
                    final TermsAggregation directionAgg = dateHistogram.getTermsAggregation("direction");

                    // Make sure we have values for both directions, since we may only have one bucket returned here
                    Double bytesWhenInitiator = Double.NaN;
                    Double bytesWhenNotInitiator = Double.NaN;
                    for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
                        final boolean isInitiator = Boolean.valueOf(directionBucket.getKeyAsString());
                        final SumAggregation sumAgg = directionBucket.getSumAggregation("total_bytes");
                        if (isInitiator) {
                            bytesWhenInitiator = sumAgg.getSum();
                        } else {
                            bytesWhenNotInitiator = sumAgg.getSum();
                        }
                    }
                    results.put(new Directional<>(groupedByBucket.getKey(), true), time, bytesWhenInitiator);
                    results.put(new Directional<>(groupedByBucket.getKey(), false), time, bytesWhenNotInitiator);
                }
            }
            return results.build();
        });
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFromTopN(int N, long start, long end, long step,
                                                                                          String groupByTerm) {
        return getTopN(N, start, end, groupByTerm)
                .thenCompose((topN) -> getSeriesFromTopN(topN, start, end, step, groupByTerm));
    }

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesFromTopN(List<String> topN, long start, long end, String groupByTerm) {
        final String query = searchQueryProvider.getTotalBytesFromTopNQuery(topN, start, end, groupByTerm);
        return searchAsync(query).thenApply(res -> {
            // Build the traffic summaries from the search results
            final Map<String, TrafficSummary<String>> summaries = new HashMap<>();
            final MetricAggregation aggs = res.getAggregations();
            final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
            for (TermsAggregation.Entry bucket : groupedBy.getBuckets()) {
                final TrafficSummary<String> trafficSummary = new TrafficSummary<>(bucket.getKey());
                final TermsAggregation directionAgg = bucket.getTermsAggregation("direction");
                for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
                    final boolean isInitiator = Boolean.valueOf(directionBucket.getKeyAsString());
                    final SumAggregation sumAgg = directionBucket.getSumAggregation("total_bytes");
                    final Double sum = sumAgg.getSum();
                    if (!isInitiator) {
                        trafficSummary.setBytesOut(sum.longValue());
                    } else {
                        trafficSummary.setBytesIn(sum.longValue());
                    }
                }
                summaries.put(bucket.getKey(), trafficSummary);
            }
            // Now build a list in the same order as the given top N list
            final List<TrafficSummary<String>> topNRes = new ArrayList<>(topN.size());
            for (String topNEntry : topN) {
                final TrafficSummary<String> summary = summaries.get(topNEntry);
                if (summary != null) {
                    topNRes.add(summary);
                }
            }
            return topNRes;
        });
    }

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesFromTopN(int N, long start, long end, String groupByTerm) {
        return getTopN(N, start, end, groupByTerm)
                .thenCompose((topN) -> getTotalBytesFromTopN(topN, start, end, groupByTerm));
    }

    private <T extends JestResult> T executeRequest(Action<T> clientRequest) throws FlowException {
        try {
            return client.execute(clientRequest);
        } catch (IOException ex) {
            LOG.error("An error occurred while executing the given request: {}", clientRequest, ex);
            throw new FlowException(ex.getMessage(), ex);
        }
    }

    private CompletableFuture<SearchResult> searchAsync(String query) {
        LOG.debug("Executing asynchronous query: {}", query);
        final CompletableFuture<SearchResult> future = new CompletableFuture<>();
        client.executeAsync(new Search.Builder(query)
                .addType(TYPE)
                .build(), new JestResultHandler<SearchResult>() {

            @Override
            public void completed(SearchResult result) {
                future.complete(result);
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
            final Directional<T> targetRowKey = new Directional<>(fn.apply(sourceRowKey.getValue()), sourceRowKey.isSource());
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
}
