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

import java.io.IOException;
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
 * @param <T> the serialized type that is being persisted to the store
 * @param <S> the deserialized type that is retrieved from the store
 * @param <U> the persisted type before being serialized
 */
public abstract class AbstractSerializedKVStore<T, S, U> implements SerializedKVStore<U> {
    private final SerializationStrategy<T, S, U> serializationStrategy;
    private final TimestampGenerator timestampGenerator;

    protected AbstractSerializedKVStore(SerializationStrategy<T, S, U> serializationStrategy,
                                        TimestampGenerator timestampGenerator) {
        this.serializationStrategy = Objects.requireNonNull(serializationStrategy);
        this.timestampGenerator = Objects.requireNonNull(timestampGenerator);
    }

    @Override
    public final long put(String key, U value) throws IOException {
        long timestamp = timestampGenerator.now();

        putSerializedValueWithTimestamp(key, serializationStrategy.serialize(value), timestamp);

        return timestamp;
    }

    @Override
    public final Optional<U> get(String key) throws IOException, ClassNotFoundException {
        Optional<S> deserializedValue = getSerializedValue(key);

        if (deserializedValue.isPresent()) {
            return Optional.of(serializationStrategy.deserialize(deserializedValue.get()));
        }

        return Optional.empty();
    }

    @Override
    public abstract OptionalLong getLastUpdated(String key);

    @Override
    public final CompletableFuture<Long> putAsync(String key, U value) {
        long timestamp = timestampGenerator.now();
        T serializedValue;

        try {
            serializedValue = serializationStrategy.serialize(value);
        } catch (IOException e) {
            CompletableFuture<Long> putFuture = new CompletableFuture<>();
            putFuture.completeExceptionally(e);
            return putFuture;
        }

        return putSerializedValueWithTimestampAsync(key, serializedValue, timestamp).thenApply(v -> timestamp);
    }

    @Override
    public final CompletableFuture<Optional<U>> getAsync(String key) {
        return getSerializedValueAsync(key).thenApply(o -> {
            if (o.isPresent()) {
                try {
                    return Optional.of(serializationStrategy.deserialize(o.get()));
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            return Optional.empty();
        });
    }

    @Override
    public abstract CompletableFuture<OptionalLong> getLastUpdatedAsync(String key);

    protected abstract void putSerializedValueWithTimestamp(String key, T serializedValue, long timestamp);

    protected abstract Optional<S> getSerializedValue(String key);

    protected abstract CompletableFuture<Void> putSerializedValueWithTimestampAsync(String key, T serializedValue,
                                                                                    long timestamp);

    protected abstract CompletableFuture<Optional<S>> getSerializedValueAsync(String key);
}
