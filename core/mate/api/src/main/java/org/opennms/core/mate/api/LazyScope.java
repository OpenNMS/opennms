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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

/**
 * A {@link Scope} that defers construction of its delegate until the first lookup and
 * memoizes the result. Entity scopes issue database transactions when built, and
 * interpolation only consults a scope when the input contains a metadata expression,
 * so this makes that cost demand-driven and paid at most once.
 */
public class LazyScope implements Scope {
    private final Supplier<Scope> delegate;

    public LazyScope(final Supplier<Scope> delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = Suppliers.memoize(() -> Objects.requireNonNull(delegate.get(), "scope supplier returned null"));
    }

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        return this.delegate.get().get(contextKey);
    }

    @Override
    public Set<ContextKey> keys() {
        return this.delegate.get().keys();
    }
}
