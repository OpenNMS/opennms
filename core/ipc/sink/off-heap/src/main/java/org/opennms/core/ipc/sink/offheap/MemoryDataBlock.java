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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;

public class MemoryDataBlock<T> implements DataBlock<T> {
    private static final ForkJoinPool serdesPool = new ForkJoinPool(
            Math.max(Runtime.getRuntime().availableProcessors() - 1, 1));
    private int queueSize;
    private BlockingQueue<Map.Entry<String, T>> queue;
    private String name;
    private DataBlock<T> nextDataBlock;

    public MemoryDataBlock(int queueSize) {
        this.queueSize = queueSize;
        queue = new ArrayBlockingQueue<>(queueSize, true);
        name = System.nanoTime() + "_" + (int) Math.floor(Math.random() * 1000);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean enqueue(String key, T message) {
        if (this.size() >= this.queueSize) {
            return false;
        }
        return queue.add(new AbstractMap.SimpleImmutableEntry<>(key, message));
    }

    @Override
    public Map.Entry<String, T> peek() {
        return queue.peek();
    }

    @Override
    public Map.Entry<String, T> dequeue() throws InterruptedException {
        return queue.take();
    }

    @Override
    public void notifyNextDataBlock() {
        if(nextDataBlock == null){
            return;
        }
        if (nextDataBlock instanceof OffHeapDataBlock) {
            serdesPool.submit(() -> ((OffHeapDataBlock<T>) nextDataBlock).enableQueue());
        }
        nextDataBlock = null;
    }

    @Override
    public void setNextDataBlock(DataBlock<T> dataBlock) {
        this.nextDataBlock = Objects.requireNonNull(dataBlock);
    }
}
