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
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.opennms.core.mate.api.Scope;

/**
 * In-memory cache of auth tokens, keyed by ({@code authName},
 * {@code resolvedFingerprint}) so one logical auth whose URL or
 * credentials are templated with per-node placeholders gets one entry
 * per distinct resolved shape (N endpoints, not N&times;nodes).
 */
public class TokenCache {

    private final TokenAcquirer acquirer;
    private final Clock clock;
    private final ConcurrentMap<CacheKey, CachedToken> cache = new ConcurrentHashMap<>();

    public TokenCache(final TokenAcquirer acquirer) {
        this(acquirer, Clock.systemUTC());
    }

    public TokenCache(final TokenAcquirer acquirer, final Clock clock) {
        this.acquirer = Objects.requireNonNull(acquirer, "acquirer");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public String getToken(final TokenAuth auth) throws IOException {
        return getToken(auth, null);
    }

    public String getToken(final TokenAuth auth, final Scope callingScope) throws IOException {
        Objects.requireNonNull(auth, "auth");
        Objects.requireNonNull(auth.getName(), "auth.name");

        final String fingerprint = acquirer.fingerprint(auth, callingScope);
        final CacheKey key = new CacheKey(auth.getName(), fingerprint);
        final Instant now = clock.instant();
        try {
            // acquire() inside compute() does network I/O, which violates
            // ConcurrentHashMap.compute's "keep it short" contract. We do
            // it deliberately so concurrent callers for the same key
            // coalesce onto a single in-flight HTTP request.
            final CachedToken cached = cache.compute(key, (k, existing) -> {
                if (existing != null && !existing.isExpired(now)) {
                    return existing;
                }
                try {
                    return acquirer.acquire(auth, callingScope);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            return cached.getValue();
        } catch (final UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public void invalidate(final String authName) {
        if (authName != null) {
            cache.keySet().removeIf(k -> authName.equals(k.authName));
        }
    }

    public void invalidateAll() {
        cache.clear();
    }

    public boolean isCached(final String authName) {
        if (authName == null) {
            return false;
        }
        final Instant now = clock.instant();
        return cache.entrySet().stream()
                .anyMatch(e -> authName.equals(e.getKey().authName) && !e.getValue().isExpired(now));
    }

    static final class CacheKey {
        final String authName;
        final String fingerprint;

        CacheKey(final String authName, final String fingerprint) {
            this.authName = Objects.requireNonNull(authName, "authName");
            this.fingerprint = fingerprint == null ? "" : fingerprint;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            final CacheKey that = (CacheKey) o;
            return authName.equals(that.authName) && fingerprint.equals(that.fingerprint);
        }

        @Override
        public int hashCode() {
            return 31 * authName.hashCode() + fingerprint.hashCode();
        }

        @Override
        public String toString() {
            return authName + "#" + fingerprint;
        }
    }
}
