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

import static com.jayway.awaitility.Awaitility.await;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.opennms.features.distributed.kvstore.api.BlobStore;

public class InMemoryMapBlobStoreTest {
    private final BlobStore kvStore = new ExceptionThrowingKVStore();

    @Test(expected = NullPointerException.class)
    public void shouldNPEWithInvalidParamsPut() {
        kvStore.put(null, null, null);
    }

    @Test
    public void shouldCompleteExceptionallyWhenError() {
        AtomicBoolean caughtException = new AtomicBoolean(false);

        kvStore.putAsync("test", new byte[0], "test").exceptionally(t -> {
            if (t.getCause() instanceof TestException) {
                caughtException.set(true);
            }

            return null;
        });
        await().atMost(1, TimeUnit.SECONDS).until(caughtException::get);

        caughtException.set(false);
        kvStore.getAsync("test", "test").exceptionally(t -> {
            if (t.getCause() instanceof TestException) {
                caughtException.set(true);
            }

            return Optional.empty();
        });
        await().atMost(1, TimeUnit.SECONDS).until(caughtException::get);

        caughtException.set(false);
        kvStore.getLastUpdatedAsync("test", "test").exceptionally(t -> {
            if (t.getCause() instanceof TestException) {
                caughtException.set(true);
            }

            return OptionalLong.empty();
        });
        await().atMost(1, TimeUnit.SECONDS).until(caughtException::get);
    }

    @Test
    public void asyncDegradesWithoutException() {
        BlobStore asyncStore = new ModifiedAsyncStore();

        // Overflow the default implementations execution queue to ensure it does not throw a RejectedExecutionException
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 1000; i++) {
            asyncStore.putAsync("test", new byte[0], "test");
        }
    }

    private class ExceptionThrowingKVStore extends InMemoryMapBlobStore {
        public ExceptionThrowingKVStore() {
            super(System::currentTimeMillis);
        }

        @Override
        public long put(String key, byte[] value, String context, Integer ttlInSeconds) {
            throw new TestException();
        }

        @Override
        public Optional<byte[]> get(String key, String context) {
            throw new TestException();
        }

        @Override
        public OptionalLong getLastUpdated(String key, String context) {
            throw new TestException();
        }
    }

    private class ModifiedAsyncStore extends InMemoryMapBlobStore {
        public ModifiedAsyncStore() {
            super(System::currentTimeMillis);
        }

        @Override
        public long put(String key, byte[] value, String context, Integer ttlInSeconds) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
            return super.put(key, value, context, ttlInSeconds);
        }

        @Override
        public Optional<byte[]> get(String key, String context) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
            return super.get(key, context);
        }

        @Override
        public OptionalLong getLastUpdated(String key, String context) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
            return super.getLastUpdated(key, context);
        }
    }

    private class TestException extends RuntimeException {
    }
}
