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
import org.h2.mvstore.MVMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    //private final File queueFile;
    private String name;
    private final Function<T, byte[]> serializer;
    private final Function<byte[], T> deserializer;
    private final Lock diskLock = new ReentrantLock(true);
    private int offHeapQueueSize = -1;

    private RunnableFuture<QueueFile> future;
    private MVMap<String, byte[]> db;

    public OffHeapDataBlock(int queueSize, Path fileDir, Function<T, byte[]> serializer, Function<byte[], T> deserializer, MVMap<String, byte[]> db) {
        Objects.requireNonNull(fileDir);
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
        this.queueSize = queueSize;
        this.db = Objects.requireNonNull(db);
        queue = new ArrayBlockingQueue<>(queueSize, true);
        name = System.nanoTime() + "_" + (int) Math.floor(Math.random() * 1000);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean enqueue(String key, T message) {
        try {
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
        }
    }

    @Override
    public Map.Entry<String, T> peek() throws InterruptedException {
        enableQueue();
        return queue.peek();
    }

    @Override
    public Map.Entry<String, T> dequeue() throws InterruptedException {
        enableQueue();
        return queue.take();
    }

    @Override
    public void notifyNextDataBlock() {
        if (nextDataBlock == null) {
            return;
        }
        if (nextDataBlock instanceof OffHeapDataBlock) {
            new Thread(() -> ((OffHeapDataBlock<T>) nextDataBlock).enableQueue());
        }
    }

    private DataBlock<T> nextDataBlock;

    @Override
    public void setNextDataBlock(DataBlock<T> dataBlock) {
        this.nextDataBlock = Objects.requireNonNull(dataBlock);
    }

    @Override
    public int size() {
        return (queue != null) ? queue.size() : offHeapQueueSize;
    }

    //freddy
    private void flushToDisk() throws IOException, ExecutionException, InterruptedException {
//        if(true)
//            return;
//        //System.out.println("enqueue flushToDisk");
        //QueueFlusher flusher = new QueueFlusher();
        future = new FutureTask<>(() -> {
            long start = System.currentTimeMillis();
            try {
                offHeapQueueSize = queue.size();
                LOG.warn("Enqueue flush to queue: {}", name);

                diskLock.lock();
                LOG.warn("Enqueue flush to queue time0 : {} time: {}", name, (System.currentTimeMillis() - start));
                //start = System.currentTimeMillis();
//                Method usedBytesMethod = offHeapQueue.getClass().getDeclaredMethod("usedBytes");
//                usedBytesMethod.setAccessible(true);
                LOG.warn("Enqueue flush to queue time1 : {} time: {}", name, (System.currentTimeMillis() - start));
                //start = System.currentTimeMillis();

                List<byte[]> serializedMessages = serdesPool.submit(() ->
                        queue.parallelStream()
                                .map(d -> d.getValue())
                                .map(serializer)
                                .collect(Collectors.toList())).get();

                LOG.warn("Enqueue flush to queue time2 : {} time: {}", name, (System.currentTimeMillis() - start));
                //start = System.currentTimeMillis();
                byte[] serializedBatch = new SerializedBatch(serializedMessages).toBytes();
                db.put(name, serializedBatch);

                //offHeapQueue.add(serializedBatch);
//
//                for (byte[] data : datas) {
//                    offHeapQueue.add(data);
//                }
                LOG.warn("Enqueue flush to queue time3 : {} time: {}", name, (System.currentTimeMillis() - start));
                //start = System.currentTimeMillis();
                // store to disk
                queue.clear();
                queue = null;
                LOG.warn("Enqueue flush to queue: {} DONE time: {}", name, (System.currentTimeMillis() - start));
                return offHeapQueue;
//            } catch (NoSuchMethodException e) {
//                LOG.warn("Could not instantiate queue", e);
//                throw new RuntimeException(e);
            } catch (Exception e) {
                LOG.warn("Could not instantiate queue", e);
                throw new RuntimeException(e);
            } finally {
                diskLock.unlock();
                LOG.warn("flush to queue DONE: {} time: {}", name, (System.currentTimeMillis() - start));
            }
        });
        serdesPool.submit(future);

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


    private void toMemory() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        var tmpQueue = queue = new ArrayBlockingQueue<>(this.queueSize, true);

//        DatabaseEntry key = new DatabaseEntry(name.getBytes(StandardCharsets.UTF_8));
//        DatabaseEntry searchEntry = new DatabaseEntry();

        byte[] serializedBatchBytes = db.remove(name);
        if(serializedBatchBytes == null){
            LOG.error("Data not found for name: {}", name);
        }
        LOG.warn("toMemory time1 : {} time: {}", name, (System.currentTimeMillis() - start));
        //start = System.currentTimeMillis();

        serdesPool.submit(() -> {
            try {
                (new SerializedBatch(serializedBatchBytes)).batchedMessages.parallelStream()
                        .map(deserializer).forEachOrdered(d -> {
                            tmpQueue.add(new AbstractMap.SimpleImmutableEntry<>(null, d));
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).get();
        LOG.warn("toMemory time2 : {} time: {}", name, (System.currentTimeMillis() - start));
        //start = System.currentTimeMillis();

        //offHeapQueue.clear();
        offHeapQueue = null;
        offHeapQueueSize = -1;
        queue = tmpQueue;

//        Files.delete(queueFile.toPath());
        LOG.warn("toMemory time3 : {} time: {}", name, (System.currentTimeMillis() - start));
        LOG.warn("toMemory DONE: {} time: {}", name, (System.currentTimeMillis() - start));
        LOG.warn("TO_MEMORY DONE size: {}", tmpQueue.size());

    }

    // make sure data is in memory queue
    public void enableQueue() {
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
            diskLock.lock();


            //System.out.println("Start to enable queue");
            toMemory();
            future = null;
//                System.out.println("Start to enable queue done");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            diskLock.unlock();
        }

    }

    /**
     * The serialized form of the batch that is written to disk.
     */
    public static final class SerializedBatch implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<byte[]> batchedMessages = new ArrayList<>();

        SerializedBatch(List<byte[]> messages) {
            batchedMessages.addAll(messages);
        }

        SerializedBatch(byte[] bytes) throws IOException {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInput in = new ObjectInputStream(bis)) {
                try {
                    batchedMessages.addAll(((SerializedBatch) in.readObject()).batchedMessages);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        byte[] toBytes() throws IOException {
            try (
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(this);
                out.flush();

                var tmp = bos.toByteArray();
                out.close();
                bos.close();

                return tmp;
            }
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
