/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
