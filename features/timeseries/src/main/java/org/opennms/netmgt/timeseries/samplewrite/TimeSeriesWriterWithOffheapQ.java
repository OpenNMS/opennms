/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Named;

import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.opennms.core.ipc.sink.offheap.QueueFileOffHeapDispatchQueue;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeriesWriterWithOffheapQ implements TimeSeriesWriter {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesWriterWithOffheapQ.class);

    private final ArrayList<QueueFileOffHeapDispatchQueue<Sample>> queues;
    private final TimeseriesStorageManager storage;
    private final List<Thread> workerPool = new ArrayList<>();

    private boolean isActive = true;

    public TimeSeriesWriterWithOffheapQ(
            final TimeseriesStorageManager storage,
            @Named("timeseries.ring_buffer_size") final Integer ringBufferSize,
            @Named("timeseries.writer_threads") final Integer numWriterThreads) {
        this.storage = Objects.requireNonNull(storage);

        // config: TODO: Patrick externalize
        final int queueAmount = 4;
        final int batchSize = 64; // for writing to disk

        // Set up Q's
        queues = new ArrayList<>(queueAmount);
        for (int queueIndex = 0; queueIndex < queueAmount; queueIndex++) {
            queues.add(queueIndex, createQueue(queueIndex, batchSize, ringBufferSize));
            setupConsumerThreads(queueIndex, numWriterThreads);
        }
    }

    private void setupConsumerThreads(int queueIndex, int numWriterThreads) {
        for(int i = 0; i < numWriterThreads; i++) {
            Thread consumerThread = new Thread(() -> this.work(queueIndex));
            this.workerPool.add(consumerThread);
            consumerThread.start();
        }
    }

    private QueueFileOffHeapDispatchQueue<Sample> createQueue(
            final int queueIndex,
            final int batchSize,
            final int inMemoryQueueSize
            ) {
        try {
            String basePath = System.getProperty("karaf.data", "/tmp");
            return new QueueFileOffHeapDispatchQueue<>(createSerializer(), createDeSerializer(),
                    "org.opennms.features.timeseries-" + queueIndex,
                    Paths.get(basePath),
                    inMemoryQueueSize,
                    batchSize,
                    Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Function<byte[], T> createDeSerializer() {
        return t -> {
            ByteArrayInputStream bs = new ByteArrayInputStream(t);
            try {
                return (T) new ObjectInputStream(bs).readObject();
            } catch (IOException | ClassNotFoundException e) {
                // shouldn't happen since we have no io
                throw new RuntimeException(e);
            }
        };
    }

    private <T> Function<T, byte[]> createSerializer() {
        return t -> {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            try {
                new ObjectOutputStream(bs).writeObject(t);
            } catch (IOException e) {
                // shouldn't happen since we have no io
                throw new RuntimeException(e);
            }
            return bs.toByteArray();
        };
    }

    @Override
    public void insert(List<Sample> samples) {
        LOG.info("Storing {} samples", samples.size());

        for(Sample sample: samples) {
            try {
                int index = toQueueIndex(sample);
                queues.get(index).enqueue(sample, UUID.randomUUID().toString());
            } catch (WriteFailedException e) {
                LOG.warn("Could not insert list of samples.", e);
            }
        }
    }

    private int toQueueIndex(final Sample sample) {
        return Math.abs(sample.getMetric().getKey().hashCode()) % this.queues.size();
    }

    public void destroy() {
        this.isActive = false;
        for(Thread thread : this.workerPool) {
            thread.interrupt();
        }
    }

    private void work(int queueIndex) {
        Sample sample;
        while (isActive) {
            try {
                sample = this.queues.get(queueIndex).dequeue().getValue();
            } catch (InterruptedException e) {
                return; // we are done.
            }
            sentToPlugin(sample);
        }
    }

    private void sentToPlugin(final Sample sample) {
        while (isActive) {
            try {
                // TODO: Patrick change contract to allow sending one sample?
                this.storage.get().store(Collections.singletonList(sample));
                return; // we are done.
            } catch (Exception e) {
                long wait = 1000; // TODO: Patrick make wait duration increasing
                LOG.warn("Could not send samples to plugin, will try again in {} ms.", wait, e); // TODO: Patrick rate limit logging
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException ex) {
                    LOG.warn("Could not send samples to plugin, got InterruptedException.", e);
                    return;
                }
            }
        }
    }
}
