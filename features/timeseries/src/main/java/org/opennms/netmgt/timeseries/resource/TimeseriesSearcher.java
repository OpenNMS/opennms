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

import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.WILDCARD_INDEX_NO;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourceId;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toSearchRegex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.inject.Named;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheConfig;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TagMatcher;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTagMatcher;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheLoader;


public class TimeseriesSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesSearcher.class);

    private final TimeseriesStorageManager timeseriesStorageManager;

    private final Cache<TagMatcher, Set<Metric>> indexMetricsByTagMatcher;

    @Autowired
    public TimeseriesSearcher(TimeseriesStorageManager timeseriesStorageManager,
                              @Named("timeseriesSearcherCache") final CacheConfig cacheConfig) {
        this.timeseriesStorageManager = Objects.requireNonNull(timeseriesStorageManager, "timeseriesStorageManager must not be null");
        indexMetricsByTagMatcher = new org.opennms.core.cache.CacheBuilder<>()
                .withConfig(cacheConfig)
                .withCacheLoader(new MetricCacheLoader(timeseriesStorageManager))
                .build();
    }

    /**
     * Gets all metrics that reside under the given path
     */
    private Set<Metric> getMetricsBelowWildcardPath(final String wildcardPath) throws StorageException {
        TagMatcher tagMatcher = ImmutableTagMatcher.builder()
                .type(TagMatcher.Type.EQUALS_REGEX)
                .key(IntrinsicTagNames.resourceId)
                .value(wildcardPath + "/.*$")
                .build();
        return getMetricFromCacheOrLoad(tagMatcher);
    }

    public Set<Metric> search(ResourcePath path, int depth) throws StorageException {
        TagMatcher indexMatcher = ImmutableTagMatcher.builder()
                .type(TagMatcher.Type.EQUALS_REGEX)
                .key(IntrinsicTagNames.resourceId)
                .value(toSearchRegex(path, depth + 1))
                .build();
        Set<Metric> metrics = indexMetricsByTagMatcher.getIfCached(indexMatcher);

        // found in cache => we are done
        if (metrics != null) {
            return metrics;
        }

        // in order not to call the TimeseriesStorage implementation for every resource, we query all resources below a certain
        // depth (defined as WILDCARD_INDEX_NO).
        if (path.elements().length >= WILDCARD_INDEX_NO) {
            String wildcardPath = toResourceId(ResourcePath.get(Arrays.asList(path.elements()).subList(0, WILDCARD_INDEX_NO)));
            Set<Metric> metricsFromWildcard = getMetricsBelowWildcardPath(wildcardPath);

            for (Metric metric : metricsFromWildcard) {
                ResourcePath pathOfMetric = ResourcePath.fromString(metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue());
                ResourcePath currentPath = pathOfMetric;
                while (true) {
                    TagMatcher matcher = ImmutableTagMatcher.builder()
                            .type(TagMatcher.Type.EQUALS_REGEX)
                            .key(IntrinsicTagNames.resourceId)
                            .value(toSearchRegex(currentPath, pathOfMetric.elements().length - currentPath.elements().length))
                            .build();
                    getMetricsFromCacheOrAddEmptySet(matcher).add(metric);
                    if (currentPath.hasParent()) {
                        currentPath = currentPath.getParent();
                    } else {
                        break;
                    }
                }
            }

            // Either we have found it by now or it doesn't exist => add empty list
            metrics = getMetricsFromCacheOrAddEmptySet(indexMatcher);

        } else {
            // we are above the wildcard level -> let's just get metrics that are associated with the index matcher
            metrics = getMetricFromCacheOrLoad(indexMatcher);
        }
        return metrics;
    }

    private Set<Metric> getMetricFromCacheOrLoad(TagMatcher matcher) throws StorageException {
        try {
            return indexMetricsByTagMatcher.get(matcher);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    private Set<Metric> getMetricsFromCacheOrAddEmptySet(TagMatcher tagMatcher) {
        try {
            return indexMetricsByTagMatcher.get(tagMatcher, ConcurrentHashMap::newKeySet);
        } catch (ExecutionException e) {
            // should never happen
            throw new RuntimeException("Error creating ConcurrentHashMap.newKeySet()", e);
        }
    }

    private final static class MetricCacheLoader extends CacheLoader<TagMatcher, Set<Metric>> {

        private TimeseriesStorageManager timeseriesStorageManager;

        public MetricCacheLoader(TimeseriesStorageManager timeseriesStorageManager) {
            this.timeseriesStorageManager = timeseriesStorageManager;
        }

        @Override
        public Set<Metric> load(final TagMatcher tagMatcher) throws Exception {
            List<Metric> metricList = timeseriesStorageManager.get().findMetrics(Collections.singletonList(tagMatcher));
            Set<Metric> metrics = ConcurrentHashMap.newKeySet();
            metrics.addAll(metricList);
            return metrics;
        }
    }
}
