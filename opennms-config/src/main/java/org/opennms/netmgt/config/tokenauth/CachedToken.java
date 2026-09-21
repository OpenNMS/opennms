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

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A token returned by {@link TokenAcquirer} and held by {@link TokenCache}.
 * Carries the token string and an optional proactive expiry. A null
 * {@code expiresAt} means the cache should hold the token until something
 * else invalidates it (for example, a 401 on a downstream call).
 */
public final class CachedToken {
    private final String value;
    private final Instant expiresAt;

    public CachedToken(final String value, final Instant expiresAt) {
        this.value = Objects.requireNonNull(value, "value");
        this.expiresAt = expiresAt;
    }

    public String getValue() {
        return value;
    }

    public Optional<Instant> getExpiresAt() {
        return Optional.ofNullable(expiresAt);
    }

    /** Returns true if {@code expiresAt} is set and {@code now} is at or past it. */
    public boolean isExpired(final Instant now) {
        return expiresAt != null && !now.isBefore(expiresAt);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CachedToken)) return false;
        final CachedToken that = (CachedToken) o;
        return Objects.equals(value, that.value) && Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, expiresAt);
    }

    @Override
    public String toString() {
        return "CachedToken{value=<redacted>, expiresAt=" + expiresAt + "}";
    }
}
