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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.swrve.ratelimitedlogger.RateLimitedLog;
import org.nustaq.serialization.FSTConfiguration;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.opennms.core.ipc.sink.offheap.DataBlocksOffHeapQueue;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class OffheapTimeSeriesWriter implements TimeseriesWriter {
    private static final Logger LOG = LoggerFactory.getLogger(OffheapTimeSeriesWriter.class);
    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();
    public static final String OFFHEAP_NAME = "offheap";
    public static final int RETRY_TIME = 500;

    private static FSTConfiguration fstConf = FSTConfiguration.createDefaultConfiguration();

    static {
        fstConf.registerClass(ArrayList.class, ImmutableSample.class);
    }

    private final DispatchQueue<List<Sample>> queue;
    private final TimeseriesStorageManager storage;
    private final List<Thread> workerPool = new ArrayList<>();
    private final TimeseriesWriterConfig timeseriesWriterConfig;

    private boolean isActive = true;

    private final Meter droppedSamples;

    private final Timer sampleWriteTsTimer;

    public OffheapTimeSeriesWriter(
            final TimeseriesStorageManager storage,
            final TimeseriesWriterConfig timeseriesWriterConfig,
            @Named("timeseriesMetricRegistry") MetricRegistry registry) {

        this.storage = Objects.requireNonNull(storage);
        Objects.requireNonNull(registry);
        this.timeseriesWriterConfig = Objects.requireNonNull(timeseriesWriterConfig);


        registry.register(MetricRegistry.name(OFFHEAP_NAME, "max-size"),
                (Gauge<Integer>) this.timeseriesWriterConfig::getBufferSize);

        droppedSamples = registry.meter(MetricRegistry.name(OFFHEAP_NAME, "dropped-samples"));
        sampleWriteTsTimer = registry.timer(MetricRegistry.name(OFFHEAP_NAME, "samples.write.ts"));

        LOG.info("ringBufferSize: {}, numWriterThreads: {}, batchSize: {}, path: {}, maxFileSize: {}",
                timeseriesWriterConfig.getBufferSize(), timeseriesWriterConfig.getNumWriterThreads(),
                timeseriesWriterConfig.getBatchSize(), timeseriesWriterConfig.getPath(),
                timeseriesWriterConfig.getMaxFileSize());

        // Set up Q's
        this.queue = createQueue(timeseriesWriterConfig);
        setupConsumerThreads(timeseriesWriterConfig.getNumWriterThreads());

        // must register after queue create
        registry.register(MetricRegistry.name(OFFHEAP_NAME, "size"),
                (Gauge<Integer>) queue::getSize);
    }

    private void setupConsumerThreads(int numWriterThreads) {
        for (int i = 0; i < numWriterThreads; i++) {
            Thread consumerThread = new Thread(this::work);
            this.workerPool.add(consumerThread);
            consumerThread.start();
        }
    }

    private DataBlocksOffHeapQueue<List<Sample>> createQueue(
            TimeseriesWriterConfig timeseriesWriterConfig) throws QueueCreateFailedException {
        return new DataBlocksOffHeapQueue<>(createSerializer(), createDeSerializer(),
                "org.opennms.features.timeseries",
                Paths.get(timeseriesWriterConfig.getPath()),
                timeseriesWriterConfig.getBufferSize(),
                timeseriesWriterConfig.getBatchSize(),
                timeseriesWriterConfig.getMaxFileSize());
    }

    private <T> Function<byte[], T> createDeSerializer() {
        return t -> (T) fstConf.asObject(t);
    }

    private <T> Function<T, byte[]> createSerializer() {
        return t -> fstConf.asByteArray(t);
    }

    @Override
    public void insert(List<Sample> samples) {
        try {
            queue.enqueue(samples, UUID.randomUUID().toString());
        } catch (WriteFailedException e) {
            RATE_LIMITED_LOGGER.warn("Could not insert list of samples.", e);
            droppedSamples.mark(samples.size());
        }
    }

    public void destroy() {
        this.isActive = false;
        for (Thread thread : this.workerPool) {
            thread.interrupt();
        }
    }

    private void work() {
        while (isActive) {
            try (Timer.Context context = this.sampleWriteTsTimer.time()) {
                var samples = queue.dequeue().getValue();
                sentToPlugin(samples);
                RATE_LIMITED_LOGGER.debug("Storing {} samples", samples.size());
            } catch (InterruptedException e) {
                return; // we are done.
            }
        }
    }

    private void sentToPlugin(final List<Sample> samples) {
        while (isActive) {
            try {
                this.storage.get().store(samples);
                return; // we are done.
            } catch (StorageException e) {
                RATE_LIMITED_LOGGER.warn("Could not send samples to plugin, will try again in {} ms.", RETRY_TIME, e);
                try {
                    Thread.sleep(RETRY_TIME);
                } catch (InterruptedException ex) {
                    RATE_LIMITED_LOGGER.error("Could not send samples to plugin, got InterruptedException.", e);
                    return;
                }
            }
        }
    }
}