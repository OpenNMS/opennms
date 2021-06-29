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


/**
 * Utility functions and constants.
 *
 * @author jwhite
 */
public final class TimeseriesUtils {

    public static final String PREFIX_EXTERNAL_TAG =  "_ext_";

    public static final int WILDCARD_INDEX_NO = 2; // => node level

    /**
     * Converts a {@link org.opennms.netmgt.model.ResourcePath} to a String.
     * The elements are separated by a slash ('/').
     * No escaping is done.
     * Reverse function to toResourcePath()
     */
    public static String toResourceId(ResourcePath path) {
        return String.join("/", path.elements());
    }

    /**
     * Converts a String to a {@link org.opennms.netmgt.model.ResourcePath}.
     * The last element is treated as the resource name and not returned.
     * @param resourceId String with elements separated by a slash ('/').
     */
    public static ResourcePath toResourcePath(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        List<String> els = Arrays.asList(resourceId.split("/"));
        return ResourcePath.get(els.subList(0, els.size() - 1));
    }

    /**
     * Extracts the metric name from the resource id.
     * The last path element is used as the name.
     */
    public static String toMetricName(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        List<String> els = Arrays.asList(resourceId.split("/"));
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

    public static String toSearchRegex(ResourcePath path, int depth) {
        // we have the guarantee that no forward slash (/) is part of the elements of a ResourcePath
        return "^" + // start string
               toResourceId(path) + // exact match
               "/[^./]*".repeat(depth) + // slash (/) plus any chars except slash (/), repeated 'depth' times
                "$"; // end of String
    }

}
