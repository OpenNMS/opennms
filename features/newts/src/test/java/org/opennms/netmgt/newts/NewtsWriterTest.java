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
package org.opennms.netmgt.newts;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Counter;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.SampleRepository;
import org.opennms.newts.api.SampleSelectCallback;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class NewtsWriterTest {

    /**
     * Uses a latch to verify that multiple that multiple threads
     * are used to concurrently insert samples into the SampleRepository.
     */
    @Test
    public void canWriteToSampleRepositoryUsingMultipleThreads() {
        int ringBufferSize = 1024;
        int numWriterThreads = 8;

        LatchedSampleRepository sampleRepo = new LatchedSampleRepository(numWriterThreads);
        MetricRegistry registry = new MetricRegistry();
        NewtsWriter writer = new NewtsWriter(1, ringBufferSize, numWriterThreads, registry);
        writer.setSampleRepository(sampleRepo);

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
        LockedSampleRepository sampleRepo = new LockedSampleRepository(lock);
        MetricRegistry registry = new MetricRegistry();
        NewtsWriter writer = new NewtsWriter(1, ringBufferSize, numWriterThreads, registry);
        writer.setSampleRepository(sampleRepo);

        lock.lock();
        for (int i = 0; i < ringBufferSize; i++) {
            Sample s = new Sample(Timestamp.now(), x, "y", MetricType.COUNTER, new Counter(i));
            writer.insert(Lists.newArrayList(s));
        }

        // The ring buffer should be full, and all of the threads should be locked
        Thread.sleep(250);
        assertEquals(numWriterThreads, sampleRepo.getNumThreadsLocked());

        // Attempt to insert another batch of samples
        for (int i = 0; i < 8; i++) {
            Sample s = new Sample(Timestamp.now(), x, "y", MetricType.COUNTER, new Counter(i));
            writer.insert(Lists.newArrayList(s));
        };

        // Unlock the writer threads and wait for the ring buffer to drain
        lock.unlock();
        writer.destroy();

        // Verify the number of inserted samples
        assertEquals(0, sampleRepo.getNumThreadsLocked());
        assertEquals(ringBufferSize, sampleRepo.getNumSamplesInserted());
    }

    private static class LatchedSampleRepository extends MockSampleRepository {
        private final CountDownLatch latch;

        public LatchedSampleRepository(int N) {
            latch = new CountDownLatch(N);
        }

        @Override
        public void insert(Collection<Sample> samples, boolean calculateTimeToLive) {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static class LockedSampleRepository extends MockSampleRepository {
        private final Lock lock;
        private final AtomicInteger numThreadsLocked = new AtomicInteger(0);
        private final AtomicInteger numSamplesInserted = new AtomicInteger(0);

        public LockedSampleRepository(Lock lock) {
            this.lock = lock;
        }

        @Override
        public void insert(Collection<Sample> samples, boolean calculateTimeToLive) {
            numThreadsLocked.incrementAndGet();
            lock.lock();
            numSamplesInserted.addAndGet(samples.size());
            lock.unlock();
            numThreadsLocked.decrementAndGet();
        }

        public int getNumThreadsLocked() {
            return numThreadsLocked.get();
        }

        public int getNumSamplesInserted() {
            return numSamplesInserted.get();
        }
    }

    private static class MockSampleRepository implements SampleRepository {
        @Override
        public void insert(Collection<Sample> samples) {
            insert(samples, false);
        }

        @Override
        public void insert(Collection<Sample> samples, boolean calculateTimeToLive) {
            // pass
        }

        @Override
        public Results<Measurement> select(Context context, Resource resource, Optional<Timestamp> start,
                Optional<Timestamp> end, ResultDescriptor descriptor, Optional<Duration> resolution) {
            return null;
        }

        @Override
        public Results<Measurement> select(Context context, Resource resource, Optional<Timestamp> start,
                Optional<Timestamp> end, ResultDescriptor descriptor, Optional<Duration> resolution,
                SampleSelectCallback callback) {
            return null;
        }

        @Override
        public Results<Sample> select(Context context, Resource resource, Optional<Timestamp> start,
                Optional<Timestamp> end) {
            return null;
        }

        @Override
        public void delete(Context context, Resource resource) {
            // pass
        }
    }
}
