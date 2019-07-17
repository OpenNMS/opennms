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
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract representation of a simple key-value store.
 * <p>
 * Retrieved values will first be deserialized before being returned.
 * <p>
 * Keys should uniquely identify the object being persisted/retrieved globally since this API does not define any key
 * hierarchy.
 *
 * @param <T> the type that will be serialized and deserialized by this store
 */
public interface SerializedKVStore<T> {
    /**
     * @return the timestamp the value was persisted with
     * @throws IOException if serialization failed
     */
    long put(String key, T value) throws IOException;

    /**
     * @return an Optional either containing the value if present or empty if the key did not exist
     * @throws IOException            if deserialization failed
     * @throws ClassNotFoundException if deserialization failed
     */
    Optional<T> get(String key) throws IOException, ClassNotFoundException;

    /**
     * @return an optional containing either the timestamp the key's value was last updated or empty if the key did
     * not exist
     */
    OptionalLong getLastUpdated(String key);

    CompletableFuture<Long> putAsync(String key, T value);

    CompletableFuture<Optional<T>> getAsync(String key);

    CompletableFuture<OptionalLong> getLastUpdatedAsync(String key);
}
