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
package org.opennms.netmgt.measurements.api;

import java.util.List;

import org.opennms.netmgt.measurements.model.Source;

/**
 * Used to retrieve measurements from the current persistence mechanism.
 *
 * @author Jesse White <jesse@opennms.org>
 */
public interface MeasurementFetchStrategy {

    /**
     * Fetches the measurements for the given sources.
     *
     * @param start      timestamp in milliseconds
     * @param end        timestamp in milliseconds
     * @param step       desired resolution in milliseconds - actual resolution might differ
     * @param maxrows    maximum number of rows - no limit when <= 0
     * @param interval   duration in milliseconds, used by strategies that implement late aggregation
     * @param heartbeat  duration in milliseconds, used by strategies that implement late aggregation 
     * @param sources    array of sources - these should have unique labels
     * @param relaxed    if <code>false</code> a missing source results in a return of <code>null</code>.
     *                   <code>true</code> on the other hand ignores that source.
     * @return           null when a resource id or attribute cannot be found
     * @throws Exception
     */
    public FetchResults fetch(long start, long end, long step, int maxrows,
                              Long interval, Long heartbeat,
                              List<Source> sources, boolean relaxed) throws Exception;
}
