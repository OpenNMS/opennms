package org.opennms.netmgt.dao.support;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.opennms.netmgt.newts.support.NewtsUtils.toResourceId;
import static org.opennms.netmgt.newts.support.NewtsUtils.toResourcePath;
import static org.opennms.netmgt.newts.support.NewtsUtils.findResourcesWithMetricsAtDepth;

import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.opennms.newts.api.search.Query;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.api.search.SearchResults.Result;
import org.opennms.newts.cassandra.search.CassandraIndexer;
import org.opennms.newts.cassandra.search.CassandraSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Resource Storage Dao implementation for Newts that leverages the Search API for walking the resource tree.
 *
 * In Newts, samples are associated with metrics, which are in turn associated with resources.
 *
 * Here we split the resource id into two parts:
 *   bucket: last element of the resource id
 *   resource path: all the elements before the bucket
 * Relating this to .rrd file on disk, the bucket would be the filename, and the resource path would be its folder.
 *
 * @author jwhite
 */
public class NewtsResourceStorageDao implements ResourceStorageDao {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsResourceStorageDao.class);

    @Autowired
    private Context m_context;

    @Autowired
    private CassandraSearcher m_searcher;

    @Autowired
    private CassandraIndexer m_indexer;

    @Autowired
    private SearchableResourceMetadataCache m_searchableCache;

    @Override
    public boolean exists(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        if (!hasCachedEntry(path, depth, depth)) {
            return searchFor(path, depth, false).size() > 0;
        } else {
            return true;
        }
    }

    @Override
    public boolean existsWithin(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        if (!hasCachedEntry(path, 0, depth)) {
            for (int i = 0; i <= depth; i++) {
                if (searchFor(path, i, false).size() > 0) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        Set<ResourcePath> matches = Sets.newTreeSet();

        SearchResults results = searchFor(path, depth, false);
        for (Result res : results) {
            ResourcePath child = toChildResourcePath(path, res.getResource().getId());
            if (child == null) {
                continue;
            }
            matches.add(child);
        }

        return matches;
    }

    @Override
    public boolean delete(ResourcePath path) {
        return false;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        Set<OnmsAttribute> attributes = Sets.newHashSet();

        // Gather the list of metrics available under the resource path
        SearchResults results = searchFor(path, 0, true);
        for (Result result : results) {
            final String resourceId = result.getResource().getId();
            final ResourcePath resultPath = toResourcePath(resourceId);
            if (!path.equals(resultPath)) {
                continue;
            }

            for (String metric : result.getMetrics()) {
                // Use the metric name as the dsName
                // Store the resource id in the rrdFile field
                attributes.add(new RrdGraphAttribute(metric, "", resourceId));
            }
        }

        // Gather resource level attributes (equivalent to reading values from strings.properties)
        getStringAttributes(path).entrySet().stream()
            .map(e -> new StringPropertyAttribute(e.getKey(), e.getValue()))
            .forEach(attr -> attributes.add(attr));

        return attributes;
    }

    @Override
    public void setStringAttribute(ResourcePath path, String key, String value) {
        // Create a mock sample referencing the resource
        Map<String, String> attributes = new ImmutableMap.Builder<String, String>()
                .put(key, value)
                .build();
        Resource resource = new Resource(toResourceId(path), Optional.of(attributes));
        Sample sample = new Sample(Timestamp.fromEpochMillis(0), m_context, resource, "strings",
                MetricType.GAUGE, ValueType.compose(0, MetricType.GAUGE));

        // Leverage the existing interface provided by the indexer to persist the attributes.
        m_indexer.update(Lists.newArrayList(sample));
    }

    @Override
    public String getStringAttribute(ResourcePath path, String key) {
        return getStringAttributes(path).get(key);
    }

    @Override
    public Map<String, String> getStringAttributes(ResourcePath path) {
        return m_searcher.getResourceAttributes(m_context, toResourceId(path));
    }

    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
        // pass
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return m_searcher.getResourceAttributes(m_context, toResourceId(path));
    }

    private boolean hasCachedEntry(ResourcePath path, int minDepth, int maxDepth) {
        List<String> resourceIds = m_searchableCache.getEntriesWithPrefix(
                Context.DEFAULT_CONTEXT, toResourceId(path));
        for (String resourceId : resourceIds) {
            int relativeDepth = path.relativeDepth(toResourcePath(resourceId));
            if (relativeDepth < 0) {
                continue;
            }
            if (relativeDepth >= minDepth && relativeDepth <= maxDepth) {
                return true;
            }
        }
        return false;
    }

    private SearchResults searchFor(ResourcePath path, int depth, boolean fetchMetrics) {
        final Query q = findResourcesWithMetricsAtDepth(path, depth);
        LOG.trace("Searching for '{}'.", q);
        final SearchResults results = m_searcher.search(m_context, q, fetchMetrics);
        LOG.trace("Found {} results.", results.size());
        return results;
    }

    protected static ResourcePath toChildResourcePath(ResourcePath parent, String resourceId) {
        final ResourcePath child = toResourcePath(resourceId);
        final String childEls[] = child.elements();
        final String parentEls[] = parent.elements();

        if (childEls.length <= parentEls.length) {
            return null;
        }

        String els[] = new String[parentEls.length + 1];
        for (int i = 0; i <= parentEls.length ; i++) {
            els[i] = childEls[i];
        }

        return ResourcePath.get(els);
    }

    @VisibleForTesting
    protected void setSearchableCache(SearchableResourceMetadataCache searchableCache) {
        m_searchableCache = searchableCache;
    }

    @VisibleForTesting
    protected void setSearcher(CassandraSearcher searcher) {
        m_searcher = searcher;
    }

    @VisibleForTesting
    protected void setContext(Context context) {
        m_context = context;
    }

}
