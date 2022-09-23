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
