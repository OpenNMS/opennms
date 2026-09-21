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
package org.opennms.netmgt.config.tokenauth;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes {@link TokenProvider} tokens as the {@code ${token:<name>}}
 * metadata context, resolving the token at interpolation time so callers
 * always see a current (cache-refreshed) value.
 */
public class TokenScope implements Scope {
    private static final Logger LOG = LoggerFactory.getLogger(TokenScope.class);

    public static final String CONTEXT = "token";

    private final TokenProvider tokenProvider;

    public TokenScope(final TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
    }

    @Override
    public Optional<ScopeValue> get(final ContextKey contextKey) {
        if (!CONTEXT.equals(contextKey.context)) {
            return Optional.empty();
        }
        try {
            return tokenProvider.getToken(contextKey.key)
                    .map(token -> new ScopeValue(ScopeName.GLOBAL, token));
        } catch (RuntimeException e) {
            // a transient token-endpoint failure must not abort the caller's
            // construction path; unresolved placeholders fail at AUTH instead
            LOG.warn("failed to resolve token '{}': {}", contextKey.key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Set<ContextKey> keys() {
        // token names are config-driven and tokens are fetched on demand;
        // enumerating them would trigger acquisition, so expose none
        return Set.of();
    }
}
