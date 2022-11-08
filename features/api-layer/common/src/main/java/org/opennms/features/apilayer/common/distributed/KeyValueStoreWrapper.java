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
