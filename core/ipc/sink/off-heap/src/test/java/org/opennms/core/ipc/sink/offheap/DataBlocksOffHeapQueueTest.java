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

import org.awaitility.core.ConditionTimeoutException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;
import org.opennms.core.ipc.sink.api.ReadFailedException;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DataBlocksOffHeapQueueTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void canQueueAndDequeue() throws IOException, WriteFailedException, InterruptedException, QueueCreateFailedException, ReadFailedException {
        DataBlocksOffHeapQueue<String> queue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", Paths.get(folder.newFolder().toURI()), 1, 1, 1000_000_000L);

        // Since size is 1, the first entry should be in-memory and the second entry should be on disk
        String payload1 = "msg1";
        queue.enqueue(payload1, "key1");

        String payload2 = "msg2";
        queue.enqueue(payload2, "key2");

        String payload3 = "msg3";
        queue.enqueue(payload3, "key3");

        assertThat(3, equalTo(queue.getSize()));
        assertThat(1, equalTo(queue.getMemoryBlockCount()));
        // one empty block
        assertThat(3, equalTo(queue.getOffHeapBlockCount()));

        assertThat(payload1, equalTo(queue.dequeue().getValue()));
        assertThat(payload2, equalTo(queue.dequeue().getValue()));
        assertThat(payload3, equalTo(queue.dequeue().getValue()));
    }

    @Test
    public synchronized void canRestoreOffHeapBlock() throws IOException, WriteFailedException, InterruptedException, QueueCreateFailedException, ReadFailedException {
        Path offHeapPath = Paths.get(folder.newFolder().toURI());
        DataBlocksOffHeapQueue<String> queue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", offHeapPath, 4, 2, 1000_000_000L);

        // Since size is 1, the first entry should be in-memory and the second entry should be on disk
        for (int i = 0; i < 11; i++) {
            String payload = "msg" + i;
            queue.enqueue(payload, "key" + i);
        }

        assertThat(11, equalTo(queue.getSize()));
        assertThat(4, equalTo(queue.getOffHeapBlockCount()));
        assertThat(2, equalTo(queue.getMemoryBlockCount()));
        //give time to flush all data
        this.wait(1000L);
        queue.shutdown();
        DataBlocksOffHeapQueue<String> newQueue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", offHeapPath, 2, 2, 10000);

        // it will lose 2 block in memory & 1 unfinished offheap block due to not persist yet
        assertThat(6, equalTo(newQueue.getSize()));

        assertThat(3, equalTo(newQueue.getOffHeapBlockCount()));
        for (int i = 4; i < 10; i++) {
            assertThat("msg" + i, equalTo(newQueue.dequeue().getValue()));
        }
        assertThat(0, equalTo(newQueue.getSize()));
    }

    private class Enqueue implements Runnable {
        private DispatchQueue<String> queue;
        private AtomicInteger counter;
        private int size;
        private String name;

        Enqueue(String name, DispatchQueue<String> queue, AtomicInteger counter, int size) {
            this.name = name;
            this.queue = queue;
            this.counter = counter;
            this.size = size;
        }

        @Override
        public void run() {
            for (int i = 0; i < size; i++) {
                try {
                    var tmp = name + "_" + i;
                    queue.enqueue(tmp, tmp);
                    counter.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Dequeue implements Runnable {
        private DispatchQueue<String> queue;
        private AtomicInteger counter;
        private int size;
        private String name;
        private ThreadPoolExecutor enQueueExecutor;

        Dequeue(String name, DispatchQueue<String> queue, AtomicInteger counter, int size, ThreadPoolExecutor enQueueExecutor) {
            this.name = Objects.requireNonNull(name);
            this.queue = Objects.requireNonNull(queue);
            this.counter = Objects.requireNonNull(counter);
            this.size = Objects.requireNonNull(size);
            this.enQueueExecutor = Objects.requireNonNull(enQueueExecutor);
        }

        @Override
        public void run() {
            while (enQueueExecutor.getActiveCount() > 0 || queue.getSize() > 0) {
                try {
                    queue.dequeue();
                    //storage.add(tmp.getValue());
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    System.out.println("ERROR Dequeue: " + e.getMessage());
                }
            }
        }
    }

    @Test
    public void checkDeadlock() throws IOException, QueueCreateFailedException {
        DispatchQueue<String> queue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "checkDeadlock", Paths.get(folder.newFolder().toURI()), 10000, 1000, 1000_000_000L);
        int itemPerThread = 50000;
        int writeThread = 20;
        int readThread = 20;
        long start = System.currentTimeMillis();

        AtomicInteger enQueueDataCounter = new AtomicInteger(0);
        AtomicInteger deQueueDataCounter = new AtomicInteger(0);
        ThreadPoolExecutor enQueueExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(readThread);
        ThreadPoolExecutor deQueueExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(writeThread);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < writeThread; i++) {
            futures.add(enQueueExecutor.submit(new Enqueue(String.valueOf(i), queue, enQueueDataCounter, itemPerThread)));
        }

        for (int i = 0; i < readThread; i++) {
            futures.add(deQueueExecutor.submit(new Dequeue(String.valueOf(i), queue, deQueueDataCounter, itemPerThread, enQueueExecutor)));
        }

        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            System.out.println("deQueueDataCounter: " + deQueueDataCounter.get() + " queue size: " + queue.getSize());
            return (deQueueDataCounter.get() > 0 && deQueueExecutor.getActiveCount() == 0) || deQueueDataCounter.get() == writeThread * itemPerThread;
        }, equalTo(true));
        System.out.println("Time spent: " + (System.currentTimeMillis() - start));
        System.out.println("enQueueData.size(): " + enQueueDataCounter.get() + " deQueueData.size() " + deQueueDataCounter.get());

        assertEquals(enQueueDataCounter.get(), deQueueDataCounter.get());
        assertEquals(writeThread * itemPerThread, deQueueDataCounter.get());
    }

    @Test
    public void canQueueAndDequeueInParallel() throws IOException, QueueCreateFailedException {
        DispatchQueue<String> queue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", Paths.get(folder.newFolder().toURI()), 20, 5, 100_000_000);

        int numEntries = 11_111;
        List<String> toQueue = IntStream.range(0, numEntries)
                .boxed()
                .map(Object::toString)
                .collect(Collectors.toList());

        CountDownLatch startedQueueing = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger(0);
        CompletableFuture.runAsync(() -> {
            while (count.get() < numEntries) {
                try {
                    var value = toQueue.get(count.getAndIncrement());
                    queue.enqueue(value, value);
                    startedQueueing.countDown();
                } catch (WriteFailedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        List<String> dequeued = new CopyOnWriteArrayList<>();
        CompletableFuture.runAsync(() -> {
            try {
                startedQueueing.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    var tmp = queue.dequeue().getValue();
                    dequeued.add(tmp);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        await().atMost(30, TimeUnit.SECONDS).until(() -> dequeued, equalTo(toQueue));

        toQueue.removeAll(dequeued);
        assertEquals(new ArrayList<>(), toQueue);
    }

    @Test
    public void dequeuesInOrder() throws IOException, WriteFailedException, InterruptedException, QueueCreateFailedException, ReadFailedException {
        DispatchQueue<String> queue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "dequeuesInOrder", Paths.get(folder.newFolder().toURI()), 10, 10, 1000_000_000L);

        int numEntries = 10020;
        List<String> toQueue = IntStream.range(0, numEntries)
                .boxed()
                .map(Object::toString)
                .collect(Collectors.toList());


        for (String s : toQueue) {
            queue.enqueue(s, "key" + s);
        }

        assertThat(queue.getSize(), equalTo(numEntries));

        List<String> dequeued = new ArrayList<>();

        while (queue.getSize() > 0) {
            dequeued.add(queue.dequeue().getValue());
        }

        List<String> diff = new ArrayList<>(toQueue);
        diff.removeAll(dequeued);

        assertThat(diff, equalTo(new ArrayList<>()));
        assertThat(dequeued, equalTo(toQueue));
    }

    @Test
    public void blocksWhenEmpty() throws IOException, WriteFailedException, QueueCreateFailedException {
        DispatchQueue<String> queue = new DataBlocksOffHeapQueue<>(String::getBytes, String::new,
                "blocksWhenEmpty", Paths.get(folder.newFolder().toURI()), 1, 1, 1000_000_000L);

        AtomicReference<String> atomicString = new AtomicReference<>(null);
        AtomicBoolean receivedValue = new AtomicBoolean(false);

        CompletableFuture.runAsync(() -> {
            try {
                atomicString.set(queue.dequeue().getValue());
                receivedValue.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(atomicString.get(), is(nullValue()));

        String payload = "a";
        try {
            await().pollDelay(10, TimeUnit.MILLISECONDS)
                    .atMost(100, TimeUnit.MILLISECONDS)
                    .until(receivedValue::get);
            fail("Dequeue did not block");
        } catch (ConditionTimeoutException expected) {
        }

        queue.enqueue(payload, "key");

        await().pollDelay(10, TimeUnit.MILLISECONDS)
                .atMost(1, TimeUnit.SECONDS)
                .until(() -> Objects.equals(payload, atomicString.get()));
    }

    @Test
    public synchronized void blocksWhenFull() throws WriteFailedException, IOException, InterruptedException, QueueCreateFailedException, RocksDBException {
        Path path = Paths.get(folder.newFolder().toURI());
        DataBlocksOffHeapQueue<byte[]> queue = new DataBlocksOffHeapQueue<>(b -> b, b -> b,
                "blocksWhenFull", path, 1, 1, 100, 100, 100);

        // Fill the in-memory queue and the file and force a file check
        for(int i = 0 ; i < 100 ; i++) {
            queue.enqueue(new byte[1000], "key" + i);
        }

        // force flush to disk
        queue.flushOffHeap();
        this.wait(1000L);
        System.out.println(queue.getOffHeapFileSize());

        AtomicBoolean didQueue = new AtomicBoolean(false);

        CompletableFuture.runAsync(() -> {
            // Now try to queue again and verify that we block
            try {
                queue.enqueue(new byte[1], "key3");
                didQueue.set(true);
            } catch (WriteFailedException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            await().pollDelay(10, TimeUnit.MILLISECONDS)
                    .atMost(100, TimeUnit.MILLISECONDS)
                    .until(didQueue::get);
            fail("Dequeue happened but we should have been blocking");
        } catch (ConditionTimeoutException expected) {
        }

        // Now dequeue which should free up space in the file
        queue.dequeue();
        queue.dequeue();

        await().atMost(1, TimeUnit.SECONDS).until(didQueue::get);
    }
}
