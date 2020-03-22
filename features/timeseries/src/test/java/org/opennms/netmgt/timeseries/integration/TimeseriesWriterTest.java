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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.Tag;
import org.opennms.netmgt.timeseries.api.domain.TimeSeriesFetchRequest;
import org.opennms.netmgt.timeseries.integration.TimeseriesWriter;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.opennms.newts.api.Counter;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class TimeseriesWriterTest {

    /**
     * Uses a latch to verify that multiple that multiple threads
     * are used to concurrently insert samples into the SampleRepository.
     */
    @Test
    public void canWriteToSampleRepositoryUsingMultipleThreads() {
        int ringBufferSize = 1024;
        int numWriterThreads = 8;

        LatchedTimeseriesStorage store = new LatchedTimeseriesStorage(numWriterThreads);
        MetricRegistry registry = new MetricRegistry();
        TimeseriesWriter writer = new TimeseriesWriter(1, ringBufferSize, numWriterThreads, registry);
        writer.setTimeSeriesStorage(store);

        for (int i = 0; i < ringBufferSize*2; i++) {
            Resource x = new Resource("x");
            Sample s = new Sample(Timestamp.now(), x, "y", MetricType.COUNTER, new Counter(i));
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
        TimeseriesWriter writer = new TimeseriesWriter(1, ringBufferSize, numWriterThreads, registry);
        writer.setTimeSeriesStorage(timeseriesStorage);
        writer.setTimeSeriesMetaDataDao(Mockito.mock(TimeSeriesMetaDataDao.class));

        lock.lock();
        for (int i = 0; i < ringBufferSize; i++) {
            Sample s = new Sample(Timestamp.now(), x, "y", MetricType.COUNTER, new Counter(i));
            writer.insert(Lists.newArrayList(s));
        }

        // The ring buffer should be full, and all of the threads should be locked
        Thread.sleep(250);
        assertEquals(numWriterThreads, timeseriesStorage.getNumThreadsLocked());

        // Attempt to insert another batch of samples
        for (int i = 0; i < 8; i++) {
            Sample s = new Sample(Timestamp.now(), x, "y", MetricType.COUNTER, new Counter(i));
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
        public void store(List<org.opennms.netmgt.timeseries.api.domain.Sample> samples) {
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
        public void store(List<org.opennms.netmgt.timeseries.api.domain.Sample> samples) throws StorageException {
            numThreadsLocked.incrementAndGet();
            lock.lock();
            numSamplesInserted.addAndGet(samples.size());
            lock.unlock();
            numThreadsLocked.decrementAndGet();
        }
    }

    private static class MockTimeSeriesStorage implements TimeSeriesStorage {

        @Override
        public void store(List<org.opennms.netmgt.timeseries.api.domain.Sample> samples) throws StorageException {
            // Do nothing, we are a mock
        }

        @Override
        public List<Metric> getMetrics(Collection<Tag> tags) throws StorageException {
            return null;
        }

        @Override
        public List<org.opennms.netmgt.timeseries.api.domain.Sample> getTimeseries(TimeSeriesFetchRequest request) throws StorageException {
            return null;
        }

        @Override
        public void delete(Metric metric) throws StorageException {
            // Do nothing, we are a mock
        }
    }
}
