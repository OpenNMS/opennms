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
package org.opennms.features.distributed.kvstore.blob.inmemory;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncKeyValueStore;
import org.opennms.features.distributed.kvstore.api.BlobStore;

/**
 * This is a test implementation of {@link BlobStore} that stores values directly in memory via a map.
 */
public class InMemoryMapBlobStore extends AbstractAsyncKeyValueStore<byte[]> implements BlobStore {
    private final Map<Map.Entry<String, String>, Map.Entry<byte[], Long>> inMemoryStore = new HashMap<>();
    private final TimestampGenerator timestampGenerator;

    public InMemoryMapBlobStore(TimestampGenerator timestampGenerator) {
        this.timestampGenerator = Objects.requireNonNull(timestampGenerator);
    }
    
    public static InMemoryMapBlobStore withDefaultTicks() {
        return new InMemoryMapBlobStore(System::currentTimeMillis);
    }

    @Override
    public long put(String key, byte[] value, String context, Integer ttlInSeconds) {
        long timestamp = timestampGenerator.now();

        inMemoryStore.put(new AbstractMap.SimpleImmutableEntry<>(key, context),
                new AbstractMap.SimpleImmutableEntry<>(value, timestamp));

        return timestamp;
    }

    @Override
    public Optional<byte[]> get(String key, String context) {
        Map.Entry<byte[], Long> valueEntry = inMemoryStore.get(new AbstractMap.SimpleImmutableEntry<>(key, context));

        if (valueEntry == null) {
            return Optional.empty();
        }

        return Optional.of(valueEntry.getKey());
    }

    @Override
    public Optional<Optional<byte[]>> getIfStale(String key, String context, long timestamp) {
        OptionalLong lastUpdated = getLastUpdated(key, context);

        if (!lastUpdated.isPresent()) {
            return Optional.empty();
        }

        if (timestamp >= lastUpdated.getAsLong()) {
            return Optional.of(Optional.empty());
        }

        return Optional.of(get(key, context));
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        Map.Entry<byte[], Long> valueEntry = inMemoryStore.get(new AbstractMap.SimpleImmutableEntry<>(key, context));

        if (valueEntry == null) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(valueEntry.getValue());
    }

    @Override
    public String getName() {
        return "In-Memory";
    }

    @Override
    public Map<String, byte[]> enumerateContext(String context) {
        return Collections.unmodifiableMap(inMemoryStore.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getValue().equals(context))
                .collect(Collectors.toMap(entry -> entry.getKey().getKey(), entry -> entry.getValue().getKey())));
    }

    @Override
    public void delete(String key, String context) {
        inMemoryStore.remove(new AbstractMap.SimpleImmutableEntry<>(key, context));
    }
}
