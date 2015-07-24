package org.opennms.netmgt.newts.support;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A copy of {@link org.opennms.newts.cassandra.search.GuavaResourceMetadataCache} with support 
 * for searching resources within the cache.
 *
 * @author jwhite
 */
public class GuavaSearchableResourceMetadataCache implements SearchableResourceMetadataCache {

    private static final Logger LOG = LoggerFactory.getLogger(GuavaSearchableResourceMetadataCache.class);

    private static final Joiner m_keyJoiner = Joiner.on(':');

    private final Cache<String, ResourceMetadata> m_cache;
    private final Meter m_metricReqs;
    private final Meter m_attributeReqs;
    private final Meter m_metricMisses;
    private final Meter m_attributeMisses;

    @Inject
    public GuavaSearchableResourceMetadataCache(@Named("search.resourceMetadata.maxCacheEntries") long maxSize, MetricRegistry registry) {
        LOG.info("Initializing resource metadata cache ({} max entries)", maxSize);
        m_cache = CacheBuilder.newBuilder().maximumSize(maxSize).build();

        m_metricReqs = registry.meter(name(getClass(), "metric-reqs"));
        m_metricMisses = registry.meter(name(getClass(), "metric-misses"));
        m_attributeReqs = registry.meter(name(getClass(), "attribute-reqs"));
        m_attributeMisses = registry.meter(name(getClass(), "attribute-misses"));
    }

    @Override
    public Optional<ResourceMetadata> get(Context context, Resource resourceId) {
        ResourceMetadata r = m_cache.getIfPresent(key(context, resourceId));
        return (r != null) ? Optional.of(r) : Optional.<ResourceMetadata>absent();
    }

    private String key(Context context, Resource resource) {
        return m_keyJoiner.join(context.getId(), resource.getId());
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
            m_cache.put(key(context, resource), newMetadata);
            return;
        }

        o.get().merge(metadata);

    }

    @Override
    public List<String> getEntriesWithPrefix(Context context, String resourceIdPrefix) {
        String prefix = m_keyJoiner.join(context.getId(), resourceIdPrefix);
        return m_cache.asMap().keySet().stream()
            .filter(key -> key.startsWith(prefix))
            .map(key -> resourceId(context, key))
            .collect(Collectors.toList());
    }

}
