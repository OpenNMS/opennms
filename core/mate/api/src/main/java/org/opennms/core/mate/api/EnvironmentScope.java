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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

public class EnvironmentScope implements Scope {
    public static final String CONTEXT = "env";

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        if (!CONTEXT.equals(contextKey.context)) {
            return Optional.empty();
        }

        if (Strings.isNullOrEmpty(contextKey.key)) {
            return Optional.empty();
        }

        final String value = System.getenv(contextKey.key);
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new ScopeValue(ScopeName.GLOBAL, value));
    }

    @Override
    public Set<ContextKey> keys() {
        return System.getenv().entrySet().stream()
                .map(entry -> new ContextKey(CONTEXT, entry.getKey()))
                .collect(Collectors.toSet());
    }
}
