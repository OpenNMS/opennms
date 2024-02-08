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
