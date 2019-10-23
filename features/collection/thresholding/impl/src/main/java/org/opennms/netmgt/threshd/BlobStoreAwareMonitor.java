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

package org.opennms.netmgt.threshd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.threshd.api.ReinitializableState;
import org.opennms.netmgt.threshd.api.ThresholdStateMonitor;

/**
 * This implementation tracks the in-memory states of thresholds while also being aware of their persistence. This
 * allows for the encapsulation of atomic clear/reinitialize logic where both the in-memory and persisted copies of the
 * state can be cleared together without clients of being aware.
 */
public class BlobStoreAwareMonitor implements ThresholdStateMonitor {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Map<String, ReinitializableState> stateMap = new ConcurrentHashMap<>();
    private final BlobStore blobStore;

    public BlobStoreAwareMonitor(BlobStore blobStore) {
        this.blobStore = Objects.requireNonNull(blobStore);
    }

    @Override
    public void trackState(String key, ReinitializableState state) {
        try {
            readLock.lock();
            stateMap.put(key, state);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void withReadLock(Runnable r) {
        try {
            readLock.lock();
            r.run();
        } finally {
            readLock.unlock();
        }
    }

    private void withWriteLock(Consumer<Map<String, ReinitializableState>> stateConsumer) {
        try {
            writeLock.lock();
            stateConsumer.accept(stateMap);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void reinitializeState(String stateKey) {
        withWriteLock(stateMap -> {
            ReinitializableState reinitializableState = stateMap.get(stateKey);
            if (reinitializableState != null) {
                reinitializableState.reinitialize();
                clearSingleStateFromPersistence(stateKey);
                stateMap.remove(stateKey);
            }
        });
    }

    @Override
    public void reinitializeStates() {
        withWriteLock(stateMap -> {
            if (!stateMap.isEmpty()) {
                stateMap.values().forEach(ReinitializableState::reinitialize);
                clearAllStatesFromPersistence();
                stateMap.clear();
            }
        });
    }

    private void clearSingleStateFromPersistence(String stateKey) {
        blobStore.delete(stateKey, AbstractThresholdEvaluatorState.THRESHOLDING_KV_CONTEXT);
    }

    private void clearAllStatesFromPersistence() {
        blobStore.truncateContext(AbstractThresholdEvaluatorState.THRESHOLDING_KV_CONTEXT);
    }
}
