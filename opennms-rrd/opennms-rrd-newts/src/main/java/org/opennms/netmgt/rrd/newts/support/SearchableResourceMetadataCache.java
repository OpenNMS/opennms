package org.opennms.netmgt.rrd.newts.support;

import java.util.List;

import org.opennms.newts.api.Context;
import org.opennms.newts.cassandra.search.ResourceMetadataCache;

public interface SearchableResourceMetadataCache extends ResourceMetadataCache {

    public List<String> getEntriesWithPrefix(Context context, String resourceIdPrefix);

}
