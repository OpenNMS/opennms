package org.opennms.netmgt.rrd.newts.support;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.codahale.metrics.MetricRegistry;

public class GuavaSearchableResourceMetadataCacheTest {

    private MetricRegistry m_registry = new MetricRegistry();

    @Test
    public void canGetEntriesWithPrefix() {
        Context ctx = Context.DEFAULT_CONTEXT;
        GuavaSearchableResourceMetadataCache cache = new GuavaSearchableResourceMetadataCache(2048, m_registry);

        assertTrue(cache.getEntriesWithPrefix(ctx, "a").isEmpty());

        Resource resource = new Resource("a:b:c");
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        cache.merge(ctx, resource, resourceMetadata);

        assertTrue(cache.getEntriesWithPrefix(ctx, "a").contains("a:b:c"));
        assertTrue(cache.getEntriesWithPrefix(ctx, "a:b").contains("a:b:c"));
        assertTrue(cache.getEntriesWithPrefix(ctx, "a:b:c").contains("a:b:c"));
        assertTrue(cache.getEntriesWithPrefix(ctx, "a:b:c:d").isEmpty());
    }
}
