/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.offheap.rocksdb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.ipc.sink.api.OffHeapQueue;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Env;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.SstFileManager;
import org.rocksdb.Status;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;

public class RocksdbStore implements OffHeapQueue {

    private static final Logger LOG = LoggerFactory.getLogger(RocksdbStore.class);
    // Configuration Options for Offheap.
    public static final String OFFHEAP_CONFIG = "org.opennms.core.ipc.sink.offheap";
    private static final String DEFAULT_OFFHEAP_SIZE = "10MB";
    public static final String OFFHEAP_SIZE = "offHeapSize";
    public static final String OFFHEAP_PATH = "offHeapPath";
    private static final int INVALID_SIZE = -1;

    private RocksDB rocksdbInstance;
    private Options options;
    private DBOptions dbOptions;
    private ConfigurationAdmin configAdmin;
    private SstFileManager sstfileManager;
    private Long lastTimeStamp = System.nanoTime();
    private Path dbPath;
    private Map<String, ColumnFamilyHandle> columnFamilyHandleMap = new ConcurrentHashMap<>();
    // Map of ModuleName and corresponding RocksIterator
    private Map<String, RocksIterator> rocksIteratorMap = new ConcurrentHashMap<>();
    private Map<String, byte[]> lastReadKeyMap = new ConcurrentHashMap<>();
    // OffHeap Metrics update number of messages in each module.
    private Map<String, AtomicInteger> messageCounterMap = new ConcurrentHashMap<>();


    public RocksdbStore(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void init() throws IOException, RocksDBException {
        final Properties config = new Properties();
        final Dictionary<String, Object> properties = configAdmin.getConfiguration(OFFHEAP_CONFIG).getProperties();
        if (properties != null) {
            final Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                config.put(key, properties.get(key));
            }
        }
        String offHeapSize = config.getProperty(OFFHEAP_SIZE);
        long maxSizeInBytes;
        if (!Strings.isNullOrEmpty(offHeapSize)) {
            maxSizeInBytes = convertByteSizes(offHeapSize);
        } else {
            maxSizeInBytes = convertByteSizes(DEFAULT_OFFHEAP_SIZE);
        }
        String offHeapPath = config.getProperty(OFFHEAP_PATH);
        if (Strings.isNullOrEmpty(offHeapPath)) {
            throw new IOException("'offHeapPath' needs to be valid path");
        }
        dbPath = Paths.get(offHeapPath);
        // Load library
        RocksDB.loadLibrary();

        try {
            sstfileManager = new SstFileManager(Env.getDefault());
            sstfileManager.setMaxAllowedSpaceUsage(maxSizeInBytes);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        options = new Options().setCreateIfMissing(true)
                .setSstFileManager(sstfileManager);

        List<byte[]> columnFamilies = RocksDB.listColumnFamilies(options, dbPath.toString());

        if (!columnFamilies.isEmpty()) {
            rocksdbInstance = createRocksDBFromColumnFamilies(columnFamilies);
        } else {
            rocksdbInstance = RocksDB.open(options, dbPath.toString());
        }

        LOG.info("Rocksdb instance initialized at {} ", dbPath);

    }

    private RocksDB createRocksDBFromColumnFamilies(List<byte[]> columnFamilies) throws RocksDBException {

        dbOptions = new DBOptions().setCreateIfMissing(true)
                .setSstFileManager(sstfileManager);
        List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
        List<ColumnFamilyDescriptor> columnFamilyDescriptors =
                new ArrayList<>();
        columnFamilies.forEach(columnFamily -> {
            columnFamilyDescriptors.add(new ColumnFamilyDescriptor(
                    columnFamily, new ColumnFamilyOptions()));
        });
        rocksdbInstance = RocksDB.open(dbOptions, dbPath.toString(),
                columnFamilyDescriptors, columnFamilyHandles);
        columnFamilyHandles.forEach(handle -> {
            try {
                columnFamilyHandleMap.put(new String(handle.getName()), handle);
            } catch (RocksDBException e) {
                // Ignore
            }
        });
        return rocksdbInstance;
    }

    @Override
    public Long writeMessage(String moduleName, byte[] message) throws WriteFailedException {
        try {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamily(moduleName);
            long currentTime = getCurrentTimeStamp();
            try (WriteOptions writeOptions = new WriteOptions().setSync(true)) {
                rocksdbInstance.put(columnFamilyHandle, writeOptions, Longs.toByteArray(currentTime), message);
            }
            incrementCounter(moduleName);
            LOG.trace("WroteMessage for module {} with key {}", moduleName, currentTime);
            return currentTime;
        } catch (RocksDBException e) {
            throw new WriteFailedException(e);
        }

    }


    @Override
    public AbstractMap.SimpleImmutableEntry<Long, byte[]> readNextMessage(String moduleName) throws InterruptedException {
        AbstractMap.SimpleImmutableEntry<Long, byte[]> keyValue = null;
        try {
            RocksIterator rocksIterator = getRocksIterator(null, moduleName);
            if (!rocksIterator.isValid()) {
                //1sec delay will prevent iterator to seek multiple times in search of new data.
                Thread.sleep(1000);
                LOG.warn("Re-creating iterator for {}", moduleName);
                rocksIterator = getRocksIterator(rocksIterator, moduleName);
            }
            if (rocksIterator.isValid()) {
                // Get the key, value and move iterator.
                byte[] keyInBytes = rocksIterator.key();
                byte[] value = rocksIterator.value();
                rocksIterator.next();
                Long key = Longs.fromByteArray(keyInBytes);
                keyValue = new AbstractMap.SimpleImmutableEntry<Long, byte[]>(key, value);
                LOG.trace("Read a message for module {} with key {}", moduleName, key);
                // Update read position with current key, get the previous one to delete.
                byte[] lastKeyRead = updateLastReadKey(moduleName, keyInBytes);
                if (lastKeyRead != null) {
                    rocksdbInstance.delete(getColumnFamily(moduleName), lastKeyRead);
                }
                decrementCounter(moduleName);
                return keyValue;
            }
        } catch (RocksDBException e) {
            LOG.error("Error while reading message : {}", e.getMessage());
            if (checkForIoError(e.getStatus())) {
                // IOError puts RocksDB into Read-Only mode.
                // restart DB to put this back into Read/Write mode.
                restart();
                return keyValue;
            }
            throw new RuntimeException(e.getMessage());
        }
        return null;
    }

    private boolean checkForIoError(Status status) {
        if (status != null && status.getCode() != null && status.getCode().equals(Status.Code.IOError)) {
            return true;
        }
        return false;
    }

    /**
     * Restart DB if IOError happens to get DB back into Read/Write Mode.
     */
    private synchronized void restart() {
        destroy();
        try {
            init();
        } catch (IOException | RocksDBException e) {
            LOG.error("Error while initializing RocksDB instance.", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        LOG.info("RocksDB instance restarted at {}", dbPath.toString());
    }

    private synchronized byte[] updateLastReadKey(String moduleName, byte[] keyInBytes) throws RocksDBException {
        byte[] lastKeyRead = lastReadKeyMap.get(moduleName);
        lastReadKeyMap.put(moduleName, keyInBytes);
        return lastKeyRead;
    }

    /**
     * Get RocksIterator for the module specified.
     * This also seeks iterator with the last read key.
     * @param rocksIterator current iterator
     * @param moduleName  module name for which iterator needs to be created.
     * @return RocksIterator for the module.
     */
    private synchronized RocksIterator getRocksIterator(RocksIterator rocksIterator, String moduleName) throws RocksDBException {

        byte[] keyInBytes = lastReadKeyMap.get(moduleName);
        if (keyInBytes == null) {
            ColumnFamilyHandle defaultColumnFamily = rocksdbInstance.getDefaultColumnFamily();
            keyInBytes = rocksdbInstance.get(defaultColumnFamily, moduleName.getBytes());
        }
        // It is recommended to close the iterator often, so we close iterator whenever iterator doesn't find data.
        if (rocksIterator != null) {
            // Close current iterator.
            rocksIterator.close();
            rocksIterator = createRocksIterator(moduleName, true);
        } else {
            rocksIterator = createRocksIterator(moduleName, false);
        }

        if (keyInBytes == null) {
            rocksIterator.seekToFirst();
        } else {
            LOG.trace("Updated seek position for module {}", moduleName);
            rocksIterator.seek(keyInBytes);
            rocksIterator.next();
        }
        return rocksIterator;
    }

    /**
     * This is to serialize RocksDB keys. Use nanoTime to get granularity needed.
     *
     * @return
     */
    private synchronized Long getCurrentTimeStamp() {
        Long curretTimeStamp = System.nanoTime();
        if (curretTimeStamp.equals(lastTimeStamp)) {
            curretTimeStamp = curretTimeStamp + 1;
        } else if (curretTimeStamp < lastTimeStamp) {
            curretTimeStamp = lastTimeStamp + 1;
        }
        lastTimeStamp = curretTimeStamp;
        return curretTimeStamp;
    }

    private void incrementCounter(String moduleName) {
        AtomicInteger counter = messageCounterMap.get(moduleName);
        if (counter == null) {
            messageCounterMap.put(moduleName, new AtomicInteger(1));
        } else {
            int value = counter.incrementAndGet();
            counter.set(value);
            messageCounterMap.put(moduleName, counter);
        }
    }

    private void decrementCounter(String moduleName) {
        AtomicInteger counter = messageCounterMap.get(moduleName);
        if (counter != null) {
            int value = counter.decrementAndGet();
            counter.set(value);
            messageCounterMap.put(moduleName, counter);
        }
    }

    /**
     * Create RocksIterator, create  one if it is not there already.
     *
     * @param moduleName  module name for which the iterator needs to be returned
     * @param newIterator option to create new iterator
     * @return RocksIterator for the module.
     * @throws RocksDBException
     */
    private RocksIterator createRocksIterator(String moduleName, boolean newIterator) throws RocksDBException {

        RocksIterator rocksIterator = rocksIteratorMap.get(moduleName);
        if (rocksIterator == null || newIterator) {
            ColumnFamilyHandle columnFamilyHandle = getColumnFamily(moduleName);
            rocksIterator = rocksdbInstance.newIterator(columnFamilyHandle);
            rocksIteratorMap.put(moduleName, rocksIterator);
            LOG.info("RocksIterator created for module {} ", moduleName);

        }
        return rocksIterator;
    }

    /**
     * Get ColumnFamily for the module, create one if it is not present
     *
     * @param moduleName module name for which ColumnFamily needs to be crated
     * @return Column family handle for the module specified.
     * @throws RocksDBException
     */
    private synchronized ColumnFamilyHandle getColumnFamily(String moduleName) throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle = columnFamilyHandleMap.get(moduleName);
        if (columnFamilyHandle != null) {
            return columnFamilyHandle;
        }
        // Create column family for this module.
        try {
            columnFamilyHandle = rocksdbInstance.createColumnFamily(
                    new ColumnFamilyDescriptor(moduleName.getBytes(),
                            new ColumnFamilyOptions()));
            // Add this to map of handles
            columnFamilyHandleMap.put(moduleName, columnFamilyHandle);
            LOG.info("ColumnFamily created for module {}", moduleName);
            return columnFamilyHandle;
        } catch (RocksDBException e) {
            LOG.error("Failed to create ColumnFamily for module {}", moduleName);
            throw e;
        }
    }


    public void destroy() {

        // Save last read positions in default column family.
        lastReadKeyMap.forEach((key, value) -> {

            try (WriteOptions writeOptions = new WriteOptions().setSync(true)) {
                rocksdbInstance.put(rocksdbInstance.getDefaultColumnFamily(), writeOptions, key.getBytes(), value);
            } catch (RocksDBException e) {
                //Ignore.
            }
        });

        // All objects that we opened need to be closed since these are all JNI objects.
        rocksIteratorMap.forEach((module, iterator) -> iterator.close());
        rocksIteratorMap.clear();

        columnFamilyHandleMap.forEach((module, handle) -> handle.close());
        columnFamilyHandleMap.clear();

        if (sstfileManager != null) {
            sstfileManager.close();
        }

        if (rocksdbInstance != null) {
            rocksdbInstance.close();
        }

        if (options != null) {
            options.close();
        }
        if (dbOptions != null) {
            dbOptions.close();
        }
        LOG.info("RocksDB instance closed at {}", dbPath.toString());

    }

    @Override
    public long getSize() {
        try {
            return rocksdbInstance.getLongProperty("rocksdb.total-sst-files-size");
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return INVALID_SIZE;
    }

    @Override
    public int getNumOfMessages(String moduleName) {
        if (messageCounterMap.get(moduleName) != null) {
            return messageCounterMap.get(moduleName).get();
        }
        return INVALID_SIZE;
    }

    private static long convertByteSizes(String size) {

        String suffix = size.substring(size.length() - 2, size.length());
        double value = 0;
        long bytes = 0;
        try {
            value = Double.parseDouble(size.substring(0, size.length() - 2));
        } catch (NumberFormatException e) {
            //pass
        }
        switch (suffix) {
            case "KB":
                bytes = (long) (value * 1024);
                break;
            case "MB":
                bytes = (long) (value * 1024 * 1024);
                break;
            case "GB":
                bytes = (long) (value * 1024 * 1024 * 1024);
                break;
        }

        if (bytes == 0) {
            LOG.error("Provided offheap size '{}' is invalid, using default as {}", size, DEFAULT_OFFHEAP_SIZE);
            return convertByteSizes(DEFAULT_OFFHEAP_SIZE);
        }
        return bytes;
    }

}
