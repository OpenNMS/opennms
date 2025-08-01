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

import com.swrve.ratelimitedlogger.RateLimitedLog;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.QueueCreateFailedException;
import org.opennms.core.ipc.sink.api.ReadFailedException;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.rocksdb.CompressionType;
import org.rocksdb.Env;
import org.rocksdb.FlushOptions;
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

/**
 * This dispatch queue is controlled by main queue with datablocks (memory & offheap), it will append specific datablock depends on resources.
 * serializer & deserializer will impact performance a lot. Suggest to use FST serializer.
 *
 * @param <T>
 */
public class DataBlocksOffHeapQueue<T> implements DispatchQueue<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DataBlocksOffHeapQueue.class);
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
    private AtomicInteger memoryBlockCount = new AtomicInteger(0);
    private AtomicInteger getOffHeapBlockCount = new AtomicInteger(0);
    private int inMemoryQueueSize;
    private long maxOffHeapFileSize;

    private RocksDB rocksdb;
    private final SstFileManager sstFileManager;
    private final Options options;
    private final LRUCache cache;

    /**
     * Create queue. Special notes about maxOffHeapFileSize. It is base on the actual file size. It will not include the size of journals and meta data.
     * @param serializer (suggest to use FST)
     * @param deserializer (suggest to use FST)
     * @param moduleName
     * @param filePath file path for offheap storage
     * @param inMemoryQueueSize
     * @param batchSize
     * @param maxOffHeapFileSize
     * @param writeBufferSize
     * @param cacheSize
     * @throws QueueCreateFailedException
     */
    public DataBlocksOffHeapQueue(Function<T, byte[]> serializer, Function<byte[], T> deserializer,
                                  String moduleName, Path filePath, int inMemoryQueueSize, int batchSize,
                                  long maxOffHeapFileSize, long writeBufferSize, long cacheSize) throws QueueCreateFailedException {
        if (inMemoryQueueSize < 1) {
            throw new IllegalArgumentException("In memory queue size must be greater than 0");
        }

        if (inMemoryQueueSize % batchSize != 0) {
            throw new IllegalArgumentException("In memory queue size must be a multiple of batch size");
        }

        if (maxOffHeapFileSize < 0) {
            throw new IllegalArgumentException("Max offheap file size must be either 0 or a positive integer");
        }

        if (cacheSize < 0) {
            throw new IllegalArgumentException("Cache size must be either 0 or a positive integer");
        }

        this.mainQueue = new LinkedBlockingQueue<>();
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
        Objects.requireNonNull(moduleName);
        this.batchSize = batchSize;
        this.inMemoryQueueSize = inMemoryQueueSize;
        this.maxOffHeapFileSize = maxOffHeapFileSize;

        File file = Paths.get(filePath.toString(), moduleName).toFile();

        if (!file.isDirectory() && !file.mkdirs()) {
            throw new QueueCreateFailedException("Fail make dir. " + file);
        }

        options = new Options();
        options.setCreateIfMissing(true);
        options.setCompressionType(CompressionType.SNAPPY_COMPRESSION);
        options.setEnableBlobFiles(true);
        options.setEnableBlobGarbageCollection(true);
        options.setMinBlobSize(100_000L);
        options.setBlobCompressionType(CompressionType.SNAPPY_COMPRESSION);
        options.setWriteBufferSize(writeBufferSize);
        options.setBlobFileSize(options.writeBufferSize());
        options.setTargetFileSizeBase(64L * 1024L * 1024L);
        options.setMaxBytesForLevelBase(options.targetFileSizeBase() * 10L);

        options.setMaxBackgroundJobs(Math.max(Runtime.getRuntime().availableProcessors(), 3));

        cache = new LRUCache(cacheSize, 8);
        options.setRowCache(cache);

        try {
            Env env = Env.getDefault();
            sstFileManager = new SstFileManager(env);
            options.setSstFileManager(sstFileManager);

            rocksdb = RocksDB.open(options, file.getAbsolutePath());

            restoreDataFromOffHeap();

            if (mainQueue.isEmpty()) {
                createDataBlock(true);
            }
        } catch (RocksDBException ex) {
            throw new QueueCreateFailedException(ex);
        }
    }

    public DataBlocksOffHeapQueue(Function<T, byte[]> serializer, Function<byte[], T> deserializer,
                                  String moduleName, Path filePath, int inMemoryQueueSize, int batchSize,
                                  long maxOffHeapFileSize) throws QueueCreateFailedException {
        this(serializer, deserializer, moduleName, filePath, inMemoryQueueSize, batchSize, maxOffHeapFileSize,
                100L * 1024L * 1024L, 512L * 1024L * 1024L);
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
                getOffHeapBlockCount.incrementAndGet();
                lastBlock = newBlock;
                ite.next();
            }
        } finally {
            ite.close();
        }
    }

    /**
     * It will create memory / offheap block base on resources
     *
     * @param force
     */
    private void createDataBlock(boolean force) {
        if (!force && (tailBlock != null && tailBlock.size() < batchSize)) {
            return;
        }
        DataBlock<T> newBlock;
        if (tailBlock == null || !isMemoryFull()) {
            newBlock = new MemoryDataBlock<>(batchSize);
            memoryBlockCount.incrementAndGet();
        } else {
            newBlock = new RocksDBOffHeapDataBlock<>(batchSize, serializer, deserializer, rocksdb);
            getOffHeapBlockCount.incrementAndGet();
        }

        RATE_LIMITED_LOGGER.debug("Create {}, mem: {}, disk: {}", newBlock, memoryBlockCount.get(), getOffHeapBlockCount.get());

        if (tailBlock != null) {
            this.tailBlock.setNextDataBlock(newBlock);
        }
        this.tailBlock = newBlock;
        this.mainQueue.add(newBlock);
    }

    @Override
    public EnqueueResult enqueue(T message, String key) throws WriteFailedException {
        try {
            synchronized (tailBlock) {
                while (isFull()) {
                    tailBlock.wait(10);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

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
    public Map.Entry<String, T> dequeue() throws InterruptedException {
        headLock.lock();

        Map.Entry<String, T> data = null;
        try {
            while (data == null) {
                data = readData();
            }
        } catch (ReadFailedException e) {
            LOG.error("Fail to dequeue. {}", e.getMessage());
            return null;
        } finally {
            headLock.unlock();
        }
        return data;
    }

    private Map.Entry<String, T> readData() throws ReadFailedException, InterruptedException {
        Map.Entry<String, T> data = null;
        boolean headTailEqual = false;
        if (mainQueue.peek() == tailBlock) {
            if (!tailLock.tryLock(1, TimeUnit.MILLISECONDS)) {
                return null;
            }
            headTailEqual = true;
        }
        var head = mainQueue.peek();
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
                memoryBlockCount.decrementAndGet();
            } else {
                getOffHeapBlockCount.decrementAndGet();
            }
            mainQueue.peek().notifyNextDataBlock();
        }
    }

    private boolean isOffHeapFull() {
        return this.sstFileManager.getTotalSize() >= this.maxOffHeapFileSize;
    }

    private boolean isMemoryFull() {
        MemoryDataBlock<T> lastBlock = (tailBlock instanceof MemoryDataBlock) ? (MemoryDataBlock) tailBlock : null;
        int remain = inMemoryQueueSize - (memoryBlockCount.get() - (lastBlock == null ? 0 : 1)) * batchSize;
        if (remain > 0 && lastBlock == null) {
            return false;
        } else {
            if (lastBlock != null) {
                return remain - lastBlock.size() <= 0;
            } else {
                return true;
            }
        }
    }

    @Override
    public boolean isFull() {
        if (isMemoryFull()) {
            return isOffHeapFull();
        } else {
            return false;
        }
    }

    @Override
    public int getSize() {
        return mainQueue.stream().map(DataBlock::size)
                .reduce(Integer::sum).orElse(0);
    }

    public int getMemoryBlockCount() {
        return memoryBlockCount.get();
    }

    public int getOffHeapBlockCount() {
        return getOffHeapBlockCount.get();
    }

    public long getOffHeapFileSize() {
        return sstFileManager.getTotalSize();
    }

    public void flushOffHeap() throws RocksDBException {
        try (FlushOptions fOptions = new FlushOptions()) {
            rocksdb.flush(fOptions.setWaitForFlush(true).setAllowWriteStall(true));
        }
    }

    public void shutdown() {
        rocksdb.close();
        sstFileManager.close();
        options.close();
        cache.close();
    }
}