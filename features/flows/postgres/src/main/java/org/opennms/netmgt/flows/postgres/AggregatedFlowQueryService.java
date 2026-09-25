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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.DscpFilter;
import org.opennms.netmgt.flows.filter.api.ExporterNodeFilter;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.processing.ConversationKeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * A {@link FlowQueryService} that answers queries from the pre-aggregated {@code flow_agg} table.
 *
 * <p>It is the counterpart to the Elasticsearch {@code AggregatedFlowQueryService}, and is meant to sit
 * behind {@link PostgresSmartQueryService}, which only routes to it the queries aggregates can answer
 * (non-specific-entity, time-bounded). It serves the <em>topN summaries and topN series</em> for
 * application/conversation/host, plus <em>DSCP field</em> values/summaries/series (from the with-TOS
 * INTERFACE rows), directly from {@code flow_agg}. Flow count, non-DSCP fields, entity listings, and
 * explicit-set queries are delegated to the raw {@link FlowQueryService}. When no DataSource is
 * configured or a query carries a filter the aggregates cannot honor, it also falls back to the delegate.
 *
 * <p>DSCP scope mirrors the ES TOS/non-TOS split: a {@link DscpFilter} selects the with-TOS rows
 * ({@code dscp IN (...)}); its absence selects the without-TOS rollup ({@code dscp IS NULL}). "Other" is
 * reconstructed from the dimension's own total (top-K rows + the stored null-key Other rows) minus the
 * selected entries, so it stays exact per dimension.
 */
public class AggregatedFlowQueryService implements FlowQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatedFlowQueryService.class);
    private static final String OTHER = "Other";

    private final FlowQueryService delegate;
    private int threads = 4;
    private FlowDataSourceProvider dataSourceProvider;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private ExecutorService executor;

    public AggregatedFlowQueryService(final FlowQueryService delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    public void start() {
        if (this.dataSource == null && this.dataSourceProvider != null) {
            this.dataSource = this.dataSourceProvider.getDataSource();
        }
        if (this.dataSource == null) {
            LOG.warn("AggregatedFlowQueryService has no flow DataSource; all queries will fall back to the raw service.");
            return;
        }
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.executor = Executors.newFixedThreadPool(threads, r -> {
            final Thread t = new Thread(r, "postgres-flow-agg-query");
            t.setDaemon(true);
            return t;
        });
        LOG.info("AggregatedFlowQueryService started (threads={}).", threads);
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    // ---- served from flow_agg (fall back to delegate when not servable) ----

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        return w == null ? delegate.getTopNApplicationSummaries(n, includeOther, filters)
                : async(() -> summaries("APPLICATION", n, includeOther, w, s -> s));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        return w == null ? delegate.getTopNConversationSummaries(n, includeOther, filters)
                : async(() -> summaries("CONVERSATION", n, includeOther, w, AggregatedFlowQueryService::toConversation));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        return w == null ? delegate.getTopNHostSummaries(n, includeOther, filters)
                : async(() -> summaries("HOST", n, includeOther, w, AggregatedFlowQueryService::toHost));
    }

    // ---- delegated (not yet served from aggregates, or inherently raw-only) ----

    // Flow count is delegated to raw on purpose: flow_agg stores no per-flow count, and counting per
    // window would overcount flows that span multiple windows. The raw COUNT(*) is exact and partition-pruned.
    @Override
    public CompletableFuture<Long> getFlowCount(final List<Filter> filters) {
        return delegate.getFlowCount(filters);
    }

    @Override
    public CompletableFuture<List<String>> getApplications(final String matchingPrefix, final long limit, final List<Filter> filters) {
        return delegate.getApplications(matchingPrefix, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(final Set<String> applications, final boolean includeOther, final List<Filter> filters) {
        return delegate.getApplicationSummaries(applications, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(final Set<String> applications, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate.getApplicationSeries(applications, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        return w == null ? delegate.getTopNApplicationSeries(n, step, includeOther, filters)
                : async(() -> series("APPLICATION", n, includeOther, step, w, s -> s));
    }

    @Override
    public CompletableFuture<List<String>> getConversations(final String locationPattern, final String protocolPattern, final String lowerIPPattern, final String upperIPPattern, final String applicationPattern, final long limit, final List<Filter> filters) {
        return delegate.getConversations(locationPattern, protocolPattern, lowerIPPattern, upperIPPattern, applicationPattern, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(final Set<String> conversations, final boolean includeOther, final List<Filter> filters) {
        return delegate.getConversationSummaries(conversations, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(final Set<String> conversations, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate.getConversationSeries(conversations, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        return w == null ? delegate.getTopNConversationSeries(n, step, includeOther, filters)
                : async(() -> series("CONVERSATION", n, includeOther, step, w, AggregatedFlowQueryService::toConversation));
    }

    @Override
    public CompletableFuture<List<String>> getHosts(final String regex, final long limit, final List<Filter> filters) {
        return delegate.getHosts(regex, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(final Set<String> hosts, final boolean includeOther, final List<Filter> filters) {
        return delegate.getHostSummaries(hosts, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(final Set<String> hosts, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate.getHostSeries(hosts, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        return w == null ? delegate.getTopNHostSeries(n, step, includeOther, filters)
                : async(() -> series("HOST", n, includeOther, step, w, AggregatedFlowQueryService::toHost));
    }

    // Only DSCP is available from aggregates (the with-TOS INTERFACE rows); other fields delegate to raw.
    @Override
    public CompletableFuture<List<String>> getFieldValues(final LimitedCardinalityField field, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        if (field != LimitedCardinalityField.DSCP || w == null) {
            return delegate.getFieldValues(field, filters);
        }
        return async(() -> {
            final List<String> out = new ArrayList<>();
            jdbcTemplate.query("SELECT DISTINCT dscp FROM flow_agg WHERE dimension = 'INTERFACE'"
                    + scopeBase(w) + dscpFieldScope(w) + " ORDER BY dscp",
                    rs -> { out.add(rs.getString(1)); });
            return out;
        });
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(final LimitedCardinalityField field, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        if (field != LimitedCardinalityField.DSCP || w == null) {
            return delegate.getFieldSummaries(field, filters);
        }
        return async(() -> {
            final List<TrafficSummary<String>> out = new ArrayList<>();
            jdbcTemplate.query("SELECT dscp::text AS entity, COALESCE(SUM(bytes_in),0) AS bin,"
                    + " COALESCE(SUM(bytes_out),0) AS bout, bool_or(congestion_encountered) AS cong,"
                    + " bool_or(non_ecn_capable_transport) AS nonect"
                    + " FROM flow_agg WHERE dimension = 'INTERFACE'" + scopeBase(w) + dscpFieldScope(w)
                    + " GROUP BY dscp ORDER BY (SUM(bytes_in)+SUM(bytes_out)) DESC", rs -> {
                final InOut io = new InOut();
                io.in = rs.getLong("bin");
                io.out = rs.getLong("bout");
                io.cong = rs.getBoolean("cong");
                io.nonect = rs.getBoolean("nonect");
                out.add(summary(rs.getString("entity"), io));
            });
            return out;
        });
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(final LimitedCardinalityField field, final long step, final List<Filter> filters) {
        final AggWhere w = servable(filters);
        if (field != LimitedCardinalityField.DSCP || w == null) {
            return delegate.getFieldSeries(field, step, filters);
        }
        return async(() -> {
            final long s = step > 0 ? step : 1L;
            final String bucket = "(((extract(epoch from window_start) * 1000)::bigint) / " + s + ") * " + s;
            final Table<Directional<String>, Long, Double> table = HashBasedTable.create();
            jdbcTemplate.query("SELECT dscp::text AS entity, " + bucket + " AS bucket_ms,"
                    + " SUM(bytes_in) AS bin, SUM(bytes_out) AS bout"
                    + " FROM flow_agg WHERE dimension = 'INTERFACE'" + scopeBase(w) + dscpFieldScope(w)
                    + " GROUP BY entity, bucket_ms", rs -> {
                final String e = rs.getString("entity");
                final long b = rs.getLong("bucket_ms");
                final long bin = rs.getLong("bin");
                final long bout = rs.getLong("bout");
                if (bin != 0) table.put(new Directional<>(e, true), b, (double) bin);
                if (bout != 0) table.put(new Directional<>(e, false), b, (double) bout);
            });
            return table;
        });
    }

    // ---- aggregation query implementation ----

    private <T> CompletableFuture<T> async(final Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    /** Parse into an aggregate WHERE, or {@code null} if the aggregates cannot honor this filter set. */
    private AggWhere servable(final List<Filter> filters) {
        if (executor == null) {
            return null; // no DataSource -> not started -> fall back
        }
        final AggWhere w = new AggWhere();
        boolean haveTime = false;
        for (final Filter filter : filters) {
            if (filter instanceof TimeRangeFilter) {
                final TimeRangeFilter t = (TimeRangeFilter) filter;
                w.start = t.getStart();
                w.end = t.getEnd();
                haveTime = true;
            } else if (filter instanceof SnmpInterfaceIdFilter) {
                w.ifIndex = ((SnmpInterfaceIdFilter) filter).getSnmpInterfaceId();
            } else if (filter instanceof DscpFilter) {
                w.dscp = ((DscpFilter) filter).getDscp();
            } else if (filter instanceof ExporterNodeFilter) {
                final org.opennms.netmgt.flows.filter.api.NodeCriteria c = ((ExporterNodeFilter) filter).getCriteria();
                if (c.getNodeId() == null) {
                    return null; // flow_agg has no foreign-source/id columns; let the raw service handle it
                }
                w.exporterNodeId = c.getNodeId();
            } else {
                return null; // unknown filter type -> not servable from aggregates
            }
        }
        return haveTime ? w : null;
    }

    /** Full scope for a per-entity dimension query: range + exporter/interface + TOS scope. */
    private String scopeSql(final AggWhere w) {
        return scopeBase(w) + dscpScope(w);
    }

    /** Range + optional exporter/interface, without any DSCP predicate. */
    private String scopeBase(final AggWhere w) {
        final StringBuilder sb = new StringBuilder();
        sb.append(" AND window_start >= to_timestamp(").append(w.start).append("/1000.0)")
          .append(" AND window_start < to_timestamp(").append(w.end).append("/1000.0)");
        if (w.exporterNodeId != null) {
            sb.append(" AND exporter_node_id = ").append(w.exporterNodeId.intValue());
        }
        if (w.ifIndex != null) {
            sb.append(" AND if_index = ").append(w.ifIndex.intValue());
        }
        return sb.toString();
    }

    private String dscpIn(final AggWhere w) {
        final StringBuilder sb = new StringBuilder(" AND dscp IN (");
        for (int i = 0; i < w.dscp.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(w.dscp.get(i).intValue());
        }
        return sb.append(')').toString();
    }

    /** TOS scope for dimension queries: an explicit DscpFilter selects with-TOS rows; else the rollup. */
    private String dscpScope(final AggWhere w) {
        return (w.dscp != null && !w.dscp.isEmpty()) ? dscpIn(w) : " AND dscp IS NULL";
    }

    /** TOS scope for a per-DSCP field breakdown: all with-TOS rows (optionally restricted by DscpFilter). */
    private String dscpFieldScope(final AggWhere w) {
        return (w.dscp != null && !w.dscp.isEmpty()) ? dscpIn(w) : " AND dscp IS NOT NULL";
    }

    private <T> List<TrafficSummary<T>> summaries(final String dimension, final int topN, final boolean includeOther,
                                                  final AggWhere w, final Function<String, T> toEntity) {
        final String scope = scopeSql(w);
        final StringBuilder sql = new StringBuilder("SELECT grouped_by_key AS entity,")
                .append(" COALESCE(SUM(bytes_in),0) AS bin, COALESCE(SUM(bytes_out),0) AS bout,")
                .append(" bool_or(congestion_encountered) AS cong, bool_or(non_ecn_capable_transport) AS nonect")
                .append(" FROM flow_agg WHERE dimension = '").append(dimension).append('\'').append(scope)
                .append(" AND grouped_by_key IS NOT NULL")
                .append(" GROUP BY grouped_by_key ORDER BY (SUM(bytes_in)+SUM(bytes_out)) DESC");
        if (topN > 0) {
            sql.append(" LIMIT ").append(topN);
        }
        final Map<String, InOut> selected = new LinkedHashMap<>();
        jdbcTemplate.query(sql.toString(), rs -> {
            final InOut io = new InOut();
            io.in = rs.getLong("bin");
            io.out = rs.getLong("bout");
            io.cong = rs.getBoolean("cong");
            io.nonect = rs.getBoolean("nonect");
            selected.put(rs.getString("entity"), io);
        });

        final List<TrafficSummary<T>> out = new ArrayList<>();
        long selIn = 0;
        long selOut = 0;
        for (final Map.Entry<String, InOut> e : selected.entrySet()) {
            out.add(summary(toEntity.apply(e.getKey()), e.getValue()));
            selIn += e.getValue().in;
            selOut += e.getValue().out;
        }
        if (includeOther) {
            // Dimension total = every row of this dimension (top-K + the stored null-key Other rows).
            final InOut total = dimensionTotal(dimension, scope);
            final InOut other = new InOut();
            other.in = Math.max(0, total.in - selIn);
            other.out = Math.max(0, total.out - selOut);
            out.add(summary(toEntity.apply(OTHER), other));
        }
        return out;
    }

    /**
     * Time series for the top-N entities of a dimension over the requested {@code step}, plus an "Other"
     * series. Each flow_agg window is assigned to the step bucket containing its start (epoch-aligned,
     * matching the raw service); the aggregation window size is the finest resolution available.
     */
    private <T> Table<Directional<T>, Long, Double> series(final String dimension, final int topN,
            final boolean includeOther, final long step, final AggWhere w, final Function<String, T> toEntity) {
        final long s = step > 0 ? step : 1L;
        final String scope = scopeSql(w);
        final String bucket = "(((extract(epoch from window_start) * 1000)::bigint) / " + s + ") * " + s;

        // 1. the top-N entities over the whole range
        final List<String> keys = new ArrayList<>();
        final StringBuilder keySql = new StringBuilder("SELECT grouped_by_key FROM flow_agg WHERE dimension = '")
                .append(dimension).append('\'').append(scope)
                .append(" AND grouped_by_key IS NOT NULL GROUP BY grouped_by_key")
                .append(" ORDER BY (SUM(bytes_in)+SUM(bytes_out)) DESC");
        if (topN > 0) {
            keySql.append(" LIMIT ").append(topN);
        }
        jdbcTemplate.query(keySql.toString(), rs -> { keys.add(rs.getString(1)); });

        final Table<Directional<T>, Long, Double> table = HashBasedTable.create();
        final Map<Long, long[]> selectedByBucket = new HashMap<>(); // bucket -> [in,out] over selected keys

        // 2. per selected entity, per bucket
        if (!keys.isEmpty()) {
            final StringBuilder in = new StringBuilder();
            final List<Object> params = new ArrayList<>();
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) in.append(',');
                in.append('?');
                params.add(keys.get(i));
            }
            final String sql = "SELECT grouped_by_key AS entity, " + bucket + " AS bucket_ms,"
                    + " SUM(bytes_in) AS bin, SUM(bytes_out) AS bout"
                    + " FROM flow_agg WHERE dimension = '" + dimension + "'" + scope
                    + " AND grouped_by_key IN (" + in + ")"
                    + " GROUP BY entity, bucket_ms";
            jdbcTemplate.query(sql, rs -> {
                final T entity = toEntity.apply(rs.getString("entity"));
                final long b = rs.getLong("bucket_ms");
                final long bin = rs.getLong("bin");
                final long bout = rs.getLong("bout");
                if (bin != 0) table.put(new Directional<>(entity, true), b, (double) bin);
                if (bout != 0) table.put(new Directional<>(entity, false), b, (double) bout);
                final long[] acc = selectedByBucket.computeIfAbsent(b, k -> new long[2]);
                acc[0] += bin;
                acc[1] += bout;
            }, params.toArray());
        }

        // 3. Other per bucket = (dimension total in bucket) - (selected in bucket)
        if (includeOther) {
            final T other = toEntity.apply(OTHER);
            final String totalSql = "SELECT " + bucket + " AS bucket_ms, SUM(bytes_in) AS bin, SUM(bytes_out) AS bout"
                    + " FROM flow_agg WHERE dimension = '" + dimension + "'" + scope
                    + " GROUP BY bucket_ms";
            jdbcTemplate.query(totalSql, rs -> {
                final long b = rs.getLong("bucket_ms");
                final long[] sel = selectedByBucket.getOrDefault(b, new long[2]);
                final long oin = Math.max(0, rs.getLong("bin") - sel[0]);
                final long oout = Math.max(0, rs.getLong("bout") - sel[1]);
                if (oin != 0) table.put(new Directional<>(other, true), b, (double) oin);
                if (oout != 0) table.put(new Directional<>(other, false), b, (double) oout);
            });
        }
        return table;
    }

    private InOut dimensionTotal(final String dimension, final String scope) {
        final String sql = "SELECT COALESCE(SUM(bytes_in),0) AS bin, COALESCE(SUM(bytes_out),0) AS bout"
                + " FROM flow_agg WHERE dimension = '" + dimension + "'" + scope;
        final InOut io = new InOut();
        jdbcTemplate.query(sql, rs -> {
            io.in = rs.getLong("bin");
            io.out = rs.getLong("bout");
        });
        return io;
    }

    private static <T> TrafficSummary<T> summary(final T entity, final InOut io) {
        return TrafficSummary.from(entity)
                .withBytes(io.in, io.out)
                .withCongestionEncountered(io.cong)
                .withNonEcnCapableTransport(io.nonect)
                .build();
    }

    private static Conversation toConversation(final String key) {
        return OTHER.equals(key) ? Conversation.forOther().build()
                : Conversation.from(ConversationKeyUtils.fromJsonString(key)).build();
    }

    private static Host toHost(final String ip) {
        return OTHER.equals(ip) ? Host.forOther().build() : Host.from(ip).build();
    }

    private static final class AggWhere {
        long start;
        long end;
        Integer exporterNodeId;
        Integer ifIndex;
        List<Integer> dscp; // null/empty -> without-TOS scope
    }

    private static final class InOut {
        long in;
        long out;
        boolean cong;
        boolean nonect;
    }

    // --- config setters (blueprint) ---
    public void setThreads(final int threads) { this.threads = threads; }
    public void setDataSourceProvider(final FlowDataSourceProvider dataSourceProvider) { this.dataSourceProvider = dataSourceProvider; }
    public void setDataSource(final DataSource dataSource) { this.dataSource = dataSource; }
}
