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

package org.opennms.netmgt.timescale;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.Duration;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.Tag;
import org.opennms.netmgt.timeseries.meta.AttributeIdentifier;
import org.opennms.netmgt.timeseries.meta.MetaData;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.search.Indexer;
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
public class TimescaleWriter implements WorkHandler<SampleBatchEvent>, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(TimescaleWriter.class);

    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();

    private WorkerPool<SampleBatchEvent> m_workerPool;

    private RingBuffer<SampleBatchEvent> m_ringBuffer;


    private final int m_ringBufferSize;

    private final int m_numWriterThreads;

    private final Meter m_droppedSamples;

    @Autowired
    private TimeSeriesStorage storage;

    @Autowired
    private TimeSeriesMetaDataDao timeSeriesMetaDataDao;

    /**
     * The {@link RingBuffer} doesn't appear to expose any methods that indicate the number
     * of elements that are currently "queued", so we keep track of them with this atomic counter.
     */
    private final AtomicLong m_numEntriesOnRingBuffer = new AtomicLong();

    @Inject
    public TimescaleWriter(@Named("newts.max_batch_size") Integer maxBatchSize, @Named("newts.ring_buffer_size") Integer ringBufferSize,
            @Named("newts.writer_threads") Integer numWriterThreads, @Named("newtsMetricRegistry") MetricRegistry registry) {
        Preconditions.checkArgument(maxBatchSize > 0, "maxBatchSize must be strictly positive");
        Preconditions.checkArgument(ringBufferSize > 0, "ringBufferSize must be positive");
        Preconditions.checkArgument(DoubleMath.isMathematicalInteger(Math.log(ringBufferSize) / Math.log(2)), "ringBufferSize must be a power of two");
        Preconditions.checkArgument(numWriterThreads > 0, "numWriterThreads must be positive");
        Preconditions.checkNotNull(registry, "metric registry");

        m_ringBufferSize = ringBufferSize;
        m_numWriterThreads = numWriterThreads;
        m_numEntriesOnRingBuffer.set(0L);

        registry.register(MetricRegistry.name("ring-buffer", "size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return m_numEntriesOnRingBuffer.get();
                    }
                });
        registry.register(MetricRegistry.name("ring-buffer", "max-size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return Long.valueOf(m_ringBufferSize);
                    }
                });

        m_droppedSamples = registry.meter(MetricRegistry.name("ring-buffer", "dropped-samples"));

        LOG.debug("Using max_batch_size: {} and ring_buffer_size: {}", maxBatchSize, m_ringBufferSize);
        setUpWorkerPool();

    }

    private void setUpWorkerPool() {
        // Executor that will be used to construct new threads for consumers
        final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("NewtsWriter-Consumer-%d").build();
        final Executor executor = Executors.newCachedThreadPool(namedThreadFactory);

        @SuppressWarnings("unchecked")
        final WorkHandler<SampleBatchEvent> handlers[] = new WorkHandler[m_numWriterThreads];
        for (int i = 0; i < m_numWriterThreads; i++) {
            handlers[i] = this;
        }

        m_ringBuffer = RingBuffer.createMultiProducer(SampleBatchEvent::new, m_ringBufferSize);
        m_workerPool = new WorkerPool<SampleBatchEvent>(
                m_ringBuffer,
                m_ringBuffer.newBarrier(),
                new FatalExceptionHandler(),
                handlers);
        m_ringBuffer.addGatingSequences(m_workerPool.getWorkerSequences());

        m_workerPool.start(executor);
    }

    @Override
    public void destroy() throws Exception {
        if (m_workerPool != null) {
            m_workerPool.drainAndHalt();
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
        if (!m_ringBuffer.tryPublishEvent(translator, samples)) {
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
            m_droppedSamples.mark(samples.size());
            return;
        }
        // Increase our entry counter
        m_numEntriesOnRingBuffer.incrementAndGet();
    }

    @Override
    public void onEvent(SampleBatchEvent event) throws Exception {
        // We'd expect the logs from this thread to be in collectd.log
        Logging.putPrefix("collectd");

        // Decrement our entry counter
        m_numEntriesOnRingBuffer.decrementAndGet();

        storeTimeseriesData(event);
        storeMetadata(event);
    }

    private void storeTimeseriesData(SampleBatchEvent event) throws StorageException {
        List<org.opennms.netmgt.timeseries.api.domain.Sample> samples
                = event.getSamples().stream().map(this::toApiSample).collect(Collectors.toList());
        this.storage.store(samples);
    }

    private void storeMetadata(SampleBatchEvent event) throws SQLException {
        // dedouble attributes
        HashMap<AttributeIdentifier, String> attributeMap = new HashMap<>();
        for(Sample sample : event.getSamples()) {
            AttributeIdentifier attributeIdentifier = AttributeIdentifier.of(
                    sample.getResource(),
                    sample.getContext().getId(), // TODO Patrick: is context == group?
                    sample.getName(),
                    sample.getType());

            // attributes of sample
            if(sample.getAttributes() != null) {
                sample.getAttributes().forEach((key, value) -> attributeMap.put(attributeIdentifier.withAttributeName(key), value));
            }

            // attributes of resource parents
            // TODO: Patrick: how do we get access to it? Maybe we need do this at another place?
        }

        List<MetaData> metaDataList = new ArrayList<>(attributeMap.size());
        for(Map.Entry<AttributeIdentifier, String> entry : attributeMap.entrySet()) {
            metaDataList.add(new MetaData(entry.getKey().toString(), entry.getValue()));
        }

        this.timeSeriesMetaDataDao.store(metaDataList);
    }

    private org.opennms.netmgt.timeseries.api.domain.Sample toApiSample(final Sample sample) {

        Metric.MetricBuilder builder = Metric.builder()
                .tag("resourceId", sample.getResource().getId()) // TODO: Patrick centralize OpenNMS common tag names
                .tag("name", sample.getName())
                .tag(typeToTag(sample.getType()))
                .tag("unit", "ms"); // TODO Patrick: how do we get the units from the sample?

        if(sample.getResource().getAttributes().isPresent()) {
            sample.getResource().getAttributes().get().forEach(builder::metaTag);
        }

        final Metric metric = builder.build();
        final Instant time = Instant.ofEpochMilli(sample.getTimestamp().asMillis());
        final Double value = sample.getValue().doubleValue();
        // sample.getContext() TODO: Patrick: not sure if we need the context?
        return org.opennms.netmgt.timeseries.api.domain.Sample.builder().metric(metric).time(time).value(value).build();
    }

    private Tag typeToTag (MetricType type) {
        Metric.Mtype mtype;
        if(type == MetricType.GAUGE){
            mtype = Metric.Mtype.gauge;
        } else if(type == MetricType.COUNTER) {
            mtype = Metric.Mtype.count;
        } else {
            throw new IllegalArgumentException("Implement me"); // TODO: Patrick are the others even relevant?
        }
        return new Tag(Metric.MandatoryTag.mtype.name(), mtype.name());
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

    public void setSampleRepository(SampleRepository sampleRepository) {
        // TODO: Patrick: remove me
    }

    public void setIndexer(Indexer indexer) {
        // TODO: Patrick: remove me
    }
}
