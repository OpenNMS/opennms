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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

public class FallbackScope implements Scope {
    private final List<Scope> scopes;

    public FallbackScope(final List<Scope> scopes) {
        this.scopes = ImmutableList.copyOf(scopes).reverse();
    }

    public FallbackScope(final Scope... scopes) {
        this.scopes = ImmutableList.copyOf(scopes).reverse();
    }

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        return this.scopes.stream()
                .map(scope -> scope.get(contextKey))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public Set<ContextKey> keys() {
        return this.scopes.stream()
                .flatMap(scope -> scope.keys().stream())
                .collect(Collectors.toSet());
    }
}
