package org.opennms.netmgt.newts.support;

import java.util.List;

import org.opennms.newts.api.Context;
import org.opennms.newts.cassandra.search.ResourceMetadataCache;

/**
 * A cache with search support.
 *
 * @author jwhite
 */
public interface SearchableResourceMetadataCache extends ResourceMetadataCache {

    public List<String> getEntriesWithPrefix(Context context, String resourceIdPrefix);

}
