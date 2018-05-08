/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.newts.support;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

/**
 * A copy of {@link org.opennms.newts.cassandra.search.GuavaResourceMetadataCache} with support 
 * for searching resources within the cache.
 *
 * Cached entries are maintained two structures, a Guava Cache and a Radix Tree. The main caching
 * functionality is provided by the Guava Cache and the Radix Tree is used for searching by prefix.
 * The additional memory usage should be minimal since we only need to duplicate the keys, the nodes
 * references the same objects.
 *
 * Both structures are kept in synch via a {@link RemovalListener}.
 *
 * @author jwhite
 */
public class GuavaSearchableResourceMetadataCache implements SearchableResourceMetadataCache, RemovalListener<String, ResourceMetadata> {

    private static final Logger LOG = LoggerFactory.getLogger(GuavaSearchableResourceMetadataCache.class);

    private static final Joiner m_keyJoiner = Joiner.on(':');

    private final Cache<String, ResourceMetadata> m_cache;
    private final ConcurrentRadixTree<ResourceMetadata> m_radixTree;
    private final Meter m_metricReqs;
    private final Meter m_attributeReqs;
    private final Meter m_metricMisses;
    private final Meter m_attributeMisses;

    @Inject
    public GuavaSearchableResourceMetadataCache(@Named("search.resourceMetadata.maxCacheEntries") long maxSize, MetricRegistry registry) {
        m_radixTree = new ConcurrentRadixTree<>(new DefaultCharArrayNodeFactory());

        LOG.info("Initializing resource metadata cache ({} max entries)", maxSize);
        m_cache = CacheBuilder.newBuilder().maximumSize(maxSize).removalListener(this).build();

        m_metricReqs = registry.meter(name("cache", "metric-reqs"));
        m_metricMisses = registry.meter(name("cache", "metric-misses"));
        m_attributeReqs = registry.meter(name("cache", "attribute-reqs"));
        m_attributeMisses = registry.meter(name("cache", "attribute-misses"));

        registry.register(MetricRegistry.name("cache", "size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return m_cache.size();
                    }
                });
        registry.register(MetricRegistry.name("cache", "max-size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return maxSize;
                    }
                });
    }

    @Override
    public Optional<ResourceMetadata> get(Context context, Resource resource) {
        ResourceMetadata r = m_cache.getIfPresent(key(context, resource.getId()));
        return (r != null) ? Optional.of(r) : Optional.<ResourceMetadata>absent();
    }

    @Override
    public void delete(final Context context, final Resource resource) {
        m_cache.invalidate(key(context, resource.getId()));
    }

    private String key(Context context, String resourceId) {
        return m_keyJoiner.join(context.getId(), resourceId);
    }

    private String resourceId(Context context, String key) {
        return key.substring(context.getId().length() + 1);
    }

    @Override
    public void merge(Context context, Resource resource, ResourceMetadata metadata) {

        Optional<ResourceMetadata> o = get(context, resource);

        if (!o.isPresent()) {
            ResourceMetadata newMetadata = new ResourceMetadata(m_metricReqs, m_attributeReqs, m_metricMisses, m_attributeMisses);
            newMetadata.merge(metadata);
            String key = key(context, resource.getId());
            m_cache.put(key, newMetadata);
            m_radixTree.put(key, newMetadata);
            return;
        }

        o.get().merge(metadata);

    }

    @Override
    public List<String> getResourceIdsWithPrefix(Context context, String resourceIdPrefix) {
        return StreamSupport.stream(m_radixTree.getKeysStartingWith(key(context, resourceIdPrefix)).spliterator(), false)
                .map(cs -> resourceId(context, cs.toString()))
                .collect(Collectors.toList());
    }

    @Override
    public void onRemoval(RemovalNotification<String, ResourceMetadata> notification) {
        m_radixTree.remove(notification.getKey());
    }

    public long getSize() {
        return m_cache.size();
    }
}
