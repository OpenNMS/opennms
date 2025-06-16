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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.MetaTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TagMatcher;
import org.opennms.integration.api.v1.timeseries.TimeSeriesData;
import org.opennms.integration.api.v1.timeseries.TimeSeriesFetchRequest;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.stats.StatisticsCollector;
import org.opennms.netmgt.timeseries.stats.StatisticsCollectorImpl;
import org.opennms.newts.api.Resource;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

public class RingBufferTimeseriesWriterTest {

    private TimeseriesStorageManager storageManager;

    @Before
    public void setUp(){
        this.storageManager = Mockito.mock(TimeseriesStorageManager.class);
    }

    /**
     * Uses a latch to verify that multiple that multiple threads
     * are used to concurrently insert samples into the SampleRepository.
     */
    @Test
    public void canWriteToSampleRepositoryUsingMultipleThreads() throws Exception {
        int ringBufferSize = 1024;
        int numWriterThreads = 8;

        LatchedTimeseriesStorage store = new LatchedTimeseriesStorage(numWriterThreads);
        MetricRegistry registry = new MetricRegistry();
        RingBufferTimeseriesWriter writer = new RingBufferTimeseriesWriter(storageManager, new StatisticsCollectorImpl(numWriterThreads), ringBufferSize, numWriterThreads, registry);
        when(storageManager.get()).thenReturn(store);
        writer.setTimeSeriesStorage(storageManager);

        Metric metric = createMetric().build();
        for (int i = 0; i < ringBufferSize*2; i++) {
            Sample s = ImmutableSample.builder()
            .metric(metric)
            .time(Instant.now())
            .value((double)i).build();
            writer.insert(Lists.newArrayList(s));
        }
    }

    /**
     * Fills the ring buffer and locks all of the writer threads to verify
     * that samples additional samples are dropped.
     */
    @Test
    public void samplesAreDroppedWhenRingBufferIsFull() throws Exception {
        Resource x = new Resource("x");
        int ringBufferSize = 1024;
        int numWriterThreads = 8;

        Lock lock = new ReentrantLock();
        LockedTimeseriesStorage timeseriesStorage = new LockedTimeseriesStorage(lock);
        MetricRegistry registry = new MetricRegistry();
        RingBufferTimeseriesWriter writer = new RingBufferTimeseriesWriter(storageManager, new StatisticsCollectorImpl(numWriterThreads), ringBufferSize, numWriterThreads, registry);
        when(storageManager.get()).thenReturn(timeseriesStorage);
        writer.setTimeSeriesStorage(storageManager);

        lock.lock();
        Metric metric = createMetric().build();
        for (int i = 0; i < ringBufferSize; i++) {
            Sample s = ImmutableSample.builder()
                    .metric(metric)
                    .time(Instant.now())
                    .value((double)i).build();
            writer.insert(Lists.newArrayList(s));
        }

        // The ring buffer should be full, and all of the threads should be locked
        Thread.sleep(250);
        assertEquals(numWriterThreads, timeseriesStorage.getNumThreadsLocked());

        // Attempt to insert another batch of samples
        for (int i = 0; i < 8; i++) {
            Sample s = ImmutableSample.builder()
                    .metric(metric)
                    .time(Instant.now())
                    .value((double)i).build();
            writer.insert(Lists.newArrayList(s));
        };

        // Unlock the writer threads and wait for the ring buffer to drain
        lock.unlock();
        writer.destroy();

        // Verify the number of inserted samples
        assertEquals(0, timeseriesStorage.getNumThreadsLocked());
        assertEquals(ringBufferSize, timeseriesStorage.getNumSamplesInserted());
    }

    private static class LatchedTimeseriesStorage extends MockTimeSeriesStorage {
        private final CountDownLatch latch;

        public LatchedTimeseriesStorage(int N) {
            latch = new CountDownLatch(N);
        }

        @Override
        public void store(List<Sample> samples) {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static class LockedTimeseriesStorage extends MockTimeSeriesStorage {
        private final Lock lock;
        private final AtomicInteger numThreadsLocked = new AtomicInteger(0);
        private final AtomicInteger numSamplesInserted = new AtomicInteger(0);

        public LockedTimeseriesStorage(Lock lock) {
            this.lock = lock;
        }

        public int getNumThreadsLocked() {
            return numThreadsLocked.get();
        }

        public int getNumSamplesInserted() {
            return numSamplesInserted.get();
        }

        @Override
        public void store(List<Sample> samples) throws StorageException {
            numThreadsLocked.incrementAndGet();
            lock.lock();
            numSamplesInserted.addAndGet(samples.size());
            lock.unlock();
            numThreadsLocked.decrementAndGet();
        }
    }

    private ImmutableMetric.MetricBuilder createMetric() {
        return ImmutableMetric
                .builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, "a/b")
                .intrinsicTag(IntrinsicTagNames.name, "c")
                .intrinsicTag(MetaTagNames.mtype, Metric.Mtype.counter.name());
    }

    static class MockTimeSeriesStorage implements TimeSeriesStorage {

        @Override
        public void store(List<Sample> samples) throws StorageException {
            // Do nothing, we are a mock
        }

        @Override
        public List<Metric> findMetrics(Collection<TagMatcher> tagMatchers) throws StorageException {
            return null;
        }

        @Override
        @Deprecated
        public List<Sample> getTimeseries(TimeSeriesFetchRequest request) throws StorageException {
            return null;
        }

        @Override
        public TimeSeriesData getTimeSeriesData(TimeSeriesFetchRequest request) {
            return null;
        }

        @Override
        public void delete(Metric metric) throws StorageException {
            // Do nothing, we are a mock
        }
    }
}
