/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dnsresolver.netty;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * Underlying cache used to back the {@link CaffeineDnsCache}.
 *
 * Uses the Caffeine caching library as opposed to Netty's io.netty.resolver.dns.Cache implementation so that we can:
 *  1) Limit the cache's size (and intelligently evict entries)
 *  2) Expose cache stats
 *
 * @author jwhite
 */
public abstract class CaffeineCache<E> {

    private final Cache<String, Entries<E>> cache;

    /**
     * Returns {@code true} if this entry should replace all other entries that are already cached for the hostname.
     */
    protected abstract boolean shouldReplaceAll(E entry);

    public CaffeineCache(long maxSize) {
        final Caffeine<String, Entries<E>> caffeine = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, Entries<E>>() {
                    @Override
                    public long expireAfterCreate(String key, Entries<E> value, long currentTime) {
                        // Use the TTL of the first entry added for the key as the initial expiry time
                        // We'll recompute the expiry if/when subsequent entries are added
                        return TimeUnit.SECONDS.toNanos(value.ttl());
                    }

                    @Override
                    public long expireAfterUpdate(String key, Entries<E> value, long currentTime, long currentDuration) {
                        // The value has been updated and may now contain an entry with a shorter TTL then what we originally used
                        // Use the minimum value of the entry TTL and the current expiry duration
                        return Math.min(TimeUnit.SECONDS.toNanos(value.ttl()), currentDuration);
                    }

                    @Override
                    public long expireAfterRead(String key, Entries<E> value, long currentTime, long currentDuration) {
                        // Don't expire after read
                        return currentDuration;
                    }
                })
                .recordStats();
        if (maxSize > 0) {
            // Apply a size limit, if set
            caffeine.maximumSize(maxSize);
        }
        cache = caffeine.build();
    }

    public void clear() {
        cache.invalidateAll();
    }

    public void clear(String key) {
        cache.invalidate(key);
    }

    public Collection<E> get(String key) {
        final Entries<E> entries = cache.getIfPresent(key);
        if (entries == null) {
            return null;
        }
        return entries.get();
    }

    public void cache(String key, E entry, int ttl) {
        Entries<E> entries = cache.getIfPresent(key);
        if (entries == null) {
            entries = new Entries<>(ttl);
        }
        entries.add(entry, ttl, shouldReplaceAll(entry));
        // Always call a put() after an update so that expiry time is recomputed
        cache.put(key, entries);
    }

    public long size() {
        return cache.estimatedSize();
    }

    public CacheStats stats() {
        return cache.stats();
    }

    private static class Entries<E> {
        private final AtomicInteger ttl;
        private final CopyOnWriteArraySet<E> container = new CopyOnWriteArraySet<>();

        Entries(int initialTtl) {
            ttl = new AtomicInteger(initialTtl);
        }

        int ttl() {
            return ttl.get();
        }

        void add(E entry, int ttl, boolean shouldReplaceAll) {
            if (shouldReplaceAll) {
                container.clear();
            }
            container.add(entry);
            // Update the TTL with the minimum value
            this.ttl.updateAndGet(existingTtl -> Math.min(existingTtl, ttl));
        }

        Collection<E> get() {
            return container;
        }
    }
}
