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
package org.opennms.core.ipc.sink.offheap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;

/**
 * NMS-20271. Before the fix, enqueue()'s backpressure wait synchronized on the current tailBlock
 * itself, the same monitor MemoryDataBlock/OffHeapDataBlock use to guard their own synchronized
 * enqueue()/dequeue()/size() methods, and dequeue() drained a block with the blocking
 * BlockingQueue.take() rather than poll(). With a small block size, head and tail are the same
 * block for most of the run, so producers checking isFull() under the tailBlock monitor and the
 * single consumer draining that same block under headLock+tailLock contend on one shared lock, and
 * a consumer that wins the race onto an instant-drained block can wait in take() past the point the
 * block is retired. Like the off-heap transition stalls, this is silent rather than a JVM-visible
 * deadlock, so the test asserts on forward progress rather than completion.
 */
public class DataBlocksOffHeapQueueBackpressureDeadlockTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final int PRODUCERS = 8;
    private static final int CONSUMERS = 8;
    private static final int PER_PRODUCER = 4000;
    private static final int TOTAL = PRODUCERS * PER_PRODUCER;
    private static final long STALL_MILLIS = 10_000L;

    @Test(timeout = 60_000)
    public void doesNotStallOnSharedTailBlockMonitor() throws Exception {
        // batchSize 1 with a large in-memory ceiling: every message gets its own block and the
        // queue never spills off-heap, so head and tail coincide constantly and every enqueue()
        // backpressure check and dequeue() race for the same tailBlock monitor pre-fix.
        final DispatchQueue<String> queue = newQueue(1_000_000, 1);

        final AtomicLong enqueued = new AtomicLong();
        final AtomicLong dequeued = new AtomicLong();
        final AtomicLong duplicates = new AtomicLong();
        final AtomicIntegerArray seen = new AtomicIntegerArray(TOTAL);
        final AtomicBoolean stopped = new AtomicBoolean();
        final CountDownLatch go = new CountDownLatch(1);
        final CountDownLatch producersDone = new CountDownLatch(PRODUCERS);

        final List<Thread> threads = new ArrayList<>();
        for (int p = 0; p < PRODUCERS; p++) {
            final int id = p;
            threads.add(daemon("producer-" + p, () -> {
                try {
                    go.await();
                    for (int i = 0; i < PER_PRODUCER && !stopped.get(); i++) {
                        queue.enqueue(id + "-" + i, id + "-" + i);
                        enqueued.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    producersDone.countDown();
                }
            }));
        }
        for (int c = 0; c < CONSUMERS; c++) {
            threads.add(daemon("consumer-" + c, () -> {
                try {
                    go.await();
                    while (!stopped.get() && dequeued.get() < TOTAL) {
                        final Map.Entry<String, String> entry = queue.dequeue();
                        if (entry == null) {
                            continue;
                        }
                        dequeued.incrementAndGet();
                        final String[] parts = entry.getValue().split("-", 2);
                        final int index = Integer.parseInt(parts[0]) * PER_PRODUCER + Integer.parseInt(parts[1]);
                        if (seen.getAndIncrement(index) != 0) {
                            duplicates.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        threads.forEach(Thread::start);
        go.countDown();

        long lastEnqueued = -1;
        long lastDequeued = -1;
        long lastProgress = System.currentTimeMillis();
        while (dequeued.get() < TOTAL) {
            Thread.sleep(100);
            if (enqueued.get() != lastEnqueued || dequeued.get() != lastDequeued) {
                lastEnqueued = enqueued.get();
                lastDequeued = dequeued.get();
                lastProgress = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastProgress > STALL_MILLIS) {
                stopped.set(true);
                threads.forEach(Thread::interrupt);
                fail(String.format("no progress for %dms: enqueued %d, dequeued %d of %d",
                        STALL_MILLIS, lastEnqueued, lastDequeued, TOTAL));
            }
        }
        stopped.set(true);
        producersDone.await(10, TimeUnit.SECONDS);

        assertEquals(TOTAL, dequeued.get());
        assertEquals(0, duplicates.get());
        int missing = 0;
        for (int i = 0; i < TOTAL; i++) {
            if (seen.get(i) == 0) {
                missing++;
            }
        }
        assertEquals(0, missing);
    }

    private DispatchQueue<String> newQueue(int inMemoryQueueSize, int batchSize)
            throws IOException, QueueCreateFailedException {
        return new DataBlocksOffHeapQueue<>(
                String::getBytes, String::new,
                "backpressure-deadlock", Paths.get(folder.newFolder().toURI()),
                inMemoryQueueSize, batchSize, 4L * 1024L * 1024L * 1024L);
    }

    private static Thread daemon(String name, Runnable body) {
        final Thread t = new Thread(body, name);
        t.setDaemon(true);
        return t;
    }
}
