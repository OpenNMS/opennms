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

package org.opennms.features.distributed.blob;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.api.SerializingBlobStore;

public abstract class BaseBlobStoreIT {
    protected BlobStore blobStore;
    protected SerializingBlobStore<String> serializingBlobStore;
    
    @Before
    public void setup() throws Exception {
        init();
    }
    
    protected abstract void init() throws Exception;
    
    @Test
    public void canPersistAndRetrieve() {
        String key = "test";
        String context = "canPersistAndRetrieve";
        String state = "state";

        serializingBlobStore.put(key, state, context);
        Optional<String> value = serializingBlobStore.get(key, context);
        assertThat(value.get(), equalTo(state));
    }

    @Test
    public void emptyWhenKeyDoesNotExist() {
        // If Cassandra is available, but the key does not exist we should get an empty optional back
        assertThat(serializingBlobStore.get("test", "emptyWhenKeyDoesNotExist"), equalTo(Optional.empty()));
    }

    @Test
    public void keysExpire() throws InterruptedException {
        String key = "test";
        String context = "keysExpire";
        String value = "test";
        int ttl = 1;
        serializingBlobStore.put(key, value, context, ttl);
        assertThat(serializingBlobStore.get(key, context).get(), equalTo(value));
        Thread.sleep(ttl * 1000);
        assertThat(serializingBlobStore.get(key, context), equalTo(Optional.empty()));
    }

    @Test
    public void canPersistAndRetrieveAsync() throws ExecutionException, InterruptedException, TimeoutException {
        List<CompletableFuture<Long>> putFutures = new ArrayList<>();

        String context = "canPersistAndRetrieveAsync";

        for (int i = 0; i < 1000; i++) {
            String iStr = Integer.toString(i);
            putFutures.add(serializingBlobStore.putAsync(iStr, Integer.toString(i), context));
        }

        // Verify that all the puts finish
        CompletableFuture[] allPutFutures = putFutures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(allPutFutures).get(1, TimeUnit.MINUTES);

        List<CompletableFuture<Optional<String>>> getFutures = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            getFutures.add(serializingBlobStore.getAsync(Integer.toString(i), context));
        }

        // Verify that all the gets finish
        CompletableFuture[] allGetFutures = getFutures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(allGetFutures).get(1, TimeUnit.MINUTES);
        getFutures.stream()
                .filter(f -> {
                    try {
                        return f.get().equals(Optional.empty());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findAny()
                .ifPresent(f -> fail("Should not have found empty optional"));
    }

    @Test
    public void canDetermineIfLatest() throws InterruptedException {
        String key = "tesT";
        String context = "canDetermineIfLatest";
        String originalState = "original";
        long originalTimestamp = serializingBlobStore.put(key, originalState, context);

        Thread.sleep(10);

        assertThat(originalTimestamp, equalTo(blobStore.getLastUpdated(key, context).getAsLong()));
        assertThat(serializingBlobStore.get(key, context).get(), equalTo(originalState));

        String updatedState = "updated";
        long updatedTimestamp = serializingBlobStore.put(key, updatedState, context);
        assertThat(originalTimestamp, lessThan(updatedTimestamp));
        assertThat(serializingBlobStore.get(key, context).get(), equalTo(updatedState));
    }

    @Test
    public void canGetLastUpdatedAsync() throws InterruptedException, ExecutionException, TimeoutException {
        String key = "test";
        String context = "canGetLastUpdatedAsync";
        long timestamp = serializingBlobStore.putAsync(key, "test", context).get(5, TimeUnit.SECONDS);
        long lastUpdated = blobStore.getLastUpdatedAsync(key, context).get(5, TimeUnit.SECONDS).getAsLong();
        assertThat(timestamp, equalTo(lastUpdated));
    }

    @Test
    public void canGetIfStale() {
        String key = "key";
        String context = "canGetIfStale";
        String value = "test";

        long timestamp = serializingBlobStore.put(key, value, context);

        Optional<Optional<String>> currentValue = serializingBlobStore.getIfStale(key, context, timestamp);
        // Should find the key but should already have the latest
        assertThat(currentValue.get(), equalTo(Optional.empty()));

        currentValue = serializingBlobStore.getIfStale(key, context, timestamp - 1);
        // Should find the key and should see its stale
        assertThat(currentValue.get().get(), equalTo(value));
    }
    
    @Test
    public void canEnumerate() {
        String key1 = "key1";
        String context = "canEnumerate";
        String value1 = "test1";
        String key2 = "key2";
        String value2 = "test2";

        serializingBlobStore.put(key1, value1, context);
        serializingBlobStore.put(key2, value2, context);
        
        Map<String, String> resultMap = serializingBlobStore.enumerateContext(context);
        
        assertThat(resultMap.entrySet(), hasItems(new AbstractMap.SimpleImmutableEntry<>(key1, value1),
                new AbstractMap.SimpleImmutableEntry<>(key1, value1)));
    }

    @Test
    public void canEnumerateAsync() throws InterruptedException, ExecutionException, TimeoutException {
        String key1 = "key1";
        String context = "canEnumerateAsync";
        String value1 = "test1";
        String key2 = "key2";
        String value2 = "test2";

        serializingBlobStore.put(key1, value1, context);
        serializingBlobStore.put(key2, value2, context);

        Map<String, String> resultMap = serializingBlobStore.enumerateContextAsync(context)
                .get(5, TimeUnit.SECONDS);

        assertThat(resultMap.entrySet(), hasItems(new AbstractMap.SimpleImmutableEntry<>(key1, value1),
                new AbstractMap.SimpleImmutableEntry<>(key1, value1)));
    }

    @Test
    public void canDelete() {
        String key = "key";
        String context = "canDelete";
        String value = "test";
        
        serializingBlobStore.put(key, value, context);
        blobStore.delete(key, context);
        serializingBlobStore.get(key, context).ifPresent(v -> fail("Failed to delete"));
    }

    @Test
    public void canDeleteAsync() throws InterruptedException, ExecutionException, TimeoutException {
        String key = "key";
        String context = "canDeleteAsync";
        String value = "test";

        serializingBlobStore.put(key, value, context);
        blobStore.deleteAsync(key, context).get(5, TimeUnit.SECONDS);
        serializingBlobStore.get(key, context).ifPresent(v -> fail("Failed to delete"));
    }
    
    @Test
    public void canTruncateContext() {
        String key1 = "key1";
        String context = "canTruncateContext";
        String value1 = "test1";
        String key2 = "key2";
        String value2 = "test2";

        serializingBlobStore.put(key1, value1, context);
        serializingBlobStore.put(key2, value2, context);

        blobStore.truncateContext(context);

        assertThat(serializingBlobStore.enumerateContext(context).keySet(), hasSize(0));
    }

    @Test
    public void canTruncateContextAsync() throws InterruptedException, ExecutionException, TimeoutException {
        String key1 = "key1";
        String context = "canTruncateContextAsync";
        String value1 = "test1";
        String key2 = "key2";
        String value2 = "test2";

        serializingBlobStore.put(key1, value1, context);
        serializingBlobStore.put(key2, value2, context);

        blobStore.truncateContextAsync(context).get(5, TimeUnit.SECONDS);

        assertThat(serializingBlobStore.enumerateContext(context).keySet(), hasSize(0));
    }
}
