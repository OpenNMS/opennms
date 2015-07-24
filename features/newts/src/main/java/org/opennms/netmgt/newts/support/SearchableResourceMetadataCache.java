package org.opennms.netmgt.newts.support;

import java.util.List;

import org.opennms.newts.api.Context;
import org.opennms.newts.cassandra.search.ResourceMetadataCache;

/**
 * A cache that supports searching by resource id prefix.
 *
 * @author jwhite
 */
public interface SearchableResourceMetadataCache extends ResourceMetadataCache {

    public List<String> getResourceIdsWithPrefix(Context context, String resourceIdPrefix);

}
