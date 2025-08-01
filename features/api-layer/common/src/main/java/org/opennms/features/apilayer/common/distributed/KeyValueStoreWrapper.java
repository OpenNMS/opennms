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
package org.opennms.features.apilayer.common.distributed;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

import org.opennms.integration.api.v1.distributed.KeyValueStore;

public class KeyValueStoreWrapper<T> implements KeyValueStore<T> {
    private static final String OIA_PREFIX ="_OIA_"; //prefix to prevent plugin access system keys
    private final org.opennms.features.distributed.kvstore.api.KeyValueStore store;

    public KeyValueStoreWrapper(org.opennms.features.distributed.kvstore.api.KeyValueStore store) {
        this.store = store;
    }

    @Override
    public long put(String key, T value, String context) {
        return store.put(key, value, OIA_PREFIX+context);
    }

    @Override
    public long put(String key, T value, String context, Integer ttlInSeconds) {
        return store.put(key, value, OIA_PREFIX+context, ttlInSeconds);
    }

    @Override
    public Optional<T> get(String key, String context) {
        return store.get(key, OIA_PREFIX+context);
    }

    @Override
    public Optional<T> getIfStale(String key, String context, long timestamp) {
        return store.getIfStale(key, OIA_PREFIX+context, timestamp);
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        return store.getLastUpdated(key, OIA_PREFIX+context);
    }

    @Override
    public Map<String, T> enumerateContext(String context) {
        return store.enumerateContext(OIA_PREFIX+context);
    }

    @Override
    public void delete(String key, String context) {
        store.delete(key, OIA_PREFIX+context);
    }

    @Override
    public void truncateContext(String context) {
        store.truncateContext(OIA_PREFIX+context);
    }

    @Override
    public CompletableFuture<Long> putAsync(String key, T value, String context) {
        return store.putAsync(key, value, OIA_PREFIX+context);
    }

    @Override
    public CompletableFuture<Long> putAsync(String key, T value, String context, Integer ttlInSeconds) {
        return store.putAsync(key, value, OIA_PREFIX+context, ttlInSeconds);
    }

    @Override
    public CompletableFuture<Optional<T>> getAsync(String key, String context) {
        return store.getAsync(key, OIA_PREFIX+context);
    }

    @Override
    public CompletableFuture<Optional<T>> getIfStaleAsync(String key, String context, long timestamp) {
        return store.getIfStaleAsync(key, OIA_PREFIX+context, timestamp);
    }

    @Override
    public CompletableFuture<OptionalLong> getLastUpdatedAsync(String key, String context) {
        return store.getLastUpdatedAsync(key, OIA_PREFIX+context);
    }

    @Override
    public String getName() {
        return store.getName();
    }

    @Override
    public CompletableFuture<Map<String, T>> enumerateContextAsync(String context) {
        return store.enumerateContextAsync(OIA_PREFIX+context);
    }

    @Override
    public CompletableFuture<Void> deleteAsync(String key, String context) {
        return store.deleteAsync(key, OIA_PREFIX+context);
    }

    @Override
    public CompletableFuture<Void> truncateContextAsync(String context) {
        return store.truncateContextAsync(OIA_PREFIX+context);
    }
}
