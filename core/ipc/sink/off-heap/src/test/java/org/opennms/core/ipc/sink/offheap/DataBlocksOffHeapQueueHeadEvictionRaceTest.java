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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;

/**
 * NMS-20271. readData() calls removeHeadBlockIfNeeded() whenever the head is not also the tail,
 * including for a head that was already empty on entry - so it can run for a head whose flushToDisk()
 * is racing on another thread. OffHeapDataBlock.size() reads the queue/offHeapQueueSize fields that
 * flush writes under diskLock, but size() itself only synchronizes on the block's own monitor, which
 * the flush task never acquires - so without those fields being volatile there is no happens-before
 * edge, and a reader can observe the post-flush queue == null alongside the pre-flush
 * offHeapQueueSize == -1, making size() return -1 and removeHeadBlockIfNeeded() evict a block that is
 * still being written, dropping a whole batch.
 *
 * A single-threaded call sequence can't force this reordering on demand - it's a genuine memory-
 * visibility race, not a logic bug, so this test leans on volume and concurrency (batchSize 1 so
 * every message flushes and every head check races a flush) to give it a real chance to surface,
 * rather than proving it deterministically. It can still pass "by luck" even without the volatile
 * fix, especially on strongly-ordered hardware; treat a failure here as meaningful, but don't treat a
 * pass as proof the fields don't need to be volatile.
 */
public class DataBlocksOffHeapQueueHeadEvictionRaceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static final int PRODUCERS = 8;
    private static final int CONSUMERS = 8;
    private static final int PER_PRODUCER = 3000;
    private static final String PADDING = new String(new char[192]).replace('\0', 'x');
    private static final int TOTAL = PRODUCERS * PER_PRODUCER;

    @Test(timeout = 120_000)
    public void doesNotDropAHeadBlockRacingAFlush() throws Exception {
        // batchSize 1 with a tiny in-memory ceiling: every message is its own block and every block
        // spills off-heap almost immediately, so head checks are constantly racing a flush in flight.
        final DispatchQueue<String> queue = newQueue(8, 1);

        final AtomicLong dequeued = new AtomicLong();
        final AtomicLong duplicates = new AtomicLong();
        final AtomicIntegerArray seen = new AtomicIntegerArray(TOTAL);
        final CountDownLatch go = new CountDownLatch(1);
        final CountDownLatch producersDone = new CountDownLatch(PRODUCERS);
        final CountDownLatch consumersDone = new CountDownLatch(CONSUMERS);

        final List<Thread> threads = new ArrayList<>();
        for (int p = 0; p < PRODUCERS; p++) {
            final int id = p;
            threads.add(daemon("producer-" + p, () -> {
                try {
                    go.await();
                    for (int i = 0; i < PER_PRODUCER; i++) {
                        queue.enqueue(id + "-" + i + "-" + PADDING, id + "-" + i);
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
                    while (dequeued.get() < TOTAL) {
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
                } finally {
                    consumersDone.countDown();
                }
            }));
        }

        threads.forEach(Thread::start);
        go.countDown();

        producersDone.await(90, TimeUnit.SECONDS);
        consumersDone.await(30, TimeUnit.SECONDS);

        assertEquals(TOTAL, dequeued.get());
        assertEquals(0, duplicates.get());
        int missing = 0;
        for (int i = 0; i < TOTAL; i++) {
            if (seen.get(i) == 0) {
                missing++;
            }
        }
        assertEquals("a block was evicted while its flush was still racing, dropping its batch",
                0, missing);
    }

    private DispatchQueue<String> newQueue(int inMemoryQueueSize, int batchSize)
            throws IOException, QueueCreateFailedException {
        return new DataBlocksOffHeapQueue<>(
                s -> s.getBytes(StandardCharsets.UTF_8),
                b -> new String(b, StandardCharsets.UTF_8),
                "head-eviction-race", Paths.get(folder.newFolder().toURI()),
                inMemoryQueueSize, batchSize, 4L * 1024L * 1024L * 1024L);
    }

    private static Thread daemon(String name, Runnable body) {
        final Thread t = new Thread(body, name);
        t.setDaemon(true);
        return t;
    }
}
