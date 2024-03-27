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
package org.opennms.features.distributed.kvstore.api;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Skeleton implementation of {@link KeyValueStore} that defaults calls without ttl specified.
 */
public abstract class AbstractKeyValueStore<T> implements KeyValueStore<T> {
    @Override
    public final long put(String key, T value, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(context);

        return put(key, value, context, null);
    }

    @Override
    public final CompletableFuture<Long> putAsync(String key, T value, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(context);

        return putAsync(key, value, context, null);
    }

    /**
     * A default truncate implementation.
     */
    @Override
    public void truncateContext(String context) {
        Objects.requireNonNull(context);

        enumerateContext(context).keySet().forEach(key -> delete(key, context));
    }
}
