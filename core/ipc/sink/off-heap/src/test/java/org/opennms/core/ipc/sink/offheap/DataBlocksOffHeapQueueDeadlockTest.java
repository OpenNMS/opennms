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
import java.nio.charset.StandardCharsets;
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
 * NMS-20271. Producers and consumers racing across the memory to off-heap transition used to wedge:
 * a consumer inside enableQueue() held diskLock while waiting on the flush future whose task needs
 * that same lock, and readData() never evicted a head block that had drained while it was the tail.
 * Both stalls are silent, so the test asserts on forward progress rather than on completion alone.
 */
public class DataBlocksOffHeapQueueDeadlockTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final int PRODUCERS = 8;
    private static final int CONSUMERS = 8;
    private static final int PER_PRODUCER = 2000;
    /** wide enough that serialization and the RocksDB write actually take time */
    private static final String PADDING = new String(new char[192]).replace('\0', 'x');
    private static final int TOTAL = PRODUCERS * PER_PRODUCER;
    private static final long STALL_MILLIS = 20_000L;

    @Test(timeout = 120_000)
    public void doesNotStallAcrossTheOffHeapTransition() throws Exception {
        // in-memory capacity of 100 with 10 per block forces off-heap blocks almost immediately
        final DispatchQueue<String> queue = newQueue(100, 10);

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
                        queue.enqueue(id + "-" + i + "-" + PADDING, id + "-" + i);
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
                        final String[] parts = entry.getValue().split("-", 3);
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
                s -> s.getBytes(StandardCharsets.UTF_8),
                b -> new String(b, StandardCharsets.UTF_8),
                "deadlock", Paths.get(folder.newFolder().toURI()),
                inMemoryQueueSize, batchSize, 4L * 1024L * 1024L * 1024L);
    }

    private static Thread daemon(String name, Runnable body) {
        final Thread t = new Thread(body, name);
        t.setDaemon(true);
        return t;
    }
}
