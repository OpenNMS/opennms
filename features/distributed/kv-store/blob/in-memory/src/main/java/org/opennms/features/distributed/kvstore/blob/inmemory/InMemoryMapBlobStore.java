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
