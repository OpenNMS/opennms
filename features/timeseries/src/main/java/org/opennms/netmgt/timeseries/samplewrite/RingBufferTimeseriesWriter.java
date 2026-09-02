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
package org.opennms.netmgt.timeseries.samplewrite;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.stats.StatisticsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.swrve.ratelimitedlogger.RateLimitedLog;

/**
 * Used to write samples to the {@link org.opennms.integration.api.v1.timeseries.TimeSeriesStorage}.
 *
 * Calls to  publish the samples to a ring buffer so
 * that they don't block while the data is being persisted.
 *
 * @author jwhite
 */
public class RingBufferTimeseriesWriter implements TimeseriesWriter, WorkHandler<SampleBatchEvent>, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(RingBufferTimeseriesWriter.class);

    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();
    private static final Duration STORAGE_GET_WARNING_DURATION = Duration.ofSeconds(5);

    private static final Duration DESTROY_GRACE_PERIOD = Duration.ofSeconds(30);

    private WorkerPool<SampleBatchEvent> workerPool;

    private ExecutorService executor;

    private RingBuffer<SampleBatchEvent> ringBuffer;

    private final int ringBufferSize;

    private final int numWriterThreads;

    private final Meter droppedSamples;

    private final Timer sampleWriteTsTimer;

    private TimeseriesStorageManager storage;

    private StatisticsCollector stats;

    /**
     * The {@link RingBuffer} doesn't appear to expose any methods that indicate the number
     * of elements that are currently "queued", so we keep track of them with this atomic counter.
     */
    private final AtomicLong numEntriesOnRingBuffer = new AtomicLong();
    private final AtomicBoolean readyToRockAndRoll = new AtomicBoolean(false);
    private final AtomicBoolean thePartyIsOver = new AtomicBoolean(false);

    @Inject
    public RingBufferTimeseriesWriter(final TimeseriesStorageManager storage,
                                      final StatisticsCollector stats,
                                      @Named("timeseries.ring_buffer_size") Integer ringBufferSize,
                                      @Named("timeseries.writer_threads") Integer numWriterThreads,
                                      @Named("timeseriesMetricRegistry") MetricRegistry registry) {
        Preconditions.checkArgument(ringBufferSize > 0, "ringBufferSize must be positive");
        Preconditions.checkArgument(DoubleMath.isMathematicalInteger(Math.log(ringBufferSize) / Math.log(2)), "ringBufferSize must be a power of two");
        Preconditions.checkArgument(numWriterThreads > 0, "numWriterThreads must be positive");
        Preconditions.checkNotNull(registry, "metric registry");

        this.storage = Objects.requireNonNull(storage);
        this.stats = Objects.requireNonNull(stats);
        this.ringBufferSize = ringBufferSize;
        this.numWriterThreads = numWriterThreads;
        numEntriesOnRingBuffer.set(0L);

        registry.register(MetricRegistry.name("ring-buffer", "size"),
                (Gauge<Long>) numEntriesOnRingBuffer::get);
        registry.register(MetricRegistry.name("ring-buffer", "max-size"),
                (Gauge<Long>) () -> (long) RingBufferTimeseriesWriter.this.ringBufferSize);

        droppedSamples = registry.meter(MetricRegistry.name("ring-buffer", "dropped-samples"));
        sampleWriteTsTimer = registry.timer("samples.write.ts");

        LOG.debug("Using ring_buffer_size: {}", this.ringBufferSize);
        setUpWorkerPool();

    }

    private void setUpWorkerPool() {
        // Executor that will be used to construct new threads for consumers
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("TimeseriesWriter-Consumer-%d").build();
        executor = Executors.newCachedThreadPool(namedThreadFactory);

        @SuppressWarnings("unchecked")
        final WorkHandler<SampleBatchEvent>[] handlers = new WorkHandler[numWriterThreads];
        for (int i = 0; i < numWriterThreads; i++) {
            handlers[i] = this;
        }

        ringBuffer = RingBuffer.createMultiProducer(SampleBatchEvent::new, ringBufferSize);
        workerPool = new WorkerPool<SampleBatchEvent>(
                ringBuffer,
                ringBuffer.newBarrier(),
                new FatalExceptionHandler(),
                handlers);
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        workerPool.start(executor);

        readyToRockAndRoll.set(true);
    }

    @Override
    public void destroy() {
        readyToRockAndRoll.set(false);
        if (workerPool != null) {
            var start = Instant.now();
            LOG.info("destroy(): Draining and halting the time series worker pool. Entries in ring buffer: {}",
                    numEntriesOnRingBuffer.get());
            var destroyStatusThread = new Thread(() -> {
                while (numEntriesOnRingBuffer.get() != 0 &&
                        Duration.between(start, Instant.now()).compareTo(DESTROY_GRACE_PERIOD) < 0) {
                    LOG.info("destroy() in progress. Entries left in ring buffer to drain: {}",
                            numEntriesOnRingBuffer.get());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        LOG.info("Apparently my work is done here. Entries in ring buffer: {}",
                                numEntriesOnRingBuffer.get());
                        break;
                    }
                }
                if (numEntriesOnRingBuffer.get() != 0) {
                    LOG.warn("destroy(): WorkerPool does not want to cooperate, forcing cooperation");
                    thePartyIsOver.set(true); // prevents new calls to onEvent from doing any work
                    executor.shutdownNow(); // will make any BlockingServiceLookup calls return immediately
                }
            }, getClass().getSimpleName() + "-destroy-status");
            destroyStatusThread.start();
            workerPool.drainAndHalt();
            LOG.info("Completed draining ring buffer entries (current size {}). Worker pool is halted. Took {}.",
                    numEntriesOnRingBuffer.get(),
                    Duration.between(start, Instant.now()));
            destroyStatusThread.interrupt();
        }
    }

    @Override
    public void insert(List<Sample> samples) {
        if (!readyToRockAndRoll.get()) {
            insertDrop(samples, "We are not ready to rock and roll");
            return;
        }

        // Add the samples to the ring buffer
        if (!ringBuffer.tryPublishEvent(TRANSLATOR, samples)) {
            insertDrop(samples, "The ring buffer is full");
            return;
        }
        // Increase our entry counter
        numEntriesOnRingBuffer.incrementAndGet();
    }

    private void insertDrop(List<Sample> samples, String message) {
        RATE_LIMITED_LOGGER.error(message + ". {} samples associated with resource ids {} will be dropped.",
                samples.size(), new Object() {
                    @Override
                    public String toString() {
                        // We wrap this in a toString() method to avoid build the string
                        // unless the log message is actually printed
                        return samples.stream()
                                .map(s -> s.getMetric().getFirstTagByKey(IntrinsicTagNames.resourceId).getValue())
                                .distinct()
                                .collect(Collectors.joining(", "));
                    }
                });
        droppedSamples.mark(samples.size());
    }

    @Override
    public void onEvent(SampleBatchEvent event) {
        if (thePartyIsOver.get()) {
            return;
        }
        try(Timer.Context context = this.sampleWriteTsTimer.time()){
            var start =  Instant.now();
            var timeSeriesStorage = this.storage.get();
            var getDuration = Duration.between(start, Instant.now());

            if (getDuration.compareTo(STORAGE_GET_WARNING_DURATION) > 0) {
                RATE_LIMITED_LOGGER.warn("storage.get() took an excessive amount of time, {}, and returned: {}", getDuration, timeSeriesStorage);
            }

            if (timeSeriesStorage == null) {
                RATE_LIMITED_LOGGER.error("There is no available TimeSeriesStorage implementation. {} samples will be lost.", event.getSamples().size());
            } else {
                timeSeriesStorage.store(event.getSamples());
                this.stats.record(event.getSamples());
            }
        } catch (Throwable t) {
            RATE_LIMITED_LOGGER.error("An error occurred while inserting samples. Up to {} samples may be lost: {}: {}", event.getSamples().size(), t.getClass().getSimpleName(), t.getMessage(), t);
        } finally {
            event.setSamples(null); // free sample reference for garbage collection
        }

        // Decrement our entry counter
        numEntriesOnRingBuffer.decrementAndGet();
    }

    private static final EventTranslatorOneArg<SampleBatchEvent, List<Sample>> TRANSLATOR = (event, sequence, samples) -> event.setSamples(samples);

    public void setTimeSeriesStorage(final TimeseriesStorageManager timeseriesStorage) {
        this.storage = timeseriesStorage;
    }

    public void setStats(StatisticsCollector stats) {
        this.stats = stats;
    }
}
