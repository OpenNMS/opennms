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

package org.opennms.netmgt.flows.victorialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.SnmpInterfaceIdFilter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.netmgt.flows.processing.ConversationKeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Answers flow queries from VictoriaLogs.
 *
 * <p><strong>Published, but deliberately outranked.</strong> {@code blueprint.xml} registers this as
 * a {@code FlowQueryService} at a negative service ranking. That interface is consumed through a
 * singleton reference, so wherever the Elasticsearch bundle is present it keeps serving the flow UI
 * and this answers only if that bundle is absent or stopped. Raising the ranking above zero is what
 * hands the UI over — see the comment on the registration for why that is a decision rather than a
 * default.
 *
 * <p>Records whose {@code netflow.direction} was never determined are resolved against the interface
 * the query filtered on, and excluded when it filtered on none — matching {@code common.ftl} and the
 * series templates respectively. See {@link #isIngress} and {@link #directionConstraint}; the
 * reference corpus covers both, on a second SNMP interface.
 */
public class VictoriaLogsFlowQueryService implements FlowQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(VictoriaLogsFlowQueryService.class);

    // Columns the aggregates are aliased to; VictoriaLogs returns every column as a string.
    private static final String COUNT_COLUMN = "count";
    private static final String TOTAL_COLUMN = "total";
    private static final String CONGESTION_COLUMN = "congestion";
    private static final String NON_ECT_COLUMN = "non_ect";

    private static final String FIELD_APPLICATION = "netflow.application";
    private static final String FIELD_CONVO_KEY = "netflow.convo_key";
    private static final String FIELD_SRC_ADDR = "netflow.src_addr";
    private static final String FIELD_DST_ADDR = "netflow.dst_addr";
    private static final String FIELD_SRC_ADDR_HOSTNAME = "netflow.src_addr_hostname";
    private static final String FIELD_DST_ADDR_HOSTNAME = "netflow.dst_addr_hostname";
    private static final String FIELD_DIRECTION = "netflow.direction";
    private static final String FIELD_ECN = "netflow.ecn";
    private static final String FIELD_BYTES = "netflow.bytes";
    private static final String FIELD_INPUT_SNMP = "netflow.input_snmp";
    private static final String FIELD_OUTPUT_SNMP = "netflow.output_snmp";

    private static final String INGRESS = "ingress";

    /** What an exporter that never told us the direction leaves behind. */
    private static final String UNKNOWN_DIRECTION = "unknown";

    /** Label the Elasticsearch repository gives the traffic outside the requested entities. */
    static final String OTHER_NAME = "Other";

    /** Label it gives flows the classification engine could not name. */
    static final String UNKNOWN_APPLICATION_NAME = "Unknown";

    /** ECN codepoint meaning congestion was seen, and the one meaning ECN was not supported. */
    private static final int ECN_CONGESTION_ENCOUNTERED = 3;
    private static final int ECN_NOT_CAPABLE = 0;

    /**
     * How long a flow may run, bounding the proportional fan-out. Matches the Elasticsearch
     * repository's default; see {@link ProportionalSumQuery} for why this is not an enforced cap.
     */
    private static final long DEFAULT_MAX_FLOW_DURATION_MS = 120_000L;

    /**
     * Ceiling on how many distinct values a listing query will pull back.
     *
     * <p>Elasticsearch bounds these lookups with the aggregation's {@code size}, applied after its
     * prefix, fuzzy or regex match. Neither fuzzy matching nor Lucene's regex dialect survives
     * translation to LogsQL, so the match happens here and the server cannot narrow the set first.
     * This bound therefore exists to stop a high-cardinality field from being dragged into the heap,
     * and if it is reached the listing is incomplete — hence the warning rather than silence.
     */
    private static final int MAX_DISTINCT_VALUES = 10_000;

    /**
     * How many queries may be in flight, and how many may wait.
     *
     * <p>The work is blocking HTTP, so threads are cheap relative to the round trips they cover. The
     * queue is bounded on purpose: an unbounded one turns a backend that has stopped answering into
     * heap exhaustion, and a caller waiting behind ten thousand queued queries has already had a
     * failure — it just has not been told yet.
     */
    private static final int DEFAULT_QUERY_THREADS = 8;
    private static final int DEFAULT_QUERY_QUEUE_DEPTH = 128;

    private final VictoriaLogsClient client;
    private final long maxFlowDurationMs;
    private final ExecutorService executor;

    /**
     * Futures handed out but not yet settled.
     *
     * <p>Kept so shutdown can fail them. {@code shutdownNow()} discards whatever is still queued,
     * and a discarded task never runs its body — so without this its future is neither completed nor
     * completed exceptionally, and {@code FlowRestServiceImpl.waitForFuture} calls {@code get()}
     * with no timeout. Each abandoned query would hold a request thread for the life of the process.
     */
    private final Set<CompletableFuture<?>> outstanding = ConcurrentHashMap.newKeySet();

    public VictoriaLogsFlowQueryService(final VictoriaLogsClient client) {
        this(client, DEFAULT_MAX_FLOW_DURATION_MS);
    }

    public VictoriaLogsFlowQueryService(final VictoriaLogsClient client, final long maxFlowDurationMs) {
        this(client, maxFlowDurationMs, defaultExecutor());
    }

    public VictoriaLogsFlowQueryService(final VictoriaLogsClient client, final long maxFlowDurationMs,
                                        final ExecutorService executor) {
        this.client = Objects.requireNonNull(client);
        this.maxFlowDurationMs = maxFlowDurationMs;
        this.executor = Objects.requireNonNull(executor);
    }

    private static ExecutorService defaultExecutor() {
        final AtomicInteger counter = new AtomicInteger();
        final ThreadPoolExecutor pool = new ThreadPoolExecutor(
                DEFAULT_QUERY_THREADS, DEFAULT_QUERY_THREADS, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(DEFAULT_QUERY_QUEUE_DEPTH),
                runnable -> {
                    final Thread thread = new Thread(runnable,
                            "VictoriaLogsFlowQueryService-" + counter.incrementAndGet());
                    // Daemon so a container that fails to call stop() can still exit.
                    thread.setDaemon(true);
                    return thread;
                });
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    /**
     * Releases the query threads.
     *
     * <p>Wired as the blueprint destroy method. Queries in flight are interrupted rather than waited
     * for: this runs during shutdown, the client they are using is about to be closed underneath
     * them, and nobody is left to receive an answer.
     */
    public void stop() {
        executor.shutdownNow();
        // Fail everything still unsettled. This cannot distinguish a query discarded from the
        // queue from one that was mid-flight -- both are in the set -- and it does not try: a
        // running task is interrupted by shutdownNow anyway, and a caller told the service shut
        // down is better served than one left waiting on a future nobody will ever complete.
        final VictoriaLogsException shuttingDown = new VictoriaLogsException(
                "The VictoriaLogs query service was shut down before this query could run.");
        for (final CompletableFuture<?> future : outstanding) {
            future.completeExceptionally(shuttingDown);
        }
        outstanding.clear();
    }

    @Override
    public CompletableFuture<Long> getFlowCount(final List<Filter> filters) {
        return supply(() -> {
            final String logsQl = LogsQlFilterVisitor.toQuery(filters)
                    + " | stats count() as " + COUNT_COLUMN;
            final List<JsonObject> rows = client.query(logsQl);
            // A stats pipe with no grouping yields exactly one row -- except over an empty result
            // set, where VictoriaLogs returns nothing at all rather than a row holding zero.
            if (rows.isEmpty()) {
                return 0L;
            }
            final JsonObject row = rows.get(0);
            if (!row.has(COUNT_COLUMN) || row.get(COUNT_COLUMN).isJsonNull()) {
                return 0L;
            }
            final String raw = row.get(COUNT_COLUMN).getAsString();
            return raw.isEmpty() ? 0L : Long.parseLong(raw);
        });
    }

    /**
     * The entities carrying the most traffic, most first.
     *
     * <p>Ranked on the <em>raw</em> byte total, not the proportionally attributed one — Elasticsearch
     * ranks with a plain {@code sum} and only applies the proportional split when totalling the
     * entities it has already chosen. Ties break on the entity name ascending, as a terms aggregation
     * does.
     *
     * <p>Records lacking the field are labelled {@code keyForMissingTerm} and compete for a place
     * like any other entity — and a record whose value <em>is</em> that label belongs in the same
     * bucket as them. Elasticsearch merges the two before ranking, via the {@code missing} parameter
     * on its terms aggregation, so this has to as well: grouping alone would rank them as two
     * entities, hand back the label twice, and then report the traffic of only one of them.
     * Over-fetching and merging here is what makes that possible, and mirrors the multiplier
     * {@code RawFlowQueryService.getTopN} applies for the same kind of accuracy.
     *
     * @param keyForMissingTerm label for records lacking the field, or null to leave them out
     */
    private List<String> topN(final int n, final String field, final String keyForMissingTerm,
                              final List<Filter> filters) throws VictoriaLogsException {
        if (n < 1) {
            return Collections.emptyList();
        }
        // Over-fetch only when two buckets can collapse into one; otherwise every row is already a
        // distinct entity and the server-side cut is exact.
        final int fetch = keyForMissingTerm == null ? n
                : (int) Math.min(MAX_DISTINCT_VALUES, 2L * n);
        // Ranked and cut down server-side. Sorting in Java after a `limit` would be wrong rather than
        // merely slow: LogsQL's limit yields an arbitrary N rows, not the heaviest ones, so on a
        // high-cardinality field -- netflow.convo_key reaches five figures on a real network within
        // minutes -- the "top N" would be the top of a random sample.
        // When there is no label for them, records lacking the field are excluded up front. Left
        // in, their bucket would take one of the N slots and then be dropped, returning fewer than N
        // entities -- whereas a terms aggregation never buckets a document missing the field at all.
        final String logsQl = LogsQlFilterVisitor.toQuery(filters)
                + (keyForMissingTerm == null ? " -" + absent(field) : "")
                + " | stats by (" + ProportionalSumQuery.quote(field) + ")"
                + " sum(" + ProportionalSumQuery.quote(FIELD_BYTES) + ") as " + TOTAL_COLUMN
                + " | sort by (" + TOTAL_COLUMN + " desc, " + ProportionalSumQuery.quote(field) + ")"
                + " | limit " + fetch;

        // Insertion-ordered, so entities that never collide keep the server's ranking exactly.
        final Map<String, Double> ranked = new LinkedHashMap<>();
        for (final JsonObject row : client.query(logsQl)) {
            final String value = text(row, field);
            final String entity = value.isEmpty() ? keyForMissingTerm : value;
            if (entity != null) {
                ranked.merge(entity, number(row, TOTAL_COLUMN), Double::sum);
            }
        }
        if (ranked.size() <= n) {
            return new ArrayList<>(ranked.keySet());
        }
        // Only reachable when a merge happened, so re-ranking cannot disturb the server's order for
        // the ordinary case. Ties break on the entity name, as a terms aggregation does.
        return ranked.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Double>>comparingDouble(Map.Entry::getValue)
                        .reversed().thenComparing(Map.Entry::getKey))
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Totals traffic for {@code from}, in that order, optionally followed by everything else.
     *
     * <p>"Other" is a separate query over the complement rather than a subtraction. Bytes could be
     * subtracted, but the ECN flags could not — they are booleans, and there is no arithmetic that
     * recovers "did any of the remaining flows see congestion" from two totals. Querying the
     * complement answers both at once, and matches what Elasticsearch does.
     */
    private List<TrafficSummary<String>> totalBytesFrom(final Collection<String> from,
                                                        final String field,
                                                        final String keyForMissingTerm,
                                                        final boolean includeOther,
                                                        final List<Filter> filters)
            throws VictoriaLogsException {
        final boolean missingIncluded = keyForMissingTerm != null && from.contains(keyForMissingTerm);
        final List<String> named = from.stream()
                .filter(entity -> !entity.equals(keyForMissingTerm))
                .collect(Collectors.toList());

        final Map<String, TrafficSummary<String>> summaries = new LinkedHashMap<>();
        if (!named.isEmpty()) {
            summaries.putAll(summarize(field, anyOf(field, named), filters));
        }
        if (missingIncluded) {
            summaries.putAll(labelled(keyForMissingTerm, absent(field), filters));
        }
        if (includeOther) {
            final Map<String, TrafficSummary<String>> other =
                    labelled(OTHER_NAME, complement(field, named, missingIncluded), filters);
            // An empty complement still reports a row, of zeroes, exactly as Elasticsearch does.
            summaries.put(OTHER_NAME, other.getOrDefault(OTHER_NAME,
                    TrafficSummary.from(OTHER_NAME).withBytes(0, 0).build()));
        }

        final List<TrafficSummary<String>> ordered = new ArrayList<>(summaries.size());
        for (final String entity : from) {
            final TrafficSummary<String> summary = summaries.remove(entity);
            if (summary != null) {
                ordered.add(summary);
            }
        }
        ordered.addAll(summaries.values());
        return ordered;
    }

    /**
     * Hosts carrying the most traffic, most first.
     *
     * <p>A flow belongs to both of its endpoints, so the ranking is over the union of the source and
     * destination groupings and has to be merged here — Elasticsearch aggregates the {@code hosts}
     * array instead, which VictoriaLogs stores as literal JSON text and cannot group by. The merge
     * means the {@link #MAX_DISTINCT_VALUES} ceiling applies to the ranking too, unlike the
     * single-field case which is ranked and cut server-side.
     */
    private List<String> topNHosts(final int n, final List<Filter> filters)
            throws VictoriaLogsException {
        if (n < 1) {
            return Collections.emptyList();
        }
        final Map<String, Double> totals = new LinkedHashMap<>(rawTotals(FIELD_SRC_ADDR, filters));
        rawTotals(FIELD_DST_ADDR, filters)
                .forEach((host, bytes) -> totals.merge(host, bytes, Double::sum));
        return totals.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Double>>comparingDouble(Map.Entry::getValue)
                        .reversed().thenComparing(Map.Entry::getKey))
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Raw byte totals per value of {@code field}; the ranking measure, before attribution. */
    private Map<String, Double> rawTotals(final String field, final List<Filter> filters)
            throws VictoriaLogsException {
        final String logsQl = LogsQlFilterVisitor.toQuery(filters)
                + " | stats by (" + ProportionalSumQuery.quote(field) + ")"
                + " sum(" + ProportionalSumQuery.quote(FIELD_BYTES) + ") as " + TOTAL_COLUMN
                // Heaviest first, so that hitting the ceiling loses the tail rather than an
                // arbitrary slice -- this feeds a ranking, and a random sample of it would
                // put quiet hosts in a "busiest hosts" answer.
                + " | sort by (" + TOTAL_COLUMN + " desc)"
                + " | limit " + MAX_DISTINCT_VALUES;
        final List<JsonObject> rows = client.query(logsQl);
        if (rows.size() >= MAX_DISTINCT_VALUES) {
            LOG.warn("Reached the {} distinct-value ceiling ranking {}; the ranking may omit entities.",
                    MAX_DISTINCT_VALUES, field);
        }
        final Map<String, Double> totals = new LinkedHashMap<>();
        for (final JsonObject row : rows) {
            final String value = text(row, field);
            if (!value.isEmpty()) {
                totals.merge(value, number(row, TOTAL_COLUMN), Double::sum);
            }
        }
        return totals;
    }

    /**
     * Totals traffic per host, in the order given, optionally followed by everything else.
     *
     * <p>Note the asymmetry, which is Elasticsearch's and not an accident here: a named host's total
     * counts a flow at <em>both</em> of its endpoints, so the per-host figures deliberately sum to
     * more than the traffic on the wire, whereas "Other" counts each unrelated flow once. The two
     * answer different questions — "how much did this host move" against "how much is not shown".
     */
    private List<TrafficSummary<String>> totalBytesFromHosts(final Collection<String> from,
                                                             final boolean includeOther,
                                                             final List<Filter> filters)
            throws VictoriaLogsException {
        final Map<String, TrafficSummary<String>> summaries = new LinkedHashMap<>();
        if (!from.isEmpty()) {
            final String touching = '(' + anyOf(FIELD_SRC_ADDR, from)
                    + " OR " + anyOf(FIELD_DST_ADDR, from) + ')';
            final Map<String, TrafficSummary<String>> byHost = summarizeHosts(from, touching, filters);
            for (final String host : from) {
                final TrafficSummary<String> summary = byHost.get(host);
                if (summary != null) {
                    summaries.put(host, summary);
                }
            }
        }
        if (includeOther) {
            summaries.put(OTHER_NAME, labelled(OTHER_NAME, hostComplement(from), filters)
                    .getOrDefault(OTHER_NAME, TrafficSummary.from(OTHER_NAME).withBytes(0, 0).build()));
        }
        return new ArrayList<>(summaries.values());
    }

    /**
     * Selects everything the caller did not ask about.
     *
     * <p>When the missing-value bucket was reported under its own name it is excluded here too,
     * otherwise it would be counted twice.
     */
    private static String complement(final String field, final Collection<String> named,
                                     final boolean missingReportedSeparately) {
        final StringBuilder complement = new StringBuilder();
        if (!named.isEmpty()) {
            complement.append('-').append(anyOf(field, named));
        }
        if (missingReportedSeparately) {
            complement.append(complement.length() == 0 ? "" : " ").append('-').append(absent(field));
        }
        return complement.toString();
    }

    /** Selects the flows that touch none of {@code from} at either end. */
    private static String hostComplement(final Collection<String> from) {
        return from.isEmpty() ? ""
                : '-' + anyOf(FIELD_SRC_ADDR, from) + " -" + anyOf(FIELD_DST_ADDR, from);
    }

    /**
     * Merges the per-source and per-destination totals so each flow counts at both endpoints.
     *
     * <p>Except a flow whose two endpoints are the same host, which is counted once; see
     * {@link #notSelfFlow}.
     */
    private Map<String, TrafficSummary<String>> summarizeHosts(final Collection<String> from,
                                                               final String extraFilter,
                                                               final List<Filter> filters)
            throws VictoriaLogsException {
        final Map<String, TrafficSummary<String>> merged =
                new LinkedHashMap<>(summarize(FIELD_SRC_ADDR, extraFilter, filters));
        summarize(FIELD_DST_ADDR, and(extraFilter, notSelfFlow(from)), filters).forEach((host, asDestination) ->
                merged.merge(host, asDestination, (a, b) -> TrafficSummary.from(host)
                        .withBytes(a.getBytesIn() + b.getBytesIn(), a.getBytesOut() + b.getBytesOut())
                        .withCongestionEncountered(a.isCongestionEncountered() || b.isCongestionEncountered())
                        .withNonEcnCapableTransport(a.isNonEcnCapableTransport() || b.isNonEcnCapableTransport())
                        .build()));
        return merged;
    }

    /** Attaches the resolved hostname to each address. */
    private List<TrafficSummary<Host>> asHosts(final List<TrafficSummary<String>> summaries,
                                               final List<Filter> filters)
            throws VictoriaLogsException {
        return relabel(summaries, hostEntity(hostnames(filters)));
    }

    /** Rebuilds each conversation from its key and attaches the endpoints' hostnames. */
    private List<TrafficSummary<Conversation>> asConversations(
            final List<TrafficSummary<String>> summaries, final List<Filter> filters)
            throws VictoriaLogsException {
        return relabel(summaries, conversationEntity(hostnames(filters)));
    }

    private static <T> List<TrafficSummary<T>> relabel(final List<TrafficSummary<String>> summaries,
                                                       final Function<String, T> entity) {
        final List<TrafficSummary<T>> resolved = new ArrayList<>(summaries.size());
        for (final TrafficSummary<String> summary : summaries) {
            resolved.add(TrafficSummary.from(entity.apply(summary.getEntity()))
                    .withBytesAndEcnInfo(summary).build());
        }
        return resolved;
    }

    /** Turns an address into a {@link Host}, naming it where a hostname was recorded. */
    private static Function<String, Host> hostEntity(final Map<String, String> hostnames) {
        return ip -> OTHER_NAME.equals(ip)
                ? Host.forOther().build()
                : new Host(ip, hostnames.get(ip));
    }

    /** Rebuilds a {@link Conversation} from its key, naming both endpoints where possible. */
    private static Function<String, Conversation> conversationEntity(
            final Map<String, String> hostnames) {
        return key -> {
            if (OTHER_NAME.equals(key)) {
                return Conversation.forOther().build();
            }
            final Conversation.Builder conversation =
                    Conversation.from(ConversationKeyUtils.fromJsonString(key));
            final String lower = hostnames.get(conversationEndpoint(key, true));
            final String upper = hostnames.get(conversationEndpoint(key, false));
            if (lower != null) {
                conversation.withLowerHostname(lower);
            }
            if (upper != null) {
                conversation.withUpperHostname(upper);
            }
            return conversation.build();
        };
    }

    private static String conversationEndpoint(final String convoKey, final boolean lower) {
        final var key = ConversationKeyUtils.fromJsonString(convoKey);
        return lower ? key.getLowerIp() : key.getUpperIp();
    }

    /**
     * Maps each address seen in the window to its hostname, where one was recorded.
     *
     * <p>Two queries rather than one per host: Elasticsearch issues a lookup per entity, which is
     * fine when the aggregation has already cut the list to N but wasteful here, and the answer is
     * the same — a hostname belongs to an address regardless of which end of a flow it appeared on.
     *
     * <p>Where an address carries a hostname in some records and not others, the first non-empty one
     * wins. Elasticsearch takes whichever record its search returned first, so neither is more
     * principled than the other; both are arbitrary in the same way.
     */
    private Map<String, String> hostnames(final List<Filter> filters) throws VictoriaLogsException {
        final Map<String, String> hostnames = new LinkedHashMap<>();
        collectHostnames(hostnames, FIELD_SRC_ADDR, FIELD_SRC_ADDR_HOSTNAME, filters);
        collectHostnames(hostnames, FIELD_DST_ADDR, FIELD_DST_ADDR_HOSTNAME, filters);
        return hostnames;
    }

    private void collectHostnames(final Map<String, String> into, final String addressField,
                                  final String hostnameField, final List<Filter> filters)
            throws VictoriaLogsException {
        final String logsQl = LogsQlFilterVisitor.toQuery(filters)
                + " | stats by (" + ProportionalSumQuery.quote(addressField)
                + ", " + ProportionalSumQuery.quote(hostnameField) + ")"
                + " count() as " + COUNT_COLUMN
                + " | sort by (" + ProportionalSumQuery.quote(addressField) + ")"
                + " | limit " + MAX_DISTINCT_VALUES;
        for (final JsonObject row : client.query(logsQl)) {
            final String address = text(row, addressField);
            final String hostname = text(row, hostnameField);
            if (!address.isEmpty() && !hostname.isEmpty()) {
                into.putIfAbsent(address, hostname);
            }
        }
    }

    /** Totals the selected records as a single group under {@code label}. */
    private Map<String, TrafficSummary<String>> labelled(final String label, final String extraFilter,
                                                         final List<Filter> filters)
            throws VictoriaLogsException {
        final Map<String, TrafficSummary<String>> summaries = summarize(null, extraFilter, filters);
        final TrafficSummary<String> summary = summaries.get("");
        return summary == null
                ? Collections.emptyMap()
                : Collections.singletonMap(label, TrafficSummary.from(label)
                        .withBytesAndEcnInfo(summary).build());
    }

    /**
     * Proportionally attributed byte totals plus ECN flags, grouped by {@code groupField}.
     *
     * <p>A summary is a series with exactly one bucket: the window is the step. That is not a
     * shortcut, it is what the Elasticsearch repository does — which is why a summary over a partial
     * window reports a flow's share of that window rather than its whole byte count.
     *
     * @param groupField field to group by, or null to total everything into one group keyed ""
     */
    private Map<String, TrafficSummary<String>> summarize(final String groupField,
                                                          final String extraFilter,
                                                          final List<Filter> filters)
            throws VictoriaLogsException {
        final TimeRangeFilter range = requireTimeRange(filters);
        final long start = range.getStart();
        final long end = range.getEnd();
        final Long snmpInterfaceId = snmpInterfaceId(filters);
        final String selector = and(and(LogsQlFilterVisitor.toQuery(filters), extraFilter),
                directionConstraint(filters));

        final List<String> groupBy = new ArrayList<>();
        if (groupField != null) {
            groupBy.add(groupField);
        }
        groupBy.addAll(directionGroupBy(snmpInterfaceId));
        // The step is the whole window: a summary is a series with one bucket. No guard against a
        // zero-width window here -- ProportionalSumQuery.build rejects end <= start first, and
        // Elasticsearch rejects the "0ms" interval it would compute, so both refuse alike.
        final String bytesQuery = ProportionalSumQuery.build(selector, groupBy, start, end,
                end - start, maxFlowDurationMs);

        // Accumulated as a double and rounded down once, at the end. Truncating each row first would
        // lose up to a byte per row, and a group is routinely several rows: filtering on an interface
        // puts netflow.input_snmp and netflow.output_snmp in the grouping, and egress traffic leaving
        // one interface arrives on many. Elasticsearch truncates once per entity and direction, so
        // per-row truncation is both a divergence and a drift between a summary and the sum of its
        // own series -- the series path keeps the double.
        final Map<String, double[]> bytes = new LinkedHashMap<>();
        for (final JsonObject row : client.query(bytesQuery)) {
            final String entity = groupField == null ? "" : text(row, groupField);
            if (groupField != null && entity.isEmpty()) {
                continue;
            }
            final Boolean ingress = isIngress(row, snmpInterfaceId);
            if (ingress == null) {
                continue;
            }
            final double[] inOut = bytes.computeIfAbsent(entity, key -> new double[2]);
            inOut[ingress ? 0 : 1] += number(row, "bytes");
        }
        if (bytes.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, boolean[]> ecn = ecnFlags(groupField, selector);

        final Map<String, TrafficSummary<String>> summaries = new LinkedHashMap<>();
        bytes.forEach((entity, inOut) -> {
            final boolean[] flags = ecn.getOrDefault(entity, new boolean[2]);
            summaries.put(entity, TrafficSummary.from(entity)
                    .withBytes((long) inOut[0], (long) inOut[1])
                    .withCongestionEncountered(flags[0])
                    .withNonEcnCapableTransport(flags[1])
                    .build());
        });
        return summaries;
    }

    /**
     * Whether any record in each group saw congestion, and whether any was not ECN-capable.
     *
     * <p>Asked separately from the byte totals because these are properties of the records
     * themselves, unaffected by how their bytes divide across time.
     *
     * <p>One known difference from Elasticsearch: it evaluates these per direction bucket and lets
     * the last one processed win, so a group whose ingress and egress records disagree takes whichever
     * direction the aggregation happened to return last. Here the flags hold if <em>any</em> record in
     * the group has them, which is the reading the field names imply.
     */
    private Map<String, boolean[]> ecnFlags(final String groupField, final String selector)
            throws VictoriaLogsException {
        final String ecnField = ProportionalSumQuery.quote(FIELD_ECN);
        final String logsQl = selector
                + " | stats " + (groupField == null ? ""
                        : "by (" + ProportionalSumQuery.quote(groupField) + ") ")
                + "count() if (" + ecnField + ":=" + ECN_CONGESTION_ENCOUNTERED + ") as " + CONGESTION_COLUMN
                + ", count() if (" + ecnField + ":=" + ECN_NOT_CAPABLE + ") as " + NON_ECT_COLUMN
                + (groupField == null ? ""
                        : " | sort by (" + ProportionalSumQuery.quote(groupField) + ")")
                + " | limit " + MAX_DISTINCT_VALUES;

        final List<JsonObject> rows = client.query(logsQl);
        // Silence here would be worse than for the other ceilings. A group that falls off the end
        // is not reported as unknown, it is reported as "no congestion seen, ECN capable" -- a
        // positive claim, indistinguishable from a measured one, because summarize() reads a missing
        // entry as two false flags.
        if (rows.size() >= MAX_DISTINCT_VALUES) {
            LOG.warn("Reached the {} distinct-value ceiling reading ECN flags for {}; groups beyond "
                    + "it are reported as having seen no congestion.", MAX_DISTINCT_VALUES,
                    groupField == null ? "the whole result" : groupField);
        }
        final Map<String, boolean[]> flags = new LinkedHashMap<>();
        for (final JsonObject row : rows) {
            final String entity = groupField == null ? "" : text(row, groupField);
            flags.put(entity, new boolean[]{
                    number(row, CONGESTION_COLUMN) > 0,
                    number(row, NON_ECT_COLUMN) > 0});
        }
        return flags;
    }

    // ------------------------------------------------------------------------------------------
    // Series
    // ------------------------------------------------------------------------------------------

    /**
     * Bucketed, proportionally attributed byte totals for {@code from}, optionally plus "Other".
     *
     * <p>The same three queries a summary uses — the named entities, the missing-value bucket, the
     * complement — only asked at the caller's step rather than over the window as a whole.
     */
    private Table<Directional<String>, Long, Double> seriesFrom(
            final Collection<String> from, final String field, final String keyForMissingTerm,
            final long step, final boolean includeOther, final List<Filter> filters)
            throws VictoriaLogsException {
        final TimeRangeFilter range = requireTimeRange(filters);
        final Table<Directional<String>, Long, Double> raw = HashBasedTable.create();

        final boolean missingIncluded = keyForMissingTerm != null && from.contains(keyForMissingTerm);
        final List<String> named = from.stream()
                .filter(entity -> !entity.equals(keyForMissingTerm))
                .collect(Collectors.toList());

        if (!named.isEmpty()) {
            collect(raw, field, null, anyOf(field, named), step, range, filters);
        }
        if (missingIncluded) {
            collect(raw, null, keyForMissingTerm, absent(field), step, range, filters);
        }
        if (includeOther) {
            putOther(raw, complement(field, named, missingIncluded), step, range, filters);
        }
        return alignColumns(fillGaps(raw, step), from, includeOther);
    }

    /**
     * Adds the complement under "Other", replacing rather than accumulating.
     *
     * <p>These are different rows in every case but one: an entity whose own name is "Other". The
     * complement excludes that entity's flows, so adding the two together would report its traffic
     * plus everyone else's under a single label and give a number belonging to neither. Replacing
     * matches what the summary path does, where the complement simply overwrites the entry —
     * behaviour inherited from {@code RawFlowQueryService.getTotalBytesFrom}.
     */
    private void putOther(final Table<Directional<String>, Long, Double> into,
                          final String complement, final long step, final TimeRangeFilter range,
                          final List<Filter> filters) throws VictoriaLogsException {
        final Table<Directional<String>, Long, Double> other = HashBasedTable.create();
        collect(other, null, OTHER_NAME, complement, step, range, filters);
        other.rowKeySet().forEach(row -> into.row(row).clear());
        other.cellSet().forEach(cell ->
                into.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
    }

    /**
     * Bucketed byte totals per host, optionally plus "Other".
     *
     * <p>Two groupings merged rather than one, for the reason {@link #summarizeHosts} gives: a flow
     * belongs to both of its endpoints, and the {@code hosts} array Elasticsearch groups by is
     * literal JSON text to VictoriaLogs. Each side is selected on its own address field, so every
     * group this produces is already one of the hosts asked for.
     *
     * <p>The destination pass excludes flows that are their own peer; see {@link #notSelfFlow}.
     */
    private Table<Directional<String>, Long, Double> hostSeriesFrom(
            final Collection<String> from, final long step, final boolean includeOther,
            final List<Filter> filters) throws VictoriaLogsException {
        final TimeRangeFilter range = requireTimeRange(filters);
        final Table<Directional<String>, Long, Double> raw = HashBasedTable.create();
        if (!from.isEmpty()) {
            collect(raw, FIELD_SRC_ADDR, null, anyOf(FIELD_SRC_ADDR, from), step, range, filters);
            collect(raw, FIELD_DST_ADDR, null,
                    and(anyOf(FIELD_DST_ADDR, from), notSelfFlow(from)), step, range, filters);
        }
        if (includeOther) {
            putOther(raw, hostComplement(from), step, range, filters);
        }
        return alignColumns(fillGaps(raw, step), from, includeOther);
    }

    /**
     * Excludes a flow whose two endpoints are the same host.
     *
     * <p>A flow belongs to both of its endpoints, which is why the source and destination groupings
     * are unioned — but when they are the same host that union counts it twice. Elasticsearch cannot
     * make that mistake: {@code FlowDocument.hosts} is a {@code Set}, so a flow with one distinct
     * endpoint yields one bucket. A self-flow is left to the source pass alone.
     *
     * <p>LogsQL cannot compare two fields, so this cannot be written as "source differs from
     * destination". It is instead spelled out per host — excluding only flows that have the
     * <em>same</em> host at both ends, never a flow merely running between two hosts that were both
     * asked about, which must still count for each of them.
     */
    private static String notSelfFlow(final Collection<String> from) {
        return "-(" + from.stream()
                .map(host -> '(' + ProportionalSumQuery.quote(FIELD_SRC_ADDR) + ":="
                        + LogsQlFilterVisitor.literal(host) + ' '
                        + ProportionalSumQuery.quote(FIELD_DST_ADDR) + ":="
                        + LogsQlFilterVisitor.literal(host) + ')')
                .collect(Collectors.joining(" OR ")) + ')';
    }

    /**
     * Adds one proportional-sum query's rows to {@code into}, accumulating where they collide.
     *
     * @param groupField field naming the entity, or null to key every row with {@code label}
     */
    private void collect(final Table<Directional<String>, Long, Double> into,
                         final String groupField, final String label, final String extraFilter,
                         final long step, final TimeRangeFilter range, final List<Filter> filters)
            throws VictoriaLogsException {
        final Long snmpInterfaceId = snmpInterfaceId(filters);
        final String selector = and(and(LogsQlFilterVisitor.toQuery(filters), extraFilter),
                directionConstraint(filters));
        final List<String> groupBy = new ArrayList<>();
        if (groupField != null) {
            groupBy.add(groupField);
        }
        groupBy.addAll(directionGroupBy(snmpInterfaceId));
        final String logsQl = ProportionalSumQuery.build(selector, groupBy,
                range.getStart(), range.getEnd(), step, maxFlowDurationMs);

        for (final JsonObject row : client.query(logsQl)) {
            final String entity = groupField == null ? label : text(row, groupField);
            if (entity.isEmpty()) {
                continue;
            }
            final Boolean ingress = isIngress(row, snmpInterfaceId);
            if (ingress == null) {
                continue;
            }
            final Directional<String> key = new Directional<>(entity, ingress);
            final long bucket = (long) number(row, "bstart");
            final Double running = into.get(key, bucket);
            // Cells accumulate rather than replace: the host series merges two groupings into one
            // row, and grouping by netflow.direction yields three values where Directional has two.
            into.put(key, bucket, (running == null ? 0d : running) + number(row, "bytes"));
        }
    }

    /**
     * Fills each row's interior gaps with zero.
     *
     * <p>A row runs from its first bucket to its last with nothing missing in between, which is what
     * the Elasticsearch aggregation produces: it reports a bucket that no flow reached as zero, not
     * as absent, so long as the group has traffic on both sides of it. The distinction is not
     * cosmetic — {@link #alignColumns} turns a genuinely absent cell into NaN, and a chart draws a
     * gap there rather than a line along the axis.
     */
    private static Table<Directional<String>, Long, Double> fillGaps(
            final Table<Directional<String>, Long, Double> table, final long step) {
        for (final Directional<String> row : new ArrayList<>(table.rowKeySet())) {
            final Map<Long, Double> cells = table.row(row);
            final NavigableSet<Long> buckets = new TreeSet<>(cells.keySet());
            for (long bucket = buckets.first() + step; bucket < buckets.last(); bucket += step) {
                cells.putIfAbsent(bucket, 0d);
            }
        }
        return table;
    }

    /**
     * Gives every row the same buckets, marking the ones it has no value for with NaN, and emits the
     * rows in the order the caller asked for them.
     *
     * <p>Rows come from separate queries over different entities, so they rarely cover the same
     * span; without the padding a caller would have to discover each row's buckets for itself.
     * {@code ElasticFlowQueryService.mapTable} does exactly this, which is why NaN rather than a
     * missing cell is what the recorded answers contain.
     *
     * <p>Row order is part of the answer even though a {@link Table} is nominally unordered.
     * {@code FlowRestServiceImpl} builds a chart's columns straight from {@code rowKeySet()}, so
     * iteration order is series order on screen — and iterating a {@link HashBasedTable} would put
     * the entities in hash order, making the busiest one land anywhere and differ between two
     * identical calls. {@code RawFlowQueryService} ends its Top-N series the same way, via
     * {@code TableUtils.sortTableByRowKeys}: requested entities first, in the requested order, then
     * anything else. Note that {@code ReferenceComparisonIT} sorts rows before comparing, so this is
     * one of the few things the recorded answers cannot check.
     */
    private static Table<Directional<String>, Long, Double> alignColumns(
            final Table<Directional<String>, Long, Double> table,
            final Collection<String> order, final boolean includeOther) {
        final Set<Long> buckets = new TreeSet<>(table.columnKeySet());
        final List<String> entityOrder = new ArrayList<>(order);
        if (includeOther) {
            entityOrder.add(OTHER_NAME);
        }
        // Insertion order is what ImmutableTable.Builder preserves in its rowKeySet.
        final ImmutableTable.Builder<Directional<String>, Long, Double> aligned = ImmutableTable.builder();
        for (final Directional<String> row : orderRows(table.rowKeySet(), entityOrder)) {
            final Map<Long, Double> cells = table.row(row);
            for (final Long bucket : buckets) {
                final Double value = cells.get(bucket);
                aligned.put(row, bucket, value == null ? Double.NaN : value);
            }
        }
        return aligned.build();
    }

    /**
     * Sorts rows by the requested entity order, appending any the caller did not name.
     *
     * <p>Within one entity the two directions are ordered egress-then-ingress, chosen only because
     * it has to be something a second call will reproduce.
     */
    private static List<Directional<String>> orderRows(final Set<Directional<String>> rows,
                                                       final List<String> entityOrder) {
        // Each row emitted once, however many times its name appears in the requested order. The
        // guard is not defensive padding: an entity called "Other" asked for alongside includeOther
        // puts that name in the list twice, and emitting its rows twice makes the immutable table
        // reject a duplicate cell and fail the whole query -- the same crash the relabel merge
        // exists to prevent, one layer further up.
        final Set<Directional<String>> emitted = new LinkedHashSet<>();
        for (final String entity : entityOrder) {
            rows.stream()
                    .filter(row -> row.getValue().equals(entity))
                    .sorted(Comparator.comparing(Directional::isIngress))
                    .forEach(emitted::add);
        }
        rows.stream()
                .filter(row -> !entityOrder.contains(row.getValue()))
                .sorted(Comparator.comparing((Directional<String> row) -> row.getValue())
                        .thenComparing(Directional::isIngress))
                .forEach(emitted::add);
        return new ArrayList<>(emitted);
    }

    /**
     * Re-keys a series by the entity its row names, keeping the direction and the row order.
     *
     * <p>Two rows can map onto one: an entity named "Other" resolves to the same {@link Host} or
     * {@link Conversation} as the "Other" bucket itself, and two conversation keys that differ only
     * in their JSON spelling parse to the same conversation. Building an {@code ImmutableTable}
     * directly would reject the second cell and fail the whole query, so the collision is absorbed
     * here with the later row winning — the same rule {@link #putOther} applies.
     */
    private static <T> Table<Directional<T>, Long, Double> relabel(
            final Table<Directional<String>, Long, Double> table, final Function<String, T> entity) {
        final Map<Directional<T>, Map<Long, Double>> rows = new LinkedHashMap<>();
        for (final Directional<String> row : table.rowKeySet()) {
            rows.put(new Directional<>(entity.apply(row.getValue()), row.isIngress()),
                    new LinkedHashMap<>(table.row(row)));
        }
        final ImmutableTable.Builder<Directional<T>, Long, Double> relabelled = ImmutableTable.builder();
        rows.forEach((row, cells) -> cells.forEach((bucket, value) ->
                relabelled.put(row, bucket, value)));
        return relabelled.build();
    }

    /**
     * Undoes the escaping the REST layer applies to a conversation key.
     *
     * <p>The Elasticsearch path has to do this because its query is assembled by Freemarker, which
     * escapes the key again on the way out; nothing here re-escapes, but a caller that has already
     * escaped its keys would otherwise match nothing at all.
     */
    private static Set<String> unescapeConversations(final Set<String> conversations) {
        return conversations.stream()
                .map(conversation -> conversation.replace("\\\"", "\""))
                // Ordered, unlike the Elasticsearch path's toSet(): for the summary methods the
                // order of `from` is the order of the answer.
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // ------------------------------------------------------------------------------------------
    // Direction
    // ------------------------------------------------------------------------------------------

    /**
     * Which way a group's traffic was flowing, or null if it cannot be told.
     *
     * <p>{@code netflow.direction} has three values and {@link Directional} has two, so a record
     * whose direction was never determined has to be resolved against something. The interface the
     * query filtered on is that something: the same physical interface is the input of a flow going
     * one way and the output of one going the other, so a record naming it as its input was inbound
     * and one naming it as its output was outbound. This mirrors the Painless script in
     * {@code common.ftl}, and a record with no direction at all is treated the same way, as that
     * script does.
     *
     * <p>Null is unreachable in practice and is not a licence to ignore a record: with an interface,
     * {@code LogsQlFilterVisitor} admits an undetermined-direction record only when that interface is
     * its input or its output, so one of the two tests matches; without one,
     * {@link #directionConstraint} has already excluded such records from the query.
     */
    private static Boolean isIngress(final JsonObject row, final Long snmpInterfaceId) {
        final String direction = text(row, FIELD_DIRECTION);
        if (!direction.isEmpty() && !UNKNOWN_DIRECTION.equals(direction)) {
            return INGRESS.equals(direction);
        }
        if (snmpInterfaceId == null) {
            return null;
        }
        if (snmpInterfaceId.equals(asLong(text(row, FIELD_INPUT_SNMP)))) {
            return Boolean.TRUE;
        }
        if (snmpInterfaceId.equals(asLong(text(row, FIELD_OUTPUT_SNMP)))) {
            return Boolean.FALSE;
        }
        return null;
    }

    /**
     * Drops undetermined-direction records when nothing can place them.
     *
     * <p>Without an interface to resolve against there is no honest way to call such a record
     * inbound or outbound, and Elasticsearch does not try: {@code series_for_terms.ftl},
     * {@code series_for_others.ftl} and {@code series_for_missing.ftl} all constrain the query to
     * ingress and egress when no interface was given. Note where this is <em>not</em> applied —
     * {@code top_n_terms.ftl} carries no such constraint, so these records still count toward the
     * ranking even though their bytes are not reported, and {@code flow_count.ftl} still counts
     * them. Both are quirks of the reference worth keeping rather than tidying, since the recorded
     * answers depend on them.
     */
    private static String directionConstraint(final List<Filter> filters) {
        return snmpInterfaceId(filters) == null
                ? "-(" + ProportionalSumQuery.quote(FIELD_DIRECTION) + ":="
                        + LogsQlFilterVisitor.literal(UNKNOWN_DIRECTION) + ")"
                : "";
    }

    /** The interface the query is about, if it names one. */
    private static Long snmpInterfaceId(final List<Filter> filters) {
        if (filters != null) {
            for (final Filter filter : filters) {
                if (filter instanceof SnmpInterfaceIdFilter) {
                    return (long) ((SnmpInterfaceIdFilter) filter).getSnmpInterfaceId();
                }
            }
        }
        return null;
    }

    /**
     * The fields a group needs to carry for {@link #isIngress} to resolve it.
     *
     * <p>Added only when there is an interface to compare them against: with none, undetermined
     * directions are excluded outright, and grouping by two more fields would multiply the groups
     * for nothing.
     */
    private static List<String> directionGroupBy(final Long snmpInterfaceId) {
        return snmpInterfaceId == null
                ? Collections.singletonList(FIELD_DIRECTION)
                : Arrays.asList(FIELD_DIRECTION, FIELD_INPUT_SNMP, FIELD_OUTPUT_SNMP);
    }

    /** A LogsQL disjunction over the values a field may take. */
    private static String anyOf(final String field, final Collection<String> values) {
        return '(' + values.stream()
                .map(value -> ProportionalSumQuery.quote(field) + ":=" + LogsQlFilterVisitor.literal(value))
                .collect(Collectors.joining(" OR ")) + ')';
    }

    /** Selects records that do not carry the field; VictoriaLogs treats a missing field as empty. */
    private static String absent(final String field) {
        return '(' + ProportionalSumQuery.quote(field) + ":=\"\")";
    }

    private static String and(final String base, final String extra) {
        return extra == null || extra.isEmpty() ? base : base + ' ' + extra;
    }

    /**
     * The window the summary covers.
     *
     * <p>Required rather than defaulted: a summary is a proportional total over a definite range, and
     * without one there is nothing to take a proportion of. Elasticsearch rejects the call for the
     * same reason.
     */
    private static TimeRangeFilter requireTimeRange(final List<Filter> filters) {
        if (filters != null) {
            for (final Filter filter : filters) {
                if (filter instanceof TimeRangeFilter) {
                    return (TimeRangeFilter) filter;
                }
            }
        }
        throw new IllegalArgumentException("A TimeRangeFilter is required to total flow traffic");
    }

    private static String text(final JsonObject row, final String field) {
        final JsonElement value = row.get(field);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    /**
     * Reads a numeric column.
     *
     * <p>Absent and empty are legitimate and mean zero. Anything else that will not parse is a
     * failure worth naming: VictoriaLogs renders every column as text, so a field an operator
     * populated with something non-numeric — {@code netflow.bytes} is read straight off the document
     * — would otherwise surface as a bare {@link NumberFormatException} with no indication of which
     * column produced it, out of methods that declare {@link VictoriaLogsException}.
     */
    private static double number(final JsonObject row, final String column)
            throws VictoriaLogsException {
        final String raw = text(row, column);
        if (raw.isEmpty()) {
            return 0d;
        }
        try {
            return Double.parseDouble(raw);
        } catch (final NumberFormatException notNumeric) {
            throw new VictoriaLogsException(
                    "Expected a number in column '" + column + "' but got: " + raw, notNumeric);
        }
    }

    /**
     * The distinct non-empty values of {@code field} among the records the filters select.
     *
     * <p>A record missing the field groups under the empty string, which is dropped: Elasticsearch's
     * {@code terms} aggregation simply does not bucket a document that lacks the field.
     */
    private List<String> distinctValues(final String field, final List<Filter> filters)
            throws VictoriaLogsException {
        final String logsQl = LogsQlFilterVisitor.toQuery(filters)
                + " | stats by (" + ProportionalSumQuery.quote(field) + ")"
                + " count() as " + COUNT_COLUMN
                // Ordered before it is cut. LogsQL's limit yields an arbitrary N rows, so
                // without this the ceiling would return the alphabetically-first of a random
                // sample rather than of the field -- a wrong answer, not a truncated one.
                + " | sort by (" + ProportionalSumQuery.quote(field) + ")"
                + " | limit " + MAX_DISTINCT_VALUES;
        final List<JsonObject> rows = client.query(logsQl);
        final List<String> values = new ArrayList<>();
        for (final JsonObject row : rows) {
            final JsonElement value = row.get(field);
            if (value == null || value.isJsonNull()) {
                continue;
            }
            final String text = value.getAsString();
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        // Counted on the rows returned, not on what survived filtering: a result that hit the ceiling
        // but included a missing-value bucket leaves one fewer value, and testing the filtered size
        // would then stay silent about a listing that really was truncated.
        if (rows.size() >= MAX_DISTINCT_VALUES) {
            LOG.warn("Reached the {} distinct-value ceiling for {}; the listing is truncated and may "
                    + "omit matches.", MAX_DISTINCT_VALUES, field);
        }
        return values;
    }

    /**
     * Compiles a pattern that must match the whole value.
     *
     * <p>Lucene's {@code regexp} query is anchored — the pattern has to consume the entire term —
     * whereas most regex engines, LogsQL's included, are happy with a substring. Using
     * {@link java.util.regex.Matcher#matches()} rather than {@code find()} restores the Elasticsearch
     * behaviour; without it {@code 10.1.1.*} would also match {@code 192.168.10.1.1.7}.
     *
     * <p>Common patterns are spelled the same way in both dialects, but Lucene's optional operators
     * ({@code ~} complement, {@code &} intersection, {@code <n-m>} intervals, {@code @} any-string)
     * have no Java equivalent and are not translated.
     */
    private static Pattern anchored(final String regex) {
        return Pattern.compile(regex == null || regex.isEmpty() ? ".*" : regex);
    }

    /**
     * Runs {@code body} on the query executor, surfacing failures through the returned future.
     *
     * <p>Off the caller's thread, which is the whole point of the {@link CompletableFuture} in these
     * signatures. Answering a summary takes three to five round trips and a series three to four,
     * and they are sequential by nature — a Top-N ranks before it totals. Completing the future
     * inline made every one of those block whichever thread the REST layer handed over, so a slow
     * VictoriaLogs would tie up request threads rather than merely answering slowly.
     *
     * <p>Rejection is reported, not absorbed. A saturated queue means the backend is not keeping up,
     * and failing the future says so; the alternative, running the work on the calling thread, would
     * quietly restore exactly the behaviour this exists to remove.
     *
     * <p>The task catches {@link Throwable} rather than {@link Exception} because a future nobody
     * completes is worse than one that fails: an {@link Error} escaping the worker would leave the
     * caller waiting on a result that can never arrive.
     */
    private <T> CompletableFuture<T> supply(final Query<T> body) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        outstanding.add(future);
        future.whenComplete((result, failure) -> outstanding.remove(future));
        try {
            executor.execute(() -> {
                try {
                    future.complete(body.run());
                } catch (final Throwable t) {
                    future.completeExceptionally(t);
                }
            });
        } catch (final RejectedExecutionException rejected) {
            // A terminated executor and a full queue arrive as the same exception. Reporting the
            // first as the second sends the operator to look at VictoriaLogs capacity when the real
            // cause is a stopped bundle -- which a consumer still holding the old service across a
            // config reload will hit every time.
            future.completeExceptionally(new VictoriaLogsException(executor.isShutdown()
                    ? "The VictoriaLogs query service has been shut down."
                    : "The VictoriaLogs query executor is saturated; the backend is not keeping up "
                            + "with the queries being asked of it.", rejected));
        }
        return future;
    }

    @FunctionalInterface
    private interface Query<T> {
        T run() throws VictoriaLogsException;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Matching happens here rather than in the server because Elasticsearch combines a prefix
     * query with a fuzzy one and LogsQL has no fuzzy operator; see {@link ElasticFuzziness}. The
     * ordering is lexicographic and the limit applies after matching, mirroring a {@code terms}
     * aggregation ordered by {@code _key} with {@code size} set — so this returns the alphabetically
     * first matches, not the busiest ones.
     */
    @Override
    public CompletableFuture<List<String>> getApplications(final String matchingPrefix, final long limit,
                                                           final List<Filter> filters) {
        return supply(() -> distinctValues(FIELD_APPLICATION, filters).stream()
                .filter(candidate -> ElasticFuzziness.matchesPrefixOrFuzzy(matchingPrefix, candidate))
                .sorted()
                .limit(Math.max(0, limit))
                .collect(Collectors.toList()));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Unclassified flows are reported under "Unknown" and compete for a place in the ranking like
     * any other application.
     */
    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(
            final int n, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> totalBytesFrom(
                topN(n, FIELD_APPLICATION, UNKNOWN_APPLICATION_NAME, filters),
                FIELD_APPLICATION, UNKNOWN_APPLICATION_NAME, includeOther, filters));
    }

    /**
     * {@inheritDoc}
     *
     * <p>The result follows the iteration order of {@code applications} rather than any ranking —
     * the caller asked for these, in this order — with "Other" appended when requested.
     */
    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(
            final Set<String> applications, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> totalBytesFrom(applications, FIELD_APPLICATION, null, includeOther, filters));
    }

    /**
     * {@inheritDoc}
     *
     * <p>An empty {@code applications} yields an empty table, and does so <em>before</em>
     * {@code includeOther} is considered. That ordering is the whole point: the complement of
     * nothing is everything, so honouring "Other" here would answer a request that named no
     * applications with a chart of all traffic labelled "Other". Elasticsearch short-circuits at the
     * same place, returning a null table which its own caller then dereferences; this returns empty
     * instead. Note the Top-N methods deliberately do <em>not</em> short-circuit, because N=0 with
     * "Other" is a coherent request for exactly that complement, and the recorded answers show
     * Elasticsearch serving it.
     */
    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(
            final Set<String> applications, final long step, final boolean includeOther,
            final List<Filter> filters) {
        return supply(() -> applications.isEmpty() ? ImmutableTable.of()
                : seriesFrom(applications, FIELD_APPLICATION, null, step, includeOther, filters));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Unclassified flows appear as "Unknown" and are ranked alongside the named applications, so
     * they can take one of the N places and are then queried separately — a record without the field
     * cannot be selected by naming a value for it.
     */
    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(
            final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> seriesFrom(topN(n, FIELD_APPLICATION, UNKNOWN_APPLICATION_NAME, filters),
                FIELD_APPLICATION, UNKNOWN_APPLICATION_NAME, step, includeOther, filters));
    }

    /**
     * {@inheritDoc}
     *
     * <p>A conversation key is a JSON array rendered as a string, so the five per-part patterns are
     * assembled into one pattern over that rendering — exactly as the Elasticsearch repository does,
     * including its handling of the application part, which is bare {@code null} rather than a quoted
     * string when the flow was never classified.
     */
    @Override
    public CompletableFuture<List<String>> getConversations(final String locationPattern,
                                                            final String protocolPattern,
                                                            final String lowerIPPattern,
                                                            final String upperIPPattern,
                                                            final String applicationPattern,
                                                            final long limit,
                                                            final List<Filter> filters) {
        return supply(() -> {
            final Pattern pattern = anchored(conversationRegex(locationPattern, protocolPattern,
                    lowerIPPattern, upperIPPattern, applicationPattern));
            return distinctValues(FIELD_CONVO_KEY, filters).stream()
                    .filter(key -> pattern.matcher(key).matches())
                    .sorted()
                    .limit(Math.max(0, limit))
                    .collect(Collectors.toList());
        });
    }

    /**
     * Composes the pattern matched against {@code netflow.convo_key}.
     *
     * <p>Kept identical to {@code RawFlowQueryService.getConversations}: an unclassified flow records
     * its application as an unquoted {@code null}, so a caller asking for "any application" has to be
     * offered both the quoted and the null spelling, and a caller naming one has to have it quoted.
     */
    static String conversationRegex(final String locationPattern, final String protocolPattern,
                                    final String lowerIPPattern, final String upperIPPattern,
                                    final String applicationPattern) {
        final String application;
        if (".*".equals(applicationPattern)) {
            application = "(\".*\"|null)";
        } else if (!"null".equals(applicationPattern)) {
            application = '"' + applicationPattern + '"';
        } else {
            application = applicationPattern;
        }
        return String.format("\\[\"%s\",%s,\"%s\",\"%s\",%s\\]",
                locationPattern, protocolPattern, lowerIPPattern, upperIPPattern, application);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(
            final int n, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> asConversations(totalBytesFrom(
                topN(n, FIELD_CONVO_KEY, null, filters),
                FIELD_CONVO_KEY, null, includeOther, filters), filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(
            final Set<String> conversations, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> asConversations(totalBytesFrom(unescapeConversations(conversations),
                FIELD_CONVO_KEY, null, includeOther, filters), filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(
            final Set<String> conversations, final long step, final boolean includeOther,
            final List<Filter> filters) {
        // Empty short-circuits before includeOther; see getApplicationSeries.
        return supply(() -> conversations.isEmpty() ? ImmutableTable.of()
                : relabel(
                        seriesFrom(unescapeConversations(conversations), FIELD_CONVO_KEY, null, step,
                                includeOther, filters),
                        conversationEntity(hostnames(filters))));
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(
            final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> relabel(
                seriesFrom(topN(n, FIELD_CONVO_KEY, null, filters), FIELD_CONVO_KEY, null, step,
                        includeOther, filters),
                conversationEntity(hostnames(filters))));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Elasticsearch matches against its {@code hosts} array, which the flow document builds from
     * the source and destination addresses. VictoriaLogs stores a JSON array as its literal text, so
     * rather than parse that back apart the two address fields are read directly and unioned — the
     * same set, without depending on how arrays happen to be serialised.
     */
    @Override
    public CompletableFuture<List<String>> getHosts(final String regex, final long limit,
                                                    final List<Filter> filters) {
        return supply(() -> {
            final Set<String> hosts = new LinkedHashSet<>(distinctValues(FIELD_SRC_ADDR, filters));
            hosts.addAll(distinctValues(FIELD_DST_ADDR, filters));
            final Pattern pattern = anchored(regex);
            return hosts.stream()
                    .filter(host -> pattern.matcher(host).matches())
                    .sorted()
                    .limit(Math.max(0, limit))
                    .collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(
            final int n, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> asHosts(
                totalBytesFromHosts(topNHosts(n, filters), includeOther, filters), filters));
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(
            final Set<String> hosts, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> asHosts(totalBytesFromHosts(hosts, includeOther, filters), filters));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(
            final Set<String> hosts, final long step, final boolean includeOther,
            final List<Filter> filters) {
        // Empty short-circuits before includeOther; see getApplicationSeries.
        return supply(() -> hosts.isEmpty() ? ImmutableTable.of()
                : relabel(hostSeriesFrom(hosts, step, includeOther, filters),
                        hostEntity(hostnames(filters))));
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(
            final int n, final long step, final boolean includeOther, final List<Filter> filters) {
        return supply(() -> relabel(hostSeriesFrom(topNHosts(n, filters), step, includeOther, filters),
                hostEntity(hostnames(filters))));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Elasticsearch answers this with a composite {@code terms} aggregation, which returns its
     * buckets in ascending key order — numerically, because the field is numeric. The ordering is
     * imposed here rather than left to the server because these values arrive as text and nothing
     * pins down how LogsQL would order them; {@code NUMERIC_FIRST} makes the answer independent of
     * that. Note this is <em>not</em> evidence that LogsQL sorts lexicographically — the recorded
     * Top-N answers rank 2310 above 300, so {@code sort by (total desc)} is numeric — it is only
     * that the guarantee is worth having locally. {@code field.size} bounds the result exactly as
     * the aggregation's {@code size} does.
     */
    @Override
    public CompletableFuture<List<String>> getFieldValues(final LimitedCardinalityField field,
                                                          final List<Filter> filters) {
        return supply(() -> distinctValues(field.fieldName, filters).stream()
                .sorted(NUMERIC_FIRST)
                .limit(field.size)
                .collect(Collectors.toList()));
    }

    /**
     * Orders values numerically when they are numbers and lexicographically otherwise.
     *
     * <p>VictoriaLogs hands back every column as text, so the numeric ordering Elasticsearch produces
     * for a numeric field has to be recovered rather than assumed.
     */
    private static final Comparator<String> NUMERIC_FIRST = (a, b) -> {
        final Long left = asLong(a);
        final Long right = asLong(b);
        if (left != null && right != null) {
            return Long.compare(left, right);
        }
        // Numbers sort ahead of everything else. Falling back to a string comparison for the mixed
        // case would make the ordering intransitive -- "9" < "10" numerically but "10" < "1x" < "9"
        // as text -- and TimSort rejects a comparator that contradicts itself.
        if (left != null || right != null) {
            return left != null ? -1 : 1;
        }
        return a.compareTo(b);
    };

    private static Long asLong(final String value) {
        try {
            return Long.valueOf(value);
        } catch (final NumberFormatException notNumeric) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Ordered by the field's own value rather than by traffic — this is a breakdown across a small
     * fixed set of codepoints, so a stable axis is more useful than a ranking.
     */
    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(
            final LimitedCardinalityField field, final List<Filter> filters) {
        return supply(() -> {
            final List<TrafficSummary<String>> summaries =
                    new ArrayList<>(summarize(field.fieldName, null, filters).values());
            summaries.sort(Comparator.comparing(TrafficSummary::getEntity, NUMERIC_FIRST));
            return summaries;
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>The only series method whose rows are <em>not</em> padded to a common set of buckets: there
     * is no "Other" to line up against and Elasticsearch does not route this one through the
     * alignment step, so each codepoint's row carries only the buckets it has traffic in.
     */
    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(
            final LimitedCardinalityField field, final long step, final List<Filter> filters) {
        return supply(() -> {
            final Table<Directional<String>, Long, Double> raw = HashBasedTable.create();
            collect(raw, field.fieldName, null, null, step, requireTimeRange(filters), filters);
            fillGaps(raw, step);
            // Ordered rather than copied straight through: a HashBasedTable iterates in hash order,
            // which would shuffle the chart's series between two identical calls.
            final ImmutableTable.Builder<Directional<String>, Long, Double> ordered =
                    ImmutableTable.builder();
            for (final Directional<String> row : orderRows(raw.rowKeySet(), Collections.emptyList())) {
                new TreeMap<>(raw.row(row)).forEach((bucket, value) ->
                        ordered.put(row, bucket, value));
            }
            return ordered.build();
        });
    }
}
