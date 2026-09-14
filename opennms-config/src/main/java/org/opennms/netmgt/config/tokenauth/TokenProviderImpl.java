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

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.mate.api.Scope;
import org.opennms.netmgt.config.TokenAuthConfigFactory;

/**
 * Default {@link TokenProvider} that bridges {@link TokenAuthConfigFactory}
 * and {@link TokenCache}. Returns empty for an unknown auth name and
 * rethrows acquisition failures wrapped in {@link RuntimeException}.
 */
public class TokenProviderImpl implements TokenProvider {

    private final TokenAuthConfigFactory configFactory;
    private final TokenCache tokenCache;

    public TokenProviderImpl(final TokenAuthConfigFactory configFactory,
                             final TokenCache tokenCache) {
        this.configFactory = Objects.requireNonNull(configFactory, "configFactory");
        this.tokenCache = Objects.requireNonNull(tokenCache, "tokenCache");
    }

    @Override
    public Optional<String> getToken(final String authName) {
        return getToken(authName, null);
    }

    @Override
    public Optional<String> getToken(final String authName, final Scope callingScope) {
        if (authName == null || authName.isEmpty()) {
            return Optional.empty();
        }
        return configFactory.getAuth(authName).map(auth -> {
            try {
                return tokenCache.getToken(auth, callingScope);
            } catch (final IOException e) {
                throw new RuntimeException(
                        "failed to acquire token for auth '" + authName + "'", e);
            }
        });
    }

    public void invalidate(final String authName) {
        tokenCache.invalidate(authName);
    }

    @Override
    public boolean hasAnyAuths() {
        return !configFactory.getAuths().isEmpty();
    }
}
