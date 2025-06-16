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
package org.opennms.netmgt.timeseries.stats;

import java.util.Collection;
import java.util.List;

import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;

/**
 * We record statistics to answer the following questions:
 * <ul>
 *     <li>What metrics series have the highest tag cardinality?
 *         What does the set tags for the top 10 look like?</li>
 *     <li>Which string properties have the most unique values?</li>
 * </ul>
 */

public interface StatisticsCollector {

    void record(Collection<Sample> samples);

    /**
     * List.get(0) => has most tags (top n)
     */
    List<Metric> getTopNMetricsWithMostTags();

    List<String> getTopNTags();
}
