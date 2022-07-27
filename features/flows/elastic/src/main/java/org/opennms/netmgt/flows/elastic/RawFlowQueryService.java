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

package org.opennms.netmgt.flows.elastic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.searchbox.client.JestClient;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

public class RawFlowQueryService extends ElasticFlowQueryService {
    public static final String INDEX_NAME = "netflow";
    public static final String OTHER_NAME = "Other";
    public static final String UNKNOWN_APPLICATION_NAME = "Unknown";

    private final SearchQueryProvider searchQueryProvider = new SearchQueryProvider();

    public RawFlowQueryService(JestClient client, IndexSelector indexSelector) {
        super(client, indexSelector);
    }

    @Override
    public CompletableFuture<Long> getFlowCount(List<Filter> filters) {
        final String query = searchQueryProvider.getFlowCountQuery(filters);
        return searchAsync(query, extractTimeRangeFilter(filters)).thenApply(SearchResultUtils::getTotal);
    }

    @Override
    public CompletableFuture<List<String>> getApplications(String matchingPrefix, long limit, List<Filter> filters) {
        final String query = searchQueryProvider.getApplicationsQuery(matchingPrefix, limit, filters);
        return searchAsync(query, extractTimeRangeFilter(filters)).thenApply(res -> processGroupedByResult(res, limit));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFromTopN(N, "netflow.application", UNKNOWN_APPLICATION_NAME, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(Set<String> applications, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFrom(applications, "netflow.application", null, includeOther,
                filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(Set<String> applications, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFor(applications, "netflow.application", step, includeOther, filters)
                .thenCompose((res) -> mapTable(res, CompletableFuture::completedFuture));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFromTopN(N, step, "netflow.application", UNKNOWN_APPLICATION_NAME, includeOther, filters)
                .thenCompose((res) -> mapTable(res, CompletableFuture::completedFuture));
    }

    @Override
    public CompletableFuture<List<String>> getConversations(String locationPattern, String protocolPattern, String lowerIPPattern, String upperIPPattern, String applicationPattern, long limit, List<Filter> filters) {
        // Handle the unquoted null value
        if (applicationPattern.equals(".*")) {
            applicationPattern = String.format("(\\\"%s\\\"|null)", applicationPattern);
        } else if (!applicationPattern.equals("null")) {
            applicationPattern = String.format("\\\"%s\\\"", applicationPattern);
        }

        String regex = String.format("\\[\\\"%s\\\",%s,\\\"%s\\\",\\\"%s\\\",%s\\]",
                locationPattern,
                protocolPattern,
                lowerIPPattern,
                upperIPPattern,
                applicationPattern);
        String query = searchQueryProvider.getConversationsRegexQuery(regex, limit, filters);
        return searchAsync(query, extractTimeRangeFilter(filters)).thenApply(res -> processGroupedByResult(res, limit));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFromTopN(N, "netflow.convo_key", null, includeOther, filters)
                .thenCompose((summaries) -> transpose(summaries.stream()
                                .map(summary -> this.resolveHostnameForConversation(summary.getEntity(), filters)
                                        .thenApply(conversation -> TrafficSummary.from(conversation)
                                                .withBytesAndEcnInfo(summary)
                                                .build()))
                                .collect(Collectors.toList()),
                        Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(Set<String> conversations, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFrom(unescapeConversations(conversations), "netflow.convo_key", null, includeOther, filters)
                .thenCompose((summaries) -> transpose(summaries.stream()
                                .map(summary -> this.resolveHostnameForConversation(summary.getEntity(), filters)
                                        .thenApply(conversation -> TrafficSummary.from(conversation)
                                                .withBytesAndEcnInfo(summary)
                                                .build()))
                                .collect(Collectors.toList()),
                        Collectors.toList()));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(Set<String> conversations, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFor(unescapeConversations(conversations), "netflow.convo_key", step, includeOther, filters)
                .thenCompose((res) -> mapTable(res, convoKey -> this.resolveHostnameForConversation(convoKey, filters)));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFromTopN(N, step, "netflow.convo_key", null, includeOther, filters)
                .thenCompose((res) -> mapTable(res, convoKey -> this.resolveHostnameForConversation(convoKey, filters)));
    }

    @Override
    public CompletableFuture<List<String>> getHosts(String regex, long limit, List<Filter> filters) {
        final String hostsQuery = searchQueryProvider.getHostsQuery(regex, limit, filters);
        return searchAsync(hostsQuery, extractTimeRangeFilter(filters)).thenApply(res -> processGroupedByResult(res,
                limit));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(int N, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFromTopN(N, "hosts", null, includeOther, filters)
                .thenCompose((summaries) -> transpose(summaries.stream()
                                .map(summary -> this.resolveHostnameForHost(summary.getEntity(), filters)
                                        .thenApply(host -> TrafficSummary.from(host)
                                                .withBytesAndEcnInfo(summary)
                                                .build()))
                                .collect(Collectors.toList()),
                        Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(Set<String> hosts, boolean includeOther, List<Filter> filters) {
        return getTotalBytesFrom(hosts, "hosts", null, includeOther, filters)
                .thenCompose((summaries) -> transpose(summaries.stream()
                                .map(summary -> this.resolveHostnameForHost(summary.getEntity(), filters)
                                        .thenApply(host -> TrafficSummary.from(host)
                                                .withBytesAndEcnInfo(summary)
                                                .build()))
                                .collect(Collectors.toList()),
                        Collectors.toList()));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(Set<String> hosts, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFor(hosts, "hosts", step, includeOther, filters)
                .thenCompose((res) -> mapTable(res, host -> this.resolveHostnameForHost(host, filters)));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return getSeriesFromTopN(N, step, "hosts", null, includeOther, filters)
                .thenCompose((res) -> mapTable(res, host -> this.resolveHostnameForHost(host, filters)));
    }

    /**
     * Constructs a GPath that select the values of the given key in a composite aggregation.
     */
    private static <X> GPath<List<X>> compositeKeyValuesPath(String field, GPath<X> fieldValueAccess) {
        return fieldValueAccess
                .field(field)
                .field("key")
                .array("buckets")
                .field("my_buckets")
                .field("aggregations", "aggs");
    }

    @Override
    public CompletableFuture<List<String>> getFieldValues(LimitedCardinalityField field, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = extractTimeRangeFilter(filters);
        return searchAsync(searchQueryProvider.getAllValues(field.fieldName, field.size, filters), timeRangeFilter)
                .thenApply(res -> {
                    List<String> fieldValues = compositeKeyValuesPath(field.fieldName, GPath.string()).eval(res.getJsonObject());
                    return fieldValues != null ? fieldValues : Collections.emptyList();
                });
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(LimitedCardinalityField field, List<Filter> filters) {
        return getTotalBytesForSize(field.size, field.fieldName, null, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(LimitedCardinalityField field, long step, List<Filter> filters) {
        return getSeriesForSize(field.size, step, field.fieldName, null, filters);
    }

    public CompletableFuture<Conversation> resolveHostnameForConversation(final String convoKey, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = extractTimeRangeFilter(filters);

        if (OTHER_NAME.equals(convoKey)) {
            return CompletableFuture.completedFuture(Conversation.forOther().build());
        }

        final ConversationKey key = ConversationKeyUtils.fromJsonString(convoKey);
        final Conversation.Builder result = Conversation.from(key);

        final String hostnameQuery = searchQueryProvider.getHostnameByConversationQuery(convoKey, filters);
        return searchAsync(hostnameQuery, timeRangeFilter)
                .thenApply(res ->  {
                    final JsonObject hit = res.getFirstHit(JsonObject.class).source;
                    if (Objects.equals(hit.getAsJsonPrimitive("netflow.src_addr").getAsString(), key.getLowerIp())) {
                        Optional.ofNullable(hit.getAsJsonPrimitive("netflow.src_addr_hostname")).map(JsonPrimitive::getAsString).ifPresent(result::withLowerHostname);
                        Optional.ofNullable(hit.getAsJsonPrimitive("netflow.dst_addr_hostname")).map(JsonPrimitive::getAsString).ifPresent(result::withUpperHostname);

                    } else if (Objects.equals(hit.getAsJsonPrimitive("netflow.dst_addr").getAsString(), key.getLowerIp())) {
                        Optional.ofNullable(hit.getAsJsonPrimitive("netflow.dst_addr_hostname")).map(JsonPrimitive::getAsString).ifPresent(result::withLowerHostname);
                        Optional.ofNullable(hit.getAsJsonPrimitive("netflow.src_addr_hostname")).map(JsonPrimitive::getAsString).ifPresent(result::withUpperHostname);
                    }

                    return result.build();
                });
    }

    public CompletableFuture<Host> resolveHostnameForHost(final String host, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = extractTimeRangeFilter(filters);

        if (OTHER_NAME.equals(host)) {
            return CompletableFuture.completedFuture(Host.forOther().build());
        }

        final Host.Builder result = Host.from(host);

        final String hostnameQuery = searchQueryProvider.getHostnameByHostQuery(host, filters);
        return searchAsync(hostnameQuery, timeRangeFilter)
                .thenApply(res ->  {
                    final JsonObject hit = res.getFirstHit(JsonObject.class).source;
                    if (Objects.equals(hit.getAsJsonPrimitive("netflow.src_addr").getAsString(), host)) {
                        Optional.ofNullable(hit.getAsJsonPrimitive("netflow.src_addr_hostname")).map(JsonPrimitive::getAsString).ifPresent(result::withHostname);
                    } else if (Objects.equals(hit.getAsJsonPrimitive("netflow.dst_addr").getAsString(), host)) {
                        Optional.ofNullable(hit.getAsJsonPrimitive("netflow.dst_addr_hostname")).map(JsonPrimitive::getAsString).ifPresent(result::withHostname);
                    }

                    return result.build();
                });
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
                .thenApply(res -> processGroupedByResult(res, N));
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFor(Set<String> entities,
                                                                                     String groupByTerm,
                                                                                     long step,
                                                                                     boolean includeOther,
                                                                                     List<Filter> filters) {
        Objects.requireNonNull(groupByTerm);

        if (entities == null || entities.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        final String seriesFromQuery = searchQueryProvider.getSeriesFromQuery(entities, step,
                timeRangeFilter.getStart(), timeRangeFilter.getEnd(), groupByTerm, filters);
        CompletableFuture<ImmutableTable.Builder<Directional<String>, Long, Double>> seriesFuture;

        seriesFuture = searchAsync(seriesFromQuery, timeRangeFilter)
                .thenApply(res -> {
                    final ImmutableTable.Builder<Directional<String>, Long, Double> builder = ImmutableTable.builder();
                    toTable(builder, res);
                    return builder;
                });

        if (includeOther) {
            // We also want to gather series for all other terms
            final String seriesFromOthersQuery = searchQueryProvider.getSeriesFromOthersQuery(entities, step,
                    timeRangeFilter.getStart(), timeRangeFilter.getEnd(), groupByTerm, false, filters);
            seriesFuture = seriesFuture.thenCombine(searchAsync(seriesFromOthersQuery, timeRangeFilter),
                    (builder, res) -> processOthersResult(res, builder));
        }

        return seriesFuture.thenApply(builder -> builder.build());
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFromTopN(List<String> topN, long step, String groupByTerm,
                                                                                          String keyForMissingTerm,
                                                                                          boolean includeOther, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);

        CompletableFuture<ImmutableTable.Builder<Directional<String>, Long, Double>> seriesFuture;
        if (topN.size() < 1) {
            // If there are no entries, skip the query
            seriesFuture = CompletableFuture.completedFuture(ImmutableTable.builder());
        } else {
            final String seriesFromTopNQuery = searchQueryProvider.getSeriesFromQuery(topN, step, timeRangeFilter.getStart(),
                    timeRangeFilter.getEnd(), groupByTerm, filters);
            seriesFuture = searchAsync(seriesFromTopNQuery, timeRangeFilter)
                    .thenApply(res -> {
                        final ImmutableTable.Builder<Directional<String>, Long, Double> builder = ImmutableTable.builder();
                        toTable(builder, res);
                        return builder;
                    });
        }

        final boolean missingTermIncludedInTopN = keyForMissingTerm != null && topN.contains(keyForMissingTerm);
        if (missingTermIncludedInTopN) {
            // We also need to query for items with a missing term, this will require a separate query
            final String seriesFromMissingQuery = searchQueryProvider.getSeriesFromMissingQuery(step,
                    timeRangeFilter.getStart(), timeRangeFilter.getEnd(), groupByTerm, keyForMissingTerm, filters);
            seriesFuture = seriesFuture
                    .thenCombine(searchAsync(seriesFromMissingQuery, extractTimeRangeFilter(filters)), (builder, res) -> {
                        toTable(builder, res);
                        return builder;
                    });
        }

        if (includeOther) {
            // We also want to gather series for terms not part of the Top N
            final String seriesFromOthersQuery = searchQueryProvider.getSeriesFromOthersQuery(topN, step,
                    timeRangeFilter.getStart(), timeRangeFilter.getEnd(), groupByTerm, missingTermIncludedInTopN, filters);
            seriesFuture = seriesFuture.thenCombine(searchAsync(seriesFromOthersQuery, timeRangeFilter),
                    (builder, res) -> processOthersResult(res, builder));
        }

        // Sort the table to ensure that the rows as in the same order as the Top N
        return seriesFuture.thenApply(builder -> TableUtils.sortTableByRowKeys(builder.build(), topN));
    }

    private static ImmutableTable.Builder<Directional<String>, Long, Double> toTable(ImmutableTable.Builder<Directional<String>, Long, Double> builder, SearchResult res) {
        final MetricAggregation aggs = res.getAggregations();
        if (aggs == null) {
            // No results
            return builder;
        }
        final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
        if (groupedBy == null) {
            // No results
            return builder;
        }
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
        return builder;
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesForSize(int size, long step, String groupByTerm,
                                                                                         String keyForMissingTerm,
                                                                                         List<Filter> filters) {
        FilterAndGroupBy fag = new FilterAndGroupBy(filters, groupByTerm, step);
        return seriesForSize(fag, size)
                .thenCompose(addSeriesForMissing(fag, keyForMissingTerm))
                .thenApply(builder -> builder.build());
    }

    private CompletableFuture<Table<Directional<String>, Long, Double>> getSeriesFromTopN(int N, long step, String groupByTerm,
                                                                                          String keyForMissingTerm, boolean includeOther,
                                                                                          List<Filter> filters) {
        return getTopN(N, groupByTerm, keyForMissingTerm, filters)
                .thenCompose((topN) -> getSeriesFromTopN(topN, step, groupByTerm, keyForMissingTerm, includeOther, filters));
    }

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesFrom(Collection<String> from, String groupByTerm,
                                                                              String keyForMissingTerm,
                                                                              boolean includeOther, List<Filter> filters) {
        final TimeRangeFilter timeRangeFilter = getRequiredTimeRangeFilter(filters);
        final long start = timeRangeFilter.getStart();
        // Remove 1 from the end to make sure we have a single bucket
        final long end = Math.max(timeRangeFilter.getStart(), timeRangeFilter.getEnd() - 1);
        // A single step
        final long step = timeRangeFilter.getEnd() - timeRangeFilter.getStart();

        CompletableFuture<Map<String, TrafficSummary<String>>> summariesFuture;
        if (from.size() < 1) {
            // If there are no entries, skip the query
            summariesFuture = CompletableFuture.completedFuture(new LinkedHashMap<>());
        } else {
            final String bytesFromQuery = searchQueryProvider.getSeriesFromQuery(from, step, start, end, groupByTerm, filters);
            summariesFuture = searchAsync(bytesFromQuery, timeRangeFilter).thenApply(RawFlowQueryService::toTrafficSummaries);
        }

        final boolean missingTermIncluded = keyForMissingTerm != null && from.contains(keyForMissingTerm);
        if (missingTermIncluded) {
            // We also need to query for items with a missing term, this will require a separate query
            final String bytesFromMissingQuery = searchQueryProvider.getSeriesFromMissingQuery(step, start, end,
                    groupByTerm, keyForMissingTerm, filters);
            summariesFuture = summariesFuture.thenCombine(searchAsync(bytesFromMissingQuery, timeRangeFilter), (summaries, results) -> {
                summaries.putAll(toTrafficSummaries(results));
                return summaries;
            });
        }

        if (includeOther) {
            // We also want to tally up traffic from other elements not part of the Top N
            final String bytesFromOthersQuery = searchQueryProvider.getSeriesFromOthersQuery(from, step, start, end,
                    groupByTerm, missingTermIncluded, filters);
            summariesFuture = summariesFuture.thenCombine(searchAsync(bytesFromOthersQuery, timeRangeFilter), (summaries, results) -> {
                final MetricAggregation aggs = results.getAggregations();
                if (aggs == null) {
                    // No results
                    return summaries;
                }
                final TermsAggregation directionAgg = aggs.getTermsAggregation("direction");
                if (directionAgg == null) {
                    // No results
                    return summaries;
                }
                final TrafficSummary.Builder<String> trafficSummary = TrafficSummary.from(OTHER_NAME);
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
                        trafficSummary.withBytesOut(sum.longValue());
                    } else {
                        trafficSummary.withBytesIn(sum.longValue());
                    }
                    trafficSummary.withEcnInfo(directionBucket);
                }
                summaries.put(OTHER_NAME, trafficSummary.build());
                return summaries;
            });
        }

        return summariesFuture.thenApply(summaries -> {
            // Now build a list in the same order as the given top N list
            final List<TrafficSummary<String>> topNRes = new ArrayList<>(from.size());
            for (String topNEntry : from) {
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

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesForSize(int size, String groupByTerm,
                                                                                 String keyForMissingTerm,
                                                                                 List<Filter> filters) {
        FilterAndGroupBy fag = new FilterAndGroupBy(filters, groupByTerm);
        return totalBytesForSize(fag, size)
                .thenCompose(addTotalBytesForMissing(fag, keyForMissingTerm))
                .thenApply(orderSummariesByIntKeys());
    }

    //
    // helper functions for composing request processing logic
    //

    private static <T> Function<T, CompletableFuture<T>> keepValue() {
        return t -> CompletableFuture.completedFuture(t);
    }

    //
    // helper functions for composing request processing logic for series
    //

    private CompletableFuture<ImmutableTable.Builder<Directional<String>, Long, Double>> seriesForSize(FilterAndGroupBy fag, int size) {
        if (size < 1) {
            // If there are no entries, skip the query
            return CompletableFuture.completedFuture(ImmutableTable.builder());
        } else {
            final String bytesFromQuery = searchQueryProvider.getSeriesFromQuery(size, fag.step, fag.start, fag.end, fag.groupByTerm, fag.filters);
            return searchAsync(bytesFromQuery, fag.timeRangeFilter).thenApply(searchResult -> {
                ImmutableTable.Builder<Directional<String>, Long, Double> builder = ImmutableTable.builder();
                toTable(builder, searchResult);
                return builder;
            });
        }
    }

    private Function<ImmutableTable.Builder<Directional<String>, Long, Double>, CompletableFuture<ImmutableTable.Builder<Directional<String>, Long, Double>>>
    addSeriesForMissing(FilterAndGroupBy fag, String keyForMissingTerm) {
        if (keyForMissingTerm != null) {
            // We also need to query for items with a missing term, this will require a separate query
            final String seriesFromMissingQuery =
                    searchQueryProvider.getSeriesFromMissingQuery(fag.step, fag.start, fag.end, fag.groupByTerm, keyForMissingTerm, fag.filters);
            CompletableFuture<SearchResult> missingFuture = searchAsync(seriesFromMissingQuery, fag.timeRangeFilter);
            return builder -> missingFuture.thenApply(searchResult -> toTable(builder, searchResult));
        } else {
            return keepValue();
        }
    }

    //
    // helper functions for composing request processing logic for totalBytes
    //

    private CompletableFuture<Map<String, TrafficSummary<String>>> totalBytesForSize(FilterAndGroupBy fag, int size) {
        if (size < 1) {
            // If there are no entries, skip the query
            return CompletableFuture.completedFuture(new LinkedHashMap<>());
        } else {
            final String bytesFromQuery = searchQueryProvider.getSeriesFromQuery(size, fag.step, fag.start, fag.end, fag.groupByTerm, fag.filters);
            return searchAsync(bytesFromQuery, fag.timeRangeFilter).thenApply(RawFlowQueryService::toTrafficSummaries);
        }
    }

    private Function<Map<String, TrafficSummary<String>>, CompletableFuture<Map<String, TrafficSummary<String>>>>
    addTotalBytesForMissing(FilterAndGroupBy fag, String keyForMissingTerm) {
        if (keyForMissingTerm != null) {
            // We also need to query for items with a missing term, this will require a separate query
            String bytesFromMissingQuery
                    = searchQueryProvider.getSeriesFromMissingQuery(fag.step, fag.start, fag.end, fag.groupByTerm, keyForMissingTerm, fag.filters);
            CompletableFuture<SearchResult> missingFuture = searchAsync(bytesFromMissingQuery, fag.timeRangeFilter);
            return summaries -> missingFuture.thenApply(results -> {
                summaries.putAll(toTrafficSummaries(results));
                return summaries;
            });
        } else {
            return keepValue();
        }
    }

    private Function<Map<String, TrafficSummary<String>>, List<TrafficSummary<String>>> orderSummariesByIntKeys() {
        return summaries -> summaries
                .values()
                .stream()
                .sorted(TRAFFIC_SUMMARY_STRING_AS_INT_COMPARATOR)
                .collect(Collectors.toList());
    }

    private static Predicate<String> IS_INT = Pattern.compile("-?\\d+").asPredicate();

    private static Comparator<TrafficSummary<String>> TRAFFIC_SUMMARY_STRING_AS_INT_COMPARATOR = (s1, s2) -> {
        // increasing; numbers first; fallback to string order
        boolean d1 = IS_INT.test(s1.getEntity());
        boolean d2 = IS_INT.test(s2.getEntity());
        return d1 && d2 ?
               new Integer(s1.getEntity()).compareTo(new Integer(s2.getEntity())) :
               d1 ? -1 : d2 ? 1 : s1.getEntity().compareTo(s2.getEntity());
    };

    private static Map<String, TrafficSummary<String>> toTrafficSummaries(SearchResult res) {
        // Build the traffic summaries from the search results
        final Map<String, TrafficSummary<String>> summaries = new LinkedHashMap<>();
        final MetricAggregation aggs = res.getAggregations();
        if (aggs == null) {
            // No results
            return summaries;
        }
        final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
        if (groupedBy == null) {
            // No results
            return summaries;
        }
        for (TermsAggregation.Entry bucket : groupedBy.getBuckets()) {
            final TrafficSummary.Builder<String> trafficSummary = TrafficSummary.from(bucket.getKey());
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
                    trafficSummary.withBytesOut(sum.longValue());
                } else {
                    trafficSummary.withBytesIn(sum.longValue());
                }
                trafficSummary.withEcnInfo(directionBucket);
            }
            summaries.put(bucket.getKey(), trafficSummary.build());
        }
        return summaries;
    }

    private CompletableFuture<List<TrafficSummary<String>>> getTotalBytesFromTopN(int N, String groupByTerm, String keyForMissingTerm, boolean includeOther, List<Filter> filters) {
        return getTopN(N, groupByTerm, keyForMissingTerm, filters)
                .thenCompose((topN) -> getTotalBytesFrom(topN, groupByTerm, keyForMissingTerm, includeOther, filters));
    }

    private static ImmutableTable.Builder<Directional<String>, Long, Double> processOthersResult(SearchResult res,
                                                                                                 ImmutableTable.Builder<Directional<String>, Long, Double> builder) {
        final MetricAggregation aggs = res.getAggregations();
        if (aggs == null) {
            // No results
            return builder;
        }
        final TermsAggregation directionAgg = aggs.getTermsAggregation("direction");
        if (directionAgg == null) {
            // No results
            return builder;
        }
        for (TermsAggregation.Entry directionBucket : directionAgg.getBuckets()) {
            final boolean isIngress = isIngress(directionBucket);
            final ProportionalSumAggregation sumAgg = directionBucket.getAggregation("bytes",
                    ProportionalSumAggregation.class);
            for (ProportionalSumAggregation.DateHistogram dateHistogram : sumAgg.getBuckets()) {
                builder.put(new Directional<>(OTHER_NAME, isIngress), dateHistogram.getTime(),
                        dateHistogram.getValue());
            }
        }
        return builder;
    }

    private static List<String> processGroupedByResult(SearchResult searchResult, long limit) {
        final MetricAggregation aggs = searchResult.getAggregations();
        if (aggs == null) {
            // No results
            return Collections.emptyList();
        }
        final TermsAggregation groupedBy = aggs.getTermsAggregation("grouped_by");
        if (groupedBy == null) {
            // No results
            return Collections.emptyList();
        }
        return groupedBy.getBuckets().stream()
                .map(TermsAggregation.Entry::getKey)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private static TimeRangeFilter getRequiredTimeRangeFilter(Collection<Filter> filters) {
        final TimeRangeFilter filter = extractTimeRangeFilter(filters);
        if (filter == null) {
            throw new IllegalArgumentException("Time range is required.");
        }
        return filter;
    }

    private static TimeRangeFilter extractTimeRangeFilter(Collection<Filter> filters) {
        return filters.stream()
                .filter(f -> f instanceof TimeRangeFilter)
                .map(f -> (TimeRangeFilter) f)
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

    private static Set<String> unescapeConversations(Set<String> conversations) {
        // freemarker template is going to auto-escape the string so we need to remove the explicit escaped quotes first
        return conversations.stream()
                .map(conversation -> conversation.replace("\\\"", "\""))
                .collect(Collectors.toSet());
    }

    private static class FilterAndGroupBy {

        public final String groupByTerm;
        public final List<Filter> filters;
        public final TimeRangeFilter timeRangeFilter;
        public final long start;
        public final long end;
        public final long step;

        /**
         * Start/End/Step are calculated such that the interval is covered by a single step
         */
        public FilterAndGroupBy(
                List<Filter> filters,
                String groupByTerm
        ) {
            Objects.requireNonNull(filters);
            Objects.requireNonNull(groupByTerm);
            this.filters = filters;
            this.groupByTerm = groupByTerm;
            timeRangeFilter = getRequiredTimeRangeFilter(filters);
            start = timeRangeFilter.getStart();
            // Remove 1 from the end to make sure we have a single bucket
            end = Math.max(timeRangeFilter.getStart(), timeRangeFilter.getEnd() - 1);
            // A single step
            step = timeRangeFilter.getEnd() - timeRangeFilter.getStart();
        }

        public FilterAndGroupBy(
                List<Filter> filters,
                String groupByTerm,
                long step
        ) {
            Objects.requireNonNull(filters);
            Objects.requireNonNull(groupByTerm);
            this.filters = filters;
            this.groupByTerm = groupByTerm;
            this.step = step;
            timeRangeFilter = getRequiredTimeRangeFilter(filters);
            start = timeRangeFilter.getStart();
            end = timeRangeFilter.getEnd();
        }
    }
}
