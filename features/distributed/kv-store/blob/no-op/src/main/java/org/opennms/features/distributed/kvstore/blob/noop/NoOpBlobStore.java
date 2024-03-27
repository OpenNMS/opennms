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
