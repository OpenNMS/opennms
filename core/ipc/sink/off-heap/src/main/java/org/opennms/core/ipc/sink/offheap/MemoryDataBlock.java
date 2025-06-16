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

import org.opennms.core.ipc.sink.api.ReadFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MemoryDataBlock<T> implements DataBlock<T> {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryDataBlock.class);

    private int queueSize;
    private BlockingQueue<Map.Entry<String, T>> queue;
    private String name;
    private DataBlock<T> nextDataBlock;

    public MemoryDataBlock(int queueSize) {
        this.queueSize = queueSize;
        queue = new ArrayBlockingQueue<>(queueSize, true);
        name = System.nanoTime() + "_" + new Random().nextInt(1000);
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized boolean enqueue(String key, T message) {
        if (this.size() >= this.queueSize) {
            return false;
        }
        return queue.add(new AbstractMap.SimpleImmutableEntry<>(key, message));
    }

    @Override
    public synchronized Map.Entry<String, T> peek() {
        return queue.peek();
    }

    @Override
    public synchronized Map.Entry<String, T> dequeue() throws InterruptedException {
        return queue.take();
    }

    @Override
    public synchronized void notifyNextDataBlock() {
        if (nextDataBlock == null) {
            return;
        }
        if (nextDataBlock instanceof OffHeapDataBlock) {
            OffHeapDataBlock.executorService.submit(() -> {
                try {
                    ((OffHeapDataBlock<T>) nextDataBlock).enableQueue();
                } catch (ReadFailedException | InterruptedException e) {
                    LOG.error("Fail to call enableQueue");
                    Thread.currentThread().interrupt();
                }
            });
        }
        nextDataBlock = null;
    }

    @Override
    public void setNextDataBlock(DataBlock<T> dataBlock) {
        this.nextDataBlock = Objects.requireNonNull(dataBlock);
    }
}
