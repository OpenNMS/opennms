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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.DispatchQueue;
import org.opennms.core.ipc.sink.api.DispatchQueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueFileOffHeapDispatchQueueFactory implements DispatchQueueFactory {

    private static final Logger LOG = LoggerFactory.getLogger(QueueFileOffHeapDispatchQueueFactory.class);

    private final int inMemoryEntrySize;
    private final long offHeapSize;
    private final int batchSize;
    private final Path baseFilePath;

    private final Map<String, DispatchQueue<?>> queues = new ConcurrentHashMap<>();

    public QueueFileOffHeapDispatchQueueFactory(int inMemoryEntrySize, int batchSize, String offHeapSize,
                                                String baseFilePath) {
        this.inMemoryEntrySize = inMemoryEntrySize;
        this.batchSize = batchSize;
        this.offHeapSize = convertToBytes(offHeapSize);

        if (baseFilePath == null || baseFilePath.length() == 0) {
            this.baseFilePath = Paths.get(System.getProperty("karaf.data"));
        } else {
            this.baseFilePath = Paths.get(baseFilePath);
        }

        LOG.info("DispatchQueue factory initialized with on-heap size: {}, batch size: {}, off-heap size: {}, " +
                        "and file path: {}", this.inMemoryEntrySize, this.batchSize, this.offHeapSize,
                this.baseFilePath);
    }

    @Override
    public <T> DispatchQueue<T> getQueue(AsyncPolicy asyncPolicy, String moduleName, Function<T, byte[]> serializer,
                                         Function<byte[], T> deserializer) {
        if (asyncPolicy.getNumThreads() > inMemoryEntrySize) {
            throw new IllegalArgumentException("The in memory queue size must be greater than or equal to the number" +
                    " of consuming threads");
        }

        return (DispatchQueue<T>) queues.computeIfAbsent(moduleName, (k) -> {
            try {
                return new QueueFileOffHeapDispatchQueue<>(serializer, deserializer, k, baseFilePath,
                        inMemoryEntrySize, batchSize,
                        offHeapSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static long convertToBytes(String sizeWithSuffix) {
        if (sizeWithSuffix == null || sizeWithSuffix.length() == 0) {
            return 0;
        }

        String suffix;
        String sizeValue;
        try {
            suffix = sizeWithSuffix.substring(sizeWithSuffix.length() - 2).toLowerCase();
            sizeValue = sizeWithSuffix.substring(0, sizeWithSuffix.length() - 2).trim();
        } catch (IndexOutOfBoundsException e) {
            LOG.warn("Invalid file size '" + sizeWithSuffix + "'. The file size must include a size and the units. eg 128MB");
            throw e;
        }

        double value = Long.parseLong(sizeValue);
        long bytes;

        switch (suffix) {
            case "kb":
                bytes = (long) value * 1024;
                break;
            case "mb":
                bytes = (long) value * 1024 * 1024;
                break;
            case "gb":
                bytes = (long) value * 1024 * 1024 * 1024;
                break;
            default:
                LOG.warn("Could not parse unit suffix of '" + suffix + "' from max file size '" + sizeWithSuffix + "'");
                throw new IllegalArgumentException("Invalid unit suffix " + suffix);
        }

        return bytes;
    }
}
