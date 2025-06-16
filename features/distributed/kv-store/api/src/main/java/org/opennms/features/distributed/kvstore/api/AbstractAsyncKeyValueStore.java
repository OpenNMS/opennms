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
