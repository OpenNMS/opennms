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
import java.util.function.Function;

import com.google.common.collect.Maps;

public class ObjectScope<T> implements Scope {
    private final ScopeName scopeName;
    private final T object;
    private final Map<ContextKey, Function<T, Optional<String>>> accessors = Maps.newHashMap();

    public ObjectScope(final ScopeName scopeName, final T object) {
        this.scopeName = Objects.requireNonNull(scopeName);
        this.object = Objects.requireNonNull(object);
    }

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        return this.accessors.getOrDefault(contextKey, (missing) -> Optional.empty())
                .apply(this.object)
                .map(value -> new ScopeValue(this.scopeName, value));
    }

    @Override
    public Set<ContextKey> keys() {
        return this.accessors.keySet();
    }

    public ObjectScope<T> map(final String context, final String key, final Function<T, Optional<String>> accessor) {
        this.accessors.put(new ContextKey(context, key), accessor);
        return this;
    }
}
