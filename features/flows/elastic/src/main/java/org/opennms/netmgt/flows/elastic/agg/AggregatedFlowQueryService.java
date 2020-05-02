/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic.agg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.netmgt.flows.api.BytesInOut;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.elastic.ElasticFlowQueryService;
import org.opennms.netmgt.flows.elastic.ProportionalSumAggregation;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import io.searchbox.client.JestClient;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.SumAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

/**
 * Reads out from aggregated flows.
 */
public class AggregatedFlowQueryService extends ElasticFlowQueryService {

    public static final String INDEX_NAME = "netflow_agg";

    private final AggregatedSearchQueryProvider searchQueryProvider = new AggregatedSearchQueryProvider();

    public AggregatedFlowQueryService(JestClient client, IndexSelector indexSelector) {
        super(client, indexSelector);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return getTopNSummary(N, includeOther, filters, GroupedBy.EXPORTER_INTERFACE_APPLICATION, Types.APPLICATION );
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getTopNSeries(N, step, includeOther, filters, GroupedBy.EXPORTER_INTERFACE_APPLICATION, Types.APPLICATION);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return getTopNSummary(N, includeOther, filters, GroupedBy.EXPORTER_INTERFACE_CONVERSATION, Types.CONVERSATION );
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getTopNSeries(N, step, includeOther, filters, GroupedBy.EXPORTER_INTERFACE_CONVERSATION, Types.CONVERSATION );
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(int N, boolean includeOther, List<Filter> filters) {
        return getTopNSummary(N, includeOther, filters, GroupedBy.EXPORTER_INTERFACE_HOST, Types.HOST );
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getTopNSeries(N, step, includeOther, filters, GroupedBy.EXPORTER_INTERFACE_HOST, Types.HOST );
    }

    /**
     * Retrieve time series data from aggregated flow statistics.
     *
     * @param N limit on number of entities in the top K that we want to query
     * @param step step size in ms
     * @param includeOther true to include the delta between top-n series and the total traffic as an additional series
     * @param filters filter criteria - see Filter
     * @param groupedBy grouped by context to query - see GroupedBy
     * @param type type descriptor for the entity we want to extract from the document
     * @param <T> entity future
     * @return a future for a table containing the time series
     */
    private <T> CompletableFuture<Table<Directional<T>, Long, Double>> getTopNSeries(int N, long step, boolean includeOther, List<Filter> filters,
                                                                                       GroupedBy groupedBy, Types.Type<T> type) {
        // Time is a must for *time* series
        final TimeRangeFilter timeRangeFilter = Filter.find(filters, TimeRangeFilter.class)
                .orElseThrow(() -> new IllegalArgumentException("Time range filter is required to derive time series."));

        // Build the series for the top-N
        final ImmutableTable.Builder<Directional<T>, Long, Double> builder = ImmutableTable.builder();
        CompletableFuture<Void> seriesFuture;
        if (N > 0) {
            final String seriesFromTopNQuery = searchQueryProvider.getSeriesFromTopNQuery(N, groupedBy,
                    type.getKey(), step, timeRangeFilter.getStart(),
                    timeRangeFilter.getEnd(), filters);
            seriesFuture = searchAsync(seriesFromTopNQuery, timeRangeFilter)
                    .thenApply(res -> {
                        toTableFromBuckets(builder, type::toEntity, res);
                        return null;
                    });
        } else {
            seriesFuture = CompletableFuture.completedFuture(null);
        }

        if (includeOther) {
            // Build the series for the total traffic
            final String seriesFromTotalQuery = searchQueryProvider.getSeriesFromTotalsQuery(groupedBy.getParent(), step, timeRangeFilter.getStart(),
                    timeRangeFilter.getEnd(), filters);
            final ImmutableTable.Builder<Directional<T>, Long, Double> totalsTableBuilder = ImmutableTable.builder();
            CompletableFuture<Void> totalSeriesFuture = searchAsync(seriesFromTotalQuery, timeRangeFilter)
                    .thenApply(res -> {
                        toTableFromTotals(totalsTableBuilder, type.getOtherEntity(), res);
                        return null;
                    });

            // Compute a new series to represent the delta
            seriesFuture = seriesFuture.thenCombine(totalSeriesFuture, (topN,totals) -> {
                ImmutableTable<Directional<T>, Long, Double> topNTable = builder.build();
                ImmutableTable<Directional<T>, Long, Double> totalsTable = totalsTableBuilder.build();

                SortedSet<Long> timestamps = new TreeSet<>();
                timestamps.addAll(topNTable.columnKeySet());
                timestamps.addAll(totalsTable.columnKeySet());
                for (Long ts : timestamps) {
                    ImmutableMap<Directional<T>, Double>  entries = topNTable.column(ts);
                    BytesInOut bytesFromTopN;
                    if (entries != null) {
                        bytesFromTopN = BytesInOut.sum(entries.entrySet());
                    } else {
                        bytesFromTopN = new BytesInOut();
                    }

                    entries = totalsTable.column(ts);
                    BytesInOut totalBytes;
                    if (entries != null) {
                        totalBytes = BytesInOut.sum(entries.entrySet());
                    } else {
                        totalBytes = new BytesInOut();
                    }

                    // Determine the remainder of bytes not represented by the top N
                    BytesInOut otherBytes = totalBytes.minus(bytesFromTopN);

                    builder.put(new Directional<>(type.getOtherEntity(), true), ts, (double)otherBytes.getBytesIn());
                    builder.put(new Directional<>(type.getOtherEntity(), false), ts, (double)otherBytes.getBytesOut());
                }

                return null;
            });
        }

        return seriesFuture.thenApply(ignored -> builder.build());
    }

    /** use the convert the results of the proportional_sum aggregation (provided by our ES plugin) to a table */
    private static <T> void toTableFromBuckets(ImmutableTable.Builder<Directional<T>, Long, Double> builder, Function<String,T> keyToEntity, SearchResult res) {
        final MetricAggregation aggs = res.getAggregations();
        if (aggs == null) {
            // No results
            return;
        }
        final TermsAggregation byKeyAgg = aggs.getTermsAggregation("by_key");
        if (byKeyAgg == null) {
            // No results
            return;
        }
        for (TermsAggregation.Entry bucket : byKeyAgg.getBuckets()) {
            final ProportionalSumAggregation bytesInAgg = bucket.getAggregation("bytes_in", ProportionalSumAggregation.class);
            for (ProportionalSumAggregation.DateHistogram dateHistogram : bytesInAgg.getBuckets()) {
                builder.put(new Directional<>(keyToEntity.apply(bucket.getKey()), true), dateHistogram.getTime(), dateHistogram.getValue());
            }
            final ProportionalSumAggregation bytesOutAgg = bucket.getAggregation("bytes_out", ProportionalSumAggregation.class);
            for (ProportionalSumAggregation.DateHistogram dateHistogram : bytesOutAgg.getBuckets()) {
                builder.put(new Directional<>(keyToEntity.apply(bucket.getKey()), false), dateHistogram.getTime(), dateHistogram.getValue());
            }
        }
    }

    /** use the convert the results of the proportional_sum aggregation (provided by our ES plugin) to a table */
    private static <T> void toTableFromTotals(ImmutableTable.Builder<Directional<T>, Long, Double> builder, T otherEntity, SearchResult res) {
        final MetricAggregation aggs = res.getAggregations();
        if (aggs == null) {
            // No results
            return;
        }
        final ProportionalSumAggregation bytesInAgg = aggs.getAggregation("bytes_in", ProportionalSumAggregation.class);
        for (ProportionalSumAggregation.DateHistogram dateHistogram : bytesInAgg.getBuckets()) {
            builder.put(new Directional<>(otherEntity, true), dateHistogram.getTime(), dateHistogram.getValue());
        }
        final ProportionalSumAggregation bytesOutAgg = aggs.getAggregation("bytes_out", ProportionalSumAggregation.class);
        for (ProportionalSumAggregation.DateHistogram dateHistogram : bytesOutAgg.getBuckets()) {
            builder.put(new Directional<>(otherEntity, false), dateHistogram.getTime(), dateHistogram.getValue());
        }
    }

    /**
     * Retrieve traffic summaries (totals) from aggregated flow statistics.
     *
     * @param N limit on number of entities in the top K that we want to query
     * @param includeOther true to include the delta between top-n summaries and the total traffic as an additional entry
     * @param filters filter criteria - see Filter
     * @param groupedBy grouped by context to query - see GroupedBy
     * @param type type descriptor for the entity we want to extract from the document
     * @param <T> entity future
     * @return a future for a list containing the summary for the different entities
     */
    private <T> CompletableFuture<List<TrafficSummary<T>>> getTopNSummary(int N, boolean includeOther, List<Filter> filters,
                                                                          GroupedBy groupedBy, Types.Type<T> type) {
        CompletableFuture<List<TrafficSummary<T>>> summaryFutures;
        if (N > 0) {
            final String query = searchQueryProvider.getTopNQuery(N, groupedBy, type.getKey(), filters);
            summaryFutures = searchAsync(query, Filter.find(filters, TimeRangeFilter.class).orElse(null))
                    .thenApply(searchResult -> {
                        final MetricAggregation aggs = searchResult.getAggregations();
                        if (aggs == null) {
                            // No results
                            return Collections.emptyList();
                        }
                        final TermsAggregation byKeyAgg = aggs.getTermsAggregation("by_key");
                        if (byKeyAgg == null) {
                            // No results
                            return Collections.emptyList();
                        }

                        List<TrafficSummary<T>> trafficSummaries = new ArrayList<>(N);
                        for (TermsAggregation.Entry bucket : byKeyAgg.getBuckets()) {
                            SumAggregation ingress = bucket.getSumAggregation("bytes_ingress");
                            SumAggregation egress = bucket.getSumAggregation("bytes_egress");

                            trafficSummaries.add(TrafficSummary.<T>builder()
                                    .withEntity(type.toEntity(bucket.getKeyAsString()))
                                    .withBytesIn(ingress.getSum().longValue())
                                    .withBytesOut(egress.getSum().longValue())
                                    .build());
                        }
                        return trafficSummaries;
                    });
        } else {
            summaryFutures = CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (!includeOther) {
            return summaryFutures;
        }

        CompletableFuture<TrafficSummary<T>> totalTrafficFuture = getOtherTraffic(groupedBy.getParent(), type.getOtherEntity(), filters);
        return summaryFutures.thenCombine(totalTrafficFuture, (topK,total) -> {
            BytesInOut totalBytes = total.getBytesInOut();
            BytesInOut bytesFromTopK = BytesInOut.sum(topK);

            // Determine the remainder of bytes not represented by the top K
            BytesInOut otherBytes = totalBytes.minus(bytesFromTopK);

            List<TrafficSummary<T>> newTopK = new ArrayList<>(topK);
            newTopK.add(TrafficSummary.<T>builder()
                    .withEntity(type.getOtherEntity())
                    .withBytesIn(otherBytes.getBytesIn())
                    .withBytesOut(otherBytes.getBytesOut())
                    .build());
            return newTopK;
        });
    }

    private <T> CompletableFuture<TrafficSummary<T>> getOtherTraffic(GroupedBy groupedBy, T entity, List<Filter> filters) {
        final String query = searchQueryProvider.getSumQuery(groupedBy, filters);
        return searchAsync(query, Filter.find(filters, TimeRangeFilter.class).orElse(null))
                .thenApply(searchResult -> {
                    final MetricAggregation aggs = searchResult.getAggregations();
                    SumAggregation ingress = aggs.getSumAggregation("bytes_ingress");
                    SumAggregation egress = aggs.getSumAggregation("bytes_egress");
                    return TrafficSummary.<T>builder()
                            .withEntity(entity)
                            .withBytesIn(ingress != null ? ingress.getSum().longValue() : 0L)
                            .withBytesOut(egress != null ? egress.getSum().longValue() : 0L)
                            .build();
                });
    }

    @Override
    public CompletableFuture<Long> getFlowCount(List<Filter> filters) {
        final String query = searchQueryProvider.getFlowCountQuery(filters);
        return searchAsync(query, Filter.find(filters, TimeRangeFilter.class).orElse(null))
                .thenApply(SearchResultUtils::getTotal);
    }

    // Unsupported methods

    // These could be approximated with the data we have - but their results are normally used to queries
    // for those same individual entities, which we don't yet support

    @Override
    public CompletableFuture<List<String>> getApplications(String matchingPrefix, long limit, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating applications is not supported.");
    }

    @Override
    public CompletableFuture<List<String>> getConversations(String locationPattern, String protocolPattern, String lowerIPPattern, String upperIPPattern, String applicationPattern, long limit, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating conversations is not supported.");
    }

    @Override
    public CompletableFuture<List<String>> getHosts(String regex, long limit, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating hosts is not supported.");
    }

    // The remaining queries ask for specific entities over time
    // We can't currently give accurate results unless the entity falls consistently in the top ks

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(Set<String> applications, boolean includeOther, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating specific application summaries is not supported.");
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(Set<String> applications, long step, boolean includeOther, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating specific application series is not supported.");
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(Set<String> conversations, boolean includeOther, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating specific conversation summaries is not supported.");
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(Set<String> conversations, long step, boolean includeOther, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating specific conversation series is not supported.");
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(Set<String> hosts, boolean includeOther, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating specific host summaries is not supported.");
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(Set<String> hosts, long step, boolean includeOther, List<Filter> filters) {
        throw new UnsupportedOperationException("Enumerating specific conversation series is not supported.");
    }

}
