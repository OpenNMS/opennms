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
package org.opennms.netmgt.flows.postgres;

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
 * Routes each flow query to either the raw ({@link PostgresFlowQueryService}) or aggregated
 * ({@link AggregatedFlowQueryService}) backend, port of the Elasticsearch {@code SmartQueryService}.
 *
 * <p>Routing (identical to ES): the override flags win first ({@code alwaysUseRawForQueries} defaults
 * true, so aggregates are opt-in); queries for specific entities (explicit sets / entity listings) always
 * go raw; a {@link TimeRangeFilter} is required for aggregates; then a query goes aggregated when its
 * range duration is at least {@code timeRangeDurationAggregateThresholdMs} (default 2 min) or its endpoint
 * is older than {@code timeRangeEndpointAggregateThresholdMs} (default 7 days) — otherwise raw.
 */
public class PostgresSmartQueryService implements FlowQueryService {

    private final FlowQueryService rawQueryService;
    private final FlowQueryService aggQueryService;

    public enum QueryServiceType { RAW, AGG }

    private boolean alwaysUseAggForQueries = false;
    private boolean alwaysUseRawForQueries = true;

    private long timeRangeDurationAggregateThresholdMs = TimeUnit.MINUTES.toMillis(2);
    private long timeRangeEndpointAggregateThresholdMs = TimeUnit.DAYS.toMillis(7);

    private final Timer rawQuerySuccessTimer;
    private final Timer rawQueryFailureTimer;
    private final Timer aggregatedQuerySuccessTimer;
    private final Timer aggregatedQueryFailureTimer;

    public PostgresSmartQueryService(final MetricRegistry metricRegistry, final FlowQueryService rawQueryService,
                                     final FlowQueryService aggQueryService) {
        this.rawQueryService = Objects.requireNonNull(rawQueryService);
        this.aggQueryService = Objects.requireNonNull(aggQueryService);
        this.rawQuerySuccessTimer = metricRegistry.timer("rawQuerySuccess");
        this.rawQueryFailureTimer = metricRegistry.timer("rawQueryFailure");
        this.aggregatedQuerySuccessTimer = metricRegistry.timer("aggregatedQuerySuccess");
        this.aggregatedQueryFailureTimer = metricRegistry.timer("aggregatedQueryFailure");
    }

    QueryServiceType getDelegate(final List<Filter> filters, final boolean isQueryForSpecificEntities) {
        if (alwaysUseRawForQueries) {
            return QueryServiceType.RAW;
        } else if (alwaysUseAggForQueries) {
            return QueryServiceType.AGG;
        }
        // Specific-entity queries are not supported by the aggregate backend.
        if (isQueryForSpecificEntities) {
            return QueryServiceType.RAW;
        }
        final Optional<TimeRangeFilter> timeRangeFilter = Filter.find(filters, TimeRangeFilter.class);
        if (!timeRangeFilter.isPresent()) {
            return QueryServiceType.RAW;
        }
        if (timeRangeFilter.get().getDurationMs() >= timeRangeDurationAggregateThresholdMs) {
            return QueryServiceType.AGG;
        }
        if ((System.currentTimeMillis() - timeRangeFilter.get().getEnd()) > timeRangeEndpointAggregateThresholdMs) {
            return QueryServiceType.AGG;
        }
        return QueryServiceType.RAW;
    }

    private <T> CompletableFuture<T> runWithDelegate(final List<Filter> filters, final boolean isQueryForSpecificEntities,
                                                     final Function<FlowQueryService, CompletableFuture<T>> query) {
        switch (getDelegate(filters, isQueryForSpecificEntities)) {
            case AGG:
                return timeAsync(aggregatedQuerySuccessTimer, aggregatedQueryFailureTimer, () -> query.apply(aggQueryService));
            case RAW:
            default:
                return timeAsync(rawQuerySuccessTimer, rawQueryFailureTimer, () -> query.apply(rawQueryService));
        }
    }

    @Override
    public CompletableFuture<Long> getFlowCount(final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getFlowCount(filters));
    }

    @Override
    public CompletableFuture<List<String>> getApplications(final String matchingPrefix, final long limit, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getApplications(matchingPrefix, limit, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getTopNApplicationSummaries(n, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(final Set<String> applications, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getApplicationSummaries(applications, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(final Set<String> applications, final long step, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getApplicationSeries(applications, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getTopNApplicationSeries(n, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<String>> getConversations(final String locationPattern, final String protocolPattern, final String lowerIPPattern, final String upperIPPattern, final String applicationPattern, final long limit, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getConversations(locationPattern, protocolPattern, lowerIPPattern, upperIPPattern, applicationPattern, limit, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getTopNConversationSummaries(n, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(final Set<String> conversations, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getConversationSummaries(conversations, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(final Set<String> conversations, final long step, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getConversationSeries(conversations, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getTopNConversationSeries(n, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<String>> getHosts(final String regex, final long limit, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getHosts(regex, limit, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getTopNHostSummaries(n, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(final Set<String> hosts, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getHostSummaries(hosts, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(final Set<String> hosts, final long step, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, true, qs -> qs.getHostSeries(hosts, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getTopNHostSeries(n, step, includeOther, filters));
    }

    @Override
    public CompletableFuture<List<String>> getFieldValues(final LimitedCardinalityField field, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getFieldValues(field, filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(final LimitedCardinalityField field, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getFieldSummaries(field, filters));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(final LimitedCardinalityField field, final long step, final List<Filter> filters) {
        return runWithDelegate(filters, false, qs -> qs.getFieldSeries(field, step, filters));
    }

    // --- config setters (blueprint) ---
    public boolean isAlwaysUseAggForQueries() { return alwaysUseAggForQueries; }

    public void setAlwaysUseAggForQueries(final boolean alwaysUseAggForQueries) {
        this.alwaysUseAggForQueries = alwaysUseAggForQueries;
        if (alwaysUseAggForQueries) {
            this.alwaysUseRawForQueries = false;
        }
    }

    public boolean isAlwaysUseRawForQueries() { return alwaysUseRawForQueries; }

    public void setAlwaysUseRawForQueries(final boolean alwaysUseRawForQueries) {
        this.alwaysUseRawForQueries = alwaysUseRawForQueries;
        if (alwaysUseRawForQueries) {
            this.alwaysUseAggForQueries = false;
        }
    }

    public void setTimeRangeDurationAggregateThresholdMs(final long v) { this.timeRangeDurationAggregateThresholdMs = v; }
    public void setTimeRangeEndpointAggregateThresholdMs(final long v) { this.timeRangeEndpointAggregateThresholdMs = v; }

    private static <T> CompletableFuture<T> timeAsync(final Timer successTimer, final Timer failureTimer,
                                                      final Callable<CompletableFuture<T>> operation) {
        final Timer.Context successContext = successTimer.time();
        final Timer.Context failureContext = failureTimer.time();
        try {
            final CompletableFuture<T> promise = new CompletableFuture<>();
            final CompletableFuture<T> future = operation.call();
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
        } catch (final Exception ex) {
            failureContext.stop();
            throw new RuntimeException(ex);
        }
    }
}
