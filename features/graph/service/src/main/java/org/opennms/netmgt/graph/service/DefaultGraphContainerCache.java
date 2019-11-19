/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.service.GraphContainerCache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class DefaultGraphContainerCache implements GraphContainerCache {

    private final Map<String, ImmutableGraphContainer> cache = new HashMap<>();
    private final Map<String, ScheduledFuture<?>> futureHandles = new HashMap<>();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5, new ThreadFactoryBuilder().setNameFormat("graph-cache-%d").build());
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public boolean has(String containerId) {
        lock.readLock().lock();
        try {
            return cache.containsKey(containerId);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void invalidate(String containerId) {
        lock.writeLock().lock();
        try {
            cache.remove(containerId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public ImmutableGraphContainer get(String containerId) {
        lock.readLock().lock();
        try {
            return cache.get(containerId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void cancel(String containerId) {
        lock.writeLock().lock();
        try {
            final ScheduledFuture<?> remove = futureHandles.remove(containerId);
            if (remove != null) {
                remove.cancel(true);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void put(ImmutableGraphContainer graphContainer) {
        Objects.requireNonNull(graphContainer);
        lock.writeLock().lock();
        try {
            cache.put(graphContainer.getId(), graphContainer);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void periodicallyInvalidate(String containerId, int reloadInterval, TimeUnit reloadUnit) {
        lock.writeLock().lock();
        try {
            cancel(containerId);
            final ScheduledFuture<?> scheduledFuture = executorService.scheduleWithFixedDelay(() -> {
                if (!Thread.currentThread().isInterrupted()) {
                    invalidate(containerId);
                }
            }, reloadInterval, reloadInterval, reloadUnit);
            futureHandles.put(containerId, scheduledFuture);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void shutdown() {
        lock.writeLock().lock();
        try {
            executorService.shutdown();
            futureHandles.values().forEach(handle -> handle.cancel(true));
            futureHandles.clear();
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
