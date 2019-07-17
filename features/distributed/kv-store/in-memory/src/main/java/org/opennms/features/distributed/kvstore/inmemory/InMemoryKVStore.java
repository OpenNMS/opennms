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

package org.opennms.features.distributed.kvstore.inmemory;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncSerializedKVStore;
import org.opennms.features.distributed.kvstore.api.TimestampGenerator;

/**
 * This is a test implementation of {@link org.opennms.features.distributed.kvstore.api.SerializedKVStore} that stores
 * values directly in memory via a map without actually serializing/deserializing.
 */
public class InMemoryKVStore<T> extends AbstractAsyncSerializedKVStore<Object, Object, T> {
    private final Map<String, Map.Entry<Object, Long>> inMemoryStore = new HashMap<>();

    public InMemoryKVStore(TimestampGenerator timestampGenerator) {
        super(new InMemorySerializationStrategy<>(), Objects.requireNonNull(timestampGenerator));
    }

    @Override
    public OptionalLong getLastUpdated(String key) {
        Map.Entry<Object, Long> entry = inMemoryStore.get(key);

        if (entry == null) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(entry.getValue());
    }

    @Override
    protected void putSerializedValueWithTimestamp(String key, Object serializedValue, long timestamp) {
        inMemoryStore.put(key, new AbstractMap.SimpleImmutableEntry<>(serializedValue, timestamp));
    }

    @Override
    protected Optional<Object> getSerializedValue(String key) {
        Map.Entry<Object, Long> entry = inMemoryStore.get(key);

        if (entry == null) {
            return Optional.empty();
        }

        return Optional.of(entry.getKey());
    }
}
