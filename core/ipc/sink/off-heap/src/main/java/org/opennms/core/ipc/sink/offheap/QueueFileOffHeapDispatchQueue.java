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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.squareup.tape2.QueueFile;
import com.swrve.ratelimitedlogger.RateLimitedLog;

/**
 * A {@link DispatchQueue} that first attempts to queue items in memory and upon overflowing the allocated in-memory
 * queue writes items "off heap" directly to disk via a file. The in-memory queue is volatile and if the process
 * crashes its contents are lost. The contents written to disk are durable and in the event of a crash will be reloaded.
 * <p>
 * This queue can be configured to only queue to memory by specifying the maximum off-heap size of 0. Using this
 * configuration causes {@link #enqueue} to block when the in-memory queue fills up rather than writing to the off-heap
 * queue.
 * <p>
 * Before queued items are written to disk they are first accumulated in a batch to limit the number of discrete writes
 * we make to disk. The batched items are considered part of the in-memory portion of the queue and are also volatile.
 *
 * @param <T> the type being queued
 */
public class QueueFileOffHeapDispatchQueue<T> implements DispatchQueue<T> {

    private static final Logger LOG = LoggerFactory.getLogger(QueueFileOffHeapDispatchQueue.class);
    private final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5)
            .every(Duration.ofSeconds(30))
            .build();

    // This must match the size of the HEADER_LENGTH in QueueFile's Element class
    //
    // We could pull this out of that class reflectively but it seemed easier to just hard code it here
    private static final int SERIALIZED_OBJECT_HEADER_SIZE_IN_BYTES = 4;
    private static final String FILE_EXTENSION = ".fifo";

    private final Function<T, byte[]> serializer;
    private final Function<byte[], T> deserializer;
    private final String moduleName;
    private final BlockingQueue<Map.Entry<String, T>> inMemoryQueue;

    // Note the queue is not thread safe so access should be synchronized
    private final QueueFile offHeapQueue;
    private final long maxFileSizeInBytes;
    private final int batchSize;
    private final Batch batch;

    private final ForkJoinPool serdesPool = new ForkJoinPool(
            Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));

    // Used to guard access to the offHeapQueue and batch data structures
    private final Lock offHeapLock = new ReentrantLock(true);
    // Used to ensure only one thread can be enqueing at a time
    private final Lock enqueueLock = new ReentrantLock(true);
    private final FileCapacityLatch fileCapacityLatch = new FileCapacityLatch();

    private final Method usedBytesMethod;

    public QueueFileOffHeapDispatchQueue(Function<T, byte[]> serializer, Function<byte[], T> deserializer,
                                         String moduleName, Path filePath, int inMemoryQueueSize, int batchSize,
                                         long maxFileSizeInBytes) throws IOException {
        Objects.requireNonNull(serializer);
        Objects.requireNonNull(deserializer);
        Objects.requireNonNull(moduleName);

        if (inMemoryQueueSize < 1) {
            throw new IllegalArgumentException("In memory queue size must be greater than 0");
        }

        if (inMemoryQueueSize % batchSize != 0) {
            throw new IllegalArgumentException("In memory queue size must be a multiple of batch size");
        }

        if (maxFileSizeInBytes < 0) {
            throw new IllegalArgumentException("Max file size must be either 0 or a positive integer");
        }

        this.serializer = serializer;
        this.deserializer = deserializer;
        this.moduleName = moduleName;
        this.batchSize = batchSize;
        this.maxFileSizeInBytes = maxFileSizeInBytes;
        batch = new Batch(batchSize);

        inMemoryQueue = new ArrayBlockingQueue<>(inMemoryQueueSize, true);

        // Setting the max file size to 0 or less will disable the off-heap portion of this queue
        if (maxFileSizeInBytes > 0) {
            Objects.requireNonNull(filePath);
            File file = Paths.get(filePath.toString(), moduleName + FILE_EXTENSION).toFile();

            QueueFile qf;
            try {
                qf = new QueueFile.Builder(file).build();
            } catch (Exception e) {
                LOG.warn("Exception while loading queue file", e);

                if (file.delete()) {
                    qf = new QueueFile.Builder(file).build();
                } else {
                    throw new IOException("Could not delete corrupted queue file " + file.getAbsolutePath());
                }
            }
            offHeapQueue = qf;

            // QueueFile unfortunately does not expose its file size usage publicly so we need to access it reflectively
            try {
                usedBytesMethod = offHeapQueue.getClass().getDeclaredMethod("usedBytes");
                usedBytesMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                LOG.warn("Could not instantiate queue", e);
                throw new RuntimeException(e);
            }

            checkFileSize();
        } else {
            offHeapQueue = null;
            usedBytesMethod = null;
        }
    }

    @VisibleForTesting
    public long checkFileSize() {
        try {
            long fileSize = (long) usedBytesMethod.invoke(offHeapQueue);
            fileCapacityLatch.setCurrentCapacityBytes(maxFileSizeInBytes - fileSize);
            LOG.trace("Checked file size for module {} and got result {} bytes", moduleName,
                    fileSize);

            return fileSize;
        } catch (IllegalAccessException | InvocationTargetException e) {
            RATE_LIMITED_LOGGER.warn("Failed to check file size for module {}", moduleName, e);
            return 0;
        }
    }

    /**
     * When enqueueing we prefer the in-memory queue unless the file based queue is already utilized. If that fails
     * (because it is full) we then enqueue via the file based queue provided it is not currently full and has been
     * configured. If the file based queue is full or not configured we block and wait for capacity.
     * <p>
     * We only write to the file based queue when we have a full batch ready. The batch container is then emptied after
     * being written to disk.
     */
    @Override
    public EnqueueResult enqueue(T message, String key) throws WriteFailedException {
        enqueueLock.lock();
        try {
            Map.Entry<String, T> msgEntry = new AbstractMap.SimpleImmutableEntry<>(key, message);

            LOG.trace("Attempting to enqueue {} with key {} into queue with current size {}", message, key, getSize());

            // Off-heap queueing is not enabled so queue directly to memory
            if (offHeapQueue == null) {
                LOG.trace("Enqueueing {} with key {} in-memory since there is no off-heap queue " +
                        "configured", message, key);

                try {
                    inMemoryQueue.put(msgEntry);
                } catch (InterruptedException e) {
                    throw new WriteFailedException(e);
                }

                return EnqueueResult.IMMEDIATE;
            }

            // Off-heap queueing is enabled but we haven't started using it yet so continue trying to fill the in-memory
            // queue
            int size = 0;
            byte[] serializedBatch = null;
            offHeapLock.lock();
            try {
                if (offHeapQueue.size() <= 0 && batch.isEmpty()) {
                    // If the in-memory queue is full, this offer will fail and we will fall through below to the 
                    // off-heap
                    // queueing logic
                    boolean inMemoryQueueHadSpace = inMemoryQueue.offer(msgEntry);

                    if (inMemoryQueueHadSpace) {
                        LOG.trace("Enqueueing {} with key {} in-memory", message, key);

                        return EnqueueResult.IMMEDIATE;
                    }
                }

                // The in-memory queue is either full or there is already message in the batch or off-heap so we 
                // continue to
                // batch
                LOG.trace("Batching message {} with key {} for off-heap queue", message, key);
                batch.add(message);

                if (batch.isFull()) {
                    LOG.trace("Flushing batch off-heap");

                    try {
                        serializedBatch = batch.toSerializedBatchAndClear();
                    } catch (Exception e) {
                        RATE_LIMITED_LOGGER.warn("Failed to flush to off-heap", e);
                        throw new WriteFailedException(e);
                    }

                    size = serializedBatch.length + SERIALIZED_OBJECT_HEADER_SIZE_IN_BYTES;
                    fileCapacityLatch.markFlushNeeded();
                }
            } finally {
                offHeapLock.unlock();
            }

            if (serializedBatch != null) {
                try {
                    // This is a critical blocking call and it has to be done outside the context of any shared lock 
                    // with
                    // dequeue() otherwise it will cause a deadlock
                    //
                    // After unblocking we will pick up the lock again and double check that we still need to flush 
                    // and then
                    // perform that while holding the lock
                    fileCapacityLatch.waitForCapacity(size);

                    offHeapLock.lock();
                    try {
                        if (!fileCapacityLatch.isFlushNeeded()) {
                            return EnqueueResult.DEFERRED;
                        }

                        try {
                            offHeapQueue.add(serializedBatch);

                            // Since we just wrote to disk, we need to check the file again to record the current 
                            // capacity
                            checkFileSize();
                        } catch (IOException e) {
                            throw new WriteFailedException(e);
                        }
                    } finally {
                        offHeapLock.unlock();
                    }
                } catch (InterruptedException e) {
                    throw new WriteFailedException(e);
                }
            }

            return EnqueueResult.DEFERRED;
        } finally {
            enqueueLock.unlock();
        }
    }

    /**
     * On every call to dequeue, if the off-heap queue is configured, we check the file for an entry and drain it to the
     * in-memory queue provided there is room. We then take exclusively from the head of the in-memory queue which
     * ensures ordering with respect to the two discrete queues.
     * <p>
     * After completely draining the queue on disk we check the existing batch for entries and drain them next.
     */
    @Override
    public Map.Entry<String, T> dequeue() throws InterruptedException {
        LOG.debug("Dequeueing an entry from queue with current size {}", getSize());

        // If off-heap queueing is enabled we need to first check if there is anything to read off-heap
        if (offHeapQueue != null) {
            offHeapLock.lock();
            try {
                // Try to move a batch from the off-heap queue to the in-memory queue
                if (offHeapQueue.size() > 0 && inMemoryQueue.remainingCapacity() >= batchSize) {
                    LOG.trace("Found an entry off-heap and there was room in-memory, moving it");

                    try {
                        byte[] entry = offHeapQueue.peek();
                        if (entry != null) {
                            offHeapQueue.remove();

                            try {
                                inMemoryQueue.addAll(unbatchSerializedBatch(new SerializedBatch(entry)));
                            } catch (ExecutionException e) {
                                RATE_LIMITED_LOGGER.warn("Exception while deserializing", e);
                                throw new RuntimeException(e);
                            }

                            // Since we read off the disk we need to check the file to see the new capacity
                            checkFileSize();
                        }
                    } catch (IOException e) {
                        RATE_LIMITED_LOGGER.warn("Exception while dequeueing", e);
                        throw new RuntimeException(e);
                    }
                } else if (!batch.isEmpty() && offHeapQueue.isEmpty()) {
                    // Try to move the batch to the in-memory queue if there is enough room
                    if (inMemoryQueue.remainingCapacity() >= batch.size()) {
                        LOG.trace("Found an entry in batch and there was room in-memory, moving it");
                        inMemoryQueue.addAll(batch.unbatch());

                        // Since we just moved the batch cancel any flush if there was one pending
                        //
                        // This is done to prevent a race condition where we were waiting on disk space to flush a batch
                        // but in the meantime we processed all the on disk entries along with the batched entries
                        //
                        // If we didn't prevent that batch from being flushed we would process it twice
                        fileCapacityLatch.cancelFlush();
                    }
                }
            } finally {
                offHeapLock.unlock();
            }
        }

        LOG.trace("Waiting for an entry from in-memory queue...");

        return inMemoryQueue.take();
    }

    @Override
    public boolean isFull() {
        if (offHeapQueue == null) {
            int remaining = inMemoryQueue.remainingCapacity();
            LOG.trace("Checked if full and remaining capacity is {}", remaining);

            return remaining <= 0;
        }

        return fileCapacityLatch.isFull();
    }

    @Override
    public int getSize() {
        if (offHeapQueue == null) {
            return inMemoryQueue.size();
        } else {
            offHeapLock.lock();
            try {
                return inMemoryQueue.size() + (offHeapQueue.size() * batchSize) + batch.size();
            } finally {
                offHeapLock.unlock();
            }
        }
    }

    private List<Map.Entry<String, T>> unbatchSerializedBatch(SerializedBatch serializedBatch)
            throws ExecutionException, InterruptedException {
        final Batch deserializedBatch = new Batch(batchSize);

        serdesPool.submit(() ->
                serializedBatch.batchedMessages.parallelStream()
                        .map(deserializer)
                        .collect(Collectors.toList()))
                .get()
                .forEach(deserializedBatch::add);

        return deserializedBatch.unbatch();
    }

    /**
     * A latch that can be used to wait for the backing file to have capacity.
     */
    private final class FileCapacityLatch {
        private boolean isFull = false;
        private long currentCapacityBytes = Long.MAX_VALUE;
        private boolean flushNeeded = false;

        public synchronized void waitForCapacity(long capacityNeededBytes) throws InterruptedException {
            while (currentCapacityBytes < capacityNeededBytes) {
                if (!flushNeeded) {
                    // Someone cleared out the batch while we were waiting to write
                    break;
                }

                markFull();
                LOG.trace("Waiting for capacity... Need {} bytes but current capacity is {} bytes",
                        capacityNeededBytes, currentCapacityBytes);
                wait();
            }

            markNotFull();
        }

        public synchronized void setCurrentCapacityBytes(long currentCapacityBytes) {
            this.currentCapacityBytes = currentCapacityBytes;
            notifyAll();
        }

        private void markFull() {
            if (!isFull) {
                RATE_LIMITED_LOGGER.info("Off heap file for module {} is now full", moduleName);
                isFull = true;
            }
        }

        private void markNotFull() {
            if (isFull) {
                RATE_LIMITED_LOGGER.info("Off heap file for module {} is no longer full", moduleName);
                isFull = false;
            }
        }

        public synchronized boolean isFull() {
            return isFull;
        }

        public synchronized void markFlushNeeded() {
            flushNeeded = true;
        }

        public synchronized void cancelFlush() {
            flushNeeded = false;
            notifyAll();
        }

        public synchronized boolean isFlushNeeded() {
            return flushNeeded;
        }
    }

    /**
     * The serialized form of the batch that is written to disk.
     */
    private static final class SerializedBatch implements Serializable {
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
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(this);
                out.flush();

                return bos.toByteArray();
            }
        }
    }

    /**
     * A batch of messages to be accumulated prior to being written to disk.
     * <p>
     * Not thread safe. Access should be guarded with a mutex.
     */
    private final class Batch {
        private final List<T> batchedMessages;
        private final int batchSize;

        Batch(int batchSize) {
            this.batchSize = batchSize;
            this.batchedMessages = new ArrayList<>(batchSize);
        }

        void add(T message) {
            if (batchedMessages.size() >= batchSize) {
                throw new IllegalStateException("Attempted to add a message while the batch was full");
            }

            batchedMessages.add(message);
        }

        List<Map.Entry<String, T>> unbatch() {
            List<Map.Entry<String, T>> messages = new ArrayList<>(batchSize);

            batchedMessages.forEach(msg -> messages.add(new AbstractMap.SimpleImmutableEntry<>(null, msg)));
            batchedMessages.clear();

            return messages;
        }

        boolean isEmpty() {
            return batchedMessages.isEmpty();
        }

        boolean isFull() {
            return batchedMessages.size() == batchSize;
        }

        int size() {
            return batchedMessages.size();
        }

        byte[] toSerializedBatchAndClear() throws IOException, ExecutionException, InterruptedException {
            List<byte[]> serializedMessages = serdesPool.submit(() ->
                    batchedMessages.parallelStream()
                            .map(serializer)
                            .collect(Collectors.toList())).get();
            byte[] serializedBatch = new SerializedBatch(serializedMessages).toBytes();
            batchedMessages.clear();

            return serializedBatch;
        }
    }
}
