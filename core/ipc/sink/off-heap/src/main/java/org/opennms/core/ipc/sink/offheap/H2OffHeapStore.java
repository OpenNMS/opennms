/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;
import org.opennms.core.ipc.sink.api.OffHeapQueue;
import org.opennms.core.ipc.sink.api.WriteFailedException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;


public class H2OffHeapStore implements OffHeapQueue {

    private static final Logger LOG = LoggerFactory.getLogger(H2OffHeapStore.class);
    private static final String OFFHEAP_CONFIG = "org.opennms.core.ipc.sink.offheap";
    private final static String OFFHEAP_SIZE = "offHeapSize";
    private final static String DEFAULT_OFFHEAP_SIZE = "10MB";
    // Default wait time for each poll is 1000msec.
    private final static long DEFAULT_WAIT_FOR_POLL = 1000L;

    private JmxReporter reporter = null;
    private MetricRegistry offheapMetrics = new MetricRegistry();
    private MVStore store;
    private long maxSizeInBytes;
    private final ConfigurationAdmin configAdmin;
    // Map of ModuleName and corresponding MvMap.
    private Map<String, MVMap<String, byte[]>> mvMapRegistry = new ConcurrentHashMap<>();
    // Map of ModuleName and corresponding Blocking queue.
    private Map<String, BlockingQueue<String>> queueMap = new ConcurrentHashMap<>();

    public H2OffHeapStore(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    public void init() throws IOException {
        // Use offHeap store.
        store = new MVStore.Builder().fileStore(new OffHeapStore()).open();
        Dictionary<String, Object> properties = configAdmin.getConfiguration(OFFHEAP_CONFIG).getProperties();
        if (properties != null && properties.get(OFFHEAP_SIZE) != null) {
            if (properties.get(OFFHEAP_SIZE) instanceof String) {
                maxSizeInBytes = convertByteSizes((String)properties.get(OFFHEAP_SIZE));
            }
        }
        reporter = JmxReporter.forRegistry(offheapMetrics).inDomain(this.getClass().getPackage().getName()).build();
        offheapMetrics.register(MetricRegistry.name("offHeapSize"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return getSize();

            }
        });
        reporter.start();
        LOG.info("initializing H2 OffHeapStore with max size : {} ", maxSizeInBytes);
    }

    @Override
    public boolean writeMessage(byte[] message, String moduleName, String key) throws WriteFailedException {
        long storeSize = store.getFileStore().size();
        if (message == null || Strings.isNullOrEmpty(moduleName)) {
            throw new WriteFailedException("Invalid message");
        }
        if (storeSize + message.length > maxSizeInBytes) {
            throw new WriteFailedException("Offheap storage exhausted, size = " + maxSizeInBytes);
        }
        MVMap<String, byte[]> mvMap = mvMapRegistry.get(moduleName);
        if (mvMap == null) {
            mvMap = store.openMap(moduleName);
            mvMapRegistry.put(moduleName, mvMap);
            queueMap.put(moduleName, new LinkedBlockingQueue<>());
            LOG.info("initialized mvMap for module : {} ", moduleName);
        }
        mvMap.put(key, message);
        BlockingQueue<String> keys = queueMap.get(moduleName);
        keys.add(key);
        return true;
    }
    
    @Override
    public AbstractMap.SimpleImmutableEntry<String, byte[]> readNextMessage(String moduleName)
            throws InterruptedException {
        BlockingQueue<String> queueOfKeys = queueMap.get(moduleName);
        if (queueOfKeys == null) {
            LOG.warn("No data was ever written for this module {}", moduleName);
            return null;
        }
        // Poll for an item to be available, max wait is 1 second.
        String uuid = queueOfKeys.poll(DEFAULT_WAIT_FOR_POLL, TimeUnit.MILLISECONDS);
        if (uuid == null) {
            return null;
        }
        MVMap<String, byte[]> mvMap = mvMapRegistry.get(moduleName);
        if (mvMap != null) {
            byte[] value = mvMap.get(uuid);
            mvMap.remove(uuid);
            // return an entry with key,value.
            return new AbstractMap.SimpleImmutableEntry<>(
                    uuid, value);
        }
        return null;
    }

    public void destroy() {
        mvMapRegistry.forEach( (module, mvMap)  -> {
            mvMap.clear();
        });
        LOG.info("closing H2OffHeapStore, size = {} ", getSize());
        store.getFileStore().close();
        store.close();
        reporter.stop();
    }

    private long convertByteSizes(String size) {
        String suffix = size.substring(size.length()-2, size.length());
        double value = 0;
        long bytes = 0;
        try {
            value = Double.parseDouble(size.substring(0, size.length() - 2));
        } catch (NumberFormatException e) {
            //pass
        }
        switch(suffix) {
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

    public long getSize() {
        return store.getFileStore().size();
    }


    public int getNumOfMessages(String moduleName) {

        BlockingQueue<String> queue = queueMap.get(moduleName);
        if (queue != null) {
            return queue.size();
        }
        return 0;
    }

}
