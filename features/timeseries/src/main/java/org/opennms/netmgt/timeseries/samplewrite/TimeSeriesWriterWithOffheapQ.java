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

    private final QueueFileOffHeapDispatchQueue<List<Sample>> queue;
    private final TimeseriesStorageManager storage;
    private final List<Thread> workerPool = new ArrayList<>();

    private boolean isActive = true;

    public TimeSeriesWriterWithOffheapQ(
            final TimeseriesStorageManager storage,
            @Named("timeseries.ring_buffer_size") final Integer ringBufferSize,
            @Named("timeseries.writer_threads") final Integer numWriterThreads) {
        // TODO: Patrick: we should probaply get the QueueFileOffHeapDispatchQueueFactory injected and create the queue via the factory


        // for testing: TODO: Patrick remove later
        int batchSize = 2;
        int inMemoryQueueSize = batchSize * 2;
        try {
            String basePath = System.getProperty("karaf.data", "/tmp");
            queue = new QueueFileOffHeapDispatchQueue<>(createSerializer(), createDeSerializer(),
                    "org.opennms.features.timeseries",
                    Paths.get(basePath),
                    inMemoryQueueSize,
                    batchSize,
            Long.MAX_VALUE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.storage = Objects.requireNonNull(storage);

        // Set up consumer side
        for(int i = 0; i < numWriterThreads; i++) {
            Thread consumerThread = new Thread(this::work);
            this.workerPool.add(consumerThread);
            consumerThread.start();
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
        try {
            queue.enqueue(samples, UUID.randomUUID().toString());
        } catch (WriteFailedException e) {
            LOG.warn("Could not insert list of samples.", e);
        }
    }

    public void destroy() {
        this.isActive = false;
        for(Thread thread : this.workerPool) {
            thread.interrupt();
        }
    }

        private void work() {
            List<Sample> samples;
            while (isActive) {
                try {
                    samples = this.queue.dequeue().getValue();
                } catch (InterruptedException e) {
                    return; // we are done.
                }
                sentToPlugin(samples);
            }
        }

        private void sentToPlugin(final List<Sample> samples) {
            while (isActive) {
                try {
                    this.storage.get().store(samples);
                    return; // we are done.
                } catch (Exception e) {
                    long wait = 1000;
                    LOG.warn("Could not send samples to plugin, will try again in {} ms.", wait, e);
                }
            }
        }
}
