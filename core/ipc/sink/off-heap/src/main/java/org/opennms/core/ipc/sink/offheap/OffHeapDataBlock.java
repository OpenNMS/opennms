/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import com.squareup.tape2.QueueFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SystemPropertyUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OffHeapDataBlock<T> implements DataBlock<T> {
    private static final Logger LOG = LoggerFactory.getLogger(OffHeapDataBlock.class);
    private static final ForkJoinPool serdesPool = new ForkJoinPool(
            Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));

    protected int queueSize;
    protected BlockingQueue<Map.Entry<String, T>> queue;
    private QueueFile offHeapQueue;
    private final File queueFile;
    private final Function<T, byte[]> serializer;
    private final Function<byte[], T> deserializer;
    private final Lock lock = new ReentrantLock(true);
    private final Lock diskLock = new ReentrantLock(true);

    private RunnableFuture<QueueFile> future;


    public OffHeapDataBlock(int queueSize, Path fileDir, Function<T, byte[]> serializer, Function<byte[], T> deserializer) throws IOException {
        Objects.requireNonNull(fileDir);
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
        this.queueSize = queueSize;
        queue = new ArrayBlockingQueue<>(queueSize, true);
        queueFile = Paths.get(fileDir.toString(), System.nanoTime() + "_" + (int) Math.floor(Math.random() * 1000) + ".fifo").toFile();
    }

    @Override
    public boolean enqueue(String key, T message) {
        try {
            lock.lock();

            if (queue == null || future != null) {
                return false;
            }
            queue.add(new AbstractMap.SimpleImmutableEntry<>(key, message));
            if (queue.remainingCapacity() == 0) {
                this.flushToDisk();
            }
            return true;
        } catch (IllegalStateException | IOException | ExecutionException | InterruptedException ex) {
            LOG.error(ex.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map.Entry<String, T> peek() throws InterruptedException {
        try {
            lock.lock();
            enableQueue();
            return queue.peek();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map.Entry<String, T> dequeue() throws InterruptedException {
        try {
            lock.lock();
            enableQueue();
            return queue.take();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        try {
            lock.lock();
            return (queue != null) ? queue.size() : offHeapQueue.size();
        } finally {
            lock.unlock();
        }
    }

    private Thread thread = null;

    //freddy
    private void flushToDisk() throws IOException, ExecutionException, InterruptedException {
//        if(true)
//            return;
//        //System.out.println("enqueue flushToDisk");
        //QueueFlusher flusher = new QueueFlusher();
        future = new FutureTask<>(() -> {
            try {
                long start = System.currentTimeMillis();
                LOG.warn("Enqueue flush to queue: {}", queueFile);

                diskLock.lock();
                offHeapQueue = new QueueFile.Builder(queueFile).build();
                Method usedBytesMethod = offHeapQueue.getClass().getDeclaredMethod("usedBytes");
                usedBytesMethod.setAccessible(true);

                List<byte[]> datas = serdesPool.submit(() ->
                        queue.parallelStream()
                                .map(d -> d.getValue())
                                .map(serializer)
                                .collect(Collectors.toList())).get();

                for (byte[] data : datas) {
                    offHeapQueue.add(data);
                }

                // store to disk
                queue.clear();
                queue = null;
                diskLock.unlock();
                LOG.warn("Enqueue flush to queue: {} DONE time: {}", queueFile, (System.currentTimeMillis() - start));
                return offHeapQueue;
            } catch (NoSuchMethodException e) {
                LOG.warn("Could not instantiate queue", e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                LOG.warn("Could not instantiate queue", e);
                throw new RuntimeException(e);
            } finally {
                diskLock.unlock();
            }
        });
        thread = new Thread(future);
        thread.start();

//        String values = queue.stream().map(d -> (String) d.getValue()).collect(Collectors.joining(","));
        //System.out.println("enqueue flushToDisk: " + values);


        /*
        try {
            diskLock.lock();
            offHeapQueue = new QueueFile.Builder(queueFile).build();

            Method usedBytesMethod = offHeapQueue.getClass().getDeclaredMethod("usedBytes");
            usedBytesMethod.setAccessible(true);

            List<byte[]> datas = serdesPool.submit(() ->
                    queue.parallelStream()
                            .map(d -> d.getValue())
                            .map(serializer)
                            .collect(Collectors.toList())).get();
            for (byte[] data : datas) {
                offHeapQueue.add(data);
            }

            // store to disk
            queue.clear();
            queue = null;
        } catch (NoSuchMethodException e) {
            LOG.warn("Could not instantiate queue", e);
            throw new RuntimeException(e);
        } finally {
            diskLock.unlock();
        }

         */
    }


    private void toMemory() throws ExecutionException, InterruptedException, IOException {
        var tmpQueue = queue = new ArrayBlockingQueue<>(this.queueSize, true);
        List<byte[]> bytesList = new ArrayList<>();
        offHeapQueue.iterator().forEachRemaining(bytesList::add);

        serdesPool.submit(() -> {
            bytesList.parallelStream()
                    .map(deserializer).forEachOrdered(d -> {
                        tmpQueue.add(new AbstractMap.SimpleImmutableEntry<>(null, d));
                    });
        }).get();

        offHeapQueue.clear();
        offHeapQueue = null;
        LOG.warn("TO_MEMORY DONE size: {}", tmpQueue.size());
        queue = tmpQueue;

        Files.delete(queueFile.toPath());
    }

    // make sure data is in memory queue
    private void enableQueue() throws InterruptedException {
//        System.out.println("check future: " + future);
//        while (!future.isDone()) {
//            Thread.sleep(10);
//        }
        //System.out.println("check future done future: " + future);
        if (this.queue != null) {
            return;
        }
        try {
            while (future == null || !future.isDone()) {
                Thread.sleep(10);
            }
            System.out.println("HERE !!! " + offHeapQueue);
            diskLock.lock();
            System.out.println("HERE AFTER !!! " + offHeapQueue);

            if (offHeapQueue != null) {
                //System.out.println("Start to enable queue");
                toMemory();
                future = null;
                thread = null;
//                System.out.println("Start to enable queue done");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            diskLock.unlock();
        }

    }
/*

    class QueueFlusher implements Callable<QueueFile> {
//        private OffHeapDataBlock<T> offHeapDataBlock;
//
//        public QueueFlusher(OffHeapDataBlock<T> offHeapDataBlock) {
//            this.offHeapDataBlock = offHeapDataBlock;
//        }

        @Override
        public QueueFile call() throws Exception {
            System.out.println("enqueue flush to queue: " + queueFile);
            diskLock.lock();
            offHeapQueue = new QueueFile.Builder(queueFile).build();

            try {
                Method usedBytesMethod = offHeapQueue.getClass().getDeclaredMethod("usedBytes");
                usedBytesMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                LOG.warn("Could not instantiate queue", e);
                throw new RuntimeException(e);
            }
            try {
                List<byte[]> datas = serdesPool.submit(() ->
                        queue.parallelStream()
                                .map(d -> d.getValue())
                                .map(serializer)
                                .collect(Collectors.toList())).get();

                for (byte[] data : datas) {
                    offHeapQueue.add(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // store to disk
            queue.clear();
            queue = null;
            diskLock.unlock();
            System.out.println("enqueue flush to queue: " + queueFile + " DONE");
            return offHeapQueue;
        }
    }

*/
}
