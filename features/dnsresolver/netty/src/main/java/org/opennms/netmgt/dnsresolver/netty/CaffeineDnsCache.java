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

/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.opennms.netmgt.dnsresolver.netty;

import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import io.netty.channel.EventLoop;
import io.netty.handler.codec.dns.DnsPtrRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.resolver.dns.DnsCacheEntry;
import io.netty.util.internal.StringUtil;

/**
 * DNS cache implementation largely copied from Netty's
 *   https://github.com/netty/netty/blob/netty-4.1.38.Final/resolver-dns/src/main/java/io/netty/resolver/dns/DefaultDnsCache.java
 * but adapted slightly to work with our {@link CaffeineCache}.
 */
public class CaffeineDnsCache implements ExtendedDnsCache {

    protected static final int MAX_SUPPORTED_TTL_SECS = (int) TimeUnit.DAYS.toSeconds(365 * 2);
    protected static final int DEFAULT_NEGATIVE_TTL_SECS = (int) TimeUnit.MINUTES.toSeconds(5);
    protected static final int DEFAULT_MAX_SIZE = 10000;

    private final CaffeineCache<ExtendedDnsCacheEntry> resolveCache;

    private final int minTtl;
    private final int maxTtl;
    private final int negativeTtl;
    private final long maxSize;

    // Track hits and misses ourselves since the underlying cache doesn't expose these
    private final Meter cacheHits = new Meter();
    private final Meter cacheMisses = new Meter();

    /**
     * Create a cache that respects the TTL returned by the DNS server
     * and doesn't cache negative responses.
     */
    public CaffeineDnsCache() {
        this(0, MAX_SUPPORTED_TTL_SECS, DEFAULT_NEGATIVE_TTL_SECS, DEFAULT_MAX_SIZE);
    }

    /**
     * Create a cache.
     * @param minTtl the minimum TTL
     * @param maxTtl the maximum TTL
     * @param negativeTtl the TTL for failed queries
     */
    public CaffeineDnsCache(int minTtl, int maxTtl, int negativeTtl, long maxSize) {
        this.minTtl = Math.min(MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(minTtl, "minTtl"));
        this.maxTtl = Math.min(MAX_SUPPORTED_TTL_SECS, checkPositiveOrZero(maxTtl, "maxTtl"));
        Preconditions.checkArgument(minTtl <= maxTtl, "minTtl: " + minTtl + ", maxTtl: " + maxTtl + " (expected: 0 <= minTtl <= maxTtl)");
        this.negativeTtl = checkPositiveOrZero(negativeTtl, "negativeTtl");
        this.maxSize = checkPositiveOrZero(maxSize, "maxSize");

        resolveCache = new CaffeineCache<ExtendedDnsCacheEntry>(maxSize) {
            @Override
            protected boolean shouldReplaceAll(ExtendedDnsCacheEntry entry) {
                return entry.cause() != null;
            }
        };
    }

    /**
     * Returns the minimum TTL of the cached DNS resource records (in seconds).
     *
     * @see #maxTtl()
     */
    public int minTtl() {
        return minTtl;
    }

    /**
     * Returns the maximum TTL of the cached DNS resource records (in seconds).
     *
     * @see #minTtl()
     */
    public int maxTtl() {
        return maxTtl;
    }

    /**
     * Returns the TTL of the cache for the failed DNS queries (in seconds). The default value is {@code 0}, which
     * disables the cache for negative results.
     */
    public int negativeTtl() {
        return negativeTtl;
    }

    /**
     * Returns the maximum number of elements allows in the cache.
     */
    public long maxSize() {
        return maxSize;
    }

    @Override
    public void clear() {
        resolveCache.clear();
    }

    @Override
    public boolean clear(String hostname) {
        checkNotNull(hostname, "hostname");
        resolveCache.clear(ensureTrailingDot(hostname));
        // The backing cache doesn't provide a return value for whether or not
        // the value was actually removed, so we always return false
        return false;
    }

    @Override
    public List<? extends DnsCacheEntry> get(String hostname, DnsRecord[] additionals) {
        checkNotNull(hostname, "hostname");
        if (!emptyAdditionals(additionals)) {
            return Collections.<DnsCacheEntry>emptyList();
        }
        final Collection<? extends  DnsCacheEntry> cachedEntries = resolveCache.get(ensureTrailingDot(hostname));
        if (cachedEntries == null) {
            cacheMisses.mark();
            return null;
        }
        cacheHits.mark();
        // Convert collection to a list
        return new LinkedList<>(cachedEntries);
    }

    /**
     * Positive caching for normal lookups.
     */
    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals,
                               InetAddress address, long originalTtl, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(address, "address");
        checkNotNull(loop, "loop");
        DefaultExtendedDnsCacheEntry e = new DefaultExtendedDnsCacheEntry(hostname, address);
        if (maxTtl == 0 || !emptyAdditionals(additionals)) {
            return e;
        }
        resolveCache.cache(ensureTrailingDot(hostname), e, Math.max(minTtl, (int) Math.min(maxTtl, originalTtl)));
        return e;
    }

    /**
     * Positive caching for reverse lookups.
     */
    @Override
    public ExtendedDnsCacheEntry cache(String hostname, DnsPtrRecord ptrRecord, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(ptrRecord, "ptrRecord");
        checkNotNull(loop, "loop");
        DefaultExtendedDnsCacheEntry e = new DefaultExtendedDnsCacheEntry(hostname, ptrRecord);
        if (maxTtl == 0) {
            return e;
        }
        resolveCache.cache(ensureTrailingDot(hostname), e, Math.max(minTtl, (int) Math.min(maxTtl, ptrRecord.timeToLive())));
        return e;
    }

    /**
     * Negative caching for failures (or empty reverse lookups).
     */
    @Override
    public DnsCacheEntry cache(String hostname, DnsRecord[] additionals, Throwable cause, EventLoop loop) {
        checkNotNull(hostname, "hostname");
        checkNotNull(cause, "cause");
        checkNotNull(loop, "loop");

        DefaultExtendedDnsCacheEntry e = new DefaultExtendedDnsCacheEntry(hostname, cause);
        if (negativeTtl == 0 || !emptyAdditionals(additionals)) {
            return e;
        }

        resolveCache.cache(ensureTrailingDot(hostname), e, negativeTtl);
        return e;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("minTtl", minTtl)
                .add("maxTtl", maxTtl)
                .add("negativeTtl", negativeTtl)
                .add("cached resolved hostname=", resolveCache.size())
                .toString();
    }

    public long getSize() {
        return resolveCache.size();
    }

    private enum CacheMetric {
        SIZE("cacheSize", c -> (Gauge<Long>) c.resolveCache::size),
        MAX_SIZE("cacheMaxSize", c -> (Gauge<Long>) c::maxSize),
        EVICTION_COUNT("cacheEvictionCount", c -> (Gauge<Long>) c.resolveCache.stats()::evictionCount),
        HITS("cacheHits", c -> c.cacheHits),
        MISSES("cacheMisses", c -> c.cacheMisses);

        String name;
        Function<CaffeineDnsCache, Metric> metricSupplier;

        CacheMetric(String name, Function<CaffeineDnsCache, Metric> metricSupplier) {
            this.name = Objects.requireNonNull(name);
            this.metricSupplier = Objects.requireNonNull(metricSupplier);
        }

        public String getName() {
            return name;
        }

        public Metric getMetric(CaffeineDnsCache cache) {
            return metricSupplier.apply(cache);
        }
    }

    public void registerMetrics(MetricRegistry metrics) {
        for (CacheMetric cm : CacheMetric.values()) {
            metrics.register(cm.getName(), cm.getMetric(this));
        }
    }

    public void unregisterMetrics(MetricRegistry metrics) {
        for (CacheMetric cm : CacheMetric.values()) {
            metrics.remove(cm.getName());
        }
    }

    private static final class DefaultExtendedDnsCacheEntry implements ExtendedDnsCacheEntry {
        private final String hostname;
        private final String hostnameFromPtrRecord;
        private final InetAddress address;
        private final Throwable cause;

        DefaultExtendedDnsCacheEntry(String hostname, InetAddress address) {
            this.hostname = hostname;
            this.address = address;
            this.cause = null;
            this.hostnameFromPtrRecord = null;
        }

        DefaultExtendedDnsCacheEntry(String hostname, Throwable cause) {
            this.hostname = hostname;
            this.cause = cause;
            this.address = null;
            this.hostnameFromPtrRecord = null;
        }

        public DefaultExtendedDnsCacheEntry(String hostname, DnsPtrRecord ptrRecord) {
            this.hostname = hostname;
            this.hostnameFromPtrRecord = ptrRecord.hostname();
            this.address = null;
            this.cause = null;
        }

        @Override
        public InetAddress address() {
            return address;
        }

        @Override
        public Throwable cause() {
            return cause;
        }

        String hostname() {
            return hostname;
        }

        @Override
        public String hostnameFromPtrRecord() {
            return hostnameFromPtrRecord;
        }

        @Override
        public String toString() {
            if (cause != null) {
                return hostname + '/' + cause;
            } else if (hostnameFromPtrRecord != null) {
                return hostname + '/' + hostnameFromPtrRecord;
            } else {
                return hostname + '/' + address;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefaultExtendedDnsCacheEntry)) return false;
            DefaultExtendedDnsCacheEntry that = (DefaultExtendedDnsCacheEntry) o;
            return Objects.equals(hostname, that.hostname) &&
                    Objects.equals(hostnameFromPtrRecord, that.hostnameFromPtrRecord) &&
                    Objects.equals(address, that.address) &&
                    Objects.equals(cause, that.cause);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hostname, hostnameFromPtrRecord, address, cause);
        }
    }

    private static boolean emptyAdditionals(DnsRecord[] additionals) {
        return additionals == null || additionals.length == 0;
    }

    private static String ensureTrailingDot(String hostname) {
        return StringUtil.endsWith(hostname, '.') ? hostname : hostname + '.';
    }
}
