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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * Unit tests for {@link TokenCache}. {@link TokenAcquirer} is exercised
 * through subclassed test doubles rather than mocks so the cache's
 * interaction with it stays explicit.
 */
public class TokenCacheTest {

    /** A test acquirer that records call count and returns scripted tokens. */
    private static class CountingAcquirer extends TokenAcquirer {
        final AtomicInteger calls = new AtomicInteger();
        final String tokenValue;
        final Instant expiresAt;

        CountingAcquirer(final String tokenValue, final Instant expiresAt) {
            this.tokenValue = tokenValue;
            this.expiresAt = expiresAt;
        }

        @Override
        public CachedToken acquire(final TokenAuth auth) {
            return acquire(auth, null);
        }

        @Override
        public CachedToken acquire(final TokenAuth auth, final org.opennms.core.mate.api.Scope callerScope) {
            calls.incrementAndGet();
            // Long-enough token so it satisfies TokenCache's
            // minimum-match-length guard for substring lookups.
            return new CachedToken(tokenValue + "-fixture-cache-token-" + calls.get(), expiresAt);
        }
    }

    private static TokenAuth namedAuth(final String name) {
        final TokenAuth a = new TokenAuth();
        a.setName(name);
        a.setUrl("http://does-not-matter");
        return a;
    }

    @Test
    public void cachesAcquiredToken() throws IOException {
        final CountingAcquirer acquirer = new CountingAcquirer("tok", null);
        final TokenCache cache = new TokenCache(acquirer);

        assertEquals("tok-fixture-cache-token-1", cache.getToken(namedAuth("a")));
        assertEquals("tok-fixture-cache-token-1", cache.getToken(namedAuth("a")));
        assertEquals(1, acquirer.calls.get());
    }

    @Test
    public void differentAuthsDoNotShareCacheEntries() throws IOException {
        final CountingAcquirer acquirer = new CountingAcquirer("tok", null);
        final TokenCache cache = new TokenCache(acquirer);

        cache.getToken(namedAuth("a"));
        cache.getToken(namedAuth("b"));
        assertEquals(2, acquirer.calls.get());
    }

    @Test
    public void invalidateForcesReacquisition() throws IOException {
        final CountingAcquirer acquirer = new CountingAcquirer("tok", null);
        final TokenCache cache = new TokenCache(acquirer);

        assertEquals("tok-fixture-cache-token-1", cache.getToken(namedAuth("a")));
        cache.invalidate("a");
        assertEquals("tok-fixture-cache-token-2", cache.getToken(namedAuth("a")));
    }

    @Test
    public void expiredEntryTriggersReacquisition() throws IOException {
        final Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
        final MutableClock clock = new MutableClock(t0);
        final CountingAcquirer acquirer = new CountingAcquirer("tok",
                t0.plusSeconds(60));
        final TokenCache cache = new TokenCache(acquirer, clock);

        cache.getToken(namedAuth("a"));
        assertTrue(cache.isCached("a"));

        clock.advanceSeconds(120); // past expiry
        assertFalse(cache.isCached("a"));

        cache.getToken(namedAuth("a"));
        assertEquals(2, acquirer.calls.get());
    }

    @Test
    public void acquisitionFailureSurfaces() {
        final TokenAcquirer failing = new TokenAcquirer() {
            @Override
            public CachedToken acquire(final TokenAuth auth) throws IOException {
                throw new IOException("boom");
            }
        };
        final TokenCache cache = new TokenCache(failing);

        final IOException ex = assertThrows(IOException.class,
                () -> cache.getToken(namedAuth("a")));
        assertEquals("boom", ex.getMessage());
    }

    @Test
    public void differentResolvedScopesProduceDistinctCacheEntries() throws IOException {
        // Same auth definition, but two callers pass different scopes that
        // resolve a placeholder in the URL to different values. The cache
        // must give them DIFFERENT entries (each its own acquire call) --
        // this is the per-region / per-tenant case.
        final CountingAcquirer acquirer = new CountingAcquirer("tok", null);
        final TokenCache cache = new TokenCache(acquirer);

        final TokenAuth tmplAuth = new TokenAuth();
        tmplAuth.setName("regional");
        tmplAuth.setUrl("https://${node:area}.example.com/auth");

        final org.opennms.core.mate.api.Scope west = singleValue(
                org.opennms.core.mate.api.Scope.ScopeName.NODE, "node", "area", "west");
        final org.opennms.core.mate.api.Scope east = singleValue(
                org.opennms.core.mate.api.Scope.ScopeName.NODE, "node", "area", "east");

        final String tokWest1 = cache.getToken(tmplAuth, west);
        final String tokWest2 = cache.getToken(tmplAuth, west);
        final String tokEast = cache.getToken(tmplAuth, east);

        // Same resolved URL -> same cache entry (one acquire shared)
        assertEquals(tokWest1, tokWest2);
        // Different resolved URL -> different entry (separate acquire)
        assertEquals("east and west should not share a cache entry", false, tokWest1.equals(tokEast));
        assertEquals("only two distinct acquires across three reads", 2, acquirer.calls.get());
    }

    @Test
    public void invalidateRemovesAllVariantsOfAuthName() throws IOException {
        // invalidate(authName) should drop every fingerprint variant for
        // that name, not just the one currently being used by the caller.
        final CountingAcquirer acquirer = new CountingAcquirer("tok", null);
        final TokenCache cache = new TokenCache(acquirer);

        final TokenAuth tmplAuth = new TokenAuth();
        tmplAuth.setName("regional");
        tmplAuth.setUrl("https://${node:area}.example.com/auth");

        final org.opennms.core.mate.api.Scope west = singleValue(
                org.opennms.core.mate.api.Scope.ScopeName.NODE, "node", "area", "west");
        final org.opennms.core.mate.api.Scope east = singleValue(
                org.opennms.core.mate.api.Scope.ScopeName.NODE, "node", "area", "east");

        cache.getToken(tmplAuth, west);
        cache.getToken(tmplAuth, east);
        assertTrue(cache.isCached("regional"));

        cache.invalidate("regional");
        assertFalse(cache.isCached("regional"));
    }

    private static org.opennms.core.mate.api.Scope singleValue(
            final org.opennms.core.mate.api.Scope.ScopeName scopeName,
            final String context, final String key, final String value) {
        return new org.opennms.core.mate.api.Scope() {
            @Override
            public java.util.Optional<ScopeValue> get(final org.opennms.core.mate.api.ContextKey k) {
                if (context.equals(k.getContext()) && key.equals(k.getKey())) {
                    return java.util.Optional.of(new ScopeValue(scopeName, value));
                }
                return java.util.Optional.empty();
            }
            @Override
            public java.util.Set<org.opennms.core.mate.api.ContextKey> keys() {
                return java.util.Set.of();
            }
        };
    }

    @Test
    public void acquisitionFailureDoesNotCachePoison() {
        final AtomicInteger calls = new AtomicInteger();
        final TokenAcquirer flaky = new TokenAcquirer() {
            @Override
            public CachedToken acquire(final TokenAuth auth) throws IOException {
                if (calls.incrementAndGet() == 1) {
                    throw new IOException("transient");
                }
                return new CachedToken("ok", null);
            }
        };
        final TokenCache cache = new TokenCache(flaky);

        assertThrows(IOException.class, () -> cache.getToken(namedAuth("a")));
        // Second call should retry the acquirer (not return a cached failure).
        try {
            assertEquals("ok", cache.getToken(namedAuth("a")));
        } catch (final IOException e) {
            throw new AssertionError("second call should have succeeded", e);
        }
    }

    /** A clock whose instant can be advanced manually. */
    private static class MutableClock extends Clock {
        private Instant now;

        MutableClock(final Instant now) {
            this.now = now;
        }

        void advanceSeconds(final long seconds) {
            now = now.plusSeconds(seconds);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final java.time.ZoneId zone) {
            return this;
        }
    }
}
