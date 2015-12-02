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

import javax.inject.Inject;
import javax.inject.Named;

import org.nustaq.serialization.FSTConfiguration;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceIdSplitter;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * A caching strategy that stores the {@link org.opennms.newts.cassandra.search.ResourceMetadata} in
 * a Redis database.
 *
 * Can be used when the cache needs to be shared amongst several JVMs, or when persistence is required.
 *
 * Note that this cache will operate slower than the {@link GuavaSearchableResourceMetadataCache} since
 * it requires network I/O and serialization/deserialization of the associated structures. However, this
 * cache will also operate with significantly less memory.
 *
 * FST is used for serialization instead Java's default implementation.
 *
 * @author jwhite
 */
public class RedisResourceMetadataCache implements SearchableResourceMetadataCache {

    private static final String METADATA_PREFIX = "_M";

    private static final String SEARCH_PREFIX = "_S";

    private static final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    private static final Joiner m_keyJoiner = Joiner.on(ResourceIdSplitter.SEPARATOR);

    private final ResourceIdSplitter m_resourceIdSplitter;

    private final Jedis m_jedis;

    private final Meter m_metricReqs;
    private final Meter m_attributeReqs;
    private final Meter m_metricMisses;
    private final Meter m_attributeMisses;

    @Inject
    public RedisResourceMetadataCache(@Named("redis.hostname") String hostname, @Named("redis.port") Integer port, MetricRegistry registry, ResourceIdSplitter resourceIdSplitter) {
        Preconditions.checkNotNull(hostname, " hostname argument");
        Preconditions.checkNotNull(port, "port argument");
        m_jedis = new Jedis(hostname, port);

        Preconditions.checkNotNull(registry, "registry argument");
        m_metricReqs = registry.meter(name("cache", "metric-reqs"));
        m_metricMisses = registry.meter(name("cache", "metric-misses"));
        m_attributeReqs = registry.meter(name("cache", "attribute-reqs"));
        m_attributeMisses = registry.meter(name("cache", "attribute-misses"));

        registry.register(MetricRegistry.name("cache", "size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return m_jedis.dbSize();
                    }
                });
        registry.register(MetricRegistry.name("cache", "max-size"),
                new Gauge<Long>() {
                    @Override
                    public Long getValue() {
                        return 0L;
                    }
                });

        m_resourceIdSplitter = Preconditions.checkNotNull(resourceIdSplitter, "resourceIdSplitter argument");
    }

    @Override
    public void merge(Context context, Resource resource, ResourceMetadata metadata) {
        final Optional<ResourceMetadata> o = get(context, resource);

        if (!o.isPresent()) {
            final ResourceMetadata newMetadata = new ResourceMetadata(m_metricReqs, m_attributeReqs, m_metricMisses, m_attributeMisses);
            newMetadata.merge(metadata);
            final Transaction t = m_jedis.multi();
            final byte[] key = key(METADATA_PREFIX, context.getId(), resource.getId());
            t.set(key, conf.asByteArray(newMetadata));

            // Index the key, element by element, in order to support calls to getResourceIdsWithPrefix()
            final List<String> elements = Lists.newArrayList(SEARCH_PREFIX, context.getId());
            for (String el : m_resourceIdSplitter.splitIdIntoElements(resource.getId())) {
                elements.add(el);
                t.lpush(m_resourceIdSplitter.joinElementsToId(elements).getBytes(), key);
            }

            // Update the keys in transaction
            t.exec();
        } else if (o.get().merge(metadata)) {
            // Update the value stored in the cache if it was changed as a result of the merge
            m_jedis.set(key(METADATA_PREFIX, context.getId(), resource.getId()), conf.asByteArray(metadata));
        }
    }

    @Override
    public Optional<ResourceMetadata> get(Context context, Resource resource) {
        final byte[] bytes = m_jedis.get(key(METADATA_PREFIX, context.getId(), resource.getId()));
        return (bytes != null) ? Optional.of((ResourceMetadata)conf.asObject(bytes)): Optional.absent();
    }

    @Override
    public void delete(final Context context, final Resource resource) {
        m_jedis.del(key(METADATA_PREFIX, context.getId(), resource.getId()));
    }

    @Override
    public List<String> getResourceIdsWithPrefix(Context context, String resourceIdPrefix) {
        final List<String> elements = Lists.newArrayList(SEARCH_PREFIX, context.getId());
        elements.addAll(m_resourceIdSplitter.splitIdIntoElements(resourceIdPrefix));
        return m_jedis.lrange(m_resourceIdSplitter.joinElementsToId(elements).getBytes(), 0, -1).stream()
                .map(bytes -> resourceId(METADATA_PREFIX, context.getId(), bytes))
                .collect(Collectors.toList()); 
    }

    /**
     * Creates a unique key for the (prefix, contextId, resourceId) tuple
     */
    private static byte[] key(String prefix, String contextId, String resourceId) {
        return m_keyJoiner.join(prefix, contextId, resourceId).getBytes();
    }

    /**
     * Extracts the resource id from a key produced by {@link #key(String, String, String)}
     */
    private String resourceId(String prefix, String contextId, byte[] key) {
        return new String(key).substring(prefix.length() + contextId.length() + 2);
    }
}
