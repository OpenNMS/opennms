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

package org.opennms.netmgt.timeseries.integration.dao;

import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourceId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.integration.CommonTagNames;
import org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.Tag;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.opennms.newts.api.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TimeseriesSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesSearcher.class);

    @Autowired
    private TimeSeriesStorage timeSeriesStorage;

    @Autowired
    private TimeSeriesMetaDataDao metaDataDao;

    private LoadingCache<String, Set<ResourcePath>> allResources = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, Set<ResourcePath>>() {
                        public Set<ResourcePath> load(String key) throws StorageException {
                            return getAllResources();
                        }
                    });

    private Set<ResourcePath> getAllResources() throws StorageException {

        List<Metric> metrics = timeSeriesStorage.getMetrics(new ArrayList<>());
        Set<ResourcePath> resources = new HashSet<>();
        for (Metric metric : metrics){
            String resourceString = metric.getFirstTagByKey("resourceId").getValue();
            ResourcePath resource = new ResourcePath(TimeseriesUtils.toResourcePath(resourceString), TimeseriesUtils.toMetricName(resourceString));
            addIncludingParent(resources, resource);
        }
        return  resources;
    }
    public void addIncludingParent(Set<ResourcePath> allPaths, ResourcePath newPath) {
        ResourcePath currentPath = newPath;
        allPaths.add(currentPath);
    }

    public Map<String, String> getResourceAttributes(ResourcePath path) {
        try {
            return metaDataDao.getForResourcePath(path);
        } catch (StorageException e) {
            LOG.warn("can not retrieve meta data for path: {}", path, e);
        }
        return new HashMap<>();
    }

    public SearchResults search(ResourcePath path, int depth, boolean fetchMetrics) throws StorageException {

        // Numeric suffix for the index name, based on the length of parent path
        int idxSuffix = path.elements().length - 1;
        // The length of the resource ids we're interested in finding
        int targetLen = idxSuffix + depth + 2;

        String key = "_idx"+idxSuffix;
        String value = String.format("(%s,%d)", toResourceId(path), targetLen);
        Tag indexTag = new Tag(key, value);

        List<Metric> metrics = timeSeriesStorage.getMetrics(Collections.singletonList(indexTag));

        Map<String, SearchResults.Result> resultPerResources = new HashMap<>();


        for(Metric metric : metrics) {
            String resourceId = metric.getFirstTagByKey(CommonTagNames.resourceId).getValue();
            SearchResults.Result result = resultPerResources.get(resourceId);
            if(result == null) {
                Map<String, String> attributes = new HashMap<>();
                metric.getMetaTags().forEach(entry -> attributes.put(entry.getKey(), entry.getValue()));
                Resource resource = new Resource(metric.getFirstTagByKey(CommonTagNames.resourceId).getValue(),
                        Optional.of(attributes));
                result = new SearchResults.Result(resource, new ArrayList<>());
                resultPerResources.put(resourceId, result);
            }
            result.getMetrics().add(metric.getFirstTagByKey(CommonTagNames.name).getValue());
        }

        SearchResults results = new SearchResults();
        resultPerResources.values().forEach(results::addResult);
        return results;
    }
}
