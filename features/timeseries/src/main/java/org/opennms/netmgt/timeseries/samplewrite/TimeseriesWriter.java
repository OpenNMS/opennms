/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.samplewrite;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.logging.Logging;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.sampleread.SampleBatchEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

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
public class TimeseriesWriter implements WorkHandler<SampleBatchEvent>, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesWriter.class);

    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private WorkerPool<SampleBatchEvent> workerPool;

    private RingBuffer<SampleBatchEvent> ringBuffer;


    private final int ringBufferSize;

    private final int numWriterThreads;

    private final Meter droppedSamples;

    private final Timer sampleWriteTsTimer;

    @Autowired
    private TimeseriesStorageManager storage;

    /**
     * The {@link RingBuffer} doesn't appear to expose any methods that indicate the number
     * of elements that are currently "queued", so we keep track of them with this atomic counter.
     */
    private final AtomicLong numEntriesOnRingBuffer = new AtomicLong();

    @Inject
    public TimeseriesWriter(@Named("timeseries.ring_buffer_size") Integer ringBufferSize,
                            @Named("timeseries.writer_threads") Integer numWriterThreads, @Named("timeseriesMetricRegistry") MetricRegistry registry) {
        Preconditions.checkArgument(ringBufferSize > 0, "ringBufferSize must be positive");
        Preconditions.checkArgument(DoubleMath.isMathematicalInteger(Math.log(ringBufferSize) / Math.log(2)), "ringBufferSize must be a power of two");
        Preconditions.checkArgument(numWriterThreads > 0, "numWriterThreads must be positive");
        Preconditions.checkNotNull(registry, "metric registry");

        this.ringBufferSize = ringBufferSize;
        this.numWriterThreads = numWriterThreads;
        numEntriesOnRingBuffer.set(0L);

        registry.register(MetricRegistry.name("ring-buffer", "size"),
                (Gauge<Long>) numEntriesOnRingBuffer::get);
        registry.register(MetricRegistry.name("ring-buffer", "max-size"),
                (Gauge<Long>) () -> (long) TimeseriesWriter.this.ringBufferSize);

        droppedSamples = registry.meter(MetricRegistry.name("ring-buffer", "dropped-samples"));
        sampleWriteTsTimer = registry.timer("samples.write.ts");

        LOG.debug("Using ring_buffer_size: {}", this.ringBufferSize);
        setUpWorkerPool();

    }

    private void setUpWorkerPool() {
        // Executor that will be used to construct new threads for consumers
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("TimeseriesWriter-Consumer-%d").build();
        final Executor executor = Executors.newCachedThreadPool(namedThreadFactory);

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
    }

    @Override
    public void destroy() {
        if (workerPool != null) {
            workerPool.drainAndHalt();
        }
    }

    public void insert(List<Sample> samples) {
        pushToRingBuffer(samples, TRANSLATOR);
    }

    private void pushToRingBuffer(List<Sample> samples, EventTranslatorOneArg<SampleBatchEvent, List<Sample>> translator) {
        // Add the samples to the ring buffer
        if (!ringBuffer.tryPublishEvent(translator, samples)) {
            RATE_LIMITED_LOGGER.error("The ring buffer is full. {} samples associated with resource ids {} will be dropped.",
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
            return;
        }
        // Increase our entry counter
        numEntriesOnRingBuffer.incrementAndGet();
    }

    @Override
    public void onEvent(SampleBatchEvent event) throws Exception {
        // We'd expect the logs from this thread to be in collectd.log
        Logging.putPrefix("collectd");

        // Decrement our entry counter
        numEntriesOnRingBuffer.decrementAndGet();

        try(Timer.Context context = this.sampleWriteTsTimer.time()){
        this.storage.get().store(event.getSamples());
        } catch (Throwable t) {
            RATE_LIMITED_LOGGER.error("An error occurred while inserting samples. Some sample may be lost.", t);
        }
    }

    private static final EventTranslatorOneArg<SampleBatchEvent, List<Sample>> TRANSLATOR = (event, sequence, samples) -> event.setSamples(samples);

    public void setTimeSeriesStorage(final TimeseriesStorageManager timeseriesStorage) {
        this.storage = timeseriesStorage;
    }
}
