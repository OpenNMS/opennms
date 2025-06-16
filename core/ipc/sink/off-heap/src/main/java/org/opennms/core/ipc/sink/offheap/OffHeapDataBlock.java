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

import org.nustaq.serialization.FSTConfiguration;
import org.opennms.core.ipc.sink.api.ReadFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class OffHeapDataBlock<T> implements DataBlock<T> {
    protected static final Logger LOG = LoggerFactory.getLogger(OffHeapDataBlock.class);
    protected static final ForkJoinPool serdesPool = new ForkJoinPool(
            Math.max(Runtime.getRuntime().availableProcessors() * 2, 4));
    protected static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    protected int queueSize;
    protected BlockingQueue<Map.Entry<String, T>> queue;

    private String name;
    private final Function<T, byte[]> serializer;
    private final Function<byte[], T> deserializer;
    private final Lock diskLock = new ReentrantLock(true);
    private int offHeapQueueSize = -1;

    private Future<Integer> future;

    private boolean restore;

    abstract void writeData(String key, byte[] data);

    abstract byte[] loadData(String key);

    /**
     * @param name         (nullable, if not null it assume restore mode)
     * @param queueSize
     * @param serializer
     * @param deserializer
     */
    protected OffHeapDataBlock(String name, int queueSize, Function<T, byte[]> serializer, Function<byte[], T> deserializer, byte[] data) {
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
        this.queueSize = queueSize;

        if (data == null) {
            this.restore = false;
            this.name = System.nanoTime() + "_" + new Random().nextInt(1000);
            this.queue = new ArrayBlockingQueue<>(queueSize, true);
        } else {
            this.restore = true;
            this.name = name;
            SerializedBatch batch = new SerializedBatch(data);
            this.offHeapQueueSize = batch.batchedMessages.size();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized boolean enqueue(String key, T message) {
        try {
            if (queue == null || future != null) {
                return false;
            }
            queue.add(new AbstractMap.SimpleImmutableEntry<>(key, message));
            if (queue.remainingCapacity() == 0) {
                this.flushToDisk();
            }
            return true;
        } catch (IllegalStateException ex) {
            LOG.error(ex.getMessage());
            return false;
        }
    }

    @Override
    public synchronized Map.Entry<String, T> peek() throws InterruptedException, ReadFailedException {
        enableQueue();
        return queue.peek();
    }

    @Override
    public synchronized Map.Entry<String, T> dequeue() throws InterruptedException, ReadFailedException {
        enableQueue();
        return queue.take();
    }

    @Override
    public synchronized void notifyNextDataBlock() {
        if (nextDataBlock == null) {
            return;
        }
        if (nextDataBlock instanceof OffHeapDataBlock) {
            executorService.submit(() -> {
                        try {
                            ((OffHeapDataBlock<T>) nextDataBlock).enableQueue();
                        } catch (ReadFailedException | InterruptedException e) {
                            LOG.error("Fail to notify next block.");
                            Thread.currentThread().interrupt();
                        }
                    }
            );
        }
    }

    private DataBlock<T> nextDataBlock;

    @Override
    public void setNextDataBlock(DataBlock<T> dataBlock) {
        this.nextDataBlock = Objects.requireNonNull(dataBlock);
    }

    @Override
    public synchronized int size() {
        return (queue != null) ? queue.size() : offHeapQueueSize;
    }

    private void flushToDisk() {
        if (future != null) {
            return;
        }
        diskLock.lock();
        // it will return the size of the bytes
        future = executorService.submit(() -> {
            diskLock.lock();
            long start = System.currentTimeMillis();
            try {
                List<byte[]> serializedMessages = serdesPool.submit(() ->
                        queue.parallelStream()
                                .map(Map.Entry::getValue)
                                .map(serializer)
                                .collect(Collectors.toList())).get();
                byte[] serializedBatch = new SerializedBatch(serializedMessages).toBytes();

                this.writeData(name, serializedBatch);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("flushToDisk writeData: {} size: {} time: {}", name, serializedBatch.length,
                            (System.currentTimeMillis() - start));
                }

                offHeapQueueSize = queue.size();
                queue = null;

                return serializedBatch.length;
            } finally {
                diskLock.unlock();
            }
        });
        diskLock.unlock();
    }

    // make sure data is in memory queue
    public synchronized void enableQueue() throws ReadFailedException, InterruptedException {
        try {
            if (!restore) {
                while (!diskLock.tryLock()) {
                    this.wait(10);
                }
                while ((future != null && !future.isDone())) {
                    this.wait(10);
                }
                if (queue != null) {
                    return;
                }
            } else {
                diskLock.lock();
            }
            toMemory();
            future = null;
            restore = false;
        } catch (ExecutionException e) {
            throw new ReadFailedException(e);
        } finally {
            diskLock.unlock();
        }
    }

    private void toMemory() throws ExecutionException, InterruptedException {
        var tmpQueue = queue = new ArrayBlockingQueue<>(this.queueSize, true);
        long start = System.currentTimeMillis();
        byte[] serializedBatchBytes = this.loadData(name);
        if (LOG.isDebugEnabled()) {
            LOG.debug("toMemory loadData: {} time: {}", name, (System.currentTimeMillis() - start));
        }
        if (serializedBatchBytes == null) {
            LOG.error("Data not found for name: {}", name);
            return;
        }
        serdesPool.submit(() ->
                (new SerializedBatch(serializedBatchBytes)).batchedMessages.parallelStream()
                        .map(deserializer).forEachOrdered(d ->
                                tmpQueue.add(new AbstractMap.SimpleImmutableEntry<>(null, d))
                        )
        ).get();
        LOG.debug("toMemory convert: {} time: {}", name, (System.currentTimeMillis() - start));

        offHeapQueueSize = -1;
        queue = tmpQueue;
    }

    /**
     * The serialized form of the batch that is written to disk.
     */
    public static final class SerializedBatch implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final FSTConfiguration fstConf = FSTConfiguration.createDefaultConfiguration();

        static {
            fstConf.registerClass(ArrayList.class, SerializedBatch.class);
        }

        private final List<byte[]> batchedMessages = new ArrayList<>();

        SerializedBatch(List<byte[]> messages) {
            batchedMessages.addAll(messages);
        }

        SerializedBatch(byte[] bytes) {
            batchedMessages.addAll((ArrayList<byte[]>) fstConf.asObject(bytes));
        }

        byte[] toBytes() {
            return fstConf.asByteArray(batchedMessages);
        }
    }
}