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

package org.opennms.netmgt.timeseries.integration;

import java.sql.SQLException;
import java.time.Instant;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.logging.Logging;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.netmgt.timeseries.impl.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.meta.MetaData;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
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
 * Used to write samples to the {@link org.opennms.newts.api.SampleRepository}.
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

    @Autowired
    private TimeseriesStorageManager storage;

    @Autowired
    private TimeSeriesMetaDataDao timeSeriesMetaDataDao;

    /**
     * The {@link RingBuffer} doesn't appear to expose any methods that indicate the number
     * of elements that are currently "queued", so we keep track of them with this atomic counter.
     */
    private final AtomicLong numEntriesOnRingBuffer = new AtomicLong();

    @Inject
    public TimeseriesWriter(@Named("timeseries.max_batch_size") Integer maxBatchSize, @Named("timeseries.ring_buffer_size") Integer ringBufferSize,
                            @Named("timeseries.writer_threads") Integer numWriterThreads, @Named("timeseriesMetricRegistry") MetricRegistry registry) {
        Preconditions.checkArgument(maxBatchSize > 0, "maxBatchSize must be strictly positive");
        Preconditions.checkArgument(ringBufferSize > 0, "ringBufferSize must be positive");
        Preconditions.checkArgument(DoubleMath.isMathematicalInteger(Math.log(ringBufferSize) / Math.log(2)), "ringBufferSize must be a power of two");
        Preconditions.checkArgument(numWriterThreads > 0, "numWriterThreads must be positive");
        Preconditions.checkNotNull(registry, "metric registry");

        this.ringBufferSize = ringBufferSize;
        this.numWriterThreads = numWriterThreads;
        numEntriesOnRingBuffer.set(0L);

        registry.register(MetricRegistry.name("ring-buffer", "size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return numEntriesOnRingBuffer.get();
                    }
                });
        registry.register(MetricRegistry.name("ring-buffer", "max-size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return Long.valueOf(TimeseriesWriter.this.ringBufferSize);
                    }
                });

        droppedSamples = registry.meter(MetricRegistry.name("ring-buffer", "dropped-samples"));

        LOG.debug("Using max_batch_size: {} and ring_buffer_size: {}", maxBatchSize, this.ringBufferSize);
        setUpWorkerPool();

    }

    private void setUpWorkerPool() {
        // Executor that will be used to construct new threads for consumers
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("TimeseriesWriter-Consumer-%d").build();
        final Executor executor = Executors.newCachedThreadPool(namedThreadFactory);

        @SuppressWarnings("unchecked")
        final WorkHandler<SampleBatchEvent> handlers[] = new WorkHandler[numWriterThreads];
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
    public void destroy() throws Exception {
        if (workerPool != null) {
            workerPool.drainAndHalt();
        }
    }

    public void insert(List<Sample> samples) {
        pushToRingBuffer(samples, TRANSLATOR);
    }

    public void index(List<Sample> samples) {
        pushToRingBuffer(samples, INDEX_ONLY_TRANSLATOR);
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
                                    .map(s -> s.getResource().getId())
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

        if (event.isIndexOnly()) {
            storeMetadata(event);
        } else {
            storeTimeseriesData(event);
            storeMetadata(event);
        }
    }

    private void storeTimeseriesData(SampleBatchEvent event) throws StorageException {
        List<org.opennms.integration.api.v1.timeseries.Sample> samples
                = event.getSamples().stream().map(this::toApiSample).collect(Collectors.toList());
        this.storage.get().store(samples);
    }

    private void storeMetadata(SampleBatchEvent event) throws SQLException {
        // dedouble attributes
        Set<MetaData> metaData = new HashSet<>();
        for(Sample sample : event.getSamples()) {

            // attributes of sample
            if(sample.getResource().getAttributes().isPresent()) {
                sample.getResource().getAttributes().get().forEach((key, value) -> metaData.add(new MetaData(sample.getResource().getId(), key, value)));
            }
        }
        this.timeSeriesMetaDataDao.store(metaData);
    }

    private org.opennms.integration.api.v1.timeseries.Sample toApiSample(final Sample sample) {

        ImmutableMetric.MetricBuilder builder = ImmutableMetric.builder()
                .intrinsicTag(CommonTagNames.resourceId, sample.getResource().getId())
                .intrinsicTag(CommonTagNames.name, sample.getName())
                .metaTag(typeToTag(sample.getType()));

        if(sample.getResource().getAttributes().isPresent()) {
            sample.getResource().getAttributes().get().forEach(builder::metaTag);
        }

        final ImmutableMetric metric = builder.build();
        final Instant time = Instant.ofEpochMilli(sample.getTimestamp().asMillis());
        final Double value = sample.getValue().doubleValue();

        return ImmutableSample.builder().metric(metric).time(time).value(value).build();
    }

    private Tag typeToTag (final MetricType type) {
        ImmutableMetric.Mtype mtype;
        if(type == MetricType.GAUGE){
            mtype = ImmutableMetric.Mtype.gauge;
        } else if(type == MetricType.COUNTER) {
            mtype = ImmutableMetric.Mtype.count;
        } else {
            throw new IllegalArgumentException(String.format("I can't find a matching %s for %s",
                    ImmutableMetric.Mtype.class.getSimpleName(), type.toString()));
        }
        return new ImmutableTag(CommonTagNames.mtype, mtype.name());
    }

    private static final EventTranslatorOneArg<SampleBatchEvent, List<Sample>> TRANSLATOR =
            new EventTranslatorOneArg<SampleBatchEvent, List<Sample>>() {
                public void translateTo(SampleBatchEvent event, long sequence, List<Sample> samples) {
                    event.setIndexOnly(false);
                    event.setSamples(samples);
                }
            };

    private static final EventTranslatorOneArg<SampleBatchEvent, List<Sample>> INDEX_ONLY_TRANSLATOR =
            new EventTranslatorOneArg<SampleBatchEvent, List<Sample>>() {
                public void translateTo(SampleBatchEvent event, long sequence, List<Sample> samples) {
                    event.setIndexOnly(true);
                    event.setSamples(samples);
                }
            };

    public void setTimeSeriesStorage(final TimeseriesStorageManager timeseriesStorage) {
        this.storage = timeseriesStorage;
    }

    public void setTimeSeriesMetaDataDao(final TimeSeriesMetaDataDao timeSeriesMetaDataDao) {
        this.timeSeriesMetaDataDao = timeSeriesMetaDataDao;
    }
}
