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

import com.swrve.ratelimitedlogger.RateLimitedLog;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.rocksdb.CompressionType;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class LinkedBlockOffHeapQueue<T> implements DispatchQueue<T> {

    private static final Logger LOG = LoggerFactory.getLogger(LinkedBlockOffHeapQueue.class);
    private final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5)
            .every(Duration.ofSeconds(30))
            .build();

    private final Lock headLock = new ReentrantLock(true);
    private final Lock tailLock = new ReentrantLock(true);

    // This must match the size of the HEADER_LENGTH in QueueFile's Element class
    //
    // We could pull this out of that class reflectively but it seemed easier to just hard code it here
    private static final int SERIALIZED_OBJECT_HEADER_SIZE_IN_BYTES = 4;

    private final Function<T, byte[]> serializer;
    private final Function<byte[], T> deserializer;
    private final String moduleName;

    private final long maxFileSizeInBytes;
    private final int batchSize;

    // it will not include head
    private final BlockingQueue<DataBlock<T>> mainQueue;
    private DataBlock<T> tailBlock;
    private AtomicInteger memoryBlocks = new AtomicInteger(0);
    private AtomicInteger offHeapBlocks = new AtomicInteger(0);
    private int inMemoryQueueSize;

    private MVStore mvstore;
    private RocksDB rocksdb;

    //private final FileCapacityLatch fileCapacityLatch = new FileCapacityLatch()

    public int getMemoryBlocks() {
        return memoryBlocks.get();
    }

    public int getOffHeapBlocks() {
        return offHeapBlocks.get();
    }


    public LinkedBlockOffHeapQueue(Function<T, byte[]> serializer, Function<byte[], T> deserializer,
                                   String moduleName, Path filePath, int inMemoryQueueSize, int batchSize,
                                   long maxFileSizeInBytes) throws IOException {
        if (inMemoryQueueSize < 1) {
            throw new IllegalArgumentException("In memory queue size must be greater than 0");
        }

        if (inMemoryQueueSize % batchSize != 0) {
            throw new IllegalArgumentException("In memory queue size must be a multiple of batch size");
        }

        if (maxFileSizeInBytes < 0) {
            throw new IllegalArgumentException("Max file size must be either 0 or a positive integer");
        }

        this.mainQueue = new LinkedBlockingQueue<>();
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
        this.moduleName = Objects.requireNonNull(moduleName);
        this.batchSize = batchSize;
        this.maxFileSizeInBytes = maxFileSizeInBytes;
        this.inMemoryQueueSize = inMemoryQueueSize;

        File file = Paths.get(filePath.toString(), moduleName).toFile();

        if (!file.isDirectory() && !file.mkdirs()) {
            throw new RuntimeException("Fail make dir. " + file);
        }

//        mvstore = new MVStore.Builder()
//                .cacheSize(500)
////                .keysPerPage(100)
////                .pageSplitSize(100)
//                .compress()
//                .fileName(filePath + "/LinkedBlockOffHeapQueue.mvstore")
//                .open();

        try {

            Options options = new Options();
            options.setCreateIfMissing(true);
            options.setCompressionType(CompressionType.SNAPPY_COMPRESSION);
            options.setEnableBlobFiles(true);
            options.setEnableBlobGarbageCollection(true);
            options.setMinBlobSize(100_000);
            options.setBlobCompressionType(CompressionType.SNAPPY_COMPRESSION);
            options.setWriteBufferSize(100 * 1024 * 1024);
            options.setBlobFileSize(100 * 1024 * 1024);
            options.setTargetFileSizeBase(64 * 1024 * 1024);
            options.setMaxBytesForLevelBase(64 * 1024 * 1024 * 10);
            //options.setMaxBackgroundCompactions(100);
            options.setMaxBackgroundJobs(Math.max(Runtime.getRuntime().availableProcessors(), 3));

            LRUCache cache = new LRUCache(512 * 1024 * 1024, 8);
            options.setRowCache(cache);
//            options.setLogReadaheadSize()
//                    options.setCompactionReadaheadSize().setAllowMmapReads().setUseDirectReads().setBlobCompactionReadaheadSize()


            //   new StackableDB();
            rocksdb = RocksDB.open(options, file.getAbsolutePath());

            long keyCount = rocksdb.getLongProperty("rocksdb.estimate-num-keys");
            LOG.warn("Current keys in db: " + keyCount);
            if (keyCount > 0) {
                //restore data
                DataBlock<T> lastBlock = null;
                RocksIterator ite = rocksdb.newIterator();
                ite.seekToFirst();
                while (ite.isValid()) {
                    String key = new String(ite.key());
                    //LOG.error("KEY: {}", key);
                    DataBlock<T> newBlock = new RocksDBOffHeapDataBlock<>(key, batchSize, serializer, deserializer, rocksdb, ite.value());
                    if (lastBlock != null) {
                        lastBlock.setNextDataBlock(newBlock);
                    }
                    mainQueue.add(newBlock);
                    offHeapBlocks.incrementAndGet();
                    lastBlock = newBlock;
                    ite.next();
                }
                //((OffHeapDataBlock) mainQueue.peek()).enableQueue();
                //mainQueue.peek().notifyNextDataBlock();
            }
        } catch (Exception e) {
            LOG.error("Exception: {}", e);
            throw new RuntimeException(e);
        }

        if (mainQueue.size() == 0) {
            createDataBlock(true);
        }

        // Setting the max file size to 0 or less will disable the off-heap portion of this queue
//        if (maxFileSizeInBytes > 0) {
//            Objects.requireNonNull(filePath);
//
//            File file = Paths.get(filePath.toString(), moduleName).toFile();
//            file.mkdirs();
//
//            // QueueFile unfortunately does not expose its file size usage publicly so we need to access it reflectively
//            try {
//                usedBytesMethod = offHeapQueue.getClass().getDeclaredMethod("usedBytes");
//                usedBytesMethod.setAccessible(true);
//            } catch (NoSuchMethodException e) {
//                LOG.warn("Could not instantiate queue", e);
//                throw new RuntimeException(e);
//            }
//
//            checkFileSize();
//        } else {
//            offHeapQueue = null;
//            usedBytesMethod = null;
//        }
    }


    private void createDataBlock(boolean force) throws IOException {
        if (!force && (tailBlock != null && tailBlock.size() < batchSize)) {
            return;
        }
        DataBlock newBlock;
        if (tailBlock == null || !isMemoryFull()) {
            newBlock = new MemoryDataBlock(batchSize);
            memoryBlocks.incrementAndGet();
        } else {
            newBlock = new RocksDBOffHeapDataBlock<>(batchSize, serializer, deserializer, rocksdb);
            //newBlock = new H2OffHeapDataBlock<>(batchSize, serializer, deserializer, mvstore);
            offHeapBlocks.incrementAndGet();
        }
        //System.out.println("Create "+newBlock+", mem:"+memoryBlocks.get()+", disk: "+ diskBlocks.get());

        LOG.warn("Create {}, mem: {}, disk: {}", newBlock, memoryBlocks.get(), offHeapBlocks.get());
//        if (tmp == null) {
//            ////System.out.println("ERROR 5 !!!! NULL");
//        } else if (tmp != tailBlock) {
//            ////System.out.println("ERROR 3 !!!! " + tmp + " " + tailBlock);
//        } else if (tmp.size() != batchSize) {
//            ////System.out.println("ERROR 4 !!!! oil tail size = " + tmp.size() + " != " + batchSize + " " + tmp);
//        }
        if (tailBlock != null) {
            this.tailBlock.setNextDataBlock(newBlock);
        }
        this.tailBlock = newBlock;
        this.mainQueue.add(newBlock);
    }

    @Override
    public EnqueueResult enqueue(T message, String key) throws WriteFailedException {
        //System.out.println("enqueue lock " + message);
        tailLock.lock();
        ////System.out.println(tailBlock + " enqueue 0 " + message);
        boolean status = tailBlock.enqueue(key, message);
        ////System.out.println(tailBlock + " enqueue 1 status = " + status + " " + message);
        if (!status) {
            ////System.out.println(tailBlock + "enqueue DEFERRED " + status + message);
            //System.out.println("enqueue unlock 1 " + message);
            tailLock.unlock();
            return EnqueueResult.DEFERRED;
        }

        try {
            this.createDataBlock(false);
        } catch (IOException e) {
            ////System.out.println(tailBlock + " enqueue WriteFailedException");
            throw new WriteFailedException(e);
        } finally {
            //System.out.println("enqueue unlock 2 " + message);
            tailLock.unlock();
        }

        ////System.out.println(tailBlock + " enqueue 2 " + " " + message);
        return EnqueueResult.IMMEDIATE;
    }

    @Override
    public Map.Entry<String, T> dequeue() throws InterruptedException {
        LOG.debug("Dequeueing an entry from queue with current size {}", getSize());
        //System.out.println("dequeue 0");
        headLock.lock();
        Map.Entry<String, T> data = null;
        DataBlock<T> head = null;
        while (data == null) {
            boolean headTailEqual = false;
            if (mainQueue.peek() == tailBlock) {
                if (!tailLock.tryLock(1, TimeUnit.MILLISECONDS)) {
                    continue;
                }
                headTailEqual = true;
                //System.out.println("dequeue lock tail");
            }
            head = mainQueue.peek();
            //System.out.println("dequeue 0.2");
            ////System.out.println(head + " dequeue 1 " + head + " size: " + head.size());
            if (head.size() > 0) {
                data = head.dequeue();
                //System.out.println(head + " dequeue 2 " + data.getValue());
                if (headTailEqual) {
                    tailLock.unlock();
                    //System.out.println("dequeue unlock tail 1");
                } else {
                    removeHeadBlockIfNeeded();
                }
            } else {
                if (headTailEqual) {
                    tailLock.unlock();
                    //System.out.println("dequeue unlock tail 2");
                }
            }
        }
        headLock.unlock();
        //System.out.println(head + " dequeue 3 " + data.getValue());
        return data;
    }


    private void removeHeadBlockIfNeeded() throws InterruptedException {
        DataBlock<T> head = mainQueue.peek();
        if (head.size() <= 0 && mainQueue.size() > 1) {
            var tmpHead = mainQueue.remove();
            if (tmpHead != head) {
                LOG.error("Queue is modified. May have data lost");
                throw new RuntimeException("Queue is modified. May have data lost");
            }
            if (head instanceof MemoryDataBlock) {
                memoryBlocks.decrementAndGet();
            } else {
                offHeapBlocks.decrementAndGet();
            }
        } else {
            ////System.out.println("removeHeadBlockIfNeeded last head not going to remove: " + head);
        }
    }

    private boolean isDiskFull() {
        int remain = inMemoryQueueSize;
        if (tailBlock instanceof OffHeapDataBlock) {
            remain -= (tailBlock.size() + this.batchSize * (offHeapBlocks.get() - 1));
        } else {
            remain -= this.batchSize * offHeapBlocks.get();
        }
        return remain >= 0;
    }

    private boolean isMemoryFull() {

//        int remain = inMemoryQueueSize;
//        if (tailBlock instanceof MemoryDataBlock) {
//            remain -= (tailBlock.size() + this.batchSize * (memoryBlocks.get() - 1));
//        } else {
//            remain -= this.batchSize * memoryBlocks.get();
//        }
//        return remain >= 0;

        int memorySize = mainQueue.stream().filter(b -> b instanceof MemoryDataBlock).map(b -> b.size())
                .reduce(0, Integer::sum);
        return inMemoryQueueSize - memorySize <= 0;
    }

    @Override
    public boolean isFull() {
        if (isMemoryFull()) {
            return isDiskFull();
        } else {
            return false;
        }
    }

    @Override
    public int getSize() {

        return mainQueue.stream().map(b -> b.size())
                .reduce(0, Integer::sum);
        //int memorySize = this.batchSize * memoryBlocks.get();
//        long diskSize = this.batchSize * diskBlocks.get();
//        if (tailBlock instanceof OffHeapDataBlock) {
//            diskSize += tailBlock.size();
//        } else {
//            memorySize += tailBlock.size();
//        }
//        return memorySize + (int) diskSize;
    }

    public void shutdown() {
        rocksdb.close();
    }

//    private List<Map.Entry<String, T>> unbatchSerializedBatch(SerializedBatch serializedBatch)
//            throws ExecutionException, InterruptedException {
//        final Batch deserializedBatch = new Batch(batchSize);
//
//        serdesPool.submit(() ->
//                        serializedBatch.batchedMessages.parallelStream()
//                                .map(deserializer)
//                                .collect(Collectors.toList()))
//                .get()
//                .forEach(deserializedBatch::add);
//
//        return deserializedBatch.unbatch();
//    }
//
//    /**
//     * A latch that can be used to wait for the backing file to have capacity.
//     */
//    private final class FileCapacityLatch {
//        private boolean isFull = false;
//        private long currentCapacityBytes = Long.MAX_VALUE;
//        private boolean flushNeeded = false;
//
//        public synchronized void waitForCapacity(long capacityNeededBytes) throws InterruptedException {
//            while (currentCapacityBytes < capacityNeededBytes) {
//                if (!flushNeeded) {
//                    // Someone cleared out the batch while we were waiting to write
//                    break;
//                }
//
//                markFull();
//                LOG.trace("Waiting for capacity... Need {} bytes but current capacity is {} bytes",
//                        capacityNeededBytes, currentCapacityBytes);
//                wait();
//            }
//
//            markNotFull();
//        }
//
//        public synchronized void setCurrentCapacityBytes(long currentCapacityBytes) {
//            this.currentCapacityBytes = currentCapacityBytes;
//            notifyAll();
//        }
//
//        private void markFull() {
//            if (!isFull) {
//                RATE_LIMITED_LOGGER.info("Off heap file for module {} is now full", moduleName);
//                isFull = true;
//            }
//        }
//
//        private void markNotFull() {
//            if (isFull) {
//                RATE_LIMITED_LOGGER.info("Off heap file for module {} is no longer full", moduleName);
//                isFull = false;
//            }
//        }
//
//        public synchronized boolean isFull() {
//            return isFull;
//        }
//
//        public synchronized void markFlushNeeded() {
//            flushNeeded = true;
//        }
//
//        public synchronized void cancelFlush() {
//            flushNeeded = false;
//            notifyAll();
//        }
//
//        public synchronized boolean isFlushNeeded() {
//            return flushNeeded;
//        }
//    }
}
