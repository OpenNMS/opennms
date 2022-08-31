/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.offheap;

import com.jayway.awaitility.core.ConditionTimeoutException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;
import org.opennms.core.ipc.sink.api.ReadFailedException;
import org.opennms.core.ipc.sink.api.WriteFailedException;

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

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LinkedBlockOffHeapQueueTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void canQueueAndDequeue() throws IOException, WriteFailedException, InterruptedException, QueueCreateFailedException, ReadFailedException {
        LinkedBlockOffHeapQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", Paths.get(folder.newFolder().toURI()), 1, 1, 10000);

        // Since size is 1, the first entry should be in-memory and the second entry should be on disk
        String payload1 = "msg1";
        queue.enqueue(payload1, "key1");

        String payload2 = "msg2";
        queue.enqueue(payload2, "key2");

        String payload3 = "msg3";
        queue.enqueue(payload3, "key3");

        assertThat(queue.getSize(), equalTo(3));
        assertThat(queue.getMemoryBlocks(), equalTo(1));
        // one empty block
        assertThat(queue.getOffHeapBlocks(), equalTo(3));

        assertThat(queue.dequeue().getValue(), equalTo(payload1));
        assertThat(queue.dequeue().getValue(), equalTo(payload2));
        assertThat(queue.dequeue().getValue(), equalTo(payload3));
    }

    @Test
    public void canRestoreOffHeapBlock() throws IOException, WriteFailedException, InterruptedException, QueueCreateFailedException, ReadFailedException {
        Path offHeapPath = Paths.get(folder.newFolder().toURI());
        LinkedBlockOffHeapQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", offHeapPath, 4, 2, 10000);

        // Since size is 1, the first entry should be in-memory and the second entry should be on disk
        for (int i = 0; i < 11; i++) {
            String payload = "msg" + i;
            queue.enqueue(payload, "key" + i);
        }

        assertThat(queue.getSize(), equalTo(11));
        assertThat(queue.getOffHeapBlocks(), equalTo(4));
        assertThat(queue.getMemoryBlocks(), equalTo(2));
        //give time to flush all data
        Thread.sleep(1000L);
        queue.shutdown();
        LinkedBlockOffHeapQueue<String> newQueue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
                "canQueueAndDequeue", offHeapPath, 2, 2, 10000);

        // it will lose 2 block in memory & 1 unfinished offheap block due to not persist yet
        assertThat(newQueue.getSize(), equalTo(6));

        assertThat(newQueue.getOffHeapBlocks(), equalTo(3));
        for (int i = 4; i < 10; i++) {
            assertThat(newQueue.dequeue().getValue(), equalTo("msg" + i));
        }
        assertThat(newQueue.getSize(), equalTo(0));
    }

    class Enqueue implements Runnable {
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

    class Dequeue implements Runnable {
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
        DispatchQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
                "checkDeadlock", Paths.get(folder.newFolder().toURI()), 10000, 1000, 100000);
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

        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            System.out.println("deQueueDataCounter: " + deQueueDataCounter.get() + " queue " + queue.getSize());
            return (deQueueDataCounter.get() > 0 && deQueueExecutor.getActiveCount() == 0) || deQueueDataCounter.get() == writeThread * itemPerThread;
        }, equalTo(true));
        System.out.println("Time spent: " + (System.currentTimeMillis() - start));
        System.out.println("enQueueData.size(): " + enQueueDataCounter.get() + " deQueueData.size() " + deQueueDataCounter.get());

        assertEquals(enQueueDataCounter.get(), deQueueDataCounter.get());
        assertEquals(writeThread * itemPerThread, deQueueDataCounter.get());
    }

    @Test
    public void canQueueAndDequeueInParallel() throws IOException, QueueCreateFailedException {
        DispatchQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
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

        try {
            await().atMost(30, TimeUnit.SECONDS).until(() -> {
                //System.out.println(String.format("dequeue: %s toqueue: %s queueSize: %s", dequeued.size(), toQueue.size(), queue.getSize()));
                return dequeued;
            }, equalTo(toQueue));
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            //System.out.println("FAIL !!!! size not matching!!!");
            //e.printStackTrace();
        }
//        await().atMost(15, TimeUnit.SECONDS).until(() -> {
//            //System.out.println(String.format("dequeue: %s toqueue: %s queueSize: %s",dequeued.size(), toQueue.size(), queue.getSize()));
//            return dequeued;
//        }, equalTo(toQueue));
//


        toQueue.removeAll(dequeued);
        //toQueue.removeAll(dequeued);
        assertEquals(new ArrayList<>(), toQueue);
    }

    @Test
    public void dequeuesInOrder() throws IOException, WriteFailedException, InterruptedException, QueueCreateFailedException, ReadFailedException {
        DispatchQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
                "dequeuesInOrder", Paths.get(folder.newFolder().toURI()), 10, 10, 10_000_000);

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
        DispatchQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
                "blocksWhenEmpty", Paths.get(folder.newFolder().toURI()), 1, 1, 10000);

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
//
//    @Test
//    public void blocksWhenFull() throws WriteFailedException, IOException, InterruptedException {
//        LinkedBlockOffHeapQueue<byte[]> queue = new LinkedBlockOffHeapQueue<>(b -> b, b -> b,
//                "blocksWhenFull", Paths.get(folder.newFolder().toURI()), 1, 1, 300);
//
//        // Fill the in-memory queue and the file and force a file check
//        queue.enqueue(new byte[0], "key1");
//        queue.enqueue(new byte[10], "key2");
//        queue.checkFileSize();
//
//        AtomicBoolean didQueue = new AtomicBoolean(false);
//
//        CompletableFuture.runAsync(() -> {
//            // Now try to queue again and verify that we block
//            try {
//                queue.enqueue(new byte[1], "key3");
//                didQueue.set(true);
//            } catch (WriteFailedException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        try {
//            await().pollDelay(10, TimeUnit.MILLISECONDS)
//                    .atMost(100, TimeUnit.MILLISECONDS)
//                    .until(didQueue::get);
//            fail("Dequeue happened but we should have been blocking");
//        } catch (ConditionTimeoutException expected) {
//        }
//
//        // Now dequeue which should free up space in the file
//        queue.dequeue();
//        queue.dequeue();
//        queue.checkFileSize();
//
//        await().atMost(1, TimeUnit.SECONDS).until(didQueue::get);
//    }

//    @Test
//    public void recoversFromCorruptFile() throws IOException, WriteFailedException, InterruptedException {
//        String moduleName = "recoversFromCorruptFile";
//        File tmpFolder = folder.newFolder();
//        File corruptfile = Paths.get(tmpFolder.getAbsolutePath(), moduleName + ".fifo").toFile();
//        Files.write(corruptfile.toPath(), "corrupt!".getBytes());
//        LinkedBlockOffHeapQueue<String> queue = new LinkedBlockOffHeapQueue<>(String::getBytes, String::new,
//                moduleName, Paths.get(tmpFolder.toURI()), 1, 1, 300);
//
//        String payload1 = "msg1";
//        queue.enqueue(payload1, "key1");
//
//        String received = queue.dequeue().getValue();
//        assertThat(received, equalTo(payload1));
//    }

}
