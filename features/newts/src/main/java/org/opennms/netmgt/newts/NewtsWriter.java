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

package org.opennms.netmgt.newts;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.logging.Logging;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;

/**
 * Used to write samples to the {@link org.opennms.newts.api.SampleRepository}.
 *
 * Calls to {@link #insert()} publish the samples to a ring buffer so
 * that they don't block while the data is being persisted.
 *
 * @author jwhite
 */
public class NewtsWriter implements WorkHandler<SampleBatchEvent>, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsWriter.class);

    @Autowired
    private SampleRepository m_sampleRepository;

    private WorkerPool<SampleBatchEvent> m_workerPool;

    private RingBuffer<SampleBatchEvent> m_ringBuffer;

    private final int m_maxBatchSize;

    private final int m_ringBufferSize;

    private final int m_numWriterThreads;

    /**
     * The {@link RingBuffer} doesn't appear to expose any methods that indicate the number
     * of elements that are currently "queued", so we keep track of them with this atomic counter.
     */
    private final AtomicLong m_numSamplesOnRingBuffer = new AtomicLong();

    @Inject
    public NewtsWriter(@Named("newts.max_batch_size") Integer maxBatchSize, @Named("newts.ring_buffer_size") Integer ringBufferSize,
            @Named("newts.writer_threads") Integer numWriterThreads, MetricRegistry registry) {
        Preconditions.checkArgument(maxBatchSize > 0, "maxBatchSize must be strictly positive");
        Preconditions.checkArgument(ringBufferSize > 0, "ringBufferSize must be positive");
        Preconditions.checkArgument(DoubleMath.isMathematicalInteger(Math.log(ringBufferSize) / Math.log(2)), "ringBufferSize must be a power of two");
        Preconditions.checkArgument(numWriterThreads > 0, "numWriterThreads must be positive");
        Preconditions.checkNotNull(registry, "metric registry");

        m_maxBatchSize = maxBatchSize;
        m_ringBufferSize = ringBufferSize;
        m_numWriterThreads = numWriterThreads;
        m_numSamplesOnRingBuffer.set(0L);

        registry.register(MetricRegistry.name("ring-buffer", "size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return m_numSamplesOnRingBuffer.get();
                    }
                });
        registry.register(MetricRegistry.name("ring-buffer", "max-size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return Long.valueOf(m_ringBufferSize);
                    }
                });

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
        // Add the samples to the ring buffer
        if (!m_ringBuffer.tryPublishEvent(TRANSLATOR, samples)) {
            String uniqueResourceIds = samples.stream()
                    .map(s -> s.getResource().getId())
                    .distinct()
                    .collect(Collectors.joining(", "));
            LOG.error("The ring buffer is full. {} samples associated with resource ids {} will be dropped.",
                    samples.size(), uniqueResourceIds);
            return;
        }
        // Increase our sample counter
        m_numSamplesOnRingBuffer.addAndGet(samples.size());
    }

    @Override
    public void onEvent(SampleBatchEvent event) throws Exception {
        // We'd expect the logs from this thread to be in collectd.log
        Logging.putPrefix("collectd");

        List<Sample> samples = event.getSamples();
        // Decrement our sample counter
        m_numSamplesOnRingBuffer.addAndGet(-samples.size());

        // Partition the samples into collections smaller then max_batch_size
        for (List<Sample> batch : Lists.partition(samples, m_maxBatchSize)) {
            try {
                LOG.debug("Inserting {} samples", batch.size());
                m_sampleRepository.insert(batch);

                if (LOG.isDebugEnabled()) {
                    String uniqueResourceIds = batch.stream()
                        .map(s -> s.getResource().getId())
                        .distinct()
                        .collect(Collectors.joining(", "));
                    LOG.debug("Successfully inserted samples for resources with ids {}", uniqueResourceIds);
                }
            } catch (Throwable t) {
                LOG.error("An error occurred while inserting the samples. They will be lost.", t);
            }
        }
    }

    private static final EventTranslatorOneArg<SampleBatchEvent, List<Sample>> TRANSLATOR =
            new EventTranslatorOneArg<SampleBatchEvent, List<Sample>>() {
                public void translateTo(SampleBatchEvent event, long sequence, List<Sample> samples) {
                    event.setSamples(samples);
                }
            };

    @VisibleForTesting
    public void setSampleRepository(SampleRepository sampleRepository) {
        m_sampleRepository = sampleRepository;
    }
}
