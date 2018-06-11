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

package org.opennms.netmgt.dao.support;

import static org.opennms.netmgt.newts.support.NewtsUtils.findResourcesWithMetricsAtDepth;
import static org.opennms.netmgt.newts.support.NewtsUtils.toMetricName;
import static org.opennms.netmgt.newts.support.NewtsUtils.toResourceId;
import static org.opennms.netmgt.newts.support.NewtsUtils.toResourcePath;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.newts.NewtsWriter;
import org.opennms.netmgt.newts.support.NewtsUtils;
import org.opennms.netmgt.newts.support.SearchableResourceMetadataCache;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.search.Query;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.api.search.SearchResults.Result;
import org.opennms.newts.cassandra.search.CassandraIndexer;
import org.opennms.newts.cassandra.search.CassandraSearcher;
import org.opennms.newts.persistence.cassandra.CassandraSampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
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
    private CassandraSampleRepository m_sampleRepository;

    @Autowired
    private CassandraIndexer m_indexer;

    @Autowired
    private NewtsWriter m_newtsWriter;

    @Autowired
    private SearchableResourceMetadataCache m_searchableCache;

    @Override
    public boolean exists(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        if (hasCachedEntry(path, depth, depth)) {
            return true; // cache hit!
        }

        return searchFor(path, depth, false).size() > 0;
    }

    @Override
    public boolean existsWithin(final ResourcePath path, final int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        if (hasCachedEntry(path, 0, depth)) {
            return true; // cache hit!
        }

        // The indices are structured in such a way that we need specify the depth
        // so here we need to iterate over all the possibilities. We could add
        // additional indices to avoid this, but it's not worth the additional
        // writes, since the specified depth should be relatively small.
        return IntStream.rangeClosed(0, depth)
            .anyMatch(i -> searchFor(path, i, false).size() > 0);
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        Set<ResourcePath> matches = Sets.newTreeSet();

        SearchResults results = searchFor(path, depth, false);
        for (Result result : results) {
            // Relativize the path
            ResourcePath child = toChildResourcePath(path, result.getResource().getId());
            if (child == null) {
                // This shouldn't happen
                LOG.warn("Encountered non-child resource {} when searching for {} with depth {}. Ignoring resource.",
                        result.getResource(), path, depth);
                continue;
            }
            matches.add(child);
        }

        return matches;
    }

    @Override
    public boolean delete(ResourcePath path) {
        final SearchResults results = searchFor(path, 0, true);

        if (results.isEmpty()) {
            return false;
        }

        for (final Result result : results) {
            m_sampleRepository.delete(m_context, result.getResource());
            m_indexer.delete(m_context, result.getResource());
        }

        return true;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        Set<OnmsAttribute> attributes = Sets.newHashSet();

        // Fetch the resource-level attributes in parallel
        Future<Map<String, String>> stringAttributes = ForkJoinPool.commonPool()
                .submit(getResourceAttributesCallable(path));

        // Gather the list of metrics available under the resource path
        SearchResults results = searchFor(path, 0, true);
        for (Result result : results) {
            final String resourceId = result.getResource().getId();
            final ResourcePath resultPath = toResourcePath(resourceId);
            if (!path.equals(resultPath)) {
                // The paths don't match exactly, but it is possible that they differ only by leading/trailing whitespace
                // so we perform a closer inspection
                if (!Arrays.stream(path.elements())
                        .map(String::trim)
                        .collect(Collectors.toList())
                        .equals(Arrays.asList(resultPath.elements()))) {
                    // This shouldn't happen
                    LOG.warn("Encountered non-child resource {} when searching for {} with depth {}. " +
                                    "Ignoring resource.", result.getResource(), path, 0);
                    continue;
                }
            }

            if (ResourceTypeUtils.isResponseTime(resourceId)) {
                // Use the last part of the resource id as the dsName
                // Store the resource id in the rrdFile field
                attributes.add(new RrdGraphAttribute(toMetricName(resourceId), "", resourceId));
            } else {
                for (String metric : result.getMetrics()) {
                    // Use the metric name as the dsName
                    // Store the resource id in the rrdFile field
                    attributes.add(new RrdGraphAttribute(metric, "", resourceId));
                }
            }
        }

        // Add the resource level attributes to the result set
        try {
            stringAttributes.get().entrySet().stream()
                .map(e -> new StringPropertyAttribute(e.getKey(), e.getValue()))
                .forEach(attributes::add);
        } catch (InterruptedException|ExecutionException e) {
            throw Throwables.propagate(e);
        }

        return attributes;
    }

    @Override
    public void setStringAttribute(ResourcePath path, String key, String value) {
        // Create a mock sample referencing the resource
        Map<String, String> attributes = new ImmutableMap.Builder<String, String>()
                .put(key, value)
                .build();
        Resource resource = new Resource(toResourceId(path), Optional.of(attributes));
        Sample sample = NewtsUtils.createSampleForIndexingStrings(m_context, resource);

        // Index, but do not insert the sample(s)
        // The key/value pair specified in the attributes map will be merged with the others.
        m_newtsWriter.index(Lists.newArrayList(sample));
    }

    @Override
    public String getStringAttribute(ResourcePath path, String key) {
        return getStringAttributes(path).get(key);
    }

    @Override
    public Map<String, String> getStringAttributes(ResourcePath path) {
        return getMetaData(path);
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return m_searcher.getResourceAttributes(m_context, toResourceId(path));
    }

    private Callable<Map<String, String>> getResourceAttributesCallable(final ResourcePath path) {
        return new Callable<Map<String, String>>() {
            @Override
            public Map<String, String> call() throws Exception {
                return m_searcher.getResourceAttributes(m_context, toResourceId(path));
            }
        };
    }

    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
        // These are already stored by the indexer
    }

    private boolean hasCachedEntry(ResourcePath path, int minDepth, int maxDepth) {
        List<String> cachedResourceIds = m_searchableCache.getResourceIdsWithPrefix(
                m_context, toResourceId(path));
        for (String resourceId : cachedResourceIds) {
            int relativeDepth = path.relativeDepth(toResourcePath(resourceId));
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
