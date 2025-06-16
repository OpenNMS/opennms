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
package org.opennms.features.status.api;

import org.opennms.netmgt.model.OnmsSeverity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatusSummary {
    private final Map<OnmsSeverity, Long> severityMap;

    public StatusSummary(List<OnmsSeverity> severityList, long totalCount) {
        this.severityMap = severityList.stream().collect(
                Collectors.groupingBy(severity -> severity, Collectors.counting()));

        updateNormalSeverity(severityMap, totalCount);
        enrich(severityMap);
    }

    public StatusSummary(Map<OnmsSeverity, Long> severityMap, long totalCount) {
        this.severityMap = Objects.requireNonNull(severityMap);

        updateNormalSeverity(severityMap, totalCount);
        enrich(severityMap);
    }

    public Map<OnmsSeverity, Long> getSeverityMap() {
        return severityMap;
    }

    private static void updateNormalSeverity(Map<OnmsSeverity, Long> severityMap, long totalCount) {
        final long severityCount = severityMap.values().stream().mapToLong(count -> count.longValue()).sum();

        // update normal severity
        final long normalCount = totalCount - severityCount + severityMap.getOrDefault(OnmsSeverity.NORMAL, 0L);
        severityMap.put(OnmsSeverity.NORMAL, normalCount);
    }

    // Ensures that each severity is present
    private static void enrich(Map<OnmsSeverity, Long> severityMap) {
        severityMap.putIfAbsent(OnmsSeverity.NORMAL, 0L);
        severityMap.putIfAbsent(OnmsSeverity.WARNING, 0L);
        severityMap.putIfAbsent(OnmsSeverity.MINOR, 0L);
        severityMap.putIfAbsent(OnmsSeverity.MAJOR, 0L);
        severityMap.putIfAbsent(OnmsSeverity.CRITICAL, 0L);
    }
}
