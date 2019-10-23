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

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract representation of a simple key-value store.
 * <p>
 * Any synchronous calls to this API that fail exceptionally will throw a RuntimeException wrapping the original
 * exception.
 * <p>
 * Asynchronous calls to this API that fail exceptionally will return their future completed exceptionally with the
 * original exception.
 * <p>
 * It is up to the client to handle failures (in the form of unchecked exceptions) resulting from calls to this
 * interface. This is particularly important in the case of asynchronous calls as the backing implementation may be
 * overwhelmed depending on the rate in which case the user must implement their own retry logic to handle the futures
 * that have completed exceptionally (in any case where it is not safe to ignore them).
 *
 * @param <T> the type this store puts/gets
 */
public interface KeyValueStore<T> {
    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return the timestamp the value was persisted with
     */
    long put(String key, T value, String context);

    /**
     * Put a value with a suggested time-to-live after which the value should be expired and removed from the store.
     * <p>
     * Records are expired on a best effort basis and depending on the implementation it is possible to get an expired
     * record.
     *
     * @param context      a context used to differentiate between keys with the same name (forms a compound key)
     * @param ttlInSeconds the time to live in seconds for this key or no ttl if null
     * @return the timestamp the value was persisted with
     */
    long put(String key, T value, String context, Integer ttlInSeconds);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return an optional containing the value if present or empty if the key did not exist
     */
    Optional<T> get(String key, String context);

    /**
     * @param context   a context used to differentiate between keys with the same name (forms a compound key)
     * @param timestamp the timestamp of the last known state such that if an record with a more recent timestamp is
     *                  found the provided timestamp will be considered stale and the new record will be returned
     * @return an optional that will be empty if the key was not found or will contain another optional that will be
     * empty if not stale or contain the value if stale
     */
    Optional<Optional<T>> getIfStale(String key, String context, long timestamp);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return an optional containing the timestamp the key's value was last updated or empty if the key did
     * not exist
     */
    OptionalLong getLastUpdated(String key, String context);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return a map of all the records matching the given context where the map's key is the record's key and the map's
     * value is the records value
     */
    Map<String, T> enumerateContext(String context);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     */
    void delete(String key, String context);

    /**
     * Remove all records for a given context.
     *
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     */
    void truncateContext(String context);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return a future containing the timestamp the value was persisted with
     */
    CompletableFuture<Long> putAsync(String key, T value, String context);

    /**
     * Put a value with a suggested time-to-live after which the value should be expired and removed from the store.
     * <p>
     * Records are expired on a best effort basis and depending on the implementation it is possible to get an expired
     * record.
     *
     * @param context      a context used to differentiate between keys with the same name (forms a compound key)
     * @param ttlInSeconds the time to live in seconds for this key or no ttl if null
     * @return a future containing the timestamp the value was persisted with
     */
    CompletableFuture<Long> putAsync(String key, T value, String context, Integer ttlInSeconds);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return a future containing an optional of the value if present or empty if the key did not exist
     */
    CompletableFuture<Optional<T>> getAsync(String key, String context);

    /**
     * @param context   a context used to differentiate between keys with the same name (forms a compound key)
     * @param timestamp the timestamp of the last known state such that if an record with a more recent timestamp is
     *                  found the provided timestamp will be considered stale and the new record will be returned
     * @return a future containing an optional that will be empty if the key was not found or will contain another
     * optional that will be empty if not stale or contain the value if stale
     */
    CompletableFuture<Optional<Optional<T>>> getIfStaleAsync(String key, String context, long timestamp);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return a future containing an optional of the the timestamp the key's value was last updated or empty if the
     * key did not exist
     */
    CompletableFuture<OptionalLong> getLastUpdatedAsync(String key, String context);

    /**
     * @return the name of the backing implementation
     */
    String getName();

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return a future containing a map of all the records matching the given context where the map's key is the
     * record's key and the map's value is the records value
     */
    CompletableFuture<Map<String, T>> enumerateContextAsync(String context);

    /**
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     * @return a future that is completed when the delete has finished
     */
    CompletableFuture<Void> deleteAsync(String key, String context);

    /**
     * Remove all records for a given context.
     *
     * @param context a context used to differentiate between keys with the same name (forms a compound key)
     */
    CompletableFuture<Void> truncateContextAsync(String context);
}
