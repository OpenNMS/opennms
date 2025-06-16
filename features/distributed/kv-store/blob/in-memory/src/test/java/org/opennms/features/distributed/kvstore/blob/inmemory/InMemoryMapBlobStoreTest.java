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

import static org.awaitility.Awaitility.await;

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
