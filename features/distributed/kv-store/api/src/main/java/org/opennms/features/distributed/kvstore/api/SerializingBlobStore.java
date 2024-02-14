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
