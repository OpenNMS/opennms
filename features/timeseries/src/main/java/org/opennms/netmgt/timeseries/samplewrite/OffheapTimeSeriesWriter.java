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