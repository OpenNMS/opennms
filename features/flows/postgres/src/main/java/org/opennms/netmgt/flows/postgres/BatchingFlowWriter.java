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
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Buffers items into batches that are flushed on whichever condition fires first:
 * a document-count threshold ({@code batchSize}) or a time threshold ({@code flushIntervalMs}).
 *
 * <p>Backpressure policy is <em>drop-newest</em>: {@link #add(Object)} performs a non-blocking
 * offer onto a bounded queue and, when the queue is full (the sink cannot keep up), increments a
 * {@code dropped} meter and returns without blocking. This protects the upstream flow pipeline /
 * parser threads from stalling when PostgreSQL falls behind.
 *
 * @param <T> the buffered item type (a mapped flow row)
 */
public class BatchingFlowWriter<T> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(BatchingFlowWriter.class);

    private final BlockingQueue<T> queue;
    private final int batchSize;
    private final long flushIntervalMs;
    private final Consumer<List<T>> flush;

    private final Meter enqueued;
    private final Meter dropped;
    private final Meter flushed;
    private final Timer flushTimer;
    private final Histogram batchSizeHistogram;
    private final Histogram flushIntervalHistogram;
    private final int threads;

    /** Wall-clock time of the previous flush (across all writer threads), for the inter-flush interval. */
    private final AtomicLong lastFlushMs = new AtomicLong(0L);

    private volatile boolean running = false;
    private final List<Thread> workers = new ArrayList<>();

    public BatchingFlowWriter(final String name,
                              final int queueCapacity,
                              final int batchSize,
                              final long flushIntervalMs,
                              final int threads,
                              final Consumer<List<T>> flush,
                              final MetricRegistry metrics) {
        if (queueCapacity < 1) {
            throw new IllegalArgumentException("queueCapacity must be >= 1");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be >= 1");
        }
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        this.threads = threads;
        this.queue = new ArrayBlockingQueue<>(queueCapacity);
        this.batchSize = batchSize;
        this.flushIntervalMs = flushIntervalMs > 0 ? flushIntervalMs : 500;
        this.flush = flush;
        this.enqueued = metrics.meter(MetricRegistry.name(name, "enqueued"));
        this.dropped = metrics.meter(MetricRegistry.name(name, "dropped"));
        // flushed: total rows persisted; its rate is the throughput to PostgreSQL (flows/second).
        this.flushed = metrics.meter(MetricRegistry.name(name, "flushed"));
        // flush: duration of each flush (batch INSERT) operation.
        this.flushTimer = metrics.timer(MetricRegistry.name(name, "flush"));
        // batchSize: distribution of the number of rows written per flush.
        this.batchSizeHistogram = metrics.histogram(MetricRegistry.name(name, "batchSize"));
        // flushIntervalMs: distribution of the wall-clock interval between consecutive flushes.
        this.flushIntervalHistogram = metrics.histogram(MetricRegistry.name(name, "flushIntervalMs"));
        metrics.register(MetricRegistry.name(name, "queueSize"), (com.codahale.metrics.Gauge<Integer>) queue::size);
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        // Each worker independently drains the shared queue and flushes its own batch on its own pooled
        // connection, so N threads write concurrently (N connections -> N PostgreSQL backends).
        for (int i = 0; i < threads; i++) {
            final Thread worker = new Thread(this::drainLoop, "postgres-flow-writer-" + i);
            worker.setDaemon(true);
            worker.start();
            workers.add(worker);
        }
    }

    /**
     * Enqueue an item for batched persistence. Non-blocking; drops the item (and counts it) when
     * the bounded queue is full.
     */
    public void add(final T item) {
        if (queue.offer(item)) {
            enqueued.mark();
        } else {
            dropped.mark();
        }
    }

    private void drainLoop() {
        final List<T> batch = new ArrayList<>(batchSize);
        long deadline = System.currentTimeMillis() + flushIntervalMs;
        while (running || !queue.isEmpty()) {
            try {
                final long wait = Math.max(0, deadline - System.currentTimeMillis());
                final T item = queue.poll(wait, TimeUnit.MILLISECONDS);
                if (item != null) {
                    batch.add(item);
                    if (batch.size() >= batchSize) {          // count-based flush
                        doFlush(batch);
                        deadline = System.currentTimeMillis() + flushIntervalMs;
                    }
                } else if (!batch.isEmpty()) {                 // time-based flush
                    doFlush(batch);
                    deadline = System.currentTimeMillis() + flushIntervalMs;
                } else {
                    deadline = System.currentTimeMillis() + flushIntervalMs;
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (!batch.isEmpty()) {
            doFlush(batch);
        }
    }

    private void doFlush(final List<T> batch) {
        final int size = batch.size();
        recordFlushInterval();
        try (Timer.Context ignored = flushTimer.time()) {
            flush.accept(new ArrayList<>(batch));
            flushed.mark(size);
            batchSizeHistogram.update(size);
        } catch (final Exception e) {
            LOG.warn("Failed to flush a batch of {} flow rows; the batch is dropped.", size, e);
        } finally {
            batch.clear();
        }
    }

    /** Record the wall-clock interval since the previous flush (across all writer threads). */
    private void recordFlushInterval() {
        final long now = System.currentTimeMillis();
        final long prev = lastFlushMs.getAndSet(now);
        if (prev != 0L) {
            flushIntervalHistogram.update(now - prev);
        }
    }

    @Override
    public synchronized void close() {
        running = false;
        for (final Thread worker : workers) {
            worker.interrupt();
        }
        for (final Thread worker : workers) {
            try {
                worker.join(TimeUnit.SECONDS.toMillis(30));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        workers.clear();
    }
}