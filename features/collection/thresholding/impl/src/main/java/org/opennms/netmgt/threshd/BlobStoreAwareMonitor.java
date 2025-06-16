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
