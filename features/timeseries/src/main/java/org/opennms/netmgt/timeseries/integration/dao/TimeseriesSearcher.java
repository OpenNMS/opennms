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
import java.util.List;
import java.util.Map;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.impl.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.opennms.newts.api.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;

public class TimeseriesSearcher {

    private static final Logger LOG = LoggerFactory.getLogger(TimeseriesSearcher.class);

    @Autowired
    private TimeseriesStorageManager timeseriesStorageManager;

    @Autowired
    private TimeSeriesMetaDataDao metaDataDao;

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
        Tag indexTag = new ImmutableTag(key, value);

        List<Metric> metrics = timeseriesStorageManager.get().getMetrics(Collections.singletonList(indexTag));

        Map<String, SearchResults.Result> resultPerResources = new HashMap<>();


        for(Metric metric : metrics) {
            String resourceId = metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue();
            SearchResults.Result result = resultPerResources.get(resourceId);
            if(result == null) {
                Map<String, String> attributes = new HashMap<>();
                metric.getMetaTags().forEach(entry -> attributes.put(entry.getKey(), entry.getValue()));
                Resource resource = new Resource(metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue(),
                        Optional.of(attributes));
                result = new SearchResults.Result(resource, new ArrayList<>());
                resultPerResources.put(resourceId, result);
            }
            result.getMetrics().add(metric.getFirstTagByKey(IntrinsicTagNames.name).getValue());
        }

        SearchResults results = new SearchResults();
        resultPerResources.values().forEach(results::addResult);
        return results;
    }
}
