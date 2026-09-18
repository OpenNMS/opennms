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
package org.opennms.netmgt.wsman.eventlog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncKeyValueStore;
import org.opennms.features.distributed.kvstore.api.JsonStore;

/** Enough of a JsonStore for the cursor tests. */
class InMemoryJsonStore extends AbstractAsyncKeyValueStore<String> implements JsonStore {

    private final Map<String, Map<String, String>> contexts = new HashMap<>();
    private final Map<String, Map<String, Long>> updated = new HashMap<>();

    @Override
    public synchronized long put(String key, String value, String context, Integer ttlInSeconds) {
        contexts.computeIfAbsent(context, c -> new HashMap<>()).put(key, value);
        final long now = System.currentTimeMillis();
        updated.computeIfAbsent(context, c -> new HashMap<>()).put(key, now);
        return now;
    }

    @Override
    public synchronized Optional<String> get(String key, String context) {
        return Optional.ofNullable(contexts.getOrDefault(context, Map.of()).get(key));
    }

    @Override
    public synchronized Optional<Optional<String>> getIfStale(String key, String context, long timestamp) {
        final OptionalLong last = getLastUpdated(key, context);
        if (!last.isPresent()) {
            return Optional.empty();
        }
        return last.getAsLong() > timestamp ? Optional.of(get(key, context)) : Optional.of(Optional.empty());
    }

    @Override
    public synchronized OptionalLong getLastUpdated(String key, String context) {
        final Long t = updated.getOrDefault(context, Map.of()).get(key);
        return t == null ? OptionalLong.empty() : OptionalLong.of(t);
    }

    @Override
    public synchronized Map<String, String> enumerateContext(String context) {
        return new HashMap<>(contexts.getOrDefault(context, Map.of()));
    }

    @Override
    public synchronized void delete(String key, String context) {
        contexts.getOrDefault(context, new HashMap<>()).remove(key);
    }

    @Override
    public synchronized void truncateContext(String context) {
        contexts.remove(context);
    }

    @Override
    public String getName() {
        return "in-memory";
    }
}
