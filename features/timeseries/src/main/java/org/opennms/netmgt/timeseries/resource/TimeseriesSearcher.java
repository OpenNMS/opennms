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

import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.WILDCARD_INDEX;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.WILDCARD_INDEX_NO;
import static org.opennms.netmgt.timeseries.util.TimeseriesUtils.toResourceId;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheConfig;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.TagMatcher;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTagMatcher;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheLoader;


public class TimeseriesSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesSearcher.class);

    private final TimeseriesStorageManager timeseriesStorageManager;

    private final TimeSeriesMetaDataDao metaDataDao;

    private final Cache<Tag, Set<Metric>> indexMetricsByTag;
    private final Cache<TagMatcher, Set<Metric>> indexMetricsByTagMatcher;

    @Autowired
    public TimeseriesSearcher(TimeseriesStorageManager timeseriesStorageManager,
                              TimeSeriesMetaDataDao metaDataDao, @Named("timeseriesSearcherCache") final CacheConfig cacheConfig) {
        this.timeseriesStorageManager = Objects.requireNonNull(timeseriesStorageManager, "timeseriesStorageManager must not be null");
        this.metaDataDao = Objects.requireNonNull(metaDataDao, "metaDataDao must not be null");
        indexMetricsByTag = new org.opennms.core.cache.CacheBuilder<>()
                .withConfig(cacheConfig)
                .withCacheLoader(new MetricCacheLoader(timeseriesStorageManager))
                .build();

        CacheConfig cacheConfig2 = new CacheConfig("timeseriesSearcherCache2");
        cacheConfig2.setEnabled(true);
        cacheConfig2.setExpireAfterRead(cacheConfig.getExpireAfterRead());
        cacheConfig2.setExpireAfterWrite(cacheConfig.getExpireAfterWrite());
        cacheConfig2.setMaximumSize(cacheConfig.getMaximumSize());
        cacheConfig2.setMetricRegistry(cacheConfig.getMetricRegistry());
        indexMetricsByTagMatcher = new org.opennms.core.cache.CacheBuilder<>()
                .withConfig(cacheConfig2) // TODO: Patrick
                .withCacheLoader(new MetricCacheLoader2(timeseriesStorageManager))
                .build();
    }

    /**
     * Gets all metrics that reside under the given path
     */
    private Set<Metric> getMetricsBelowWildcardPathOld(final String wildcardPath) throws StorageException {
        String wildcardValue = String.format("(%s,*)", wildcardPath);
        Tag wildCardTag = new ImmutableTag(WILDCARD_INDEX, wildcardValue);
        return getMetricFromCacheOrLoad(wildCardTag);
    }

    /**
     * Gets all metrics that reside under the given path
     */
    private Set<Metric> getMetricsBelowWildcardPathNew(final String wildcardPath) throws StorageException {
        TagMatcher tagMatcher = ImmutableTagMatcher.builder()
                .type(TagMatcher.Type.EQUALS_REGEX)
                .key(IntrinsicTagNames.resourceId)
                .value(wildcardPath + ":.*$")
                .build();
        return getMetricFromCacheOrLoad(tagMatcher);
    }

    public Map<String, String> getResourceAttributes(ResourcePath path) {
        try {
            return metaDataDao.getForResourcePath(path);
        } catch (StorageException e) {
            LOG.warn("can not retrieve meta data for path: {}", path, e);
        }
        return new HashMap<>();
    }

    public Set<Metric> search(ResourcePath path, int depth) throws StorageException {
        Set<Metric> oldSearch = searchOld(path, depth);
        Set<Metric> newSearch = searchNew(path, depth);
        if(!oldSearch.equals(newSearch)) {
            LOG.info("elles isch he!");
        }
        return newSearch;
    }

    // TODO: Patrick: remove when not needed anymore
    public Set<Metric> searchOld(ResourcePath path, int depth) throws StorageException {

        // Numeric suffix for the index name, based on the length of parent path
        int idxSuffix = path.elements().length - 1;
        // The length of the resource ids we're interested in finding
        int targetLen = idxSuffix + depth + 2;

        String key = "_idx" + idxSuffix;
        String value = String.format("(%s,%d)", toResourceId(path), targetLen);
        Tag indexTag = new ImmutableTag(key, value);

        Set<Metric> metrics = indexMetricsByTag.getIfCached(indexTag);

        // cache found => we are done
        if (metrics != null) {
            return metrics;
        }

        // in order not to call the TimeseriesStorage implementation for every resource, we query all resources below a certain
        // depth (defined as WILDCARD_INDEX_NO). We have a special index, the "wildcard" index for that.
        if (path.elements().length >= WILDCARD_INDEX_NO) {
            String wildcardPath = toResourceId(ResourcePath.get(Arrays.asList(path.elements()).subList(0, WILDCARD_INDEX_NO)));
            Set<Metric> metricsFromWildcard = getMetricsBelowWildcardPathOld(wildcardPath);
            for (Metric metric : metricsFromWildcard) {
                metric.getMetaTags().stream()
                        .filter(tag -> tag.getKey() != null)
                        .filter(tag -> tag.getKey().startsWith("_idx"))
                        .filter(tag -> !tag.getKey().equals(WILDCARD_INDEX))
                        .forEach(tag -> getMetricsFromCacheOrAddEmptySet(tag).add(metric));
            }
            // Either we have found it by now or it doesn't exist => add empty list
            metrics = getMetricsFromCacheOrAddEmptySet(indexTag);

        } else {
            // we are above the wildcard level -> let's just get metrics that are associated with the index tag
            metrics = getMetricFromCacheOrLoad(indexTag);
        }
        return metrics;
    }

    public Set<Metric> searchNew(ResourcePath path, int depth) throws StorageException {
        if (depth < 0) {
            throw new IllegalArgumentException("depth cannot be negative.");
        }
        TagMatcher indexMatcher = ImmutableTagMatcher.builder()
                .type(TagMatcher.Type.EQUALS_REGEX)
                .key(IntrinsicTagNames.resourceId)
                .value(pathToRegex(path, depth + 1))
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
            Set<Metric> metricsFromWildcard = getMetricsBelowWildcardPathNew(wildcardPath);

            for (Metric metric : metricsFromWildcard) {
                ResourcePath pathOfMetric = ResourcePath.fromString(metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue().replace(':', '/'));
                ResourcePath currentPath = pathOfMetric;
                while (true) {
                    TagMatcher matcher = ImmutableTagMatcher.builder()
                            .type(TagMatcher.Type.EQUALS_REGEX)
                            .key(IntrinsicTagNames.resourceId)
                            .value(pathToRegex(currentPath, pathOfMetric.elements().length - currentPath.elements().length))
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
            // we are above the wildcard level -> let's just get metrics that are associated with the index tag
            metrics = getMetricFromCacheOrLoad(indexMatcher);
        }
        return metrics;
    }
    String pathToRegex(ResourcePath path, int depth) {
        return toResourceId(path) +
                ":[^.:]*".repeat(depth) +
                "$";
    }

    private Set<Metric> getMetricFromCacheOrLoad(Tag tag) throws StorageException {
        try {
            return indexMetricsByTag.get(tag);
        } catch (ExecutionException e) {
            throw new StorageException(e);
        }
    }

    private Set<Metric> getMetricFromCacheOrLoad(TagMatcher matcher) throws StorageException {
        try {
            return indexMetricsByTagMatcher.get(matcher);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    private Set<Metric> getMetricsFromCacheOrAddEmptySet(Tag tag) {
        try {
            return indexMetricsByTag.get(tag, ConcurrentHashMap::newKeySet);
        } catch (ExecutionException e) {
            // should never happen
            throw new RuntimeException("Error creating ConcurrentHashMap.newKeySet()", e);
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

    private final static class MetricCacheLoader extends CacheLoader<Tag, Set<Metric>> {

        private TimeseriesStorageManager timeseriesStorageManager;

        public MetricCacheLoader(TimeseriesStorageManager timeseriesStorageManager) {
            this.timeseriesStorageManager = timeseriesStorageManager;
        }

        @Override
        public Set<Metric> load(final Tag tag) throws Exception {
            TagMatcher matcher = ImmutableTagMatcher.TagMatcherBuilder.of(tag).build();
            List<Metric> metricList = timeseriesStorageManager.get().findMetrics(Collections.singletonList(matcher));
            Set<Metric> metrics = ConcurrentHashMap.newKeySet();
            metrics.addAll(metricList);
            return metrics;
        }
    }

    private final static class MetricCacheLoader2 extends CacheLoader<TagMatcher, Set<Metric>> {

        private TimeseriesStorageManager timeseriesStorageManager;

        public MetricCacheLoader2(TimeseriesStorageManager timeseriesStorageManager) {
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
