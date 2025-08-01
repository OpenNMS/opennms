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
    public GuavaSearchableResourceMetadataCache(@Named("search.resourceMetadata.maxCacheEntries") long maxSize, @Named("newtsMetricRegistry") MetricRegistry registry) {
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
