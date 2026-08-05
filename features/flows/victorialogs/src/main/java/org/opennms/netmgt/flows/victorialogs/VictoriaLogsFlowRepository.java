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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.swrve.ratelimitedlogger.RateLimitedLog;

/**
 * Persists flows into VictoriaLogs.
 *
 * <p>Implements the Integration API's {@link FlowRepository}, which is a public extension point, so
 * this can be registered alongside the Elasticsearch repository rather than in place of it. Running
 * both at once is the intended mode during evaluation: it is the only way to compare the two
 * backends on identical input.
 *
 * <p><strong>Failures are absorbed, never propagated.</strong> {@code PipelineImpl} persists to each
 * registered repository in an unguarded loop, so an exception raised here would abort that loop and
 * could prevent the Elasticsearch repository from being called at all — a VictoriaLogs outage would
 * become a total flow-persistence outage. Since this backend is additive, the correct behaviour is
 * to swallow its own failures, count them on {@code flowsDropped}, and log at a limited rate. The
 * {@code flowsPersisted} and {@code flowsDropped} meters, not the absence of exceptions, are how
 * this repository reports health.
 *
 * <p><strong>{@link #persist} only buffers; the flush thread sends.</strong> Absorbing exceptions is
 * not enough to keep this backend additive — {@code PipelineImpl} walks its persisters serially on
 * the enrichment thread, so an HTTP call made on that thread would put VictoriaLogs' latency ahead
 * of Elasticsearch's, and a merely slow backend would throttle flow persistence for everyone. Giving
 * the thread straight back is what stops that.
 *
 * <p>Buffered flows leave every {@code bulkFlushMs}, in chunks of {@code bulkSize}. The buffer is
 * bounded by {@code maxBufferedFlows}: if VictoriaLogs is unreachable the alternative to dropping is
 * growing the heap until the JVM dies, which would take the whole of OpenNMS down over a flow-storage
 * outage. Chunking keeps a recovery after a long outage from turning a full buffer into one enormous
 * request.
 *
 * <p>Note what that bound is: a count of retained {@link Flow} references, not a number of bytes. A
 * flow carries hostnames, node information and category lists, so the heap cost of a full buffer
 * varies with the data and is not something this setting pins down. Treat the default as a starting
 * point to be measured against a real exporter population rather than as a heap guarantee.
 */
public class VictoriaLogsFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VictoriaLogsFlowRepository.class);

    /** How long {@link #stop()} waits for an in-flight send before interrupting it. */
    private static final long SHUTDOWN_WAIT_SECONDS = 30L;

    /**
     * How long the final drain in {@link #stop()} may take in total.
     *
     * <p>Separate from {@link #SHUTDOWN_WAIT_SECONDS}, which bounds only the join with the flush
     * thread. Without a bound here a backend that is slow rather than broken stalls shutdown for as
     * long as it takes to send the whole buffer one chunk at a time — a full buffer at the default
     * settings is a hundred sequential requests, each allowed thirty seconds. A failing backend was
     * always fine, because the first failure ends the loop; it is the slow-but-working case that
     * hangs, and blueprint destroys beans serially, so it hangs the container with it.
     */
    private static final long SHUTDOWN_DRAIN_SECONDS = 60L;

    /**
     * A backend outage produces one failure per flow log, which at exporter rates is a log flood
     * exactly when the logs are least useful.
     */
    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final VictoriaLogsClient client;
    private final FlowJsonSerializer serializer = new FlowJsonSerializer();

    private final Meter flowsPersistedMeter;
    private final Meter flowsDroppedMeter;
    private final com.codahale.metrics.Timer logPersistingTimer;

    // Written from the Config Admin thread, read by persisting threads.
    private volatile int bulkSize = 1000;
    private volatile int bulkFlushMs = 500;
    private volatile int maxBufferedFlows = 100_000;

    private final List<Flow> buffer = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    /** Guards the send path so a timed flush and a shutdown flush cannot overlap. */
    private final ReentrantLock sendLock = new ReentrantLock();

    private volatile ScheduledExecutorService flushExecutor;

    /** Guards against queueing more than one ad-hoc flush; see {@link #requestFlush()}. */
    private final AtomicBoolean flushQueued = new AtomicBoolean();

    /** Set by {@link #stop()} so an in-flight timed flush stops between chunks instead of sending on. */
    private volatile boolean stopped;

    /** True until configuration says this backend is enabled; see {@link #start()}. */
    private volatile boolean disabled = true;

    public VictoriaLogsFlowRepository(final MetricRegistry metricRegistry,
                                      final VictoriaLogsClient client) {
        this.client = Objects.requireNonNull(client);
        Objects.requireNonNull(metricRegistry);
        this.flowsPersistedMeter = metricRegistry.meter("flowsPersisted");
        this.flowsDroppedMeter = metricRegistry.meter("flowsDropped");
        this.logPersistingTimer = metricRegistry.timer("logPersisting");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Never throws. See the class javadoc: propagating would risk taking the Elasticsearch
     * repository down with it.
     */
    @Override
    public void persist(final Collection<? extends Flow> flows) {
        if (flows == null || flows.isEmpty()) {
            return;
        }
        lock.lock();
        try {
            // Tested under the lock, not before it. discardRemaining() empties the buffer holding
            // this same lock, so a check outside it could pass, be descheduled, and add to a buffer
            // that shutdown has already drained -- flows nobody would ever send and nobody counted,
            // breaking the promise that flowsPersisted + flowsDropped covers everything handed over.
            if (stopped) {
                flowsDroppedMeter.mark(flows.size());
                RATE_LIMITED_LOG.warn("Dropping {} flows offered after shutdown.", flows.size());
                return;
            }
            final int headroom = maxBufferedFlows - buffer.size();
            if (flows.size() > headroom) {
                final int dropped = flows.size() - Math.max(0, headroom);
                flowsDroppedMeter.mark(dropped);
                RATE_LIMITED_LOG.warn("VictoriaLogs buffer is full ({} flows); dropping {} flows. "
                        + "VictoriaLogs is unreachable or too slow.", buffer.size(), dropped);
                if (headroom <= 0) {
                    return;
                }
                buffer.addAll(new ArrayList<>(flows).subList(0, headroom));
            } else {
                buffer.addAll(flows);
            }
        } finally {
            lock.unlock();
        }
        // Buffering is all this thread does. Sending belongs to the flush thread: PipelineImpl walks
        // its persisters serially on the enrichment thread, so an HTTP call made here would put this
        // backend's latency in front of the Elasticsearch one -- a slow VictoriaLogs would throttle
        // flow persistence as a whole, which is the failure this repository is supposed to be
        // incapable of causing. Absorbing exceptions is not enough on its own; the thread has to be
        // given back too.
        final boolean batchReady;
        lock.lock();
        try {
            batchReady = buffer.size() >= bulkSize;
        } finally {
            lock.unlock();
        }
        if (batchReady) {
            requestFlush();
        }
    }

    /**
     * Removes up to {@code limit} flows from the buffer.
     *
     * @return the flows taken, or null if there was nothing to take
     */
    private List<Flow> drain(final int limit) {
        // A non-positive limit would take nothing and report success, and the caller loops until
        // this returns null -- so without this guard a misconfigured bulkSize spins a thread forever.
        if (limit <= 0 || buffer.isEmpty()) {
            return null;
        }
        final int take = Math.min(limit, buffer.size());
        final List<Flow> head = buffer.subList(0, take);
        final List<Flow> ready = new ArrayList<>(head);
        head.clear();
        return ready;
    }

    /**
     * Flushes everything buffered, in {@code bulkSize} chunks.
     *
     * <p>Chunking matters after an outage: the buffer may hold {@code maxBufferedFlows} entries, and
     * serializing all of them at once would hold the NDJSON string, its UTF-8 bytes and the gzipped
     * copy in memory simultaneously — the very heap blow-up {@code maxBufferedFlows} exists to
     * prevent.
     *
     * @param force keep draining even once {@link #stopped} is set. The timed flush passes false so
     *              that it stops between chunks during shutdown instead of sending another batch;
     *              {@link #stop()} passes true because its whole purpose is the final drain.
     * @param deadlineNanos {@link System#nanoTime()} after which to stop starting new chunks, or
     *                      null for no deadline. Whatever is left is the caller's to account for.
     */
    private void flushAll(final boolean force, final Long deadlineNanos) {
        while (force || !stopped) {
            if (deadlineNanos != null && System.nanoTime() - deadlineNanos >= 0) {
                LOG.warn("Stopping the shutdown drain after {}s with flows still buffered; "
                        + "VictoriaLogs is responding too slowly to finish.", SHUTDOWN_DRAIN_SECONDS);
                return;
            }
            final List<Flow> ready;
            lock.lock();
            try {
                ready = drain(bulkSize);
            } finally {
                lock.unlock();
            }
            if (ready == null) {
                return;
            }
            if (!sendQuietly(ready)) {
                // The backend is unhappy; draining the rest now would only lose more.
                return;
            }
        }
    }

    /**
     * Sends everything currently buffered, on the calling thread.
     *
     * <p>Package-private for tests: sending is otherwise the flush thread's job, and a test that had
     * to wait for a timer to fire would be slow and flaky.
     */
    void flushNow() {
        flushAll(true, null);
    }

    /**
     * @return true when the batch reached VictoriaLogs.
     *
     * <p>A flow the serializer cannot render is skipped and counted as dropped, not allowed to fail
     * the batch: the wire format is one independent line per flow, so a single unusable record has no
     * claim on the delivery of the others.
     *
     * <p>What {@code flowsPersisted} means here is worth being exact about. It counts flows this
     * repository handed to a request VictoriaLogs accepted — <em>not</em> flows VictoriaLogs stored.
     * A 2xx means the body was received, and lines it cannot parse or whose timestamp falls outside
     * retention are discarded server-side without any of that showing up in the response. See
     * {@link VictoriaLogsClient#fetchIngestStats()} for the counters that do reveal it; nothing in
     * this class reconciles against them yet.
     */
    private boolean sendQuietly(final List<Flow> flows) {
        if (flows == null || flows.isEmpty()) {
            return true;
        }
        sendLock.lock();
        try (final com.codahale.metrics.Timer.Context ignored = logPersistingTimer.time()) {
            final AtomicInteger skipped = new AtomicInteger();
            final String body = serializer.toNdJson(flows, skipped);
            final int unusable = skipped.get();
            if (unusable > 0) {
                flowsDroppedMeter.mark(unusable);
            }
            final int sent = flows.size() - unusable;
            if (sent > 0) {
                client.ingest(body);
                flowsPersistedMeter.mark(sent);
            }
            return true;
        } catch (final Throwable t) {
            // Counted, not rethrown -- these flows are gone and the meters must say so.
            //
            // Throwable rather than Exception, for the accounting invariant as much as for the
            // schedule. Serializing a chunk holds the NDJSON string, its UTF-8 bytes and the gzipped
            // copy at once, so OutOfMemoryError surfaces here; letting it past would leave the batch
            // counted as neither persisted nor dropped and quietly break the promise that the two
            // meters together account for every flow handed over. Returning false stops the drain,
            // so the next tick retries rather than compounding the pressure.
            flowsDroppedMeter.mark(flows.size());
            RATE_LIMITED_LOG.warn("Failed to persist {} flows to VictoriaLogs; they are lost.",
                    flows.size(), t);
            return false;
        } finally {
            sendLock.unlock();
        }
    }

    /**
     * Starts the flush thread, unless this backend is switched off.
     *
     * <p>The enable flag lives on the {@code SwitchedFlowRepository} wrapping this one, which stops
     * {@link #persist} being called but knows nothing about the timer. Without the same flag here,
     * every install would run a thread waking twice a second forever on behalf of a backend nobody
     * turned on — and persistence is off by default.
     */
    public void start() {
        shutdownFlushThread();
        stopped = false;
        if (disabled) {
            LOG.debug("VictoriaLogs flow persistence is disabled; not starting the flush thread.");
            return;
        }
        final int period = bulkFlushMs;
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "VictoriaLogsFlowRepository-flush");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(this::flushSafely, period, period, TimeUnit.MILLISECONDS);
        this.flushExecutor = executor;
    }

    /**
     * Asks the flush thread to send what is buffered, without waiting for it.
     *
     * <p>This is what keeps a full batch from sitting in the buffer until the next tick while still
     * leaving the caller's thread free. At most one flush is queued <em>or running</em> at a time:
     * the flag is cleared when the task finishes, not when it starts. Clearing it on entry would let
     * every {@code persist} crossing {@code bulkSize} during a thirty-second send queue another
     * task, building exactly the backlog of no-ops this exists to prevent.
     */
    private void requestFlush() {
        final ScheduledExecutorService executor = this.flushExecutor;
        if (executor == null || stopped || !flushQueued.compareAndSet(false, true)) {
            return;
        }
        try {
            executor.execute(() -> {
                try {
                    flushSafely();
                } finally {
                    flushQueued.set(false);
                }
            });
        } catch (final RejectedExecutionException shuttingDown) {
            flushQueued.set(false);
        }
    }

    /**
     * Runs a flush, swallowing anything that escapes.
     *
     * <p>{@code scheduleWithFixedDelay} cancels the schedule permanently if its task throws, which
     * would silently end all further flushing for the lifetime of the process.
     *
     * <p>{@link Throwable}, not {@link Exception}, and that distinction is the whole value of this
     * method. An {@link Error} is exactly what this code can produce: serializing a chunk holds the
     * NDJSON string, its UTF-8 bytes and the gzipped copy at once, so heap pressure surfaces here as
     * {@link OutOfMemoryError}. Catching only {@code Exception} would let it past, cancel the
     * schedule, and leave a repository that accepts flows forever and sends none — with nothing in
     * the log to say why, because the executor swallows the cause into a future nobody reads.
     *
     * <p>Rethrowing after logging would defeat the purpose; the schedule has to survive. An
     * {@code Error} severe enough to matter will resurface on the next attempt.
     */
    private void flushSafely() {
        try {
            flushAll(false, null);
        } catch (final Throwable t) {
            RATE_LIMITED_LOG.warn("Flush to VictoriaLogs failed unexpectedly; the flush schedule "
                    + "continues.", t);
        }
    }

    /**
     * Flushes anything still buffered so a clean shutdown does not lose a partial batch.
     *
     * <p>If the backend is unavailable the flush cannot succeed, and whatever remains is genuinely
     * lost — there is nowhere left to put it. Those flows are counted as dropped rather than
     * discarded quietly, so that {@code flowsPersisted + flowsDropped} still accounts for every flow
     * this repository was handed.
     */
    public void stop() {
        // Set before shutting down so a flush already running stops at its next chunk boundary
        // rather than starting another one behind our back.
        stopped = true;
        final boolean interrupted = shutdownFlushThread();
        flushAll(true, System.nanoTime() + TimeUnit.SECONDS.toNanos(SHUTDOWN_DRAIN_SECONDS));
        discardRemaining(interrupted);
    }

    /**
     * @param interrupted whether the shutdown itself was cut short, rather than the backend failing
     */
    private void discardRemaining(final boolean interrupted) {
        final int abandoned;
        lock.lock();
        try {
            abandoned = buffer.size();
            buffer.clear();
        } finally {
            lock.unlock();
        }
        if (abandoned > 0) {
            flowsDroppedMeter.mark(abandoned);
            // Naming the right cause matters here: an interrupted shutdown and an unreachable
            // backend need different things looked at, and the old message asserted the second
            // whatever had happened -- sending the operator to check VictoriaLogs when the real
            // cause was a shutdown timeout that never let the drain start.
            if (interrupted) {
                LOG.warn("Discarding {} buffered flows; shutdown was interrupted before they could "
                        + "be sent.", abandoned);
            } else {
                LOG.warn("Discarding {} buffered flows on shutdown; VictoriaLogs could not be "
                        + "reached or did not keep up.", abandoned);
            }
        }
    }

    /**
     * Stops the flush thread and waits for it to finish.
     *
     * <p>The wait is the point. Blueprint destroys the {@code VictoriaLogsClient} right after this
     * bean, so a flush still inside {@code client.ingest()} when {@link #stop()} returns would go on
     * to use a closed {@link java.net.http.HttpClient}. {@code awaitTermination} gives a real join,
     * which is something {@code Timer.cancel()} cannot do — it prevents future runs but neither
     * interrupts nor waits for the one executing.
     */
    private boolean shutdownFlushThread() {
        final ScheduledExecutorService executor = this.flushExecutor;
        this.flushExecutor = null;
        // A task discarded by shutdownNow never runs its body, so the flag it would have cleared
        // stays set and every later ad-hoc flush is refused by the CAS. Cleared here instead.
        flushQueued.set(false);
        if (executor == null) {
            return false;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                LOG.warn("The VictoriaLogs flush thread did not finish within {}s; interrupting it.",
                        SHUTDOWN_WAIT_SECONDS);
                executor.shutdownNow();
                // Wait again: the join is the point of this method, and shutdownNow only asks.
                if (!executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                    LOG.warn("The VictoriaLogs flush thread is still running after being "
                            + "interrupted; continuing shutdown without it.");
                }
            }
            return false;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
            // The flag is set, so the drain that follows will fail immediately on its first send.
            // Say so rather than letting it be reported as a backend failure.
            return true;
        }
    }

    public int getBulkSize() {
        return bulkSize;
    }

    /**
     * Sets the batch size, ignoring non-positive values.
     *
     * <p>A batch of zero would mean nothing ever leaves the buffer, so flows would pile up until the
     * cap and then be dropped. Refusing the value keeps the previous one rather than quietly turning
     * persistence off.
     */
    public void setBulkSize(final int bulkSize) {
        if (bulkSize <= 0) {
            LOG.warn("Ignoring bulkSize={}; it must be positive. Keeping {}.", bulkSize, this.bulkSize);
            return;
        }
        this.bulkSize = bulkSize;
    }

    public int getBulkFlushMs() {
        return bulkFlushMs;
    }

    /**
     * Sets the flush period, ignoring non-positive values.
     *
     * <p>Takes effect on the next {@link #start()}; the timer lifecycle is deliberately confined to
     * {@code start}/{@code stop} so that property injection cannot spawn a background thread
     * mid-construction, nor revive one on an already-stopped repository.
     *
     * <p>Non-positive is refused rather than treated as "never flush": the flush thread is now the
     * only thing that sends, so disabling it would turn the buffer into a leak that fills to
     * {@code maxBufferedFlows} and then drops everything.
     */
    public void setBulkFlushMs(final int bulkFlushMs) {
        if (bulkFlushMs <= 0) {
            LOG.warn("Ignoring bulkFlushMs={}; it must be positive. Keeping {}.",
                    bulkFlushMs, this.bulkFlushMs);
            return;
        }
        this.bulkFlushMs = bulkFlushMs;
    }

    public boolean isDisabled() {
        return disabled;
    }

    /** Mirrors {@code skipVictoriaLogsPersistence}; see {@link #start()}. */
    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    public int getMaxBufferedFlows() {
        return maxBufferedFlows;
    }

    /**
     * @param maxBufferedFlows how many flows may wait to be sent; must be positive
     *
     * <p>Rejected rather than accepted when non-positive, for the same reason {@link #setBulkSize}
     * is. Zero reads like "unlimited" and does the opposite: the headroom check in {@link #persist}
     * then fails for every call, so every flow is dropped and the only symptom is a rate-limited
     * warning blaming VictoriaLogs for being unreachable — which it is not.
     */
    public void setMaxBufferedFlows(final int maxBufferedFlows) {
        if (maxBufferedFlows <= 0) {
            LOG.warn("Ignoring maxBufferedFlows={}; it must be positive. Keeping {}.",
                    maxBufferedFlows, this.maxBufferedFlows);
            return;
        }
        this.maxBufferedFlows = maxBufferedFlows;
    }

    @Override
    public String toString() {
        return "VictoriaLogsFlowRepository{bulkSize=" + bulkSize
                + ", bulkFlushMs=" + bulkFlushMs
                + ", maxBufferedFlows=" + maxBufferedFlows
                + '}';
    }
}
