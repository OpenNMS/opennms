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
package org.opennms.netmgt.flows.aggregation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * In-process, write-time flow aggregator: the embeddable, Beam-free counterpart to Nephron. It buffers
 * flows into fixed event-time windows keyed by {@code (exporter, interface, dimension, value)}, prorates
 * each flow's (sampling-scaled) bytes across the windows it spans via {@link FixedWindowAggregation}, and
 * emits one summed {@link AggregatedFlow} per key when a window closes.
 *
 * <h2>Windowing and watermark</h2>
 * Windows use the global grid ({@code shift == 0}), so every exporter shares the same boundaries and the
 * results roll up cleanly when partials are summed. Event time is the flow's {@code lastSwitched}; the watermark is the
 * greatest event time seen. A window {@code [start, start+size)} closes once
 * {@code watermark >= start + size + allowedLateness}; {@code allowedLateness} therefore doubles as the
 * out-of-order holdback. This phase fires each window exactly once, at close (no early/incremental panes);
 * a flow arriving for an already-closed window is dropped and counted.
 *
 * <h2>Distribution</h2>
 * Each instance aggregates only the flows it is fed. Under multiple writers (e.g. several Sentinels) each
 * emits partial rows and the reader sums them per {@code (window, key)} — so no cross-instance state is
 * needed. A single Horizon-core instance is simply the one-writer case, where a row is already final.
 *
 * <h2>Scope (phase 2)</h2>
 * Sum-by-key over interface totals plus application/conversation/host; no TOS dimension and no top-K
 * capping yet (those are the next phase). Fault tolerance is bounded by the concession that a restart
 * loses the still-open windows held in memory.
 */
public class FlowAggregator implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FlowAggregator.class);

    /** Dimensions capped by top-K; the long tail is rolled into one null-key "Other" row per window. */
    private static final Set<AggregatedFlow.Dimension> CAPPED = EnumSet.of(
            AggregatedFlow.Dimension.APPLICATION,
            AggregatedFlow.Dimension.CONVERSATION,
            AggregatedFlow.Dimension.HOST);

    /** Order for top-K selection: most bytes first, then key ascending for deterministic ties. */
    private static final Comparator<Map.Entry<Key, Acc>> BY_BYTES_DESC = (x, y) -> {
        final int c = Long.compare(y.getValue().bytes(), x.getValue().bytes());
        if (c != 0) {
            return c;
        }
        final String kx = x.getKey().groupedByKey;
        final String ky = y.getKey().groupedByKey;
        if (kx == null) {
            return ky == null ? 0 : 1;
        }
        return ky == null ? -1 : kx.compareTo(ky);
    };

    /** A flow whose {@code lastSwitched} is farther in the future than this (vs the wall clock) is
     *  rejected, so one bad record cannot push the watermark ahead and strand every later flow. */
    private static final long MAX_FUTURE_SKEW_MS = TimeUnit.HOURS.toMillis(1);
    /** A flow spanning a longer wall-clock duration than this is rejected (implausible timestamps),
     *  bounding the per-window loop so a near-epoch {@code deltaSwitched} cannot iterate for ages. */
    private static final long MAX_FLOW_DURATION_MS = TimeUnit.DAYS.toMillis(1);

    private final long windowSizeMs;
    private final long allowedLatenessMs;
    private final long flushIntervalMs;
    private final long idleFlushMs;
    private final int topK;
    private final AggregatedFlowSink sink;

    /** Open windows, keyed by window start (ascending), each holding its per-key accumulators. */
    private final ConcurrentSkipListMap<Long, ConcurrentHashMap<Key, Acc>> windows = new ConcurrentSkipListMap<>();
    /** Wall-clock time each open window was first created, for the idle (processing-time) flush fallback. */
    private final ConcurrentHashMap<Long, Long> windowFirstSeenMs = new ConcurrentHashMap<>();
    private final AtomicLong maxEventTimeMs = new AtomicLong(Long.MIN_VALUE);
    /** Guards flow ingestion (read) against window eviction (write): a flow can never land in a window
     *  that the flusher is emitting/evicting, and cannot resurrect one that has already been emitted. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /** Highest window-start already evicted (any flush path); flows for it or earlier are late. Lock-guarded. */
    private long emittedFloor = Long.MIN_VALUE;

    /** Wall clock, injectable for tests. */
    private java.util.function.LongSupplier clock = System::currentTimeMillis;

    private final Meter flowsAggregated;
    private final Meter flowsDroppedLate;
    private final Meter flowsDroppedInvalid;
    private final Meter rowsEmitted;
    private final Meter rowsDroppedOnSink;

    private volatile boolean running = false;
    private Thread flusher;

    /**
     * @param topK per (exporter, interface, dimension) cap for the high-cardinality dimensions
     *             (application/conversation/host); the remainder is rolled into one "Other" row.
     *             {@code <= 0} disables capping (emit every key — interface totals are never capped).
     * @param idleFlushMs wall-clock age after which an open window is flushed even if the event-time
     *             watermark has stalled (e.g. an exporter went quiet). {@code <= 0} auto-derives a value
     *             safely larger than the normal event-time close ({@code windowSize + lateness + 60s}).
     */
    public FlowAggregator(final long windowSizeMs, final long allowedLatenessMs, final long flushIntervalMs,
                          final int topK, final long idleFlushMs, final AggregatedFlowSink sink,
                          final MetricRegistry metrics) {
        if (windowSizeMs < 1) {
            throw new IllegalArgumentException("windowSizeMs must be >= 1");
        }
        if (allowedLatenessMs < 0) {
            throw new IllegalArgumentException("allowedLatenessMs must be >= 0");
        }
        this.windowSizeMs = windowSizeMs;
        this.allowedLatenessMs = allowedLatenessMs;
        this.flushIntervalMs = flushIntervalMs > 0 ? flushIntervalMs : 1000;
        this.idleFlushMs = idleFlushMs > 0 ? idleFlushMs : windowSizeMs + allowedLatenessMs + 60_000L;
        this.topK = topK;
        this.sink = Objects.requireNonNull(sink);
        Objects.requireNonNull(metrics);
        this.flowsAggregated = metrics.meter(MetricRegistry.name("flowAggregator", "flowsAggregated"));
        this.flowsDroppedLate = metrics.meter(MetricRegistry.name("flowAggregator", "flowsDroppedLate"));
        this.flowsDroppedInvalid = metrics.meter(MetricRegistry.name("flowAggregator", "flowsDroppedInvalid"));
        this.rowsEmitted = metrics.meter(MetricRegistry.name("flowAggregator", "rowsEmitted"));
        this.rowsDroppedOnSink = metrics.meter(MetricRegistry.name("flowAggregator", "rowsDroppedOnSink"));
        metrics.register(MetricRegistry.name("flowAggregator", "openWindows"),
                (com.codahale.metrics.Gauge<Integer>) windows::size);
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        flusher = new Thread(this::flushLoop, "flow-aggregator");
        flusher.setDaemon(true);
        flusher.start();
        LOG.info("FlowAggregator started (windowSizeMs={}, allowedLatenessMs={}, flushIntervalMs={}, topK={}, idleFlushMs={}).",
                windowSizeMs, allowedLatenessMs, flushIntervalMs, topK, idleFlushMs);
    }

    /** Aggregate a flow. Returns {@code false} (and counts it) when the flow is unusable or wholly late. */
    public boolean add(final FlowInput f) {
        if (f == null) {
            return false;
        }
        // Timestamp sanity, before touching the watermark: reject a flow whose lastSwitched is implausibly
        // far in the future (it would advance the watermark and mark every later flow late forever) or
        // whose span is implausibly long (a near-epoch deltaSwitched would iterate millions of windows).
        if (f.lastSwitchedMs - f.deltaSwitchedMs > MAX_FLOW_DURATION_MS
                || f.lastSwitchedMs > clock.getAsLong() + MAX_FUTURE_SKEW_MS) {
            flowsDroppedInvalid.mark();
            return false;
        }

        boolean anyAccepted = false;
        // Read lock: flows aggregate concurrently, but none runs while the flusher evicts (write lock),
        // so a flow can neither land in a window mid-eviction nor resurrect one already emitted.
        lock.readLock().lock();
        try {
            // Decide lateness against the watermark from PRIOR flows AND the highest window already emitted
            // (so an idle-flushed window is not resurrected), then advance the watermark. A flow must not
            // close its own earlier windows just because it also extends into a later one.
            final long closedAtOrBefore = Math.max(closedWindowStartCeiling(maxEventTimeMs.get()), emittedFloor);
            maxEventTimeMs.updateAndGet(prev -> Math.max(prev, f.lastSwitchedMs));

            final double value = f.bytes * FixedWindowAggregation.samplingMultiplier(f.samplingInterval);
            final long firstWindow = FixedWindowAggregation.windowNumber(0L, windowSizeMs, f.deltaSwitchedMs);
            final long lastWindow = FixedWindowAggregation.windowNumber(0L, windowSizeMs, f.lastSwitchedMs);

            for (long wn = firstWindow; wn <= lastWindow; wn++) {
                final long windowStart = wn * windowSizeMs; // shift == 0
                if (windowStart <= closedAtOrBefore) {
                    // The window has already closed (and likely flushed); do not resurrect it.
                    flowsDroppedLate.mark();
                    continue;
                }
                final long windowEndInclusive = windowStart + windowSizeMs - 1;
                final long bytes = FixedWindowAggregation.bytesInWindow(
                        f.deltaSwitchedMs, f.lastSwitchedMs, value, windowStart, windowEndInclusive);
                if (bytes <= 0) {
                    continue; // no bytes attributable to this window (e.g. sub-millisecond overlap rounding)
                }
                final ConcurrentHashMap<Key, Acc> w = windows.computeIfAbsent(windowStart, k -> new ConcurrentHashMap<>());
                windowFirstSeenMs.putIfAbsent(windowStart, clock.getAsLong()); // for the idle-flush fallback
                accumulateDimensions(w, f, bytes, null);        // without-TOS rollup (over all DSCP)
                if (f.dscp != null) {
                    accumulateDimensions(w, f, bytes, f.dscp);  // with-TOS (this flow's DSCP)
                }
                anyAccepted = true;
            }
        } finally {
            lock.readLock().unlock();
        }
        if (anyAccepted) {
            flowsAggregated.mark();
        }
        return anyAccepted;
    }

    /** Convenience adapter for the enriched-flow API; skips flows missing required fields. */
    boolean add(final org.opennms.integration.api.v1.flows.Flow flow) {
        return add(FlowInput.from(flow));
    }

    /** Accumulate every dimension for one DSCP scope ({@code dscp == null} is the without-TOS rollup). */
    private static void accumulateDimensions(final Map<Key, Acc> w, final FlowInput f, final long bytes, final Integer dscp) {
        accumulate(w, f.exporterNodeId, f.ifIndex, dscp, AggregatedFlow.Dimension.INTERFACE, null, bytes, f.ingress, f.ecn, null);
        if (f.application != null) {
            accumulate(w, f.exporterNodeId, f.ifIndex, dscp, AggregatedFlow.Dimension.APPLICATION, f.application, bytes, f.ingress, f.ecn, null);
        }
        if (f.convoKey != null) {
            accumulate(w, f.exporterNodeId, f.ifIndex, dscp, AggregatedFlow.Dimension.CONVERSATION, f.convoKey, bytes, f.ingress, f.ecn, null);
        }
        if (f.srcAddr != null) {
            accumulate(w, f.exporterNodeId, f.ifIndex, dscp, AggregatedFlow.Dimension.HOST, f.srcAddr, bytes, f.ingress, f.ecn, f.srcHostname);
        }
        if (f.dstAddr != null) {
            accumulate(w, f.exporterNodeId, f.ifIndex, dscp, AggregatedFlow.Dimension.HOST, f.dstAddr, bytes, f.ingress, f.ecn, f.dstHostname);
        }
    }

    private static void accumulate(final Map<Key, Acc> window, final int exporterNodeId, final int ifIndex,
                                   final Integer dscp, final AggregatedFlow.Dimension dimension, final String groupedByKey,
                                   final long bytes, final boolean ingress, final Integer ecn, final String hostname) {
        window.computeIfAbsent(new Key(exporterNodeId, ifIndex, dscp, dimension, groupedByKey), k -> new Acc())
                .add(bytes, ingress, ecn, hostname);
    }

    /** The greatest window-start that is considered closed given a watermark, or {@code Long.MIN_VALUE} if none. */
    private long closedWindowStartCeiling(final long watermark) {
        if (watermark == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        // window [s, s+size) closed when watermark >= s + size + lateness, i.e. s <= watermark - size - lateness
        return watermark - windowSizeMs - allowedLatenessMs;
    }

    private void flushLoop() {
        while (running) {
            try {
                Thread.sleep(flushIntervalMs);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            try {
                flushClosedWindows();
                flushIdleWindows();
            } catch (final Exception e) {
                LOG.warn("Flow aggregation flush encountered an unexpected error.", e);
            }
        }
    }

    /** Emit and evict every window that has closed under the current watermark. */
    void flushClosedWindows() {
        final long threshold = closedWindowStartCeiling(maxEventTimeMs.get());
        drainWindowsUpTo(threshold);
    }

    /**
     * Processing-time fallback: emit and evict windows that have been open longer than {@code idleFlushMs}
     * in wall-clock time, regardless of the event-time watermark. This releases windows whose exporter has
     * gone quiet (so the watermark no longer advances to close them).
     */
    void flushIdleWindows() {
        final long cutoff = clock.getAsLong() - idleFlushMs;
        final List<AggregatedFlow> batch = new ArrayList<>();
        lock.writeLock().lock();
        try {
            for (final Map.Entry<Long, Long> e : windowFirstSeenMs.entrySet()) {
                if (e.getValue() <= cutoff) {
                    final Long windowStart = e.getKey();
                    windowFirstSeenMs.remove(windowStart);
                    final ConcurrentHashMap<Key, Acc> acc = windows.remove(windowStart);
                    if (acc != null) {
                        emittedFloor = Math.max(emittedFloor, windowStart);
                        emitWindow(windowStart, acc, batch);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        deliver(batch); // outside the lock: sink I/O must not block flow ingestion
    }

    private void drainWindowsUpTo(final long thresholdInclusive) {
        final List<AggregatedFlow> batch = new ArrayList<>();
        lock.writeLock().lock();
        try {
            while (!windows.isEmpty()) {
                final Long first = windows.firstKey();
                if (first == null || first > thresholdInclusive) {
                    break;
                }
                final Map.Entry<Long, ConcurrentHashMap<Key, Acc>> entry = windows.pollFirstEntry();
                if (entry != null) {
                    windowFirstSeenMs.remove(entry.getKey());
                    emittedFloor = Math.max(emittedFloor, entry.getKey());
                    emitWindow(entry.getKey(), entry.getValue(), batch);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        deliver(batch); // outside the lock: sink I/O must not block flow ingestion
    }

    /**
     * Hand a drained batch to the sink. The engine is best-effort and in-memory: these rows have already
     * been evicted, so if the sink throws they are dropped (and counted), NOT retried &mdash; the sink
     * owns its own durability and error handling (see {@link AggregatedFlowSink}).
     */
    private void deliver(final List<AggregatedFlow> batch) {
        if (batch.isEmpty()) {
            return;
        }
        rowsEmitted.mark(batch.size());
        try {
            sink.accept(batch);
        } catch (final RuntimeException e) {
            rowsDroppedOnSink.mark(batch.size());
            LOG.warn("Aggregated flow sink rejected a batch of {} rows; the rows are dropped (not retried).",
                    batch.size(), e);
        }
    }

    /** Test hook: override the wall clock used for the idle flush. */
    void setClock(final java.util.function.LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    private void emitWindow(final long windowStart, final Map<Key, Acc> accumulators, final List<AggregatedFlow> out) {
        final long windowEnd = windowStart + windowSizeMs;
        if (topK <= 0) {
            for (final Map.Entry<Key, Acc> e : accumulators.entrySet()) {
                emit(out, windowStart, windowEnd, e.getKey(), e.getValue());
            }
            return;
        }
        // Group entries by outer key (exporter, interface, dimension) so top-K is applied per parent.
        final Map<Key, List<Map.Entry<Key, Acc>>> byOuter = new HashMap<>();
        for (final Map.Entry<Key, Acc> e : accumulators.entrySet()) {
            final Key k = e.getKey();
            byOuter.computeIfAbsent(new Key(k.exporterNodeId, k.ifIndex, k.dscp, k.dimension, null), x -> new ArrayList<>()).add(e);
        }
        for (final Map.Entry<Key, List<Map.Entry<Key, Acc>>> group : byOuter.entrySet()) {
            final Key outer = group.getKey();
            final List<Map.Entry<Key, Acc>> entries = group.getValue();
            if (!CAPPED.contains(outer.dimension)) {
                // Interface (and future exporter/tos) totals are never capped.
                for (final Map.Entry<Key, Acc> e : entries) {
                    emit(out, windowStart, windowEnd, e.getKey(), e.getValue());
                }
                continue;
            }
            // Keep the top-K by bytes; roll everything else into one null-key "Other" row.
            entries.sort(BY_BYTES_DESC);
            Acc other = null;
            for (int i = 0; i < entries.size(); i++) {
                if (i < topK) {
                    emit(out, windowStart, windowEnd, entries.get(i).getKey(), entries.get(i).getValue());
                } else {
                    if (other == null) {
                        other = new Acc();
                    }
                    other.mergeCounts(entries.get(i).getValue());
                }
            }
            if (other != null) {
                out.add(new AggregatedFlow(windowStart, windowEnd, outer.exporterNodeId, outer.ifIndex,
                        outer.dscp, outer.dimension, null, other.bytesIn, other.bytesOut,
                        other.congestionEncountered, other.nonEcnCapableTransport, null));
            }
        }
    }

    private static void emit(final List<AggregatedFlow> out, final long windowStart, final long windowEnd,
                             final Key k, final Acc a) {
        out.add(new AggregatedFlow(windowStart, windowEnd, k.exporterNodeId, k.ifIndex, k.dscp, k.dimension,
                k.groupedByKey, a.bytesIn, a.bytesOut, a.congestionEncountered, a.nonEcnCapableTransport, a.hostname));
    }

    @Override
    public synchronized void close() {
        running = false;
        if (flusher != null) {
            flusher.interrupt();
            try {
                flusher.join(TimeUnit.SECONDS.toMillis(30));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // Best-effort final flush of everything still buffered, regardless of the watermark.
        drainWindowsUpTo(Long.MAX_VALUE);
        LOG.info("FlowAggregator stopped.");
    }

    /** Immutable grouping key within a window ({@code dscp == null} is the without-TOS scope). */
    private static final class Key {
        private final int exporterNodeId;
        private final int ifIndex;
        private final Integer dscp;
        private final AggregatedFlow.Dimension dimension;
        private final String groupedByKey;

        Key(final int exporterNodeId, final int ifIndex, final Integer dscp,
            final AggregatedFlow.Dimension dimension, final String groupedByKey) {
            this.exporterNodeId = exporterNodeId;
            this.ifIndex = ifIndex;
            this.dscp = dscp;
            this.dimension = dimension;
            this.groupedByKey = groupedByKey;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }
            final Key key = (Key) o;
            return exporterNodeId == key.exporterNodeId && ifIndex == key.ifIndex
                    && Objects.equals(dscp, key.dscp) && dimension == key.dimension
                    && Objects.equals(groupedByKey, key.groupedByKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exporterNodeId, ifIndex, dscp, dimension, groupedByKey);
        }
    }

    /** Mutable per-key accumulator. Mirrors Nephron's {@code Aggregate} merge semantics. */
    private static final class Acc {
        private long bytesIn;
        private long bytesOut;
        private boolean congestionEncountered;
        private boolean nonEcnCapableTransport;
        private String hostname;

        void add(final long bytes, final boolean ingress, final Integer ecn, final String hn) {
            if (ingress) {
                bytesIn += bytes;
            } else {
                bytesOut += bytes;
            }
            // ECN: congestion when any record has CE (3); non-ECT (0) or an absent value both flag non-ECT.
            if (ecn != null) {
                if (ecn == 3) {
                    congestionEncountered = true;
                }
                if (ecn == 0) {
                    nonEcnCapableTransport = true;
                }
            } else {
                nonEcnCapableTransport = true;
            }
            // Deterministic host name: keep the lexicographically smaller non-empty value.
            if (hn != null && !hn.isEmpty() && (hostname == null || hn.compareTo(hostname) < 0)) {
                hostname = hn;
            }
        }

        long bytes() {
            return bytesIn + bytesOut;
        }

        /** Fold another accumulator's byte counts and ECN flags into this one (for the "Other" bucket). */
        void mergeCounts(final Acc o) {
            bytesIn += o.bytesIn;
            bytesOut += o.bytesOut;
            congestionEncountered |= o.congestionEncountered;
            nonEcnCapableTransport |= o.nonEcnCapableTransport;
        }
    }
}
