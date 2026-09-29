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

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

/**
 * NMS-20271. notifyNextDataBlock() submits enableQueue() prefetch tasks that block (poll) until an
 * in-flight flush's Future completes. Those prefetch tasks and flushToDisk()'s own tasks must run on
 * separate pools: if they shared one pool, enough concurrently-blocked prefetch tasks could occupy
 * every thread and starve the flush(es) they are waiting on, which are stuck behind them in the same
 * queue and can never start - the same class of silent wedge NMS-20271 fixed, just relocated onto the
 * prefetch path. This saturates OffHeapDataBlock.prefetchExecutorService with blocked enableQueue()
 * calls and asserts a flush on a different block (which only ever needs executorService) still
 * completes promptly.
 */
public class OffHeapDataBlockPrefetchPoolTest {

    /** matches Executors.newFixedThreadPool(10) in OffHeapDataBlock */
    private static final int POOL_SIZE = 10;

    @Test(timeout = 20_000)
    public void flushIsNotStarvedByPrefetchPool() throws Exception {
        // x's flush never completes for the life of the test (writeData blocks on a latch we never
        // release until cleanup), so every prefetch task waiting on it stays parked in enableQueue().
        final CountDownLatch xWriteGate = new CountDownLatch(1);
        final ControllableBlock<String> x = new ControllableBlock<>(1, xWriteGate, 0L);
        x.enqueue("k", "v");

        final List<Future<?>> waiters = new ArrayList<>();
        for (int i = 0; i < POOL_SIZE; i++) {
            waiters.add(OffHeapDataBlock.prefetchExecutorService.submit(() -> {
                try {
                    x.enableQueue();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        // let the pool actually dispatch all POOL_SIZE waiters before relying on it being saturated
        Thread.sleep(500);

        final ExecutorService probe = Executors.newSingleThreadExecutor();
        try {
            // b is unrelated to x; its flush only ever needs executorService, which the ten blocked
            // waiters above never touch once the pools are properly separated.
            final ControllableBlock<String> b = new ControllableBlock<>(1, null, 100L);
            final Future<?> flushed = probe.submit(() -> {
                b.enqueue("k", "v");
                try {
                    b.peek();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            boolean timedOut = false;
            try {
                flushed.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                timedOut = true;
            }
            assertFalse("flush on a separate block stalled while the prefetch pool was saturated; "
                    + "flush and prefetch tasks must not share a thread pool", timedOut);
        } finally {
            probe.shutdownNow();
            xWriteGate.countDown();
            waiters.forEach(f -> f.cancel(true));
        }
    }

    /**
     * Minimal OffHeapDataBlock whose disk I/O is fully controllable: writeData either blocks on a
     * gate (to simulate a flush that never finishes) or sleeps a fixed delay (to simulate one that
     * takes a little while), and loadData always returns null so toMemory() completes without
     * needing a real round trip.
     */
    private static final class ControllableBlock<T> extends OffHeapDataBlock<T> {
        private final CountDownLatch writeGate;
        private final long writeDelayMillis;

        ControllableBlock(int queueSize, CountDownLatch writeGate, long writeDelayMillis) {
            super(null, queueSize, o -> new byte[0], b -> null, null);
            this.writeGate = writeGate;
            this.writeDelayMillis = writeDelayMillis;
        }

        @Override
        void writeData(String key, byte[] data) {
            try {
                if (writeGate != null) {
                    writeGate.await();
                } else if (writeDelayMillis > 0) {
                    Thread.sleep(writeDelayMillis);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        byte[] loadData(String key) {
            return null;
        }
    }
}
