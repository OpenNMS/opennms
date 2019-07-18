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
 * A skeleton implementation of {@link KeyValueStore} that persists values in a serialized form.
 * Serialization/deserialization is delegated to a {@link SerializationStrategy}.
 *
 * @param <T> the serialized type
 * @param <S> the type before serialization
 */
public abstract class AbstractSerializedKeyValueStore<T, S> extends AbstractKeyValueStore<S> {
    private final SerializationStrategy<T, S> serializationStrategy;
    private final TimestampGenerator timestampGenerator;

    protected AbstractSerializedKeyValueStore(SerializationStrategy<T, S> serializationStrategy,
                                              TimestampGenerator timestampGenerator) {
        this.serializationStrategy = Objects.requireNonNull(serializationStrategy);
        this.timestampGenerator = Objects.requireNonNull(timestampGenerator);
    }

    @Override
    public long put(String key, S value, String context, Integer ttlInSeconds) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(context);

        long timestamp = timestampGenerator.now();

        putSerializedValueWithTimestamp(key, serializationStrategy.serialize(value), timestamp, context, ttlInSeconds);

        return timestamp;
    }

    @Override
    public final Optional<S> get(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        Optional<T> deserializedValue = getSerializedValue(key, context);

        return deserializedValue.map(serializationStrategy::deserialize);

    }

    @Override
    public abstract OptionalLong getLastUpdated(String key, String context);

    @Override
    public CompletableFuture<Long> putAsync(String key, S value, String context, Integer ttlInSeconds) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(context);

        long timestamp = timestampGenerator.now();
        T serializedValue;

        try {
            serializedValue = serializationStrategy.serialize(value);
        } catch (RuntimeException e) {
            CompletableFuture<Long> putFuture = new CompletableFuture<>();
            putFuture.completeExceptionally(e);
            return putFuture;
        }

        return putSerializedValueWithTimestampAsync(key, serializedValue, timestamp, context, ttlInSeconds).thenApply(v -> timestamp);
    }

    @Override
    public final CompletableFuture<Optional<S>> getAsync(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        return getSerializedValueAsync(key, context).thenApply(opt -> opt.map(serializationStrategy::deserialize));
    }

    @Override
    public abstract CompletableFuture<OptionalLong> getLastUpdatedAsync(String key, String context);

    protected abstract void putSerializedValueWithTimestamp(String key, T serializedValue, long timestamp,
                                                            String context, Integer ttlInSeconds);

    protected abstract Optional<T> getSerializedValue(String key, String context);

    protected abstract CompletableFuture<Void> putSerializedValueWithTimestampAsync(String key, T serializedValue,
                                                                                    long timestamp, String context,
                                                                                    Integer ttlInSeconds);

    protected abstract CompletableFuture<Optional<T>> getSerializedValueAsync(String key, String context);
}
