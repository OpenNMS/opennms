/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.core.logging.Logging;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * Used to write samples to the {@link org.opennms.newts.api.SampleRepository}.
 *
 * Calls to {@link #insert()} publish the samples to a ring buffer so
 * that they don't block while the data is being persisted.
 *
 * @author jwhite
 */
public class NewtsWriter implements EventHandler<SampleBatchEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsWriter.class);

    @Autowired
    private SampleRepository m_sampleRepository;

    private RingBuffer<SampleBatchEvent> m_ringBuffer;

    private final int m_maxBatchSize;

    private final int m_ringBufferSize;

    @Inject
    public NewtsWriter(@Named("newts.max_batch_size") Integer maxBatchSize, @Named("newts.ring_buffer_size") Integer ringBufferSize)  {
        Preconditions.checkArgument(maxBatchSize > 0, "max_batch_size must be strictly positive");
        Preconditions.checkArgument(ringBufferSize >= 0, "ringBufferSize must be positive");
        Preconditions.checkArgument(DoubleMath.isMathematicalInteger(Math.log(ringBufferSize) / Math.log(2)), "ringBufferSize must be a power of two");

        m_maxBatchSize = maxBatchSize;
        m_ringBufferSize = ringBufferSize;

        LOG.debug("Using max_batch_size: {} and ring_buffer_size: {}", maxBatchSize, m_ringBufferSize);
        setUpDisruptor();
    }

    @SuppressWarnings("unchecked")
    private void setUpDisruptor() {
        // Executor that will be used to construct new threads for consumers
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("NewtsWriter-Consumer-%d").build();
        Executor executor = Executors.newCachedThreadPool(namedThreadFactory);

        // Construct the Disruptor
        Disruptor<SampleBatchEvent> disruptor = new Disruptor<>(SampleBatchEvent::new, m_ringBufferSize, executor);

        // Connect the handler
        disruptor.handleEventsWith(this);

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        m_ringBuffer = disruptor.getRingBuffer();
    }

    public void insert(List<Sample> samples) {
        // Add the samples to the ring buffer
        m_ringBuffer.publishEvent(TRANSLATOR, samples);
    }

    @Override
    public void onEvent(SampleBatchEvent event, long sequence, boolean endOfBatch) {
        // We'd expect the logs from this thread to be in collectd.log
        Logging.putPrefix("collectd");

        // Partition the samples into collections smaller then max_batch_size
        List<Sample> samples = event.getSamples();
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
}
