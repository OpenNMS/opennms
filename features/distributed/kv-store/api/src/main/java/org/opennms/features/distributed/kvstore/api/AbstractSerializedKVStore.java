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

/**
 * A skeleton implementation of {@link SerializedKVStore} that delegates serialization/deserialization and timestamp
 * generation to a {@link SerializationStrategy} and {@link TimestampGenerator}.
 * <p>
 * All {@link SerializedKVStore} implementations should inherit from this class.
 *
 * @param <T> the serialized type
 * @param <S> the type before serialization
 */
public abstract class AbstractSerializedKVStore<T, S> implements SerializedKVStore<S> {
    private final SerializationStrategy<T, S> serializationStrategy;
    private final TimestampGenerator timestampGenerator;

    protected AbstractSerializedKVStore(SerializationStrategy<T, S> serializationStrategy,
                                        TimestampGenerator timestampGenerator) {
        this.serializationStrategy = Objects.requireNonNull(serializationStrategy);
        this.timestampGenerator = Objects.requireNonNull(timestampGenerator);
    }

    @Override
    public final long put(String key, S value) {
        long timestamp = timestampGenerator.now();

        putSerializedValueWithTimestamp(key, serializationStrategy.serialize(value), timestamp);

        return timestamp;
    }

    @Override
    public final Optional<S> get(String key) {
        Optional<T> deserializedValue = getSerializedValue(key);

        return deserializedValue.map(serializationStrategy::deserialize);

    }

    @Override
    public abstract OptionalLong getLastUpdated(String key);

    @Override
    public final CompletableFuture<Long> putAsync(String key, S value) {
        long timestamp = timestampGenerator.now();
        T serializedValue;

        try {
            serializedValue = serializationStrategy.serialize(value);
        } catch (RuntimeException e) {
            CompletableFuture<Long> putFuture = new CompletableFuture<>();
            putFuture.completeExceptionally(e);
            return putFuture;
        }

        return putSerializedValueWithTimestampAsync(key, serializedValue, timestamp).thenApply(v -> timestamp);
    }

    @Override
    public final CompletableFuture<Optional<S>> getAsync(String key) {
        return getSerializedValueAsync(key).thenApply(opt -> opt.map(serializationStrategy::deserialize));
    }

    @Override
    public abstract CompletableFuture<OptionalLong> getLastUpdatedAsync(String key);

    protected abstract void putSerializedValueWithTimestamp(String key, T serializedValue, long timestamp);

    protected abstract Optional<T> getSerializedValue(String key);

    protected abstract CompletableFuture<Void> putSerializedValueWithTimestampAsync(String key, T serializedValue,
                                                                                    long timestamp);

    protected abstract CompletableFuture<Optional<T>> getSerializedValueAsync(String key);
}
