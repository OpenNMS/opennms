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

package org.opennms.features.distributed.kvstore.blob.noop;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncKeyValueStore;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.api.KeyValueStore;

/**
 * A {@link BlobStore key value store} that does nothing. Since no puts result in any operation, all retrieves
 * will always return an empty {@link Optional optional}. This implies any clients using this will also be holding onto
 * their own local copies of the key-values since persisting to this won't store them.
 */
public class NoOpBlobStore extends AbstractAsyncKeyValueStore<byte[]> implements BlobStore {
    private static final NoOpBlobStore INSTANCE = new NoOpBlobStore();

    // A collection of listeners to facilitate testing, all of the listeners will be called with the same calls this
    // impl receives
    private final Collection<KeyValueStore<byte[]>> blobStoreListeners = new CopyOnWriteArrayList<>();

    public static BlobStore getInstance() {
        return INSTANCE;
    }

    @Override
    public long put(String key, byte[] value, String context, Integer ttlInSeconds) {
        blobStoreListeners.forEach(bl -> bl.put(key, value, context, ttlInSeconds));
        return 0;
    }

    @Override
    public Optional<byte[]> get(String key, String context) {
        blobStoreListeners.forEach(bl -> bl.get(key, context));
        return Optional.empty();
    }

    @Override
    public Optional<Optional<byte[]>> getIfStale(String key, String context, long timestamp) {
        blobStoreListeners.forEach(bl -> bl.getIfStale(key, context, timestamp));
        return Optional.empty();
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        blobStoreListeners.forEach(bl -> bl.getLastUpdated(key, context));
        return OptionalLong.empty();
    }

    public void addListener(KeyValueStore<byte[]> listener) {
        blobStoreListeners.add(listener);
    }

    @Override
    public String getName() {
        return "NoOp";
    }

    @Override
    public Map<String, byte[]> enumerateContext(String context) {
        return Collections.emptyMap();
    }

    @Override
    public void delete(String key, String context) {
    }
}
