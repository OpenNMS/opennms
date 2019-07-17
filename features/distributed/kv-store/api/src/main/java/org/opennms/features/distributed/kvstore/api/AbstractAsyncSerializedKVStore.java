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

package org.opennms.features.distributed.kvstore.api;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An implementation of {@link SerializedKVStore} to extend for implementations that do not otherwise have access to
 * async get/put operations.
 * <p>
 * This implementation wraps the synchronous get/put calls in a {@link CompletableFuture} and executes them async.
 * <p>
 * A separate thread will be used by this implementation by each blocked async call while waiting for the response.
 */
public abstract class AbstractAsyncSerializedKVStore<T, S, U> extends AbstractSerializedKVStore<T, S, U> {
    private final Executor executor;

    protected AbstractAsyncSerializedKVStore(SerializationStrategy<T, S, U> serializationStrategy,
                                             TimestampGenerator timestampGenerator, Executor executor) {
        super(serializationStrategy, timestampGenerator);
        this.executor = Objects.requireNonNull(executor);
    }

    protected AbstractAsyncSerializedKVStore(SerializationStrategy<T, S, U> serializationStrategy,
                                             TimestampGenerator timestampGenerator) {
        // Default impl using a cached thread pool
        this(serializationStrategy, timestampGenerator, Executors.newCachedThreadPool(r -> new Thread(r,
                "kvstore-async-thread")));
    }
    
    @Override
    protected CompletableFuture<Void> putSerializedValueWithTimestampAsync(String key, T serializedValue,
                                                                           long timestamp) {
        return CompletableFuture.supplyAsync(() -> {
            putSerializedValueWithTimestamp(key, serializedValue, timestamp);
            return null;
        }, executor);
    }

    @Override
    protected CompletableFuture<Optional<S>> getSerializedValueAsync(String key) {
        return CompletableFuture.supplyAsync(() -> getSerializedValue(key), executor);
    }

    @Override
    public final CompletableFuture<OptionalLong> getLastUpdatedAsync(String key) {
        CompletableFuture<OptionalLong> lastUpdatedFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                lastUpdatedFuture.complete(getLastUpdated(key));
            } catch (RuntimeException e) {
                lastUpdatedFuture.completeExceptionally(e);
            }
        }, executor);

        return lastUpdatedFuture;
    }
}
