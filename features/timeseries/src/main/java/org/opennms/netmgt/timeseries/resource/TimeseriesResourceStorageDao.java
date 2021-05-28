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

package org.opennms.netmgt.timeseries.resource;

import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.PREFIX_RESOURCE_LEVEL_ATTRIBUTE;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.USE_TS_FOR_STRING_ATTRIBUTES;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toMetricName;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourceId;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourcePath;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.samplewrite.TimeseriesWriter;
import org.opennms.netmgt.timeseries.util.TimeseriesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Resource Storage Dao implementation for Timeseries Integration Layer that leverages the Search API for walking the resource tree.
 *
 * In Timeseries Integration Layer, samples are associated with metrics, which are in turn associated with resources.
 *
 * Here we split the resource id into two parts:
 *   bucket: last element of the resource id
 *   resource path: all the elements before the bucket
 * Relating this to .rrd file on disk, the bucket would be the filename, and the resource path would be its folder.
 *
 */
public class TimeseriesResourceStorageDao implements ResourceStorageDao {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesResourceStorageDao.class);

    @Autowired
    private TimeseriesStorageManager storageManager;

    @Autowired
    private TimeseriesSearcher searcher;

    @Autowired
    private TimeseriesWriter writer;

    @Override
    public boolean exists(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        return searchFor(path, depth).size() > 0;
    }

    @Override
    public boolean existsWithin(final ResourcePath path, final int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");

        // The indices are structured in such a way that we need specify the depth
        // so here we need to iterate over all the possibilities. We could add
        // additional indices to avoid this, but it's not worth the additional
        // writes, since the specified depth should be relatively small.
        return IntStream.rangeClosed(0, depth)
            .anyMatch(i -> searchFor(path, i).size() > 0);
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        Preconditions.checkArgument(depth >= 0, "depth must be non-negative");
        Set<ResourcePath> matches = Sets.newTreeSet();

        Set<Metric> metrics = searchFor(path, depth);
        for (Metric metric : metrics) {
            // Relativize the path
            ResourcePath child = toChildResourcePath(path, metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue());
            if (child == null) {
                // This shouldn't happen
                LOG.warn("Encountered non-child resource {} when searching for {} with depth {}. Ignoring resource.",
                        metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue(), path, depth);
                continue;
            }
            matches.add(child);
        }
        return matches;
    }

    @Override
    public boolean delete(ResourcePath path) {
        final Set<Metric> results = searchFor(path, 0);

        if (results.isEmpty()) {
            return false;
        }

        for (final Metric metric : results) {
                try {
                    storageManager.get().delete(metric);
                } catch (StorageException e) {
                    LOG.error("Could not delete {}, will ignore problem and continue ", metric, e);
                }
        }

        return true;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        Set<OnmsAttribute> attributes = Sets.newHashSet();

        Future<Map<String, String>> stringAttributes;
        if(!USE_TS_FOR_STRING_ATTRIBUTES) {
            // Fetch the resource-level attributes in parallel
            stringAttributes = ForkJoinPool.commonPool()
                    .submit(getResourceAttributesCallable(path));
        }

        // Gather the list of metrics available under the resource path
        Set<Metric> metrics = searchFor(path, 0);
        for (Metric metric : metrics) {
            final String resourceId = metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue();
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
                            "Ignoring resource.", resourceId, path, 0);
                    continue;
                }
            }

            if (ResourceTypeUtils.isResponseTime(resourceId)) {
                // Use the last part of the resource id as the dsName
                // Store the resource id in the rrdFile field
                attributes.add(new RrdGraphAttribute(toMetricName(resourceId), "", resourceId));
            } else {
                // Use the metric name as the dsName
                // Store the resource id in the rrdFile field
                attributes.add(new RrdGraphAttribute(metric.getFirstTagByKey(IntrinsicTagNames.name).getValue(), "", resourceId));
            }
        }

        // Add the resource level attributes to the result set
        try {
            if(USE_TS_FOR_STRING_ATTRIBUTES) {
                Set<Metric> metricsWithStringAttributes = new HashSet<>(metrics);
                metricsWithStringAttributes.addAll(searchFor(path, -1));
                if(!metricsWithStringAttributes.isEmpty()) {
                    metricsWithStringAttributes.iterator().next()
                            .getMetaTags().stream()
                            .filter(t -> t.getKey() !=null && t.getKey().startsWith(PREFIX_RESOURCE_LEVEL_ATTRIBUTE))
                            .map(t -> new StringPropertyAttribute(t.getKey().substring(PREFIX_RESOURCE_LEVEL_ATTRIBUTE.length()), t.getValue()))
                            .forEach(attributes::add);
                }
            } else {
                stringAttributes.get().entrySet().stream()
                        .map(e -> new StringPropertyAttribute(e.getKey(), e.getValue()))
                        .forEach(attributes::add);
            }

        } catch (InterruptedException|ExecutionException e) {
            throw Throwables.propagate(e);
        }

        return attributes;
    }

    @Override
    // TODO: Patrick: We can remove this method from interface and force clients to use KV store?
    public void setStringAttribute(ResourcePath path, String key, String value) {
        // Create a mock sample referencing the resource. This is a bit of a miss use of the Sample class but it allows
        // us to use the ring buffer
        Map<String, String> attributes = new ImmutableMap.Builder<String, String>()
                .put(key, value)
                .build();
        Sample sample = TimeseriesUtils.createSampleForIndexingStrings(toResourceId(path), attributes);

        // Index, but do not insert the sample(s)
        // The key/value pair specified in the attributes map will be merged with the others.
        writer.index(Lists.newArrayList(sample));
    }

    @Override
    // TODO: Patrick: We can remove this method from interface and force clients to use KV store?
    public String getStringAttribute(ResourcePath path, String key) {
        return getStringAttributes(path).get(key);
    }

    @Override
    // TODO: Patrick: We can remove this method from interface and force clients to use KV store?
    public Map<String, String> getStringAttributes(ResourcePath path) {
        return getMetaData(path);
    }

    @Override
    // TODO: Patrick: We can remove this method from interface and force clients to use KV store?
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

    private Set<Metric> searchFor(ResourcePath path, int depth) {
        final Set<Metric> results;
        try {
            results = searcher.search(path, depth);
            LOG.trace("Found {} results.", results.size());
        } catch (StorageException e) {
            LOG.error("An error occurred while querying for {}", path, e);
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

    public void setWriter(TimeseriesWriter writer) {
        this.writer = writer;
    }

    public void setSearcher(TimeseriesSearcher searcher) {
        this.searcher = searcher;
    }
}
