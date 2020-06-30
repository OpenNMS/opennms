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

package org.opennms.netmgt.timeseries.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.resource.TimeseriesResourceStorageDao;
import org.opennms.newts.cassandra.search.EscapableResourceIdSplitter;
import org.opennms.newts.cassandra.search.ResourceIdSplitter;


/**
 * Utility functions and constants.
 *
 * @author jwhite
 */
public final class TimeseriesUtils {


    public static final int WILDCARD_INDEX_NO = 2; // => node level
    public static final String WILDCARD_INDEX = "_idx" + WILDCARD_INDEX_NO + "w";

    private static final ResourceIdSplitter s_splitter = new EscapableResourceIdSplitter();

    /**
     * Extends the attribute map with indices used by the {@link TimeseriesResourceStorageDao}.
     *
     * A resource path of the form [a, b, c, d] will be indexed with:
     * <ul>
     * <li> _idx1: (a, 4)
     * <li> _idx2: (a:b, 4)
     * <li> _idx2w=(a:b,*) // wildcard index to query for all resources under that resource
     * <li> _idx3: (a:b:c, 4)
     */
    public static void addIndicesToAttributes(ResourcePath path, Map<String, String> attributes) {
        final List<String> elements = Arrays.asList(path.elements());
        final int n = elements.size();
        for (int i = 0; i < n; i++) {
            final String id = s_splitter.joinElementsToId(elements.subList(0, i+1));
            attributes.put("_idx" + i, String.format("(%s,%d)", id, n));
        }
        if(elements.size() >= WILDCARD_INDEX_NO) {
            final String id = s_splitter.joinElementsToId(elements.subList(0, WILDCARD_INDEX_NO));
            attributes.put(String.format("_idx%sw", WILDCARD_INDEX_NO), String.format("(%s,*)", id));
        }
    }

    /**
     * Converts a {@link org.opennms.netmgt.model.ResourcePath} to a Newts resource id.
     *
     * @param path path to convert
     * @return Newts resource id
     */
    public static String toResourceId(ResourcePath path) {
        return s_splitter.joinElementsToId(Arrays.asList(path.elements()));
    }

    /**
     * Converts a Newts resource id to a {@link org.opennms.netmgt.model.ResourcePath}.
     *
     * @param resourceId Newts resource id
     * @return path
     */
    public static ResourcePath toResourcePath(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        List<String> els = s_splitter.splitIdIntoElements(resourceId);
        return ResourcePath.get(els.subList(0, els.size() - 1));
    }

    /**
     * Extracts the metric name from the resource id.
     *
     * @param resourceId Newts resource id
     * @return metric name
     */
    public static String toMetricName(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        List<String> els = s_splitter.splitIdIntoElements(resourceId);
        return els.get(els.size() - 1);
    }

    /**
     * Creates a sample used to index string attributes.
     *
     * These should only be index and not be persisted.
     */
    public static Sample createSampleForIndexingStrings(final String resourceId, Map<String, String> attributes) {
        ImmutableMetric.MetricBuilder metric = ImmutableMetric.builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, resourceId)
                .intrinsicTag(IntrinsicTagNames.name, "Not needed");
        attributes.forEach(metric::metaTag);

        return ImmutableSample.builder()
                .time(Instant.EPOCH)
                .value(0.0)
                .metric(metric.build())
                .build();
    }

}
