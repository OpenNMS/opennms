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
package org.opennms.core.mate.api;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MapScope implements Scope {
    private final ScopeName scopeName;
    private final Map<ContextKey, String> values;

    public MapScope(final ScopeName scopeName, final Map<ContextKey, String> values) {
        this.scopeName = Objects.requireNonNull(scopeName);
        this.values = Objects.requireNonNull(values);
    }

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        return Optional.ofNullable(this.values.get(contextKey))
                .map(value -> new ScopeValue(this.scopeName, value));
    }

    @Override
    public Set<ContextKey> keys() {
        return this.values.keySet();
    }

    public static MapScope singleContext(final ScopeName scopeName, final String context, final Map<String, String> values) {
        return new MapScope(
                scopeName, values.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> new ContextKey(context, e.getKey()),
                        e -> e.getValue())
                ));
    }
}
