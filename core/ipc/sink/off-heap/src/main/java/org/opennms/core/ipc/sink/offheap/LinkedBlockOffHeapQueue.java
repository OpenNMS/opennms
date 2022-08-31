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
import org.h2.mvstore.MVStore;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;
import org.opennms.core.ipc.sink.api.ReadFailedException;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.rocksdb.CompressionType;
import org.rocksdb.Env;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.SstFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5)
            .every(Duration.ofSeconds(30))
            .build();

    private final Lock headLock = new ReentrantLock(true);
    private final Lock tailLock = new ReentrantLock(true);

    private final Function<T, byte[]> serializer;
    private final Function<byte[], T> deserializer;

    private final int batchSize;

    // it will not include head
    private final BlockingQueue<DataBlock<T>> mainQueue;
    private DataBlock<T> tailBlock;
    private AtomicInteger memoryBlocks = new AtomicInteger(0);
    private AtomicInteger offHeapBlocks = new AtomicInteger(0);
    private int inMemoryQueueSize;

    private MVStore mvstore;
    private RocksDB rocksdb;

    public int getMemoryBlocks() {
        return memoryBlocks.get();
    }

    public int getOffHeapBlocks() {
        return offHeapBlocks.get();
    }

    public LinkedBlockOffHeapQueue(Function<T, byte[]> serializer, Function<byte[], T> deserializer,
                                   String moduleName, Path filePath, int inMemoryQueueSize, int batchSize,
                                   long maxFileSizeInBytes) throws QueueCreateFailedException {
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
        Objects.requireNonNull(moduleName);
        this.batchSize = batchSize;
        this.inMemoryQueueSize = inMemoryQueueSize;

        File file = Paths.get(filePath.toString(), moduleName).toFile();

        if (!file.isDirectory() && !file.mkdirs()) {
            throw new QueueCreateFailedException("Fail make dir. " + file);
        }

        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setCompressionType(CompressionType.SNAPPY_COMPRESSION);
        options.setEnableBlobFiles(true);
        options.setEnableBlobGarbageCollection(true);
        options.setMinBlobSize(100_000L);
        options.setBlobCompressionType(CompressionType.SNAPPY_COMPRESSION);
        options.setWriteBufferSize(100L * 1024L * 1024L);
        options.setBlobFileSize(options.writeBufferSize());
        options.setTargetFileSizeBase(64L * 1024L * 1024L);
        options.setMaxBytesForLevelBase(options.targetFileSizeBase() * 10L);

        options.setMaxBackgroundJobs(Math.max(Runtime.getRuntime().availableProcessors(), 3));

        LRUCache cache = new LRUCache(512L * 1024L * 1024L, 8);
        options.setRowCache(cache);

        try {
            rocksdb = RocksDB.open(options, file.getAbsolutePath());
            Env env = Env.getDefault();
            SstFileManager sstFileManager = new SstFileManager(env);
            sstFileManager.setMaxAllowedSpaceUsage(maxFileSizeInBytes);
            options.setSstFileManager(sstFileManager);

            restoreDataFromOffHeap();

            if (mainQueue.isEmpty()) {
                createDataBlock(true);
            }
        } catch (RocksDBException ex) {
            throw new QueueCreateFailedException(ex);
        }
    }

    /**
     * It will read the existing offheap data into mainQueue
     *
     * @throws RocksDBException
     */
    private void restoreDataFromOffHeap() throws RocksDBException {
        long keyCount = rocksdb.getLongProperty("rocksdb.estimate-num-keys");
        LOG.info("Current keys in db: {}", keyCount);
        if (keyCount <= 0) {
            return;
        }
        RocksIterator ite = rocksdb.newIterator();
        try {
            //restore data
            DataBlock<T> lastBlock = null;
            ite.seekToFirst();
            while (ite.isValid()) {
                String key = new String(ite.key());
                DataBlock<T> newBlock = new RocksDBOffHeapDataBlock<>(key, batchSize, serializer, deserializer, rocksdb, ite.value());
                if (lastBlock != null) {
                    lastBlock.setNextDataBlock(newBlock);
                }
                mainQueue.add(newBlock);
                offHeapBlocks.incrementAndGet();
                lastBlock = newBlock;
                ite.next();
            }
        } finally {
            ite.close();
        }
    }

    private void createDataBlock(boolean force) {
        if (!force && (tailBlock != null && tailBlock.size() < batchSize)) {
            return;
        }
        DataBlock<T> newBlock;
        if (tailBlock == null || !isMemoryFull()) {
            newBlock = new MemoryDataBlock<>(batchSize);
            memoryBlocks.incrementAndGet();
        } else {
            newBlock = new RocksDBOffHeapDataBlock<>(batchSize, serializer, deserializer, rocksdb);
            offHeapBlocks.incrementAndGet();
        }

        RATE_LIMITED_LOGGER.debug("Create {}, mem: {}, disk: {}", newBlock, memoryBlocks.get(), offHeapBlocks.get());

        if (tailBlock != null) {
            this.tailBlock.setNextDataBlock(newBlock);
        }
        this.tailBlock = newBlock;
        this.mainQueue.add(newBlock);
    }

    @Override
    public EnqueueResult enqueue(T message, String key) throws WriteFailedException {
        tailLock.lock();
        boolean status = tailBlock.enqueue(key, message);
        if (!status) {
            tailLock.unlock();
            return EnqueueResult.DEFERRED;
        }

        this.createDataBlock(false);
        tailLock.unlock();

        return EnqueueResult.IMMEDIATE;
    }

    @Override
    public synchronized Map.Entry<String, T> dequeue() throws InterruptedException {
        headLock.lock();

        Map.Entry<String, T> data = null;
        DataBlock<T> head;
        try {
            while (data == null) {
                boolean headTailEqual = false;
                if (mainQueue.peek() == tailBlock) {
                    if (!tailLock.tryLock(1, TimeUnit.MILLISECONDS)) {
                        continue;
                    }
                    headTailEqual = true;
                }
                head = mainQueue.peek();
                if (head.size() > 0) {
                    data = head.dequeue();
                    if (headTailEqual) {
                        tailLock.unlock();
                    } else {
                        removeHeadBlockIfNeeded();
                    }
                } else {
                    if (headTailEqual) {
                        tailLock.unlock();
                    }
                }
            }
        } catch (ReadFailedException e) {
            LOG.error("Fail to dequeue. {}", e.getMessage());
            return null;
        } finally {
            headLock.unlock();
        }
        return data;
    }

    private void removeHeadBlockIfNeeded() throws ReadFailedException {
        DataBlock<T> head = mainQueue.peek();
        if (head.size() <= 0 && mainQueue.size() > 1) {
            var tmpHead = mainQueue.remove();
            if (tmpHead != head) {
                LOG.error("Queue is modified. May have data lost");
            }
            if (head instanceof MemoryDataBlock) {
                memoryBlocks.decrementAndGet();
            } else {
                offHeapBlocks.decrementAndGet();
            }
            mainQueue.peek().notifyNextDataBlock();
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
        int memorySize = mainQueue.stream().filter(MemoryDataBlock.class::isInstance).map(DataBlock::size)
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
        return mainQueue.stream().map(DataBlock::size)
                .reduce(Integer::sum).orElse(0);
    }

    public void shutdown() {
        rocksdb.close();
    }
}
