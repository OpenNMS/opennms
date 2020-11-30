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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Table;

/**
 * Used to intelligently delegate to the proper {@link FlowQueryService}
 * based on whether or not aggregated flows can be used.
 */
public class SmartQueryService implements FlowQueryService {

    private final FlowQueryService rawQueryService;
    private final FlowQueryService aggQueryService;

    public enum QueryServiceType {
        RAW,
        AGG
    }

    private boolean alwaysUseAggForQueries = false;
    private boolean alwaysUseRawForQueries = true;

    private long timeRangeDurationAggregateThresholdMs = TimeUnit.MINUTES.toMillis(2);
    private long timeRangeEndpointAggregateThresholdMs = TimeUnit.DAYS.toMillis(7);

    private final Timer rawQuerySuccessTimer;
    private final Timer rawQueryFailureTimer;
    private final Timer aggregatedQuerySuccessTimer;
    private final Timer aggregatedQueryFailureTimer;

    public SmartQueryService(MetricRegistry metricRegistry, FlowQueryService rawQueryService, FlowQueryService aggQueryService) {
        this.rawQueryService = Objects.requireNonNull(rawQueryService);
        this.aggQueryService = Objects.requireNonNull(aggQueryService);

        rawQuerySuccessTimer = metricRegistry.timer("rawQuerySuccess");
        rawQueryFailureTimer = metricRegistry.timer("rawQueryFailure");
        aggregatedQuerySuccessTimer = metricRegistry.timer("aggregatedQuerySuccess");
        aggregatedQueryFailureTimer = metricRegistry.timer("aggregatedQueryFailure");
    }

    private QueryServiceType getDelegate(List<Filter> filters, boolean isQueryForSpecificEntities) {
        // If we're configured to use a specific query service, then always return that one
        if (alwaysUseRawForQueries) {
            return QueryServiceType.RAW;
        } else if (alwaysUseAggForQueries) {
            return QueryServiceType.AGG;
        }

        // We do not currently support queries for specific entities in the agg service - use the raw
        if (isQueryForSpecificEntities) {
            return QueryServiceType.RAW;
        }

        final Optional<TimeRangeFilter> timeRangeFilter = Filter.find(filters, TimeRangeFilter.class);
        // We currently require a time filter for aggregated queries
        if (!timeRangeFilter.isPresent()) {
            return QueryServiceType.RAW;
        }

        // If the duration exceeds the threshold, then use aggregated queries
        if (timeRangeFilter.get().getDurationMs() >= timeRangeDurationAggregateThresholdMs) {
            return QueryServiceType.AGG;
        }

        // If the endpoint is further back in time than the threshold, then use aggregated queries
        if ((System.currentTimeMillis() - timeRangeFilter.get().getEnd()) > timeRangeEndpointAggregateThresholdMs) {
            return QueryServiceType.AGG;
        }

        return QueryServiceType.RAW;
    }

    private <T> CompletableFuture<T> runWithDelegate(List<Filter> filters, boolean isQueryForSpecificEntities,
                                                     Function<FlowQueryService, CompletableFuture<T>> query) {
        final QueryServiceType queryServiceType = getDelegate(filters, isQueryForSpecificEntities);
        switch(queryServiceType) {
            case AGG:
                return timeAsync(aggregatedQuerySuccessTimer, aggregatedQueryFailureTimer,
                        () -> query.apply(aggQueryService));
            case RAW:
            default:
                return timeAsync(rawQuerySuccessTimer, rawQueryFailureTimer,
                        () -> query.apply(rawQueryService));
        }
    }

    @Override
    public CompletableFuture<Long> getFlowCount(List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getFlowCount(filters));
    }

    @Override
    public CompletableFuture<List<String>> getApplications(String matchingPrefix, long limit, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getApplications(matchingPrefix, limit, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getTopNApplicationSummaries(N, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(Set<String> applications, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getApplicationSummaries(applications, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(Set<String> applications, long step, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getApplicationSeries(applications, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getTopNApplicationSeries(N, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<String>> getConversations(String locationPattern, String protocolPattern, String lowerIPPattern, String upperIPPattern, String applicationPattern, long limit, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getConversations(locationPattern, protocolPattern,
                        lowerIPPattern, upperIPPattern,
                        applicationPattern, limit, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getTopNConversationSummaries(N, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(Set<String> conversations, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getConversationSummaries(conversations, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(Set<String> conversations, long step, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getConversationSeries(conversations, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getTopNConversationSeries(N, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<String>> getHosts(String regex, long limit, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getHosts(regex, limit, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(int N, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getTopNHostSummaries(N, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(Set<String> hosts, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getHostSummaries(hosts, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(Set<String> hosts, long step, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, true,
                qs -> qs.getHostSeries(hosts, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getTopNHostSeries(N, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<String>> getFieldValues(LimitedCardinalityField field, List<Filter> filters) {
        return runWithDelegate(filters, false,
                qs -> qs.getFieldValues(field, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(LimitedCardinalityField field, List<Filter> filters) {
        return runWithDelegate(filters, false,
                               qs -> qs.getFieldSummaries(field, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(LimitedCardinalityField field, long step, List<Filter> filters) {
        return runWithDelegate(filters, false,
                               qs -> qs.getFieldSeries(field, step, filters));
    }

    public boolean isAlwaysUseAggForQueries() {
        return alwaysUseAggForQueries;
    }

    public void setAlwaysUseAggForQueries(boolean alwaysUseAggForQueries) {
        this.alwaysUseAggForQueries = alwaysUseAggForQueries;
        if (alwaysUseAggForQueries) {
            alwaysUseRawForQueries = false;
        }
    }

    public boolean isAlwaysUseRawForQueries() {
        return alwaysUseRawForQueries;
    }

    public void setAlwaysUseRawForQueries(boolean alwaysUseRawForQueries) {
        this.alwaysUseRawForQueries = alwaysUseRawForQueries;
        if (alwaysUseRawForQueries) {
            alwaysUseAggForQueries = false;
        }
    }

    public long getTimeRangeDurationAggregateThresholdMs() {
        return timeRangeDurationAggregateThresholdMs;
    }

    public void setTimeRangeDurationAggregateThresholdMs(long timeRangeDurationAggregateThresholdMs) {
        this.timeRangeDurationAggregateThresholdMs = timeRangeDurationAggregateThresholdMs;
    }

    public long getTimeRangeEndpointAggregateThresholdMs() {
        return timeRangeEndpointAggregateThresholdMs;
    }

    public void setTimeRangeEndpointAggregateThresholdMs(long timeRangeEndpointAggregateThresholdMs) {
        this.timeRangeEndpointAggregateThresholdMs = timeRangeEndpointAggregateThresholdMs;
    }

    @Override
    public String toString() {
        return "SmartQueryService{" +
                "alwaysUseAggForQueries=" + alwaysUseAggForQueries +
                ", alwaysUseRawForQueries=" + alwaysUseRawForQueries +
                ", timeRangeDurationAggregateThresholdMs=" + timeRangeDurationAggregateThresholdMs +
                ", timeRangeEndpointAggregateThresholdMs=" + timeRangeEndpointAggregateThresholdMs +
                '}';
    }


    /** adapted from a different version of metrics
     *     https://github.com/avast/metrics/blob/9641bceda1c4e07aed89ae092b8628d3b90e0cf8/dropwizard-common/src/main/java/com/avast/metrics/dropwizard/MetricsTimer.java#L73
     */
    private static <T> CompletableFuture<T> timeAsync(Timer successTimer, Timer failureTimer, Callable<CompletableFuture<T>> operation) {
        com.codahale.metrics.Timer.Context successContext = successTimer.time();
        com.codahale.metrics.Timer.Context failureContext = failureTimer.time();
        try {
            CompletableFuture<T> promise = new CompletableFuture<>();
            CompletableFuture<T> future = operation.call();
            future.handleAsync((success, failure) -> {
                if (failure == null) {
                    successContext.stop();
                    promise.complete(success);
                } else {
                    failureContext.stop();
                    promise.completeExceptionally(failure);
                }
                return null;
            });
            return promise;
        } catch (Exception ex) {
            failureContext.stop();
            throw new RuntimeException(ex);
        }
    }
}
