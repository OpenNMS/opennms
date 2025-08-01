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
package org.opennms.features.distributed.kvstore.json.noop;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import org.opennms.features.distributed.kvstore.api.AbstractAsyncKeyValueStore;
import org.opennms.features.distributed.kvstore.api.JsonStore;

public class NoOpJsonStore extends AbstractAsyncKeyValueStore<String> implements JsonStore {
    @Override
    public long put(String key, String value, String context, Integer ttlInSeconds) {
        return 0;
    }

    @Override
    public Optional<String> get(String key, String context) {
        return Optional.empty();
    }

    @Override
    public Optional<Optional<String>> getIfStale(String key, String context, long timestamp) {
        return Optional.empty();
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        return OptionalLong.empty();
    }

    @Override
    public String getName() {
        return "NoOp";
    }

    @Override
    public Map<String, String> enumerateContext(String context) {
        return Collections.emptyMap();
    }

    @Override
    public void delete(String key, String context) {
    }
}
