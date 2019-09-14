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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * An implementation of {@link KeyValueStore} to extend for implementations that do not otherwise have access to
 * async get/put operations.
 * <p>
 * This implementation wraps the synchronous get/put calls in a {@link CompletableFuture} and executes them async.
 * <p>
 * This implementation attempts to process all requests async but may instead process synchronously depending on the
 * executor implementation used. The default is to degrade to synchronous processing if the executor is full.
 */
public abstract class AbstractAsyncKeyValueStore<T> extends AbstractKeyValueStore<T> {
    private final Executor executor;

    protected AbstractAsyncKeyValueStore(Executor executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    protected AbstractAsyncKeyValueStore() {
        // A reasonable default executor based on available processors that degrades to synchronous processing when full
        this(new ThreadPoolExecutor(1,
                Runtime.getRuntime().availableProcessors() * 10,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("kvstore-async-thread-%d").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    @Override
    public final CompletableFuture<Long> putAsync(String key, T value, String context, Integer ttlInSeconds) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> put(key, value, context, ttlInSeconds), executor);
    }

    @Override
    public final CompletableFuture<Optional<T>> getAsync(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> get(key, context), executor);
    }

    @Override
    public final CompletableFuture<Optional<Optional<T>>> getIfStaleAsync(String key, String context,
                                                                          long timestamp) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> getIfStale(key, context, timestamp));
    }

    @Override
    public final CompletableFuture<OptionalLong> getLastUpdatedAsync(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> getLastUpdated(key, context), executor);
    }

    @Override
    public CompletableFuture<Map<String, T>> enumerateContextAsync(String context) {
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> enumerateContext(context), executor);
    }

    @Override
    public CompletableFuture<Void> deleteAsync(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> {
            delete(key, context);
            return null;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> truncateContextAsync(String context) {
        Objects.requireNonNull(context);

        return CompletableFuture.supplyAsync(() -> {
            truncateContext(context);
            return null;
        }, executor);
    }
}
