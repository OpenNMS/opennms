package org.opennms.netmgt.dao.support;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.cassandra.search.ResourceMetadata;

import com.google.common.base.Optional;

public class MockSearchableResourceMetadataCache implements SearchableResourceMetadataCache {
    @Override
    public void merge(Context context, Resource resource,
            ResourceMetadata rMetadata) {
        // pass
    }

    @Override
    public Optional<ResourceMetadata> get(Context context,
            Resource resource) {
        return Optional.absent();
    }

    @Override
    public List<String> getEntriesWithPrefix(Context context,
            String resourceIdPrefix) {
        return Collections.emptyList();
    }
}
