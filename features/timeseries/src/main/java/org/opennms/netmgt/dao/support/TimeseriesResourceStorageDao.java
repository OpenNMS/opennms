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

import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toMetricName;
import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourceId;
import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourcePath;

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
import org.opennms.netmgt.dao.support.SearchResults.Result;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.timeseries.integration.TimeseriesWriter;
import org.opennms.netmgt.timeseries.integration.support.SearchableResourceMetadataCache;
import org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.integration.CommonTagNames;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.Setter;

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
public class TimeseriesResourceStorageDao implements ResourceStorageDao {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesResourceStorageDao.class);

    @Autowired
    private TimeSeriesStorage storage;

    @Autowired
    private Context m_context;

    @Autowired
    @Setter
    private TimeseriesSearcher searcher;

    @Autowired
    private TimeseriesWriter writer;

    @Autowired
    private SearchableResourceMetadataCache searchableCache;

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
            for(String metricName: result.getMetrics()) {
                Metric metric = Metric.builder()
                        .tag(CommonTagNames.resourceId, result.getResource().getId())
                        .tag(CommonTagNames.name, metricName)
                        .tag(Metric.MandatoryTag.mtype.name(), Metric.Mtype.gauge.name()) // TODO Patrick: where do we get the type from?
                        .tag(Metric.MandatoryTag.unit.name(), "ms") // TODO Patrick: where do we get the unit from?
                        .build();
                try {
                    storage.delete(metric);
                } catch (StorageException e) {
                    LOG.error("Could not delete {}, will ignore problem and continue ", metric, e); // TODO Patrick: is this the expected behaviour?
                }
            }

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
                    LOG.warn("Encountered non-child resource {} when searching for {} with depth {}. " + // TODO Patrick: breakpoint
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
        Sample sample = TimeseriesUtils.createSampleForIndexingStrings(m_context, resource);

        // Index, but do not insert the sample(s)
        // The key/value pair specified in the attributes map will be merged with the others.
        writer.index(Lists.newArrayList(sample));
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
        return searcher.getResourceAttributes(path);
    }

    private Callable<Map<String, String>> getResourceAttributesCallable(final ResourcePath path) {
        return new Callable<Map<String, String>>() {
            @Override
            public Map<String, String> call() {
                return searcher.getResourceAttributes(path);
            }
        };
    }

    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
        // These are already stored by the indexer
    }

    private boolean hasCachedEntry(ResourcePath path, int minDepth, int maxDepth) {
        List<String> cachedResourceIds = searchableCache.getResourceIdsWithPrefix(
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
        final SearchResults results;
        try {
            results = searcher.search(path, depth, fetchMetrics);
            LOG.trace("Found {} results.", results.size());
        } catch (StorageException e) {
            // TODO Patrick
            throw new RuntimeException(e);
        }
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

    public void setSearchableCache(SearchableResourceMetadataCache searchableCache) {
        this.searchableCache = searchableCache;
    }

    public void setContext(Context context) {
        m_context = context;
    }

    public void setWriter(TimeseriesWriter writer) {
        this.writer = writer;
    }

}
