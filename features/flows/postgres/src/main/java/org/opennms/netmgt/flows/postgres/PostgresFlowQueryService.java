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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.ConversationKey;
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
 * Prototype {@link FlowQueryService} backed by the PostgreSQL {@code flow} table.
 *
 * <p>Replicates the Elasticsearch {@code proportional_sum} aggregation in SQL: a document's bytes
 * (scaled by its sampling interval) are distributed across the query window / time-series buckets
 * in proportion to the temporal overlap of {@code [delta_switched, last_switched]}, with the
 * <em>full</em> flow duration as the denominator (so a flow straddling the window contributes only
 * its in-window fraction) and a zero-duration flow contributing its whole value to a single bucket.
 *
 * <p>Not tuned for scale (functional parity first): generate_series bucket expansion, no COPY.
 */
public class PostgresFlowQueryService implements FlowQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresFlowQueryService.class);

    private static final String OTHER = "Other";

    // The two proration expressions below are the SQL rendering of FlowProration (pure-Java mirror),
    // which the proportional-sum parity test checks against the drift-plugin ProportionalSumAggregator.
    // Keep the three in sync.

    /**
     * bytes * effectiveSampling, distributed by overlap of [delta_switched,last_switched] with the
     * single summary bucket [{s},{e}) (half-open at {e}, matching the drift plugin's histogram bucket).
     * The sampling factor is applied only when finite and non-zero (0/NaN/±Inf -> x1).
     */
    private static String proratedForWindow(final long s, final long e) {
        return "(COALESCE(f.bytes,0) * " + SAMPLING + ") * " +
               "CASE WHEN f.last_switched <= f.delta_switched " +
               "     THEN (CASE WHEN f.delta_switched >= " + s + " AND f.delta_switched < " + e + " THEN 1.0 ELSE 0.0 END) " +
               "ELSE GREATEST(0, LEAST(f.last_switched, " + e + ") - GREATEST(f.delta_switched, " + s + "))::float8 " +
               "/ (f.last_switched - f.delta_switched) END";
    }

    /** Per-(row,bucket) contribution for a series with the given step, bucket = [b.bucket_ms, b.bucket_ms+step). */
    private static String proratedForBucket(final long step) {
        return "(COALESCE(f.bytes,0) * " + SAMPLING + ") * " +
               "CASE WHEN f.last_switched <= f.delta_switched THEN 1.0 " +
               "ELSE GREATEST(0, LEAST(f.last_switched, b.bucket_ms + " + step + ") - GREATEST(f.delta_switched, b.bucket_ms))::float8 " +
               "/ (f.last_switched - f.delta_switched) END";
    }

    /** Sampling multiplier: the stored interval when finite and non-zero, otherwise 1 (no scaling). */
    private static final String SAMPLING =
            "(CASE WHEN f.sampling_interval IS NULL OR f.sampling_interval = 0 " +
            "OR f.sampling_interval <> f.sampling_interval " +               // NaN
            "OR f.sampling_interval = 'Infinity'::float8 OR f.sampling_interval = '-Infinity'::float8 " +
            "THEN 1 ELSE f.sampling_interval END)";

    private int threads = 4;

    private FlowDataSourceProvider dataSourceProvider;
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private ExecutorService executor;

    public void start() {
        if (this.dataSource == null && this.dataSourceProvider != null) {
            this.dataSource = this.dataSourceProvider.getDataSource();
        }
        if (this.dataSource == null) {
            // No flow DataSource configured (see FlowDataSourceProvider). Stay inert rather than failing
            // the blueprint container: the feature loads; query methods complete exceptionally if called.
            LOG.error("PostgresFlowQueryService not started: no flow DataSource is configured. Flow queries "
                    + "will NOT be served by PostgreSQL until datasource.url is set on the "
                    + "org.opennms.features.flows.persistence.postgres pid.");
            return;
        }
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.executor = Executors.newFixedThreadPool(threads, r -> {
            final Thread t = new Thread(r, "postgres-flow-query");
            t.setDaemon(true);
            return t;
        });
        LOG.info("PostgresFlowQueryService started (threads={}).", threads);
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private <T> CompletableFuture<T> async(final Supplier<T> supplier) {
        if (executor == null) {
            // Inert (no DataSource configured): fail the query cleanly instead of NPEing.
            final CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalStateException("PostgresFlowQueryService is not configured: "
                    + "no flow DataSource (set datasource.url on the org.opennms.features.flows.persistence.postgres pid)."));
            return failed;
        }
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    // ---------------------------------------------------------------------
    // Filter -> WHERE
    // ---------------------------------------------------------------------

    /** Accumulated WHERE clause + bind params + the required time window. */
    private static final class Where {
        final StringBuilder sql = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        long start;
        long end;
        Integer snmpInterfaceId;
    }

    private static Where where(final List<Filter> filters) {
        final Where w = new Where();
        boolean haveTime = false;
        for (final Filter filter : filters) {
            if (filter instanceof TimeRangeFilter) {
                final TimeRangeFilter t = (TimeRangeFilter) filter;
                w.start = t.getStart();
                w.end = t.getEnd();
                haveTime = true;
                // Row selection on flow_ts (@timestamp equivalent) -> partition pruning.
                w.sql.append(" AND f.flow_ts >= to_timestamp(").append(w.start).append("/1000.0)")
                     .append(" AND f.flow_ts <= to_timestamp(").append(w.end).append("/1000.0)");
            } else if (filter instanceof SnmpInterfaceIdFilter) {
                final int id = ((SnmpInterfaceIdFilter) filter).getSnmpInterfaceId();
                w.snmpInterfaceId = id;
                w.sql.append(" AND (f.input_snmp = ").append(id).append(" OR f.output_snmp = ").append(id).append(")");
            } else if (filter instanceof DscpFilter) {
                final List<Integer> dscp = ((DscpFilter) filter).getDscp();
                if (dscp != null && !dscp.isEmpty()) {
                    final StringBuilder in = new StringBuilder();
                    for (int i = 0; i < dscp.size(); i++) {
                        if (i > 0) in.append(',');
                        in.append(dscp.get(i).intValue());
                    }
                    w.sql.append(" AND f.dscp IN (").append(in).append(")");
                }
            } else if (filter instanceof ExporterNodeFilter) {
                final org.opennms.netmgt.flows.filter.api.NodeCriteria c = ((ExporterNodeFilter) filter).getCriteria();
                if (c.getNodeId() != null) {
                    w.sql.append(" AND f.exporter_node_id = ").append(c.getNodeId().intValue());
                } else {
                    w.sql.append(" AND f.document->'node_exporter'->>'foreign_source' = ?")
                         .append(" AND f.document->'node_exporter'->>'foreign_id' = ?");
                    w.params.add(c.getForeignSource());
                    w.params.add(c.getForeignId());
                }
            }
        }
        if (!haveTime) {
            throw new IllegalArgumentException("A TimeRangeFilter is required.");
        }
        return w;
    }

    // ---------------------------------------------------------------------
    // Flow count
    // ---------------------------------------------------------------------

    @Override
    public CompletableFuture<Long> getFlowCount(final List<Filter> filters) {
        return async(() -> {
            final Where w = where(filters);
            final String sql = "SELECT count(*) FROM flow f WHERE 1=1" + w.sql;
            final Long n = jdbcTemplate.queryForObject(sql, Long.class, w.params.toArray());
            return n != null ? n : 0L;
        });
    }

    // ---------------------------------------------------------------------
    // Generic summary/series over a group expression
    // ---------------------------------------------------------------------

    private static final class InOut {
        long in;
        long out;
        boolean congestionEncountered;
        boolean nonEcnCapableTransport;
    }

    // ECN parity: a flow marks Congestion Encountered when its ecn codepoint is 3 (CE), and
    // Not-ECT (non-ECN-capable transport) when its ecn codepoint is 0. Elastic's query builder
    // OVERWRITES these per direction bucket (order-dependent); we take the union (bool_or) across
    // the entity's flows, which is the intended "did any flow see CE / Not-ECT?" semantics.
    private static final String ECN_CONGESTION = "bool_or((f.document->>'netflow.ecn') = '3')";
    private static final String ECN_NON_ECT = "bool_or((f.document->>'netflow.ecn') = '0')";

    // Direction handling, EXACTLY as ElasticFlowRepository / RawFlowQueryService:
    //  * With NO SnmpInterfaceIdFilter, the Elastic query template constrains the aggregation to
    //    netflow.direction in {ingress,egress}, so UNKNOWN-direction flows are excluded from the whole
    //    summary/series (grouping, sums and ECN alike) -> directionConstraint() renders that filter.
    //  * With a SnmpInterfaceIdFilter, Elastic's unknownDirectionScript reclassifies an UNKNOWN flow to
    //    ingress when input_snmp matches the interface, else egress when output_snmp matches (an already
    //    explicit ingress/egress is kept), and nothing is dropped -> effectiveDirection() renders that CASE.
    //  RawFlowQueryService.isIngress() then maps the resulting direction ingress->in / egress->out and
    //  throws on anything else; here accumulate() simply skips anything that is not INGRESS/EGRESS.

    /** The direction a row is attributed to: the stored value, or (under an interface filter) UNKNOWN reclassified. */
    private static String effectiveDirection(final Where w) {
        if (w.snmpInterfaceId == null) {
            return "f.direction";
        }
        final int id = w.snmpInterfaceId;
        return "(CASE WHEN f.direction <> 'UNKNOWN' THEN f.direction " +
               "WHEN f.input_snmp = " + id + " THEN 'INGRESS' " +
               "WHEN f.output_snmp = " + id + " THEN 'EGRESS' END)";
    }

    /** Extra WHERE that drops UNKNOWN-direction flows when there is no interface filter to reclassify them. */
    private static String directionConstraint(final Where w) {
        return w.snmpInterfaceId == null ? " AND f.direction IN ('INGRESS','EGRESS')" : "";
    }

    /**
     * Returns entity -> (bytesIn,bytesOut) prorated over the window, ordered by total desc.
     * @param entityExpr SQL expression producing the group key (aliased column)
     * @param fromExtra  extra FROM/JOIN (e.g. host unnest); may be empty
     * @param topN       if > 0, limit to the top N by total bytes; else all (subject to explicit)
     * @param explicit   if non-null, restrict to these entity keys
     */
    private Map<String, InOut> summaries(final String entityExpr, final String fromExtra,
                                         final Where w, final Integer topN, final Set<String> explicit) {
        final long s = w.start;
        final long e = w.end;
        final String prorated = proratedForWindow(s, e);
        final String effDir = effectiveDirection(w);
        final List<Object> params = new ArrayList<>(w.params);
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(entityExpr).append(" AS entity,")
           .append(" COALESCE(SUM(").append(prorated).append(") FILTER (WHERE ").append(effDir).append(" = 'INGRESS'),0) AS bin,")
           .append(" COALESCE(SUM(").append(prorated).append(") FILTER (WHERE ").append(effDir).append(" = 'EGRESS'),0) AS bout,")
           .append(' ').append(ECN_CONGESTION).append(" AS cong,")
           .append(' ').append(ECN_NON_ECT).append(" AS nonect")
           .append(" FROM flow f").append(fromExtra)
           .append(" WHERE 1=1").append(w.sql).append(directionConstraint(w))
           .append(" AND ").append(entityExpr).append(" IS NOT NULL");
        if (explicit != null) {
            sql.append(" AND ").append(entityExpr).append(" IN (");
            int i = 0;
            for (final String key : explicit) {
                if (i++ > 0) sql.append(',');
                sql.append('?');
                params.add(key);
            }
            sql.append(')');
        }
        sql.append(" GROUP BY entity ORDER BY (COALESCE(SUM(").append(prorated).append("),0)) DESC");
        if (topN != null && topN > 0) {
            sql.append(" LIMIT ").append(topN);
        }
        final Map<String, InOut> out = new LinkedHashMap<>();
        jdbcTemplate.query(sql.toString(), rs -> {
            final InOut io = new InOut();
            io.in = rs.getLong("bin");
            io.out = rs.getLong("bout");
            io.congestionEncountered = rs.getBoolean("cong");
            io.nonEcnCapableTransport = rs.getBoolean("nonect");
            out.put(rs.getString("entity"), io);
        }, params.toArray());
        return out;
    }

    /** Total prorated bytes over the whole window (for the "Other" bucket). */
    private InOut totals(final String fromExtra, final Where w, final String entityNotNull) {
        final long s = w.start;
        final long e = w.end;
        final String prorated = proratedForWindow(s, e);
        final String effDir = effectiveDirection(w);
        final String sql = "SELECT " +
                " COALESCE(SUM(" + prorated + ") FILTER (WHERE " + effDir + " = 'INGRESS'),0) AS bin," +
                " COALESCE(SUM(" + prorated + ") FILTER (WHERE " + effDir + " = 'EGRESS'),0) AS bout" +
                " FROM flow f" + fromExtra + " WHERE 1=1" + w.sql + directionConstraint(w) +
                (entityNotNull != null ? " AND " + entityNotNull : "");
        final InOut io = new InOut();
        jdbcTemplate.query(sql, rs -> {
            io.in = rs.getLong("bin");
            io.out = rs.getLong("bout");
        }, w.params.toArray());
        return io;
    }

    private static void addOther(final Map<String, InOut> ordered, final InOut total) {
        long sumIn = 0, sumOut = 0;
        for (final InOut io : ordered.values()) {
            sumIn += io.in;
            sumOut += io.out;
        }
        final InOut other = new InOut();
        other.in = Math.max(0, total.in - sumIn);
        other.out = Math.max(0, total.out - sumOut);
        ordered.put(OTHER, other);
    }

    /** Histogram bucket alignment origin. Elastic's proportional_sum always uses offset 0 (epoch-aligned). */
    private static final long DEFAULT_ORIGIN = 0L;

    /**
     * FROM ... CROSS JOIN LATERAL that expands each row into its overlapping buckets. Buckets align to
     * {@code origin} + k*{@code step} (floor toward -inf, matching date_bin / the drift plugin's rounding);
     * with {@code origin == 0} this is epoch-aligned, which is Elastic's only mode.
     */
    private static String seriesLateral(final String fromExtra, final long step, final long origin,
                                        final long s, final long e) {
        return " FROM flow f" + fromExtra +
               " CROSS JOIN LATERAL generate_series(" +
               "   (FLOOR((GREATEST(f.delta_switched, " + s + ") - " + origin + ")::numeric / " + step + ") * "
                    + step + " + " + origin + ")::bigint," +
               "   LEAST(f.last_switched, " + e + ")," +
               "   " + step + ") AS b(bucket_ms)";
    }

    private void accumulate(final Table<String, Long, double[]> table, final String entity,
                            final String direction, final long bucket, final double v) {
        // Mirror RawFlowQueryService.isIngress: ingress -> in, egress -> out, anything else not
        // attributable. UNKNOWN is excluded upstream (no interface filter) or reclassified to
        // ingress/egress (interface filter present), so it never reaches here.
        final int idx;
        if ("INGRESS".equals(direction)) {
            idx = 0;
        } else if ("EGRESS".equals(direction)) {
            idx = 1;
        } else {
            return;
        }
        double[] cell = table.get(entity, bucket);
        if (cell == null) {
            cell = new double[2];
            table.put(entity, bucket, cell);
        }
        cell[idx] += v;
    }

    /**
     * entity/direction/bucket -> summed prorated value, for a series. When {@code includeOther}, an
     * extra {@link #OTHER} row aggregates every in-window row whose key is NOT among the selected keys
     * (mirroring Elastic's must_not "others" query — always non-negative, no cross-bucket subtraction).
     */
    private Table<String, Long, double[]> series(final String entityExpr, final String fromExtra,
                                                 final Where w, final long step, final long origin,
                                                 final Integer topN, final Set<String> explicit,
                                                 final boolean includeOther) {
        // Restrict the series to the same entities the summary would select (top N or explicit set).
        final Set<String> keys = (explicit != null) ? explicit : summaries(entityExpr, fromExtra, w, topN, null).keySet();
        final Table<String, Long, double[]> table = HashBasedTable.create();
        final long s = w.start;
        final long e = w.end;
        final String bucketExpr = proratedForBucket(step);

        if (!keys.isEmpty()) {
            final List<Object> params = new ArrayList<>(w.params);
            final StringBuilder in = new StringBuilder();
            int i = 0;
            for (final String key : keys) {
                if (i++ > 0) in.append(',');
                in.append('?');
                params.add(key);
            }
            final String sql =
                    "SELECT entity, direction, bucket_ms, SUM(contribution) AS v FROM (" +
                    "  SELECT " + entityExpr + " AS entity, " + effectiveDirection(w) + " AS direction, b.bucket_ms AS bucket_ms, " +
                         bucketExpr + " AS contribution" +
                         seriesLateral(fromExtra, step, origin, s, e) +
                    "  WHERE 1=1" + w.sql + directionConstraint(w) +
                    "    AND " + entityExpr + " IS NOT NULL" +
                    "    AND " + entityExpr + " IN (" + in + ")" +
                    ") t GROUP BY entity, direction, bucket_ms";
            jdbcTemplate.query(sql, rs -> { accumulate(table, rs.getString("entity"),
                    rs.getString("direction"), rs.getLong("bucket_ms"), rs.getDouble("v")); },
                    params.toArray());
        }

        if (includeOther) {
            final List<Object> params = new ArrayList<>(w.params);
            final StringBuilder notIn = new StringBuilder();
            for (final String key : keys) {
                notIn.append(notIn.length() == 0 ? "" : ",").append('?');
                params.add(key);
            }
            final String excludeSelected = keys.isEmpty() ? "" : " AND " + entityExpr + " NOT IN (" + notIn + ")";
            final String sql =
                    "SELECT direction, bucket_ms, SUM(contribution) AS v FROM (" +
                    "  SELECT " + effectiveDirection(w) + " AS direction, b.bucket_ms AS bucket_ms, " + bucketExpr + " AS contribution" +
                         seriesLateral(fromExtra, step, origin, s, e) +
                    "  WHERE 1=1" + w.sql + directionConstraint(w) +
                    "    AND " + entityExpr + " IS NOT NULL" + excludeSelected +
                    ") t GROUP BY direction, bucket_ms";
            jdbcTemplate.query(sql, rs -> { accumulate(table, OTHER,
                    rs.getString("direction"), rs.getLong("bucket_ms"), rs.getDouble("v")); },
                    params.toArray());
        }
        return table;
    }

    // ---------------------------------------------------------------------
    // Applications
    // ---------------------------------------------------------------------

    @Override
    public CompletableFuture<List<String>> getApplications(final String matchingPrefix, final long limit, final List<Filter> filters) {
        return async(() -> {
            final Where w = where(filters);
            final StringBuilder sql = new StringBuilder("SELECT DISTINCT f.application FROM flow f WHERE 1=1")
                    .append(w.sql).append(" AND f.application IS NOT NULL");
            final List<Object> params = new ArrayList<>(w.params);
            if (matchingPrefix != null && !matchingPrefix.isEmpty()) {
                sql.append(" AND f.application ILIKE ?");
                params.add(matchingPrefix + "%");
            }
            sql.append(" ORDER BY f.application");
            if (limit > 0) sql.append(" LIMIT ").append(limit);
            return jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
        });
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        return async(() -> stringSummaries("f.application", "", where(filters), n, null, includeOther, "f.application IS NOT NULL"));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(final Set<String> applications, final boolean includeOther, final List<Filter> filters) {
        return async(() -> stringSummaries("f.application", "", where(filters), null, applications, includeOther, "f.application IS NOT NULL"));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(final Set<String> applications, final long step, final boolean includeOther, final List<Filter> filters) {
        return async(() -> stringSeries("f.application", "", where(filters), step, null, applications, includeOther));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return async(() -> stringSeries("f.application", "", where(filters), step, n, null, includeOther));
    }

    // ---------------------------------------------------------------------
    // Conversations
    // ---------------------------------------------------------------------

    @Override
    public CompletableFuture<List<String>> getConversations(final String locationPattern, final String protocolPattern,
                                                            final String lowerIPPattern, final String upperIPPattern,
                                                            final String applicationPattern, final long limit, final List<Filter> filters) {
        return async(() -> {
            final Where w = where(filters);
            final String sql = "SELECT DISTINCT f.convo_key FROM flow f WHERE 1=1" + w.sql + " AND f.convo_key IS NOT NULL";
            final List<String> keys = jdbcTemplate.queryForList(sql, String.class, w.params.toArray());
            final List<String> result = new ArrayList<>();
            for (final String key : keys) {
                final ConversationKey ck = ConversationKeyUtils.fromJsonString(key);
                if (ck == null) continue;
                if (!matches(locationPattern, ck.getLocation())) continue;
                if (!matches(protocolPattern, String.valueOf(ck.getProtocol()))) continue;
                if (!matches(lowerIPPattern, ck.getLowerIp())) continue;
                if (!matches(upperIPPattern, ck.getUpperIp())) continue;
                if (!matches(applicationPattern, ck.getApplication())) continue;
                result.add(key);
                if (limit > 0 && result.size() >= limit) break;
            }
            return result;
        });
    }

    private static boolean matches(final String pattern, final String value) {
        if (pattern == null || pattern.isEmpty() || ".*".equals(pattern)) {
            return true;
        }
        return value != null && value.matches(pattern);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        return async(() -> conversationSummaries(where(filters), n, null, includeOther));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(final Set<String> conversations, final boolean includeOther, final List<Filter> filters) {
        return async(() -> conversationSummaries(where(filters), null, conversations, includeOther));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(final Set<String> conversations, final long step, final boolean includeOther, final List<Filter> filters) {
        return async(() -> mapSeries(series("f.convo_key", "", where(filters), step, DEFAULT_ORIGIN, null, conversations, includeOther), PostgresFlowQueryService::toConversation));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return async(() -> mapSeries(series("f.convo_key", "", where(filters), step, DEFAULT_ORIGIN, n, null, includeOther), PostgresFlowQueryService::toConversation));
    }

    private List<TrafficSummary<Conversation>> conversationSummaries(final Where w, final Integer topN, final Set<String> explicit, final boolean includeOther) {
        final Map<String, InOut> m = summaries("f.convo_key", "", w, topN, explicit);
        if (includeOther) addOther(m, totals("", w, "f.convo_key IS NOT NULL"));
        final List<TrafficSummary<Conversation>> out = new ArrayList<>();
        for (final Map.Entry<String, InOut> en : m.entrySet()) {
            out.add(summaryBuilder(toConversation(en.getKey()), en.getValue()).build());
        }
        return out;
    }

    private static Conversation toConversation(final String key) {
        if (OTHER.equals(key)) {
            return Conversation.forOther().build();
        }
        final ConversationKey ck = ConversationKeyUtils.fromJsonString(key);
        return Conversation.from(ck).build();
    }

    // ---------------------------------------------------------------------
    // Hosts (a flow contributes to BOTH its src and dst host)
    // ---------------------------------------------------------------------

    private static final String HOST_FROM = " CROSS JOIN LATERAL (VALUES (host(f.src_addr)), (host(f.dst_addr))) AS h(host)";
    private static final String HOST_EXPR = "h.host";

    @Override
    public CompletableFuture<List<String>> getHosts(final String regex, final long limit, final List<Filter> filters) {
        return async(() -> {
            final Where w = where(filters);
            final StringBuilder sql = new StringBuilder("SELECT DISTINCT " + HOST_EXPR + " FROM flow f" + HOST_FROM + " WHERE 1=1")
                    .append(w.sql).append(" AND ").append(HOST_EXPR).append(" IS NOT NULL");
            final List<Object> params = new ArrayList<>(w.params);
            if (regex != null && !regex.isEmpty() && !".*".equals(regex)) {
                sql.append(" AND ").append(HOST_EXPR).append(" ~ ?");
                params.add(regex);
            }
            sql.append(" ORDER BY ").append(HOST_EXPR);
            if (limit > 0) sql.append(" LIMIT ").append(limit);
            return jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
        });
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(final int n, final boolean includeOther, final List<Filter> filters) {
        return async(() -> hostSummaries(where(filters), n, null, includeOther));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(final Set<String> hosts, final boolean includeOther, final List<Filter> filters) {
        return async(() -> hostSummaries(where(filters), null, hosts, includeOther));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(final Set<String> hosts, final long step, final boolean includeOther, final List<Filter> filters) {
        return async(() -> mapSeries(series(HOST_EXPR, HOST_FROM, where(filters), step, DEFAULT_ORIGIN, null, hosts, includeOther), PostgresFlowQueryService::toHost));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return async(() -> mapSeries(series(HOST_EXPR, HOST_FROM, where(filters), step, DEFAULT_ORIGIN, n, null, includeOther), PostgresFlowQueryService::toHost));
    }

    private List<TrafficSummary<Host>> hostSummaries(final Where w, final Integer topN, final Set<String> explicit, final boolean includeOther) {
        final Map<String, InOut> m = summaries(HOST_EXPR, HOST_FROM, w, topN, explicit);
        if (includeOther) addOther(m, totals(HOST_FROM, w, HOST_EXPR + " IS NOT NULL"));
        final List<TrafficSummary<Host>> out = new ArrayList<>();
        for (final Map.Entry<String, InOut> en : m.entrySet()) {
            out.add(summaryBuilder(toHost(en.getKey()), en.getValue()).build());
        }
        return out;
    }

    private static Host toHost(final String ip) {
        return OTHER.equals(ip) ? Host.forOther().build() : Host.from(ip).build();
    }

    // ---------------------------------------------------------------------
    // Generic field queries (LimitedCardinalityField)
    // ---------------------------------------------------------------------

    private static String fieldExpr(final LimitedCardinalityField field) {
        // DSCP is promoted to a real column; any other limited-cardinality field falls back to its
        // jsonb value under the same netflow.* key the Elastic schema uses (field.fieldName).
        if (field == LimitedCardinalityField.DSCP) {
            return "f.dscp::text";
        }
        return "(f.document->>'" + field.fieldName + "')";
    }

    @Override
    public CompletableFuture<List<String>> getFieldValues(final LimitedCardinalityField field, final List<Filter> filters) {
        return async(() -> {
            final Where w = where(filters);
            final String expr = fieldExpr(field);
            final String sql = "SELECT DISTINCT " + expr + " FROM flow f WHERE 1=1" + w.sql + " AND " + expr + " IS NOT NULL ORDER BY 1";
            return jdbcTemplate.queryForList(sql, String.class, w.params.toArray());
        });
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(final LimitedCardinalityField field, final List<Filter> filters) {
        return async(() -> stringSummaries(fieldExpr(field), "", where(filters), null, null, false, fieldExpr(field) + " IS NOT NULL"));
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(final LimitedCardinalityField field, final long step, final List<Filter> filters) {
        return async(() -> {
            // All distinct field values become the "explicit" set (limited cardinality by definition).
            final Where w = where(filters);
            final String expr = fieldExpr(field);
            final List<String> values = jdbcTemplate.queryForList(
                    "SELECT DISTINCT " + expr + " FROM flow f WHERE 1=1" + w.sql + " AND " + expr + " IS NOT NULL",
                    String.class, w.params.toArray());
            return mapSeries(series(expr, "", w, step, DEFAULT_ORIGIN, null, new java.util.HashSet<>(values), false), s -> s);
        });
    }

    // ---------------------------------------------------------------------
    // String-entity summary/series assembly + Table mapping
    // ---------------------------------------------------------------------

    private List<TrafficSummary<String>> stringSummaries(final String entityExpr, final String fromExtra, final Where w,
                                                         final Integer topN, final Set<String> explicit,
                                                         final boolean includeOther, final String entityNotNull) {
        final Map<String, InOut> m = summaries(entityExpr, fromExtra, w, topN, explicit);
        if (includeOther) addOther(m, totals(fromExtra, w, entityNotNull));
        final List<TrafficSummary<String>> out = new ArrayList<>();
        for (final Map.Entry<String, InOut> en : m.entrySet()) {
            out.add(summaryBuilder(en.getKey(), en.getValue()).build());
        }
        return out;
    }

    /** Assemble a TrafficSummary from an entity + its prorated bytes and ECN flags. */
    private static <T> TrafficSummary.Builder<T> summaryBuilder(final T entity, final InOut io) {
        return TrafficSummary.from(entity)
                .withBytes(io.in, io.out)
                .withCongestionEncountered(io.congestionEncountered)
                .withNonEcnCapableTransport(io.nonEcnCapableTransport);
    }

    private Table<Directional<String>, Long, Double> stringSeries(final String entityExpr, final String fromExtra, final Where w,
                                                                  final long step, final Integer topN, final Set<String> explicit,
                                                                  final boolean includeOther) {
        return mapSeries(series(entityExpr, fromExtra, w, step, DEFAULT_ORIGIN, topN, explicit, includeOther), s -> s);
    }

    private <T> Table<Directional<T>, Long, Double> mapSeries(final Table<String, Long, double[]> raw,
                                                              final java.util.function.Function<String, T> toEntity) {
        final Table<Directional<T>, Long, Double> out = HashBasedTable.create();
        for (final Table.Cell<String, Long, double[]> cell : raw.cellSet()) {
            final T entity = toEntity.apply(cell.getRowKey());
            final double[] v = cell.getValue();
            if (v[0] != 0.0) out.put(new Directional<>(entity, true), cell.getColumnKey(), v[0]);
            if (v[1] != 0.0) out.put(new Directional<>(entity, false), cell.getColumnKey(), v[1]);
        }
        return out;
    }

    // --- config setters (blueprint) ---
    public void setThreads(final int threads) { this.threads = threads; }
    /** Blueprint wiring: supplies the flow DataSource in start() (may be inert, i.e. return null). */
    public void setDataSourceProvider(final FlowDataSourceProvider dataSourceProvider) { this.dataSourceProvider = dataSourceProvider; }
    /** Test/embedding hook: use this DataSource directly instead of resolving one from the provider. */
    public void setDataSource(final DataSource dataSource) { this.dataSource = dataSource; }
}