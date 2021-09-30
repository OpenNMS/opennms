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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class SerializingBlobStore<V> {
    private final BlobStore blobStore;
    private final Serializer<V> serializer;
    private final Deserializer<V> deserializer;

    public SerializingBlobStore(BlobStore blobStore, Serializer<V> serializer,
                                Deserializer<V> deserializer) {
        this.blobStore = Objects.requireNonNull(blobStore);
        this.serializer = Objects.requireNonNull(serializer);
        this.deserializer = Objects.requireNonNull(deserializer);
    }

    public static <U> SerializingBlobStore<U> ofType(BlobStore blobStore, Serializer<U> serializer,
                                                     Deserializer<U> deserializer) {
        return new SerializingBlobStore<>(blobStore, serializer, deserializer);
    }

    public long put(String key, V value, String context) {
        return blobStore.put(key, serializer.serialize(value), context);
    }

    public long put(String key, V value, String context, Integer ttlInSeconds) {
        return blobStore.put(key, serializer.serialize(value), context, ttlInSeconds);
    }

    public Optional<V> get(String key, String context) {
        return blobStore.get(key, context).map(deserializer::deserialize);
    }

    public Optional<Optional<V>> getIfStale(String key, String context, long timestamp) {
        return blobStore.getIfStale(key, context, timestamp).map(o -> o.map(deserializer::deserialize));
    }

    public CompletableFuture<Long> putAsync(String key, V value, String context) {
        return blobStore.putAsync(key, serializer.serialize(value), context);
    }

    public CompletableFuture<Long> putAsync(String key, V value, String context, Integer ttlInSeconds) {
        return blobStore.putAsync(key, serializer.serialize(value), context, ttlInSeconds);
    }

    public CompletableFuture<Optional<V>> getAsync(String key, String context) {
        return blobStore.getAsync(key, context).thenApply(o -> o.map(deserializer::deserialize));
    }

    public CompletableFuture<Optional<Optional<V>>> getIfStaleAsync(String key, String context, long timestamp) {
        return blobStore.getIfStaleAsync(key, context, timestamp).thenApply(o -> o.map(ov -> ov.map(deserializer::deserialize)));
    }

    public Map<String, V> enumerateContext(String context) {
        return deserializeMap(blobStore.enumerateContext(context));
    }

    public CompletableFuture<Map<String, V>> enumerateContextAsync(String context) {
        return blobStore.enumerateContextAsync(context).thenApply(this::deserializeMap);
    }

    private Map<String, V> deserializeMap(Map<String, byte[]> inputMap) {
        return Collections.unmodifiableMap(inputMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> deserializer.deserialize(e.getValue()))));
    }

    @FunctionalInterface
    public interface Serializer<V> {
        byte[] serialize(V value);
    }

    @FunctionalInterface
    public interface Deserializer<V> {
        V deserialize(byte[] value);
    }
}
